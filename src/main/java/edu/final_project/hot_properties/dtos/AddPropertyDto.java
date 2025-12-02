package edu.final_project.hot_properties.dtos;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class AddPropertyDto {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title cannot exceed 255 characters")
    @Pattern(regexp = ".*[^0-9].*", message = "Title cannot consist solely of numbers")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description cannot exceed 2000 characters")
    @Pattern(regexp = ".*[^0-9].*", message = "Description cannot consist solely of numbers")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotBlank(message = "Zip Code is required")
    @Size(min = 5, max = 5, message = "Zip Code must be 5 digits")
    @Pattern(regexp = "^\\d{5}$", message = "Zip Code must be 5 digits")
    private String zipCode;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "1.00", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Size is required")
    @Min(value = 100, message = "Size must be at least 100 sq ft")
    @Max(value = 100000000, message = "Size cannot exceed 100,000,000 sq ft")
    private Integer size;

    // for images
    private List<MultipartFile> files;

    public AddPropertyDto(String title, Double price, String location, String zipCode, Integer size, String description) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.zipCode = zipCode;
        this.size = size;
        this.price = price;
    }

    public AddPropertyDto() {

    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
