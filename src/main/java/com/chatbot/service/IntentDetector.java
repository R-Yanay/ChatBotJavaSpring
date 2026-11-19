package com.chatbot.service;

import com.chatbot.model.ChatSession;
import com.chatbot.model.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Erkennt die Absicht (Intent) einer Nachricht durch Regex-Matching.
 * Jede Regel wird der Reihe nach geprueft — die erste Uebereinstimmung gewinnt.
 * Unterstuetzt Kontext-bewusste Erkennung: Pronomen ("it", "that") und Follow-up-Fragen ("and pants?").
 */
@Service
public class IntentDetector {

    private static final Logger log = LoggerFactory.getLogger(IntentDetector.class);

    /**
     * Regex-Regeln fuer jeden Intent.
     * Die Reihenfolge ist wichtig: Spezifischere Regeln kommen zuerst.
     * CANCEL und RETURN muessen vor ORDER, da "cancel my order" und "I want to return"
     * auch ORDER-Keywords enthalten.
     */
    private static final IntentRule[] RULES = {
            // GREETING — muss vor HELP kommen, da "hi" in "help" vorkommen koennte
            new IntentRule(Intent.GREETING,
                    "(?i)\\b(hi|hello|hey|good morning|good afternoon|good evening|greetings|howdy)\\b"),

            // ASK_PRICE — spezifisch vor ORDER
            new IntentRule(Intent.ASK_PRICE,
                    "(?i)\\b(how much|cost|price|pricetag|expensive|cheap)\\b"),

            // CANCEL — muss vor ORDER, da "cancel my order" das Wort "order" enthaelt
            new IntentRule(Intent.CANCEL,
                    "(?i)\\b(no|nah|nope|cancel|stop|never mind|forget it|not now)\\b"),

            // RETURN — muss vor ORDER, da "I want to return" das Wort "want" enthaelt
            new IntentRule(Intent.RETURN,
                    "(?i)\\b(return|refund|exchange|send back|give back)\\b"),

            // ORDER — Kaufabsicht (nach CANCEL und RETURN)
            new IntentRule(Intent.ORDER,
                    "(?i)\\b(order|buy|purchase|get|want|would like|add to cart|add to basket)\\b"),

            // CONFIRM — Bestaetigung
            new IntentRule(Intent.CONFIRM,
                    "(?i)^(yes|yeah|yep|sure|ok|okay|please|go ahead|confirm|absolutely)\\b"),

            // GOODBYE — Verabschiedung
            new IntentRule(Intent.GOODBYE,
                    "(?i)\\b(bye|goodbye|see you|see you later|cya|farewell|take care|later)\\b"),

            // HELP — Hilfe
            new IntentRule(Intent.HELP,
                    "(?i)\\b(help|what can you do|how does this work|support|info|information)\\b"),

            // SEARCH — Produktsuche
            new IntentRule(Intent.SEARCH,
                    "(?i)\\b(show me|find|search|look for|browse|filter)\\b"),

            // SHIPPING — Versand
            new IntentRule(Intent.SHIPPING,
                    "(?i)\\b(shipping|delivery|deliver|ship|dispatch|send)\\b")
    };

    /**
     * Erkennt den Intent einer Nachricht.
     *
     * @param message Die Nutzereingabe
     * @return Der erkannte Intent, oder UNKNOWN wenn keine Regel passt
     */
    public Intent detect(String message) {
        return detect(message, null);
    }

    /**
     * Erkennt den Intent einer Nachricht mit Kontext aus der ChatSession.
     * Erkennt Pronomen ("it", "that", "them") und Follow-up-Fragen ("and pants?").
     *
     * @param message Die Nutzereingabe
     * @param session Die ChatSession mit Kontext (oder null)
     * @return Der erkannte Intent, oder UNKNOWN wenn keine Regel passt
     */
    public Intent detect(String message, ChatSession session) {
        if (message == null || message.trim().isEmpty()) {
            return Intent.UNKNOWN;
        }

        // 1. Pronomen erkennen: "it", "that", "them" -> letzten Intent verwenden
        if (isPronounOnly(message)) {
            if (session != null && session.getLastIntent() != null) {
                log.debug("Pronomen '{}' erkannt, verwende letzten Intent: {}", message, session.getLastIntent());
                return session.getLastIntent();
            }
        }

        // 2. Follow-up-Fragen erkennen: "and pants?", "how much?" -> letzten Intent verwenden
        if (isFollowUp(message)) {
            if (session != null && session.getLastIntent() != null) {
                log.debug("Follow-up '{}' erkannt, verwende letzten Intent: {}", message, session.getLastIntent());
                return session.getLastIntent();
            }
        }

        // 3. Standard Regex-Matching
        for (IntentRule rule : RULES) {
            Pattern pattern = Pattern.compile(rule.pattern);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                log.debug("Intent erkannt: {} fuer '{}'", rule.intent, message);
                return rule.intent;
            }
        }

        log.debug("Kein Intent erkannt fuer '{}'", message);
        return Intent.UNKNOWN;
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
     * Prueft, ob die Nachricht eine Follow-up-Frage ist (mit "and" am Anfang).
     */
    private boolean isFollowUp(String message) {
        String lower = message.toLowerCase().trim();
        return lower.startsWith("and ") || lower.startsWith("and,");
    }

    /**
     * Interne Klasse fuer Intent-Regeln.
     */
    private static class IntentRule {
        final Intent intent;
        final String pattern;

        IntentRule(Intent intent, String pattern) {
            this.intent = intent;
            this.pattern = pattern;
        }
    }
}
