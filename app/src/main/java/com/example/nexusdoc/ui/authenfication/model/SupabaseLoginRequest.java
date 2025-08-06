package com.example.nexusdoc.ui.authenfication.model;

public class SupabaseLoginRequest {
    private final String email;
    private final String password;

    public SupabaseLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
