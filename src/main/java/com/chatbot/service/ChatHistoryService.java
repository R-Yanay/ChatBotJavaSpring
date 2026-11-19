package com.chatbot.service;

import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.chatbot.model.ChatMessage;
import com.chatbot.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service fuer Chat-Verlauf.
 * Speichert jede Nachricht (Nutzer und Bot) in der Datenbank
 * und liefert den Chat-Verlauf zurueck.
 */
@Service
public class ChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);

    private static final int DEFAULT_HISTORY_LIMIT = 50;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;

    public ChatHistoryService(ChatMessageRepository chatMessageRepository, ChatService chatService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatService = chatService;
    }

    /**
     * Verarbeitet eine Chat-Nachricht und speichert sowohl Nutzereingabe als auch Bot-Antwort.
     *
     * @param request Die Nutzereingabe
     * @return Die Bot-Antwort
     */
    public ChatResponse processAndSave(ChatRequest request) {
        String message = request.getMessage();

        // 1. Nutzernachricht speichern
        ChatMessage userMessage = new ChatMessage(message, "USER", null);
        chatMessageRepository.save(userMessage);
        log.info("Nutzer-Nachricht gespeichert: {}", message);

        // 2. Bot-Antwort berechnen
        ChatResponse response = chatService.process(request);

        // 3. Bot-Antwort speichern
        ChatMessage botMessage = new ChatMessage(response.getResponse(), "BOT", response.getIntent());
        chatMessageRepository.save(botMessage);
        log.info("Bot-Antwort gespeichert: {}", response.getResponse());

        return response;
    }

    /**
     * Gibt die letzten N Chat-Nachrichten zurueck.
     *
     * @param limit Anzahl der Nachrichten (Standard: 50)
     * @return Liste der Chat-Nachrichten in aufsteigender Zeitreihenfolge
     */
    public List<ChatMessage> getHistory(int limit) {
        List<ChatMessage> messages = chatMessageRepository.findLatest();
        int max = Math.min(limit, 100);
        if (messages.size() > max) {
            messages = messages.subList(0, max);
        }
        Collections.reverse(messages);
        return messages;
    }

    /**
     * Gibt die letzten 50 Chat-Nachrichten zurueck.
     *
     * @return Liste der Chat-Nachrichten
     */
    public List<ChatMessage> getHistory() {
        return getHistory(DEFAULT_HISTORY_LIMIT);
    }

    /**
     * Loescht den gesamten Chat-Verlauf.
     */
    public void clearHistory() {
        chatMessageRepository.deleteAll();
        log.info("Chat-Verlauf geloescht");
    }
}
