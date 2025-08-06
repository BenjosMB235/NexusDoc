package com.example.nexusdoc.ui.authenfication.model;

public class AuthResult {
    private final boolean success;
    private final String errorMessage;
    private final AuthProvider successfulProvider;

    public enum AuthProvider {
        FIREBASE, SUPABASE, BOTH
    }

    private AuthResult(boolean success, String errorMessage, AuthProvider provider) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.successfulProvider = provider;
    }

    public static AuthResult success(AuthProvider provider) {
        return new AuthResult(true, null, provider);
    }

    public static AuthResult error(String errorMessage) {
        return new AuthResult(false, errorMessage, null);
    }

    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public AuthProvider getSuccessfulProvider() { return successfulProvider; }
}