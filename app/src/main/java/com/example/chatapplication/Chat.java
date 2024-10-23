package com.example.chatapplication;

import java.util.Date;
import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participants;
    private Date createdAt;
    private Date lastMessageTime;
    private String lastMessage;
    private String userId;
    private boolean hasUnsentMessages;

    // Default constructor for Firestore
    public Chat() {}

    public Chat(String userId, String lastMessage, boolean hasUnsentMessages) {
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.hasUnsentMessages = hasUnsentMessages;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getters and setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isHasUnsentMessages() {
        return hasUnsentMessages;
    }

    public void setHasUnsentMessages(boolean hasUnsentMessages) {
        this.hasUnsentMessages = hasUnsentMessages;
    }
}