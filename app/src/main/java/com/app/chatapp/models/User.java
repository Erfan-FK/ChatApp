package com.app.chatapp.models;

import java.io.Serializable;

public class User implements Serializable {
    String name, email, image, token, id;

    public User() {
    }

    public User(String name, String email, String image, String token, String id) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.token = token;
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }
}
