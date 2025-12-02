package edu.final_project.hot_properties.controllers;

import edu.final_project.hot_properties.dtos.EditProfileDto;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.exceptions.InvalidUserParameterException;
import edu.final_project.hot_properties.services.UserService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // === PROFILE MAPPING
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String showProfile(Model model) {
        userService.prepareProfileModel(model);
        return "profile/profile";
    }
    
    // === EDIT PROFILE MAPPING
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/editprofile")
    // @PreAuthorize("hasAnyAuthority('USER', 'MANAGER', 'ADMIN')")
    public String editprofile(Model model, RedirectAttributes redirectAttributes) {
        
        EditProfileDto dto = new EditProfileDto();
        User user = null;
        try {
            user = userService.getCurrentUserContext().user();
        } catch (InvalidUserParameterException e) {
            logger.warn("Invalid user parameter when fetching current user context: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Please log in first.");
            return "redirect:/login";
        }
        
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        model.addAttribute("email", user.getEmail());
        
        model.addAttribute("editProfileDto", dto);
        return "profile/editprofile";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("editProfileDto") EditProfileDto editProfileDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            User user = userService.getCurrentUserContext().user();
            model.addAttribute("email", user.getEmail());
            return "profile/editprofile";
        }
        try {
            userService.updateProfile(editProfileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile Updated Successfully");
            return "redirect:/profile";
        } catch (InvalidUserParameterException e) {
            logger.warn("Invalid user parameter when updating profile: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}