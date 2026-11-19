package com.chatbot.service;

import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.chatbot.model.ChatSession;
import com.chatbot.model.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Haupt-Service des ChatBots.
 * Koordiniert Intent-Erkennung, Entity-Extraktion und Antwort-Generierung.
 * Unterstuetzt ChatSession fuer Kontext-bewusste Antworten.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final IntentDetector intentDetector;
    private final EntityExtractor entityExtractor;
    private final ResponseGenerator responseGenerator;
    private final SessionManager sessionManager;

    public ChatService(IntentDetector intentDetector,
                       EntityExtractor entityExtractor,
                       ResponseGenerator responseGenerator,
                       SessionManager sessionManager) {
        this.intentDetector = intentDetector;
        this.entityExtractor = entityExtractor;
        this.responseGenerator = responseGenerator;
        this.sessionManager = sessionManager;
    }

    /**
     * Verarbeitet eine Chat-Nachricht und gibt die Bot-Antwort zurueck.
     *
     * @param request Die Nutzereingabe
     * @return Die Bot-Antwort mit Intent und Entities
     */
    public ChatResponse process(ChatRequest request) {
        return process(request, null);
    }

    /**
     * Verarbeitet eine Chat-Nachricht mit Session-Kontext.
     *
     * @param request Die Nutzereingabe
     * @param session Die ChatSession (wenn null, wird aus SessionManager geholt)
     * @return Die Bot-Antwort mit Intent und Entities
     */
    public ChatResponse process(ChatRequest request, ChatSession session) {
        String message = request.getMessage();
        log.info("Nutzer-Nachricht: {}", message);

        // Session holen oder erstellen
        if (session == null) {
            String userId = request.getUserId() != null ? request.getUserId() : "anonymous";
            session = sessionManager.getSession(userId);
        }

        // 1. Intent erkennen (mit Kontext)
        Intent intent = intentDetector.detect(message, session);

        // 2. Entities extrahieren (mit Kontext)
        Map<String, String> entities = entityExtractor.extract(message, session);

        // 3. Antwort generieren (mit Kontext)
        String response = responseGenerator.generate(intent, entities, session);

        // 4. Session aktualisieren
        session.update(message, intent, entities);

        // 5. Bestell-Workflow: bei neuem, unvereinbarem Intent Workflow zuruecksetzen
        // Wenn der Workflow aktiv ist (ASKING_PRODUCT/ASKING_CONFIRMATION) und ein
        // unvereinbarer Intent kommt (nicht ORDER, CONFIRM, CANCEL, UNKNOWN), resetten
        ChatSession.OrderStatus status = session.getOrderStatus();
        if (status == ChatSession.OrderStatus.ASKING_PRODUCT ||
            status == ChatSession.OrderStatus.ASKING_CONFIRMATION) {
            if (intent != Intent.ORDER && intent != Intent.CONFIRM &&
                intent != Intent.CANCEL && intent != Intent.UNKNOWN) {
                session.resetOrder();
            }
        }

        log.info("Bot-Antwort (Intent {}): {}", intent, response);
        return new ChatResponse(response, intent, entities);
    }
}
