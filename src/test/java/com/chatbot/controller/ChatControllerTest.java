package com.chatbot.controller;

import com.chatbot.dto.ChatRequest;
import com.chatbot.model.ChatMessage;
import com.chatbot.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstests fuer die Chat REST-API.
 * Testet alle Endpoints mit MockMvc, echter H2-Datenbank und Spring Security.
 *
 * Alle Tests verwenden @WithMockUser("user") fuer Authentifizierung.
 * Ohne Authentifizierung werden 401 Unauthorized zurueckgegeben.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // ==================== Security Tests ====================

    @Test
    void chat_returns401_withoutAuthentication() throws Exception {
        ChatRequest request = new ChatRequest("Hello");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHistory_returns401_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/chat/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clearHistory_returns401_withoutAuthentication() throws Exception {
        mockMvc.perform(delete("/api/chat/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void indexHtml_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("RuleBot Chat")));
    }

    // ==================== Authenticated Tests ====================

    @Test
    @WithMockUser("user")
    void chat_returns200_withValidInput() throws Exception {
        ChatRequest request = new ChatRequest("Hello");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.intent").value("GREETING"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser("user")
    void chat_returns400_withEmptyBody() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser("user")
    void chat_returns400_withBlankMessage() throws Exception {
        ChatRequest request = new ChatRequest("   ");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser("user")
    void chat_returns400_withTooLongMessage() throws Exception {
        ChatRequest request = new ChatRequest("a".repeat(501));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser("user")
    void chat_savesMessagesToDatabase() throws Exception {
        chatMessageRepository.deleteAll();

        ChatRequest request = new ChatRequest("How much is a t-shirt?");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Beide Nachrichten (USER + BOT) sollten gespeichert sein
        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getSender()).isEqualTo("USER");
        assertThat(messages.get(1).getSender()).isEqualTo("BOT");
    }

    @Test
    @WithMockUser("user")
    void chat_detectsPriceIntent() throws Exception {
        ChatRequest request = new ChatRequest("What is the price of jeans?");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("ASK_PRICE"))
                .andExpect(jsonPath("$.entities.product").value("jeans"));
    }

    @Test
    @WithMockUser("user")
    void chat_detectsOrderIntent() throws Exception {
        ChatRequest request = new ChatRequest("I want to order 3 pants");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("ORDER"))
                .andExpect(jsonPath("$.entities.product").value("pants"))
                .andExpect(jsonPath("$.entities.quantity").value("3"));
    }

    @Test
    @WithMockUser("user")
    void chat_detectsGoodbyeIntent() throws Exception {
        ChatRequest request = new ChatRequest("Bye!");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("GOODBYE"));
    }

    @Test
    @WithMockUser("user")
    void chat_detectsUnknownIntent() throws Exception {
        ChatRequest request = new ChatRequest("xyzabc123");

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("UNKNOWN"));
    }

    @Test
    @WithMockUser("user")
    void getHistory_returnsEmptyList_whenNoMessages() throws Exception {
        chatMessageRepository.deleteAll();

        mockMvc.perform(get("/api/chat/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser("user")
    void getHistory_returnsMessages() throws Exception {
        chatMessageRepository.deleteAll();

        // Chat-Nachricht senden
        ChatRequest request = new ChatRequest("Hello");
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        // Verlauf abrufen
        mockMvc.perform(get("/api/chat/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sender").value("USER"))
                .andExpect(jsonPath("$[1].sender").value("BOT"));
    }

    @Test
    @WithMockUser("user")
    void getHistory_withLimit() throws Exception {
        chatMessageRepository.deleteAll();

        // Mehrere Nachrichten senden (6 Messages insgesamt)
        for (int i = 0; i < 3; i++) {
            ChatRequest request = new ChatRequest("Message " + i);
            mockMvc.perform(post("/api/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        // Nur 2 Nachrichten anfordern (von 6)
        mockMvc.perform(get("/api/chat/history").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser("user")
    void clearHistory_deletesAllMessages() throws Exception {
        // Nachricht senden
        ChatRequest request = new ChatRequest("Hello");
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        assertThat(chatMessageRepository.count()).isGreaterThan(0);

        // Verlauf loeschen
        mockMvc.perform(delete("/api/chat/history"))
                .andExpect(status().isOk())
                .andExpect(content().string("Chat-Verlauf gelöscht"));

        assertThat(chatMessageRepository.count()).isZero();
    }
}
