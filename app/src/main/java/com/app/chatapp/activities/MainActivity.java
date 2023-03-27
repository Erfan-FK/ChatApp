package com.app.chatapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.chatapp.R;
import com.app.chatapp.adapters.RecentConversationAdapter;
import com.app.chatapp.databinding.ActivityMainBinding;
import com.app.chatapp.listeners.ConversationListener;
import com.app.chatapp.models.Message;
import com.app.chatapp.models.User;
import com.app.chatapp.utilities.Constants;
import com.app.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private AlertDialog dialog;
    private List<Message> conversations;
    private RecentConversationAdapter recentConversationAdapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserInformation();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init(){
        conversations = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(recentConversationAdapter);
        db = FirebaseFirestore.getInstance();
    }
    private void setListeners(){
        binding.signOut.setOnClickListener(v -> signOut());
        binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    private void loadUserInformation(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.profileImage.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations() {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    Message message = new Message();
                    message.setSenderId(senderId);
                    message.setReceiverId(receiverId);
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        message.setConversionImage(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE));
                        message.setConversionName(documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME));
                        message.setConversionId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    } else {
                        message.setConversionImage(documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE));
                        message.setConversionName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                        message.setConversionId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    }
                    message.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    message.setDate(documentChange.getDocument().getDate(Constants.KEY_TIME));
                    conversations.add(message);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)) {
                            conversations.get(i).setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                            conversations.get(i).setDate(documentChange.getDocument().getDate(Constants.KEY_TIME));
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.getDate().compareTo(obj1.getDate()));
            recentConversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token!"));
    }

    private void signOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        View view = getLayoutInflater().inflate(R.layout.signout_dialog, null);
        view.setBackgroundResource(R.drawable.dialog_background);

        Button yes = view.findViewById(R.id.yes);
        Button no = view.findViewById(R.id.no);

        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Signing Out...");
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));

                HashMap<String, Object> updates = new HashMap<>();
                updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());

                documentReference.update(updates)
                        .addOnSuccessListener(unused -> {
                            preferenceManager.clear();
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                            dialog.dismiss();
                            finish();
                        })
                        .addOnFailureListener(e -> showToast("Unable to Sign Out!"));
            }
        });

        no.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}