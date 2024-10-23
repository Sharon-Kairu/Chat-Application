package com.example.chatapplication;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Contacts.OnContactClickListener {

    private static final int SIGN_IN_REQUEST_CODE = 1001;
    private RecyclerView recyclerViewContacts;
    private Contacts contactsAdapter;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListenerRegistration contactsListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_home);

        initializeViews();
        setupFirebase();
        checkAuthenticationState();
    }

    private void initializeViews() {
        // Initialize RecyclerView
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new Contacts(this);
        recyclerViewContacts.setAdapter(contactsAdapter);

        // Initialize ProgressBar
        progressBar = findViewById(R.id.progressBar);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadContacts);

        // Initialize FABs
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);
        FloatingActionButton fabAddContact = findViewById(R.id.fabAddContact);

        fabNewChat.setOnClickListener(view -> startNewChat());
        fabAddContact.setOnClickListener(view -> addNewContact());
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void checkAuthenticationState() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startSignIn();
        } else {
            onSignInInitialize(currentUser.getDisplayName());
        }
    }

    private void startSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                SIGN_IN_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            handleSignInResult(resultCode);
        }
    }

    private void handleSignInResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                onSignInInitialize(user.getDisplayName());
            }
        } else {
            Toast.makeText(this, "Sign in failed. Please try again.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void onSignInInitialize(String username) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String email = currentUser.getEmail();

        ContactsAdapter userContact = new ContactsAdapter(userId, email);
        userContact.setUsername(username);

        db.collection("users").document(userId)
                .set(userContact)
                .addOnSuccessListener(aVoid -> {
                    loadContacts();
                    setupContactsListener();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this,
                                "Error saving user data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void setupContactsListener() {
        if (contactsListener != null) {
            contactsListener.remove();
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        contactsListener = db.collection("users")
                .whereNotEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(MainActivity.this,
                                "Error listening for contact updates: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        loadContacts();
                    }
                });
    }

    private void loadContacts() {
        progressBar.setVisibility(View.VISIBLE);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .whereNotEqualTo("userId", currentUserId)
                .orderBy("userId", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ContactsAdapter> newContacts = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getString("userId");
                        String email = document.getString("email");
                        String username = document.getString("username");

                        ContactsAdapter contact = new ContactsAdapter(userId, email);
                        contact.setUsername(username);

                        loadContactDetails(contact, newContacts);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,
                            "Error loading contacts: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadContactDetails(ContactsAdapter contact, List<ContactsAdapter> contactsList) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("chats")
                .whereArrayContains("participants", contact.getUserId())
                .get()
                .addOnSuccessListener(chatSnapshots -> {
                    int chatCount = 0;
                    int unreadCount = 0;
                    long lastMessageTimestamp = 0;

                    for (DocumentSnapshot chatDoc : chatSnapshots) {
                        Chat chat = chatDoc.toObject(Chat.class);
                        if (chat != null && chat.getParticipants().contains(currentUserId)) {
                            chatCount++;

                            if (chat.getLastMessageTime() != null) {
                                long messageTimestamp = chat.getLastMessageTime().getTime();
                                if (messageTimestamp > lastMessageTimestamp) {
                                    lastMessageTimestamp = messageTimestamp;
                                    contact.setLastMessageTime(chat.getLastMessageTime());
                                }
                            }

                            // Load unread messages count
                            loadUnreadCount(chatDoc.getId(), currentUserId, count -> {
                                contact.setUnreadMessageCount(
                                        contact.getUnreadMessageCount() + count);
                                contactsAdapter.notifyDataSetChanged();
                            });
                        }
                    }

                    contact.setTotalChatCount(chatCount);
                    contactsList.add(contact);

                    // Sort contacts by last message time and update adapter
                    contactsList.sort((c1, c2) -> {
                        if (c1.getLastMessageTime() == null) return 1;
                        if (c2.getLastMessageTime() == null) return -1;
                        return c2.getLastMessageTime().compareTo(c1.getLastMessageTime());
                    });

                    contactsAdapter.updateContacts(contactsList);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,
                            "Error loading chat details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUnreadCount(String chatId, String currentUserId, UnreadCountCallback callback) {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereEqualTo("read", false)
                .whereNotEqualTo("senderId", currentUserId)
                .get()
                .addOnSuccessListener(messages -> callback.onUnreadCountLoaded(messages.size()))
                .addOnFailureListener(e -> callback.onUnreadCountLoaded(0));
    }

    private void startNewChat() {
        Intent intent = new Intent(this, NewChatActivity.class);
        startActivity(intent);
    }

    private void addNewContact() {
        Intent intent = new Intent(this, NewContactActivity.class);
        startActivity(intent);
    }

    @Override
    public void onContactClick(ContactsAdapter contact) {
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("contactId", contact.getUserId());
        intent.putExtra("contactName", contact.getUsername());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (contactsListener != null) {
                        contactsListener.remove();
                    }

                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    clearUserData();
                                    startSignIn();
                                } else {
                                    Toast.makeText(MainActivity.this,
                                            "Sign out failed. Please try again.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void clearUserData() {
        contactsAdapter.updateContacts(new ArrayList<>());
        getSharedPreferences("ChatAppPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contactsListener != null) {
            contactsListener.remove();
        }
    }

    interface UnreadCountCallback {
        void onUnreadCountLoaded(int count);
    }
}