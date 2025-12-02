package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.dtos.AddPropertyDto;
import edu.final_project.hot_properties.dtos.PropertyFilterDto;
import edu.final_project.hot_properties.entities.*;
import edu.final_project.hot_properties.exceptions.AlreadyExistsException;
import edu.final_project.hot_properties.exceptions.InvalidPropertyImageParameterException;
import edu.final_project.hot_properties.exceptions.InvalidPropertyParameterException;
import edu.final_project.hot_properties.exceptions.NotFoundException;
import edu.final_project.hot_properties.repositories.PropertyImageRepository;
import edu.final_project.hot_properties.repositories.PropertyRepository;
import edu.final_project.hot_properties.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PropertyServiceImpl implements PropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    @Value("${property.upload.dir}")
    private String UPLOAD_DIR;

    @Autowired
    public PropertyServiceImpl(PropertyRepository propertyRepository, PropertyImageRepository propertyImageRepository,
                               UserService userService, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

//    @PreAuthorize("hasAuthority('BUYER')")
    @Override
    public List<Property> getFilteredAndSortedProperties(PropertyFilterDto propertyFilterDto) {

        String zipCode = propertyFilterDto.getZipCode();
        Double minPrice = propertyFilterDto.getMinPrice();
        Double maxPrice = propertyFilterDto.getMaxPrice();
        Integer minSqFt = propertyFilterDto.getMinSqft();
        String sortOrder = propertyFilterDto.getSortOrder();

        String finalZipCode = (zipCode != null && !zipCode.trim().isEmpty()) ? zipCode.trim() : null;

        Double finalMinPrice = minPrice;
        if (finalMinPrice != null) {
            if (finalMinPrice <= 0) {
                throw new InvalidPropertyParameterException("Min price should be greater than 0.");
            }
        }

        Double finalMaxPrice = maxPrice;
        if (finalMaxPrice != null) {
            if (finalMaxPrice <= 0) {
                throw new InvalidPropertyParameterException("Max price should be greater than 0.");
            }
        }

        if (finalMinPrice != null && finalMaxPrice != null && finalMinPrice > finalMaxPrice) {
            throw new InvalidPropertyParameterException(
                    "Max price (" + finalMaxPrice + ") cannot be less than min price (" + finalMinPrice + ").");
        }

        Integer finalMinSqFt = minSqFt;
        if (finalMinSqFt != null && finalMinSqFt < 0) {
            System.err.println("Warning: Negative minSize (" + finalMinSqFt + "). Min size filter ignored.");
            finalMinSqFt = null; // Treat as no min size filter
        }
        // List<String> allowedSortFields = Arrays.asList("id", "price", "size",
        // "title", "zipCode");

        Sort sort = Sort.unsorted(); // no sorting default

        if (sortOrder != null) {
            switch (sortOrder.toLowerCase()) {
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.DESC, "id");
                    break;
            }
        } else { // default sorting
            sort = Sort.by(Sort.Direction.DESC, "id");
        }

        return propertyRepository.findAllPropertiesWithFilters(finalZipCode, finalMinSqFt, finalMinPrice, finalMaxPrice,
                sort);
    }

    @PreAuthorize("hasAnyAuthority('BUYER', 'AGENT')")
    @Override
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property with ID: " + id + " not found."));
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    @Transactional
    public void createProperty(Property property) {
        if (property == null) {
            throw new InvalidPropertyParameterException("Property cannot be null.");
        }
        Property prop = propertyRepository.findByLocation(property.getLocation());
        if (prop != null) {
            logger.warn("Failed to add property: Property with location {} already exists.", property.getLocation());
            throw new AlreadyExistsException("Property with location " + property.getLocation() + " already exists.");
        }
        propertyRepository.save(property);
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    @Transactional
    public void updateProperty(Long propertyId,
                               AddPropertyDto addPropertyDto,
                               List<MultipartFile> newFiles) throws IOException {

        Property existingProperty = propertyRepository.findById(propertyId)
                .orElseThrow(
                        () -> new NotFoundException("Property with ID: " + propertyId + " not found."));

        // updating property details thru dto
        existingProperty.setTitle(addPropertyDto.getTitle());
        existingProperty.setPrice(addPropertyDto.getPrice());
        existingProperty.setDescription(addPropertyDto.getDescription());
        existingProperty.setLocation(addPropertyDto.getLocation());
        existingProperty.setZipCode(addPropertyDto.getZipCode());
        existingProperty.setSize(addPropertyDto.getSize());

        // handling new image uploads
        if (newFiles != null && !newFiles.isEmpty()) {
            // added for image dir
            // Path propertyDir = Paths.get(UPLOAD_DIR,
            // String.valueOf(existingProperty.getId()));
            Path uploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR,
                    existingProperty.getId().toString());
            Files.createDirectories(uploadPath);

            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();

                    String uniqueFilename = UUID.randomUUID() + originalFilename;
                    Path newUploadPath = uploadPath.resolve(uniqueFilename);
                    try {
                        file.transferTo(newUploadPath.toFile());
                    } catch (IOException e) {
                        throw new InvalidPropertyImageParameterException("Unable to upload Image: " + e.getMessage());
                    }

                    PropertyImage image = new PropertyImage(uniqueFilename, existingProperty);
                    existingProperty.addImage(image);
                } else {
                    logger.warn("Skipping empty new image file during property update.");
                }
            }
        }

        propertyRepository.save(existingProperty);
        logger.info("Property with ID {} updated successfully.", propertyId);
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    @Transactional
    public void deleteProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property with ID: " + id + " not found to delete."));

        try {
            Path propertyDir = Paths.get(UPLOAD_DIR, String.valueOf(property.getId()));
            if (Files.exists(propertyDir)) {
                Files.walk(propertyDir).sorted(java.util.Comparator.reverseOrder()).map(Path::toFile)
                        .forEach(java.io.File::delete);
                logger.info("Deleted image directory for property ID {}: {}", id, propertyDir);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete image directory for property ID {}: {}", id, e.getMessage());
        }

        // Remove property from agent's list
        User agent = property.getAgent();
        if (agent != null) {
            agent.removeProperty(property);
            userRepository.save(agent);
        }

        // Remove all favorites referencing this property
        for (Favorite favorite : new ArrayList<>(property.getFavorites())) {
            User buyer = favorite.getBuyer();
            if (buyer != null) {
                buyer.removeFavorite(favorite);
                userRepository.save(buyer);
            }
        }

        // Remove all messages referencing this property
        for (Message message : new ArrayList<>(property.getMessages())) {
            User sender = message.getSender();
            if (sender != null) {
                sender.removeSentMessage(message);
                userRepository.save(sender);
            }
        }

        propertyRepository.delete(property);
        logger.info("Property with ID {} and its associated images deleted successfully.", id);
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    @Transactional
    public void addPropertyFromDto(AddPropertyDto dto, List<MultipartFile> files) {
        Property property = new Property();

        // mapping the fieds from DTO to entity
        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setLocation(dto.getLocation());
        property.setZipCode(dto.getZipCode());
        property.setPrice(dto.getPrice());
        property.setSize(dto.getSize());

        User currAgent = userService.getCurrentUserContext().user();
        currAgent.addProperty(property);

        Property saved = propertyRepository.save(property);

        logger.info("Service received files for new property (ID: {}): {} files.", property.getId(),
                (files != null ? files.size() : "null"));

        if (files != null && !files.isEmpty()) {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR, saved.getId().toString());
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new InvalidPropertyImageParameterException("Unable save image: " + e.getMessage());
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();

                    String uniqueFilename = UUID.randomUUID() + originalFilename;
                    Path newUploadPath = uploadPath.resolve(uniqueFilename);
                    try {
                        file.transferTo(newUploadPath.toFile());
                    } catch (IOException e) {
                        throw new InvalidPropertyImageParameterException("Unable to upload Image: " + e.getMessage());
                    }

                    PropertyImage image = new PropertyImage(uniqueFilename, saved);
                    saved.addImage(image);
                } else {
                    logger.warn("Skipping empty new image file during property update.");
                }
            }
        } else {
            logger.info("No files/empty files list provided to service for new property (ID: {}).", property.getId());
        }

        // re-saving the propety for newly added images
        propertyRepository.save(property);
    }

    @PreAuthorize("hasAnyAuthority('AGENT', 'BUYER')")
    @Override
    public Property findById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property with ID " + id + " not found."));
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    @Transactional
    public void deletePropertyImage(@PathVariable Long propertyId, @PathVariable Long imageId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property with ID: " + propertyId + " not found."));

        logger.info("Service: Found property ID: {}", propertyId);

        PropertyImage imageDelete = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image with ID: " + imageId + " not found."));

        logger.info("Service: Found image ID: {}. Filename: {}", imageId, imageDelete.getImageFilename());

        // image belongs to specific property
        if (!imageDelete.getProperty().getId().equals(propertyId)) {
            logger.warn("Security check failed: Image ID: {} does NOT belong to Property ID: {}", imageId, propertyId);
            throw new InvalidPropertyImageParameterException("Image does not belong to property.");
        }

        logger.info("Service: Image ID: {} belongs to Property ID: {}", imageId, propertyId);

        Path filePath = Paths.get(UPLOAD_DIR, imageDelete.getImageFilename());
        try {
            Files.deleteIfExists(filePath);
            logger.info("Service deleted image file: {}", imageDelete.getImageFilename());
        } catch (IOException e) {
            logger.error("Service could not delete image file {}: {}", imageDelete.getImageFilename(), e.getMessage(),
                    e);
            throw new RuntimeException("Failed to delete image: " + e.getMessage(), e);
        }

        // using helper function to remove image
        property.removeImage(imageDelete);
        logger.info("Service: Removed image ID: {} from property's collection.", imageId);

        propertyImageRepository.delete(imageDelete);
        logger.info("Service deleted image with ID: {}", imageId);

        propertyRepository.save(property);
        logger.info("Service: Property ID {} saved after image deletion.", propertyId);
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @Override
    public void favouritePropertyById(Long propertyId) {
        Property p = propertyRepository.findPropertyById(propertyId);
        User user = userService.getCurrentUserContext().user();
        if (p == null) {
            throw new NotFoundException("No property with id " + propertyId);
        }

        Favorite f = new Favorite(user, p);
        p.addFavorite(f);

        propertyRepository.save(p);
    }


    @PreAuthorize("hasAnyAuthority('AGENT', 'BUYER')")
    @Override
    public Boolean exist(Long propertyId) {
        return propertyRepository.existsById(propertyId);
    }

    //@PreAuthorize("hasAuthority('AGENT')")
    @Override
    public List<Property> getPropertiesByAgent(Long id) {
        User agent = userService.getCurrentUserContext().user();

        if (agent == null) {
            throw new NotFoundException("Agent with ID: " + id + " not found.");
        }

        return propertyRepository.findAllByAgentId(agent.getId());
    }
}
