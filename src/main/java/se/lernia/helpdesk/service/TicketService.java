
package se.lernia.helpdesk.service;

import se.lernia.helpdesk.entity.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.entity.User;
import se.lernia.helpdesk.repository.TicketRepository;
import se.lernia.helpdesk.repository.UserRepository;
import se.lernia.helpdesk.entity.TicketStatus;


import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Ticket createTicket(
            String title,
            String description,
            String username
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        Ticket ticket = new Ticket(title, description, user);

        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsForUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        return ticketRepository.findByCreatedById(user.getId());
    }


    @Transactional(readOnly = true)
    public Ticket getTicketById(Long ticketId, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        Ticket ticket = ticketRepository.findWithCreatedByById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ticket not found")
                );

        boolean isOwner = ticket.getCreatedBy().getId().equals(user.getId());

        boolean isStaff = user.getRole() == Role.AGENT
                || user.getRole() == Role.ADMIN;

        if (!isOwner && !isStaff) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view this ticket"
            );
        }

        return ticket;
    }


    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (user.getRole() != Role.AGENT &&
                user.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only agents and admins can view all tickets"
            );
        }

        return ticketRepository.findAll();
    }


    @Transactional
    public Ticket assignTicketToSelf(Long ticketId, String username) {


        User agent = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );


        if (agent.getRole() != Role.AGENT &&
                agent.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only agents and admins can assign tickets"
            );
        }


        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ticket not found")
                );


        if (ticket.getAssignedTo() != null) {
            throw new IllegalStateException(
                    "Ticket is already assigned"
            );
        }


        ticket.setAssignedTo(agent);

        return ticketRepository.save(ticket);
    }


    @Transactional
    public Ticket updateTicketStatus(
            Long ticketId,
            TicketStatus newStatus,
            String username
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );


        if (user.getRole() != Role.AGENT &&
                user.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only agents and admins can update ticket status"
            );
        }


        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Ticket status cannot be null"
            );
        }


        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ticket not found")
                );


        ticket.setStatus(newStatus);

        return ticketRepository.save(ticket);
    }

}
