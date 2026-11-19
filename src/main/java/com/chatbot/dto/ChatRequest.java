package com.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO für eine eingehende Chat-Nachricht.
 */
public class ChatRequest {

    @NotBlank(message = "Nachricht darf nicht leer sein")
    @Size(max = 500, message = "Nachricht darf maximal 500 Zeichen haben")
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
