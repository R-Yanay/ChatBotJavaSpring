package com.chatbot.service;

import com.chatbot.config.BotConfig;
import com.chatbot.model.ChatSession;
import com.chatbot.model.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generiert die Antwort des ChatBots basierend auf erkanntem Intent und Entities.
 * Jeder Intent hat mehrere vordefinierte Antworten — eine wird zufaellig ausgewaehlt.
 * Unterstuetzt ChatSession fuer Bestell-Workflow und Kontext-bewusste Antworten.
 */
@Service
public class ResponseGenerator {

    private static final Logger log = LoggerFactory.getLogger(ResponseGenerator.class);
    private static final Random random = new Random();

    /**
     * Generiert eine Antwort basierend auf Intent und extrahierten Entities.
     *
     * @param intent    Der erkannte Intent
     * @param entities  Extrahierte Entities (Produkt, Farbe, Menge)
     * @return Die Antwort des Bots als Text
     */
    public String generate(Intent intent, Map<String, String> entities) {
        return generate(intent, entities, null);
    }

    /**
     * Generiert eine Antwort basierend auf Intent, Entities und ChatSession-Kontext.
     *
     * @param intent    Der erkannte Intent
     * @param entities  Extrahierte Entities (Produkt, Farbe, Menge)
     * @param session   Die ChatSession fuer Kontext (oder null)
     * @return Die Antwort des Bots als Text
     */
    public String generate(Intent intent, Map<String, String> entities, ChatSession session) {
        // Bestell-Workflow: wenn Bot nach Produkt fragt und Nutzer antwortet
        if (session != null && session.getOrderStatus() == ChatSession.OrderStatus.ASKING_PRODUCT) {
            return handleOrderWorkflow(session, entities);
        }

        // Bestell-Workflow: wenn Bot nach Bestaetigung fragt
        if (session != null && session.getOrderStatus() == ChatSession.OrderStatus.ASKING_CONFIRMATION) {
            return handleConfirmationWorkflow(session, intent);
        }

        String response = switch (intent) {
            case GREETING -> randomChoice(
                    "Hello! How can I help you today?",
                    "Hi there! What can I do for you?",
                    "Hey! Welcome to our shop. How can I assist you?"
            );
            case ASK_PRICE -> generatePriceResponse(entities, session);
            case ORDER -> generateOrderResponse(entities, session);
            case CONFIRM -> randomChoice(
                    "Great! Your order has been confirmed. Thank you for shopping with us!",
                    "Perfect! I've processed your order. You'll receive a confirmation email shortly.",
                    "Done! Your order is on its way. Is there anything else I can help with?"
            );
            case CANCEL -> randomChoice(
                    "No problem, I've cancelled that. Is there anything else you need?",
                    "Cancelled. Let me know if you change your mind!",
                    "Alright, no worries. I'm here if you need anything else."
            );
            case GOODBYE -> randomChoice(
                    "Goodbye! Have a great day!",
                    "See you later! Happy shopping!",
                    "Bye! Don't hesitate to come back if you need help."
            );
            case HELP -> generateHelpResponse();
            case SEARCH -> generateSearchResponse(entities);
            case RETURN -> BotConfig.RETURN_INFO;
            case SHIPPING -> BotConfig.SHIPPING_INFO;
            case UNKNOWN -> randomChoice(
                    "I'm sorry, I didn't understand that. Could you rephrase?",
                    "Hmm, I'm not sure what you mean. Try asking about prices, orders, or shipping.",
                    "I didn't catch that. You can ask me about product prices, place orders, or check shipping info."
            );
        };

        log.debug("Antwort fuer Intent {}: {}", intent, response);
        return response;
    }

    /**
     * Handhabt den Bestell-Workflow: Bot fragt nach Produkt.
     */
    private String handleOrderWorkflow(ChatSession session, Map<String, String> entities) {
        String product = entities.get("product");
        if (product != null && BotConfig.hasProduct(product)) {
            session.setOrderProduct(product);
            String color = entities.get("color");
            if (color != null) {
                session.setOrderColor(color);
            }
            String quantity = entities.get("quantity");
            if (quantity != null) {
                session.setOrderQuantity(Integer.parseInt(quantity));
            }
            session.setOrderStatus(ChatSession.OrderStatus.ASKING_CONFIRMATION);

            StringBuilder sb = new StringBuilder();
            sb.append("You want to order ");
            if (session.getOrderQuantity() != null) {
                sb.append(session.getOrderQuantity()).append(" ");
            }
            if (session.getOrderColor() != null) {
                sb.append(session.getOrderColor()).append(" ");
            }
            sb.append(session.getOrderProduct());
            double price = BotConfig.getPrice(session.getOrderProduct());
            int qty = session.getOrderQuantity() != null ? session.getOrderQuantity() : 1;
            sb.append(String.format(". Total: %.2f EUR. ", price * qty));
            sb.append("Shall I confirm your order?");
            return sb.toString();
        }
        return "I'm not sure which product you mean. We have: " + String.join(", ", BotConfig.getProducts());
    }

    /**
     * Handhabt den Bestaetigungs-Workflow: Bot fragt nach Bestaetigung.
     */
    private String handleConfirmationWorkflow(ChatSession session, Intent intent) {
        if (intent == Intent.CONFIRM) {
            session.setOrderStatus(ChatSession.OrderStatus.CONFIRMED);
            StringBuilder sb = new StringBuilder();
            sb.append("Great! Your order for ");
            if (session.getOrderQuantity() != null) {
                sb.append(session.getOrderQuantity()).append(" ");
            }
            if (session.getOrderColor() != null) {
                sb.append(session.getOrderColor()).append(" ");
            }
            sb.append(session.getOrderProduct()).append(" has been confirmed!");
            sb.append(" Thank you for shopping with us.");
            session.resetOrder();
            return sb.toString();
        } else if (intent == Intent.CANCEL) {
            session.resetOrder();
            return "No problem, I've cancelled that. Is there anything else you need?";
        }
        // Kein CONFIRM/CANCEL -> frage weiter
        return "Please say 'yes' to confirm or 'no' to cancel your order.";
    }

    /**
     * Generiert eine Antwort auf Preisfragen.
     */
    private String generatePriceResponse(Map<String, String> entities, ChatSession session) {
        String product = entities.get("product");
        // Falls kein Produkt, aber Session hat letztes Produkt (Pronomen/Follow-up)
        if (product == null && session != null) {
            product = session.getLastEntities().get("product");
        }
        if (product != null && BotConfig.hasProduct(product)) {
            double price = BotConfig.getPrice(product);
            return String.format("A %s costs %.2f EUR.", product, price);
        }
        return "I'm not sure which product you mean. We have: " + String.join(", ", BotConfig.getProducts());
    }

    /**
     * Generiert eine Antwort auf Bestellungsanfragen.
     */
    private String generateOrderResponse(Map<String, String> entities, ChatSession session) {
        String product = entities.get("product");
        String quantity = entities.get("quantity");
        String color = entities.get("color");

        if (product != null && BotConfig.hasProduct(product)) {
            StringBuilder sb = new StringBuilder();
            sb.append("I can help with that! ");

            if (quantity != null) {
                sb.append(quantity).append(" ");
            }
            if (color != null) {
                sb.append(color).append(" ");
            }
            sb.append(product);

            double price = BotConfig.getPrice(product);
            int qty = quantity != null ? Integer.parseInt(quantity) : 1;
            sb.append(String.format(" costs %.2f EUR each. ", price));
            sb.append(String.format("Total: %.2f EUR. ", price * qty));
            sb.append("Shall I confirm your order?");

            return sb.toString();
        }

        // Kein Produkt gefunden -> Bestell-Workflow starten
        if (session != null) {
            session.setOrderProduct(null);
            session.setOrderQuantity(null);
            session.setOrderColor(null);
            session.setOrderStatus(ChatSession.OrderStatus.ASKING_PRODUCT);
        }
        return "What product would you like to order? We have: " + String.join(", ", BotConfig.getProducts());
    }

    /**
     * Generiert eine Antwort auf Hilfeanfragen.
     */
    private String generateHelpResponse() {
        return "I can help you with:\n" +
                "- **Prices**: Ask 'How much is a [product]?'\n" +
                "- **Orders**: Say 'I want to order [product]'\n" +
                "- **Shipping**: Ask about delivery times\n" +
                "- **Returns**: Check our return policy\n" +
                "- **Search**: Browse products by color or name\n\n" +
                "Our products: " + String.join(", ", BotConfig.getProducts());
    }

    /**
     * Generiert eine Antwort auf Suchanfragen.
     */
    private String generateSearchResponse(Map<String, String> entities) {
        String color = entities.get("color");
        String product = entities.get("product");

        if (product != null && BotConfig.hasProduct(product)) {
            double price = BotConfig.getPrice(product);
            String colorInfo = color != null ? " in " + color : "";
            return "I found " + product + colorInfo + " for " + String.format("%.2f", price) + " EUR. Would you like to order it?";
        }

        if (color != null) {
            return "We have several products in " + color + ". Which product are you looking for?";
        }

        return "Here are our products: " + String.join(", ", BotConfig.getProducts()) + ". Which one interests you?";
    }

    /**
     * Wählt zufällig eine Antwort aus einer Liste.
     */
    private String randomChoice(String... options) {
        return options[random.nextInt(options.length)];
    }
}
