package com.chatbot.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Konfiguration des ChatBots: Produktkatalog, Preise und Regeln.
 * Alle Daten sind hier zentral definiert — keine verteilte Konfiguration.
 */
public class BotConfig {

    /** Produktkatalog mit Preisen in Euro */
    private static final Map<String, Double> PRODUCT_PRICES = new HashMap<>();

    /** Liste der erkannten Farben */
    public static final String[] COLORS = {"blue", "red", "black", "white", "green", "yellow", "pink", "gray"};

    static {
        // Kleidung
        PRODUCT_PRICES.put("t-shirt", 25.0);
        PRODUCT_PRICES.put("shirt", 40.0);
        PRODUCT_PRICES.put("pants", 55.0);
        PRODUCT_PRICES.put("jeans", 60.0);
        PRODUCT_PRICES.put("jacket", 90.0);
        PRODUCT_PRICES.put("dress", 70.0);
        PRODUCT_PRICES.put("skirt", 45.0);
        PRODUCT_PRICES.put("sweater", 50.0);
        PRODUCT_PRICES.put("hoodie", 45.0);

        // Schuhe
        PRODUCT_PRICES.put("sneakers", 80.0);
        PRODUCT_PRICES.put("boots", 100.0);
        PRODUCT_PRICES.put("sandals", 35.0);

        // Accessoires
        PRODUCT_PRICES.put("hat", 20.0);
        PRODUCT_PRICES.put("scarf", 15.0);
        PRODUCT_PRICES.put("belt", 25.0);
        PRODUCT_PRICES.put("bag", 60.0);
    }

    /**
     * Gibt den Preis eines Produkts zurück.
     *
     * @param product Produktname (kleingeschrieben)
     * @return Preis in Euro oder null wenn Produkt nicht gefunden
     */
    public static Double getPrice(String product) {
        return PRODUCT_PRICES.get(product.toLowerCase());
    }

    /**
     * Gibt alle Produktnamen zurück.
     */
    public static String[] getProducts() {
        return PRODUCT_PRICES.keySet().toArray(new String[0]);
    }

    /**
     * Prüft ob ein Produkt im Katalog existiert.
     */
    public static boolean hasProduct(String product) {
        return PRODUCT_PRICES.containsKey(product.toLowerCase());
    }

    /**
     * Versandinformationen
     */
    public static final String SHIPPING_INFO = "Standard shipping takes 3-5 business days. Express shipping (1-2 days) is available for an extra 9.99 EUR.";
    public static final String RETURN_INFO = "You can return any item within 30 days of purchase. Items must be unused and in original packaging.";
}
