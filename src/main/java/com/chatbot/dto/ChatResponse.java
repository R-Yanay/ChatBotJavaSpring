package com.chatbot.dto;

import com.chatbot.model.Intent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO für eine Bot-Antwort.
 */
public class ChatResponse {

    private String response;
    private Intent intent;
    private Map<String, String> entities;
    private LocalDateTime timestamp;

    public ChatResponse() {
    }

    public ChatResponse(String response, Intent intent, Map<String, String> entities) {
        this.response = response;
        this.intent = intent;
        this.entities = entities;
        this.timestamp = LocalDateTime.now();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Map<String, String> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, String> entities) {
        this.entities = entities;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
