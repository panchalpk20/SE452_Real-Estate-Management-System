package edu.final_project.hot_properties.services;

import edu.final_project.hot_properties.entities.Message;

import java.util.List;


public interface MessageService {
    List<Message> getMessagesForCurrentUser();

    List<Message> getMessagesForCurrentAgent();

    void sendMessageToAgent(Long propertyId, String content);

    Message getMessageById(Long id);

    void replyToMessage(Long id, String reply);

    void deleteMessage(Long id);
}