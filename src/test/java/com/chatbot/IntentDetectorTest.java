package com.chatbot.service;

import com.chatbot.model.Intent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für die Intent-Erkennung.
 * Jede Regel wird einzeln getestet, um sicherzustellen, dass sie korrekt funktioniert.
 */
class IntentDetectorTest {

    private final IntentDetector detector = new IntentDetector();

    @Test
    void greeting_hello() {
        assertEquals(Intent.GREETING, detector.detect("Hello"));
    }

    @Test
    void greeting_hi() {
        assertEquals(Intent.GREETING, detector.detect("Hi there!"));
    }

    @Test
    void greeting_goodMorning() {
        assertEquals(Intent.GREETING, detector.detect("Good morning"));
    }

    @Test
    void greeting_hey() {
        assertEquals(Intent.GREETING, detector.detect("Hey!"));
    }

    @Test
    void askPrice_howMuch() {
        assertEquals(Intent.ASK_PRICE, detector.detect("How much is a t-shirt?"));
    }

    @Test
    void askPrice_cost() {
        assertEquals(Intent.ASK_PRICE, detector.detect("What does it cost?"));
    }

    @Test
    void askPrice_price() {
        assertEquals(Intent.ASK_PRICE, detector.detect("What's the price of jeans?"));
    }

    @Test
    void order_order() {
        assertEquals(Intent.ORDER, detector.detect("I want to order 2 pants"));
    }

    @Test
    void order_buy() {
        assertEquals(Intent.ORDER, detector.detect("Buy a jacket"));
    }

    @Test
    void order_purchase() {
        assertEquals(Intent.ORDER, detector.detect("I would like to purchase sneakers"));
    }

    @Test
    void confirm_yes() {
        assertEquals(Intent.CONFIRM, detector.detect("Yes"));
    }

    @Test
    void confirm_okay() {
        assertEquals(Intent.CONFIRM, detector.detect("Okay, please do it"));
    }

    @Test
    void confirm_sure() {
        assertEquals(Intent.CONFIRM, detector.detect("Sure!"));
    }

    @Test
    void cancel_no() {
        assertEquals(Intent.CANCEL, detector.detect("No"));
    }

    @Test
    void cancel_cancel() {
        assertEquals(Intent.CANCEL, detector.detect("Cancel my order"));
    }

    @Test
    void cancel_neverMind() {
        assertEquals(Intent.CANCEL, detector.detect("Never mind"));
    }

    @Test
    void goodbye_bye() {
        assertEquals(Intent.GOODBYE, detector.detect("Bye!"));
    }

    @Test
    void goodbye_goodbye() {
        assertEquals(Intent.GOODBYE, detector.detect("Goodbye"));
    }

    @Test
    void goodbye_seeYou() {
        assertEquals(Intent.GOODBYE, detector.detect("See you later"));
    }

    @Test
    void help_help() {
        assertEquals(Intent.HELP, detector.detect("Help me"));
    }

    @Test
    void help_whatCanYouDo() {
        assertEquals(Intent.HELP, detector.detect("What can you do?"));
    }

    @Test
    void search_showMe() {
        assertEquals(Intent.SEARCH, detector.detect("Show me blue sneakers"));
    }

    @Test
    void search_find() {
        assertEquals(Intent.SEARCH, detector.detect("Find a dress"));
    }

    @Test
    void return_return() {
        assertEquals(Intent.RETURN, detector.detect("I want to return this"));
    }

    @Test
    void return_refund() {
        assertEquals(Intent.RETURN, detector.detect("Can I get a refund?"));
    }

    @Test
    void shipping_shipping() {
        assertEquals(Intent.SHIPPING, detector.detect("How long is shipping?"));
    }

    @Test
    void shipping_delivery() {
        assertEquals(Intent.SHIPPING, detector.detect("When will my delivery arrive?"));
    }

    @Test
    void unknown_randomText() {
        assertEquals(Intent.UNKNOWN, detector.detect("asdfghjkl"));
    }

    @Test
    void unknown_empty() {
        assertEquals(Intent.UNKNOWN, detector.detect(""));
    }

    @Test
    void unknown_null() {
        assertEquals(Intent.UNKNOWN, detector.detect(null));
    }

    @Test
    void caseInsensitive() {
        assertEquals(Intent.GREETING, detector.detect("HELLO"));
        assertEquals(Intent.GREETING, detector.detect("HeLlO"));
    }

    @Test
    void askPrice_inventory() {
        assertEquals(Intent.ASK_PRICE, detector.detect("what are the prices of your inventory?"));
    }

    @Test
    void askPrice_sell() {
        assertEquals(Intent.ASK_PRICE, detector.detect("What do you sell"));
    }

    @Test
    void askPrice_offer() {
        assertEquals(Intent.ASK_PRICE, detector.detect("What do you offer?"));
    }

    @Test
    void help_functions() {
        assertEquals(Intent.HELP, detector.detect("What are your functions?"));
    }

    @Test
    void help_capabilities() {
        assertEquals(Intent.HELP, detector.detect("What are your capabilities?"));
    }

    @Test
    void help_features() {
        assertEquals(Intent.HELP, detector.detect("Tell me about your features"));
    }

    @Test
    void followUp_whatAbout() {
        assertEquals(Intent.ASK_PRICE, detector.detect("what about shirts?"));
    }

    @Test
    void followUp_tellMeAbout() {
        assertEquals(Intent.ASK_PRICE, detector.detect("tell me about pants"));
    }

    @Test
    void askPrice_products() {
        assertEquals(Intent.ASK_PRICE, detector.detect("what products do you have?"));
    }

    @Test
    void askPrice_whatElse() {
        assertEquals(Intent.ASK_PRICE, detector.detect("what else do you have?"));
    }

    @Test
    void askPrice_whatIsEuro() {
        assertEquals(Intent.ASK_PRICE, detector.detect("what is 20 euro?"));
    }

    @Test
    void order_whatCanIOrder() {
        assertEquals(Intent.ORDER, detector.detect("what can i order?"));
    }
}
