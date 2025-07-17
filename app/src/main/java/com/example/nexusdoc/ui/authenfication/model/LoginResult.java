package com.example.nexusdoc.ui.authenfication.model;


public class LoginResult {
    private final boolean success;
    private final String errorMessage;

    private LoginResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static LoginResult success() {
        return new LoginResult(true, null);
    }

    public static LoginResult error(String errorMessage) {
        return new LoginResult(false, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}