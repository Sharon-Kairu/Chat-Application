package com.example.chatapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contacts> contactsList;
    private OnContactClickListener clickListener;

    public ContactsAdapter(List<Contacts> contactsList) {
        this.contactsList = contactsList;
    }

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contacts contact = contactsList.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imageViewAvatar;
        private TextView textViewName;
        private TextView textViewChatCount;
        private TextView textViewTime;
        private TextView textViewUnreadCount;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewChatCount = itemView.findViewById(R.id.textViewChatCount);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewUnreadCount = itemView.findViewById(R.id.textViewUnreadCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onContactClick(contactsList.get(position));
                }
            });
        }

        public void bind(Contacts contact) {
            textViewName.setText(contact.getUsername());
            textViewChatCount.setText(contact.getTotalChatCount() + " chats");

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textViewTime.setText(sdf.format(contact.getLastMessageTime()));

            int unreadCount = contact.getUnreadMessageCount();
            if (unreadCount > 0) {
                textViewUnreadCount.setVisibility(View.VISIBLE);
                textViewUnreadCount.setText(String.valueOf(unreadCount));
            } else {
                textViewUnreadCount.setVisibility(View.GONE);
            }

            // Load avatar image
            if (contact.getProfileImageUri() != null) {
                Glide.with(itemView.getContext())
                        .load(contact.getProfileImageUri())
                        .placeholder(R.drawable.default_avatar)
                        .into(imageViewAvatar);
            } else {
                imageViewAvatar.setImageResource(R.drawable.default_avatar);
            }
        }
    }

    public interface OnContactClickListener {
        void onContactClick(Contacts contact);
    }
}
