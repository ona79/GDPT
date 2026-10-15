package com.gdpt.gdpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints — /sites n'est mappé dans aucun contrôleur, retiré
                .requestMatchers("/", "/sites/details/**", "/login", "/register", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                // Access to H2 Console (for debugging and dev purposes)
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                // L'annulation est accessible à l'admin (depuis admin/reservations.html) ET au visiteur
                // IMPORTANT : cette règle doit précéder la règle générale /visiteur/** (Spring applique la première règle qui matche)
                .requestMatchers("/visiteur/reservation/cancel/**").hasAnyAuthority("ROLE_VISITEUR", "ROLE_ADMIN")
                // Toutes les autres routes visiteur sont réservées au rôle VISITEUR (pas ADMIN)
                .requestMatchers("/visiteur/**").hasAuthority("ROLE_VISITEUR")
                // Guide dashboard and endpoints
                .requestMatchers("/guide/**").hasAuthority("ROLE_GUIDE")
                // Admin dashboard and endpoints
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            // Fix frame options to allow H2 console
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            // Disable CSRF for H2 Console
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
