package edu.final_project.hot_properties.controllers.property;

import edu.final_project.hot_properties.dtos.AddPropertyDto;
import edu.final_project.hot_properties.dtos.PropertyFilterDto;
import edu.final_project.hot_properties.entities.Property;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.exceptions.InvalidOperationException;
import edu.final_project.hot_properties.exceptions.InvalidPropertyImageParameterException;
import edu.final_project.hot_properties.exceptions.InvalidPropertyParameterException;
import edu.final_project.hot_properties.exceptions.NotFoundException;
import edu.final_project.hot_properties.services.FavoriteService;
import edu.final_project.hot_properties.services.PropertyService;
import edu.final_project.hot_properties.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);
    private final PropertyService propertyService;
    private final FavoriteService favoriteService;
    private final UserService userService;

    @Autowired
    public PropertyController(PropertyService propertyService, FavoriteService favoriteService,
            UserService userService) {
        this.propertyService = propertyService;
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    // need to add for different dashboards

//    @PreAuthorize("hasAuthority('AGENT')")
    @GetMapping("/manage")
    public String manageProperties(Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentAgent = userService.getCurrentUserContext().user();
            List<Property> properties = propertyService.getPropertiesByAgent(currentAgent.getId());
            model.addAttribute("properties", properties);
        } catch (NotFoundException | InvalidOperationException e) {
            logger.error(e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (UsernameNotFoundException e) {
            logger.error(e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard";
        }

        return "property/manage-properties";
    }
    
    // adding property page
//    @PreAuthorize("hasAuthority('AGENT')")
    @GetMapping("/add")
    public String showAddProperty(Model model) { // Removed @Valid and BindingResult from GET mapping
        model.addAttribute("addPropertyDto", new AddPropertyDto());
        return "property/add-property";
    }
    
//    @PreAuthorize("hasAuthority('AGENT')")
    @PostMapping("/add")
    public String addProperty(@ModelAttribute("addPropertyDto") @Valid AddPropertyDto addPropertyDto,
    BindingResult bindingResult,
            @RequestParam("files") List<MultipartFile> files,
            RedirectAttributes redirectAttributes,
            Model model) {
                
                if (bindingResult.hasErrors()) {
                    model.addAttribute("addPropertyDto", addPropertyDto);
                    return "property/add-property"; // Return to form with validation errors
                }
                try {
                    propertyService.addPropertyFromDto(addPropertyDto, files);
                    redirectAttributes.addFlashAttribute("successMessage", "Property added successfully!");
                    return "redirect:/properties/manage";
                } catch (InvalidPropertyImageParameterException e) {
                    logger.error("Error adding property: {}", e.getMessage(), e);
                    redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                    
            return "redirect:/properties/add";
        }
    }
    
    // edit property page
//    @PreAuthorize("hasAuthority('AGENT')")
    @GetMapping("/edit/{id}")
    public String editProperty(@PathVariable Long id, Model model,
    RedirectAttributes redirectAttributes) {
        try {
            Property property = propertyService.getPropertyById(id);
            
            AddPropertyDto addPropertyDto = new AddPropertyDto(
                property.getTitle(),
                property.getPrice(),
                property.getLocation(),
                property.getZipCode(),
                property.getSize(),
                    property.getDescription());
                    
                    model.addAttribute("propertyId", id); // adding id to model
                    model.addAttribute("property", property);
                    model.addAttribute("addPropertyDto", addPropertyDto);
                    return "property/edit-property";
                } catch (NotFoundException e) {
                    logger.error("Error while fetching property for edit: {}", e.getMessage());
                    redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/properties/manage";
        }
    }
    
    // editing property details
//    @PreAuthorize("hasAuthority('AGENT')")
    @PostMapping("/edit/{id}")
    public String updateProperty(@PathVariable Long id, @ModelAttribute @Valid AddPropertyDto addPropertyDto,
    BindingResult bindingResult,
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages,
            Model model,
            RedirectAttributes redirectAttributes) {
                
                // refecthcing property for checkig images present
        logger.info("New images received: {}", (newImages != null ? newImages.size() : "null")); // for debugging
        
        if (bindingResult.hasErrors()) {
            try {
                Property property = propertyService.getPropertyById(id);
                // re-adding property + images
                model.addAttribute("property", property);
                redirectAttributes.addFlashAttribute("successMessage", "Property updated successfully.");
            } catch (NotFoundException e) {
                logger.error("Error reloading property: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", " property.");
                return "redirect:/properties/manage";
            }
            model.addAttribute("propertyId", id);
            model.addAttribute("addPropertyDto", addPropertyDto);
            return "property/edit-property";
        }
        
        try {
            propertyService.updateProperty(id, addPropertyDto, newImages);
            redirectAttributes.addFlashAttribute("successMessage", "Property updated successfully!");
            return "redirect:/properties/manage";
        } catch (NotFoundException | IOException e) {
            logger.error("Error updating property: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // if error occurs returning to edit page
            try {
                Property property = propertyService.getPropertyById(id);
                model.addAttribute("property", property);
            } catch (RuntimeException exception) {
                logger.error("Failed to reload property after update error: {}", exception.getMessage());
                return "redirect:/properties/manage"; // back to manage page
            }
            model.addAttribute("propertyId", id);
            model.addAttribute("addPropertyDto", addPropertyDto);
            return "property/edit-property";
        }
    }

    // delete property
//    @PreAuthorize("hasAuthority('AGENT')")
    @PostMapping("/delete/{id}")
    public String deleteProperty(@PathVariable Long id,
    RedirectAttributes redirectAttributes) {
        try {
            propertyService.deleteProperty(id);
            redirectAttributes.addFlashAttribute("successMessage", "Property deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting property: " + e.getMessage());
        }
        return "redirect:/properties/manage";
    }
    
    // browse properties
    
//    @PreAuthorize("hasAuthority('BUYER')")
    @GetMapping("/list")
    public String listProperties(@ModelAttribute("propertyFilterDto") PropertyFilterDto propertyFilterDto,
    Model model,
    RedirectAttributes redirectAttributes) {

        List<Property> properties;
        try {
            properties = propertyService.getFilteredAndSortedProperties(propertyFilterDto);
        } catch (InvalidPropertyParameterException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/properties/list";
        }

        model.addAttribute("properties", properties);
        model.addAttribute("propertyFilterDto", propertyFilterDto);
        
        return "property/browse-properties";
    }
    
    // view details
//    @PreAuthorize("hasAuthority('BUYER')")
    @GetMapping("/view/{id}")
    public String viewPropertyDetails(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Property property = propertyService.getPropertyById(id);
            model.addAttribute("property", property);
            // checking if this property if favorite for current user
            boolean isFavorite = favoriteService.isPropertyFavoritedByUser(id);
            model.addAttribute("isFavorite", isFavorite);
            return "property/view-details";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/properties/list";
        }
    }
    
    // deleting images
//    @PreAuthorize("hasAuthority('AGENT')")
    @PostMapping("/{propertyId}/images/{imageId}/delete")
    public String deletePropertyImage(@PathVariable Long propertyId, @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {
                
        logger.info("Received request to delete image ID: {} from property ID: {}", imageId, propertyId);
        
        try {
            propertyService.deletePropertyImage(propertyId, imageId);
            logger.info("Image ID: {} successfully deleted.", imageId);
            redirectAttributes.addFlashAttribute("successMessage", "Image deleted successfully.");

        } catch (InvalidPropertyImageParameterException e) {
            logger.error("Error deleting image ID: {} from property ID: {}: {}", imageId, propertyId, e.getMessage(),
            e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting image: " + e.getMessage());
        }
        
        return "redirect:/properties/edit/" + propertyId;
    }
    
}

