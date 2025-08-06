package com.example.nexusdoc.ui.authenfication.validator;

import android.util.Patterns;

public class AuthValidator {

    public ValidationResult validateLogin(String email, String password) {
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

    public ValidationResult validateRegistration(String username, String email, String password,
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