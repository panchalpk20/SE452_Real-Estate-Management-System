package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.entities.Message;
import edu.final_project.hot_properties.entities.Property;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.exceptions.InvalidMessageParameterException;
import edu.final_project.hot_properties.exceptions.InvalidOperationException;
import edu.final_project.hot_properties.exceptions.NotFoundException;
import edu.final_project.hot_properties.repositories.MessageRepository;
import edu.final_project.hot_properties.repositories.PropertyRepository;
import edu.final_project.hot_properties.repositories.UserRepository;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserService userService,
    UserRepository userRepository, PropertyRepository propertyRepository) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    // displaying sent user messages
//    @PreAuthorize("hasAuthority('BUYER')")
    @Override
    public List<Message> getMessagesForCurrentUser() {
        User user = userService.getCurrentUserContext().user();
        logger.info("Fetching messages for user: {}", user != null ? user.getId() : "unknown");
        List<Message> messages = messageRepository.findAllBySender(user);
        logger.debug("Found {} messages for user: {}", messages.size(), user != null ? user.getId() : "unknown");
        return messages;
    }
    
    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    public List<Message> getMessagesForCurrentAgent() {
        User currAgent = userService.getCurrentUserContext().user();
        logger.info("Fetching messages for agent: {}", currAgent != null ? currAgent.getId() : "unknown");
        List<Message> allAgentMessage = messageRepository.findAllByAgent(currAgent);
        logger.debug("Found {} messages for agent: {}", allAgentMessage.size(),
        currAgent != null ? currAgent.getId() : "unknown");
        return allAgentMessage;
    }
    
    @PreAuthorize("hasAuthority('BUYER')")
    @Override
    public void sendMessageToAgent(Long propertyId, String content) {

        User sender = userService.getCurrentUserContext().user();

        if (sender == null) {
            logger.warn("Attempted to send a message without a logged-in user.");
            throw new InvalidMessageParameterException("User is not logged in.");
        }

        if (content == null || content.trim().isEmpty()) {
            logger.warn("Attempted to send a message with empty content by user: {}", sender.getEmail());
            throw new InvalidMessageParameterException("Message content cannot be null or empty.");
        }
        
        Property property = propertyRepository.findPropertyById(propertyId);
        if (property == null) {
            logger.error("Property with ID {} not found when user {} tried to send a message.", propertyId,
            sender.getEmail());
            throw new NotFoundException("Property you trying to send message regarding is not found.");
        }
        
        Message message = new Message(sender, content.trim(), property);
        
        messageRepository.save(message);
    }
    
    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Message with ID " + id + " not found."));
    }
    
    @PreAuthorize("hasAuthority('AGENT')")
    @Override
    public void replyToMessage(Long id, String reply) {
        Message message = getMessageById(id);
        if (message == null) {
            logger.error("Attempted to reply to a message with ID {} that does not exist.", id);
            throw new NotFoundException("Message with ID " + id + " not found.");
        }

        if (reply == null || reply.trim().isEmpty()) {
            logger.warn("Attempted to reply with empty content for message ID {}", id);
            throw new InvalidOperationException("Reply cannot be empty.");
        }

        if (message.getReply() != null) {
            logger.warn("Attempted to reply to a message with ID {} that has already been replied to.", id);
            throw new InvalidOperationException("This message has already been replied to.");
        }

        message.setReply(reply.trim());
        messageRepository.save(message);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('AGENT', 'BUYER')")
    public void deleteMessage(Long id) {

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        User sender = message.getSender();
        if (sender != null) {
            logger.info("Deleting message with ID {} sent by user: {}", id, sender.getEmail());
            sender.removeSentMessage(message); // remove from list and sets sender to null
            userRepository.save(sender); // Ensure the change is persisted
        } else {
            logger.warn("Attempted to delete message with ID {} but sender is null, deleting directly.", id);
            messageRepository.delete(message); // Fallback delete if sender is null
        }
    }

}
