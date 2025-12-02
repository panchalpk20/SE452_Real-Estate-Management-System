package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.dtos.AgentDto;
import edu.final_project.hot_properties.dtos.CurrentUserContext;
import edu.final_project.hot_properties.dtos.EditProfileDto;
import edu.final_project.hot_properties.dtos.RegisterDto;
import edu.final_project.hot_properties.entities.*;
import edu.final_project.hot_properties.exceptions.AlreadyExistsException;
import edu.final_project.hot_properties.exceptions.InvalidOperationException;
import edu.final_project.hot_properties.exceptions.InvalidUserParameterException;
import edu.final_project.hot_properties.exceptions.NotFoundException;
import edu.final_project.hot_properties.repositories.PropertyRepository;
import edu.final_project.hot_properties.repositories.RoleRepository;
import edu.final_project.hot_properties.repositories.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PropertyRepository propertyRepository;

    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, PropertyRepository propertyRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.propertyRepository = propertyRepository;
    }

    @Override
    public CurrentUserContext getCurrentUserContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User is not logged in for email: " + email));
        return new CurrentUserContext(user, auth);
    }

    @Override
    public User registerNewUser(RegisterDto registerDto) throws AlreadyExistsException {
        logger.info("Registering new user with email: {}", registerDto.getEmail());
        Role role = roleRepository.findByName(RoleEnum.BUYER.toString())
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.BUYER));

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            logger.warn("User registration failed: User already exists with email {}", registerDto.getEmail());
            throw new AlreadyExistsException("User already exist with email " + registerDto.getEmail());
        }
        // encode password
        String encodedPass = passwordEncoder.encode(registerDto.getPassword());

        logger.info("Password for user {} encoded successfully.", registerDto.getEmail());
        // registering user
        User user = new User(registerDto, role);
        user.setPassword(encodedPass);
        logger.info("Saving new user to the repository: {}", registerDto.getEmail());
        userRepository.save(user);
        logger.info("User registered successfully: {}", registerDto.getEmail());
        return user;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public User registerNewAgent(RegisterDto agentDto) {
        Role role = roleRepository.findByName(RoleEnum.AGENT.toString())
                .orElseThrow(() -> new NotFoundException("Role not found: " + RoleEnum.AGENT));

        logger.info("Registering new agent with email: {}", agentDto.getEmail());
        if (userRepository.existsByEmail(agentDto.getEmail())) {
            logger.warn("User registration failed: User already exists with email {}", agentDto.getEmail());
            throw new AlreadyExistsException("User already exist with email " + agentDto.getEmail());
        }

        // encode password
        String encodedPass = passwordEncoder.encode(agentDto.getPassword());
        logger.info("Password for agent {} encoded successfully.", agentDto.getEmail());

        User newAgent = new User(agentDto, role);
        newAgent.setPassword(encodedPass);

        logger.info("Saving new agent to the repository: {}", agentDto.getEmail());
        userRepository.save(newAgent);
        logger.info("Agent registered successfully: {}", agentDto.getEmail());
        return newAgent;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public void prepareDashboardModel(Model model) {
        logger.info("Preparing dashboard model for the current user.");
        CurrentUserContext context = getCurrentUserContext();
        model.addAttribute("user", context.user());
        model.addAttribute("authorization", context.auth());
        model.addAttribute("userRole", context.user().getRole());
        logger.debug("Dashboard model prepared with user: {}", context.user().getEmail());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    @Override
    public void deleteUserByEmail(String email) {
        logger.info("Attempting to delete user with email: {}", email);
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Attempted to delete user with null or empty email.");
            throw new InvalidUserParameterException("Email cannot be null or empty");
        }

        User currentUser = getCurrentUserContext().user();
        if (currentUser.getEmail().equalsIgnoreCase(email)) {
            logger.warn("Attempted to delete own account while logged in: {}", email);
            throw new InvalidOperationException("You cannot delete your own account while logged in.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No user with email " + email));

        logger.info("User found for deletion: {}", user.getEmail());



        // Remove all favorites associated with the user before deleting
        for (Favorite favorite : new ArrayList<>(user.getFavorites())) {
            logger.info("Removing favorite property with ID {} for user {}", favorite.getProperty().getId(),
                    user.getEmail());
            user.removeFavorite(favorite);
        }

        // if its agent ddelete all propertiess related to iit

        // If agent, delete all properties and their relations
        if (user.getRole().getName().equalsIgnoreCase("AGENT")) {
            List<Property> properties = propertyRepository.findAllByAgentId(user.getId());
            for (Property property : properties) {
                // Remove all favorites for this property
                for (Favorite favorite : new ArrayList<>(property.getFavorites())) {
                    User buyer = favorite.getBuyer();
                    if (buyer != null) {
                        buyer.removeFavorite(favorite);
                        userRepository.save(buyer);
                    }
                }
                // Remove all messages for this property
                for (Message message : new ArrayList<>(property.getMessages())) {
                    User sender = message.getSender();
                    if (sender != null) {
                        sender.removeSentMessage(message);
                        userRepository.save(sender);
                    }
                }
                // Remove property from agent's list
                user.removeProperty(property);
                propertyRepository.delete(property);
            }
        }

        userRepository.delete(user);
        logger.info("User with email {} deleted successfully.", email);
    }

    @PreAuthorize("hasAnyAuthority('BUYER', 'AGENT', 'ADMIN')")
    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updateUserByEmail(String email, AgentDto userDto) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Attempted to update user with null or empty email.");
            throw new InvalidUserParameterException("Email cannot be null or empty");
        }
        if (userDto == null) {
            logger.warn("Attempted to update user with null DTO.");
            throw new InvalidUserParameterException("User DTO cannot be null");
        }
        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
            logger.warn("Attempted to update user with null or empty first name.");
            throw new InvalidUserParameterException("Firstname cannot be null or empty");
        }
        User user = findUserByEmail(email); // throws not found exception if user does not exist
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        logger.info("Updating user with email: {}", email);
        userRepository.save(user);
        logger.info("User with email {} updated successfully.", email);
    }

    @Override
    public void prepareProfileModel(Model model) {
        CurrentUserContext context = getCurrentUserContext();
        model.addAttribute("user", context.user());

    }

    @Override
    public User updateProfile(EditProfileDto updatedDto) {
        if (updatedDto == null) {
            throw new InvalidUserParameterException("Update DTO is null");
        }
        if (updatedDto.getFirstName() == null || updatedDto.getFirstName().trim().isEmpty()) {
            throw new InvalidUserParameterException("Firstname cannot be null or empty");
        }

        if (updatedDto.getLastName() == null || updatedDto.getLastName().trim().isEmpty()) {
            throw new InvalidUserParameterException("Lastname cannot be null empty");
        }

        String currUserEmail = getCurrentUserContext().user().getEmail();
        User existingUser = userRepository.findByEmail(currUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with not found with email " + currUserEmail));

        existingUser.setFirstName(updatedDto.getFirstName());
        existingUser.setLastName(updatedDto.getLastName());
        return userRepository.save(existingUser);

    }

    @Override
    public List<User> getAllAgents() {
        return userRepository.findUsersByAgentRole();
    }

    @Override
    public List<User> getAllAdmins() {
        return userRepository.findUsersByAdminRole();
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void updateAgent(AgentDto agentDto) {
        User user = userRepository.findByEmail(agentDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + agentDto.getEmail()));

        user.setFirstName(agentDto.getFirstName());
        user.setLastName(agentDto.getLastName());
        userRepository.save(user);
    }

}
