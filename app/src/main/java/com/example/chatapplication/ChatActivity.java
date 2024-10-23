package com.example.chatapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String contactId;
    private String contactName;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String currentUserId;
    private MessageAdapter messageAdapter;

    // UI Components
    private RecyclerView messageRecyclerView;
    private EditText messageEditText;
    private ImageButton attachmentButton;
    private ImageButton audioRecordButton;
    private ImageButton videoRecordButton;
    private ImageButton sendButton;
    private FrameLayout recordingOverlay;
    private Chronometer recordingTimer;

    private boolean isRecording = false;
    private float initialTouchY;
    private String chatId;

    // Permissions and Activity Result Launchers
    private final ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    Toast.makeText(this, "Permissions required for this feature", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> attachmentPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadAttachment(fileUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get contact details from intent
        contactId = getIntent().getStringExtra("contactId");
        contactName = getIntent().getStringExtra("contactName");

        if (contactId == null || contactName == null) {
            Toast.makeText(this, "Error: Contact information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate or get chat ID
        chatId = generateChatId(currentUserId, contactId);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        initializeChat();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar); // Assuming the toolbar is defined in your XML layout with id 'toolbar'
        setSupportActionBar(toolbar);  // Set the toolbar as the app bar for the activity

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Ensure the back button is shown
            getSupportActionBar().setTitle(contactName);  // Set the toolbar title to the contact's name
        }
    }


    private String generateChatId(String currentUserId, String contactId) {
        return currentUserId.compareTo(contactId) > 0 ? currentUserId + "_" + contactId : contactId + "_" + currentUserId;
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setAdapter(messageAdapter);
    }

    private void initializeViews() {
        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        attachmentButton = findViewById(R.id.attachmentButton);
        audioRecordButton = findViewById(R.id.audioRecordButton);
        videoRecordButton = findViewById(R.id.videoRecordButton);
        sendButton = findViewById(R.id.sendButton);
        recordingOverlay = findViewById(R.id.recordingOverlay);
        recordingTimer = findViewById(R.id.recordingTimer);
    }

    private void initializeChat() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        List<Message1> messages = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            String senderId = doc.getString("senderId");
                            String content = doc.getString("content");
                            String type = doc.getString("type");
                            Date timestamp = doc.getDate("timestamp");

                            messages.add(new Message1(senderId, content, type, timestamp));
                        }
                        messageAdapter.setMessages(messages);
                        messageRecyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
                messageEditText.setText("");
            }
        });

        attachmentButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            attachmentPicker.launch(intent);
        });

        audioRecordButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAudioRecording();
                    initialTouchY = event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (isRecording) {
                        float deltaY = initialTouchY - event.getY();
                        if (deltaY > 200) {
                            stopAudioRecording(true);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isRecording) {
                        stopAudioRecording(false);
                    }
                    return true;
            }
            return false;
        });

        videoRecordButton.setOnClickListener(v -> {
            if (checkVideoPermissions()) {
                startVideoRecording();
            }
        });
    }

    private boolean checkVideoPermissions() {
        // Check for camera and audio recording permissions
        // Return true if granted, otherwise false
        // Example dummy check (you'd implement real permission checks here)
        return true;
    }

    private void startVideoRecording() {
        // Start recording a video (implementation depends on how you handle video recording)
        Toast.makeText(this, "Video recording started", Toast.LENGTH_SHORT).show();
    }

    private void startAudioRecording() {
        isRecording = true;
        recordingOverlay.setVisibility(FrameLayout.VISIBLE);
        recordingTimer.start();
        Toast.makeText(this, "Audio recording started", Toast.LENGTH_SHORT).show();
    }

    private void stopAudioRecording(boolean cancelled) {
        isRecording = false;
        recordingOverlay.setVisibility(FrameLayout.GONE);
        recordingTimer.stop();
        if (cancelled) {
            Toast.makeText(this, "Recording cancelled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show();
            // Save the recording here
        }
    }

    private void uploadAttachment(Uri fileUri) {
        // Upload the selected file to Firebase Storage
        // Use fileUri to get the file and upload it
        Toast.makeText(this, "Attachment uploaded", Toast.LENGTH_SHORT).show();
    }

    private void sendMessage(String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUserId);
        message.put("content", content);
        message.put("timestamp", new Date());
        message.put("type", "text");

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageEditText.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
