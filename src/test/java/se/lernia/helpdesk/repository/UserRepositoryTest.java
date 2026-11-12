package se.lernia.helpdesk.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.entity.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByUsername() {

        // Arrange - skapa en testanvändare
        User user = new User(
                "testuser",
                "temporary-password",
                Role.USER
        );

        // Act - spara och hämta användaren
        userRepository.save(user);

        User foundUser = userRepository
                .findByUsername("testuser")
                .orElseThrow();

        // Assert - kontrollera resultatet
        assertEquals("testuser", foundUser.getUsername());
        assertEquals(Role.USER, foundUser.getRole());
        assertTrue(foundUser.isEnabled());
    }
}