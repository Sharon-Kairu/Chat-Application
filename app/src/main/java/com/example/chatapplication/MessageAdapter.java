package com.example.chatapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.type.Date;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Message1> messages = new ArrayList<>();
    private final String currentUserId;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutResource = viewType == VIEW_TYPE_SENT
                ? R.layout.item_message_sent
                : R.layout.item_message_received;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutResource, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message1 message = messages.get(position);
        holder.contentText.setText(message.getContent());
        // TODO: Set timestamp and handle different message types (text, file, etc.)
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message1 message = messages.get(position);
        return message.getSenderId().equals(currentUserId)
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    public void addMessage(Message1 message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<Message1> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView contentText;

        MessageViewHolder(View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.messageContent);
        }
    }
}

class Message {
    private String senderId;
    private String content;
    private String type;
    private Date timestamp;

    public Message(String senderId, String content, String type, Date timestamp) {
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public Date getTimestamp() { return timestamp; }
}