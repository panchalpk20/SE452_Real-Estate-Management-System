package edu.final_project.hot_properties.controllers.login;

import edu.final_project.hot_properties.dtos.LoginRequestDto;
import edu.final_project.hot_properties.dtos.RegisterDto;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.exceptions.AlreadyExistsException;
import edu.final_project.hot_properties.exceptions.BadParameterException;
import edu.final_project.hot_properties.services.AuthService;
import edu.final_project.hot_properties.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/// / Login and register with JWT
@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final AuthService authService;
    private final UserService userService;
    @Value("${app.version:dev}")
    private String appVersion;

    @Autowired
    public LoginController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping({"/", "/index"})
    public String showIndex(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            log.info("User already logged in.");
            return "redirect:/dashboard";
        }
        model.addAttribute("version", appVersion);
        return "dashboard/landingPage";

    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            log.info("User already logged in.");
            return "redirect:/dashboard";
        }
        model.addAttribute("loginRequest", new LoginRequestDto());
        return "login/loginPage";

    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginRequest") LoginRequestDto loginRequest,
                               BindingResult bindingResult,
                               Model model,
                               HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            log.warn("Login form has errors: {}", bindingResult.getAllErrors());
            return "login/loginPage";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            log.info("User already logged in.");
            return "redirect:/dashboard";
        }

        try {
            log.info("Attempting login for {}", loginRequest);
            Cookie jwtCookie = authService.loginAndCreateJwtCookie(loginRequest);
            response.addCookie(jwtCookie);
            log.info("Login Successful for {}", loginRequest);
            return "redirect:/dashboard";
        } catch (BadCredentialsException | BadParameterException e) {
            log.warn("Login failed for {}: {}", loginRequest.getEmail(), e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "login/loginPage";
        }
    }

    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/logout1")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {

        authService.clearJwtCookie(response);
        redirectAttributes.addFlashAttribute("message", "You have been logged out.");
        log.info("User logged out successfully.");
        return "redirect:/login";

    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {

        model.addAttribute("RegisterDto", new RegisterDto());
        return "login/registerPage";

    }

    @PostMapping("/register")
    public String registerUse(
            @Valid @ModelAttribute("RegisterDto") RegisterDto newUser,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Registration form has errors: {}", bindingResult.getAllErrors());
            return "login/registerPage";
        }

        try {
            log.info("Attempting to register new buyer {}", newUser);
            User savedUser = userService.registerNewUser(newUser);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful.");
            log.info("Registration successful for {}", newUser.getEmail());
            return "redirect:/login";
        } catch (AlreadyExistsException e) {
            log.warn("Registration failed for {}: {}", newUser.getEmail(), e.getMessage());
            bindingResult.rejectValue("email", "email.duplicate", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "login/registerPage";
        }
    }
}
