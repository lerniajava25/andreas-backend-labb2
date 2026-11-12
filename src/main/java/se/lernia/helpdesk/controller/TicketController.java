
package se.lernia.helpdesk.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.service.TicketService;
import se.lernia.helpdesk.service.CommentService;

@Controller
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    public TicketController(
            TicketService ticketService,
            CommentService commentService
    ) {
        this.ticketService = ticketService;
        this.commentService = commentService;
    }

@GetMapping("/tickets")
    public String showTickets(Authentication authentication, Model model) {

        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("tickets",
                ticketService.getTicketsForUser(username));

        return "tickets";
    }


@PostMapping("/tickets/{id}/comments")
public String addComment(
        @PathVariable Long id,
        @RequestParam String content,
        Authentication authentication,
        RedirectAttributes redirectAttributes
) {
    commentService.addComment(
            id,
            content,
            authentication.getName()
    );

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Kommentaren har lagts till!"
    );

    return "redirect:/tickets/" + id;
}




@GetMapping("/tickets/{id}")
public String showTicketDetails(
        @PathVariable Long id,
        Authentication authentication,
        Model model
) {
    String username = authentication.getName();

    Ticket ticket = ticketService.getTicketById(id, username);

    model.addAttribute("ticket", ticket);

    model.addAttribute(
            "comments",
            commentService.getCommentsForTicket(id, username)
    );

    return "ticket-details";
}



@GetMapping("/tickets/new")
    public String showCreateTicketForm() {
        return "ticket-form";
    }

    @PostMapping("/tickets")
    public String createTicket(
            @RequestParam String title,
            @RequestParam String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        ticketService.createTicket(
                title,
                description,
                authentication.getName()
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Ärendet har skapats!"
        );

        return "redirect:/tickets";
    }
}
