
package se.lernia.helpdesk.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import se.lernia.helpdesk.entity.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedById(Long userId);

    List<Ticket> findByAssignedToId(Long agentId);

    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findWithCreatedByById(@Param("id") Long id);
}
