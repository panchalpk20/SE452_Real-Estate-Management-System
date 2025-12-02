package edu.final_project.hot_properties.repositories;

import edu.final_project.hot_properties.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role.name = 'AGENT'")
    List<User> findUsersByAgentRole();

    @Query("SELECT u FROM User u WHERE u.role.name = 'ADMIN'")
    List<User> findUsersByAdminRole();

    List<User> findAll();

}
