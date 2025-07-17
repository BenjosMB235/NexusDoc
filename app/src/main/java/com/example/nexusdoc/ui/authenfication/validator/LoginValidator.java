package com.example.nexusdoc.ui.authenfication.validator;


import android.util.Patterns;

public class LoginValidator {

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public ValidationResult validate(String email, String password) {

        if (isEmpty(email, password)) {
            return ValidationResult.error("Veuillez remplir tous les champs");
        }

        if (!isValidEmail(email)) {
            return ValidationResult.error("Format d'email invalide");
        }

        if (password.length() < 6) {
            return ValidationResult.error("Le mot de passe doit contenir au moins 6 caractères");
        }

        return ValidationResult.success();
    }

    private boolean isEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
