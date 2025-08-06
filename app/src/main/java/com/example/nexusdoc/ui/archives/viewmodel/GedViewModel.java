package com.example.nexusdoc.ui.archives.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.archives.model.GedDocument;
import com.example.nexusdoc.ui.archives.model.GedFolder;
import com.example.nexusdoc.ui.archives.repository.GedRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GedViewModel extends AndroidViewModel {
    private final GedRepository repository;

    // LiveData pour l'état global
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    // LiveData pour les sections
    private final MutableLiveData<Map<String, Boolean>> sectionsExpansionState = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<String>>> sectionsPaths = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<GedFolder>>> sectionsFolders = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<GedDocument>>> sectionsDocuments = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> sectionsDocumentCounts = new MutableLiveData<>();

    // LiveData pour la recherche et filtres
    private final MutableLiveData<List<Object>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<String> currentSearchQuery = new MutableLiveData<>();
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>();

    // LiveData pour les statistiques
    private final MutableLiveData<String> storageInfo = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Object>> userStats = new MutableLiveData<>();

    // État interne
    private final Map<String, String> sectionCurrentFolderIds = new HashMap<>();
    private final Map<String, Boolean> sectionExpansionStates = new HashMap<>();
    private String currentActiveSectionId = null;

    public GedViewModel(@NonNull Application application) {
        super(application);
        this.repository = new GedRepository(application.getApplicationContext());

        initializeViewModel();
    }

    private void initializeViewModel() {
        // Initialiser les états par défaut
        loading.postValue(false);
        currentSearchQuery.postValue("");
        currentFilter.postValue("all");

        // Initialiser les sections
        Map<String, Boolean> initialExpansion = new HashMap<>();
        initialExpansion.put("admin", false);
        initialExpansion.put("fournisseurs", false);
        initialExpansion.put("clients", false);
        initialExpansion.put("autres", false);

        sectionExpansionStates.putAll(initialExpansion);
        sectionsExpansionState.postValue(new HashMap<>(initialExpansion));

        // Initialiser les autres maps
        sectionsPaths.postValue(new HashMap<>());
        sectionsFolders.postValue(new HashMap<>());
        sectionsDocuments.postValue(new HashMap<>());
        sectionsDocumentCounts.postValue(new HashMap<>());

        // Charger les statistiques initiales
        loadUserStats();
    }

    // Getters pour LiveData
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getOperationSuccess() { return operationSuccess; }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public LiveData<Map<String, Boolean>> getSectionsExpansionState() { return sectionsExpansionState; }
    public LiveData<Map<String, List<String>>> getSectionsPaths() { return sectionsPaths; }
    public LiveData<Map<String, List<GedFolder>>> getSectionsFolders() { return sectionsFolders; }
    public LiveData<Map<String, List<GedDocument>>> getSectionsDocuments() { return sectionsDocuments; }
    public LiveData<Map<String, Integer>> getSectionsDocumentCounts() { return sectionsDocumentCounts; }

    public LiveData<List<Object>> getSearchResults() { return searchResults; }
    public LiveData<String> getCurrentSearchQuery() { return currentSearchQuery; }
    public LiveData<String> getCurrentFilter() { return currentFilter; }

    public LiveData<String> getStorageInfo() { return storageInfo; }
    public LiveData<Map<String, Object>> getUserStats() { return userStats; }

    // Méthodes pour la gestion des sections
    public void toggleSectionExpansion(String sectionId) {
        Boolean currentState = sectionExpansionStates.get(sectionId);
        boolean newState = currentState == null || !currentState;

        sectionExpansionStates.put(sectionId, newState);
        sectionsExpansionState.postValue(new HashMap<>(sectionExpansionStates));

        if (newState) {
            loadSectionData(sectionId);
        }
    }

    public void loadSectionData(String sectionId) {
        loading.postValue(true);
        statusMessage.postValue("Chargement de la section " + getSectionDisplayName(sectionId) + "...");
        currentActiveSectionId = sectionId;

        repository.getFoldersByType(sectionId, new GedRepository.FoldersCallback() {
            @Override
            public void onSuccess(List<GedFolder> folders) {
                loading.postValue(false);
                statusMessage.postValue(null);

                // Mettre à jour les données de la section
                updateSectionFolders(sectionId, folders);

                // Calculer le nombre total de documents
                int totalDocs = 0;
                for (GedFolder folder : folders) {
                    totalDocs += folder.getDocumentCount();
                }
                updateSectionDocumentCount(sectionId, totalDocs);

                loadUserStats();
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur chargement section: " + errorMessage);
            }
        });
    }

    public void navigateToSectionFolder(String sectionId, GedFolder folder) {
        if (folder == null || folder.getId() == null) {
            error.postValue("Dossier invalide");
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Navigation vers " + folder.getName() + "...");
        currentActiveSectionId = sectionId;
        sectionCurrentFolderIds.put(sectionId, folder.getId());

        // Mettre à jour le chemin de navigation
        updateSectionPath(sectionId, folder.getPath());

        repository.getFolderContents(folder.getId(), new GedRepository.FolderContentsCallback() {
            @Override
            public void onSuccess(List<GedFolder> subfolders, List<GedDocument> documents) {
                loading.postValue(false);
                statusMessage.postValue(null);

                updateSectionFolders(sectionId, subfolders != null ? subfolders : new ArrayList<>());
                updateSectionDocuments(sectionId, documents != null ? documents : new ArrayList<>());

                /*if (subfolders == null) subfolders = new ArrayList<>();
                if (documents == null) documents = new ArrayList<>();

                if (subfolders.isEmpty() && documents.isEmpty()) {
                    // Dossier vide - afficher l'état vide
                    updateSectionFolders(sectionId, new ArrayList<>());
                    updateSectionDocuments(sectionId, new ArrayList<>());
                } else if (!subfolders.isEmpty()) {
                    // Afficher les sous-dossiers
                    updateSectionFolders(sectionId, subfolders);
                    updateSectionDocuments(sectionId, new ArrayList<>());
                } else {
                    // Afficher les documents
                    updateSectionFolders(sectionId, new ArrayList<>());
                    updateSectionDocuments(sectionId, documents);
                }*/
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur navigation: " + errorMessage);

                // Revenir à la vue précédente en cas d'erreur
                sectionCurrentFolderIds.remove(sectionId);
                loadSectionData(sectionId);
            }
        });
    }

    public void navigateBackInSection(String sectionId, int pathIndex) {
        if (pathIndex == 0) {
            navigateToSectionRoot(sectionId);
        } else {
            Map<String, List<String>> currentPaths = sectionsPaths.getValue();
            if (currentPaths != null && currentPaths.containsKey(sectionId)) {
                List<String> sectionPath = currentPaths.get(sectionId);
                if (sectionPath != null && pathIndex < sectionPath.size()) {
                    String targetPath = "/" + String.join("/", sectionPath.subList(0, pathIndex + 1));
                    findAndNavigateToPath(sectionId, targetPath);
                }
            }
        }
    }

    public void navigateToSectionRoot(String sectionId) {
        sectionCurrentFolderIds.remove(sectionId);
        updateSectionPath(sectionId, "/");
        loadSectionData(sectionId);
    }

    private void findAndNavigateToPath(String sectionId, String targetPath) {
        repository.getFolderByPath(targetPath, sectionId, new GedRepository.FolderCallback() {
            @Override
            public void onSuccess(GedFolder folder) {
                if (folder != null) {
                    navigateToSectionFolder(sectionId, folder);
                } else {
                    navigateToSectionRoot(sectionId);
                }
            }

            @Override
            public void onError(String errorMessage) {
                navigateToSectionRoot(sectionId);
            }
        });
    }

    // Méthodes CRUD pour les dossiers
    public void createFolderInSection(String sectionId, String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            error.postValue("Le nom du dossier ne peut pas être vide");
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Création du dossier...");

        String parentFolderId = sectionCurrentFolderIds.get(sectionId);
        GedFolder newFolder = new GedFolder(folderName.trim(), sectionId, parentFolderId);

        repository.createFolder(newFolder, new GedRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Dossier créé avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur création dossier: " + errorMessage);
            }
        });
    }

    public void renameFolderInSection(String sectionId, GedFolder folder, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            error.postValue("Le nom ne peut pas être vide");
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Renommage en cours...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName.trim());

        repository.updateFolder(folder.getId(), updates, new GedRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Dossier renommé avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur renommage: " + errorMessage);
            }
        });
    }

    public void deleteFolderInSection(String sectionId, GedFolder folder) {
        loading.postValue(true);
        statusMessage.postValue("Suppression en cours...");

        repository.deleteFolder(folder.getId(), new GedRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Dossier supprimé avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur suppression: " + errorMessage);
            }
        });
    }

    // Méthodes CRUD pour les documents
    public void renameDocumentInSection(String sectionId, GedDocument document, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            error.postValue("Le nom ne peut pas être vide");
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Renommage du document...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName.trim());

        repository.updateDocument(document.getId(), updates, new GedRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Document renommé avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur renommage document: " + errorMessage);
            }
        });
    }

    public void deleteDocumentInSection(String sectionId, GedDocument document) {
        loading.postValue(true);
        statusMessage.postValue("Suppression du document...");

        repository.deleteDocument(document.getId(), new GedRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Document supprimé avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur suppression document: " + errorMessage);
            }
        });
    }

    public void toggleFavoriteInSection(String sectionId, GedDocument document) {
        loading.postValue(true);
        statusMessage.postValue("Mise à jour des favoris...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("is_favorite", !document.isFavorite());

        repository.updateDocument(document.getId(), updates, new GedRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue(document.isFavorite() ? "Retiré des favoris" : "Ajouté aux favoris");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur favoris: " + errorMessage);
            }
        });
    }

    // Méthodes d'import de fichiers
    public void importFileInSection(String sectionId, Uri fileUri) {
        if (fileUri == null) {
            error.postValue("Fichier invalide");
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Importation du fichier...");
        currentActiveSectionId = sectionId;

        String folderId = sectionCurrentFolderIds.get(sectionId);

        repository.importDocument(fileUri, folderId, new GedRepository.ProgressCallback() {
            @Override
            public void onProgress(int percentage) {
                statusMessage.postValue("Importation en cours... " + percentage + "%");
            }

            @Override
            public void onComplete(String result) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Fichier importé avec succès");
                refreshSectionView(sectionId);
                loadUserStats();
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur importation: " + errorMessage);
            }
        });
    }

    public void importImageInSection(String sectionId, Uri imageUri) {
        if (imageUri == null) {
            error.postValue("Image invalide");
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Traitement de l'image...");
        currentActiveSectionId = sectionId;

        String folderId = sectionCurrentFolderIds.get(sectionId);

        repository.importDocument(imageUri, folderId, new GedRepository.ProgressCallback() {
            @Override
            public void onProgress(int percentage) {
                statusMessage.postValue("Traitement en cours... " + percentage + "%");
            }

            @Override
            public void onComplete(String result) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Image importée avec succès");
                refreshSectionView(sectionId);
                loadUserStats();
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur traitement image: " + errorMessage);
            }
        });
    }

    public void processCameraImageInSection(String sectionId, Intent data) {
        if (data == null || data.getExtras() == null) {
            error.postValue("Image de caméra invalide");
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Traitement de la photo...");
        currentActiveSectionId = sectionId;

        // Traiter l'image de la caméra (bitmap dans les extras)
        android.graphics.Bitmap bitmap = (android.graphics.Bitmap) data.getExtras().get("data");
        if (bitmap == null) {
            error.postValue("Impossible de récupérer l'image");
            loading.postValue(false);
            return;
        }

        String folderId = sectionCurrentFolderIds.get(sectionId);

        repository.processCameraBitmap(bitmap, folderId, new GedRepository.ProgressCallback() {
            @Override
            public void onProgress(int percentage) {
                statusMessage.postValue("Traitement photo... " + percentage + "%");
            }

            @Override
            public void onComplete(String result) {
                loading.postValue(false);
                statusMessage.postValue(null);
                operationSuccess.postValue("Photo traitée avec succès");
                refreshSectionView(sectionId);
                loadUserStats();
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur traitement photo: " + errorMessage);
            }
        });
    }

    // Méthodes de recherche et filtrage
    public void searchItems(String query) {
        currentSearchQuery.postValue(query);

        if (query == null || query.trim().isEmpty()) {
            searchResults.postValue(null);
            return;
        }

        loading.postValue(true);
        statusMessage.postValue("Recherche en cours...");

        repository.searchDocuments(query.trim(), new GedRepository.SearchCallback() {
            @Override
            public void onSuccess(List<GedFolder> folders, List<GedDocument> documents) {
                loading.postValue(false);
                statusMessage.postValue(null);

                List<Object> results = new ArrayList<>();
                if (folders != null) results.addAll(folders);
                if (documents != null) results.addAll(documents);
                searchResults.postValue(results);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                statusMessage.postValue(null);
                error.postValue("Erreur recherche: " + errorMessage);
            }
        });
    }

    public void clearSearch() {
        currentSearchQuery.postValue("");
        searchResults.postValue(null);
    }

    public void applyFilter(String filterType) {
        currentFilter.postValue(filterType);

        // Recharger les données avec le filtre appliqué
        for (String sectionId : sectionExpansionStates.keySet()) {
            if (Boolean.TRUE.equals(sectionExpansionStates.get(sectionId))) {
                loadSectionData(sectionId);
            }
        }
    }

    // Méthodes utilitaires
    public void openDocument(Context context, GedDocument document) {
        repository.openDocument(context, document, new GedRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                // Document ouvert avec succès
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Erreur ouverture document: " + errorMessage);
            }
        });
    }

    public void refreshAllSections() {
        loading.postValue(true);
        statusMessage.postValue("Actualisation...");

        for (String sectionId : sectionExpansionStates.keySet()) {
            if (Boolean.TRUE.equals(sectionExpansionStates.get(sectionId))) {
                loadSectionData(sectionId);
            }
        }

        loadUserStats();
    }

    private void refreshSectionView(String sectionId) {
        String folderId = sectionCurrentFolderIds.get(sectionId);
        if (folderId == null) {
            loadSectionData(sectionId);
        } else {
            repository.getFolderById(folderId, new GedRepository.FolderCallback() {
                @Override
                public void onSuccess(GedFolder folder) {
                    if (folder != null) {
                        navigateToSectionFolder(sectionId, folder);
                    } else {
                        // Le dossier n'existe plus, revenir à la racine
                        navigateToSectionRoot(sectionId);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // En cas d'erreur, revenir à la racine de la section
                    navigateToSectionRoot(sectionId);
                }
            });
        }
    }

    private void loadUserStats() {
        repository.getStorageStats(new GedRepository.StorageStatsCallback() {
            @Override
            public void onSuccess(int documentCount, long totalSize) {
                String info = formatFileSize(totalSize) + " • " + documentCount + " document" +
                        (documentCount > 1 ? "s" : "");
                storageInfo.postValue(info);

                Map<String, Object> stats = new HashMap<>();
                stats.put("documentCount", documentCount);
                stats.put("totalSize", totalSize);
                userStats.postValue(stats);
            }

            @Override
            public void onError(String errorMessage) {
                storageInfo.postValue("0 B • 0 documents");
            }
        });
    }

    // Méthodes privées pour la mise à jour des LiveData
    private void updateSectionFolders(String sectionId, List<GedFolder> folders) {
        Map<String, List<GedFolder>> current = sectionsFolders.getValue();
        if (current == null) current = new HashMap<>();
        current.put(sectionId, folders != null ? folders : new ArrayList<>());
        sectionsFolders.postValue(current);
    }

    private void updateSectionDocuments(String sectionId, List<GedDocument> documents) {
        Map<String, List<GedDocument>> current = sectionsDocuments.getValue();
        if (current == null) current = new HashMap<>();
        current.put(sectionId, documents != null ? documents : new ArrayList<>());
        sectionsDocuments.postValue(current);
    }

    private void updateSectionDocumentCount(String sectionId, int count) {
        Map<String, Integer> current = sectionsDocumentCounts.getValue();
        if (current == null) current = new HashMap<>();
        current.put(sectionId, count);
        sectionsDocumentCounts.postValue(current);
    }

    private void updateSectionPath(String sectionId, String path) {
        Map<String, List<String>> current = sectionsPaths.getValue();
        if (current == null) current = new HashMap<>();

        List<String> pathList = new ArrayList<>();
        if (path != null && !path.equals("/")) {
            String[] parts = path.split("/");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    pathList.add(part);
                }
            }
        }

        current.put(sectionId, pathList);
        sectionsPaths.postValue(current);
    }

    private String getSectionDisplayName(String sectionId) {
        switch (sectionId) {
            case "admin": return "Documents Administratifs";
            case "fournisseurs": return "Fournisseurs";
            case "clients": return "Clients";
            case "autres": return "Autres Documents";
            default: return sectionId;
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // Getters pour l'état
    public String getCurrentActiveSectionId() {
        return currentActiveSectionId;
    }

    public boolean isSectionExpanded(String sectionId) {
        return Boolean.TRUE.equals(sectionExpansionStates.get(sectionId));
    }

    // Méthode pour vérifier si on est dans un sous-dossier
    public boolean isInSubfolder(String sectionId) {
        return sectionCurrentFolderIds.containsKey(sectionId);
    }

    // Méthode pour obtenir l'ID du dossier courant
    public String getCurrentFolderId(String sectionId) {
        return sectionCurrentFolderIds.get(sectionId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}

