package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.dtos.AgentDto;
import edu.final_project.hot_properties.dtos.CurrentUserContext;
import edu.final_project.hot_properties.dtos.EditProfileDto;
import edu.final_project.hot_properties.dtos.RegisterDto;
import edu.final_project.hot_properties.entities.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.aspectj.weaver.loadtime.Agent;
import org.springframework.ui.Model;

import java.util.List;

public interface UserService {

    CurrentUserContext getCurrentUserContext();

    User registerNewUser(RegisterDto user);

    void prepareDashboardModel(Model model);

    void prepareProfileModel(Model model);

    @Transactional
    User updateProfile(EditProfileDto updatedDto);

    @Transactional
    void deleteUserByEmail(String email);

    User findUserByEmail(String email);

    boolean existsByEmail(String email);

    @Transactional
    void updateUserByEmail(String email, AgentDto userDto);

    // @Transactional
    // void updateUser(@Valid User userDto);

    List<User> getAllAgents();

    List<User> getAllAdmins();

    List<User> getAll();

    User registerNewAgent(RegisterDto agentDto);

    // throws userNotFound exception
    void updateAgent(AgentDto agentDto);

}
