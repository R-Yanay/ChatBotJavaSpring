package com.chatbot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket-Konfiguration mit STOMP-Protokoll.
 * Ermoglicht Echtzeit-Chat zwischen Browser und Server.
 *
 * Nachrichten-Fluss:
 *   Client → /chat/send (zum Server)
 *   Server → /chat/response (zuruck an den Client)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registriert den WebSocket-Endpunkt.
     * Der Client verbindet sich uber /ws mit SockJS-Fallback.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Konfiguriert den einfachen Message-Broker.
     * /chat/ wird vom Server an den Client gesendet (z.B. Bot-Antworten).
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/chat");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
