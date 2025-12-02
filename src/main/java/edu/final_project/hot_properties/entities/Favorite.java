package edu.final_project.hot_properties.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-one: user who favorited a property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User buyer; // The User that added the favorite entry

    // Many-to-one: the favorited property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property; // The property that this favorite is related

    @Column(name = "created_at")
    private LocalDateTime createdAt; // when favorite was created

    //Constructors

    public Favorite() {
    }

    public Favorite(User buyer, Property property) {
        setBuyer(buyer);
        setProperty(property);
        setCreatedAt();
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public void setProperty(Property property) {

        this.property = property;
    }

    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    ///Utility setters
    public void setFavoriteBuyer(User buyer) {
        setBuyer(buyer);
        buyer.getFavorites().add(this);
    }

    public void setFavoriteProperty(Property property) {
        setProperty(property);
        property.getFavorites().add(this);
    }

    /// getters

    public Long getId() {
        return id;
    }

    public Property getProperty() {
        return property;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


}