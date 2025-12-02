package edu.final_project.hot_properties.repositories;

import edu.final_project.hot_properties.entities.Property;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // --- individual method filters
    // finding by Id
    Property findPropertyById(Long id);

    // --- combined filter
    @Query("SELECT p FROM Property p LEFT JOIN FETCH p.favorites f WHERE " +
            "(:zipCode IS NULL OR p.zipCode = :zipCode) AND " +
            "(:minSqft IS NULL OR p.size >= :minSqft) AND " +
            "(:minPrice IS NULL OR (p.price IS NOT NULL AND p.price >= :minPrice)) AND " +
            "(:maxPrice IS NULL OR (p.price IS NOT NULL AND p.price <= :maxPrice)) "
    )
    List<Property> findAllPropertiesWithFilters(
            @Param("zipCode") String zipCode,
            @Param("minSqft") Integer minSqft,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Sort sort
    );

    Property findByLocation(String location);

    List<Property> findAllByAgentId(Long agentId);


    List<Property> findByTitle(String title);

    boolean existsByTitle(String title);


}
