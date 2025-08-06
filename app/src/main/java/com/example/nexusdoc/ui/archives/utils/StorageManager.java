package com.example.nexusdoc.ui.archives.utils;

import android.content.Context;
import android.util.Log;

import com.example.nexusdoc.config.GedConfig;
import com.example.nexusdoc.ui.utilitaires.TokenManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class StorageManager {
    private static final String TAG = "StorageManager";

    private final Context context;
    private final OkHttpClient httpClient;
    private final TokenManager tokenManager; // Ajout

    public StorageManager(Context context, TokenManager tokenManager) {
        this.context = context;
        this.httpClient = createHttpClient();
        this.tokenManager = tokenManager; // Initialisation
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS) // Upload peut être long
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /*public String uploadToStorage(String bucket, String path, byte[] data, String mimeType) throws IOException {
        String url = GedConfig.SUPABASE_STORAGE_URL + bucket + "/" + path;

        RequestBody body = RequestBody.create(MediaType.parse(mimeType), data);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + GedConfig.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", mimeType)
                .addHeader("x-upsert", "true") // Permet d'écraser si le fichier existe
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Construire l'URL publique
                String publicUrl = GedConfig.SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path;
                Log.d(TAG, "Upload réussi: " + publicUrl);
                return publicUrl;
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Erreur inconnue";
                throw new IOException("Erreur upload: " + response.code() + " - " + errorBody);
            }
        }
    }*/

    public String uploadToStorage(String bucket, String path, byte[] data, String mimeType) throws IOException {
        String url = GedConfig.SUPABASE_STORAGE_URL + bucket + "/" + path;
        String token = tokenManager.getCurrentAccessToken();

        // Log pour vérification du token
        Log.d(TAG, "Token pour upload: " + (token != null ? "PRÉSENT" : "MANQUANT"));

        RequestBody body = RequestBody.create(MediaType.parse(mimeType), data);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + token) // Utilisation du token utilisateur
                .addHeader("apikey", GedConfig.SUPABASE_ANON_KEY) // Ajout de la clé API
                .addHeader("Content-Type", mimeType)
                .addHeader("x-upsert", "true")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String publicUrl = GedConfig.SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path;
                Log.d(TAG, "Upload réussi: " + publicUrl);
                return publicUrl;
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Erreur inconnue";
                throw new IOException("Erreur upload: " + response.code() + " - " + errorBody);
            }
        }
    }

    /*public boolean deleteFromStorage(String bucket, String path) {
        String url = GedConfig.SUPABASE_STORAGE_URL + bucket + "/" + path;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + GedConfig.SUPABASE_ANON_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d(TAG, "Suppression réussie: " + path);
                return true;
            } else {
                Log.w(TAG, "Erreur suppression: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur réseau suppression", e);
            return false;
        }
    }*/

    public boolean deleteFromStorage(String bucket, String path) {
        String url = GedConfig.SUPABASE_STORAGE_URL + bucket + "/" + path;
        String token = tokenManager.getCurrentAccessToken();

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + token) // Utilisation du token utilisateur
                .addHeader("apikey", GedConfig.SUPABASE_ANON_KEY) // Ajout de la clé API
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d(TAG, "Suppression réussie: " + path);
                return true;
            } else {
                Log.w(TAG, "Erreur suppression: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur réseau suppression", e);
            return false;
        }
    }

    public byte[] downloadFromStorage(String bucket, String path) throws IOException {
        String url = GedConfig.SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().bytes();
            } else {
                throw new IOException("Erreur téléchargement: " + response.code());
            }
        }
    }

    public static String generateTempPath(String userId, String fileName) {
        return userId + "/temp/" + System.currentTimeMillis() + "_" + fileName;
    }

    /*public boolean fileExists(String bucket, String path) {
        String url = GedConfig.SUPABASE_STORAGE_URL + bucket + "/" + path;

        Request request = new Request.Builder()
                .url(url)
                .head() // HEAD request pour vérifier l'existence
                .addHeader("Authorization", "Bearer " + GedConfig.SUPABASE_ANON_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            Log.e(TAG, "Erreur vérification existence fichier", e);
            return false;
        }
    }*/

    public boolean fileExists(String bucket, String path) {
        String url = GedConfig.SUPABASE_STORAGE_URL + bucket + "/" + path;
        String token = tokenManager.getCurrentAccessToken();

        Request request = new Request.Builder()
                .url(url)
                .head()
                .addHeader("Authorization", "Bearer " + token) // Utilisation du token utilisateur
                .addHeader("apikey", GedConfig.SUPABASE_ANON_KEY) // Ajout de la clé API
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            Log.e(TAG, "Erreur vérification existence fichier", e);
            return false;
        }
    }

    public String getPublicUrl(String bucket, String path) {
        return GedConfig.SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    public void cleanup() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }

    // Méthodes utilitaires
    public static String extractPathFromUrl(String url) {
        if (url == null || !url.contains("/storage/v1/object/public/")) {
            return null;
        }

        int startIndex = url.indexOf("/storage/v1/object/public/") + "/storage/v1/object/public/".length();
        return url.substring(startIndex);
    }

    public static String extractBucketFromPath(String path) {
        if (path == null || !path.contains("/")) {
            return null;
        }

        return path.substring(0, path.indexOf("/"));
    }

    public static String extractFilePathFromPath(String path) {
        if (path == null || !path.contains("/")) {
            return path;
        }

        return path.substring(path.indexOf("/") + 1);
    }
}