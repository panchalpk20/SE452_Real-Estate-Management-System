package edu.final_project.hot_properties.repositories;

import edu.final_project.hot_properties.entities.Favorite;
import edu.final_project.hot_properties.entities.Property;
import edu.final_project.hot_properties.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Favorite findByBuyerAndProperty(User buyer, Property property);

    List<Favorite> findByBuyer(User buyer);

    @Query("SELECT f.property FROM Favorite f WHERE f.buyer.id = :userId")
    List<Property> findFavoritedPropertiesByUserId(@Param("userId") Long userId);
}
