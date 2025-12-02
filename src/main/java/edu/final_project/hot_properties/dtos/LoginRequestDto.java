package edu.final_project.hot_properties.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDto {

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Size(min = 4, max = 50, message = "email address must be between 4 and 50 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 5, max = 20, message = "Password must be between 5 and 200 characters.")
    private String password;

    // Default constructor
    public LoginRequestDto() {
    }

    // Parameterized constructor
    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter and Setter methods
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}