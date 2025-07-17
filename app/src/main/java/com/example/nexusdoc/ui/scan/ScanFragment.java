package com.example.nexusdoc.ui.scan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.archives.adapter.DocumentAdapter;
import com.example.nexusdoc.ui.archives.model.ArchiveDocument;
import com.example.nexusdoc.ui.scan.viewmodel.ScanViewModel;
import com.example.nexusdoc.ui.utilitaires.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class ScanFragment extends Fragment implements DocumentAdapter.OnDocumentClickListener {

    private ScanViewModel viewModel;

    // File selection launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> fileLauncher;

    // UI Components
    private MaterialCardView smartScanCard, importImagesCard, importFilesCard, scanBatchCard;
    private ExtendedFloatingActionButton fabQuickScan;
    private MaterialButton btnViewAll;
    private RecyclerView recyclerRecentFiles;

    // Adapter
    private DocumentAdapter recentFilesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        setupLaunchers();
        initViews(view);
        setupObservers();
        setupClickListeners();

        // Load recent files
        viewModel.loadRecentFiles();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);
    }

    private void setupLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        viewModel.processCameraResult(result.getData());
                    }
                }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            viewModel.processGalleryImage(imageUri);
                        }
                    }
                }
        );

        // File launcher
        fileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            viewModel.processImportedFile(fileUri);
                        }
                    }
                }
        );
    }

    private void initViews(View view) {
        // Scan option cards
        smartScanCard = view.findViewById(R.id.smart_scan_card);
        importImagesCard = view.findViewById(R.id.import_images_card);
        importFilesCard = view.findViewById(R.id.import_files_card);
        scanBatchCard = view.findViewById(R.id.scan_batch_card);

        // Other components
        fabQuickScan = view.findViewById(R.id.fab_quick_scan);
        btnViewAll = view.findViewById(R.id.btn_view_all);
        recyclerRecentFiles = view.findViewById(R.id.recycler_recent_files);

        // Setup RecyclerView
        recyclerRecentFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        recentFilesAdapter = new DocumentAdapter(this, false); // List view for recent files
        recyclerRecentFiles.setAdapter(recentFilesAdapter);
    }

    private void setupObservers() {
        viewModel.getRecentFiles().observe(getViewLifecycleOwner(), recentFiles -> {
            if (recentFiles != null) {
                recentFilesAdapter.updateDocuments(recentFiles);
            }
        });

        viewModel.getScanResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                DialogUtils.showSuccessMessage(requireContext(), "Document scanné avec succès");
                viewModel.loadRecentFiles(); // Refresh recent files
            } else {
                DialogUtils.showErrorMessage(requireContext(), result.getErrorMessage());
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            fabQuickScan.setEnabled(!loading);
            if (loading) {
                fabQuickScan.setText("Traitement...");
            } else {
                fabQuickScan.setText("Scan rapide");
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                DialogUtils.showErrorMessage(requireContext(), error);
            }
        });
    }

    private void setupClickListeners() {
        // Smart Scan - Camera with AI processing
        smartScanCard.setOnClickListener(v -> openSmartScan());

        // Import Images - Gallery
        importImagesCard.setOnClickListener(v -> openGallery());

        // Import Files - File picker
        importFilesCard.setOnClickListener(v -> openFilePicker());

        // Scan Batch - Multiple documents
        scanBatchCard.setOnClickListener(v -> openBatchScan());

        // Quick scan FAB
        fabQuickScan.setOnClickListener(v -> openSmartScan());

        // View all recent files
        btnViewAll.setOnClickListener(v -> navigateToArchives());
    }

    private void openSmartScan() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            DialogUtils.showErrorMessage(requireContext(), "Caméra non disponible");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileLauncher.launch(Intent.createChooser(intent, "Sélectionner un fichier"));
    }

    private void openBatchScan() {
        // For batch scanning, we can open camera multiple times or use a specialized scanner
        DialogUtils.showInfoMessage(requireContext(),
                "Fonctionnalité de scan par lot à venir");
    }

    private void navigateToArchives() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_scan_to_archives);
    }

    // DocumentAdapter.OnDocumentClickListener implementation
    @Override
    public void onDocumentClick(ArchiveDocument document) {
        viewModel.openDocument(document);
    }

    @Override
    public void onDocumentLongClick(ArchiveDocument document) {
        showDocumentOptions(document);
    }

    private void showDocumentOptions(ArchiveDocument document) {
        String[] options = {"Ouvrir", "Partager", "Supprimer", "Voir détails"};

        DialogUtils.showSingleChoiceDialog(requireContext(),
                document.getName(),
                options,
                -1,
                new DialogUtils.DialogCallback() {
                    @Override
                    public void onPositive() {
                        // Handle document action based on selection
                    }
                });
    }
}