package com.example.nexusdoc.ui.utilitaires;

public class Constants {

    // ==================== CLÉS UTILISATEUR ====================
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FONCTION = "fonction";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PROFILE_IMAGE = "profileImage";
    public static final String KEY_LAST_LOGIN = "lastLogin";

    // ==================== CLÉS SÉCURISÉES ====================
    public static final String KEY_AUTH_TOKEN = "authToken";
    public static final String KEY_REFRESH_TOKEN = "refreshToken";
    public static final String KEY_BIOMETRIC_KEY = "biometricKey";

    // ==================== PRÉFÉRENCES APPLICATION ====================
    public static final String KEY_DARK_MODE = "darkMode";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    public static final String KEY_AUTO_SYNC = "autoSync";
    public static final String KEY_LAST_SYNC = "lastSync";

    // ==================== PRÉFÉRENCES SÉCURITÉ ====================
    public static final String KEY_BIOMETRIC_ENABLED = "biometricEnabled";
    public static final String KEY_PIN_ENABLED = "pinEnabled";
    public static final String KEY_AUTO_LOCK_TIME = "autoLockTime";
    public static final String KEY_FAILED_ATTEMPTS = "failedAttempts";
    public static final String KEY_LAST_FAILED_ATTEMPT = "lastFailedAttempt";

    // ==================== PRÉFÉRENCES CACHE ====================
    public static final String KEY_CACHE_SIZE = "cacheSize";
    public static final String KEY_CACHE_EXPIRY = "cacheExpiry";
    public static final String KEY_OFFLINE_MODE = "offlineMode";

    // ==================== PRÉFÉRENCES INTERFACE ====================
    public static final String KEY_FONT_SIZE = "fontSize";
    public static final String KEY_ANIMATION_ENABLED = "animationEnabled";
    public static final String KEY_HAPTIC_FEEDBACK = "hapticFeedback";
    public static final String KEY_SOUND_ENABLED = "soundEnabled";

    // ==================== PRÉFÉRENCES RÉSEAU ====================
    public static final String KEY_WIFI_ONLY_SYNC = "wifiOnlySync";
    public static final String KEY_BACKGROUND_SYNC = "backgroundSync";
    public static final String KEY_SYNC_FREQUENCY = "syncFrequency";

    // ==================== PRÉFÉRENCES DÉVELOPPEUR ====================
    public static final String KEY_DEBUG_MODE = "debugMode";
    public static final String KEY_CRASH_REPORTING = "crashReporting";
    public static final String KEY_ANALYTICS_ENABLED = "analyticsEnabled";

    // ==================== VALEURS PAR DÉFAUT ====================
    public static final String DEFAULT_LANGUAGE = "fr";
    public static final String DEFAULT_FONCTION = "user";
    public static final int DEFAULT_AUTO_LOCK_TIME = 300000; // 5 minutes
    public static final int DEFAULT_SYNC_FREQUENCY = 3600000; // 1 heure
    public static final int MAX_FAILED_ATTEMPTS = 5;

    // ==================== CODES D'ERREUR ====================
    public static final int ERROR_NETWORK = 1001;
    public static final int ERROR_AUTH = 1002;
    public static final int ERROR_VALIDATION = 1003;
    public static final int ERROR_PERMISSION = 1004;
    public static final int ERROR_STORAGE = 1005;

    // ==================== CODES DE REQUÊTE ====================
    public static final int REQUEST_CODE_CAMERA = 2001;
    public static final int REQUEST_CODE_GALLERY = 2002;
    public static final int REQUEST_CODE_PERMISSION = 2003;
    public static final int REQUEST_CODE_BIOMETRIC = 2004;

    // ==================== EXTRAS INTENT ====================
    public static final String EXTRA_USER_ID = "extraUserId";
    public static final String EXTRA_EMAIL = "extraEmail";
    public static final String EXTRA_REDIRECT_TO = "extraRedirectTo";
    public static final String EXTRA_NOTIFICATION_DATA = "extraNotificationData";

    // ==================== COLLECTIONS FIRESTORE ====================
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_DOCUMENTS = "documents";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_SETTINGS = "settings";

    // ==================== CHAMPS FIRESTORE ====================
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_IS_ACTIVE = "isActive";

    // ==================== URLS ET ENDPOINTS ====================
    public static final String BASE_URL = "https://api.nexusdoc.com/";
    public static final String PRIVACY_POLICY_URL = "https://nexusdoc.com/privacy";
    public static final String TERMS_OF_SERVICE_URL = "https://nexusdoc.com/terms";
    public static final String SUPPORT_EMAIL = "support@nexusdoc.com";

    // ==================== FORMATS ET PATTERNS ====================
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    public static final String PHONE_PATTERN = "^[+]?[0-9]{10,15}$";

    // ==================== TAILLES ET LIMITES ====================
    public static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    public static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MAX_BIO_LENGTH = 500;

    // Constructeur privé pour empêcher l'instanciation
    private Constants() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }
}