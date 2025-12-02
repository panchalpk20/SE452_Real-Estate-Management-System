package edu.final_project.hot_properties.entities;


import edu.final_project.hot_properties.exceptions.InvalidPropertyImageParameterException;
import jakarta.persistence.*;

@Entity
@Table(name = "property_images")
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_filename")
    private String imageFilename;

    // Many-to-one: associated property
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "property_id")
    private Property property;


    public PropertyImage() {
    }

    public PropertyImage(String imageFilename, Property property) {
        setImageFilename(imageFilename);
        setProperty(property);
    }


    /// Setters
    public void setProperty(Property property) {
        this.property = property;
    }

    public void setImageFilename(String imageFilename) {
        if(imageFilename == null || imageFilename.trim().isEmpty())
            throw new InvalidPropertyImageParameterException("Image file name cannot be null or empty.");
        this.imageFilename = imageFilename;
    }

    //utility setters
    public void setPropertyForImage(Property property) {
        setProperty(property);
        property.getImages().add(this);
    }

    /// Getters
    public Long getId() {
        return id;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public Property getProperty() {
        return property;
    }


}