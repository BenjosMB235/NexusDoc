package com.example.nexusdoc.ui.authenfication.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.nexusdoc.network.SupabaseAuthClient;
import com.example.nexusdoc.network.SupabaseAuthService;
import com.example.nexusdoc.ui.authenfication.model.LoginRequest;
import com.example.nexusdoc.ui.authenfication.model.RegistrationRequest;
import com.example.nexusdoc.ui.authenfication.model.SupabaseLoginRequest;
import com.example.nexusdoc.ui.authenfication.model.SupabaseSignUpRequest;
import com.example.nexusdoc.ui.utilitaires.Constants;
import com.example.nexusdoc.ui.utilitaires.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;


public class AuthRepository {

 /*   private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final PreferenceManager preferenceManager;
    private final Context context;
    //private final SupabaseAuthClient supabaseClient;
    private final SupabaseAuthService supabaseService;

    public AuthRepository(Context context, SupabaseAuthClient supabaseClient) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
        this.supabaseService = supabaseClient.getSupabaseService();
    }

    public interface RegistrationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface EmailVerificationCallback {
        void onSent();
        void onError(String errorMessage);
    }

    public interface LoginCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface PasswordResetCallback {
        void onSent();
        void onError(String errorMessage);
    }

    public void registerUser(RegistrationRequest request, RegistrationCallback callback) {
        auth.createUserWithEmailAndPassword(request.getEmail(), request.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            updateUserProfile(user, request, callback);
                            // 🔄 Inscription dans Supabase
                            registerUserInSupabase(request);
                        }
                    } else {
                        callback.onError("Échec de l'inscription: " + task.getException().getMessage());
                    }
                });
    }

    private void updateUserProfile(FirebaseUser user, RegistrationRequest request, RegistrationCallback callback) {
        user.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(request.getUsername())
                        .build())
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        createUserDocument(user, request);
                        saveToPreferences(user);
                        callback.onSuccess();
                    } else {
                        callback.onError("Échec de la mise à jour du profil: " + profileTask.getException().getMessage());
                    }
                });
    }

    private void createUserDocument(FirebaseUser user, RegistrationRequest request) {
        firestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("email", user.getEmail());
                        data.put("username", request.getUsername());
                        data.put("fonction", request.getFonction());
                        data.put("telephone", request.getPhone());
                        data.put("createdAt", FieldValue.serverTimestamp());
                        data.put("updatedAt", FieldValue.serverTimestamp());
                        data.put("provider", user.getProviderId());

                        if (request.getProfileImageUri() != null) {
                            convertImageToBase64(request.getProfileImageUri(), base64Image -> {
                                if (base64Image != null) {
                                    data.put("profileImageBase64", base64Image);
                                }
                                saveUserDocument(user.getUid(), data);
                            });
                        } else {
                            saveUserDocument(user.getUid(), data);
                        }
                    }
                });
    }

    private void saveUserDocument(String userId, Map<String, Object> data) {
        firestore.collection("users").document(userId).set(data)
                .addOnSuccessListener(unused -> Log.d("AuthRepository", "User document created"))
                .addOnFailureListener(e -> Log.e("AuthRepository", "Error creating user document", e));
    }

    private void convertImageToBase64(Uri imageUri, ImageCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onResult(null);
                return;
            }

            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                callback.onResult(null);
                return;
            }

            android.graphics.Bitmap resizedBitmap = resizeImageIfNeeded(bitmap);

            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle();
            }
            bitmap.recycle();
            byteArrayOutputStream.close();

            callback.onResult(base64Image);

        } catch (Exception e) {
            Log.e("AuthRepository", "Error converting image to base64", e);
            callback.onResult(null);
        }
    }

    private android.graphics.Bitmap resizeImageIfNeeded(android.graphics.Bitmap originalBitmap) {
        int maxWidth = 800;
        int maxHeight = 800;

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        if (ratio >= 1.0f) return originalBitmap;

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    private interface ImageCallback {
        void onResult(String base64Image);
    }

    private void saveToPreferences(FirebaseUser user) {
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
        preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
        preferenceManager.putString(Constants.KEY_EMAIL, user.getEmail());
        preferenceManager.putString(Constants.KEY_FONCTION, "user");
    }

    public void sendEmailVerification(EmailVerificationCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSent();
                        } else {
                            callback.onError("Échec de l'envoi de l'email: " + task.getException().getMessage());
                        }
                    });
        }
    }

    public void checkEmailVerification(EmailVerificationCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    callback.onSent();
                } else {
                    callback.onError("Email not verified yet");
                }
            });
        }
    }

    public void loginUser(LoginRequest request, LoginCallback callback) {
        auth.signInWithEmailAndPassword(request.getEmail(), request.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveToPreferences(user);
                            callback.onSuccess();
                            // 🔄 Connexion Supabase
                            loginUserInSupabase(request);
                        }
                    } else {
                        callback.onError("Échec de la connexion: " + task.getException().getMessage());
                    }
                });
    }

    public void resetPassword(String email, PasswordResetCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSent();
                    } else {
                        callback.onError("Échec de l'envoi de l'email: " + task.getException().getMessage());
                    }
                });
    }

    // 🔐 Appelle l’API Supabase Auth login
    private void loginUserInSupabase(LoginRequest request) {
        SupabaseAuthService authService = supabaseService;

        SupabaseLoginRequest loginRequest = new SupabaseLoginRequest(
                request.getEmail(),
                request.getPassword()
        );

        authService.loginUser(loginRequest).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    String accessToken = body.get("access_token").getAsString();
                    String refreshToken = body.get("refresh_token").getAsString();

                    // Stockage optionnel du token pour requêtes futures
                    preferenceManager.putString("supabase_access_token", accessToken);
                    preferenceManager.putString("supabase_refresh_token", refreshToken);

                    Log.d("Supabase", "Connexion Supabase réussie");
                } else {
                    Log.e("Supabase", "Erreur connexion Supabase: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Supabase", "Erreur réseau Supabase: " + t.getMessage());
            }
        });
    }

    // 🔐 Appelle l’API Supabase Auth signup
    private void registerUserInSupabase(RegistrationRequest request) {
        SupabaseAuthService authService = supabaseService;

        SupabaseSignUpRequest supabaseRequest = new SupabaseSignUpRequest(
                request.getEmail(),
                request.getPassword()
        );

        authService.registerUser(supabaseRequest).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Log.d("Supabase", "Utilisateur inscrit dans Supabase");
                } else {
                    Log.e("Supabase", "Erreur inscription Supabase: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Supabase", "Erreur réseau Supabase: " + t.getMessage());
            }
        });
    }
*/
}
