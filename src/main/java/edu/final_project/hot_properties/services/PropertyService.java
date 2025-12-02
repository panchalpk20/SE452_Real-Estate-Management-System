package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.dtos.AddPropertyDto;
import edu.final_project.hot_properties.dtos.PropertyFilterDto;
import edu.final_project.hot_properties.entities.Property;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PropertyService {

    // browsing with filters
    List<Property> getFilteredAndSortedProperties(PropertyFilterDto propertyFilterDto);

    // property by id
    Property getPropertyById(Long id);

    // agents add new property
    void createProperty(Property property);

    // agents update existing property
    void updateProperty(Long id, AddPropertyDto addPropertyDto, List<MultipartFile> newFiles) throws IOException;

    // agents/admins to delete a property
    void deleteProperty(Long id);

    void addPropertyFromDto(AddPropertyDto dto, List<MultipartFile> files);

    Property findById(Long id);

    void deletePropertyImage(Long propertyId, Long imageId);

    void favouritePropertyById(Long propertyId);

    Boolean exist(Long propertyId);

    List<Property> getPropertiesByAgent(Long id);
}
