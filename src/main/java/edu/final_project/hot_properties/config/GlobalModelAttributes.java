package edu.final_project.hot_properties.config;

import edu.final_project.hot_properties.dtos.PropertyFilterDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/// putting attributes in model to be used in header

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("userRole")
    public String getUserRole() {
        String role = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getAuthorities().isEmpty()) {
            role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            if (role.contains("ANONYMOUS"))
                role = null;
        }
        return role;
    }

    @ModelAttribute("currentUrl")
    public String getCurrentUrl(HttpServletRequest request) {
        return request.getRequestURI();
    }

    // this will be always present in the model
    @ModelAttribute("propertyFilterDto")
    public PropertyFilterDto propertyFilterDto() {
        return new PropertyFilterDto();
    }

}