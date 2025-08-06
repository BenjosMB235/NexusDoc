package com.example.nexusdoc.ui.authenfication.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.nexusdoc.ui.authenfication.model.AuthLoginRequest;
import com.example.nexusdoc.ui.authenfication.model.AuthRegistrationRequest;
import com.example.nexusdoc.ui.authenfication.model.AuthResult;
import com.example.nexusdoc.ui.utilitaires.Constants;
import com.example.nexusdoc.ui.utilitaires.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseAuthRepository {
    private static final String TAG = "FirebaseAuthRepository";

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final PreferenceManager preferenceManager;
    private final Context context;
    private final ExecutorService executorService;

    public FirebaseAuthRepository(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
        this.executorService = Executors.newCachedThreadPool();
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void signUp(AuthRegistrationRequest request, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(request.getEmail(), request.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            updateUserProfile(user, request, callback);
                        }
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Erreur d'inscription";
                        callback.onError("Firebase: " + errorMsg);
                    }
                });
    }

    public void signIn(AuthLoginRequest request, AuthCallback callback) {
        auth.signInWithEmailAndPassword(request.getEmail(), request.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToPreferences(user);
                            callback.onSuccess();
                        }
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Erreur de connexion";
                        callback.onError("Firebase: " + errorMsg);
                    }
                });
    }

    public void sendEmailVerification(AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorMsg = task.getException() != null ?
                                    task.getException().getMessage() : "Erreur d'envoi d'email";
                            callback.onError(errorMsg);
                        }
                    });
        } else {
            callback.onError("Utilisateur non connecté");
        }
    }

    public void resetPassword(String email, AuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Erreur de réinitialisation";
                        callback.onError(errorMsg);
                    }
                });
    }

    public void signOut() {
        auth.signOut();
        preferenceManager.clear();
    }

    private void updateUserProfile(FirebaseUser user, AuthRegistrationRequest request, AuthCallback callback) {
        user.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(request.getUsername())
                        .build())
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        createUserDocument(user, request);
                        saveUserToPreferences(user);
                        callback.onSuccess();
                    } else {
                        String errorMsg = profileTask.getException() != null ?
                                profileTask.getException().getMessage() : "Erreur de profil";
                        callback.onError("Profil: " + errorMsg);
                    }
                });
    }

    private void createUserDocument(FirebaseUser user, AuthRegistrationRequest request) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("username", request.getUsername());
        userData.put("fonction", request.getFonction());
        userData.put("telephone", request.getPhone());
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("updatedAt", FieldValue.serverTimestamp());
        userData.put("provider", "firebase");

        if (request.getProfileImageUri() != null) {
            // Traitement asynchrone de l'image
            executorService.execute(() -> {
                String base64Image = convertImageToBase64(request.getProfileImageUri());
                if (base64Image != null) {
                    userData.put("profileImageBase64", base64Image);
                }
                saveUserDocument(user.getUid(), userData);
            });
        } else {
            saveUserDocument(user.getUid(), userData);
        }
    }

    private void saveUserDocument(String userId, Map<String, Object> data) {
        firestore.collection("users").document(userId).set(data)
                .addOnSuccessListener(unused -> Log.d(TAG, "Document utilisateur créé"))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur création document", e));
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            // Redimensionnement optimisé
            android.graphics.Bitmap resizedBitmap = resizeImageIfNeeded(bitmap);

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            String base64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);

            // Nettoyage mémoire
            if (resizedBitmap != bitmap) resizedBitmap.recycle();
            bitmap.recycle();
            outputStream.close();

            return base64;
        } catch (Exception e) {
            Log.e(TAG, "Erreur conversion image", e);
            return null;
        }
    }

    private android.graphics.Bitmap resizeImageIfNeeded(android.graphics.Bitmap original) {
        int maxSize = 800;
        int width = original.getWidth();
        int height = original.getHeight();

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        if (ratio >= 1.0f) return original;

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return android.graphics.Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    private void saveUserToPreferences(FirebaseUser user) {
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
        preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
        preferenceManager.putString(Constants.KEY_EMAIL, user.getEmail());
        preferenceManager.putString("auth_provider", "firebase");
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}