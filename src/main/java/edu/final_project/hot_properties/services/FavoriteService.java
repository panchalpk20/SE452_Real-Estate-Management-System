package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.entities.Favorite;
import edu.final_project.hot_properties.entities.Property;
import java.util.List;

public interface FavoriteService {

    Favorite addPropertyToFavorites(Long propertyId);

    boolean removePropertyFromFavorites(Long propertyId);

    boolean isPropertyFavoritedByUser(Long propertyId);

    List<Property> getMyFavorites();
}
