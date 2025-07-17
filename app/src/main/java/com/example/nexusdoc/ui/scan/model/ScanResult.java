package com.example.nexusdoc.ui.scan.model;

public class ScanResult {
    private final boolean success;
    private final String message;

    private ScanResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ScanResult success(String message) {
        return new ScanResult(true, message);
    }

    public static ScanResult error(String errorMessage) {
        return new ScanResult(false, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getErrorMessage() { return success ? null : message; }
}