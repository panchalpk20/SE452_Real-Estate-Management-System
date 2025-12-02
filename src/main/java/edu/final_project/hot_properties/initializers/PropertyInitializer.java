package edu.final_project.hot_properties.initializers;

import edu.final_project.hot_properties.entities.Property;
import edu.final_project.hot_properties.entities.PropertyImage;
import edu.final_project.hot_properties.entities.Role;
import edu.final_project.hot_properties.entities.RoleEnum;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.repositories.PropertyImageRepository;
import edu.final_project.hot_properties.repositories.PropertyRepository;
import edu.final_project.hot_properties.repositories.RoleRepository;
import edu.final_project.hot_properties.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@DependsOn("usersDataInitializer")
public class PropertyInitializer {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private static final String UPLOADS_ROOT = "uploads";

    @Autowired
    public PropertyInitializer(PropertyRepository propertyRepository,
            PropertyImageRepository propertyImageRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {

        if (propertyRepository.count() == 0) {
            System.out.println("Initializing properties data from...");

            Optional<Role> agentRoleOptional = roleRepository.findByName(RoleEnum.AGENT.name());
            if (agentRoleOptional.isEmpty()) {
                System.err.println("Agent role not found. Cannot initialize properties without an agent.");
                return;
            }
            Role agentRole = agentRoleOptional.get();
            List<User> agents = userRepository.findUsersByAgentRole();

            if (agents.isEmpty()) {
                System.err.println("No agent users found. Cannot initialize properties without an agent.");
                return;
            }

            User defaultAgent = agents.get(0);

            Property p1 = new Property(
                    "3818 N Christiana Ave",
                    (double) 1025000,
                    "Experience luxury living in this beautifully redesigned single-family home, where expert craftsmanship and meticulous attention to detail shine throughout. The sun-drenched open-concept main level boasts a seamless flow between the living and dining areas, complemented by a cozy family room featuring a gas fireplace and elegant doors leading to the rear patio & spacious back yard. The stunning chef's kitchen is a true showpiece, featuring custom cabinetry, a designer tile backsplash, high-end stainless steel appliances, and an expansive center island-perfect for entertaining. Upstairs, the spacious primary suite offers a spa-like retreat with a luxurious ensuite featuring a frameless glass shower and dual vanities. Step through the patio doors to enjoy a large private balcony, perfect for morning coffee and fresh air. Two additional bedrooms, a stylish full bath, and generous storage complete the second floor, including a bright and airy bedroom that features a beautiful terrace overlooking the tree-lined street-an inviting space to relax and enjoy the neighborhood views. The sunlit third level is the epitome of this home, offering a bright and inviting alternative to a traditional basement. This exceptional space features a fourth bedroom, another full guest bath, a dedicated laundry area, and a versatile, spacious, recreation room complete with a wet bar, wine fridge, and a walk-out terrace. Flooded with natural light, it provides the perfect blend of comfort and functionality for both relaxing and entertaining. Outside, the professionally landscaped sizable yard offers a picturesque setting, while the two-car garage adds convenience. Situated in a sought-after neighborhood, this stunning home boasts incredible curb appeal and is truly a must-see! All of the outdoor spaces, including the charming front porch, balconies off of multiple rooms, rear patio, and beautifully designed yard, are unique highlights that enhance this home's appeal!",
                    "Chicago, IL 60618",
                    "60618",
                    3600);
            p1.setAgent(defaultAgent);
            propertyRepository.save(p1);
            addImagesFromFolder(p1, p1.getId().toString());

        } else {
            System.out.println("Properties data already exists, skipping initialization.");
        }
    }

    private void addImagesFromFolder(Property property, String folderName) {
        File dir = new File(UPLOADS_ROOT, folderName);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".webp"));
            if (files != null) {
                for (File file : files) {
                    propertyImageRepository.save(new PropertyImage(file.getName(), property));
                }
            }
        } else {
            System.err.println("Image folder not found for property: " + folderName);
        }
    }

}