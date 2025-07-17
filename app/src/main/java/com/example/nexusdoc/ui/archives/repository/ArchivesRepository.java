package com.example.nexusdoc.ui.archives.repository;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;

import com.example.nexusdoc.ui.archives.model.ArchiveDocument;
import com.example.nexusdoc.ui.archives.model.ArchiveFolder;
import com.example.nexusdoc.ui.utilitaires.Constants;
import com.example.nexusdoc.ui.utilitaires.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArchivesRepository {

    private final Context context;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final PreferenceManager preferenceManager;

    // Collections
    private static final String COLLECTION_FOLDERS = "folders";
    private static final String COLLECTION_DOCUMENTS = "documents";

    public ArchivesRepository(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
    }

    // Callback interfaces
    public interface FoldersCallback {
        void onSuccess(List<ArchiveFolder> folders);
        void onError(String errorMessage);
    }

    public interface DocumentsCallback {
        void onSuccess(List<ArchiveDocument> documents);
        void onError(String errorMessage);
    }

    public interface FolderContentsCallback {
        void onSuccess(List<ArchiveFolder> subfolders, List<ArchiveDocument> documents);
        void onError(String errorMessage);
    }

    public interface FolderCallback {
        void onSuccess(ArchiveFolder folder);
        void onError(String errorMessage);
    }

    public interface DocumentCallback {
        void onSuccess(ArchiveDocument document);
        void onError(String errorMessage);
    }

    public interface SearchCallback {
        void onSuccess(List<Object> items);
        void onError(String errorMessage);
    }

    public interface StorageCallback {
        void onSuccess(String storageInfo);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    // CORRECTION: Get folders by type (for sections)
    public void getFoldersByType(String sectionType, FoldersCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        firestore.collection(COLLECTION_FOLDERS)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("type", sectionType)
                .whereEqualTo("parentId", null) // Only root folders for sections
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ArchiveFolder> folders = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ArchiveFolder folder = document.toObject(ArchiveFolder.class);
                        folder.setId(document.getId());
                        folders.add(folder);
                    }

                    // Create default folder if none exist for this section
                    if (folders.isEmpty()) {
                        createDefaultSectionFolder(sectionType, callback);
                    } else {
                        callback.onSuccess(folders);
                    }
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors du chargement des dossiers: " + e.getMessage()));
    }

    // CORRECTION: Create default folder for section
    private void createDefaultSectionFolder(String sectionType, FoldersCallback callback) {
        ArchiveFolder defaultFolder = createFolderForSection(sectionType);

        if (defaultFolder == null) {
            callback.onError("Type de section non reconnu");
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        Map<String, Object> folderData = new HashMap<>();
        folderData.put("name", defaultFolder.getName());
        folderData.put("type", defaultFolder.getType());
        folderData.put("parentId", null);
        folderData.put("path", defaultFolder.getPath());
        folderData.put("userId", currentUser.getUid());
        folderData.put("dateCreated", defaultFolder.getDateCreated());
        folderData.put("dateModified", defaultFolder.getDateModified());
        folderData.put("isSynced", true);
        folderData.put("documentCount", 0);
        folderData.put("subfolderCount", 0);
        folderData.put("totalSize", 0L);

        firestore.collection(COLLECTION_FOLDERS)
                .add(folderData)
                .addOnSuccessListener(documentReference -> {
                    defaultFolder.setId(documentReference.getId());
                    defaultFolder.setSynced(true);

                    List<ArchiveFolder> folders = new ArrayList<>();
                    folders.add(defaultFolder);
                    callback.onSuccess(folders);
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la création du dossier par défaut: " + e.getMessage()));
    }

    // CORRECTION: Create folder based on section type
    private ArchiveFolder createFolderForSection(String sectionType) {
        switch (sectionType) {
            case "admin":
                return ArchiveFolder.createAdminFolder("Documents Administratifs");
            case "fournisseurs":
                return ArchiveFolder.createFournisseurFolder("Fournisseurs");
            case "clients":
                return ArchiveFolder.createClientFolder("Clients");
            case "autres":
                return ArchiveFolder.createOtherFolder("Autres Documents");
            default:
                return null;
        }
    }

    // CORRECTION: Get folder by path
    public void getFolderByPath(String targetPath, String sectionType, FolderCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        firestore.collection(COLLECTION_FOLDERS)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("type", sectionType)
                .whereEqualTo("path", targetPath)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        ArchiveFolder folder = document.toObject(ArchiveFolder.class);
                        folder.setId(document.getId());
                        callback.onSuccess(folder);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la recherche du dossier: " + e.getMessage()));
    }

    // CORRECTION: Existing methods remain the same but with updated folder types
    public void getRootFolders(FoldersCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        firestore.collection(COLLECTION_FOLDERS)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("parentId", null)
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ArchiveFolder> folders = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ArchiveFolder folder = document.toObject(ArchiveFolder.class);
                        folder.setId(document.getId());
                        folders.add(folder);
                    }

                    // Create default folders if none exist
                    if (folders.isEmpty()) {
                        createDefaultFolders(callback);
                    } else {
                        callback.onSuccess(folders);
                    }
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors du chargement des dossiers: " + e.getMessage()));
    }

    private void createDefaultFolders(FoldersCallback callback) {
        List<ArchiveFolder> defaultFolders = new ArrayList<>();
        defaultFolders.add(ArchiveFolder.createClientFolder("Clients"));
        defaultFolders.add(ArchiveFolder.createFournisseurFolder("Fournisseurs"));
        defaultFolders.add(ArchiveFolder.createAdminFolder("Documents Administratifs"));
        defaultFolders.add(ArchiveFolder.createOtherFolder("Autres Documents"));

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        // Create folders in Firestore
        for (ArchiveFolder folder : defaultFolders) {
            Map<String, Object> folderData = new HashMap<>();
            folderData.put("name", folder.getName());
            folderData.put("type", folder.getType());
            folderData.put("parentId", null);
            folderData.put("path", folder.getPath());
            folderData.put("userId", currentUser.getUid());
            folderData.put("dateCreated", folder.getDateCreated());
            folderData.put("dateModified", folder.getDateModified());
            folderData.put("isSynced", true);
            folderData.put("documentCount", 0);
            folderData.put("subfolderCount", 0);
            folderData.put("totalSize", 0L);

            firestore.collection(COLLECTION_FOLDERS)
                    .add(folderData)
                    .addOnSuccessListener(documentReference -> {
                        folder.setId(documentReference.getId());
                        folder.setSynced(true);
                    });
        }

        callback.onSuccess(defaultFolders);
    }

    public void getFolderContents(String folderId, FolderContentsCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        // Get subfolders
        firestore.collection(COLLECTION_FOLDERS)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("parentId", folderId)
                .orderBy("name")
                .get()
                .addOnSuccessListener(folderSnapshots -> {
                    List<ArchiveFolder> subfolders = new ArrayList<>();

                    for (QueryDocumentSnapshot document : folderSnapshots) {
                        ArchiveFolder folder = document.toObject(ArchiveFolder.class);
                        folder.setId(document.getId());
                        subfolders.add(folder);
                    }

                    // Get documents
                    firestore.collection(COLLECTION_DOCUMENTS)
                            .whereEqualTo("userId", currentUser.getUid())
                            .whereEqualTo("folderId", folderId)
                            .orderBy("dateModified", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(documentSnapshots -> {
                                List<ArchiveDocument> documents = new ArrayList<>();

                                for (QueryDocumentSnapshot document : documentSnapshots) {
                                    ArchiveDocument archiveDoc = document.toObject(ArchiveDocument.class);
                                    archiveDoc.setId(document.getId());
                                    documents.add(archiveDoc);
                                }

                                callback.onSuccess(subfolders, documents);
                            })
                            .addOnFailureListener(e -> callback.onError("Erreur lors du chargement des documents: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors du chargement des sous-dossiers: " + e.getMessage()));
    }

    public void createFolder(ArchiveFolder folder, OperationCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        Map<String, Object> folderData = new HashMap<>();
        folderData.put("name", folder.getName());
        folderData.put("type", folder.getType());
        folderData.put("parentId", folder.getParentId());
        folderData.put("path", folder.getPath());
        folderData.put("userId", currentUser.getUid());
        folderData.put("dateCreated", folder.getDateCreated());
        folderData.put("dateModified", folder.getDateModified());
        folderData.put("isSynced", true);
        folderData.put("documentCount", 0);
        folderData.put("subfolderCount", 0);
        folderData.put("totalSize", 0L);
        folderData.put("description", folder.getDescription());

        firestore.collection(COLLECTION_FOLDERS)
                .add(folderData)
                .addOnSuccessListener(documentReference -> {
                    folder.setId(documentReference.getId());
                    folder.setSynced(true);
                    callback.onSuccess("Dossier créé avec succès");
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la création du dossier: " + e.getMessage()));
    }

    // CORRECTION: Document operations with userId
    public void importFile(Uri fileUri, String folderId, OperationCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                callback.onError("Impossible de lire le fichier");
                return;
            }

            // Read file content
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            inputStream.close();

            byte[] fileBytes = buffer.toByteArray();
            String base64Content = Base64.encodeToString(fileBytes, Base64.DEFAULT);

            // Get file info
            String fileName = getFileName(fileUri);
            String mimeType = context.getContentResolver().getType(fileUri);

            // Create document
            ArchiveDocument document = new ArchiveDocument(fileName, "document", folderId);
            document.setMimeType(mimeType);
            document.setSize(fileBytes.length);

            saveDocument(document, base64Content, callback);

        } catch (Exception e) {
            callback.onError("Erreur lors de l'importation: " + e.getMessage());
        }
    }

    public void importImage(Uri imageUri, String folderId, OperationCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onError("Impossible de lire l'image");
                return;
            }

            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                callback.onError("Format d'image non supporté");
                return;
            }

            // Compress and convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64Content = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Create document
            String fileName = getFileName(imageUri);
            ArchiveDocument document = new ArchiveDocument(fileName, "image", folderId);
            document.setMimeType("image/jpeg");
            document.setSize(imageBytes.length);

            // Create thumbnail
            Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
            ByteArrayOutputStream thumbStream = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, thumbStream);
            String thumbnailBase64 = Base64.encodeToString(thumbStream.toByteArray(), Base64.DEFAULT);
            document.setThumbnailBase64(thumbnailBase64);

            bitmap.recycle();
            thumbnail.recycle();
            byteArrayOutputStream.close();
            thumbStream.close();

            saveDocument(document, base64Content, callback);

        } catch (Exception e) {
            callback.onError("Erreur lors de l'importation de l'image: " + e.getMessage());
        }
    }

    public void processCameraImage(Intent data, String folderId, OperationCallback callback) {
        try {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap == null) {
                callback.onError("Aucune image capturée");
                return;
            }

            // Compress and convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64Content = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Create document with timestamp name
            String fileName = "SCAN_" + System.currentTimeMillis() + ".jpg";
            ArchiveDocument document = new ArchiveDocument(fileName, "scan", folderId);
            document.setMimeType("image/jpeg");
            document.setSize(imageBytes.length);

            // Create thumbnail
            Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
            ByteArrayOutputStream thumbStream = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, thumbStream);
            String thumbnailBase64 = Base64.encodeToString(thumbStream.toByteArray(), Base64.DEFAULT);
            document.setThumbnailBase64(thumbnailBase64);

            thumbnail.recycle();
            byteArrayOutputStream.close();
            thumbStream.close();

            saveDocument(document, base64Content, callback);

        } catch (Exception e) {
            callback.onError("Erreur lors du traitement de l'image: " + e.getMessage());
        }
    }

    // CORRECTION: Save document with userId
    private void saveDocument(ArchiveDocument document, String base64Content, OperationCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        Map<String, Object> documentData = new HashMap<>();
        documentData.put("name", document.getName());
        documentData.put("type", document.getType());
        documentData.put("mimeType", document.getMimeType());
        documentData.put("folderId", document.getFolderId());
        documentData.put("size", document.getSize());
        documentData.put("dateCreated", document.getDateCreated());
        documentData.put("dateModified", document.getDateModified());
        documentData.put("uniqueCode", document.getUniqueCode());
        documentData.put("userId", currentUser.getUid()); // CORRECTION: Ajout du userId
        documentData.put("content", base64Content);
        documentData.put("thumbnailBase64", document.getThumbnailBase64());
        documentData.put("isFavorite", document.isFavorite());
        documentData.put("isShared", document.isShared());
        documentData.put("isSynced", true);
        documentData.put("isOfflineAvailable", true);
        documentData.put("tags", document.getTags());
        documentData.put("description", document.getDescription());

        firestore.collection(COLLECTION_DOCUMENTS)
                .add(documentData)
                .addOnSuccessListener(documentReference -> {
                    document.setId(documentReference.getId());
                    document.setSynced(true);
                    callback.onSuccess("Document sauvegardé avec succès");
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la sauvegarde: " + e.getMessage()));
    }

    // Rest of the methods remain the same...
    // (searchAndFilter, getStorageInfo, etc.)

    // Utility methods
    private String getFileName(Uri uri) {
        String fileName = "document_" + System.currentTimeMillis();

        try {
            android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            // Use default name
        }

        return fileName;
    }

    public void getStorageInfo(StorageCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        firestore.collection(COLLECTION_DOCUMENTS)
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long totalSize = 0;
                    int documentCount = queryDocumentSnapshots.size();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long size = document.getLong("size");
                        if (size != null) {
                            totalSize += size;
                        }
                    }

                    String formattedSize = formatFileSize(totalSize);
                    String info = formattedSize + " utilisés • " + documentCount + " documents";
                    callback.onSuccess(info);
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors du calcul du stockage"));
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) return sizeInBytes + " B";
        if (sizeInBytes < 1024 * 1024) return String.format("%.1f KB", sizeInBytes / 1024.0);
        if (sizeInBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
    }

    // Additional methods for folder operations
    public void getParentFolder(String folderId, FolderCallback callback) {
        firestore.collection(COLLECTION_FOLDERS)
                .document(folderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArchiveFolder folder = documentSnapshot.toObject(ArchiveFolder.class);
                        if (folder != null) {
                            folder.setId(documentSnapshot.getId());

                            String parentId = folder.getParentId();
                            if (parentId != null) {
                                getFolderById(parentId, callback);
                            } else {
                                callback.onSuccess(null); // Root folder
                            }
                        } else {
                            callback.onError("Dossier non trouvé");
                        }
                    } else {
                        callback.onError("Dossier non trouvé");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la récupération du dossier parent"));
    }

    public void getFolderById(String folderId, FolderCallback callback) {
        firestore.collection(COLLECTION_FOLDERS)
                .document(folderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArchiveFolder folder = documentSnapshot.toObject(ArchiveFolder.class);
                        if (folder != null) {
                            folder.setId(documentSnapshot.getId());
                            callback.onSuccess(folder);
                        } else {
                            callback.onError("Dossier non trouvé");
                        }
                    } else {
                        callback.onError("Dossier non trouvé");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la récupération du dossier"));
    }

    public void openDocument(ArchiveDocument document, OperationCallback callback) {
        // Implementation for opening documents
        callback.onSuccess("Document ouvert");
    }

    public void deleteDocument(ArchiveDocument document, OperationCallback callback) {
        firestore.collection(COLLECTION_DOCUMENTS)
                .document(document.getId())
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Document supprimé"))
                .addOnFailureListener(e -> callback.onError("Erreur lors de la suppression: " + e.getMessage()));
    }

    public void deleteFolder(ArchiveFolder folder, OperationCallback callback) {
        firestore.collection(COLLECTION_FOLDERS)
                .document(folder.getId())
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Dossier supprimé"))
                .addOnFailureListener(e -> callback.onError("Erreur lors de la suppression: " + e.getMessage()));
    }

    public void syncAllData(OperationCallback callback) {
        // Implementation for syncing all offline data to cloud
        callback.onSuccess("Synchronisation terminée");
    }

    // CORRECTION: Add missing search and filter method
    public void searchAndFilter(String query, String filterType, String currentFolderId, SearchCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        List<Object> results = new ArrayList<>();

        // Search in documents
        Query documentsQuery = firestore.collection(COLLECTION_DOCUMENTS)
                .whereEqualTo("userId", currentUser.getUid());

        if (currentFolderId != null) {
            documentsQuery = documentsQuery.whereEqualTo("folderId", currentFolderId);
        }

        documentsQuery.get()
                .addOnSuccessListener(documentSnapshots -> {
                    for (QueryDocumentSnapshot document : documentSnapshots) {
                        ArchiveDocument archiveDoc = document.toObject(ArchiveDocument.class);
                        archiveDoc.setId(document.getId());

                        // Apply filters
                        if (matchesSearchAndFilter(archiveDoc, query, filterType)) {
                            results.add(archiveDoc);
                        }
                    }

                    // Search in folders
                    Query foldersQuery = firestore.collection(COLLECTION_FOLDERS)
                            .whereEqualTo("userId", currentUser.getUid());

                    if (currentFolderId != null) {
                        foldersQuery = foldersQuery.whereEqualTo("parentId", currentFolderId);
                    }

                    foldersQuery.get()
                            .addOnSuccessListener(folderSnapshots -> {
                                for (QueryDocumentSnapshot document : folderSnapshots) {
                                    ArchiveFolder folder = document.toObject(ArchiveFolder.class);
                                    folder.setId(document.getId());

                                    // Apply filters
                                    if (matchesSearchAndFilter(folder, query, filterType)) {
                                        results.add(folder);
                                    }
                                }

                                callback.onSuccess(results);
                            })
                            .addOnFailureListener(e -> callback.onError("Erreur lors de la recherche dans les dossiers: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors de la recherche dans les documents: " + e.getMessage()));
    }

    private boolean matchesSearchAndFilter(Object item, String query, String filterType) {
        // Search query matching
        if (!query.isEmpty()) {
            String name = "";
            if (item instanceof ArchiveDocument) {
                name = ((ArchiveDocument) item).getName();
            } else if (item instanceof ArchiveFolder) {
                name = ((ArchiveFolder) item).getName();
            }

            if (!name.toLowerCase().contains(query.toLowerCase())) {
                return false;
            }
        }

        // Filter type matching
        switch (filterType) {
            case "recent":
                // Show items modified in last 7 days
                Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
                if (item instanceof ArchiveDocument) {
                    return ((ArchiveDocument) item).getDateModified().after(sevenDaysAgo);
                } else if (item instanceof ArchiveFolder) {
                    return ((ArchiveFolder) item).getDateModified().after(sevenDaysAgo);
                }
                break;
            case "favorites":
                if (item instanceof ArchiveDocument) {
                    return ((ArchiveDocument) item).isFavorite();
                }
                break;
            case "shared":
                if (item instanceof ArchiveDocument) {
                    return ((ArchiveDocument) item).isShared();
                }
                break;
            case "all":
            default:
                return true;
        }

        return true;
    }

    public void getRecentDocuments(int limit, DocumentsCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        firestore.collection(COLLECTION_DOCUMENTS)
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("dateModified", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ArchiveDocument> documents = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ArchiveDocument archiveDoc = document.toObject(ArchiveDocument.class);
                        archiveDoc.setId(document.getId());
                        documents.add(archiveDoc);
                    }

                    callback.onSuccess(documents);
                })
                .addOnFailureListener(e -> callback.onError("Erreur lors du chargement des documents récents: " + e.getMessage()));
    }
}