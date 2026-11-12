
package se.lernia.helpdesk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // Publika resurser
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Endast ADMIN
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // AGENT och ADMIN
                        .requestMatchers("/agent/**")
                        .hasAnyRole("AGENT", "ADMIN")

                        // Inloggade användare
                        .requestMatchers("/tickets/**")
                        .authenticated()

                        // Övriga sidor kräver inloggning
                        .anyRequest().authenticated()
                )

                // Spring Security hanterar inloggningen
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/tickets", true)
                        .permitAll()
                )

                // Utloggning
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
