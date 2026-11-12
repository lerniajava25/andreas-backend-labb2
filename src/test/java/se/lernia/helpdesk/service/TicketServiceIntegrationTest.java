
package se.lernia.helpdesk.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.entity.User;
import se.lernia.helpdesk.repository.TicketRepository;
import se.lernia.helpdesk.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TicketServiceIntegrationTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void shouldAllowOwnerToViewTicket() {

        // Arrange
        User owner = userRepository.save(
                new User("owner", "password", Role.USER)
        );

        Ticket ticket = ticketRepository.save(
                new Ticket("Mitt ärende", "Beskrivning", owner)
        );

        // Act
        Ticket result = ticketService.getTicketById(
                ticket.getId(),
                owner.getUsername()
        );

        // Assert
        assertEquals(ticket.getId(), result.getId());
        assertEquals("Mitt ärende", result.getTitle());
    }

    @Test
    void shouldRejectUserViewingAnotherUsersTicket() {

        // Arrange
        User owner = userRepository.save(
                new User("owner", "password", Role.USER)
        );

        User otherUser = userRepository.save(
                new User("other", "password", Role.USER)
        );

        Ticket ticket = ticketRepository.save(
                new Ticket("Privat ärende", "Hemlig beskrivning", owner)
        );

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> ticketService.getTicketById(
                        ticket.getId(),
                        otherUser.getUsername()
                )
        );
    }

    @Test
    void shouldAllowAgentToViewAnyTicket() {

        // Arrange
        User owner = userRepository.save(
                new User("owner", "password", Role.USER)
        );

        User agent = userRepository.save(
                new User("agent", "password", Role.AGENT)
        );

        Ticket ticket = ticketRepository.save(
                new Ticket("Supportärende", "Behöver hjälp", owner)
        );

        // Act
        Ticket result = ticketService.getTicketById(
                ticket.getId(),
                agent.getUsername()
        );

        // Assert
        assertEquals(ticket.getId(), result.getId());
    }

    @Test
    void shouldAllowAdminToViewAnyTicket() {

        // Arrange
        User owner = userRepository.save(
                new User("owner", "password", Role.USER)
        );

        User admin = userRepository.save(
                new User("admin", "password", Role.ADMIN)
        );

        Ticket ticket = ticketRepository.save(
                new Ticket("Administrativt ärende", "Beskrivning", owner)
        );

        // Act
        Ticket result = ticketService.getTicketById(
                ticket.getId(),
                admin.getUsername()
        );

        // Assert
        assertEquals(ticket.getId(), result.getId());
    }
}
