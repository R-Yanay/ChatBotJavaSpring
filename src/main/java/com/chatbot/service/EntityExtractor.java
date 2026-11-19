package com.chatbot.service;

import com.chatbot.config.BotConfig;
import com.chatbot.model.ChatSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrahiert wichtige Informationen (Entities) aus einer Nachricht.
 * Erkennt Produktnamen, Farben und Mengen.
 * Unterstuetzt Pronomen-Aufloesung via ChatSession.
 */
@Service
public class EntityExtractor {

    /**
     * Extrahiert alle Entities aus einer Nachricht.
     *
     * @param message Die Nutzereingabe
     * @return Map mit Entity-Namen und Werten (z.B. "product" → "t-shirt")
     */
    public Map<String, String> extract(String message) {
        return extract(message, null);
    }

    /**
     * Extrahiert alle Entities aus einer Nachricht mit Kontext aus der ChatSession.
     * Loest Pronomen ("it", "that") auf die letzten Entities auf.
     *
     * @param message Die Nutzereingabe
     * @param session Die ChatSession mit Kontext (oder null)
     * @return Map mit Entity-Namen und Werten
     */
    public Map<String, String> extract(String message, ChatSession session) {
        Map<String, String> entities = new HashMap<>();

        if (message == null || message.trim().isEmpty()) {
            return entities;
        }

        // Pronomen: "it", "that", "them" -> letzte Entities verwenden
        if (isPronounOnly(message) && session != null) {
            return new HashMap<>(session.getLastEntities());
        }

        String lower = message.toLowerCase();

        // Produkt erkennen
        String product = extractProduct(lower);
        if (product != null) {
            entities.put("product", product);
        }

        // Farbe erkennen
        String color = extractColor(lower);
        if (color != null) {
            entities.put("color", color);
        }

        // Menge erkennen
        Integer quantity = extractQuantity(lower);
        if (quantity != null) {
            entities.put("quantity", quantity.toString());
        }

        return entities;
    }

    /**
     * Prueft, ob die Nachricht nur ein Pronomen ist.
     */
    private boolean isPronounOnly(String message) {
        String lower = message.toLowerCase().trim();
        return lower.equals("it") || lower.equals("that") || lower.equals("them")
                || lower.equals("this") || lower.equals("those");
    }

    /**
     * Erkennt den Produktnamen in der Nachricht.
     * Sucht nach den längsten Treffer, um "t-shirt" vor "shirt" zu bevorzugen.
     */
    private String extractProduct(String message) {
        String[] products = BotConfig.getProducts();
        String bestMatch = null;
        int bestLength = 0;

        for (String product : products) {
            if (message.contains(product) && product.length() > bestLength) {
                bestMatch = product;
                bestLength = product.length();
            }
        }

        return bestMatch;
    }

    /**
     * Erkennt die Farbe in der Nachricht.
     */
    private String extractColor(String message) {
        for (String color : BotConfig.COLORS) {
            if (message.contains(color)) {
                return color;
            }
        }
        return null;
    }

    /**
     * Erkennt die Menge in der Nachricht (z.B. "2 pants", "three shirts").
     */
    private Integer extractQuantity(String message) {
        // Ziffern erkennen: "2 pants", "order 3 items"
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        // Geschriebene Zahlen: "one", "two", "three", ...
        if (message.contains("one") || message.contains("1")) return 1;
        if (message.contains("two") || message.contains("2")) return 2;
        if (message.contains("three") || message.contains("3")) return 3;
        if (message.contains("four") || message.contains("4")) return 4;
        if (message.contains("five") || message.contains("5")) return 5;
        if (message.contains("six") || message.contains("6")) return 6;
        if (message.contains("seven") || message.contains("7")) return 7;
        if (message.contains("eight") || message.contains("8")) return 8;
        if (message.contains("nine") || message.contains("9")) return 9;
        if (message.contains("ten") || message.contains("10")) return 10;

        return null;
    }
}
