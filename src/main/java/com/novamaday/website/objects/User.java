package com.novamaday.website.objects;

import java.util.UUID;

/**
 * Created by: NovaFox161 at 4/11/2018
 * Website: https://www.novamaday.com
 * For Project: Personal-Site
 */
public class User {
    private final UUID id;
    private String username;
    private String email;

    public User(UUID _id) {
        id = _id;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    //Setters
    public void setUsername(String _username) {
        username = _username;
    }

    public void setEmail(String _email) {
        email = _email;
    }
}