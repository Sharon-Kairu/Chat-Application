package com.example.chatapplication;

import android.net.Uri;
import java.util.Date;

public class ContactsAdapter {
    private String userId;
    private String email;
    private String username;
    private int totalChatCount;
    private Date lastMessageTime;
    private int unreadMessageCount;
    private Uri profileImageUri;

    // Constructor
    public ContactsAdapter(String userId, String email) {
        this.userId = userId;
        this.email = email;
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

    public int getTotalChatCount() {
        return totalChatCount;
    }

    public void setTotalChatCount(int totalChatCount) {
        this.totalChatCount = totalChatCount;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(Uri profileImageUri) {
        this.profileImageUri = profileImageUri;
    }
}