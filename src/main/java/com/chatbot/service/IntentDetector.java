package com.chatbot.service;

import com.chatbot.model.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Erkennt die Absicht (Intent) einer Nachricht durch Regex-Matching.
 * Jede Regel wird der Reihe nach geprüft — die erste Übereinstimmung gewinnt.
 */
@Service
public class IntentDetector {

    private static final Logger log = LoggerFactory.getLogger(IntentDetector.class);

    /**
     * Regex-Regeln für jeden Intent.
     * Die Reihenfolge ist wichtig: Spezifischere Regeln kommen zuerst.
     * CANCEL und RETURN müssen vor ORDER, da "cancel my order" und "I want to return"
     * auch ORDER-Keywords enthalten.
     */
    private static final IntentRule[] RULES = {
            // GREETING — muss vor HELP kommen, da "hi" in "help" vorkommen könnte
            new IntentRule(Intent.GREETING,
                    "(?i)\\b(hi|hello|hey|good morning|good afternoon|good evening|greetings|howdy)\\b"),

            // ASK_PRICE — spezifisch vor ORDER
            new IntentRule(Intent.ASK_PRICE,
                    "(?i)\\b(how much|cost|price|pricetag|expensive|cheap)\\b"),

            // CANCEL — muss vor ORDER, da "cancel my order" das Wort "order" enthält
            new IntentRule(Intent.CANCEL,
                    "(?i)\\b(no|nah|nope|cancel|stop|never mind|forget it|not now)\\b"),

            // RETURN — muss vor ORDER, da "I want to return" das Wort "want" enthält
            new IntentRule(Intent.RETURN,
                    "(?i)\\b(return|refund|exchange|send back|give back)\\b"),

            // ORDER — Kaufabsicht (nach CANCEL und RETURN)
            new IntentRule(Intent.ORDER,
                    "(?i)\\b(order|buy|purchase|get|want|would like|add to cart|add to basket)\\b"),

            // CONFIRM — Bestätigung
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
        if (message == null || message.trim().isEmpty()) {
            return Intent.UNKNOWN;
        }

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
