package com.app.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatapp.databinding.ReceivedMessageContainerBinding;
import com.app.chatapp.databinding.SentMessageContainerBinding;
import com.app.chatapp.models.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    private static final int SENT = 1;
    private static final int RECEIVED = 2;

    public void  setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<Message> messages, Bitmap receiverProfileImage, String senderId) {
        this.messages = messages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENT) {
            return new SentMessageViewHolder(SentMessageContainerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ReceivedMessageViewHolder(ReceivedMessageContainerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == SENT) {
            ((SentMessageViewHolder) holder).setData(messages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(messages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(senderId)) {
            return SENT;
        }
        return RECEIVED;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final SentMessageContainerBinding binding;

        SentMessageViewHolder(SentMessageContainerBinding sentMessageContainerBinding) {
            super(sentMessageContainerBinding.getRoot());
            binding = sentMessageContainerBinding;
        }

        void setData(Message message) {
            binding.textMessage.setText(message.getMessage());
            binding.textDateTime.setText(message.getDateTime());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ReceivedMessageContainerBinding binding;

        ReceivedMessageViewHolder(ReceivedMessageContainerBinding receivedMessageContainerBinding) {
            super(receivedMessageContainerBinding.getRoot());
            binding = receivedMessageContainerBinding;
        }

        void setData(Message message, Bitmap receiverProfileImage) {
            binding.textMessage.setText(message.getMessage());
            binding.textDateTime.setText(message.getDateTime());
            if(receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
