package edu.final_project.hot_properties.entities;

import edu.final_project.hot_properties.exceptions.InvalidPropertyParameterException;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", unique = true)
    private String title;

    @Column(name = "price")
    private Double price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location")
    private String location; // City, state, zip

    @Column(name = "zipCode")
    private String zipCode;

    @Column(name = "size")
    private Integer size; // Property size in sq feet

//    @Column(nullable = false)
//    private Integer favoritesCount;

    // Many-to-one: the Agent who listed the property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;

    // One-to-many: images of the property
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<PropertyImage> images;

    // Many-to-many: users who have favorited this property
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Favorite> favorites;

    // One-to-many: messages related with this property
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    //Constructors
    public Property() {
        this.messages = new ArrayList<>();
        this.favorites = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    public Property(String title, Double price, String description, String location, String zipCode, Integer size) {
        setTitle(title);
        setPrice(price);
        setDescription(description);
        setLocation(location);
        setZipCode(zipCode);
        setSize(size);
        setAgent(agent);
        this.messages = new ArrayList<>();
        this.favorites = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    // used @Transient to avoid mapping to a database column.
    @Transient
    public int getFavoritesCount() {
        return this.favorites != null ? this.favorites.size() : 0;
    }

    /// Setters
    public void setAgent(User agent) {
        this.agent = agent;
    }

    public void setTitle(String title) {
        if (title == null) {
            throw new InvalidPropertyParameterException("Title cannot be null");
        }
        if (title.trim().isEmpty()) {
            throw new InvalidPropertyParameterException("Title cannot be empty");
        }
        this.title = title;
    }

    public void setPrice(Double price) {
        if (price == null) {
            throw new InvalidPropertyParameterException("Price cannot be null");
        }
        if (price <= 0) {
            throw new InvalidPropertyParameterException("Price must be greater than zero");
        }
        this.price = price;
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new InvalidPropertyParameterException("Description cannot be null");
        }
        if (description.trim().isEmpty()) {
            throw new InvalidPropertyParameterException("Description cannot be empty");
        }
        this.description = description;
    }

    public void setLocation(String location) {
        if (location == null) {
            throw new InvalidPropertyParameterException("Location cannot be null");
        }
        if (location.trim().isEmpty()) {
            throw new InvalidPropertyParameterException("Location cannot be empty");
        }
        this.location = location;
    }

    public void setZipCode(String zipCode) {
        if (zipCode == null) {
            throw new InvalidPropertyParameterException("Zip code cannot be null");
        }
        if (zipCode.trim().isEmpty()) {
            throw new InvalidPropertyParameterException("Zip code cannot be empty");
        }
        this.zipCode = zipCode;
    }

    public void setSize(Integer size) {
        if (size == null) {
            throw new InvalidPropertyParameterException("Size cannot be null");
        }
        if (size <= 0) {
            throw new InvalidPropertyParameterException("Size must be greater than zero");
        }
        this.size = size;
    }

    public void setImages(List<PropertyImage> images) {
        if (images == null) {
            throw new InvalidPropertyParameterException("Images set cannot be null");
        }
        for (PropertyImage image : images) {
            image.setProperty(this);
        }
    }

    public void setFavorites(List<Favorite> favorites) {
        if (favorites == null || favorites.isEmpty()) {
            throw new InvalidPropertyParameterException("Favorites cannot be null or negative.");
        }
        this.favorites = favorites;
    }

    public void setMessages(List<Message> messages) {
        if (messages == null) {
            throw new InvalidPropertyParameterException("Messages set cannot be null");
        }
        this.messages = messages;
    }


    /// Utility setter functions
    public void addImage(PropertyImage image) {
        images.add(image);
        image.setProperty(this);  // Set the property reference in the image
    }

    public void removeImage(PropertyImage image) {

        images.remove(image);
        image.setProperty(null);  // Remove the reference to the property in the image
    }

    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
        favorite.setProperty(this);  // Set the property reference in the favorite
    }

    public void removeFavorite(Favorite favorite) {
        favorites.remove(favorite);
        favorite.setProperty(null);  // Remove the reference to the property in the favorite
    }

    // Add a message to the property
    public void addMessage(Message message) {
        messages.add(message);
        message.setMessageProperty(this);  // Set the property reference in the message
    }

    // Remove a message from the property
    public void removeMessage(Message message) {
        messages.remove(message);
        message.setMessageProperty(null);  // Remove the reference to the property in the message
    }


    /// Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getZipCode() {
        return zipCode;
    }

    public Integer getSize() {
        return size;
    }

    public User getAgent() {
        return agent;
    }

    public List<PropertyImage> getImages() {
        return images;
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
