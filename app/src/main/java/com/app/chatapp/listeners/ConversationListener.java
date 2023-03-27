package com.app.chatapp.listeners;

import com.app.chatapp.models.User;

public interface ConversationListener {
    void  onConversationClicked(User user);
}
