
package se.lernia.helpdesk.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.lernia.helpdesk.entity.Comment;
import se.lernia.helpdesk.entity.Ticket;
import se.lernia.helpdesk.entity.User;
import se.lernia.helpdesk.repository.CommentRepository;
import se.lernia.helpdesk.repository.UserRepository;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;

    public CommentService(
            CommentRepository commentRepository,
            UserRepository userRepository,
            TicketService ticketService
    ) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
    }

    @Transactional
    public Comment addComment(
            Long ticketId,
            String content,
            String username
    ) {
        // Kontrollera att användaren får läsa ärendet
        Ticket ticket = ticketService.getTicketById(ticketId, username);

        // Kontrollera att kommentaren innehåller text
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Comment cannot be empty"
            );
        }

        if (content.length() > 5000) {
            throw new IllegalArgumentException(
                    "Comment cannot exceed 5000 characters"
            );
        }

        // Hämta författaren
        User author = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        // Skapa och spara kommentaren
        Comment comment = new Comment(content, ticket, author);

        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsForTicket(
            Long ticketId,
            String username
    ) {
        // Samma behörighetskontroll vid läsning
        ticketService.getTicketById(ticketId, username);

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(
                ticketId
        );
    }
}
