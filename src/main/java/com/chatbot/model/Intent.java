package com.chatbot.model;

/**
 * Liste aller erkannten Absichten (Intents) des ChatBots.
 * Jeder Intent entspricht einer Nutzerabsicht, z.B. Gruess, Preisfrage, Bestellung.
 */
public enum Intent {
    GREETING,       // "Hello", "Hi", "Good morning"
    ASK_PRICE,      // "How much is a T-shirt?", "What's the price?"
    ORDER,          // "I want to order 2 pants", "Buy a jacket"
    CONFIRM,        // "Yes", "Please", "OK"
    CANCEL,         // "No", "Cancel", "Stop"
    GOODBYE,        // "Bye", "Goodbye", "See you"
    HELP,           // "Help", "What can you do?"
    SEARCH,         // "Show me blue sneakers", "Find a dress"
    RETURN,         // "I want to return", "Refund"
    SHIPPING,       // "How long is shipping?", "Delivery time"
    UNKNOWN         // Keine Regel hat gepasst
}
