package com.example.chatapplication;

import java.util.Date;

public class Message1 {
    private String senderId;
    private String content;
    private String type;
    private Date timestamp;

    public Message1(String senderId, String content, String type, Date timestamp) {
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public Date getTimestamp() { return timestamp; }
}