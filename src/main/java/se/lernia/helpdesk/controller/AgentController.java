
package se.lernia.helpdesk.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.lernia.helpdesk.entity.TicketStatus;

import se.lernia.helpdesk.service.TicketService;

@Controller
public class AgentController {

    private final TicketService ticketService;

    public AgentController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/agent/tickets")
    public String showAllTickets(
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        model.addAttribute("tickets",
                ticketService.getAllTickets(username));

        return "agent-tickets";
    }


    @PostMapping("/agent/tickets/{id}/assign")
    public String assignTicketToSelf(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        ticketService.assignTicketToSelf(
                id,
                authentication.getName()
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Ärendet har tilldelats dig!"
        );

        return "redirect:/tickets/" + id;
    }


    @PostMapping("/agent/tickets/{id}/status")
    public String updateTicketStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        ticketService.updateTicketStatus(
                id,
                status,
                authentication.getName()
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Ärendets status har uppdaterats!"
        );

        return "redirect:/tickets/" + id;
    }


}
