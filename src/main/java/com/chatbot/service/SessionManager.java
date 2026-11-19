package com.chatbot.service;

import com.chatbot.model.ChatSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet die Sessions aller Benutzer.
 * Sessions werden im Speicher gehalten und nach 30 Minuten Inaktivitaet automatisch geloescht.
 */
@Service
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    /** Timeout in Millisekunden — 30 Minuten */
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000L;

    /** Alle aktiven Sessions, keyed by userId */
    private final ConcurrentHashMap<String, ChatSession> sessions = new ConcurrentHashMap<>();

    /**
     * Gibt die Session fuer die gegebene userId zurueck.
     * Wenn keine Session existiert oder sie abgelaufen ist, wird eine neue erstellt.
     *
     * @param userId Die Benutzer-ID
     * @return Die ChatSession fuer diesen Benutzer
     */
    public ChatSession getSession(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            userId = "anonymous";
        }

        // Bestehende Session holen
        ChatSession session = sessions.get(userId);
        if (session != null && !session.isExpired(SESSION_TIMEOUT_MS)) {
            session.touch();
            return session;
        }

        // Abgelaufene Session entfernen und neue erstellen
        if (session != null && session.isExpired(SESSION_TIMEOUT_MS)) {
            log.info("Session fuer {} abgelaufen, erstelle neue", userId);
            sessions.remove(userId);
        }

        session = new ChatSession(userId);
        sessions.put(userId, session);
        log.info("Neue Session erstellt fuer {} (gesamt: {})", userId, sessions.size());
        return session;
    }

    /**
     * Loescht die Session fuer die gegebene userId.
     *
     * @param userId Die Benutzer-ID
     * @return true, wenn die Session existierte und geloescht wurde
     */
    public boolean removeSession(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            userId = "anonymous";
        }
        ChatSession removed = sessions.remove(userId);
        if (removed != null) {
            log.info("Session fuer {} geloescht", userId);
            return true;
        }
        return false;
    }

    /**
     * Loescht alle abgelaufenen Sessions.
     * Wird periodisch aufgerufen, um Speicher zu freigegeben.
     *
     * @return Anzahl der geloeschten Sessions
     */
    public int cleanupExpiredSessions() {
        long timeout = SESSION_TIMEOUT_MS;
        int[] removed = {0};
        sessions.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired(timeout);
            if (expired) {
                removed[0]++;
            }
            return expired;
        });
        if (removed[0] > 0) {
            log.info("{} abgelaufene Sessions geloescht (verbleibend: {})", removed[0], sessions.size());
        }
        return removed[0];
    }

    /**
     * Gibt die Anzahl der aktuellen aktiven Sessions zurueck.
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
