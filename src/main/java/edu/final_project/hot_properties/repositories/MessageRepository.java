package edu.final_project.hot_properties.repositories;

import edu.final_project.hot_properties.entities.Message;
import edu.final_project.hot_properties.entities.Property;
import edu.final_project.hot_properties.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllBySender(User sender);

    List<Message> findAllByProperty(Property property);

    List<Message> findAllByPropertyAndSender(Property property, User sender);

    @Query("SELECT m FROM Message m WHERE m.property.agent = :agent")
    List<Message> findAllByAgent(@Param("agent") User agent);

}
