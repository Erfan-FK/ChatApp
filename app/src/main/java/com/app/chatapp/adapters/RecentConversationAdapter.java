package com.app.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatapp.databinding.RecentChatContainerBinding;
import com.app.chatapp.listeners.ConversationListener;
import com.app.chatapp.models.Message;
import com.app.chatapp.models.User;

import java.util.List;


public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder>{

    private final List<Message> messageList;
    private final ConversationListener conversationListener;


    public RecentConversationAdapter(List<Message> messageList, ConversationListener conversationListener) {
        this.messageList = messageList;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(RecentChatContainerBinding.inflate(LayoutInflater.from(parent.getContext())));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        RecentChatContainerBinding binding;
        private static final String TAG = "ConversationViewHolder";

        ConversationViewHolder(RecentChatContainerBinding recentChatContainerBinding) {
            super(recentChatContainerBinding.getRoot());
            binding = recentChatContainerBinding;
        }

        void setData (Message message) {
            Log.d(TAG, "setData: " + message.getConversionName() + " - " + message.getMessage());
            binding.imageProfile.setImageBitmap(getConversionImage(message.getConversionImage()));
            binding.textName.setText(message.getConversionName());
            binding.textMessage.setText(message.getMessage());
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.setId(message.getConversionId());
                user.setImage(message.getConversionImage());
                user.setName(message.getConversionName());
                conversationListener.onConversationClicked(user);
            });
        }
    }
    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
