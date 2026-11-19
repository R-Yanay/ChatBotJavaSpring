package com.chatbot.controller;

import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.chatbot.model.ChatMessage;
import com.chatbot.service.ChatHistoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für Chat-Nachrichten.
 * Bietet POST /api/chat (Nachricht senden) und GET /api/chat/history (Verlauf laden).
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatHistoryService chatHistoryService;

    public ChatController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * Sendet eine Nachricht an den Bot und erhält die Antwort.
     * Beide Nachrichten (Nutzer und Bot) werden in der Datenbank gespeichert.
     *
     * @param request Die Nutzereingabe
     * @return Die Bot-Antwort
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Chat-Nachricht empfangen: {}", request.getMessage());
        ChatResponse response = chatHistoryService.processAndSave(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gibt den Chat-Verlauf zurück (letzte N Nachrichten).
     *
     * @param limit Anzahl der Nachrichten (Standard: 50, Maximum: 100)
     * @return Liste der Chat-Nachrichten in aufsteigender Zeitreihenfolge
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessage> messages = chatHistoryService.getHistory(limit);
        return ResponseEntity.ok(messages);
    }

    /**
     * Löscht den gesamten Chat-Verlauf.
     *
     * @return Bestätigung
     */
    @DeleteMapping("/history")
    public ResponseEntity<String> clearHistory() {
        chatHistoryService.clearHistory();
        return ResponseEntity.ok("Chat-Verlauf gelöscht");
    }
}
