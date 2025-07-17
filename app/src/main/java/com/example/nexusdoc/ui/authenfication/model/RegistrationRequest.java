package com.example.nexusdoc.ui.authenfication.model;

import android.net.Uri;

public class RegistrationRequest {
    private final String username;
    private final String email;
    private final String password;
    private final String phone;
    private final String fonction;
    private final Uri profileImageUri;

    public RegistrationRequest(String username, String email, String password, String phone, String fonction, Uri profileImageUri) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.fonction = fonction;
        this.profileImageUri = profileImageUri;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getFonction() {
        return fonction;
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }
}