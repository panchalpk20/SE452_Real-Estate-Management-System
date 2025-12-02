
package edu.final_project.hot_properties.entities;

import edu.final_project.hot_properties.dtos.AgentDto;
import edu.final_project.hot_properties.dtos.RegisterDto;
import edu.final_project.hot_properties.exceptions.InvalidParameterException;
import edu.final_project.hot_properties.exceptions.InvalidUserParameterException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password; // Encrypted password

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Timestamp of when the user was created

    // One-to-many: properties listed by the user (for Agents)
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Property> properties;

    // One-to-many: messages sent and received (for Agents and Buyers)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> sentMessages;

    // One-to-many: favorited properties (for Buyers)
    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Favorite> favorites;

    // Constructor

    public User() {
    }

    public User(AgentDto dto, Role role) {
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.email = dto.getEmail();
//        this.password = dto.getPassword();
        this.role = role;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public User(RegisterDto dto, Role role) {
        this.setFirstName(dto.getFirstName());
        this.setLastName(dto.getLastName());
        this.setEmail(dto.getEmail());
        this.setPassword(dto.getPassword());
        setRole(role);
        setCreatedAt();
        properties = new ArrayList<>();
        sentMessages = new ArrayList<>();
        favorites = new ArrayList<>();
    }

    public User(String firstName, String lastName, String email, String password, Role role) {
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPassword(password);
        setRole(role);
        setCreatedAt();
        properties = new ArrayList<>();
        sentMessages = new ArrayList<>();
        favorites = new ArrayList<>();
    }

    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    /// Utility functions to manage relationships
    // property add
    public void addProperty(Property property) {
        properties.add(property);
        property.setAgent(this);
    }

    // property remove
    public void removeProperty(Property property) {
        // TODO: Change this to adapt removal process
        properties.remove(property);
        property.setAgent(null);
    }

    // add message
    public void addSentMessage(Message message) {
        sentMessages.add(message);
        message.setSender(this);
    }

    // delete message
    public void removeSentMessage(Message message) {
        sentMessages.remove(message);
        message.setSender(null);
    }

    // Add a favorite
    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
        favorite.setBuyer(this);
    }

    // Remove a favorite
    public void removeFavorite(Favorite favorite) {
        favorites.remove(favorite);
        favorite.setBuyer(null);
    }

    /// getters

    // TODO: Remove unwanted
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    /// Setters

    public void setFirstName(String firstName) {
        if (firstName == null)
            throw new InvalidUserParameterException("First name cannot be null");

        if (firstName.trim().isEmpty())
            throw new InvalidUserParameterException("First name cannot be empty");

        if (!firstName.matches("[a-zA-Z]+"))
            throw new InvalidUserParameterException("First Name must contain Alphabets");

        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {

        if (lastName == null)
            throw new InvalidParameterException("last name cannot be null");

        if (lastName.trim().isEmpty())
            throw new InvalidParameterException("last name cannot be empty");

        if (!lastName.matches("[a-zA-Z]+"))
            throw new InvalidUserParameterException("Last Name must contain Alphabets");

        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        if (email == null)
            throw new InvalidParameterException("email cannot be null");

        if (email.trim().isEmpty())
            throw new InvalidParameterException("email cannot be empty");

        if (!email.contains("@") || !email.contains("."))
            throw new InvalidParameterException("Invalid email: Email seems to be missing '@' or '.'");

        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null) {
            throw new InvalidParameterException("Password cannot be null");
        }
        if (password.trim().isEmpty()) {
            throw new InvalidParameterException("Password cannot be empty");
        }

        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Message> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(List<Message> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }
}
