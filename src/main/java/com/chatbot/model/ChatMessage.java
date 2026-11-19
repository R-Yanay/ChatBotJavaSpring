package com.chatbot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA-Entity für Chat-Nachrichten.
 * Jede Nachricht (Nutzer und Bot) wird in der H2-Datenbank gespeichert.
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Der Text der Nachricht */
    @Column(nullable = false, length = 500)
    private String text;

    /** Sender: "USER" oder "BOT" */
    @Column(nullable = false, length = 10)
    private String sender;

    /** Erkannter Intent (nur bei Bot-Antworten) */
    @Enumerated(EnumType.STRING)
    private Intent intent;

    /** Zeitstempel der Nachricht */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String text, String sender, Intent intent) {
        this.text = text;
        this.sender = sender;
        this.intent = intent;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
