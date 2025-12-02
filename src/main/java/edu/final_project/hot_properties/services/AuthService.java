package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.dtos.JwtResponse;
import edu.final_project.hot_properties.dtos.LoginRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

public interface AuthService {

    JwtResponse authenticateAndGenerateToken(LoginRequestDto user);

    Cookie loginAndCreateJwtCookie(LoginRequestDto loginRequest) throws BadCredentialsException;

    //logout
    void clearJwtCookie(HttpServletResponse response);

}
