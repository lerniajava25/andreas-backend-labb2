package se.lernia.helpdesk.repository;
import se.lernia.helpdesk.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import se.lernia.helpdesk.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRoleAndEnabledTrue(Role role);
}