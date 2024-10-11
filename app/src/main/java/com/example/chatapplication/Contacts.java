package com.example.chatapplication;

import android.net.Uri;
import java.util.Date;

public class Contacts {
    private String userId;
    private String email;
    private String username;
    private Uri profileImageUri;
    private int unreadMessageCount;
    private Date lastMessageTime;
    private int totalChatCount;

    // Constructor
    public Contacts(String userId, String email) {
        this.userId = userId;
        this.email = email;
        this.unreadMessageCount = 0;
        this.totalChatCount = 0;
        this.lastMessageTime = new Date(); // Initialize with current time
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(Uri profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getTotalChatCount() {
        return totalChatCount;
    }

    public void setTotalChatCount(int totalChatCount) {
        this.totalChatCount = totalChatCount;
    }

    // Method to increment unread message count
    public void incrementUnreadMessageCount() {
        this.unreadMessageCount++;
    }

    // Method to reset unread message count
    public void resetUnreadMessageCount() {
        this.unreadMessageCount = 0;
    }

    // Method to increment total chat count
    public void incrementTotalChatCount() {
        this.totalChatCount++;
    }

    // Method to update last message time
    public void updateLastMessageTime() {
        this.lastMessageTime = new Date();
    }

    // Method to check if the profile is complete
    public boolean isProfileComplete() {
        return username != null && !username.isEmpty() && profileImageUri != null;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", unreadMessageCount=" + unreadMessageCount +
                ", lastMessageTime=" + lastMessageTime +
                ", totalChatCount=" + totalChatCount +
                '}';
    }
}