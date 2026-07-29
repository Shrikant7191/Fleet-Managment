package com.fleman.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Spring Security is included only for BCryptPasswordEncoder — real password
// hashing for Customer.passwordHash — not for endpoint-level authorization.
// Every endpoint is intentionally left open (permitAll) because the current
// frontend never attaches a token to its requests (see JwtUtil's note).
// Adding a JwtAuthFilter that reads Authorization: Bearer <token> and
// restricts /api/customers/**, /api/bookings/me etc. to the matching
// customerId is the natural next step, not implemented here to match
// today's actual frontend behaviour.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
