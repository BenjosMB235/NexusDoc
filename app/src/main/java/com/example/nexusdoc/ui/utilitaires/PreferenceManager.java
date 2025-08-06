package com.example.nexusdoc.ui.utilitaires;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public class PreferenceManager {

    private static final String TAG = "PreferenceManager";
    private static final String PREF_NAME = "nexusdoc_preferences";
    private static final String ENCRYPTED_PREF_NAME = "nexusdoc_encrypted_preferences";

    private final SharedPreferences preferences;
    private final SharedPreferences encryptedPreferences;
    private final Context context;

    public PreferenceManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.encryptedPreferences = createEncryptedPreferences(context);
    }

    /**
     * Crée des préférences chiffrées pour les données sensibles
     */
    private SharedPreferences createEncryptedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lors de la création des préférences chiffrées", e);
            // Fallback vers les préférences normales en cas d'erreur
            return context.getSharedPreferences(ENCRYPTED_PREF_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    // ==================== MÉTHODES POUR DONNÉES NON SENSIBLES ====================

    /**
     * Sauvegarde une valeur String
     */
    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Récupère une valeur String
     */
    public String getString(String key) {
        return preferences.getString(key, null);
    }

    /**
     * Récupère une valeur String avec valeur par défaut
     */
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    /**
     * Sauvegarde une valeur Boolean
     */
    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Récupère une valeur Boolean
     */
    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    /**
     * Récupère une valeur Boolean avec valeur par défaut
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * Sauvegarde une valeur Integer
     */
    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * Récupère une valeur Integer
     */
    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    /**
     * Récupère une valeur Integer avec valeur par défaut
     */
    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    /**
     * Sauvegarde une valeur Long
     */
    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    /**
     * Récupère une valeur Long
     */
    public long getLong(String key) {
        return preferences.getLong(key, 0L);
    }

    /**
     * Récupère une valeur Long avec valeur par défaut
     */
    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    /**
     * Sauvegarde une valeur Float
     */
    public void putFloat(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    /**
     * Récupère une valeur Float
     */
    public float getFloat(String key) {
        return preferences.getFloat(key, 0f);
    }

    /**
     * Récupère une valeur Float avec valeur par défaut
     */
    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    /**
     * Sauvegarde un Set de Strings
     */
    public void putStringSet(String key, Set<String> value) {
        preferences.edit().putStringSet(key, value).apply();
    }

    /**
     * Récupère un Set de Strings
     */
    public Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, null);
    }

    /**
     * Récupère un Set de Strings avec valeur par défaut
     */
    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return preferences.getStringSet(key, defaultValue);
    }

    // ==================== MÉTHODES POUR DONNÉES SENSIBLES (CHIFFRÉES) ====================

    /**
     * Sauvegarde une valeur String sensible (chiffrée)
     */
    public void putSecureString(String key, String value) {
        try {
            encryptedPreferences.edit().putString(key, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde sécurisée de " + key, e);
            // Fallback vers préférences normales avec encodage Base64
            String encodedValue = Base64.encodeToString(value.getBytes(), Base64.DEFAULT);
            preferences.edit().putString("secure_" + key, encodedValue).apply();
        }
    }

    /**
     * Récupère une valeur String sensible (déchiffrée)
     */
    public String getSecureString(String key) {
        try {
            return encryptedPreferences.getString(key, null);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération sécurisée de " + key, e);
            // Fallback vers préférences normales avec décodage Base64
            String encodedValue = preferences.getString("secure_" + key, null);
            if (encodedValue != null) {
                try {
                    return new String(Base64.decode(encodedValue, Base64.DEFAULT));
                } catch (Exception decodeException) {
                    Log.e(TAG, "Erreur lors du décodage de " + key, decodeException);
                }
            }
            return null;
        }
    }

    /**
     * Sauvegarde une valeur Boolean sensible (chiffrée)
     */
    public void putSecureBoolean(String key, boolean value) {
        try {
            encryptedPreferences.edit().putBoolean(key, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde sécurisée de " + key, e);
            preferences.edit().putBoolean("secure_" + key, value).apply();
        }
    }

    /**
     * Récupère une valeur Boolean sensible (déchiffrée)
     */
    public boolean getSecureBoolean(String key) {
        try {
            return encryptedPreferences.getBoolean(key, false);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération sécurisée de " + key, e);
            return preferences.getBoolean("secure_" + key, false);
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si une clé existe
     */
    public boolean contains(String key) {
        return preferences.contains(key);
    }

    /**
     * Vérifie si une clé sécurisée existe
     */
    public boolean containsSecure(String key) {
        try {
            return encryptedPreferences.contains(key);
        } catch (Exception e) {
            return preferences.contains("secure_" + key);
        }
    }

    /**
     * Supprime une valeur
     */
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    /**
     * Supprime une valeur sécurisée
     */
    public void removeSecure(String key) {
        try {
            encryptedPreferences.edit().remove(key).apply();
        } catch (Exception e) {
            preferences.edit().remove("secure_" + key).apply();
        }
    }

    /**
     * Efface toutes les préférences
     */
    public void clear() {
        preferences.edit().clear().apply();
        try {
            encryptedPreferences.edit().clear().apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'effacement des préférences chiffrées", e);
        }
    }

    /**
     * Efface seulement les préférences utilisateur (garde les préférences système)
     */
    public void clearUserData() {
        SharedPreferences.Editor editor = preferences.edit();

        // Supprimer les données utilisateur spécifiques
        editor.remove(Constants.KEY_IS_SIGNED_IN);
        editor.remove(Constants.KEY_USER_ID);
        editor.remove(Constants.KEY_EMAIL);
        editor.remove(Constants.KEY_USERNAME);
        editor.remove(Constants.KEY_FONCTION);
        editor.remove(Constants.KEY_PHONE);
        editor.remove(Constants.KEY_PROFILE_IMAGE);
        editor.remove(Constants.KEY_LAST_LOGIN);

        editor.apply();

        // Supprimer les données sécurisées
        try {
            SharedPreferences.Editor secureEditor = encryptedPreferences.edit();
            secureEditor.remove(Constants.KEY_AUTH_TOKEN);
            secureEditor.remove(Constants.KEY_REFRESH_TOKEN);
            secureEditor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'effacement des données sécurisées", e);
        }
    }

    /**
     * Sauvegarde les informations utilisateur complètes
     */
    public void saveUserSession(String userId, String email, String username,
                                String fonction, String phone, String authToken) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_FONCTION, fonction);
        editor.putString(Constants.KEY_PHONE, phone);
        editor.putLong(Constants.KEY_LAST_LOGIN, System.currentTimeMillis());

        editor.apply();

        // Sauvegarder le token de manière sécurisée
        if (authToken != null) {
            putSecureString(Constants.KEY_AUTH_TOKEN, authToken);
        }
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    public boolean isUserSignedIn() {
        return getBoolean(Constants.KEY_IS_SIGNED_IN, false) &&
                getString(Constants.KEY_USER_ID) != null;
    }

    /**
     * Récupère l'ID utilisateur actuel
     */
    public String getCurrentUserId() {
        return getString(Constants.KEY_USER_ID);
    }

    /**
     * Récupère l'email utilisateur actuel
     */
    public String getCurrentUserEmail() {
        return getString(Constants.KEY_EMAIL);
    }

    /**
     * Récupère le nom d'utilisateur actuel
     */
    public String getCurrentUsername() {
        return getString(Constants.KEY_USERNAME);
    }

    /**
     * Récupère la fonction utilisateur actuelle
     */
    public String getCurrentUserFunction() {
        return getString(Constants.KEY_FONCTION, "user");
    }

    /**
     * Récupère le token d'authentification
     */
    public String getAuthToken() {
        return getSecureString(Constants.KEY_AUTH_TOKEN);
    }

    /**
     * Met à jour le token d'authentification
     */
    public void updateAuthToken(String token) {
        putSecureString(Constants.KEY_AUTH_TOKEN, token);
    }

    /**
     * Sauvegarde les préférences de l'application
     */
    public void saveAppPreferences(boolean darkMode, String language,
                                   boolean notifications, boolean autoSync) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(Constants.KEY_DARK_MODE, darkMode);
        editor.putString(Constants.KEY_LANGUAGE, language);
        editor.putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, notifications);
        editor.putBoolean(Constants.KEY_AUTO_SYNC, autoSync);

        editor.apply();
    }

    /**
     * Récupère les préférences de thème
     */
    public boolean isDarkModeEnabled() {
        return getBoolean(Constants.KEY_DARK_MODE, false);
    }

    /**
     * Récupère la langue préférée
     */
    public String getPreferredLanguage() {
        return getString(Constants.KEY_LANGUAGE, "fr");
    }

    /**
     * Vérifie si les notifications sont activées
     */
    public boolean areNotificationsEnabled() {
        return getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true);
    }

    /**
     * Vérifie si la synchronisation automatique est activée
     */
    public boolean isAutoSyncEnabled() {
        return getBoolean(Constants.KEY_AUTO_SYNC, true);
    }

    /**
     * Sauvegarde la dernière synchronisation
     */
    public void updateLastSyncTime() {
        putLong(Constants.KEY_LAST_SYNC, System.currentTimeMillis());
    }

    /**
     * Récupère la dernière synchronisation
     */
    public long getLastSyncTime() {
        return getLong(Constants.KEY_LAST_SYNC, 0L);
    }

    /**
     * Exporte les préférences pour sauvegarde/restauration
     */
    public String exportPreferences() {
        // Implémentation pour exporter les préférences non sensibles
        // Retourne un JSON avec les préférences
        return "{}"; // Placeholder
    }

    /**
     * Importe les préférences depuis une sauvegarde
     */
    public boolean importPreferences(String jsonData) {
        // Implémentation pour importer les préférences
        // Parse le JSON et restaure les préférences
        return false; // Placeholder
    }
}