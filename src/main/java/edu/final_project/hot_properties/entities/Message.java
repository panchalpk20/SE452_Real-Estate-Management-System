package edu.final_project.hot_properties.entities;

import edu.final_project.hot_properties.exceptions.InvalidParameterException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-one: message sender
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender; // The User that sent this message

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // Many-to-one: related property
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property; // The property that this message is related with (Property)

    @Column(name = "timestamp")
    private LocalDateTime timestamp; // Time the message was sent

    @Column(name = "reply")
    private String reply;

    // constructor
    public Message() {
    }

    public Message(User sender, String content, Property property) {
        setSender(sender);
        setContent(content);
        setProperty(property);
        setTimestamp(timestamp);
       // setReply(reply);
    }

    // setters
    public void setContent(String content) {
        if (content.trim().isEmpty()) {
            throw new InvalidParameterException("Content cannot be empty.");
        }
        this.content = content;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = LocalDateTime.now();
    }

    public void setReply(String reply) {
        if (reply.trim().isEmpty()) {
            throw new InvalidParameterException("reply cannot be empty.");
        }
        this.reply = reply;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    //utility setters
    public void setMessageSender(User sender) {
        setSender(sender);
        sender.getSentMessages().add(this);
    }

    public void setMessageProperty(Property property) {
        setProperty(property);
        property.getMessages().add(this);
    }

    // getters
    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Property getProperty() {
        return property;
    }

    public String getReply() {
        return reply;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}