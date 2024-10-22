package com.example.chatapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 1001;
    private RecyclerView recyclerViewContacts;
    private Contacts contactsAdapter;
    private List<ContactsAdapter> contactsList;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_home);

        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactsList = new ArrayList<>();
        contactsAdapter = new Contacts((Contacts.OnContactClickListener) contactsList);
        recyclerViewContacts.setAdapter(contactsAdapter);

        // Initialize FABs
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);
        FloatingActionButton fabAddContact = findViewById(R.id.fabAddContact);

        fabNewChat.setOnClickListener(view -> startNewChat());
        fabAddContact.setOnClickListener(view -> addNewContact());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            // User is already signed in
            onSignInInitialize(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                                "Successfully signed in. Welcome!",
                                Toast.LENGTH_LONG)
                        .show();
                onSignInInitialize(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            } else {
                Toast.makeText(this,
                                "We couldn't sign you in. Please try again later.",
                                Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }

    private void onSignInInitialize(String username) {
        // Welcome message
        Toast.makeText(this, "Welcome " + username, Toast.LENGTH_LONG).show();

        // Create or update user in Firestore
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        ContactsAdapter userContact = new ContactsAdapter(userId, email);
        userContact.setUsername(username);

        db.collection("users").document(userId).set(userContact)
                .addOnSuccessListener(aVoid -> loadContacts())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show());
    }

    private void loadContacts() {
        // TODO: Implement contact loading from Firestore
        // This is a placeholder. You should replace this with actual Firestore queries.
        contactsList.clear();
        contactsList.add(new ContactsAdapter("1", "john@example.com"));
        contactsList.add(new ContactsAdapter("2", "jane@example.com"));
        contactsAdapter.notifyDataSetChanged();
    }

    private void startNewChat() {
        // TODO: Implement new chat functionality
        Toast.makeText(this, "Starting new chat", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NewChatActivity.class);
        startActivity(intent);
    }

    private void addNewContact() {
        // TODO: Implement add new contact functionality
        Toast.makeText(this, "Adding new contact", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NewContactActivity.class);
        startActivity(intent);
    }

}