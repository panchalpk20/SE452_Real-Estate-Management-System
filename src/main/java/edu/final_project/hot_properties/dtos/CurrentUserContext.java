package edu.final_project.hot_properties.dtos;

import edu.final_project.hot_properties.entities.User;
import org.springframework.security.core.Authentication;

public record CurrentUserContext(User user, Authentication auth) {}
