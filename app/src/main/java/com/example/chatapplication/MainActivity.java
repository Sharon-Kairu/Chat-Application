package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    // UI Components
    private RecyclerView recyclerViewContacts;
    private Contacts contactsAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabNewChat;
    private FloatingActionButton fabAddContact;

    // Firebase Components
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration contactsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFirebase();
        initializeViews();
        checkAuthenticationState();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        // Setup RecyclerView
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new Contacts(this);
        recyclerViewContacts.setAdapter(contactsAdapter);

        // Setup Progress Bar
        progressBar = findViewById(R.id.progressBar);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshContacts);

        // Setup FABs
        fabNewChat = findViewById(R.id.fabNewChat);
        fabAddContact = findViewById(R.id.fabAddContact);

        fabNewChat.setOnClickListener(v -> startNewChat());
        fabAddContact.setOnClickListener(v -> addNewContact());
    }

    private void setupActionBar(int chatCount) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            if (chatCount > 0) {
                getSupportActionBar().setTitle("Chats (" + chatCount + ")");
            } else {
                getSupportActionBar().setTitle("Chats"); // No count if 0 chats
            }
        }
    }

    private void checkAuthenticationState() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startSignIn();
        } else {
            onSignInInitialize(currentUser);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            handleSignInResult(resultCode);
        }
    }

    private void handleSignInResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                onSignInInitialize(user);
                Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void onSignInInitialize(FirebaseUser user) {
        setupUserProfile(user);
        setupContactsListener();
        loadContacts();
    }

    private void setupUserProfile(FirebaseUser user) {
        ContactsAdapter userProfile = new ContactsAdapter(user.getUid(), user.getEmail());
        userProfile.setUsername(user.getDisplayName());

        db.collection("users").document(user.getUid())
                .set(userProfile)
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this,
                        "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupContactsListener() {
        if (contactsListener != null) {
            contactsListener.remove();
        }

        String currentUserId = auth.getCurrentUser().getUid();
        contactsListener = db.collection("users")
                .whereNotEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        loadContacts();
                    }
                });
    }

    private void refreshContacts() {
        loadContacts();
    }

    private void loadContacts() {
        progressBar.setVisibility(View.VISIBLE);
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("users")
                .whereNotEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ContactsAdapter> contacts = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ContactsAdapter contact = document.toObject(ContactsAdapter.class);
                        if (contact != null) {
                            contacts.add(contact);
                        }
                    }
                    contactsAdapter.updateContacts(contacts);
                    setupActionBar(contacts.size()); // Update the toolbar
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading contacts: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                });
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
        Intent intent = new Intent(this, ChatActivity.class);
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
        if (contactsListener != null) {
            contactsListener.remove();
        }

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contactsAdapter.updateContacts(new ArrayList<>());
                        startSignIn();
                    } else {
                        Toast.makeText(this, "Sign out failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contactsListener != null) {
            contactsListener.remove();
        }
    }
}
