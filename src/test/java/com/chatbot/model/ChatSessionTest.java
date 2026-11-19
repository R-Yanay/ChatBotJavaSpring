package com.chatbot.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer ChatSession und SessionManager.
 */
class ChatSessionTest {

    private ChatSession session;

    @BeforeEach
    void setUp() {
        session = new ChatSession("user1");
    }

    @Test
    void constructor_setsDefaultValues() {
        assertEquals("user1", session.getUserId());
        assertNull(session.getLastMessage());
        assertNull(session.getLastIntent());
        assertTrue(session.getLastEntities().isEmpty());
        assertEquals(ChatSession.OrderStatus.IDLE, session.getOrderStatus());
        assertNull(session.getOrderProduct());
        assertNull(session.getOrderQuantity());
        assertNull(session.getOrderColor());
    }

    @Test
    void update_setsMessageIntentAndEntities() {
        Map<String, String> entities = new HashMap<>();
        entities.put("product", "shirt");
        session.update("I want a shirt", Intent.ORDER, entities);

        assertEquals("I want a shirt", session.getLastMessage());
        assertEquals(Intent.ORDER, session.getLastIntent());
        assertEquals("shirt", session.getLastEntities().get("product"));
    }

    @Test
    void update_handlesNullEntities() {
        session.update("Hello", Intent.GREETING, null);
        assertNotNull(session.getLastEntities());
        assertTrue(session.getLastEntities().isEmpty());
    }

    @Test
    void touch_updatesLastActive() {
        long before = session.getLastActive();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        session.touch();
        assertTrue(session.getLastActive() >= before);
    }

    @Test
    void isExpired_notExpiredImmediately() {
        assertFalse(session.isExpired(30 * 60 * 1000L));
    }

    @Test
    void isExpired_expiredAfterTimeout() {
        // Session mit sehr kurzem Timeout testen
        assertTrue(session.isExpired(-1L)); // negative Zeit = immer abgelaufen
    }

    @Test
    void resetOrder_clearsOrderFields() {
        session.setOrderStatus(ChatSession.OrderStatus.ASKING_PRODUCT);
        session.setOrderProduct("pants");
        session.setOrderQuantity(2);
        session.setOrderColor("blue");

        session.resetOrder();

        assertEquals(ChatSession.OrderStatus.IDLE, session.getOrderStatus());
        assertNull(session.getOrderProduct());
        assertNull(session.getOrderQuantity());
        assertNull(session.getOrderColor());
    }

    @Test
    void orderStatus_transitions() {
        session.setOrderStatus(ChatSession.OrderStatus.ASKING_PRODUCT);
        assertEquals(ChatSession.OrderStatus.ASKING_PRODUCT, session.getOrderStatus());

        session.setOrderStatus(ChatSession.OrderStatus.ASKING_CONFIRMATION);
        assertEquals(ChatSession.OrderStatus.ASKING_CONFIRMATION, session.getOrderStatus());

        session.setOrderStatus(ChatSession.OrderStatus.CONFIRMED);
        assertEquals(ChatSession.OrderStatus.CONFIRMED, session.getOrderStatus());
    }

    @Test
    void setLastEntities_createsCopy() {
        Map<String, String> original = new HashMap<>();
        original.put("product", "shirt");
        session.setLastEntities(original);

        // Original-Map aendern sollte Session nicht beeinflussen
        original.put("product", "pants");
        assertEquals("shirt", session.getLastEntities().get("product"));
    }

    @Test
    void userId_isImmutable() {
        assertEquals("user1", session.getUserId());
        // userId hat keinen Setter — ist immutable
    }
}
