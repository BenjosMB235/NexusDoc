package com.example.nexusdoc.ui.utilitaires;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.nexusdoc.ui.authenfication.repository.SupabaseAuthRepository;

import org.json.JSONObject;

import java.util.Date;

/**
 * Gestionnaire centralisé des tokens d'authentification
 */
public class TokenManager {
    private static final String TAG = "TokenManager";

    private final SupabaseAuthRepository supabaseAuthRepo;
    private final PreferenceManager preferenceManager;

    public TokenManager(Context context) {
        this.supabaseAuthRepo = new SupabaseAuthRepository(context);
        this.preferenceManager = new PreferenceManager(context);
    }

    /**
     * Récupère un token d'accès valide (refresh automatique si nécessaire)
     */
    public void getValidAccessToken(TokenCallback callback) {
        String accessToken = supabaseAuthRepo.getAccessToken();

        if (accessToken == null) {
            callback.onError("Aucun token d'accès disponible");
            return;
        }

        // Vérifier si le token est expiré
        if (AuthUtils.isTokenExpired(accessToken)) {
            Log.d(TAG, "Token expiré, tentative de refresh...");

            supabaseAuthRepo.refreshAccessToken(new SupabaseAuthRepository.SupabaseAuthCallback() {
                @Override
                public void onSuccess(String newAccessToken, String newRefreshToken) {
                    Log.d(TAG, "Token refreshé avec succès");
                    callback.onSuccess(newAccessToken);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Échec du refresh token: " + errorMessage);
                    callback.onError("Session expirée, veuillez vous reconnecter");
                }
            });
        } else {
            // Token valide
            callback.onSuccess(accessToken);
        }
    }

    /**
     * Récupère l'UID utilisateur depuis le token actuel
     */
    public String getCurrentUserId() {
        String accessToken = supabaseAuthRepo.getAccessToken();
        if (accessToken == null) {
            return null;
        }

        return AuthUtils.extractUserIdFromJWT(accessToken);
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    public boolean isUserAuthenticated() {
        return supabaseAuthRepo.isAuthenticated();
    }


    /*public String getCurrentAccessToken() {
        //String token = getStoredAccessToken(); // Votre méthode actuelle
        String token = getCurrentUserId();

        // AJOUTER CES LOGS DE DEBUG
        Log.d("TokenManager", "=== TOKEN DEBUG ===");
        Log.d("TokenManager", "Token récupéré: " + (token != null ? "OUI" : "NON"));

        if (token != null) {
            Log.d("TokenManager", "Token length: " + token.length());
            Log.d("TokenManager", "Token preview: " + token.substring(0, Math.min(50, token.length())) + "...");

            // Vérifier si le token est expiré
            try {
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
                    JSONObject json = new JSONObject(payload);
                    long exp = json.getLong("exp");
                    long now = System.currentTimeMillis() / 1000;

                    Log.d("TokenManager", "Token expire à: " + new Date(exp * 1000));
                    Log.d("TokenManager", "Token expiré: " + (now > exp ? "OUI" : "NON"));

                    if (now > exp) {
                        Log.w("TokenManager", "Token expiré, tentative de refresh...");
                        // Logique de refresh du token
                        return refreshAccessToken();
                    }
                }
            } catch (Exception e) {
                Log.e("TokenManager", "Erreur validation token", e);
            }
        } else {
            Log.e("TokenManager", "Aucun token disponible");
        }

        return token;
    }*/

    public String getCurrentAccessToken() {
        String token = supabaseAuthRepo.getAccessToken();

        if (token != null && AuthUtils.isTokenExpired(token)) {
            try {
                Log.w(TAG, "Token expiré, tentative de refresh synchrone...");
                token = supabaseAuthRepo.refreshAccessTokenSync();
            } catch (Exception e) {
                Log.e(TAG, "Échec du refresh token synchrone", e);
                return null;
            }
        }

        return token;
    }

    /**
     * Interface pour les callbacks de token
     */
    public interface TokenCallback {
        void onSuccess(String accessToken);
        void onError(String errorMessage);
    }
}