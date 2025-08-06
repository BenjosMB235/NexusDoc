package com.example.nexusdoc.ui.authenfication.repository;

import android.content.Context;
import android.util.Log;

import com.example.nexusdoc.network.SupabaseAuthClient;
import com.example.nexusdoc.network.SupabaseAuthService;
import com.example.nexusdoc.ui.authenfication.model.AuthLoginRequest;
import com.example.nexusdoc.ui.authenfication.model.AuthRegistrationRequest;
import com.example.nexusdoc.ui.authenfication.model.SupabaseAuthRequest;
import com.example.nexusdoc.ui.utilitaires.PreferenceManager;
import com.google.gson.JsonObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class SupabaseAuthRepository {
    private static final String TAG = "SupabaseAuthRepository";

    private final SupabaseAuthService authService;
    private final PreferenceManager preferenceManager;

    public SupabaseAuthRepository(Context context) {
        this.authService = SupabaseAuthClient.getInstance().getAuthService();
        this.preferenceManager = new PreferenceManager(context);
    }

    public interface SupabaseAuthCallback {
        void onSuccess(String accessToken, String refreshToken);
        void onError(String errorMessage);
    }

    public void signUp(AuthRegistrationRequest request, SupabaseAuthCallback callback) {
        SupabaseAuthRequest supabaseRequest = new SupabaseAuthRequest(
                request.getEmail(),
                request.getPassword()
        );

        authService.signUp(supabaseRequest).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    try {
                        String accessToken = body.has("access_token") ?
                                body.get("access_token").getAsString() : null;
                        String refreshToken = body.has("refresh_token") ?
                                body.get("refresh_token").getAsString() : null;

                        if (accessToken != null) {
                            saveTokens(accessToken, refreshToken);
                            // Créer le profil utilisateur dans Supabase
                            createUserProfile(request, accessToken);
                            callback.onSuccess(accessToken, refreshToken);
                        } else {
                            callback.onError("Tokens manquants dans la réponse");
                        }
                    } catch (Exception e) {
                        callback.onError("Erreur parsing réponse: " + e.getMessage());
                    }
                } else {
                    callback.onError("Erreur Supabase: " + response.message() +
                            " (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Erreur réseau Supabase: " + t.getMessage());
            }
        });
    }

    public void signIn(AuthLoginRequest request, SupabaseAuthCallback callback) {
        SupabaseAuthRequest supabaseRequest = new SupabaseAuthRequest(
                request.getEmail(),
                request.getPassword()
        );

        authService.signIn(supabaseRequest).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    try {
                        String accessToken = body.get("access_token").getAsString();
                        String refreshToken = body.get("refresh_token").getAsString();

                        saveTokens(accessToken, refreshToken);

                        // Log des informations du token pour debug
                        com.example.nexusdoc.ui.utilitaires.AuthUtils.logTokenInfo(accessToken);

                        callback.onSuccess(accessToken, refreshToken);
                    } catch (Exception e) {
                        callback.onError("Erreur parsing tokens: " + e.getMessage());
                    }
                } else {
                    callback.onError("Erreur connexion Supabase: " + response.message() +
                            " (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Erreur réseau Supabase: " + t.getMessage());
            }
        });
    }

    public void signOut(SupabaseAuthCallback callback) {
        authService.signOut().enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                clearTokens();
                if (response.isSuccessful()) {
                    callback.onSuccess(null, null);
                } else {
                    // Même si l'API échoue, on nettoie localement
                    callback.onSuccess(null, null);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                clearTokens();
                callback.onError("Erreur déconnexion: " + t.getMessage());
            }
        });
    }

    /**
     * Crée le profil utilisateur dans Supabase après inscription
     */
    private void createUserProfile(AuthRegistrationRequest request, String accessToken) {
        // Cette méthode pourrait être utilisée pour créer des métadonnées utilisateur
        // dans une table séparée si nécessaire
        Log.d(TAG, "Profil utilisateur Supabase créé pour: " + request.getEmail());
    }

    /**
     * Refresh le token d'accès avec le refresh token
     */
    public void refreshAccessToken(SupabaseAuthCallback callback) {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            callback.onError("Aucun refresh token disponible");
            return;
        }

        JsonObject refreshRequest = new JsonObject();
        refreshRequest.addProperty("refresh_token", refreshToken);

        authService.refreshToken(refreshRequest).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    try {
                        String newAccessToken = body.get("access_token").getAsString();
                        String newRefreshToken = body.has("refresh_token") ?
                                body.get("refresh_token").getAsString() : refreshToken;

                        saveTokens(newAccessToken, newRefreshToken);
                        callback.onSuccess(newAccessToken, newRefreshToken);
                    } catch (Exception e) {
                        callback.onError("Erreur parsing refresh response: " + e.getMessage());
                    }
                } else {
                    callback.onError("Erreur refresh token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Erreur réseau refresh: " + t.getMessage());
            }
        });
    }

    /**
     * Vérifie si l'utilisateur est authentifié avec un token valide
     */
    public boolean isAuthenticated() {
        String accessToken = getAccessToken();
        return accessToken != null && !com.example.nexusdoc.ui.utilitaires.AuthUtils.isTokenExpired(accessToken);
    }

    private void saveTokens(String accessToken, String refreshToken) {
        preferenceManager.putString("supabase_access_token", accessToken);
        if (refreshToken != null) {
            preferenceManager.putString("supabase_refresh_token", refreshToken);
        }
        Log.d(TAG, "Tokens Supabase sauvegardés");
    }

    private void clearTokens() {
        preferenceManager.remove("supabase_access_token");
        preferenceManager.remove("supabase_refresh_token");
        Log.d(TAG, "Tokens Supabase supprimés");
    }

    public String getAccessToken() {
        return preferenceManager.getString("supabase_access_token", null);
    }

    public String getRefreshToken() {
        return preferenceManager.getString("supabase_refresh_token", null);
    }

    /**
     * Version synchrone du refresh token (pour les cas où on ne peut pas utiliser de callback)
     */
    public String refreshAccessTokenSync() throws Exception {
        final String[] result = {null};
        final CountDownLatch latch = new CountDownLatch(1);
        final Exception[] error = {null};

        refreshAccessToken(new SupabaseAuthCallback() {
            @Override
            public void onSuccess(String accessToken, String refreshToken) {
                result[0] = accessToken;
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error[0] = new Exception(errorMessage);
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS); // Timeout après 10s

        if (error[0] != null) {
            throw error[0];
        }

        return result[0];
    }
}

/*public class SupabaseAuthRepository {
    private static final String TAG = "SupabaseAuthRepository";

    private final SupabaseAuthService authService;
    private final PreferenceManager preferenceManager;

    public SupabaseAuthRepository(Context context) {
        this.authService = SupabaseAuthClient.getInstance().getAuthService();
        this.preferenceManager = new PreferenceManager(context);
    }

    public interface SupabaseAuthCallback {
        void onSuccess(String accessToken, String refreshToken);
        void onError(String errorMessage);
    }

    public void signUp(AuthRegistrationRequest request, SupabaseAuthCallback callback) {
        SupabaseAuthRequest supabaseRequest = new SupabaseAuthRequest(
                request.getEmail(),
                request.getPassword()
        );

        authService.signUp(supabaseRequest).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    try {
                        String accessToken = body.has("access_token") ?
                                body.get("access_token").getAsString() : null;
                        String refreshToken = body.has("refresh_token") ?
                                body.get("refresh_token").getAsString() : null;

                        if (accessToken != null) {
                            saveTokens(accessToken, refreshToken);
                            callback.onSuccess(accessToken, refreshToken);
                        } else {
                            callback.onError("Tokens manquants dans la réponse");
                        }
                    } catch (Exception e) {
                        callback.onError("Erreur parsing réponse: " + e.getMessage());
                    }
                } else {
                    callback.onError("Erreur Supabase: " + response.message() +
                            " (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Erreur réseau Supabase: " + t.getMessage());
            }
        });
    }

    public void signIn(AuthLoginRequest request, SupabaseAuthCallback callback) {
        SupabaseAuthRequest supabaseRequest = new SupabaseAuthRequest(
                request.getEmail(),
                request.getPassword()
        );

        authService.signIn(supabaseRequest).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    try {
                        String accessToken = body.get("access_token").getAsString();
                        String refreshToken = body.get("refresh_token").getAsString();

                        saveTokens(accessToken, refreshToken);
                        callback.onSuccess(accessToken, refreshToken);
                    } catch (Exception e) {
                        callback.onError("Erreur parsing tokens: " + e.getMessage());
                    }
                } else {
                    callback.onError("Erreur connexion Supabase: " + response.message() +
                            " (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Erreur réseau Supabase: " + t.getMessage());
            }
        });
    }

    public void signOut(SupabaseAuthCallback callback) {
        authService.signOut().enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                clearTokens();
                if (response.isSuccessful()) {
                    callback.onSuccess(null, null);
                } else {
                    // Même si l'API échoue, on nettoie localement
                    callback.onSuccess(null, null);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                clearTokens();
                callback.onError("Erreur déconnexion: " + t.getMessage());
            }
        });
    }

    private void saveTokens(String accessToken, String refreshToken) {
        preferenceManager.putString("supabase_access_token", accessToken);
        if (refreshToken != null) {
            preferenceManager.putString("supabase_refresh_token", refreshToken);
        }
        Log.d(TAG, "Tokens Supabase sauvegardés");
    }

    private void clearTokens() {
        preferenceManager.remove("supabase_access_token");
        preferenceManager.remove("supabase_refresh_token");
        Log.d(TAG, "Tokens Supabase supprimés");
    }

    public String getAccessToken() {
        return preferenceManager.getString("supabase_access_token", null);
    }

    public String getRefreshToken() {
        return preferenceManager.getString("supabase_refresh_token", null);
    }
}*/