package com.chatbot.service;

import com.chatbot.model.Intent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für die Antwort-Generierung.
 */
class ResponseGeneratorTest {

    private final ResponseGenerator generator = new ResponseGenerator();

    @Test
    void greeting_returnsGreeting() {
        String response = generator.generate(Intent.GREETING, Map.of());
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

    @Test
    void askPrice_withProduct_returnsPrice() {
        String response = generator.generate(Intent.ASK_PRICE, Map.of("product", "t-shirt"));
        assertTrue(response.contains("25.00"));
        assertTrue(response.contains("t-shirt"));
    }

    @Test
    void askPrice_withoutProduct_returnsProductList() {
        String response = generator.generate(Intent.ASK_PRICE, Map.of());
        assertTrue(response.contains("not sure"));
    }

    @Test
    void order_withProduct_returnsOrderInfo() {
        String response = generator.generate(Intent.ORDER,
                Map.of("product", "jeans", "quantity", "2", "color", "blue"));
        assertTrue(response.contains("jeans"));
        assertTrue(response.contains("blue"));
        assertTrue(response.contains("2"));
        assertTrue(response.contains("EUR"));
    }

    @Test
    void order_withoutProduct_returnsProductList() {
        String response = generator.generate(Intent.ORDER, Map.of());
        assertTrue(response.contains("order") || response.contains("not sure"));
    }

    @Test
    void confirm_returnsConfirmation() {
        String response = generator.generate(Intent.CONFIRM, Map.of());
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

    @Test
    void cancel_returnsCancellation() {
        String response = generator.generate(Intent.CANCEL, Map.of());
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

    @Test
    void goodbye_returnsGoodbye() {
        String response = generator.generate(Intent.GOODBYE, Map.of());
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

    @Test
    void help_returnsHelpInfo() {
        String response = generator.generate(Intent.HELP, Map.of());
        assertTrue(response.toLowerCase().contains("price"));
        assertTrue(response.toLowerCase().contains("order"));
    }

    @Test
    void search_withProduct_returnsSearchResult() {
        String response = generator.generate(Intent.SEARCH,
                Map.of("product", "sneakers", "color", "red"));
        assertTrue(response.contains("sneakers"));
        assertTrue(response.contains("red"));
    }

    @Test
    void return_returnsReturnInfo() {
        String response = generator.generate(Intent.RETURN, Map.of());
        assertTrue(response.contains("return"));
    }

    @Test
    void shipping_returnsShippingInfo() {
        String response = generator.generate(Intent.SHIPPING, Map.of());
        assertTrue(response.contains("shipping"));
    }

    @Test
    void unknown_returnsFallback() {
        String response = generator.generate(Intent.UNKNOWN, Map.of());
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }
}
