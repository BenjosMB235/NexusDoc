package com.example.nexusdoc.ui.authenfication.validator;

import android.util.Patterns;

public class RegistrationValidator {

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

    public ValidationResult validate(String username, String email, String password,
                                     String confirmPassword, String phone, String fonction) {

        if (isEmpty(username, email, password, confirmPassword, phone, fonction)) {
            return ValidationResult.error("Veuillez remplir tous les champs");
        }

        if (!isValidEmail(email)) {
            return ValidationResult.error("Format d'email invalide");
        }

        if (!isValidPassword(password)) {
            return ValidationResult.error("Le mot de passe doit contenir au moins 6 caractères");
        }

        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("Les mots de passe ne correspondent pas");
        }

        if (!isValidPhone(phone)) {
            return ValidationResult.error("Format de téléphone invalide");
        }

        if (username.length() < 2) {
            return ValidationResult.error("Le nom d'utilisateur doit contenir au moins 2 caractères");
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

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    private boolean isValidPhone(String phone) {
        return phone.length() >= 10 && phone.matches("^[0-9+\\-\\s]+$");
    }
}