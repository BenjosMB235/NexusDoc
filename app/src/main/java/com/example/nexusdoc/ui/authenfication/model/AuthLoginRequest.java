package com.example.nexusdoc.ui.authenfication.model;

import android.net.Uri;

// Modèle unifié pour les requêtes de connexion
public class AuthLoginRequest {
    private final String email;
    private final String password;

    public AuthLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}



