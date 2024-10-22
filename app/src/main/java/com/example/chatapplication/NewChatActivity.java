package com.example.chatapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewChatActivity extends AppCompatActivity implements Contacts.OnContactClickListener {
    private RecyclerView recyclerViewContacts;
    private Contacts adapter;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        initializeFirebase();
        setupToolbar();
        setupRecyclerView();
        loadContacts();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("New Chat");
        }
    }

    private void setupRecyclerView() {
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Contacts(this);
        recyclerViewContacts.setAdapter(adapter);
    }

    private void loadContacts() {
        db.collection("users")
                .document(currentUserId)
                .collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ContactsAdapter> contacts = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ContactsAdapter contact = document.toObject(ContactsAdapter.class);
                        if (contact != null) {
                            contacts.add(contact);
                        }
                    }
                    adapter.updateContacts(contacts);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading contacts: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onContactClick(ContactsAdapter contact) {
        if (contact == null || contact.getUserId() == null) {
            Toast.makeText(this, "Invalid contact", Toast.LENGTH_SHORT).show();
            return;
        }
        createNewChat(contact);
    }

    private void createNewChat(ContactsAdapter contact) {
        String chatId = db.collection("chats").document().getId();
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(contact.getUserId());

        Chat newChat = new Chat();
        newChat.setChatId(chatId);
        newChat.setParticipants(participants);
        newChat.setCreatedAt(new Date());
        newChat.setLastMessageTime(new Date());

        db.collection("chats")
                .document(chatId)
                .set(newChat)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Chat created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating chat: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}