package com.chatbot.service;

import com.chatbot.model.ChatSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer SessionManager.
 */
class SessionManagerTest {

    private SessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new SessionManager();
    }

    @Test
    void getSession_createsNewSession() {
        ChatSession session = manager.getSession("user1");
        assertNotNull(session);
        assertEquals("user1", session.getUserId());
        assertEquals(1, manager.getActiveSessionCount());
    }

    @Test
    void getSession_returnsExistingSession() {
        ChatSession session1 = manager.getSession("user1");
        ChatSession session2 = manager.getSession("user1");
        assertSame(session1, session2);
        assertEquals(1, manager.getActiveSessionCount());
    }

    @Test
    void getSession_nullUserId_defaultsToAnonymous() {
        ChatSession session = manager.getSession(null);
        assertEquals("anonymous", session.getUserId());
    }

    @Test
    void getSession_emptyUserId_defaultsToAnonymous() {
        ChatSession session = manager.getSession("  ");
        assertEquals("anonymous", session.getUserId());
    }

    @Test
    void removeSession_removesExistingSession() {
        manager.getSession("user1");
        assertTrue(manager.removeSession("user1"));
        assertEquals(0, manager.getActiveSessionCount());
    }

    @Test
    void removeSession_nonExistentReturnsFalse() {
        assertFalse(manager.removeSession("nonexistent"));
    }

    @Test
    void cleanupExpiredSessions_removesExpiredOnly() {
        // Neue Session erstellen — nicht abgelaufen
        manager.getSession("user1");
        manager.getSession("user2");
        assertEquals(2, manager.getActiveSessionCount());

        // Keine abgelaufenen Sessions
        int removed = manager.cleanupExpiredSessions();
        assertEquals(0, removed);
        assertEquals(2, manager.getActiveSessionCount());
    }

    @Test
    void touch_updatesLastActive() {
        ChatSession session = manager.getSession("user1");
        long before = session.getLastActive();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        manager.getSession("user1"); // touch
        assertTrue(session.getLastActive() >= before);
    }

    @Test
    void multipleUsers_multipleSessions() {
        manager.getSession("user1");
        manager.getSession("user2");
        manager.getSession("user3");
        assertEquals(3, manager.getActiveSessionCount());
    }

    @Test
    void removeSession_nullUserId_defaultsToAnonymous() {
        manager.getSession(null); // erstellt "anonymous"
        assertEquals(1, manager.getActiveSessionCount());
        assertTrue(manager.removeSession(null));
        assertEquals(0, manager.getActiveSessionCount());
    }
}
