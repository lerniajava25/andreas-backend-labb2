
package se.lernia.helpdesk.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.entity.TicketStatus;
import se.lernia.helpdesk.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindTicketByCreator() {

        // Arrange - skapa en användare
        User user = new User(
                "ticketuser",
                "hashedPassword",
                Role.USER
        );

        user = userRepository.save(user);

        // Arrange - skapa ett ärende
        Ticket ticket = new Ticket(
                "Problem med datorn",
                "Datorn startar inte",
                user
        );

        ticketRepository.save(ticket);

        // Act - hämta ärenden som skapats av användaren
        List<Ticket> tickets =
                ticketRepository.findByCreatedById(user.getId());

        // Assert - kontrollera resultatet
        assertEquals(1, tickets.size());
        assertEquals("Problem med datorn", tickets.get(0).getTitle());
        assertEquals(TicketStatus.NEW, tickets.get(0).getStatus());
        assertEquals(user.getId(), tickets.get(0).getCreatedBy().getId());
    }
}
