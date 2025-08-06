package com.example.nexusdoc.config;

//import com.example.nexusdoc.BuildConfig;

public class GedConfig {
    // Configuration Supabase sécurisée
    public static final String SUPABASE_URL = "https://vjhelhghluvbvnztkuso.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZqaGVsaGdobHV2YnZuenRrdXNvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM2NTIwNTgsImV4cCI6MjA2OTIyODA1OH0.itKMf2zVZ5N7P8Ob1GPj4n3hylk3KbojAEclLi9pmqs";

    // URLs API
    public static final String SUPABASE_REST_URL = SUPABASE_URL + "/rest/v1/";
    public static final String SUPABASE_STORAGE_URL = SUPABASE_URL + "/storage/v1/object/";

    // Buckets Storage
    public static final String BUCKET_DOCUMENTS = "ged-documents";
    public static final String BUCKET_THUMBNAILS = "ged-thumbnails";
    public static final String BUCKET_TEMP = "ged-temp";

    // Limites de fichiers
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    public static final long MAX_IMAGE_SIZE = 25 * 1024 * 1024; // 25MB
    public static final long MAX_THUMBNAIL_SIZE = 5 * 1024 * 1024; // 5MB

    // Configuration OCR
    public static final boolean OCR_ENABLED = true;
    public static final String OCR_LANGUAGE = "fra"; // Français par défaut
    public static final float MIN_OCR_CONFIDENCE = 0.6f;

    // Configuration cache
    public static final int CACHE_SIZE_MB = 100;
    public static final long CACHE_EXPIRY_HOURS = 24;

    // Configuration pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Configuration synchronisation
    public static final long SYNC_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes
    public static final int MAX_RETRY_ATTEMPTS = 3;

    // Types MIME supportés
    public static final String[] SUPPORTED_IMAGE_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    public static final String[] SUPPORTED_DOCUMENT_TYPES = {
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    };

    // Configuration tags automatiques
    public static final String[] AUTO_TAGS_KEYWORDS = {
            "contrat", "facture", "devis", "bon de commande", "reçu",
            "certificat", "attestation", "rapport", "procès-verbal",
            "courrier", "email", "note", "mémo"
    };

    // Configuration notifications
    public static final String NOTIFICATION_CHANNEL_ID = "ged_notifications";
    public static final String NOTIFICATION_CHANNEL_NAME = "GED Notifications";

    // Validation des types de fichiers
    public static boolean isImageType(String mimeType) {
        if (mimeType == null) return false;
        for (String type : SUPPORTED_IMAGE_TYPES) {
            if (type.equals(mimeType)) return true;
        }
        return false;
    }

    public static boolean isDocumentType(String mimeType) {
        if (mimeType == null) return false;
        for (String type : SUPPORTED_DOCUMENT_TYPES) {
            if (type.equals(mimeType)) return true;
        }
        return false;
    }

    public static boolean isSupportedType(String mimeType) {
        return isImageType(mimeType) || isDocumentType(mimeType);
    }

    // Génération des chemins de stockage
    public static String generateStoragePath(String userId, String folderId, String fileName) {
        return userId + "/" + folderId + "/" + System.currentTimeMillis() + "_" + fileName;
    }

    public static String generateThumbnailPath(String userId, String documentId) {
        return userId + "/thumbnails/" + documentId + "_thumb.jpg";
    }

    public static String generateTempPath(String userId, String fileName) {
        return userId + "/temp/" + System.currentTimeMillis() + "_" + fileName;
    }
}