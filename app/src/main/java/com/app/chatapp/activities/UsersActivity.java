package com.app.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.app.chatapp.adapters.UsersAdapter;
import com.app.chatapp.databinding.ActivityUsersBinding;
import com.app.chatapp.listeners.UserListener;
import com.app.chatapp.models.User;
import com.app.chatapp.utilities.Constants;
import com.app.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.back.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId()))
                                continue;
                            User user = new User(queryDocumentSnapshot.getString(Constants.KEY_NAME),
                                    queryDocumentSnapshot.getString(Constants.KEY_EMAIL),
                                    queryDocumentSnapshot.getString(Constants.KEY_IMAGE),
                                    queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN),
                                    queryDocumentSnapshot.getId());

                            users.add(user);
                        }

                        if (users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.userRecyclerView.setAdapter(usersAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showError();
                        }
                    } else {
                        showError();
                    }
                });

    }
    private void showError(){
        binding.errorMessage.setText(String.format("%s", "No Users Available."));
        binding.errorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean isLoading){
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}