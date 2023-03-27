package com.app.chatapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.chatapp.adapters.ChatAdapter;
import com.app.chatapp.databinding.ActivityChatBinding;
import com.app.chatapp.models.Message;
import com.app.chatapp.models.User;
import com.app.chatapp.network.ApiClient;
import com.app.chatapp.network.ApiService;
import com.app.chatapp.utilities.Constants;
import com.app.chatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.C;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<Message> messages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private String conversionId = null;
    private Boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
        listenMessage();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, getBitmap(receiverUser.getImage()), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
        message.put(Constants.KEY_TIME, new Date());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        if (conversionId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        } else {
            HashMap <String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.getName());
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.getImage());
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIME, new Date());
            addConversation(conversation);
        }
        if (!isOnline) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.getToken());

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MESSAGE_DATA, data);
                body.put(Constants.STRING_MESSAGE_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception e) {
                showToast(e.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String message) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMessageHeaders(), message)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            try {
                                if (response.body() != null) {
                                    JSONObject responseJson = new JSONObject(response.body());
                                    JSONArray results = responseJson.getJSONArray("results");
                                    if (responseJson.getInt("failure") == 1) {
                                        JSONObject errors = (JSONObject) results.get(0);
                                        showToast(errors.getString("error"));
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            showToast("Notification sent successfully.");
                        } else {
                            showToast("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        showToast(t.getMessage());
                    }
                });
    }

    private void listenOnlineReceiver() {
        db.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.getId())
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                   if (error != null) {
                       return;
                   }
                   if (value != null) {
                       if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                           int online = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY))
                                   .intValue();

                           isOnline = online == 1;
                       }
                       receiverUser.setToken(value.getString(Constants.KEY_FCM_TOKEN));
                        if (receiverUser.getImage() == null) {
                            receiverUser.setImage(value.getString(Constants.KEY_FCM_TOKEN));
                            chatAdapter.setReceiverProfileImage(getBitmap(receiverUser.getImage()));
                            chatAdapter.notifyItemRangeChanged(0, messages.size());
                        }
                   }
                   if (isOnline) {
                       binding.textOnline.setVisibility(View.VISIBLE);
                   } else {
                       binding.textOnline.setVisibility(View.GONE);
                   }
                });
    }

    private void listenMessage() {
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);

        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null){
            int count = messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message(documentChange.getDocument().getString(Constants.KEY_SENDER_ID),
                            documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID),
                            documentChange.getDocument().getString(Constants.KEY_MESSAGE),
                            getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIME)),
                            documentChange.getDocument().getDate(Constants.KEY_TIME));

                    messages.add(message);
                }
            }
            Collections.sort(messages, (obj1,obj2) -> obj1.getDate().compareTo(obj2.getDate()));

            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(messages.size(), messages.size());
                binding.chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

        if (conversionId == null) {
            checkForConversation();
        }
    };

    private Bitmap getBitmap(String encoded) {
        if (encoded != null) {
            byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void checkForConversation() {
        if (messages.size() != 0) {
            checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.getId());

            checkForConversionRemotely(receiverUser.getId(),
                    preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.getName());
    }

    private void setListeners() {
        binding.back.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDate(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);

        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIME, new Date());
    }

    private void checkForConversionRemotely(String senderId, String receiverId){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenOnlineReceiver();
    }
}