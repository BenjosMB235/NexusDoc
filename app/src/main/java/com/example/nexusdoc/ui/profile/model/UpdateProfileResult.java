package com.example.nexusdoc.ui.profile.model;

public class UpdateProfileResult {
    private final boolean success;
    private final String errorMessage;

    private UpdateProfileResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static UpdateProfileResult success() {
        return new UpdateProfileResult(true, null);
    }

    public static UpdateProfileResult error(String errorMessage) {
        return new UpdateProfileResult(false, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}