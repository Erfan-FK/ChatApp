package com.app.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatapp.databinding.UserContainerBinding;
import com.app.chatapp.listeners.UserListener;
import com.app.chatapp.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserContainerBinding userContainerBinding = UserContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new viewHolder(userContainerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public UsersAdapter (List<User> users, UserListener userListener){
        this.users = users;
        this.userListener = userListener;
    }

    class viewHolder extends RecyclerView.ViewHolder {

        UserContainerBinding binding;

        public viewHolder(UserContainerBinding userContainerBinding) {
            super(userContainerBinding.getRoot());
            binding = userContainerBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.getName());
            binding.textEmail.setText(user.getEmail());
            binding.imageProfile.setImageBitmap(getUserImage(user.getImage()));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClick(user));
        }
    }
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
