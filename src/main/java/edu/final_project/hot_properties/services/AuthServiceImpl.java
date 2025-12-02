package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.auth.jwt.JwtUtil;
import edu.final_project.hot_properties.dtos.JwtResponse;
import edu.final_project.hot_properties.dtos.LoginRequestDto;
import edu.final_project.hot_properties.exceptions.BadParameterException;
import edu.final_project.hot_properties.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    //logger
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public JwtResponse authenticateAndGenerateToken(LoginRequestDto user) {
        log.info("Attempting authentication for user: {}", user != null ? user.getEmail() : "null");
        if(user == null) {
            log.warn("Login attempt failed: User object is null");
            throw new BadParameterException("User cannot be null");
        }
        if(user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.warn("Login attempt failed: Email is null or empty");
            throw new BadParameterException("email cannot be null or empty.");
        }
        if(user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            log.warn("Login attempt failed: Password is null or empty");
            throw new BadParameterException("password cannot be null or empty.");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            log.info("Authentication successful for user: {}", user.getEmail());
            return new JwtResponse(token);

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", user.getEmail());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public Cookie loginAndCreateJwtCookie(LoginRequestDto loginRequest) throws BadCredentialsException, BadParameterException {
        log.info("Attempting to log in and create JWT cookie for user: {}", loginRequest != null ? loginRequest.getEmail() : "null");

        JwtResponse jwtResponse = authenticateAndGenerateToken(loginRequest);

        Cookie jwtCookie = new Cookie("jwt", jwtResponse.getToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour

        log.info("JWT cookie created for user: {}", loginRequest.getEmail());

        return jwtCookie;
    }

    @Override
    public void clearJwtCookie(HttpServletResponse response) {
        log.info("Clearing JWT cookie");
        Cookie cookie = new Cookie("jwt", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        // cookie.setSecure(true); // only if your app uses HTTPS
        response.addCookie(cookie);
        log.info("JWT cookie cleared and added to response");
    }

}
