package com.hivehub.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Handshake mapping as public
                        .requestMatchers("/api/handshake").permitAll()
                        .requestMatchers("/hivehub/**").permitAll() //Line just to test the endpoints till we get done with the login

                        // Any other request will require authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}