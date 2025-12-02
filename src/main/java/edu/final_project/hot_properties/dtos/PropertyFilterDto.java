package edu.final_project.hot_properties.dtos;

public class PropertyFilterDto {
    private String zipCode;
    private Integer minSqft;
    private Double minPrice;
    private Double maxPrice;
    private String sortOrder;

    // Getters and setters
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Integer getMinSqft() {
        return minSqft;
    }
    public void setMinSqft(Integer minSqft) {
        this.minSqft = minSqft;
    }

    public Double getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }
    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}