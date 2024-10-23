package com.example.chatapplication;

import android.net.Uri;
import com.google.firebase.firestore.Exclude;
import java.util.Date;

public class ContactsAdapter {
    private String userId;
    private String email;
    private String username;
    private int totalChatCount;
    private Date lastMessageTime;
    private int unreadMessageCount;
    private String profileImageUri;  // Changed from Uri to String

    // Required no-argument constructor for Firebase
    public ContactsAdapter() {
        // Empty constructor required for Firebase
    }

    // Existing constructor
    public ContactsAdapter(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {  // Added setter for userId
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {  // Added setter for email
        this.email = email;
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

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    // Helper methods for Uri conversion
    @Exclude
    public Uri getProfileImageUriObject() {
        return profileImageUri != null ? Uri.parse(profileImageUri) : null;
    }

    @Exclude
    public void setProfileImageUriObject(Uri uri) {
        this.profileImageUri = uri != null ? uri.toString() : null;
    }


}