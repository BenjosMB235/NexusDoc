package com.example.nexusdoc.ui.profile.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.example.nexusdoc.ui.profile.model.UpdateProfileRequest;
import com.example.nexusdoc.ui.profile.viewmodel.ProfileViewModel;
import com.example.nexusdoc.ui.utilitaires.Constants;
import com.example.nexusdoc.ui.utilitaires.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final PreferenceManager preferenceManager;
    private final Context context;

    // Taille maximale pour l'image (1MB en Base64 ≈ 750KB image originale)
    private static final int MAX_IMAGE_SIZE = 1024 * 1024; // 1MB
    private static final int IMAGE_QUALITY = 80; // Qualité de compression JPEG

    public ProfileRepository(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
    }

    public interface ProfileCallback {
        void onSuccess(ProfileViewModel.UserProfile profile);
        void onError(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface LogoutCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void getCurrentUserProfile(ProfileCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        firestore.collection(Constants.COLLECTION_USERS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("username");
                        String fonction = document.getString("fonction");
                        String email = document.getString("email");
                        String phone = document.getString("telephone");
                        String profileImageBase64 = document.getString("profileImageBase64");

                        ProfileViewModel.UserProfile profile = new ProfileViewModel.UserProfile(
                                username, fonction, email, phone, profileImageBase64
                        );
                        callback.onSuccess(profile);
                    } else {
                        callback.onError("Profil utilisateur non trouvé");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors du chargement du profil: " + e.getMessage()));
    }

    public void updateUserProfile(UpdateProfileRequest request, UpdateCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        if (request.getProfileImageUri() != null) {
            // Convertir l'image en Base64 avant de sauvegarder
            convertImageToBase64(request.getProfileImageUri(), new ImageConversionCallback() {
                @Override
                public void onSuccess(String base64Image) {
                    updateUserDocument(currentUser.getUid(), request, base64Image, callback);
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            });
        } else {
            updateUserDocument(currentUser.getUid(), request, null, callback);
        }
    }

    private void convertImageToBase64(Uri imageUri, ImageConversionCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onError("Impossible de lire l'image sélectionnée");
                return;
            }

            // Décoder l'image
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                callback.onError("Format d'image non supporté");
                return;
            }

            // Redimensionner l'image si nécessaire
            Bitmap resizedBitmap = resizeImageIfNeeded(originalBitmap);

            // Convertir en Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Vérifier la taille
            if (byteArray.length > MAX_IMAGE_SIZE) {
                callback.onError("L'image est trop volumineuse. Veuillez choisir une image plus petite.");
                return;
            }

            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Nettoyer les ressources
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle();
            }
            originalBitmap.recycle();
            byteArrayOutputStream.close();

            callback.onSuccess(base64Image);

        } catch (Exception e) {
            callback.onError("Erreur lors du traitement de l'image: " + e.getMessage());
        }
    }

    private Bitmap resizeImageIfNeeded(Bitmap originalBitmap) {
        int maxWidth = 800;
        int maxHeight = 800;

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Calculer le ratio de redimensionnement
        float ratio = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        // Si l'image est déjà assez petite, la retourner telle quelle
        if (ratio >= 1.0f) {
            return originalBitmap;
        }

        // Redimensionner l'image
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    private void updateUserDocument(String userId, UpdateProfileRequest request, String base64Image, UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", request.getUsername());
        updates.put("fonction", request.getFonction());
        updates.put("telephone", request.getPhone());
        updates.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        if (base64Image != null) {
            updates.put("profileImageBase64", base64Image);
        }

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Mettre à jour les préférences locales
                    preferenceManager.putString(Constants.KEY_USERNAME, request.getUsername());
                    preferenceManager.putString(Constants.KEY_FONCTION, request.getFonction());
                    preferenceManager.putString(Constants.KEY_PHONE, request.getPhone());

                    if (base64Image != null) {
                        preferenceManager.putString(Constants.KEY_PROFILE_IMAGE, base64Image);
                    }

                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la mise à jour: " + e.getMessage()));
    }

    public void logout(LogoutCallback callback) {
        try {
            auth.signOut();
            preferenceManager.clearUserData();
            callback.onSuccess();
        } catch (Exception e) {
            callback.onError("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    /**
     * Convertit une chaîne Base64 en Bitmap pour l'affichage
     */
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifie si une chaîne Base64 est valide
     */
    public static boolean isValidBase64Image(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return false;
        }

        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            boolean isValid = bitmap != null;
            if (bitmap != null) {
                bitmap.recycle();
            }
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }

    private interface ImageConversionCallback {
        void onSuccess(String base64Image);
        void onError(String errorMessage);
    }
}