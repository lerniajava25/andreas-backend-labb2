
package se.lernia.helpdesk.service;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.entity.User;
import se.lernia.helpdesk.repository.UserRepository;

@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String password, Role role) {


        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username cannot be empty"
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty"
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters"
            );
        }

        if (role == null) {
            throw new IllegalArgumentException(
                    "Role must be selected"
            );
        }

        username = username.trim();


        // Kontrollera om användarnamnet redan finns
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists: " + username
            );
        }

        // Hasha lösenordet
        String hashedPassword = passwordEncoder.encode(password);

        // Skapa användaren
        User user = new User(username, hashedPassword, role);

        // Spara användaren i databasen
        return userRepository.save(user);
    }

    public List<User> getAllUsers(String username) {

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only admins can view all users"
            );
        }

        return userRepository.findAll();
    }

    @Transactional
    public User updateUserRole(
            Long userId,
            Role newRole,
            String adminUsername
    ) {
        // Hämta den inloggade administratören
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin not found")
                );

        // Endast ADMIN får ändra roller
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only admins can change user roles"
            );
        }

        // Kontrollera att en giltig roll skickats in
        if (newRole == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }

        // Hämta användaren som ska ändras
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        // Förhindra att ADMIN ändrar sin egen roll
        if (user.getId().equals(admin.getId())) {
            throw new IllegalStateException(
                    "Admins cannot change their own role"
            );
        }

        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {

            long activeAdmins =
                    userRepository.countByRoleAndEnabledTrue(Role.ADMIN);

            if (user.isEnabled() && activeAdmins <= 1) {
                throw new IllegalStateException(
                        "Cannot remove the last active admin"
                );
            }
        }

        // Uppdatera rollen
        user.setRole(newRole);

        return userRepository.save(user);
    }


    @Transactional
    public User updateUserEnabled(
            Long userId,
            boolean enabled,
            String adminUsername
    ) {
        // Hämta den inloggade administratören
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin not found")
                );

        // Endast ADMIN får ändra kontostatus
        if (admin.getRole() != Role.ADMIN || !admin.isEnabled()) {
            throw new AccessDeniedException(
                    "Only active admins can change account status"
            );
        }

        // Hämta användaren som ska ändras
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        // Förhindra att administratören inaktiverar sig själv
        if (user.getId().equals(admin.getId()) && !enabled) {
            throw new IllegalStateException(
                    "Admins cannot disable their own account"
            );
        }


        if (user.getRole() == Role.ADMIN
                && user.isEnabled()
                && !enabled) {

            long activeAdmins =
                    userRepository.countByRoleAndEnabledTrue(Role.ADMIN);

            if (activeAdmins <= 1) {
                throw new IllegalStateException(
                        "Cannot disable the last active admin"
                );
            }
        }


        // Uppdatera kontots status
        user.setEnabled(enabled);

        return userRepository.save(user);
    }


}
