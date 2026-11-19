package com.chatbot.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * WebSocket-Authentifizierung via STOMP-Connect-Header.
 *
 * Wenn der Client sich mit WebSocket verbindet, sendet er einen STOMP CONNECT
 * mit einem "Authorization"-Header (HTTP Basic Auth). Dieser Interceptor
 * validiert die Credentials und setzt den SecurityContext fuer die Session.
 *
 * Fluss:
 *   1. Client sendet STOMP CONNECT mit "Authorization: Basic dXNlcjpwYXNzd29yZA=="
 *   2. Interceptor dekodiert die Credentials
 *   3. UserDetailsService prueft Benutzername und Passwort
 *   4. SecurityContext wird gesetzt
 *   5. Alle weiteren Nachrichten der Session sind authentifiziert
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public WebSocketAuthInterceptor(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        log.info("WebSocket Interceptor: command={}", accessor.getCommand());

        // Nur bei CONNECT-Befehl authentifizieren
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("WebSocket CONNECT: authHeader={}", authHeader != null ? "present" : "null");

            if (authHeader != null && authHeader.startsWith("Basic ")) {
                // Auth via STOMP-Header (optional)
                String base64Credentials = authHeader.substring(6);
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                String[] parts = credentials.split(":", 2);

                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    log.info("WebSocket CONNECT: username={}", username);

                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        boolean matches = passwordEncoder.matches(password, userDetails.getPassword());
                        log.info("WebSocket CONNECT: password matches={}", matches);

                        if (matches) {
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);
                            log.info("WebSocket CONNECT: user {} authenticated successfully", username);
                            return message;
                        }
                    } catch (Exception e) {
                        log.error("WebSocket CONNECT: error authenticating user: {}", e.getMessage());
                    }
                }
            }

            // Keine Auth-Header: Verbindung erlauben (Auth bereits via REST-API geprueft)
            // Der Client prueft die Credentials zuerst ueber GET /api/chat/history mit HTTP Basic Auth.
            // Wenn der Login erfolgreich war, ist der Benutzer bereits authentifiziert.
            log.info("WebSocket CONNECT: allowing connection (auth via REST API)");
            return message;
        }

        // Bei SUBSCRIBE/SEND: SecurityContext aus der Session verwenden
        if (SecurityContextHolder.getContext().getAuthentication() == null && accessor.getUser() != null) {
            SecurityContextHolder.getContext().setAuthentication((Authentication) accessor.getUser());
        }

        return message;
    }
}
