package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.entities.Favorite;
import edu.final_project.hot_properties.entities.Property;
import edu.final_project.hot_properties.entities.RoleEnum;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.exceptions.InvalidFavoriteParameterException;
import edu.final_project.hot_properties.exceptions.InvalidPropertyParameterException;
import edu.final_project.hot_properties.repositories.FavoriteRepository;
import edu.final_project.hot_properties.repositories.PropertyRepository;
import edu.final_project.hot_properties.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final FavoriteRepository favoriteRepository;

    private static final Logger logger = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    @Autowired
    public FavoriteServiceImpl(UserRepository userRepository, PropertyRepository propertyRepository, FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.favoriteRepository = favoriteRepository;
    }

    private User getCurrentAuthenticatedUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.error("Authenticated user with email {} not found in database.", userEmail);
                    return new InvalidFavoriteParameterException("Authenticated user not found.");
                });
    }

    private boolean isCurrentUserBuyer(User user) {
        return user != null && user.getRole() != null && user.getRole().getName().equals(RoleEnum.BUYER.name());
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @Override
    @Transactional
    public Favorite addPropertyToFavorites(Long propertyId) {
        User user = getCurrentAuthenticatedUser();
        
        if (propertyId == null) {
            logger.warn("Attempted to add a favorite with null property ID by user {}.", user.getEmail());
            throw new InvalidFavoriteParameterException("Property ID cannot be null.");
        }
        
        Property property = propertyRepository.findById(propertyId)
        .orElseThrow(() -> new InvalidPropertyParameterException("Property ID cannot be null."));
        
        Favorite alreadyFavorited = favoriteRepository.findByBuyerAndProperty(user, property);
        
        if (alreadyFavorited != null) {
            logger.warn("User {} attempted to add property {} to favorites, but it is already favorited.", user.getEmail(), propertyId);
            throw new InvalidFavoriteParameterException("Property is already in favorites.");
        }
        
        Favorite favorite = new Favorite();
        favorite.setBuyer(user);
        favorite.setProperty(property);
        favorite.setCreatedAt();
        
        Favorite savedFavorite = favoriteRepository.save(favorite);
        logger.info("Property {} added to favorites by user {}.",propertyId, user.getEmail());
        return savedFavorite;
    }
    
    @PreAuthorize("hasAuthority('BUYER')")
    @Override
    @Transactional
    public boolean removePropertyFromFavorites(Long propertyId) {
        User currentUser = getCurrentAuthenticatedUser();
        
        if (propertyId == null) {
            throw new InvalidFavoriteParameterException("Property ID cannot be null.");
        }
        
        Property property = propertyRepository.findById(propertyId)
        .orElseThrow(() -> new InvalidFavoriteParameterException("Property with ID " + propertyId + " not found."));
        
        Favorite favorite = favoriteRepository.findByBuyerAndProperty(currentUser, property);
        
        if (favorite != null) {
            currentUser.removeFavorite(favorite);
            userRepository.save(currentUser);
            logger.info("Property {} removed from favorites by user {}.", propertyId, currentUser.getEmail());
            return true;
        } else {
            logger.warn("Attempted to remove favorite for property {} by user {}, but it was not found.", propertyId, currentUser.getEmail());
            return false;
        }
    }
    
    @PreAuthorize("hasAnyAuthority('BUYER', 'AGENT')")
    @Override
    public boolean isPropertyFavoritedByUser(Long propertyId) {
        User currentUser = getCurrentAuthenticatedUser();
        
        if (!isCurrentUserBuyer(currentUser)) {
            logger.warn("User {} (role: {}) attempted to check favorite status for property {}, but only BUYERS can check.",
            currentUser.getEmail(), currentUser.getRole().getName(), propertyId);
            throw new InvalidFavoriteParameterException("Only buyers can check favorite status for properties.");
        }
        
        if (propertyId == null) {
            throw new InvalidFavoriteParameterException("Property ID cannot be null.");
        }
        
        Property property = propertyRepository.findById(propertyId)
        .orElseThrow(() -> new InvalidFavoriteParameterException("Property with ID " + propertyId + " not found."));
        
        return favoriteRepository.findByBuyerAndProperty(currentUser, property) != null;
    }
    
    @PreAuthorize("hasAuthority('BUYER')")
    @Override
    public List<Property> getMyFavorites() {
        User currentUser = getCurrentAuthenticatedUser();

        if (!isCurrentUserBuyer(currentUser)) {
            logger.warn("User {} (role: {}) attempted to retrieve favorites, but only BUYERS can do so.",
            currentUser.getEmail(), currentUser.getRole().getName());
            throw new InvalidFavoriteParameterException("Only buyers can retrieve their favorite properties.");
        }
        
        return favoriteRepository.findFavoritedPropertiesByUserId(currentUser.getId());
    }
}
