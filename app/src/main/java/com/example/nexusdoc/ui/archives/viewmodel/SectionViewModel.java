package com.example.nexusdoc.ui.archives.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.archives.model.ArchiveDocument;
import com.example.nexusdoc.ui.archives.model.ArchiveFolder;
import com.example.nexusdoc.ui.archives.repository.ArchivesRepository;

import java.util.ArrayList;
import java.util.List;

public class SectionViewModel extends AndroidViewModel {

    private final String sectionType;
    private final ArchivesRepository repository;

    // LiveData for UI
    private final MutableLiveData<Boolean> isExpanded = new MutableLiveData<>(false);
    private final MutableLiveData<List<String>> currentPath = new MutableLiveData<>();
    private final MutableLiveData<List<ArchiveFolder>> currentFolders = new MutableLiveData<>();
    private final MutableLiveData<List<ArchiveDocument>> currentDocuments = new MutableLiveData<>();
    private final MutableLiveData<Integer> documentCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // Current state
    private ArchiveFolder currentFolder = null;

    public SectionViewModel(Application application, String sectionType) {
        super(application);
        this.sectionType = sectionType;
        this.repository = new ArchivesRepository(application.getApplicationContext());

        // Initialize
        currentPath.setValue(new ArrayList<>());
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsExpanded() { return isExpanded; }
    public LiveData<List<String>> getCurrentPath() { return currentPath; }
    public LiveData<List<ArchiveFolder>> getCurrentFolders() { return currentFolders; }
    public LiveData<List<ArchiveDocument>> getCurrentDocuments() { return currentDocuments; }
    public LiveData<Integer> getDocumentCount() { return documentCount; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void toggleExpansion() {
        Boolean current = isExpanded.getValue();
        boolean newState = current == null ? true : !current;
        isExpanded.setValue(newState);

        if (newState) {
            loadSectionData();
        }
    }

    public void loadSectionData() {
        loading.setValue(true);

        repository.getFoldersByType(sectionType, new ArchivesRepository.FoldersCallback() {
            @Override
            public void onSuccess(List<ArchiveFolder> folders) {
                loading.setValue(false);
                currentFolders.setValue(folders);
                updateDocumentCount(folders);
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
        currentFolder = folder;

        // Update path
        List<String> newPath = new ArrayList<>();
        if (folder.getPath() != null) {
            String[] pathParts = folder.getPath().split("/");
            for (String part : pathParts) {
                if (!part.isEmpty()) {
                    newPath.add(part);
                }
            }
        }
        currentPath.setValue(newPath);

        // Load folder contents
        repository.getFolderContents(folder.getId(), new ArchivesRepository.FolderContentsCallback() {
            @Override
            public void onSuccess(List<ArchiveFolder> subfolders, List<ArchiveDocument> documents) {
                loading.setValue(false);

                if (subfolders.isEmpty()) {
                    // Show documents if no subfolders
                    currentDocuments.setValue(documents);
                    currentFolders.setValue(new ArrayList<>());
                } else {
                    // Show subfolders
                    currentFolders.setValue(subfolders);
                    currentDocuments.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void navigateBack(int pathIndex) {
        if (pathIndex == 0) {
            // Return to root
            navigateToRoot();
        } else {
            // Navigate to specific path level
            List<String> currentPathList = currentPath.getValue();
            if (currentPathList != null && pathIndex < currentPathList.size()) {
                // Find folder at this path level and navigate to it
                String targetPath = String.join("/", currentPathList.subList(0, pathIndex + 1));
                findAndNavigateToPath(targetPath);
            }
        }
    }

    public void navigateToRoot() {
        currentFolder = null;
        currentPath.setValue(new ArrayList<>());
        loadSectionData();
    }

    private void findAndNavigateToPath(String targetPath) {
        // Implementation to find folder by path and navigate to it
        repository.getFolderByPath(targetPath, sectionType, new ArchivesRepository.FolderCallback() {
            @Override
            public void onSuccess(ArchiveFolder folder) {
                if (folder != null) {
                    navigateToFolder(folder);
                } else {
                    navigateToRoot();
                }
            }

            @Override
            public void onError(String errorMessage) {
                navigateToRoot();
            }
        });
    }

    private void updateDocumentCount(List<ArchiveFolder> folders) {
        int totalCount = 0;
        for (ArchiveFolder folder : folders) {
            totalCount += folder.getDocumentCount();
        }
        documentCount.setValue(totalCount);
    }

    public String getSectionType() {
        return sectionType;
    }

    public boolean isInRootFolder() {
        return currentFolder == null;
    }
}