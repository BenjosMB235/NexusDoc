package com.example.nexusdoc.ui.utilitaires;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Utilitaires pour l'authentification et la gestion des tokens JWT
 */
public class AuthUtils {
    private static final String TAG = "AuthUtils";

    /**
     * Extrait l'UID utilisateur depuis un token JWT Supabase
     */
    public static String extractUserIdFromJWT(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            Log.e(TAG, "Token JWT vide ou null");
            return null;
        }

        try {
            // Diviser le JWT en ses parties (header.payload.signature)
            String[] jwtParts = jwtToken.split("\\.");
            if (jwtParts.length != 3) {
                Log.e(TAG, "JWT malformé - nombre de parties incorrect: " + jwtParts.length);
                return null;
            }

            // Décoder le payload (partie 2)
            String payload = jwtParts[1];

            // Ajouter le padding si nécessaire pour Base64
            while (payload.length() % 4 != 0) {
                payload += "=";
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            // Parser le JSON
            JSONObject payloadJson = new JSONObject(decodedPayload);

            // Extraire l'UID (champ "sub")
            if (payloadJson.has("sub")) {
                String userId = payloadJson.getString("sub");
                Log.d(TAG, "UID Supabase extrait avec succès: " + userId);
                return userId;
            } else {
                Log.e(TAG, "Champ 'sub' manquant dans le payload JWT");
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'extraction de l'UID depuis le JWT", e);
            return null;
        }
    }

    /**
     * Vérifie si un token JWT est expiré
     */
    public static boolean isTokenExpired(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return true;
        }

        try {
            String[] jwtParts = jwtToken.split("\\.");
            if (jwtParts.length != 3) {
                return true;
            }

            String payload = jwtParts[1];
            while (payload.length() % 4 != 0) {
                payload += "=";
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            JSONObject payloadJson = new JSONObject(decodedPayload);

            if (payloadJson.has("exp")) {
                long expirationTime = payloadJson.getLong("exp") * 1000; // Convertir en millisecondes
                long currentTime = System.currentTimeMillis();

                boolean isExpired = currentTime >= expirationTime;
                Log.d(TAG, "Token expiré: " + isExpired + " (exp: " + expirationTime + ", now: " + currentTime + ")");
                return isExpired;
            }

            return false; // Si pas d'expiration, considérer comme valide
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la vérification d'expiration du token", e);
            return true; // En cas d'erreur, considérer comme expiré
        }
    }

    /**
     * Extrait l'email depuis un token JWT Supabase
     */
    public static String extractEmailFromJWT(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }

        try {
            String[] jwtParts = jwtToken.split("\\.");
            if (jwtParts.length != 3) {
                return null;
            }

            String payload = jwtParts[1];
            while (payload.length() % 4 != 0) {
                payload += "=";
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            JSONObject payloadJson = new JSONObject(decodedPayload);

            return payloadJson.optString("email", null);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'extraction de l'email depuis le JWT", e);
            return null;
        }
    }

    /**
     * Extrait le rôle utilisateur depuis un token JWT Supabase
     */
    public static String extractRoleFromJWT(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }

        try {
            String[] jwtParts = jwtToken.split("\\.");
            if (jwtParts.length != 3) {
                return null;
            }

            String payload = jwtParts[1];
            while (payload.length() % 4 != 0) {
                payload += "=";
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            JSONObject payloadJson = new JSONObject(decodedPayload);

            return payloadJson.optString("role", "authenticated");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'extraction du rôle depuis le JWT", e);
            return "authenticated"; // Rôle par défaut
        }
    }

    /**
     * Valide la structure d'un token JWT
     */
    public static boolean isValidJWTStructure(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return false;
        }

        String[] parts = jwtToken.split("\\.");
        return parts.length == 3;
    }

    /**
     * Log les informations du token pour debug (sans exposer le token complet)
     */
    public static void logTokenInfo(String jwtToken) {
        if (jwtToken == null) {
            Log.d(TAG, "Token JWT: null");
            return;
        }

        try {
            String userId = extractUserIdFromJWT(jwtToken);
            String email = extractEmailFromJWT(jwtToken);
            String role = extractRoleFromJWT(jwtToken);
            boolean expired = isTokenExpired(jwtToken);

            Log.d(TAG, "Token Info - UID: " + userId + ", Email: " + email +
                    ", Role: " + role + ", Expired: " + expired);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du log des infos token", e);
        }
    }
}