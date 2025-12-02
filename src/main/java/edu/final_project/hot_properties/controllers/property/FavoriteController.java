package edu.final_project.hot_properties.controllers.property;

import edu.final_project.hot_properties.entities.Property;
import edu.final_project.hot_properties.exceptions.InvalidFavoriteParameterException;
import edu.final_project.hot_properties.services.FavoriteService;
import edu.final_project.hot_properties.services.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;
    private final PropertyService propertyService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService, PropertyService propertyService) {
        this.favoriteService = favoriteService;
        this.propertyService = propertyService;
    }

    // add to favorites
    @PreAuthorize("hasAuthority('BUYER')")
    @PostMapping("/add")
    public String addFavorite(@RequestParam("propertyId") Long propertyId,
            RedirectAttributes redirectAttributes) {

        if (!propertyService.exist(propertyId)) {
            // throw exception
            logger.warn("No property with id {}", propertyId);
            redirectAttributes.addFlashAttribute("errorMessage", "Property with ID " + propertyId + " not found.");
            return "redirect:/properties/list";
        }

        try {
            favoriteService.addPropertyToFavorites(propertyId);
            redirectAttributes.addFlashAttribute("successMessage", "Property added to favorites!");
        } catch (InvalidFavoriteParameterException e) {
            logger.error("Error adding property {} to favorites: {}", propertyId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/properties/view/" + propertyId;
    }

    // remove from favorites
    @PreAuthorize("hasAuthority('BUYER')")
    @PostMapping("/remove")
    public String removeFavorite(@RequestParam("propertyId") Long propertyId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = favoriteService.removePropertyFromFavorites(propertyId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Property removed from favorites :( ");
            } else {
                logger.warn("Favorite not found.");
                redirectAttributes.addFlashAttribute("errorMessage", "Property not found in your favorites.");
            }
        } catch (InvalidFavoriteParameterException e) {
            logger.error("Error removing property {} from favorites: {}", propertyId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred removing property {} from favorites: {}", propertyId,
            e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/properties/view/" + propertyId;
    }
    
    @PreAuthorize("hasAuthority('BUYER')")
    @GetMapping("/favorites")
    public String viewMyFavorites(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Property> favorites = favoriteService.getMyFavorites();
            model.addAttribute("favorites", favorites);
            return "property/view-favorites";
        } catch (InvalidFavoriteParameterException e) {
            logger.error("Error retrieving favorites: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/properties/list";
        }
    }
    
    @PreAuthorize("hasAnyAuthority('BUYER', 'AGENT')")
    @GetMapping("/is-favorite/{propertyId}")
    public boolean isPropertyFavorited(@PathVariable Long propertyId) {
        try {
            // check favorite status
            return favoriteService.isPropertyFavoritedByUser(propertyId);
        } catch (InvalidFavoriteParameterException e) {
            logger.warn("Error checking favorite status for property {}: {}", propertyId, e.getMessage());
            throw new InvalidFavoriteParameterException("Unable to check for favorite.");
        }
    }
}
