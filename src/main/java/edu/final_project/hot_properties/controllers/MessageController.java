package edu.final_project.hot_properties.controllers;

import edu.final_project.hot_properties.entities.Message;
import edu.final_project.hot_properties.exceptions.InvalidMessageParameterException;
import edu.final_project.hot_properties.exceptions.InvalidOperationException;
import edu.final_project.hot_properties.exceptions.NotFoundException;
import edu.final_project.hot_properties.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }


    @PreAuthorize("hasAuthority('BUYER')")
    @GetMapping("/buyer")
    public String viewMessagesBuyer(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Message> messages = messageService.getMessagesForCurrentUser();
            model.addAttribute("messages", messages);
            return "messages/buyer-message";
        } catch (NotFoundException e) {
            logger.error("No messages found for buyer: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "No messages found: " + e.getMessage());
            return "redirect:/dashboard";
        } catch (InvalidMessageParameterException e) {
            logger.error("Invalid message parameter while retrieving buyer messages: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error retrieving messages: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @GetMapping("/agent")
    public String viewMessagesAgent(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Message> messages = messageService.getMessagesForCurrentAgent();
            model.addAttribute("messages", messages);
            return "messages/agent-message";
        } catch (NotFoundException e) {
            logger.error("No messages found for agent: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "No messages found: " + e.getMessage());
            return "redirect:/dashboard";
        } catch (InvalidOperationException e) {
            logger.error("Invalid operation while retrieving agent messages: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error retrieving messages: " + e.getMessage());
            return "redirect:/dashboard";
        } catch (InvalidMessageParameterException e) {
            logger.error("Invalid message parameter while retrieving agent messages: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error retrieving messages: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @PostMapping("/send")
    public String sendMessage(
            @RequestParam("propertyId") Long propertyId,
            @RequestParam("message") String content,
            RedirectAttributes redirectAttributes) {
        try {
            messageService.sendMessageToAgent(propertyId, content);
            redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully!");
        } catch (NotFoundException e) {
            logger.error("Property not found for id {}: {}", propertyId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (InvalidMessageParameterException e) {
            logger.error("Invalid message parameter for property {}: {}", propertyId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (InvalidOperationException e) {
            logger.error("Invalid operation while sending message for property {}: {}", propertyId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/properties/view/" + propertyId;
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @GetMapping("/view/{id}")
    public String viewSingleMessage(@PathVariable Long id, Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            Message message = messageService.getMessageById(id);
            model.addAttribute("message", message);
            return "messages/view-message";
        } catch (NotFoundException e) {
            logger.error("Message not found with id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/messages/agent";
        } catch (InvalidOperationException e) {
            logger.error("Invalid operation while viewing message {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/messages/agent";
        } catch (InvalidMessageParameterException e) {
            logger.error("Invalid message parameter for message {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/messages/agent";
        }
    }

    @PreAuthorize("hasAuthority('AGENT')")
    @PostMapping("/reply/{id}")
    public String sendReply(
            @PathVariable Long id,
            @RequestParam("reply") String reply,
            RedirectAttributes redirectAttributes
    ) {
        try {
            messageService.replyToMessage(id, reply);
            redirectAttributes.addFlashAttribute("successMessage", "Reply sent successfully!");
        } catch (NotFoundException e) {
            logger.error("Message not found with id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (InvalidOperationException e) {
            logger.error("Invalid operation while replying to message {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (InvalidMessageParameterException e) {
            logger.error("Invalid reply parameter for message {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/messages/view/" + id;
    }


    @PreAuthorize("hasAnyAuthority('AGENT', 'BUYER')")
    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes,
                                @RequestHeader(value = "Referer", required = false) String referer
    ) {
        try {
            messageService.deleteMessage(id);
            redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully!");
        } catch (NotFoundException e) {
            logger.error("Error deleting message {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (InvalidOperationException e) {
            logger.error("Invalid operation while deleting message {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

}