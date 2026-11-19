package com.chatbot.service;

import com.chatbot.config.BotConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für die Entity-Extraktion.
 * Prüft, ob Produktnamen, Farben und Mengen korrekt erkannt werden.
 */
class EntityExtractorTest {

    private final EntityExtractor extractor = new EntityExtractor();

    @Test
    void extractProduct_tShirt() {
        Map<String, String> entities = extractor.extract("I want a t-shirt");
        assertEquals("t-shirt", entities.get("product"));
    }

    @Test
    void extractProduct_jeans() {
        Map<String, String> entities = extractor.extract("How much are jeans?");
        assertEquals("jeans", entities.get("product"));
    }

    @Test
    void extractProduct_longestMatch() {
        // "t-shirt" sollte vor "shirt" erkannt werden (längeres Match)
        Map<String, String> entities = extractor.extract("I want a t-shirt");
        assertEquals("t-shirt", entities.get("product"));
    }

    @Test
    void extractColor_blue() {
        Map<String, String> entities = extractor.extract("Show me blue sneakers");
        assertEquals("blue", entities.get("color"));
    }

    @Test
    void extractColor_red() {
        Map<String, String> entities = extractor.extract("I want a red dress");
        assertEquals("red", entities.get("color"));
    }

    @Test
    void extractQuantity_digit() {
        Map<String, String> entities = extractor.extract("Order 3 pants");
        assertEquals("3", entities.get("quantity"));
    }

    @Test
    void extractQuantity_word() {
        Map<String, String> entities = extractor.extract("I want two jackets");
        assertEquals("2", entities.get("quantity"));
    }

    @Test
    void extractAllEntities() {
        Map<String, String> entities = extractor.extract("I want to order 2 blue sneakers");
        assertEquals("sneakers", entities.get("product"));
        assertEquals("blue", entities.get("color"));
        assertEquals("2", entities.get("quantity"));
    }

    @Test
    void extractEmpty() {
        Map<String, String> entities = extractor.extract("");
        assertTrue(entities.isEmpty());
    }

    @Test
    void extractNull() {
        Map<String, String> entities = extractor.extract(null);
        assertTrue(entities.isEmpty());
    }

    @Test
    void extractNoMatch() {
        Map<String, String> entities = extractor.extract("Hello world");
        assertTrue(entities.isEmpty());
    }

    @Test
    void botConfig_prices() {
        assertEquals(25.0, BotConfig.getPrice("t-shirt"));
        assertEquals(60.0, BotConfig.getPrice("jeans"));
        assertEquals(80.0, BotConfig.getPrice("sneakers"));
    }

    @Test
    void botConfig_hasProduct() {
        assertTrue(BotConfig.hasProduct("t-shirt"));
        assertTrue(BotConfig.hasProduct("jacket"));
        assertFalse(BotConfig.hasProduct("bicycle"));
    }

    @Test
    void botConfig_caseInsensitive() {
        assertEquals(25.0, BotConfig.getPrice("T-SHIRT"));
        assertEquals(25.0, BotConfig.getPrice("T-shirt"));
    }

    @Test
    void botConfig_getProducts() {
        String[] products = BotConfig.getProducts();
        assertTrue(products.length > 0);
    }
}
