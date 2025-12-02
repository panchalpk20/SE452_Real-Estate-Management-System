package edu.final_project.hot_properties.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import edu.final_project.hot_properties.exceptions.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex, Model model, HttpServletRequest request) {
        logger.error("404 Not Found: {}", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "The page you are looking for does not exist. " + ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserLoggedOut(UsernameNotFoundException ex, Model model, HttpServletRequest request) {
        logger.error("User not found or logged out: {}", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "You have been logged out. " + ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(RuntimeException ex, Model model, HttpServletRequest request) {
        logger.error("Unhandled exception at {}: ", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. " + ex.getMessage());
        model.addAttribute("status", 500);
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllUncaughtExceptions(Throwable ex, Model model, HttpServletRequest request) {
        logger.error("Uncaught exception at {}: ", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "A critical error occurred. Please contact support.");
        model.addAttribute("status", 500);
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }
}