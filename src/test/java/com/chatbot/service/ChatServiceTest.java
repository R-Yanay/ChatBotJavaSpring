package com.chatbot.service;

import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.chatbot.model.ChatSession;
import com.chatbot.model.Intent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer ChatService mit Session-Kontext.
 */
class ChatServiceTest {

    private SessionManager sessionManager;
    private IntentDetector intentDetector;
    private EntityExtractor entityExtractor;
    private ResponseGenerator responseGenerator;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        intentDetector = new IntentDetector();
        entityExtractor = new EntityExtractor();
        responseGenerator = new ResponseGenerator();
        chatService = new ChatService(intentDetector, entityExtractor, responseGenerator, sessionManager);
    }

    @Test
    void process_basicGreeting() {
        ChatRequest request = new ChatRequest("Hello");
        ChatResponse response = chatService.process(request);
        assertNotNull(response);
        assertEquals(Intent.GREETING, response.getIntent());
    }

    @Test
    void process_pronounResolvesToLastIntent() {
        // Erste Nachricht: Preisfrage
        ChatRequest request1 = new ChatRequest("How much is a shirt?");
        request1.setUserId("user1");
        ChatResponse response1 = chatService.process(request1);
        assertEquals(Intent.ASK_PRICE, response1.getIntent());

        // Zweite Nachricht: Pronomen "it"
        ChatRequest request2 = new ChatRequest("it");
        request2.setUserId("user1");
        ChatResponse response2 = chatService.process(request2);
        assertEquals(Intent.ASK_PRICE, response2.getIntent());
    }

    @Test
    void process_followUpResolvesToLastIntent() {
        // Erste Nachricht: Preisfrage
        ChatRequest request1 = new ChatRequest("How much is a shirt?");
        request1.setUserId("user1");
        chatService.process(request1);

        // Zweite Nachricht: Follow-up "and pants?"
        ChatRequest request2 = new ChatRequest("and pants?");
        request2.setUserId("user1");
        ChatResponse response2 = chatService.process(request2);
        assertEquals(Intent.ASK_PRICE, response2.getIntent());
    }

    @Test
    void process_sessionStoredAndRetrieved() {
        ChatRequest request = new ChatRequest("Hello");
        request.setUserId("user1");
        chatService.process(request);

        ChatSession session = sessionManager.getSession("user1");
        assertNotNull(session);
        assertEquals(Intent.GREETING, session.getLastIntent());
    }

    @Test
    void process_differentUsersDifferentSessions() {
        ChatRequest request1 = new ChatRequest("How much is a shirt?");
        request1.setUserId("user1");
        chatService.process(request1);

        ChatRequest request2 = new ChatRequest("How much is a pants?");
        request2.setUserId("user2");
        chatService.process(request2);

        ChatSession session1 = sessionManager.getSession("user1");
        ChatSession session2 = sessionManager.getSession("user2");
        assertNotSame(session1, session2);
    }

    @Test
    void process_orderWorkflow_startsAskingProduct() {
        ChatRequest request = new ChatRequest("I want to order");
        request.setUserId("user1");
        ChatResponse response = chatService.process(request);

        ChatSession session = sessionManager.getSession("user1");
        assertEquals(ChatSession.OrderStatus.ASKING_PRODUCT, session.getOrderStatus());
    }

    @Test
    void process_orderWorkflow_productProvided() {
        // Schritt 1: Order ohne Produkt
        ChatRequest request1 = new ChatRequest("I want to order");
        request1.setUserId("user1");
        chatService.process(request1);

        // Schritt 2: Produkt angeben
        ChatRequest request2 = new ChatRequest("a shirt");
        request2.setUserId("user1");
        ChatResponse response2 = chatService.process(request2);

        ChatSession session = sessionManager.getSession("user1");
        assertEquals(ChatSession.OrderStatus.ASKING_CONFIRMATION, session.getOrderStatus());
        assertEquals("shirt", session.getOrderProduct());
    }

    @Test
    void process_orderWorkflow_confirm() {
        // Order starten
        ChatRequest request1 = new ChatRequest("I want to order");
        request1.setUserId("user1");
        chatService.process(request1);

        // Produkt angeben
        ChatRequest request2 = new ChatRequest("a shirt");
        request2.setUserId("user1");
        chatService.process(request2);

        // Bestaetigen
        ChatRequest request3 = new ChatRequest("yes");
        request3.setUserId("user1");
        ChatResponse response3 = chatService.process(request3);

        assertTrue(response3.getResponse().contains("confirmed"));
        ChatSession session = sessionManager.getSession("user1");
        assertEquals(ChatSession.OrderStatus.IDLE, session.getOrderStatus());
    }

    @Test
    void process_orderWorkflow_cancel() {
        // Order starten
        ChatRequest request1 = new ChatRequest("I want to order");
        request1.setUserId("user1");
        chatService.process(request1);

        // Produkt angeben
        ChatRequest request2 = new ChatRequest("a shirt");
        request2.setUserId("user1");
        chatService.process(request2);

        // Abbrechen
        ChatRequest request3 = new ChatRequest("no");
        request3.setUserId("user1");
        ChatResponse response3 = chatService.process(request3);

        ChatSession session = sessionManager.getSession("user1");
        assertEquals(ChatSession.OrderStatus.IDLE, session.getOrderStatus());
    }

    @Test
    void process_newIntentResetsOrderWorkflow() {
        // Order starten
        ChatRequest request1 = new ChatRequest("I want to order");
        request1.setUserId("user1");
        chatService.process(request1);

        ChatSession session = sessionManager.getSession("user1");
        assertEquals(ChatSession.OrderStatus.ASKING_PRODUCT, session.getOrderStatus());

        // Neues Thema: Greeting
        ChatRequest request2 = new ChatRequest("Hello");
        request2.setUserId("user1");
        chatService.process(request2);

        session = sessionManager.getSession("user1");
        assertEquals(ChatSession.OrderStatus.IDLE, session.getOrderStatus());
    }

    @Test
    void process_nullUserId_defaultsToAnonymous() {
        ChatRequest request = new ChatRequest("Hello");
        chatService.process(request);

        ChatSession session = sessionManager.getSession("anonymous");
        assertNotNull(session);
        assertEquals(Intent.GREETING, session.getLastIntent());
    }

    @Test
    void process_entitiesExtractedAndStored() {
        ChatRequest request = new ChatRequest("How much is a blue shirt?");
        request.setUserId("user1");
        chatService.process(request);

        ChatSession session = sessionManager.getSession("user1");
        assertEquals("shirt", session.getLastEntities().get("product"));
        assertEquals("blue", session.getLastEntities().get("color"));
    }

    @Test
    void process_priceWithPronoun() {
        // Erste Nachricht: Preisfrage mit Produkt
        ChatRequest request1 = new ChatRequest("How much is a shirt?");
        request1.setUserId("user1");
        chatService.process(request1);

        // Zweite Nachricht: Pronomen
        ChatRequest request2 = new ChatRequest("it");
        request2.setUserId("user1");
        ChatResponse response2 = chatService.process(request2);

        assertTrue(response2.getResponse().contains("shirt"));
    }

    @Test
    void process_followUpPrice() {
        // Erste Nachricht: Preisfrage
        ChatRequest request1 = new ChatRequest("How much is a shirt?");
        request1.setUserId("user1");
        chatService.process(request1);

        // Zweite Nachricht: Follow-up
        ChatRequest request2 = new ChatRequest("and pants?");
        request2.setUserId("user1");
        ChatResponse response2 = chatService.process(request2);

        assertTrue(response2.getResponse().contains("pants"));
    }

    @Test
    void process_sessionUpdatedAfterEachMessage() {
        ChatRequest request1 = new ChatRequest("Hello");
        request1.setUserId("user1");
        chatService.process(request1);

        ChatRequest request2 = new ChatRequest("How much is a shirt?");
        request2.setUserId("user1");
        chatService.process(request2);

        ChatSession session = sessionManager.getSession("user1");
        assertEquals(Intent.ASK_PRICE, session.getLastIntent());
        assertEquals("How much is a shirt?", session.getLastMessage());
    }
}
