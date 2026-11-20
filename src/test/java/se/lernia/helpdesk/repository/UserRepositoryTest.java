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

        // Arrange
        User user = new User(
                "testuser",
                "temporary-password",
                Role.USER
        );

        // Act
        userRepository.save(user);

        User foundUser = userRepository
                .findByUsername("testuser")
                .orElseThrow();

        // Assert
        assertEquals("testuser", foundUser.getUsername());
        assertEquals(Role.USER, foundUser.getRole());
        assertTrue(foundUser.isEnabled());
    }
}