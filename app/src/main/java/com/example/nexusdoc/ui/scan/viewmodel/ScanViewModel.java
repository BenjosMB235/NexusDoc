package com.example.nexusdoc.ui.scan.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.archives.model.ArchiveDocument;
import com.example.nexusdoc.ui.archives.repository.ArchivesRepository;
import com.example.nexusdoc.ui.scan.model.ScanResult;

import java.util.List;

public class ScanViewModel extends AndroidViewModel {

    private final ArchivesRepository archivesRepository;

    private final MutableLiveData<List<ArchiveDocument>> recentFiles = new MutableLiveData<>();
    private final MutableLiveData<ScanResult> scanResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ScanViewModel(Application application) {
        super(application);
        this.archivesRepository = new ArchivesRepository(application.getApplicationContext());
        loading.setValue(false);
    }

    public LiveData<List<ArchiveDocument>> getRecentFiles() { return recentFiles; }
    public LiveData<ScanResult> getScanResult() { return scanResult; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadRecentFiles() {
        loading.setValue(true);

        // Load recent documents (last 10)
        archivesRepository.getRecentDocuments(10, new ArchivesRepository.DocumentsCallback() {
            @Override
            public void onSuccess(List<ArchiveDocument> documents) {
                loading.setValue(false);
                recentFiles.setValue(documents);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void processCameraResult(Intent data) {
        loading.setValue(true);

        archivesRepository.processCameraImage(data, null, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                scanResult.setValue(ScanResult.success(message));
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                scanResult.setValue(ScanResult.error(errorMessage));
            }
        });
    }

    public void processGalleryImage(Uri imageUri) {
        loading.setValue(true);

        archivesRepository.importImage(imageUri, null, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                scanResult.setValue(ScanResult.success(message));
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                scanResult.setValue(ScanResult.error(errorMessage));
            }
        });
    }

    public void processImportedFile(Uri fileUri) {
        loading.setValue(true);

        archivesRepository.importFile(fileUri, null, new ArchivesRepository.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                loading.setValue(false);
                scanResult.setValue(ScanResult.success(message));
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                scanResult.setValue(ScanResult.error(errorMessage));
            }
        });
    }

    public void openDocument(ArchiveDocument document) {
        archivesRepository.openDocument(document, new ArchivesRepository.OperationCallback() {
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
}