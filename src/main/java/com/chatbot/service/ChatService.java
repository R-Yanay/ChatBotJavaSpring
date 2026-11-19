package com.chatbot.service;

import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.chatbot.model.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Haupt-Service des ChatBots.
 * Koordiniert Intent-Erkennung, Entity-Extraktion und Antwort-Generierung.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final IntentDetector intentDetector;
    private final EntityExtractor entityExtractor;
    private final ResponseGenerator responseGenerator;

    public ChatService(IntentDetector intentDetector,
                       EntityExtractor entityExtractor,
                       ResponseGenerator responseGenerator) {
        this.intentDetector = intentDetector;
        this.entityExtractor = entityExtractor;
        this.responseGenerator = responseGenerator;
    }

    /**
     * Verarbeitet eine Chat-Nachricht und gibt die Bot-Antwort zurück.
     *
     * @param request Die Nutzereingabe
     * @return Die Bot-Antwort mit Intent und Entities
     */
    public ChatResponse process(ChatRequest request) {
        String message = request.getMessage();
        log.info("Nutzer-Nachricht: {}", message);

        // 1. Intent erkennen
        Intent intent = intentDetector.detect(message);

        // 2. Entities extrahieren
        Map<String, String> entities = entityExtractor.extract(message);

        // 3. Antwort generieren
        String response = responseGenerator.generate(intent, entities);

        log.info("Bot-Antwort (Intent {}): {}", intent, response);
        return new ChatResponse(response, intent, entities);
    }
}
