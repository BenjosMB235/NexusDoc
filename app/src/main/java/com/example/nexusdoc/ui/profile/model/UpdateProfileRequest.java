package com.example.nexusdoc.ui.profile.model;

import android.net.Uri;

public class UpdateProfileRequest {
    private final String username;
    private final String fonction;
    private final String email;
    private final String phone;
    private final Uri profileImageUri;

    public UpdateProfileRequest(String username, String fonction, String email, String phone, Uri profileImageUri) {
        this.username = username;
        this.fonction = fonction;
        this.email = email;
        this.phone = phone;
        this.profileImageUri = profileImageUri;
    }

    public String getUsername() { return username; }
    public String getFonction() { return fonction; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Uri getProfileImageUri() { return profileImageUri; }
}