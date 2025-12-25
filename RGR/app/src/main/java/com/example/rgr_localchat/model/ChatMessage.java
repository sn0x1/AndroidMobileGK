package com.example.rgr_localchat.model;

public class ChatMessage {
    private String text;
    private boolean isSentByMe;
    private boolean isSystem; // Це системне повідомлення?
    private String senderName;
    private long timestamp;

    // Конструктор для звичайних повідомлень
    public ChatMessage(String text, boolean isSentByMe, String senderName) {
        this.text = text;
        this.isSentByMe = isSentByMe;
        this.senderName = senderName;
        this.isSystem = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Конструктор для системних повідомлень
    public ChatMessage(String text) {
        this.text = text;
        this.isSystem = true;
        this.isSentByMe = false;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() { return text; }
    public boolean isSentByMe() { return isSentByMe; }
    public boolean isSystem() { return isSystem; }
    public String getSenderName() { return senderName; }
    public long getTimestamp() { return timestamp; }
}