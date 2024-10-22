package com.example.chatapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;

public class NewContactActivity extends AppCompatActivity {
    private EditText emailEditText;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add New Contact");

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        Button addButton = findViewById(R.id.addContactButton);

        addButton.setOnClickListener(v -> addNewContact());
    }

    private void addNewContact() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }

        // Search for user with this email
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User found
                        ContactsAdapter newContact = queryDocumentSnapshots.getDocuments()
                                .get(0)
                                .toObject(ContactsAdapter.class);

                        // Add to contacts collection
                        db.collection("users")
                                .document(currentUserId)
                                .collection("contacts")
                                .document(newContact.getUserId())
                                .set(newContact)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error adding contact", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error searching for user", Toast.LENGTH_SHORT).show());
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