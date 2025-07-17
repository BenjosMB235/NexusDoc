package com.example.nexusdoc.ui.authenfication.model;

public class RegistrationResult {
    private final boolean success;
    private final String errorMessage;

    private RegistrationResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static RegistrationResult success() {
        return new RegistrationResult(true, null);
    }

    public static RegistrationResult error(String errorMessage) {
        return new RegistrationResult(false, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}