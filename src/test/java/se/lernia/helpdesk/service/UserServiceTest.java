
package se.lernia.helpdesk.service;

import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;

import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.entity.User;
import se.lernia.helpdesk.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldCreateUserSuccessfully() {

        // Arrange
        when(userRepository.existsByUsername("andreas"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.createUser(
                "andreas",
                "password123",
                Role.USER
        );

        // Assert
        assertEquals("andreas", createdUser.getUsername());
        assertEquals("hashedPassword", createdUser.getPassword());
        assertEquals(Role.USER, createdUser.getRole());
        assertTrue(createdUser.isEnabled());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUsername() {

        // Arrange
        when(userRepository.existsByUsername("andreas"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        "andreas",
                        "password123",
                        Role.USER
                )
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldAllowAdminToViewAllUsers() {

        // Arrange
        User admin = new User("admin", "password", Role.ADMIN);
        User agent = new User("agent", "password", Role.AGENT);
        User user = new User("user", "password", Role.USER);

        List<User> users = List.of(admin, agent, user);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findAll())
                .thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers("admin");

        // Assert
        assertEquals(3, result.size());
        assertEquals(users, result);

        verify(userRepository).findAll();
    }


    @Test
    void shouldRejectAgentViewingAllUsers() {

        // Arrange
        User agent = new User("agent", "password", Role.AGENT);

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> userService.getAllUsers("agent")
        );

        verify(userRepository, never()).findAll();
    }


    @Test
    void shouldRejectUserViewingAllUsers() {

        // Arrange
        User user = new User("user", "password", Role.USER);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> userService.getAllUsers("user")
        );

        verify(userRepository, never()).findAll();
    }


    @Test
    void shouldRejectUnknownUserViewingAllUsers() {

        // Arrange
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getAllUsers("unknown")
        );

        verify(userRepository, never()).findAll();
    }


    @Test
    void shouldAllowAdminToChangeUserRole() {

        // Arrange
        User admin = new User("admin", "password", Role.ADMIN);
        User user = new User("user", "password", Role.USER);

        ReflectionTestUtils.setField(admin, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 2L);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        // Act
        User result = userService.updateUserRole(
                2L,
                Role.AGENT,
                "admin"
        );

        // Assert
        assertEquals(Role.AGENT, result.getRole());
        verify(userRepository).save(user);
    }



    @Test
    void shouldAllowAdminToPromoteAgentToAdmin() {

        User admin = new User("admin", "password", Role.ADMIN);
        User agent = new User("agent", "password", Role.AGENT);

        ReflectionTestUtils.setField(admin, "id", 1L);
        ReflectionTestUtils.setField(agent, "id", 2L);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(agent));

        when(userRepository.save(agent))
                .thenReturn(agent);

        User result = userService.updateUserRole(
                2L,
                Role.ADMIN,
                "admin"
        );

        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).save(agent);
    }



    @Test
    void shouldRejectUserChangingRoles() {

        User user = new User("user", "password", Role.USER);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUserRole(
                        2L,
                        Role.ADMIN,
                        "user"
                )
        );

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldRejectAgentChangingRoles() {

        User agent = new User("agent", "password", Role.AGENT);

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUserRole(
                        2L,
                        Role.ADMIN,
                        "agent"
                )
        );

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldRejectAdminChangingOwnRole() {

        User admin = new User("admin", "password", Role.ADMIN);

        ReflectionTestUtils.setField(admin, "id", 1L);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalStateException.class,
                () -> userService.updateUserRole(
                        1L,
                        Role.USER,
                        "admin"
                )
        );

        assertEquals(Role.ADMIN, admin.getRole());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldRejectNullRole() {

        User admin = new User("admin", "password", Role.ADMIN);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserRole(
                        2L,
                        null,
                        "admin"
                )
        );

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldRejectChangingRoleForNonExistingUser() {

        User admin = new User("admin", "password", Role.ADMIN);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserRole(
                        999L,
                        Role.AGENT,
                        "admin"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldAllowAdminToDisableUser() {

        // Arrange
        User admin = new User("admin", "password", Role.ADMIN);
        User user = new User("user", "password", Role.USER);

        ReflectionTestUtils.setField(admin, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 2L);

        admin.setEnabled(true);
        user.setEnabled(true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        // Act
        User result = userService.updateUserEnabled(
                2L, false, "admin"
        );

        // Assert
        assertFalse(result.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void shouldAllowAdminToEnableUser() {

        User admin = new User("admin", "password", Role.ADMIN);
        User user = new User("user", "password", Role.USER);

        ReflectionTestUtils.setField(admin, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 2L);

        admin.setEnabled(true);
        user.setEnabled(false);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.updateUserEnabled(
                2L, true, "admin"
        );

        assertTrue(result.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectUserDisablingAccounts() {

        User user = new User("user", "password", Role.USER);
        user.setEnabled(true);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUserEnabled(
                        2L, false, "user"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectAgentDisablingAccounts() {

        User agent = new User("agent", "password", Role.AGENT);
        agent.setEnabled(true);

        when(userRepository.findByUsername("agent"))
                .thenReturn(Optional.of(agent));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUserEnabled(
                        2L, false, "agent"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectDisabledAdminChangingAccountStatus() {

        User admin = new User("admin", "password", Role.ADMIN);
        admin.setEnabled(false);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUserEnabled(
                        2L, false, "admin"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectAdminDisablingOwnAccount() {

        User admin = new User("admin", "password", Role.ADMIN);

        ReflectionTestUtils.setField(admin, "id", 1L);
        admin.setEnabled(true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalStateException.class,
                () -> userService.updateUserEnabled(
                        1L, false, "admin"
                )
        );

        assertTrue(admin.isEnabled());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectChangingStatusForNonExistingUser() {

        User admin = new User("admin", "password", Role.ADMIN);
        admin.setEnabled(true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserEnabled(
                        999L, false, "admin"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectRemovingLastActiveAdminRole() {

        User admin = new User("admin", "password", Role.ADMIN);
        User targetAdmin = new User("admin2", "password", Role.ADMIN);

        ReflectionTestUtils.setField(admin, "id", 1L);
        ReflectionTestUtils.setField(targetAdmin, "id", 2L);

        admin.setEnabled(true);
        targetAdmin.setEnabled(true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(targetAdmin));

        when(userRepository.countByRoleAndEnabledTrue(Role.ADMIN))
                .thenReturn(1L);

        assertThrows(
                IllegalStateException.class,
                () -> userService.updateUserRole(
                        2L,
                        Role.USER,
                        "admin"
                )
        );

        assertEquals(Role.ADMIN, targetAdmin.getRole());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectDisablingLastActiveAdmin() {

        User admin = new User("admin", "password", Role.ADMIN);
        User targetAdmin = new User("admin2", "password", Role.ADMIN);

        ReflectionTestUtils.setField(admin, "id", 1L);
        ReflectionTestUtils.setField(targetAdmin, "id", 2L);

        admin.setEnabled(true);
        targetAdmin.setEnabled(true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(targetAdmin));

        when(userRepository.countByRoleAndEnabledTrue(Role.ADMIN))
                .thenReturn(1L);

        assertThrows(
                IllegalStateException.class,
                () -> userService.updateUserEnabled(
                        2L,
                        false,
                        "admin"
                )
        );

        assertTrue(targetAdmin.isEnabled());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectBlankUsername() {

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        "   ",
                        "password123",
                        Role.USER
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectNullUsername() {

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        null,
                        "password123",
                        Role.USER
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectBlankPassword() {

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        "newuser",
                        "   ",
                        Role.USER
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectShortPassword() {

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        "newuser",
                        "1234567",
                        Role.USER
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectNullRoleWhenCreatingUser() {

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        "newuser",
                        "password123",
                        null
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldTrimUsernameWhenCreatingUser() {

        // Arrange
        when(userRepository.existsByUsername("newuser"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(
                "  newuser  ",
                "password123",
                Role.USER
        );

        // Assert
        assertEquals("newuser", result.getUsername());
        assertEquals("hashedPassword", result.getPassword());
        assertEquals(Role.USER, result.getRole());

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }


}
