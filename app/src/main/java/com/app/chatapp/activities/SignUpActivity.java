package com.app.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.chatapp.databinding.ActivitySignUpBinding;
import com.app.chatapp.utilities.Constants;
import com.app.chatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners(){
//        binding.goSignIn.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));
//        Or
        binding.goSignIn.setOnClickListener(v -> onBackPressed());

        binding.buttonSignUp.setOnClickListener(v -> {
            if (isCredentialsValid())
                signUp();
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user. put (Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put (Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put (Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put (Constants.KEY_IMAGE, encodedImage);
        db.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        loading(false);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true) ;
                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                        preferenceManager.putString(Constants. KEY_IMAGE, encodedImage);
                        Intent intent = new Intent(getApplicationContext (), MainActivity. class) ;
                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading(false);
                        showToast(e.getMessage());
                    }
                });
    }

    private void loading(boolean isLoading){
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    
    private boolean isCredentialsValid(){
        if (encodedImage == null) {
            showToast("Enter a valid Profile Image!");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            binding.inputName.setError("Enter a valid Name!");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.inputEmail.setError("Enter a valid E-mail!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            binding.inputEmail.setError("Enter a valid E-mail!");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            binding.inputPassword.setError("Enter a valid Password!");
            return false;
        } else if (!binding.inputConfirmPassword.getText().toString().trim().equals(binding.inputPassword.getText().toString().trim())) {
            binding.inputConfirmPassword.setError("Passwords don't match!");
            return false;
        }
        return true;
    }

    private String encodeImage(Bitmap bitmap){
        int width = 150;
//        to maintain the scale
        int height = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK)
                    if (result.getData() != null) {
                        Uri imageURI = result.getData().getData();


                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageURI);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.profileImage.setImageBitmap(bitmap);
                            binding.imageText.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            });
}