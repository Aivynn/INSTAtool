package com.socialmedia.parser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Modern style for disabling CSRF
            .authorizeHttpRequests(
                auth -> auth.requestMatchers("/api/submit", "/api/media/**").permitAll().anyRequest().permitAll() // or .authenticated() if needed
            );

        return http.build();
    }
}
