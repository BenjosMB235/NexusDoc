package com.example.nexusdoc.ui.archives.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.archives.model.ArchiveDocument;
import com.example.nexusdoc.ui.archives.model.ArchiveFolder;
import com.example.nexusdoc.ui.archives.repository.ArchivesRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArchivesViewModel extends AndroidViewModel {

    private final ArchivesRepository repository;

    // LiveData for UI - EXISTANT
    private final MutableLiveData<String> currentPath = new MutableLiveData<>();
    private final MutableLiveData<List<ArchiveFolder>> currentFolders = new MutableLiveData<>();
    private final MutableLiveData<List<ArchiveDocument>> currentDocuments = new MutableLiveData<>();
    private final MutableLiveData<List<Object>> filteredItems = new MutableLiveData<>();
    private final MutableLiveData<String> storageInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> operationSuccess = new MutableLiveData<>();

    // NOUVEAU - LiveData pour les sections
    private final MutableLiveData<Map<String, Boolean>> sectionsExpansionState = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<String>>> sectionsPaths = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<ArchiveFolder>>> sectionsFolders = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<ArchiveDocument>>> sectionsDocuments = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> sectionsDocumentCounts = new MutableLiveData<>();

    // Current state - EXISTANT
    private String currentFolderId = null;
    private String currentSearchQuery = "";
    private String currentFilter = "all";
    private boolean isInFolderView = true;

    // NOUVEAU - État des sections
    private String currentSectionId = null;
    private Map<String, String> sectionCurrentFolderIds = new HashMap<>();
    private Map<String, Boolean> sectionExpansionStates = new HashMap<>();

    public ArchivesViewModel(Application application) {
        super(application);
        this.repository = new ArchivesRepository(application.getApplicationContext());

        // Initialize - EXISTANT
        currentPath.setValue("/");
        loading.setValue(false);

        // NOUVEAU - Initialiser les sections
        initializeSections();
    }

    // NOUVEAU - Initialisation des sections
    private void initializeSections() {
        sectionExpansionStates.put("admin", false);
        sectionExpansionStates.put("fournisseurs", false);
        sectionExpansionStates.put("clients", false);
        sectionExpansionStates.put("autres", false);

        sectionsExpansionState.setValue(new HashMap<>(sectionExpansionStates));
        sectionsPaths.setValue(new HashMap<>());
        sectionsFolders.setValue(new HashMap<>());
        sectionsDocuments.setValue(new HashMap<>());
        sectionsDocumentCounts.setValue(new HashMap<>());
    }

    // Getters for LiveData - EXISTANT
    public LiveData<String> getCurrentPath() { return currentPath; }
    public LiveData<List<ArchiveFolder>> getCurrentFolders() { return currentFolders; }
    public LiveData<List<ArchiveDocument>> getCurrentDocuments() { return currentDocuments; }
    public LiveData<List<Object>> getFilteredItems() { return filteredItems; }
    public LiveData<String> getStorageInfo() { return storageInfo; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getOperationSuccess() { return operationSuccess; }

    // NOUVEAU - Getters pour les sections
    public LiveData<Map<String, Boolean>> getSectionsExpansionState() { return sectionsExpansionState; }
    public LiveData<Map<String, List<String>>> getSectionsPaths() { return sectionsPaths; }
    public LiveData<Map<String, List<ArchiveFolder>>> getSectionsFolders() { return sectionsFolders; }
    public LiveData<Map<String, List<ArchiveDocument>>> getSectionsDocuments() { return sectionsDocuments; }
    public LiveData<Map<String, Integer>> getSectionsDocumentCounts() { return sectionsDocumentCounts; }

    // NOUVEAU - Méthodes pour les sections
    public void toggleSectionExpansion(String sectionId) {
        Boolean currentState = sectionExpansionStates.get(sectionId);
        boolean newState = currentState == null ? true : !currentState;

        sectionExpansionStates.put(sectionId, newState);
        sectionsExpansionState.setValue(new HashMap<>(sectionExpansionStates));

        if (newState) {
            loadSectionData(sectionId);
        }
    }

    public void loadSectionData(String sectionId) {
        loading.setValue(true);
        currentSectionId = sectionId;

        repository.getFoldersByType(sectionId, new ArchivesRepository.FoldersCallback() {
            @Override
            public void onSuccess(List<ArchiveFolder> folders) {
                loading.setValue(false);

                // Mettre à jour les données de la section
                Map<String, List<ArchiveFolder>> currentSectionsFolders = sectionsFolders.getValue();
                if (currentSectionsFolders == null) currentSectionsFolders = new HashMap<>();
                currentSectionsFolders.put(sectionId, folders);
                sectionsFolders.setValue(currentSectionsFolders);

                // Calculer le nombre de documents
                int totalDocs = 0;
                for (ArchiveFolder folder : folders) {
                    totalDocs += folder.getDocumentCount();
                }

                Map<String, Integer> currentCounts = sectionsDocumentCounts.getValue();
                if (currentCounts == null) currentCounts = new HashMap<>();
                currentCounts.put(sectionId, totalDocs);
                sectionsDocumentCounts.setValue(currentCounts);

                updateStorageInfo();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void navigateToSectionFolder(String sectionId, ArchiveFolder folder) {
        loading.setValue(true);
        currentSectionId = sectionId;
        sectionCurrentFolderIds.put(sectionId, folder.getId());

        // Mettre à jour le chemin de la section
        Map<String, List<String>> currentPaths = sectionsPaths.getValue();
        if (currentPaths == null) currentPaths = new HashMap<>();

        String[] pathParts = folder.getPath().split("/");
        java.util.List<String> pathList = new java.util.ArrayList<>();
        for (String part : pathParts) {
            if (!part.isEmpty()) {
                pathList.add(part);
            }
        }
        currentPaths.put(sectionId, pathList);
        sectionsPaths.setValue(currentPaths);

        // Charger le contenu du dossier
        repository.getFolderContents(folder.getId(), new ArchivesRepository.FolderContentsCallback() {
            @Override
            public void onSuccess(List<ArchiveFolder> subfolders, List<ArchiveDocument> documents) {
                loading.setValue(false);

                Map<String, List<ArchiveFolder>> currentSectionsFolders = sectionsFolders.getValue();
                Map<String, List<ArchiveDocument>> currentSectionsDocuments = sectionsDocuments.getValue();

                if (currentSectionsFolders == null) currentSectionsFolders = new HashMap<>();
                if (currentSectionsDocuments == null) currentSectionsDocuments = new HashMap<>();

                if (subfolders.isEmpty()) {
                    // Afficher les documents s'il n'y a pas de sous-dossiers
                    currentSectionsDocuments.put(sectionId, documents);
                    currentSectionsFolders.put(sectionId, new java.util.ArrayList<>());
                } else {
                    // Afficher les sous-dossiers
                    currentSectionsFolders.put(sectionId, subfolders);
                    currentSectionsDocuments.put(sectionId, new java.util.ArrayList<>());
                }

                sectionsFolders.setValue(currentSectionsFolders);
                sectionsDocuments.setValue(currentSectionsDocuments);
                updateStorageInfo();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void navigateBackInSection(String sectionId, int pathIndex) {
        if (pathIndex == 0) {
            // Retour à la racine de la section
            navigateToSectionRoot(sectionId);
        } else {
            // Navigation vers un niveau spécifique du chemin
            Map<String, List<String>> currentPaths = sectionsPaths.getValue();
            if (currentPaths != null && currentPaths.containsKey(sectionId)) {
                List<String> sectionPath = currentPaths.get(sectionId);
                if (sectionPath != null && pathIndex < sectionPath.size()) {
                    String targetPath = String.join("/", sectionPath.subList(0, pathIndex + 1));
                    findAndNavigateToSectionPath(sectionId, targetPath);
                }
            }
        }
    }

    public void navigateToSectionRoot(String sectionId) {
        sectionCurrentFolderIds.remove(sectionId);

        // Réinitialiser le chemin
        Map<String, List<String>> currentPaths = sectionsPaths.getValue();
        if (currentPaths == null) currentPaths = new HashMap<>();
        currentPaths.put(sectionId, new java.util.ArrayList<>());
        sectionsPaths.setValue(currentPaths);

        loadSectionData(sectionId);
    }

    private void findAndNavigateToSectionPath(String sectionId, String targetPath) {
        repository.getFolderByPath(targetPath, sectionId, new ArchivesRepository.FolderCallback() {
            @Override
            public void onSuccess(ArchiveFolder folder) {
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

    // MODIFIÉ - Création de dossier avec support des sections
    public void createFolderInSection(String sectionId, String folderName) {
        loading.setValue(true);
        currentSectionId = sectionId;

        String parentFolderId = sectionCurrentFolderIds.get(sectionId);
        ArchiveFolder newFolder = new ArchiveFolder(folderName, sectionId, parentFolderId);

        repository.createFolder(newFolder, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Dossier créé avec succès");

                // Rafraîchir la section
                if (parentFolderId == null) {
                    loadSectionData(sectionId);
                } else {
                    repository.getFolderById(parentFolderId, new ArchivesRepository.FolderCallback() {
                        @Override
                        public void onSuccess(ArchiveFolder folder) {
                            navigateToSectionFolder(sectionId, folder);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            loadSectionData(sectionId);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    // MODIFIÉ - Import de fichier avec support des sections
    public void importFileInSection(String sectionId, Uri fileUri) {
        loading.setValue(true);
        currentSectionId = sectionId;

        String folderId = sectionCurrentFolderIds.get(sectionId);

        repository.importFile(fileUri, folderId, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Fichier importé avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    // MODIFIÉ - Import d'image avec support des sections
    public void importImageInSection(String sectionId, Uri imageUri) {
        loading.setValue(true);
        currentSectionId = sectionId;

        String folderId = sectionCurrentFolderIds.get(sectionId);

        repository.importImage(imageUri, folderId, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Image importée avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    // MODIFIÉ - Traitement d'image caméra avec support des sections
    public void processCameraImageInSection(String sectionId, android.content.Intent data) {
        loading.setValue(true);
        currentSectionId = sectionId;

        String folderId = sectionCurrentFolderIds.get(sectionId);

        repository.processCameraImage(data, folderId, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Document scanné avec succès");
                refreshSectionView(sectionId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    private void refreshSectionView(String sectionId) {
        String folderId = sectionCurrentFolderIds.get(sectionId);
        if (folderId == null) {
            loadSectionData(sectionId);
        } else {
            repository.getFolderById(folderId, new ArchivesRepository.FolderCallback() {
                @Override
                public void onSuccess(ArchiveFolder folder) {
                    navigateToSectionFolder(sectionId, folder);
                }

                @Override
                public void onError(String errorMessage) {
                    loadSectionData(sectionId);
                }
            });
        }
    }

    // MÉTHODES EXISTANTES CONSERVÉES
    public void loadRootFolders() {
        loading.setValue(true);
        currentFolderId = null;
        currentPath.setValue("/");
        isInFolderView = true;

        repository.getRootFolders(new ArchivesRepository.FoldersCallback() {
            @Override
            public void onSuccess(List<ArchiveFolder> folders) {
                loading.setValue(false);
                currentFolders.setValue(folders);
                updateStorageInfo();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void navigateToFolder(ArchiveFolder folder) {
        loading.setValue(true);
        currentFolderId = folder.getId();
        currentPath.setValue(folder.getPath());

        repository.getFolderContents(folder.getId(), new ArchivesRepository.FolderContentsCallback() {
            @Override
            public void onSuccess(List<ArchiveFolder> subfolders, List<ArchiveDocument> documents) {
                loading.setValue(false);

                if (subfolders.isEmpty()) {
                    isInFolderView = false;
                    currentDocuments.setValue(documents);
                } else {
                    isInFolderView = true;
                    currentFolders.setValue(subfolders);
                }

                updateStorageInfo();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void navigateUp() {
        if (currentFolderId != null) {
            repository.getParentFolder(currentFolderId, new ArchivesRepository.FolderCallback() {
                @Override
                public void onSuccess(ArchiveFolder parentFolder) {
                    if (parentFolder != null) {
                        navigateToFolder(parentFolder);
                    } else {
                        loadRootFolders();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    loadRootFolders();
                }
            });
        }
    }

    // Search and filter methods - EXISTANT
    public void searchItems(String query) {
        currentSearchQuery = query;
        applySearchAndFilter();
    }

    public void clearSearch() {
        currentSearchQuery = "";
        applySearchAndFilter();
    }

    public void applyFilter(String filterType) {
        currentFilter = filterType;
        applySearchAndFilter();
    }

    private void applySearchAndFilter() {
        if (currentSearchQuery.isEmpty() && currentFilter.equals("all")) {
            return;
        }

        loading.setValue(true);

        repository.searchAndFilter(currentSearchQuery, currentFilter, currentFolderId,
                new ArchivesRepository.SearchCallback() {
                    @Override
                    public void onSuccess(List<Object> items) {
                        loading.setValue(false);
                        filteredItems.setValue(items);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loading.setValue(false);
                        error.setValue(errorMessage);
                    }
                });
    }

    // CRUD operations - EXISTANT (conservé pour compatibilité)
    public void createFolder(String folderName) {
        loading.setValue(true);

        ArchiveFolder newFolder = new ArchiveFolder(folderName, ArchiveFolder.TYPE_AUTRES, currentFolderId);

        repository.createFolder(newFolder, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Dossier créé avec succès");
                refreshCurrentView();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void importFile(Uri fileUri) {
        loading.setValue(true);

        repository.importFile(fileUri, currentFolderId, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Fichier importé avec succès");
                refreshCurrentView();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void importImage(Uri imageUri) {
        loading.setValue(true);

        repository.importImage(imageUri, currentFolderId, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Image importée avec succès");
                refreshCurrentView();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void processCameraImage(android.content.Intent data) {
        loading.setValue(true);

        repository.processCameraImage(data, currentFolderId, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Document scanné avec succès");
                refreshCurrentView();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void openDocument(ArchiveDocument document) {
        repository.openDocument(document, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                // Document opened successfully
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
            }
        });
    }

    public void deleteDocument(ArchiveDocument document) {
        loading.setValue(true);

        repository.deleteDocument(document, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Document supprimé");
                refreshCurrentView();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void deleteFolder(ArchiveFolder folder) {
        loading.setValue(true);

        repository.deleteFolder(folder, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Dossier supprimé");
                refreshCurrentView();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    // Utility methods - EXISTANT
    private void refreshCurrentView() {
        if (currentFolderId == null) {
            loadRootFolders();
        } else {
            repository.getFolderById(currentFolderId, new ArchivesRepository.FolderCallback() {
                @Override
                public void onSuccess(ArchiveFolder folder) {
                    navigateToFolder(folder);
                }

                @Override
                public void onError(String errorMessage) {
                    loadRootFolders();
                }
            });
        }
    }

    private void updateStorageInfo() {
        repository.getStorageInfo(new ArchivesRepository.StorageCallback() {
            @Override
            public void onSuccess(String info) {
                storageInfo.setValue(info);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle silently
            }
        });
    }

    public boolean isInFolderView() {
        return isInFolderView;
    }

    // Sync methods - EXISTANT
    public void syncAll() {
        loading.setValue(true);

        repository.syncAllData(new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                operationSuccess.setValue("Synchronisation terminée");
                refreshCurrentView();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    // NOUVEAU - Méthodes utilitaires pour les sections
    public String getCurrentSectionId() {
        return currentSectionId;
    }

    public boolean isSectionExpanded(String sectionId) {
        Boolean state = sectionExpansionStates.get(sectionId);
        return state != null && state;
    }
}