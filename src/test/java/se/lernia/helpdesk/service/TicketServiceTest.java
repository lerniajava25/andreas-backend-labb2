
package se.lernia.helpdesk.service;


import se.lernia.helpdesk.entity.Role;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.entity.TicketStatus;
import se.lernia.helpdesk.entity.User;
import se.lernia.helpdesk.repository.TicketRepository;
import se.lernia.helpdesk.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void shouldCreateTicketSuccessfully() {

        // Arrange
        User user = new User("testuser", "hashedPassword", Role.USER);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ticket ticket = ticketService.createTicket(
                "Problem med datorn",
                "Datorn startar inte",
                "testuser"
        );

        // Assert
        assertEquals("Problem med datorn", ticket.getTitle());
        assertEquals("Datorn startar inte", ticket.getDescription());
        assertEquals(user, ticket.getCreatedBy());

        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldGetTicketsForUser() {

        // Arrange
        User user = new User("testuser", "hashedPassword", Role.USER);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(ticketRepository.findByCreatedById(user.getId()))
                .thenReturn(List.of(
                        new Ticket("Testärende", "Beskrivning", user)
                ));

        // Act
        List<Ticket> tickets =
                ticketService.getTicketsForUser("testuser");

        // Assert
        assertEquals(1, tickets.size());
        assertEquals("Testärende", tickets.get(0).getTitle());
    }

    @Test
    void shouldRejectTicketCreationForUnknownUser() {

        // Arrange
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(
                        "Test",
                        "Beskrivning",
                        "unknown"
                )
        );

        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    void shouldAllowAgentToViewAllTickets() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);

        User owner = new User("user", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med datorn",
                "Datorn startar inte",
                owner
        );

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.findAll())
                .thenReturn(List.of(ticket));

        // Act
        List<Ticket> tickets = ticketService.getAllTickets("agent");

        // Assert
        assertEquals(1, tickets.size());
        assertEquals("Problem med datorn", tickets.get(0).getTitle());

        verify(ticketRepository).findAll();
    }


    @Test
    void shouldAllowAdminToViewAllTickets() {

        // Arrange
        User admin = new User("admin", "password", Role.ADMIN);

        User owner = new User("user", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med skrivaren",
                "Skrivaren fungerar inte",
                owner
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(ticketRepository.findAll())
                .thenReturn(List.of(ticket));

        // Act
        List<Ticket> tickets = ticketService.getAllTickets("admin");

        // Assert
        assertEquals(1, tickets.size());
        assertEquals("Problem med skrivaren", tickets.get(0).getTitle());

        verify(ticketRepository).findAll();
    }


    @Test
    void shouldRejectUserViewingAllTickets() {

        // Arrange
        User user = new User("user", "password", Role.USER);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> ticketService.getAllTickets("user")
        );

        verify(ticketRepository, never()).findAll();
    }


    @Test
    void shouldAllowAgentToAssignTicketToSelf() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);
        User owner = new User("user", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med datorn",
                "Datorn startar inte",
                owner
        );

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ticket result = ticketService.assignTicketToSelf(1L, "agent");

        // Assert
        assertSame(agent, result.getAssignedTo());

        verify(ticketRepository).save(ticket);
    }


    @Test
    void shouldAllowAdminToAssignTicketToSelf() {

        // Arrange
        User admin = new User("admin", "password", Role.ADMIN);
        User owner = new User("user", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med skrivaren",
                "Skrivaren fungerar inte",
                owner
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ticket result = ticketService.assignTicketToSelf(1L, "admin");

        // Assert
        assertSame(admin, result.getAssignedTo());

        verify(ticketRepository).save(ticket);
    }


    @Test
    void shouldRejectUserAssigningTicket() {

        // Arrange
        User user = new User("user", "password", Role.USER);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> ticketService.assignTicketToSelf(1L, "user")
        );

        verify(ticketRepository, never()).findById(anyLong());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    void shouldRejectAssigningAlreadyAssignedTicket() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);
        User otherAgent = new User("otherAgent", "password", Role.AGENT);
        User owner = new User("user", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med nätverket",
                "Internet fungerar inte",
                owner
        );

        ticket.setAssignedTo(otherAgent);

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> ticketService.assignTicketToSelf(1L, "agent")
        );

        assertSame(otherAgent, ticket.getAssignedTo());

        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    void shouldRejectAssigningNonExistingTicket() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.assignTicketToSelf(999L, "agent")
        );

        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    void shouldAllowAgentToUpdateTicketStatus() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);
        User owner = new User("user", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med datorn",
                "Datorn startar inte",
                owner
        );

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        // Act
        Ticket result = ticketService.updateTicketStatus(
                1L,
                TicketStatus.IN_PROGRESS,
                "agent"
        );

        // Assert
        assertEquals(TicketStatus.IN_PROGRESS, result.getStatus());

        verify(ticketRepository).save(ticket);
    }


    @Test
    void shouldAllowAdminToUpdateTicketStatus() {

        // Arrange
        User admin = new User("admin", "password", Role.ADMIN);
        User owner = new User("user", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med skrivaren",
                "Skrivaren fungerar inte",
                owner
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        // Act
        Ticket result = ticketService.updateTicketStatus(
                1L,
                TicketStatus.RESOLVED,
                "admin"
        );

        // Assert
        assertEquals(TicketStatus.RESOLVED, result.getStatus());

        verify(ticketRepository).save(ticket);
    }


    @Test
    void shouldRejectUserUpdatingTicketStatus() {

        // Arrange
        User user = new User("user", "password", Role.USER);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> ticketService.updateTicketStatus(
                        1L,
                        TicketStatus.CLOSED,
                        "user"
                )
        );

        verify(ticketRepository, never()).findById(anyLong());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    void shouldRejectNullTicketStatus() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateTicketStatus(
                        1L,
                        null,
                        "agent"
                )
        );

        verify(ticketRepository, never()).findById(anyLong());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    void shouldRejectUpdatingNonExistingTicket() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateTicketStatus(
                        999L,
                        TicketStatus.IN_PROGRESS,
                        "agent"
                )
        );

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

}
