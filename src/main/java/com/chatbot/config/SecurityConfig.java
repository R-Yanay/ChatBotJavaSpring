package com.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Konfiguration mit HTTP Basic Auth.
 *
 * Sicherheitsregeln:
 * - REST-API (/api/*) erfordert Authentifizierung
 * - WebSocket-Endpoint (/ws/*) ist offen, Auth erfolgt im WebSocket-Layer
 * - Statische Dateien (index.html, CSS, JS) sind offen
 * - CSRF ist deaktiviert (stateless REST-API)
 *
 * Benutzer:
 * - user / password (Standard-Demo-Benutzer)
 */
@Configuration
public class SecurityConfig {

    /**
     * Definiert die Sicherheitsregeln fuer alle HTTP-Anfragen.
     * - Statische Ressourcen und WebSocket sind frei zugreifbar
     * - REST-API (/api/*) erfordert Authentifizierung
     * - WebSocket-Auth erfolgt im ChatWebSocketController via STOMP-Interceptors
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/error", "/ws/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {});
        return http.build();
    }

    /**
     * In-Memory Benutzer fuer Demo-Zwecke.
     * In einer echten Anwendung wuerde man eine Datenbank verwenden.
     *
     * Benutzer: user
     * Passwort: password
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
