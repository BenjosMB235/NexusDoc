package com.example.nexusdoc.config;

public class AuthConfig {
    // Configuration centralisée - À déplacer vers des variables d'environnement
    public static final String SUPABASE_URL = "https://vjhelhghluvbvnztkuso.supabase.co";
    public static final String SUPABASE_AUTH_URL = SUPABASE_URL + "/auth/v1/";

    // IMPORTANT : Ces clés doivent être stockées de manière sécurisée
    // Utilisez BuildConfig ou un service de gestion des secrets
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZqaGVsaGdobHV2YnZuenRrdXNvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM2NTIwNTgsImV4cCI6MjA2OTIyODA1OH0.itKMf2zVZ5N7P8Ob1GPj4n3hylk3KbojAEclLi9pmqs";
}