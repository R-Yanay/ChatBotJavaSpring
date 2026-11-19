package com.chatbot.controller;

import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.chatbot.service.ChatHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket-Controller fur Echtzeit-Chat.
 * Empfangt Nachrichten vom Client und sendet Bot-Antworten zuruck.
 *
 * Nachrichten-Fluss:
 *   Client sendet an /app/chat/send
 *   Server antwortet an /chat/response
 */
@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatHistoryService chatHistoryService;

    public ChatWebSocketController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * Empfangt eine Chat-Nachricht vom Client uber WebSocket.
     * Verarbeitet die Nachricht und sendet die Bot-Antwort an /chat/response.
     *
     * @param request Die Nutzereingabe
     * @return Die Bot-Antwort
     */
    @MessageMapping("/chat/send")
    @SendTo("/chat/response")
    public ChatResponse handleWebSocketMessage(ChatRequest request) {
        log.info("WebSocket-Nachricht empfangen: {}", request.getMessage());
        ChatResponse response = chatHistoryService.processAndSave(request);
        log.info("WebSocket-Antwort gesendet: {}", response.getResponse());
        return response;
    }
}
