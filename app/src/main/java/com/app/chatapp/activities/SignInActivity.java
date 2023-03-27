package com.app.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.chatapp.databinding.ActivitySignInBinding;
import com.app.chatapp.utilities.Constants;
import com.app.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        setListeners();
    }

    private void setListeners(){
        binding.goSignUp.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));

        binding.buttonSignIn.setOnClickListener(v -> {
            if (isCredentialsValid())
                signIn();
        });
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString().trim())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext (), MainActivity. class) ;
                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to Sign In!");
                    }
                });
    }

    private void loading(boolean isLoading){
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isCredentialsValid(){
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.inputEmail.setError("Enter a valid E-mail!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            binding.inputEmail.setError("Enter a valid E-mail!");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            binding.inputPassword.setError("Enter a valid Password!");
            return false;
        }
        return true;
    }


}