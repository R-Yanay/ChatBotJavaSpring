package com.chatbot.repository;

import com.chatbot.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-Repository fuer Chat-Nachrichten.
 * Speichert und laedt Nachrichten aus der H2-Datenbank.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Gibt alle Nachrichten in absteigender Zeitreihenfolge zurueck.
     * Limit wird im Service-Layer angewendet.
     */
    @Query("SELECT m FROM ChatMessage m ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatest();
}
