
package se.lernia.helpdesk.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import se.lernia.helpdesk.entity.Comment;
import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.entity.User;
import se.lernia.helpdesk.repository.CommentRepository;
import se.lernia.helpdesk.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void shouldAddCommentSuccessfully() {

        // Arrange
        User user = new User("testuser", "password", Role.USER);

        Ticket ticket = new Ticket(
                "Problem med datorn",
                "Datorn startar inte",
                user
        );

        when(ticketService.getTicketById(1L, "testuser"))
                .thenReturn(ticket);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Comment comment = commentService.addComment(
                1L,
                "Jag har testat att starta om datorn.",
                "testuser"
        );

        // Assert
        assertEquals(
                "Jag har testat att starta om datorn.",
                comment.getContent()
        );

        assertEquals(user, comment.getAuthor());
        assertEquals(ticket, comment.getTicket());

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldRejectEmptyComment() {

        // Arrange
        User user = new User("testuser", "password", Role.USER);

        Ticket ticket = new Ticket("Test", "Beskrivning", user);

        when(ticketService.getTicketById(1L, "testuser"))
                .thenReturn(ticket);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.addComment(
                        1L,
                        "   ",
                        "testuser"
                )
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthorizedUser() {

        // Arrange
        when(ticketService.getTicketById(1L, "other"))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> commentService.addComment(
                        1L,
                        "Obehörig kommentar",
                        "other"
                )
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldGetCommentsForTicket() {

        // Arrange
        User user = new User("testuser", "password", Role.USER);

        Ticket ticket = new Ticket("Test", "Beskrivning", user);

        Comment comment = new Comment(
                "Min kommentar",
                ticket,
                user
        );

        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(comment));

        // Act
        List<Comment> comments =
                commentService.getCommentsForTicket(1L, "testuser");

        // Assert
        assertEquals(1, comments.size());
        assertEquals("Min kommentar", comments.get(0).getContent());

        verify(ticketService).getTicketById(1L, "testuser");
    }

    @Test
    void shouldRejectCommentExceedingMaxLength() {

        // Arrange
        User user = new User("testuser", "password", Role.USER);
        Ticket ticket = new Ticket("Test", "Beskrivning", user);

        when(ticketService.getTicketById(1L, "testuser"))
                .thenReturn(ticket);

        String longComment = "a".repeat(5001);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.addComment(
                        1L,
                        longComment,
                        "testuser"
                )
        );

        verify(commentRepository, never()).save(any());
    }
}
