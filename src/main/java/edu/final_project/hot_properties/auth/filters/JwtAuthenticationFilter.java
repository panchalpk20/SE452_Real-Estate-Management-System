package edu.final_project.hot_properties.auth.filters;

import edu.final_project.hot_properties.auth.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // log.info("JWT filter on URI: {}", request.getRequestURI());

        String path = request.getRequestURI();

        if (path.startsWith("/login") || path.startsWith("/css") || path.startsWith("/register")
                || path.startsWith("/images")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Extract token from cookie (named "jwt")
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Skip JWT logic if token is missing or empty
        if (token == null || token.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractUsername(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (token != null && jwtUtil.validateToken(token, userDetails)) {
            // String username = jwtUtil.extractUsername(token);
            // UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            String email1 = jwtUtil.extractUsername(token);
            Map<String, Object> claims = jwtUtil.extractAllClaims(token); // You need this method in your JwtUtil

            log.info("----------JWT Claims for {}: {}", email, claims);

            // logging user
            String roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("none");
            log.info("JWT Authenticated: {} | Roles: {} | URI: {}", userDetails.getUsername(), roles,
                    request.getRequestURI());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
