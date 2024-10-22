package com.example.chatapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;

public class Contacts extends RecyclerView.Adapter<Contacts.ContactViewHolder> {
    private List<ContactsAdapter> contactsList;
    private final OnContactClickListener clickListener;

    public Contacts(OnContactClickListener clickListener) {
        this.contactsList = new ArrayList<>();
        this.clickListener = clickListener;
    }

    public void updateContacts(List<ContactsAdapter> newContacts) {
        this.contactsList = new ArrayList<>(newContacts);
        notifyDataSetChanged();
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
        ContactsAdapter contact = contactsList.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imageViewAvatar;
        private final TextView textViewName;
        private final TextView textViewChatCount;
        private final TextView textViewTime;
        private final TextView textViewUnreadCount;

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

        public void bind(ContactsAdapter contact) {
            if (contact == null) return;

            textViewName.setText(contact.getUsername());
            textViewChatCount.setText(String.format(Locale.getDefault(), "%d chats", contact.getTotalChatCount()));

            if (contact.getLastMessageTime() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                textViewTime.setText(sdf.format(contact.getLastMessageTime()));
            } else {
                textViewTime.setText("");
            }

            int unreadCount = contact.getUnreadMessageCount();
            textViewUnreadCount.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
            if (unreadCount > 0) {
                textViewUnreadCount.setText(String.valueOf(unreadCount));
            }

            // Load avatar image with error handling
            if (contact.getProfileImageUri() != null && !contact.getProfileImageUri().toString().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(contact.getProfileImageUri())
                        .error(R.drawable.default_avatar)
                        .placeholder(R.drawable.default_avatar)
                        .into(imageViewAvatar);
            } else {
                imageViewAvatar.setImageResource(R.drawable.default_avatar);
            }
        }
    }

    public interface OnContactClickListener {
        void onContactClick(ContactsAdapter contact);
    }
}