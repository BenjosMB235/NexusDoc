package com.example.nexusdoc.ui.archives.repository;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.example.nexusdoc.config.GedConfig;
import com.example.nexusdoc.network.GedSupabaseClient;
import com.example.nexusdoc.network.GedSupabaseService;
import com.example.nexusdoc.network.SupabaseAuthClient;
import com.example.nexusdoc.ui.archives.model.GedDocument;
import com.example.nexusdoc.ui.archives.model.GedFolder;
import com.example.nexusdoc.ui.archives.model.ScanHistory;
import com.example.nexusdoc.ui.archives.model.UserPreferences;
import com.example.nexusdoc.ui.archives.utils.FileUtils;
import com.example.nexusdoc.ui.archives.utils.ImageProcessor;
import com.example.nexusdoc.ui.archives.utils.OcrProcessor;
import com.example.nexusdoc.ui.archives.utils.StorageManager;
import com.example.nexusdoc.ui.authenfication.repository.SupabaseAuthRepository;
import com.example.nexusdoc.ui.data.models.User;
import com.example.nexusdoc.ui.utilitaires.AuthUtils;
import com.example.nexusdoc.ui.utilitaires.TokenManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GedRepository {
    private static final String TAG = "GedRepository";

    private final Context context;
    private final GedSupabaseService supabaseService;
    private final SupabaseAuthRepository supabaseAuthRepo;
    private final TokenManager tokenManager;
    private final StorageManager storageManager;
    private final ImageProcessor imageProcessor;
    private final OcrProcessor ocrProcessor;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    // Interfaces de callback
    public interface FoldersCallback {
        void onSuccess(List<GedFolder> folders);
        void onError(String errorMessage);
    }

    public interface DocumentsCallback {
        void onSuccess(List<GedDocument> documents);
        void onError(String errorMessage);
    }

    public interface FolderCallback {
        void onSuccess(GedFolder folder);
        void onError(String errorMessage);
    }

    public interface DocumentCallback {
        void onSuccess(GedDocument document);
        void onError(String errorMessage);
    }

    public interface FolderContentsCallback {
        void onSuccess(List<GedFolder> folders, List<GedDocument> documents);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface ProgressCallback {
        void onProgress(int percentage);
        void onComplete(String result);
        void onError(String errorMessage);
    }

    public interface SearchCallback {
        void onSuccess(List<GedFolder> folders, List<GedDocument> documents);
        void onError(String errorMessage);
    }

    public interface StorageStatsCallback {
        void onSuccess(int documentCount, long totalSize);
        void onError(String errorMessage);
    }

    public GedRepository(Context context) {
        this.context = context;
        this.supabaseAuthRepo = new SupabaseAuthRepository(context);
        this.tokenManager = new TokenManager(context);
        this.mainHandler = new Handler(Looper.getMainLooper());

        // Configurer le client Supabase avec le repository d'auth
        GedSupabaseClient.getInstance().setAuthRepository(supabaseAuthRepo);
        this.supabaseService = GedSupabaseClient.getInstance().getService();

        this.storageManager = new StorageManager(context, tokenManager);
        this.imageProcessor = new ImageProcessor(context);
        this.ocrProcessor = new OcrProcessor(context);

        this.executorService = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    // ==================== FOLDERS ====================

    public void getFoldersByType(String sectionType, FoldersCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                String select = GedSupabaseClient.buildSelectQuery(
                        "id", "name", "parent_id", "path", "type", "description",
                        "document_count", "subfolder_count", "total_size", "is_synced",
                        "created_at", "updated_at"
                );

                supabaseService.getFoldersByType("eq." + userId, "eq." + sectionType, "is.null", select)
                        .enqueue(new Callback<List<GedFolder>>() {
                            @Override
                            public void onResponse(Call<List<GedFolder>> call, Response<List<GedFolder>> response) {
                                runOnMainThread(() -> {
                                    if (response.isSuccessful() && response.body() != null) {
                                        callback.onSuccess(response.body());
                                    } else {
                                        try {
                                            Log.e(TAG, "Erreur getFoldersByType - URL: " + call.request().url() +
                                                    " | Code: " + response.code() +
                                                    " | Message: " + response.message() +
                                                    " | Body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        callback.onError("Erreur HTTP: " + response.code());
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<List<GedFolder>> call, Throwable t) {
                                Log.e(TAG, "Erreur réseau getFoldersByType", t);
                                runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    public void getFolderByPath(String targetPath, String sectionType, FolderCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                String select = GedSupabaseClient.buildSelectQuery(
                        "id", "name", "parent_id", "path", "type", "description",
                        "document_count", "subfolder_count", "total_size", "created_at", "updated_at"
                );

                supabaseService.getFolderByPath("eq." + userId, "eq." + sectionType, "eq." + targetPath, select)
                        .enqueue(new Callback<List<GedFolder>>() {
                            @Override
                            public void onResponse(Call<List<GedFolder>> call, Response<List<GedFolder>> response) {
                                runOnMainThread(() -> {
                                    if (response.isSuccessful() && response.body() != null) {
                                        List<GedFolder> folders = response.body();
                                        if (!folders.isEmpty()) {
                                            callback.onSuccess(folders.get(0));
                                        } else {
                                            callback.onSuccess(null);
                                        }
                                    } else {
                                        callback.onError("Erreur HTTP: " + response.code());
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<List<GedFolder>> call, Throwable t) {
                                runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    public void getFolderById(String folderId, FolderCallback callback) {
        String select = GedSupabaseClient.buildSelectQuery(
                "id", "name", "parent_id", "path", "type", "description",
                "document_count", "subfolder_count", "total_size", "created_at", "updated_at"
        );

        supabaseService.getFolderById("eq." + folderId, select)
                .enqueue(new Callback<List<GedFolder>>() {
                    @Override
                    public void onResponse(Call<List<GedFolder>> call, Response<List<GedFolder>> response) {
                        runOnMainThread(() -> {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                callback.onSuccess(response.body().get(0));
                            } else {
                                callback.onError("Dossier introuvable: " + response.code());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<GedFolder>> call, Throwable t) {
                        runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                    }
                });
    }

    public void getFolderContents(String folderId, FolderContentsCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                class FolderContentsTracker {
                    private List<GedFolder> folders = new ArrayList<>();
                    private List<GedDocument> documents = new ArrayList<>();
                    private boolean foldersSuccess = false;
                    private boolean documentsSuccess = false;
                    private String errorMessage = null;

                    synchronized void onFoldersLoaded(Response<List<GedFolder>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            this.folders = response.body();
                            this.foldersSuccess = true;
                        } else {
                            this.errorMessage = "Erreur chargement dossiers: " + response.code();
                        }
                        checkCompletion();
                    }

                    synchronized void onDocumentsLoaded(Response<List<GedDocument>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            this.documents = response.body();
                            this.documentsSuccess = true;
                        } else {
                            this.errorMessage = "Erreur chargement documents: " + response.code();
                        }
                        checkCompletion();
                    }

                    synchronized void onFailure(String message) {
                        this.errorMessage = message;
                        checkCompletion();
                    }

                    private void checkCompletion() {
                        if ((foldersSuccess || errorMessage != null) &&
                                (documentsSuccess || errorMessage != null)) {
                            notifyCompletion();
                        }
                    }

                    private void notifyCompletion() {
                        runOnMainThread(() -> {
                            if (errorMessage != null) {
                                callback.onError(errorMessage);
                            } else {
                                callback.onSuccess(folders, documents);
                            }
                        });
                    }
                }

                final FolderContentsTracker tracker = new FolderContentsTracker();

                String folderSelect = "id,name,parent_id,path,type,document_count," +
                        "subfolder_count,total_size,created_at,updated_at";

                supabaseService.getFoldersByParent("eq." + userId, "eq." + folderId, folderSelect)
                        .enqueue(new Callback<List<GedFolder>>() {
                            @Override
                            public void onResponse(Call<List<GedFolder>> call, Response<List<GedFolder>> response) {
                                tracker.onFoldersLoaded(response);
                            }

                            @Override
                            public void onFailure(Call<List<GedFolder>> call, Throwable t) {
                                tracker.onFailure("Erreur réseau (dossiers): " + t.getMessage());
                            }
                        });

                String documentSelect = "id,name,type,mime_type,size,content_url," +
                        "thumbnail_url,is_favorite,is_shared,created_at," +
                        "updated_at,ged_document_tags(tag_id(name,color))";

                supabaseService.getDocumentsByFolder("eq." + userId, "eq." + folderId, documentSelect, "created_at.desc")
                        .enqueue(new Callback<List<GedDocument>>() {
                            @Override
                            public void onResponse(Call<List<GedDocument>> call, Response<List<GedDocument>> response) {
                                tracker.onDocumentsLoaded(response);
                            }

                            @Override
                            public void onFailure(Call<List<GedDocument>> call, Throwable t) {
                                tracker.onFailure("Erreur réseau (documents): " + t.getMessage());
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    public void createFolder(GedFolder folder, OperationCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                if (folder.getParentId() != null) {
                    getFolderById(folder.getParentId(), new FolderCallback() {
                        @Override
                        public void onSuccess(GedFolder parentFolder) {
                            // CORRECTION: Construction correcte du path
                            String parentPath = parentFolder.getPath();
                            if (parentPath == null || parentPath.isEmpty()) {
                                parentPath = "/";
                            }

                            String newPath;
                            if (parentPath.equals("/")) {
                                newPath = "/" + folder.getName();
                            } else {
                                newPath = parentPath + "/" + folder.getName();
                            }
                            folder.setPath(newPath);

                            completeFolderCreation(folder, userId, callback);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError("Erreur récupération dossier parent: " + errorMessage);
                        }
                    });
                } else {
                    // Dossier racine avec le type de section
                    folder.setPath("/" + folder.getName());
                    completeFolderCreation(folder, userId, callback);
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    private void completeFolderCreation(GedFolder folder, String userId, OperationCallback callback) {
        folder.setUserId(userId);
        folder.setCreatedAt(new Date());
        folder.setUpdatedAt(new Date());
        folder.setSynced(true);

        supabaseService.createFolder(folder).enqueue(new Callback<List<GedFolder>>() {
            @Override
            public void onResponse(Call<List<GedFolder>> call, Response<List<GedFolder>> response) {
                runOnMainThread(() -> {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        GedFolder createdFolder = response.body().get(0);
                        Log.d(TAG, "Dossier créé avec succès: " + createdFolder.getName() + " - Path: " + createdFolder.getPath());

                        if (folder.getParentId() != null) {
                            updateParentFolderCount(folder.getParentId(), 1, 0);
                        }
                        callback.onSuccess("Dossier créé avec succès");
                    } else {
                        try {
                            Log.e(TAG, "Erreur création dossier '" + folder.getName() + "' - User: " + userId +
                                    " | Parent: " + folder.getParentId() +
                                    " | Code: " + response.code() +
                                    " | Erreur: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                        } catch (IOException e) {
                            Log.e(TAG, "Erreur lecture errorBody", e);
                        }
                        callback.onError("Erreur création: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<List<GedFolder>> call, Throwable t) {
                Log.e(TAG, "Erreur réseau création dossier", t);
                runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
            }
        });
    }

    // ==================== DOCUMENTS ====================

    public void importDocument(Uri fileUri, String folderId, ProgressCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            runOnMainThread(() -> callback.onError("Utilisateur non connecté"));
            return;
        }

        executorService.execute(() -> {
            try {
                runOnMainThread(() -> callback.onProgress(10));

                FileUtils.FileInfo fileInfo = FileUtils.getFileInfo(context, fileUri);
                if (fileInfo == null) {
                    runOnMainThread(() -> callback.onError("Impossible de lire le fichier"));
                    return;
                }

                if (fileInfo.size > GedConfig.MAX_FILE_SIZE) {
                    runOnMainThread(() -> callback.onError("Fichier trop volumineux (max " +
                            FileUtils.formatFileSize(GedConfig.MAX_FILE_SIZE) + ")"));
                    return;
                }

                if (!GedConfig.isSupportedType(fileInfo.mimeType)) {
                    runOnMainThread(() -> callback.onError("Type de fichier non supporté"));
                    return;
                }

                runOnMainThread(() -> callback.onProgress(30));

                if (GedConfig.isImageType(fileInfo.mimeType)) {
                    processImageDocument(fileUri, fileInfo, folderId, userId, callback);
                } else {
                    processRegularDocument(fileUri, fileInfo, folderId, userId, callback);
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur import document", e);
                runOnMainThread(() -> callback.onError("Erreur lors de l'importation: " + e.getMessage()));
            }
        });
    }

    private void processRegularDocument(Uri fileUri, FileUtils.FileInfo fileInfo,
                                        String folderId, String userId, ProgressCallback callback) {
        // AJOUTER CES LOGS DE DEBUG
        Log.d(TAG, "=== DEBUG UPLOAD ===");
        Log.d(TAG, "User ID from token: " + userId);
        Log.d(TAG, "Folder ID: " + folderId);
        Log.d(TAG, "File name: " + fileInfo.name);

        try {
            runOnMainThread(() -> callback.onProgress(50));

            byte[] fileData = FileUtils.readFileBytes(context, fileUri);
            if (fileData == null) {
                runOnMainThread(() -> callback.onError("Impossible de lire le fichier"));
                return;
            }

            runOnMainThread(() -> callback.onProgress(70));

            // CORRECTION: Construction correcte du chemin de stockage
            String sanitizedName = fileInfo.name.replaceAll("[^a-zA-Z0-9._-]", "_");
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filePath = userId + "/" + (folderId != null ? folderId : "root") + "/" + timestamp + "_" + sanitizedName;

            Log.d(TAG, "Upload fichier vers: " + filePath);

            try {
                String fileUrl = storageManager.uploadToStorage(
                        GedConfig.BUCKET_DOCUMENTS, filePath, fileData, fileInfo.mimeType
                );

                runOnMainThread(() -> callback.onProgress(90));

                GedDocument document = new GedDocument(fileInfo.name, "document", folderId);
                document.setUserId(userId);
                document.setMimeType(fileInfo.mimeType);
                document.setSize(fileData.length);
                document.setContentUrl(fileUrl);
                document.setSynced(true);

                // CORRECTION: Ne plus définir les tags directement ici
                // Les tags seront gérés séparément après la création du document
                List<String> autoTags = generateAutoTags(fileInfo.name);

                createDocumentInDatabase(document, autoTags, callback);

            } catch (Exception uploadError) {
                Log.e(TAG, "Erreur upload vers Supabase Storage", uploadError);
                runOnMainThread(() -> callback.onError("Erreur upload: " + uploadError.getMessage()));
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur traitement document '" + fileInfo.name + "'", e);
            runOnMainThread(() -> callback.onError("Erreur traitement document: " + e.getMessage()));
        }
    }

    private void processImageDocument(Uri imageUri, FileUtils.FileInfo fileInfo,
                                      String folderId, String userId, ProgressCallback callback) {
        try {
            runOnMainThread(() -> callback.onProgress(40));

            ImageProcessor.ProcessedImage processed = imageProcessor.processImage(
                    imageUri, ImageProcessor.Quality.MEDIUM
            );

            runOnMainThread(() -> callback.onProgress(60));

            // Upload de l'image principale avec path corrigé
            String sanitizedName = fileInfo.name.replaceAll("[^a-zA-Z0-9._-]", "_");
            String timestamp = String.valueOf(System.currentTimeMillis());
            String imagePath = userId + "/" + (folderId != null ? folderId : "root") + "/" + timestamp + "_" + sanitizedName;

            String imageUrl = storageManager.uploadToStorage(
                    GedConfig.BUCKET_DOCUMENTS, imagePath, processed.imageData, fileInfo.mimeType
            );

            runOnMainThread(() -> callback.onProgress(70));

            // Upload de la miniature
            String thumbPath = userId + "/thumbnails/" + timestamp + "_thumb_" + sanitizedName.replaceAll("\\.[^.]+$", ".jpg");
            String thumbUrl = storageManager.uploadToStorage(
                    GedConfig.BUCKET_THUMBNAILS, thumbPath, processed.thumbnailData, "image/jpeg"
            );

            runOnMainThread(() -> callback.onProgress(80));

            // OCR si activé
            String ocrText = null;
            Float ocrConfidence = null;
            if (GedConfig.OCR_ENABLED) {
                OcrProcessor.OcrResult ocrResult = ocrProcessor.processImage(processed.imageData);
                if (ocrResult != null && ocrResult.confidence >= GedConfig.MIN_OCR_CONFIDENCE) {
                    ocrText = ocrResult.text;
                    ocrConfidence = ocrResult.confidence;
                }
            }

            runOnMainThread(() -> callback.onProgress(90));

            GedDocument document = new GedDocument(fileInfo.name, "image", folderId);
            document.setUserId(userId);
            document.setMimeType(fileInfo.mimeType);
            document.setSize(processed.imageData.length);
            document.setContentUrl(imageUrl);
            document.setThumbnailUrl(thumbUrl);
            document.setOcrText(ocrText);
            document.setOcrConfidence(ocrConfidence);
            document.setSynced(true);

            List<String> autoTags = new ArrayList<>();
            if (ocrText != null) {
                autoTags = generateAutoTags(ocrText);
            }

            createDocumentInDatabase(document, autoTags, callback);

        } catch (Exception e) {
            Log.e(TAG, "Erreur traitement image", e);
            runOnMainThread(() -> callback.onError("Erreur traitement image: " + e.getMessage()));
        }
    }

    public void processCameraBitmap(Bitmap bitmap, String folderId, ProgressCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            runOnMainThread(() -> callback.onError("Utilisateur non connecté"));
            return;
        }

        executorService.execute(() -> {
            try {
                runOnMainThread(() -> callback.onProgress(20));

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageData = stream.toByteArray();
                stream.close();

                runOnMainThread(() -> callback.onProgress(40));

                ImageProcessor.ProcessedImage processed = imageProcessor.processImageData(
                        imageData, ImageProcessor.Quality.MEDIUM
                );

                runOnMainThread(() -> callback.onProgress(60));

                // Paths corrigés pour la caméra
                String timestamp = String.valueOf(System.currentTimeMillis());
                String imagePath = userId + "/" + (folderId != null ? folderId : "root") + "/camera_" + timestamp + ".jpg";
                String imageUrl = storageManager.uploadToStorage(
                        GedConfig.BUCKET_DOCUMENTS, imagePath, processed.imageData, "image/jpeg"
                );

                runOnMainThread(() -> callback.onProgress(70));

                String thumbPath = userId + "/thumbnails/camera_thumb_" + timestamp + ".jpg";
                String thumbUrl = storageManager.uploadToStorage(
                        GedConfig.BUCKET_THUMBNAILS, thumbPath, processed.thumbnailData, "image/jpeg"
                );

                runOnMainThread(() -> callback.onProgress(80));

                String ocrText = null;
                Float ocrConfidence = null;
                if (GedConfig.OCR_ENABLED) {
                    OcrProcessor.OcrResult ocrResult = ocrProcessor.processImage(processed.imageData);
                    if (ocrResult != null && ocrResult.confidence >= GedConfig.MIN_OCR_CONFIDENCE) {
                        ocrText = ocrResult.text;
                        ocrConfidence = ocrResult.confidence;
                    }
                }

                runOnMainThread(() -> callback.onProgress(90));

                GedDocument document = new GedDocument("Camera_" + timestamp + ".jpg", "image", folderId);
                document.setUserId(userId);
                document.setMimeType("image/jpeg");
                document.setSize(processed.imageData.length);
                document.setContentUrl(imageUrl);
                document.setThumbnailUrl(thumbUrl);
                document.setOcrText(ocrText);
                document.setOcrConfidence(ocrConfidence);
                document.setSynced(true);

                List<String> autoTags = new ArrayList<>();
                if (ocrText != null) {
                    autoTags = generateAutoTags(ocrText);
                }

                createDocumentInDatabase(document, autoTags, callback);

            } catch (Exception e) {
                Log.e(TAG, "Erreur traitement camera", e);
                runOnMainThread(() -> callback.onError("Erreur traitement photo: " + e.getMessage()));
            }
        });
    }

    // CORRECTION: Méthode modifiée pour gérer les tags séparément
    private void createDocumentInDatabase(GedDocument document, List<String> autoTags, ProgressCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                document.setUserId(userId);
                document.setCreatedAt(new Date());
                document.setUpdatedAt(new Date());
                document.setSynced(true);

                supabaseService.createDocument(document).enqueue(new Callback<List<GedDocument>>() {
                    @Override
                    public void onResponse(Call<List<GedDocument>> call, Response<List<GedDocument>> response) {
                        runOnMainThread(() -> {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                GedDocument createdDoc = response.body().get(0);
                                Log.d(TAG, "Document créé: " + createdDoc.getName());

                                // Gérer les tags après la création du document
                                if (autoTags != null && !autoTags.isEmpty()) {
                                    createDocumentTags(createdDoc.getId(), autoTags);
                                }

                                updateParentFolderCount(createdDoc.getFolderId(), 0, createdDoc.getSize());
                                callback.onComplete("Document créé avec succès");
                            } else {
                                handleError(response, callback, "Erreur création document");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<GedDocument>> call, Throwable t) {
                        Log.e(TAG, "Erreur réseau création document", t);
                        runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    // NOUVEAU: Méthode pour créer les tags d'un document
    private void createDocumentTags(String documentId, List<String> tagNames) {
        // Cette méthode pourra être implémentée plus tard pour gérer les tags
        // Pour l'instant, on log juste les tags qui auraient dû être créés
        Log.d(TAG, "Tags à créer pour le document " + documentId + ": " + tagNames.toString());
    }

    private void handleError(Response<?> response, ProgressCallback callback, String baseMessage) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
            try {
                JSONObject errorJson = new JSONObject(errorBody);
                String errorDetails = "Code: " + response.code() +
                        " | Message: " + errorJson.optString("message") +
                        " | Details: " + errorJson.optString("details");
                callback.onError(baseMessage + " - " + errorDetails);
            } catch (JSONException jsonException) {
                callback.onError(baseMessage + ": " + response.code() +
                        " - Raw error: " + errorBody);
            }
        } catch (IOException ioException) {
            callback.onError(baseMessage + ": " + response.code() +
                    " - Failed to read error body: " + ioException.getMessage());
        }
    }

    // ==================== AUTRES MÉTHODES ====================

    public void updateFolder(String folderId, Map<String, Object> updates, OperationCallback callback) {
        updates.put("updated_at", new Date());

        supabaseService.updateFolder("eq." + folderId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnMainThread(() -> {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Dossier mis à jour");
                    } else {
                        callback.onError("Erreur mise à jour: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
            }
        });
    }

    public void deleteFolder(String folderId, OperationCallback callback) {
        supabaseService.deleteFolder("eq." + folderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnMainThread(() -> {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Dossier supprimé");
                    } else {
                        callback.onError("Erreur suppression: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
            }
        });
    }

    public void updateDocument(String documentId, Map<String, Object> updates, OperationCallback callback) {
        updates.put("updated_at", new Date());

        supabaseService.updateDocument("eq." + documentId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnMainThread(() -> {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Document mis à jour");
                    } else {
                        callback.onError("Erreur mise à jour: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
            }
        });
    }

    public void deleteDocument(String documentId, OperationCallback callback) {
        supabaseService.deleteDocument("eq." + documentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnMainThread(() -> {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Document supprimé");
                    } else {
                        callback.onError("Erreur suppression: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
            }
        });
    }

    public void openDocument(Context context, GedDocument document, OperationCallback callback) {
        try {
            if (document.getContentUrl() != null && !document.getContentUrl().isEmpty()) {
                Uri uri = Uri.parse(document.getContentUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, document.getMimeType());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(intent);
                runOnMainThread(() -> callback.onSuccess("Document ouvert"));
            } else {
                runOnMainThread(() -> callback.onError("URL du document non disponible"));
            }
        } catch (Exception e) {
            runOnMainThread(() -> callback.onError("Erreur ouverture document: " + e.getMessage()));
        }
    }

    public void searchDocuments(String query, SearchCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                Map<String, Object> searchParams = new HashMap<>();
                searchParams.put("search_query", query);
                searchParams.put("user_uuid", userId);
                searchParams.put("limit_count", 50);

                supabaseService.searchDocuments(searchParams).enqueue(new Callback<List<GedDocument>>() {
                    @Override
                    public void onResponse(Call<List<GedDocument>> call, Response<List<GedDocument>> response) {
                        runOnMainThread(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                callback.onSuccess(new ArrayList<>(), response.body());
                            } else {
                                callback.onError("Erreur recherche: " + response.code());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<GedDocument>> call, Throwable t) {
                        runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    public void getStorageStats(StorageStatsCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);

                supabaseService.getStorageStats(params).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        runOnMainThread(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                Map<String, Object> stats = response.body();
                                try {
                                    int count = ((Number) stats.get("count")).intValue();
                                    long totalSize = ((Number) stats.get("total_size")).longValue();
                                    callback.onSuccess(count, totalSize);
                                } catch (Exception e) {
                                    callback.onError("Format de réponse invalide");
                                }
                            } else {
                                callback.onError("Erreur: " + response.code());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    // ==================== UTILITY METHODS ====================

    private String getCurrentUserId() {
        String userId = tokenManager.getCurrentUserId();
        Log.d(TAG, "getCurrentUserId() retourne: " + userId);

        // Vérifier que c'est un UUID valide (format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)
        if (userId != null && !userId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            Log.w(TAG, "User ID ne semble pas être un UUID valide: " + userId);
        }

        return userId;
    }

    private void executeWithValidToken(TokenOperation operation) {
        tokenManager.getValidAccessToken(new TokenManager.TokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                String userId = AuthUtils.extractUserIdFromJWT(accessToken);
                if (userId != null) {
                    operation.execute(userId);
                } else {
                    operation.onError("Erreur token - Impossible d'extraire l'UID. Token: " +
                            (accessToken != null ? accessToken.substring(0, 10) + "..." : "null"));
                }
            }

            @Override
            public void onError(String errorMessage) {
                operation.onError(errorMessage);
            }
        });
    }

    private interface TokenOperation {
        void execute(String userId);
        void onError(String errorMessage);
    }

    private void updateParentFolderCount(String folderId, int subfolderDelta, long sizeDelta) {
        Map<String, Object> updates = new HashMap<>();
        if (subfolderDelta != 0) {
            updates.put("subfolder_count", "subfolder_count + " + subfolderDelta);
        }
        if (sizeDelta != 0) {
            updates.put("total_size", "total_size + " + sizeDelta);
            updates.put("document_count", "document_count + 1");
        }
        updates.put("updated_at", new Date());

        updateFolder(folderId, updates, new OperationCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Parent folder updated");
            }

            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "Failed to update parent folder: " + errorMessage);
            }
        });
    }

    private List<String> generateAutoTags(String text) {
        return new ArrayList<>(); // Placeholder
    }

    public void saveScanHistory(ScanHistory history, OperationCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                history.setUserId(userId);
                history.setCreatedAt(new Date());

                supabaseService.createScanHistory(history).enqueue(new Callback<List<ScanHistory>>() {
                    @Override
                    public void onResponse(Call<List<ScanHistory>> call, Response<List<ScanHistory>> response) {
                        runOnMainThread(() -> {
                            if (response.isSuccessful()) {
                                callback.onSuccess("Historique de scan enregistré");
                            } else {
                                String errorMsg = "Erreur création historique: ";
                                try {
                                    errorMsg += response.errorBody() != null ?
                                            response.errorBody().string() :
                                            "Code " + response.code();
                                } catch (IOException e) {
                                    errorMsg += "Erreur lecture réponse";
                                }
                                callback.onError(errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<ScanHistory>> call, Throwable t) {
                        Log.e(TAG, "Erreur réseau historique scan", t);
                        runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    public void saveUserPreferences(UserPreferences prefs, OperationCallback callback) {
        executeWithValidToken(new TokenOperation() {
            @Override
            public void execute(String userId) {
                prefs.setUserId(userId);
                prefs.setUpdatedAt(new Date());

                supabaseService.createUserPreferences(prefs).enqueue(new Callback<List<UserPreferences>>() {
                    @Override
                    public void onResponse(Call<List<UserPreferences>> call, Response<List<UserPreferences>> response) {
                        runOnMainThread(() -> {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                callback.onSuccess("Préférences sauvegardées");
                            } else {
                                String errorMsg = "Erreur sauvegarde préférences: ";
                                if (response.errorBody() != null) {
                                    try {
                                        JSONObject errorJson = new JSONObject(response.errorBody().string());
                                        errorMsg += errorJson.optString("message", "Erreur inconnue");
                                    } catch (Exception e) {
                                        errorMsg += response.code();
                                    }
                                } else {
                                    errorMsg += response.code();
                                }
                                callback.onError(errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<UserPreferences>> call, Throwable t) {
                        Log.e(TAG, "Erreur réseau sauvegarde préférences", t);
                        runOnMainThread(() -> callback.onError("Erreur réseau: " + t.getMessage()));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> callback.onError(errorMessage));
            }
        });
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (storageManager != null) {
            storageManager.cleanup();
        }
    }
}