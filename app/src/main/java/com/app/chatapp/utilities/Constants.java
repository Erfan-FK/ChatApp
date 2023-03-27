package com.app.chatapp.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME= "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn" ;
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image" ;
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIME = "time";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MESSAGE_DATA = "data";
    public static final String STRING_MESSAGE_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMessageHeaders = null;
    public static HashMap<String, String> getRemoteMessageHeaders() {
        if (remoteMessageHeaders == null) {
            remoteMessageHeaders = new HashMap<>();
            remoteMessageHeaders.put(REMOTE_MSG_AUTHORIZATION, "key=AAAAHilg2_w:APA91bGPA97C9XzyQskK0E6-eX5wZwjax7SLgDIHRp4kvPXQN0xfWwUco1jWArTbwF-aVEhHH3ckSZc7CVAi1FhBR0-DRAXQ_KoioPKdnMdwqSoLKqwyUFhZtmzobbR8ng7fE8pZSLq4");
            remoteMessageHeaders.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
        }
        return remoteMessageHeaders;
    }
}
