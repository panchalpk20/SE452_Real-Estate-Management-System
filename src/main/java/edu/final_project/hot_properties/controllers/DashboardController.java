package edu.final_project.hot_properties.controllers;

import edu.final_project.hot_properties.services.AuthService;
import edu.final_project.hot_properties.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public DashboardController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping({ "/dashboard" })
    public String showIndex(Model model) {
        userService.prepareDashboardModel(model);
        return "dashboard/dashboardPage";
    }



}
