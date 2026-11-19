package com.chatbot.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Repraesentiert die Session eines Benutzers — das "Gedaechtnis" des ChatBots.
 * Speichert letzte Nachricht, Intent, Entities und Bestellstatus.
 */
public class ChatSession {

    /**
     * Status des Bestell-Workflows.
     */
    public enum OrderStatus {
        IDLE,                    // Kein aktiver Bestell-Workflow
        ASKING_PRODUCT,          // Bot fragt nach Produkt
        ASKING_CONFIRMATION,     // Bot fragt nach Bestaetigung
        CONFIRMED                // Bestellung bestaetigt
    }

    private final String userId;
    private String lastMessage;
    private Intent lastIntent;
    private Map<String, String> lastEntities;
    private OrderStatus orderStatus;
    private String orderProduct;
    private Integer orderQuantity;
    private String orderColor;
    private final long createdAt;
    private long lastActive;

    public ChatSession(String userId) {
        this.userId = userId;
        this.lastEntities = new HashMap<>();
        this.orderStatus = OrderStatus.IDLE;
        this.createdAt = System.currentTimeMillis();
        this.lastActive = this.createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Intent getLastIntent() {
        return lastIntent;
    }

    public void setLastIntent(Intent lastIntent) {
        this.lastIntent = lastIntent;
    }

    public Map<String, String> getLastEntities() {
        return lastEntities;
    }

    public void setLastEntities(Map<String, String> lastEntities) {
        this.lastEntities = lastEntities != null ? new HashMap<>(lastEntities) : new HashMap<>();
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderProduct() {
        return orderProduct;
    }

    public void setOrderProduct(String orderProduct) {
        this.orderProduct = orderProduct;
    }

    public Integer getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(Integer orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public String getOrderColor() {
        return orderColor;
    }

    public void setOrderColor(String orderColor) {
        this.orderColor = orderColor;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void touch() {
        this.lastActive = System.currentTimeMillis();
    }

    /**
     * Aktualisiert die Session mit den Ergebnissen der aktuellen Nachricht.
     */
    public void update(String message, Intent intent, Map<String, String> entities) {
        this.lastMessage = message;
        this.lastIntent = intent;
        this.lastEntities = entities != null ? new HashMap<>(entities) : new HashMap<>();
        this.lastActive = System.currentTimeMillis();
    }

    /**
     * Setzt den Bestell-Workflow zurueck auf IDLE.
     */
    public void resetOrder() {
        this.orderStatus = OrderStatus.IDLE;
        this.orderProduct = null;
        this.orderQuantity = null;
        this.orderColor = null;
    }

    /**
     * Gibt zurueck, ob die Session seit lastActive abgelaufen ist.
     *
     * @param timeoutMillis Timeout in Millisekunden (Standard: 30 Minuten)
     * @return true, wenn die Session abgelaufen ist
     */
    public boolean isExpired(long timeoutMillis) {
        return (System.currentTimeMillis() - lastActive) > timeoutMillis;
    }
}
