
package se.lernia.helpdesk.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import se.lernia.helpdesk.entity.Role;
import se.lernia.helpdesk.service.UserService;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            UserService userService,
            Environment environment) {

        return args -> {

            // Skapa bara testkonton när dev-profilen är aktiv
            if (!environment.matchesProfiles("dev")) {
                return;
            }

            createIfMissing(userService, "user",
                    "DEV_USER_PASSWORD", Role.USER);

            createIfMissing(userService, "agent",
                    "DEV_AGENT_PASSWORD", Role.AGENT);

            createIfMissing(userService, "admin",
                    "DEV_ADMIN_PASSWORD", Role.ADMIN);
        };
    }

    private void createIfMissing(
            UserService userService,
            String username,
            String passwordVariable,
            Role role) {

        // Hämta lösenord från miljövariabler
        String password = System.getenv(passwordVariable);

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Missing environment variable: " + passwordVariable
            );
        }

        try {
            userService.createUser(username, password, role);
        } catch (IllegalArgumentException exception) {
            // Kontot finns redan - skapa inte en dubblett
        }
    }
}
