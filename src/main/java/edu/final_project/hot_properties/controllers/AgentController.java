package edu.final_project.hot_properties.controllers;

import edu.final_project.hot_properties.dtos.AgentDto;
import edu.final_project.hot_properties.dtos.RegisterDto;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.exceptions.AlreadyExistsException;
import edu.final_project.hot_properties.exceptions.InvalidOperationException;
import edu.final_project.hot_properties.exceptions.NotFoundException;
import edu.final_project.hot_properties.services.UserService;
import jakarta.transaction.TransactionRolledbackException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/agents")

public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final UserService userService;

    public AgentController(UserService userService) {
        this.userService = userService;
    }

    // only admin can create agent
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/create")
    public String showCreateAgentForm(Model model) {
        model.addAttribute("agentDto", new RegisterDto());
        return "agent/create";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public String createAgent(@Valid @ModelAttribute("agentDto") RegisterDto agentDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "agent/create";
        }

        try {
            userService.registerNewAgent(agentDto);
            log.info("Agent created: {}", agentDto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Agent with email " + agentDto.getEmail() + " has been created.");
        } catch (AlreadyExistsException e) {
            log.error("Agent already exists: {}", agentDto.getEmail(), e);
            model.addAttribute("errorMessage", e.getMessage());
            return "agent/create";
        } catch (NotFoundException e) {
            log.error("Related entity not found while creating agent: {}", agentDto.getEmail(), e);
            model.addAttribute("errorMessage", e.getMessage());
            return "agent/create";
        }
        return "redirect:/agents/manage";
    }

    // Delete agent
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/delete/{email}")
    public String deleteAgent(@PathVariable String email, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserByEmail(email);
            log.info("Agent deleted: {}", email);
            redirectAttributes.addFlashAttribute("successMessage", "Agent with email " + email + " has been deleted.");
        } catch (NotFoundException e) {
            log.error("Agent not found for deletion: {}", email, e);
            redirectAttributes.addFlashAttribute("errorMessage", ": " + e.getMessage());
        }catch (InvalidOperationException e) {
            log.error("tried to delete while logged in: {}", email, e);
            redirectAttributes.addFlashAttribute("errorMessage", "error deleting: " + e.getMessage());
        }
        return "redirect:/agents/manage";
    }

    // Show form to edit agent
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/edit/{email}")
    public String showEditAgentForm(@PathVariable String email, Model model) {
        try {
            User agent = userService.findUserByEmail(email);
            AgentDto agentDto = new AgentDto(agent);
            model.addAttribute("agentDto", agentDto);
            return "agent/edit";
        } catch (NotFoundException e) {
            log.error("Agent not found with email: {}", email, e);
            model.addAttribute("errorMessage", "Agent not found: " + e.getMessage());
            return "redirect:/agents/manage";
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/update")
    public String updateAgent(@Valid @ModelAttribute("agentDto") AgentDto agentDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "agent/edit";
        }
        try {
            userService.updateUserByEmail(agentDto.getEmail(), agentDto);
            redirectAttributes.addFlashAttribute("successMessage", "Agent updated.");
        } catch (NotFoundException | AlreadyExistsException e) {
            log.error("Error updating agent", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "agent/edit";
        }
        return "redirect:/agents/manage";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping({ "/manage", "/" })
    public String manageAgents(Model model) {

        List<User> allUsers = userService.getAll();
        model.addAttribute("allUsers", allUsers);
        return "agent/manage";
    }

}
