
package se.lernia.helpdesk.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import se.lernia.helpdesk.entity.Comment;
import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindCommentsForTicket() {

        // Arrange
        User user = userRepository.save(
                new User("commentuser", "password", Role.USER)
        );

        Ticket ticket = ticketRepository.save(
                new Ticket(
                        "Problem med datorn",
                        "Datorn startar inte",
                        user
                )
        );

        Comment firstComment = new Comment(
                "Jag behöver hjälp med datorn.",
                ticket,
                user
        );

        Comment secondComment = new Comment(
                "Jag har redan testat att starta om.",
                ticket,
                user
        );

        commentRepository.saveAndFlush(firstComment);
        commentRepository.saveAndFlush(secondComment);

        // Act
        List<Comment> comments =
                commentRepository.findByTicketIdOrderByCreatedAtAsc(
                        ticket.getId()
                );

        // Assert
        assertEquals(2, comments.size());

        assertEquals(
                "Jag behöver hjälp med datorn.",
                comments.get(0).getContent()
        );

        assertEquals(
                "Jag har redan testat att starta om.",
                comments.get(1).getContent()
        );

        assertEquals(
                user.getUsername(),
                comments.get(0).getAuthor().getUsername()
        );
    }

    @Test
    void shouldNotReturnCommentsFromOtherTickets() {

        // Arrange
        User user = userRepository.save(
                new User("anotheruser", "password", Role.USER)
        );

        Ticket firstTicket = ticketRepository.save(
                new Ticket("Ärende 1", "Beskrivning 1", user)
        );

        Ticket secondTicket = ticketRepository.save(
                new Ticket("Ärende 2", "Beskrivning 2", user)
        );

        commentRepository.saveAndFlush(
                new Comment("Kommentar på ärende 1", firstTicket, user)
        );

        commentRepository.saveAndFlush(
                new Comment("Kommentar på ärende 2", secondTicket, user)
        );

        // Act
        List<Comment> comments =
                commentRepository.findByTicketIdOrderByCreatedAtAsc(
                        firstTicket.getId()
                );

        // Assert
        assertEquals(1, comments.size());

        assertEquals(
                "Kommentar på ärende 1",
                comments.get(0).getContent()
        );
    }
}
