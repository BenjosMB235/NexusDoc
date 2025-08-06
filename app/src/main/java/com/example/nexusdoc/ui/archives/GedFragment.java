package com.example.nexusdoc.ui.archives;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.archives.adapter.GedMixedAdapter;
import com.example.nexusdoc.ui.archives.model.DocumentSection;
import com.example.nexusdoc.ui.archives.model.GedDocument;
import com.example.nexusdoc.ui.archives.model.GedFolder;
import com.example.nexusdoc.ui.archives.viewmodel.GedViewModel;
import com.example.nexusdoc.ui.utilitaires.AnimationUtils;
import com.example.nexusdoc.ui.utilitaires.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GedFragment extends Fragment implements GedMixedAdapter.OnItemClickListener {

    private static final String TAG = "GedFragment";
    private GedViewModel viewModel;

    // File selection launchers
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    // UI Components
    private EditText searchEditText;
    private ImageView btnClearSearch, btnViewMode;
    private ChipGroup filterChipGroup;
    private LinearLayout sectionsContainer;
    private TextView storageInfo, statusText;
    private FloatingActionButton fabAdd;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;

    // Sections configuration
    private List<DocumentSection> sections;
    private Map<String, View> sectionViews;

    // View state
    private String currentActiveSectionId = null;
    private boolean isSelectionMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ged, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            initializeComponents();
            setupPermissions();
            setupFilePickers();
            initViews(view);
            setupSections();
            setupObservers();
            setupClickListeners();
            loadSavedStates();
        } catch (Exception e) {
            Log.e(TAG, "Erreur initialisation fragment", e);
            showSnackbar("Erreur d'initialisation", Snackbar.LENGTH_LONG);
        }
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(GedViewModel.class);

        sections = Arrays.asList(
                new DocumentSection("admin", "Documents Administratifs",
                        R.drawable.ic_admin_panel_settings, "#1976D2"),
                new DocumentSection("fournisseurs", "Fournisseurs",
                        R.drawable.ic_local_shipping, "#388E3C"),
                new DocumentSection("clients", "Clients",
                        R.drawable.ic_business, "#F57C00"),
                new DocumentSection("autres", "Autres Documents",
                        R.drawable.ic_folder, "#7B1FA2")
        );

        sectionViews = new HashMap<>();
    }

    private void setupPermissions() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = permissions.values().stream().allMatch(granted -> granted);
                    if (!allGranted) {
                        showSnackbar("Permissions requises pour accéder aux fichiers et à la caméra",
                                Snackbar.LENGTH_LONG);
                    }
                }
        );
    }

    private void setupFilePickers() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            Uri fileUri = result.getData().getData();
                            if (fileUri != null && currentActiveSectionId != null) {
                                Log.d(TAG, "Fichier sélectionné: " + fileUri);
                                viewModel.importFileInSection(currentActiveSectionId, fileUri);
                            } else {
                                Log.w(TAG, "URI fichier ou section null");
                                showSnackbar("Erreur: fichier non sélectionné", Snackbar.LENGTH_SHORT);
                            }
                        } else {
                            Log.d(TAG, "Sélection fichier annulée");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur traitement sélection fichier", e);
                        showSnackbar("Erreur lors de la sélection du fichier", Snackbar.LENGTH_LONG);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            if (currentActiveSectionId != null) {
                                Log.d(TAG, "Photo prise avec succès");
                                viewModel.processCameraImageInSection(currentActiveSectionId, result.getData());
                            } else {
                                Log.w(TAG, "Section active null pour photo");
                                showSnackbar("Erreur: section non sélectionnée", Snackbar.LENGTH_SHORT);
                            }
                        } else {
                            Log.d(TAG, "Prise de photo annulée");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur traitement photo caméra", e);
                        showSnackbar("Erreur lors de la prise de photo", Snackbar.LENGTH_LONG);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null && currentActiveSectionId != null) {
                                Log.d(TAG, "Image galerie sélectionnée: " + imageUri);
                                viewModel.importImageInSection(currentActiveSectionId, imageUri);
                            } else {
                                Log.w(TAG, "URI image ou section null");
                                showSnackbar("Erreur: image non sélectionnée", Snackbar.LENGTH_SHORT);
                            }
                        } else {
                            Log.d(TAG, "Sélection galerie annulée");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur traitement sélection galerie", e);
                        showSnackbar("Erreur lors de la sélection d'image", Snackbar.LENGTH_LONG);
                    }
                }
        );
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.search_edit_text);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        btnViewMode = view.findViewById(R.id.btn_view_mode);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        sectionsContainer = view.findViewById(R.id.sections_container);
        storageInfo = view.findViewById(R.id.storage_info);
        statusText = view.findViewById(R.id.status_text);
        fabAdd = view.findViewById(R.id.fab_add);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
    }

    private void setupSections() {
        for (DocumentSection section : sections) {
            View sectionView = createSectionView(section);
            sectionViews.put(section.getId(), sectionView);
            sectionsContainer.addView(sectionView);
        }
    }

    private View createSectionView(DocumentSection section) {
        View sectionView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_ged_section, sectionsContainer, false);

        // Header components
        MaterialCardView headerContainer = sectionView.findViewById(R.id.section_header_container);
        ShapeableImageView sectionIcon = sectionView.findViewById(R.id.section_icon);
        MaterialTextView sectionTitle = sectionView.findViewById(R.id.section_title);
        MaterialTextView sectionCounter = sectionView.findViewById(R.id.section_counter);
        ShapeableImageView chevronIcon = sectionView.findViewById(R.id.chevron_icon);

        // Content components
        LinearLayout contentContainer = sectionView.findViewById(R.id.section_content_container);
        ChipGroup breadcrumbContainer = sectionView.findViewById(R.id.breadcrumb_container);
        LinearLayout quickActionsContainer = sectionView.findViewById(R.id.quick_actions_container);
        RecyclerView itemsRecyclerView = sectionView.findViewById(R.id.items_recycler_view);

        // Configure section
        sectionIcon.setImageResource(section.getIconResource());
        sectionTitle.setText(section.getTitle());
        sectionCounter.setText("0");

        // Setup RecyclerView avec l'adaptateur mixte
        itemsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        itemsRecyclerView.setHasFixedSize(true);

        // Setup click listeners
        headerContainer.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Section cliquée: " + section.getId());
                currentActiveSectionId = section.getId();
                viewModel.toggleSectionExpansion(section.getId());
            } catch (Exception e) {
                Log.e(TAG, "Erreur clic section", e);
                showSnackbar("Erreur lors de l'ouverture de la section", Snackbar.LENGTH_SHORT);
            }
        });

        // Setup quick actions
        setupQuickActions(quickActionsContainer, section.getId());

        // Empty state click
        View emptyState = sectionView.findViewById(R.id.empty_state);
        if (emptyState != null) {
            emptyState.setOnClickListener(v -> {
                currentActiveSectionId = section.getId();
                showAddOptionsForSection(section.getId());
            });
        }

        return sectionView;
    }

    private void setupQuickActions(LinearLayout container, String sectionId) {
        MaterialButton btnScan = container.findViewById(R.id.btn_scan_document);
        MaterialButton btnCreateFolder = container.findViewById(R.id.btn_create_folder);
        MaterialButton btnUploadFile = container.findViewById(R.id.btn_upload_file);

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                currentActiveSectionId = sectionId;
                openDocumentScanner();
            });
        }

        if (btnCreateFolder != null) {
            btnCreateFolder.setOnClickListener(v -> {
                currentActiveSectionId = sectionId;
                showCreateFolderDialog(sectionId);
            });
        }

        if (btnUploadFile != null) {
            btnUploadFile.setOnClickListener(v -> {
                currentActiveSectionId = sectionId;
                openFilePicker();
            });
        }
    }

    private void setupObservers() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null && loadingIndicator != null) {
                loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(loading);
                }
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (statusText != null) {
                statusText.setText(message);
                statusText.setVisibility(message != null ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "Erreur ViewModel: " + error);
                showSnackbar(error, Snackbar.LENGTH_LONG);
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Log.d(TAG, "Succès: " + message);
                showSnackbar(message, Snackbar.LENGTH_SHORT);
            }
        });

        viewModel.getSectionsExpansionState().observe(getViewLifecycleOwner(), expansionStates -> {
            if (expansionStates != null) {
                for (DocumentSection section : sections) {
                    Boolean isExpanded = expansionStates.get(section.getId());
                    if (isExpanded != null) {
                        updateSectionExpansion(section.getId(), isExpanded);
                    }
                }
            }
        });

        // CORRECTION: Observer unifié pour les dossiers ET documents
        viewModel.getSectionsFolders().observe(getViewLifecycleOwner(), sectionsFolders -> {
            if (sectionsFolders != null) {
                for (Map.Entry<String, List<GedFolder>> entry : sectionsFolders.entrySet()) {
                    updateSectionContent(entry.getKey(), entry.getValue(), null);
                }
            }
        });

        viewModel.getSectionsDocuments().observe(getViewLifecycleOwner(), sectionsDocuments -> {
            if (sectionsDocuments != null) {
                for (Map.Entry<String, List<GedDocument>> entry : sectionsDocuments.entrySet()) {
                    updateSectionContent(entry.getKey(), null, entry.getValue());
                }
            }
        });

        viewModel.getSectionsDocumentCounts().observe(getViewLifecycleOwner(), documentCounts -> {
            if (documentCounts != null) {
                for (Map.Entry<String, Integer> entry : documentCounts.entrySet()) {
                    updateSectionCounter(entry.getKey(), entry.getValue());
                }
            }
        });

        viewModel.getSectionsPaths().observe(getViewLifecycleOwner(), sectionsPaths -> {
            if (sectionsPaths != null) {
                for (Map.Entry<String, List<String>> entry : sectionsPaths.entrySet()) {
                    updateSectionBreadcrumb(entry.getKey(), entry.getValue());
                }
            }
        });

        viewModel.getStorageInfo().observe(getViewLifecycleOwner(), info -> {
            if (info != null && storageInfo != null) {
                storageInfo.setText(info);
            }
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            // Gérer l'affichage des résultats de recherche
        });
    }

    private void setupClickListeners() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                    viewModel.searchItems(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                searchEditText.setText("");
                viewModel.clearSearch();
            });
        }

        if (btnViewMode != null) {
            btnViewMode.setOnClickListener(v -> toggleViewMode());
        }

        if (filterChipGroup != null) {
            filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                String filterType = "all";
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chip_recent) {
                        filterType = "recent";
                    } else if (checkedId == R.id.chip_favorites) {
                        filterType = "favorites";
                    } else if (checkedId == R.id.chip_shared) {
                        filterType = "shared";
                    }
                }
                viewModel.applyFilter(filterType);
            });
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddOptions());
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshAllSections());
        }
    }

    private void updateSectionExpansion(String sectionId, boolean isExpanded) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            LinearLayout contentContainer = sectionView.findViewById(R.id.section_content_container);
            ShapeableImageView chevronIcon = sectionView.findViewById(R.id.chevron_icon);

            if (contentContainer != null) {
                AnimationUtils.animateExpansion(contentContainer, isExpanded);
            }

            if (chevronIcon != null) {
                AnimationUtils.animateChevronRotation(chevronIcon, isExpanded);
            }

            saveSectionState(sectionId, isExpanded);
        }
    }

    // CORRECTION: Méthode unifiée pour afficher dossiers ET documents
    private void updateSectionContent(String sectionId, List<GedFolder> folders, List<GedDocument> documents) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            RecyclerView recyclerView = sectionView.findViewById(R.id.items_recycler_view);
            if (recyclerView != null) {
                try {
                    // Récupérer l'adaptateur existant ou en créer un nouveau
                    GedMixedAdapter adapter;
                    if (recyclerView.getAdapter() instanceof GedMixedAdapter) {
                        adapter = (GedMixedAdapter) recyclerView.getAdapter();
                    } else {
                        adapter = new GedMixedAdapter(this);
                        recyclerView.setAdapter(adapter);
                    }

                    // Récupérer les données existantes si une des listes est null
                    List<GedFolder> currentFolders = folders;
                    List<GedDocument> currentDocuments = documents;

                    if (currentFolders == null) {
                        Map<String, List<GedFolder>> sectionsFolders = viewModel.getSectionsFolders().getValue();
                        if (sectionsFolders != null && sectionsFolders.containsKey(sectionId)) {
                            currentFolders = sectionsFolders.get(sectionId);
                        }
                    }

                    if (currentDocuments == null) {
                        Map<String, List<GedDocument>> sectionsDocuments = viewModel.getSectionsDocuments().getValue();
                        if (sectionsDocuments != null && sectionsDocuments.containsKey(sectionId)) {
                            currentDocuments = sectionsDocuments.get(sectionId);
                        }
                    }

                    // Mise à jour de l'adaptateur avec les deux types de données
                    adapter.updateItems(
                            currentFolders != null ? currentFolders : new ArrayList<>(),
                            currentDocuments != null ? currentDocuments : new ArrayList<>()
                    );

                    // Show/hide empty state
                    View emptyState = sectionView.findViewById(R.id.empty_state);
                    if (emptyState != null) {
                        boolean isEmpty = (currentFolders == null || currentFolders.isEmpty()) &&
                                (currentDocuments == null || currentDocuments.isEmpty());
                        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Erreur mise à jour contenu section " + sectionId, e);
                }
            }
        }
    }

    private void updateSectionCounter(String sectionId, int count) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            MaterialTextView counter = sectionView.findViewById(R.id.section_counter);
            if (counter != null) {
                counter.setText(String.valueOf(count));
            }
        }
    }

    private void updateSectionBreadcrumb(String sectionId, List<String> path) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            ChipGroup breadcrumbContainer = sectionView.findViewById(R.id.breadcrumb_container);
            if (breadcrumbContainer != null) {
                breadcrumbContainer.removeAllViews();

                if (path == null || path.isEmpty()) {
                    breadcrumbContainer.setVisibility(View.GONE);
                    return;
                }

                breadcrumbContainer.setVisibility(View.VISIBLE);

                DocumentSection section = findSectionById(sectionId);
                if (section != null) {
                    Chip homeChip = createBreadcrumbChip(section.getTitle(), true);
                    homeChip.setOnClickListener(v -> {
                        currentActiveSectionId = sectionId;
                        viewModel.navigateToSectionRoot(sectionId);
                    });
                    breadcrumbContainer.addView(homeChip);

                    for (int i = 0; i < path.size(); i++) {
                        if (i > 0 || !path.get(i).equals(section.getTitle())) {
                            final int pathIndex = i;
                            Chip pathChip = createBreadcrumbChip(path.get(i), false);
                            pathChip.setOnClickListener(v -> {
                                currentActiveSectionId = sectionId;
                                viewModel.navigateBackInSection(sectionId, pathIndex);
                            });
                            breadcrumbContainer.addView(pathChip);
                        }
                    }
                }
            }
        }
    }

    private Chip createBreadcrumbChip(String text, boolean isHome) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.chip_background_unselected);
        chip.setTextColor(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnSurface));

        if (isHome) {
            chip.setChipIconResource(R.drawable.ic_home);
        }

        return chip;
    }

    private DocumentSection findSectionById(String sectionId) {
        return sections.stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElse(null);
    }

    private void toggleViewMode() {
        // Cette méthode peut être complètement supprimée ou modifiée pour autre chose
        // Si vous voulez garder le bouton mais pour une autre fonctionnalité
        showSnackbar("Mode liste uniquement disponible", Snackbar.LENGTH_SHORT);
    }

    // Quick Action Methods
    private void openDocumentScanner() {
        if (checkPermissions()) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    cameraLauncher.launch(intent);
                } else {
                    showSnackbar("Application caméra non disponible", Snackbar.LENGTH_SHORT);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur ouverture caméra", e);
                showSnackbar("Erreur ouverture caméra", Snackbar.LENGTH_SHORT);
            }
        }
    }

    private void showCreateFolderDialog(String sectionId) {
        DialogUtils.showInputDialog(requireContext(),
                "Créer un nouveau dossier",
                "Nom du dossier",
                "",
                folderName -> {
                    if (!folderName.trim().isEmpty()) {
                        currentActiveSectionId = sectionId;
                        viewModel.createFolderInSection(sectionId, folderName.trim());
                    }
                });
    }

    private void openFilePicker() {
        if (checkPermissions()) {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                String[] mimeTypes = {
                        "image/*",
                        "application/pdf",
                        "text/plain",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                };
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                Log.d(TAG, "Ouverture sélecteur de fichiers");
                filePickerLauncher.launch(Intent.createChooser(intent, "Sélectionner un fichier"));
            } catch (Exception e) {
                Log.e(TAG, "Erreur ouverture sélecteur fichier", e);
                showSnackbar("Erreur ouverture sélecteur de fichier", Snackbar.LENGTH_SHORT);
            }
        }
    }

    private void showAddOptions() {
        if (currentActiveSectionId == null) {
            showSnackbar("Veuillez d'abord sélectionner une section", Snackbar.LENGTH_SHORT);
            return;
        }
        showAddOptionsForSection(currentActiveSectionId);
    }

    private void showAddOptionsForSection(String sectionId) {
        currentActiveSectionId = sectionId;

        String[] options = {
                "Scanner un document",
                "Prendre une photo",
                "Importer depuis la galerie",
                "Importer un fichier",
                "Créer un dossier"
        };

        DialogUtils.showSingleChoiceDialog(requireContext(),
                "Ajouter",
                options,
                -1,
                new DialogUtils.DialogCallback() {
                    @Override
                    public void onPositive(int which) {
                        try {
                            switch (which) {
                                case 0: openDocumentScanner(); break;
                                case 1: openCamera(); break;
                                case 2: openGallery(); break;
                                case 3: openFilePicker(); break;
                                case 4: showCreateFolderDialog(sectionId); break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur action ajout", e);
                            showSnackbar("Erreur lors de l'action", Snackbar.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onPositive() {}

                    @Override
                    public void onNegative() {}
                });
    }

    private void openCamera() {
        if (checkPermissions()) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    cameraLauncher.launch(intent);
                } else {
                    showSnackbar("Application caméra non disponible", Snackbar.LENGTH_SHORT);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur ouverture caméra", e);
                showSnackbar("Erreur ouverture caméra", Snackbar.LENGTH_SHORT);
            }
        }
    }

    private void openGallery() {
        if (checkPermissions()) {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            } catch (Exception e) {
                Log.e(TAG, "Erreur ouverture galerie", e);
                showSnackbar("Erreur ouverture galerie", Snackbar.LENGTH_SHORT);
            }
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean images = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean camera = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

            if (!images || !camera) {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA
                });
                return false;
            }
        } else {
            boolean storage = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean camera = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

            if (!storage || !camera) {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                });
                return false;
            }
        }
        return true;
    }

    // Interface implementations pour GedMixedAdapter.OnItemClickListener
    @Override
    public void onFolderClick(GedFolder folder) {
        try {
            if (isSelectionMode) {
                // Toggle selection
            } else {
                if (currentActiveSectionId != null && folder != null) {
                    Log.d(TAG, "Navigation vers dossier: " + folder.getName() + " (ID: " + folder.getId() + ")");
                    viewModel.navigateToSectionFolder(currentActiveSectionId, folder);
                } else {
                    Log.w(TAG, "Section active ou dossier null");
                    showSnackbar("Erreur: section non sélectionnée", Snackbar.LENGTH_SHORT);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur clic dossier", e);
            showSnackbar("Erreur ouverture dossier", Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onFolderLongClick(GedFolder folder) {
        if (!isSelectionMode) {
            showFolderContextMenu(folder);
        }
    }

    @Override
    public void onDocumentClick(GedDocument document) {
        try {
            if (isSelectionMode) {
                // Toggle selection
            } else {
                viewModel.openDocument(getContext(), document);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur clic document", e);
            showSnackbar("Erreur ouverture document", Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onDocumentLongClick(GedDocument document) {
        if (!isSelectionMode) {
            showDocumentContextMenu(document);
        }
    }

    @Override
    public void onFavoriteToggle(GedDocument document) {
        try {
            if (currentActiveSectionId != null) {
                viewModel.toggleFavoriteInSection(currentActiveSectionId, document);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur toggle favori", e);
            showSnackbar("Erreur modification favori", Snackbar.LENGTH_SHORT);
        }
    }

    private void showDocumentContextMenu(GedDocument document) {
        String[] options = {"Ouvrir", "Partager", "Renommer", "Favoris", "Supprimer"};

        DialogUtils.showSingleChoiceDialog(requireContext(),
                document.getName(),
                options,
                -1,
                new DialogUtils.DialogCallback() {
                    @Override
                    public void onPositive(int which) {
                        try {
                            switch (which) {
                                case 0: viewModel.openDocument(getContext(), document); break;
                                case 1: shareDocument(document); break;
                                case 2: showRenameDocumentDialog(document); break;
                                case 3: viewModel.toggleFavoriteInSection(currentActiveSectionId, document); break;
                                case 4: showDeleteDocumentConfirmation(document); break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur action document", e);
                            showSnackbar("Erreur lors de l'action", Snackbar.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onPositive() {}

                    @Override
                    public void onNegative() {}
                });
    }

    private void showFolderContextMenu(GedFolder folder) {
        String[] options = {"Ouvrir", "Renommer", "Supprimer", "Propriétés"};

        DialogUtils.showSingleChoiceDialog(requireContext(),
                folder.getName(),
                options,
                -1,
                new DialogUtils.DialogCallback() {
                    @Override
                    public void onPositive(int which) {
                        try {
                            switch (which) {
                                case 0: onFolderClick(folder); break;
                                case 1: showRenameFolderDialog(folder); break;
                                case 2: showDeleteFolderConfirmation(folder); break;
                                case 3: showFolderProperties(folder); break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur action dossier", e);
                            showSnackbar("Erreur lors de l'action", Snackbar.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onPositive() {}

                    @Override
                    public void onNegative() {}
                });
    }

    private void showRenameDocumentDialog(GedDocument document) {
        DialogUtils.showInputDialog(requireContext(),
                "Renommer le document",
                "Nouveau nom",
                document.getName(),
                newName -> {
                    if (!newName.trim().isEmpty() && !newName.equals(document.getName())) {
                        viewModel.renameDocumentInSection(currentActiveSectionId, document, newName.trim());
                    }
                });
    }

    private void showRenameFolderDialog(GedFolder folder) {
        DialogUtils.showInputDialog(requireContext(),
                "Renommer le dossier",
                "Nouveau nom",
                folder.getName(),
                newName -> {
                    if (!newName.trim().isEmpty() && !newName.equals(folder.getName())) {
                        viewModel.renameFolderInSection(currentActiveSectionId, folder, newName.trim());
                    }
                });
    }

    private void showDeleteDocumentConfirmation(GedDocument document) {
        DialogUtils.showConfirmationDialog(requireContext(),
                "Supprimer le document",
                "Êtes-vous sûr de vouloir supprimer \"" + document.getName() + "\" ?",
                () -> viewModel.deleteDocumentInSection(currentActiveSectionId, document));
    }

    private void showDeleteFolderConfirmation(GedFolder folder) {
        DialogUtils.showConfirmationDialog(requireContext(),
                "Supprimer le dossier",
                "Êtes-vous sûr de vouloir supprimer \"" + folder.getName() + "\" et tout son contenu ?",
                () -> viewModel.deleteFolderInSection(currentActiveSectionId, folder));
    }

    private void showFolderProperties(GedFolder folder) {
        String properties = "Nom: " + folder.getName() + "\n" +
                "Type: " + folder.getType() + "\n" +
                "Documents: " + folder.getDocumentCount() + "\n" +
                "Sous-dossiers: " + folder.getSubfolderCount() + "\n" +
                "Taille: " + folder.getFormattedSize() + "\n" +
                "Créé: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault()).format(folder.getCreatedAt()) + "\n" +
                "Modifié: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault()).format(folder.getUpdatedAt());

        DialogUtils.showInfoDialog(requireContext(), "Propriétés du dossier", properties);
    }

    private void shareDocument(GedDocument document) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Document partagé");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Je partage avec vous le document: " + document.getName() +
                    "\n\nCode unique: " + document.getUniqueCode());
            startActivity(Intent.createChooser(shareIntent, "Partager le document"));
        } catch (Exception e) {
            Log.e(TAG, "Erreur partage document", e);
            showSnackbar("Erreur lors du partage", Snackbar.LENGTH_SHORT);
        }
    }

    private void showSnackbar(String message, int duration) {
        if (getView() != null) {
            try {
                Snackbar.make(getView(), message, duration).show();
            } catch (Exception e) {
                Log.e(TAG, "Erreur affichage snackbar", e);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSectionState(String sectionId, boolean isExpanded) {
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("ged_sections_state", Context.MODE_PRIVATE);
            prefs.edit().putBoolean(sectionId + "_expanded", isExpanded).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde état section", e);
        }
    }

    private void loadSavedStates() {
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("ged_sections_state", Context.MODE_PRIVATE);

            for (DocumentSection section : sections) {
                boolean isExpanded = prefs.getBoolean(section.getId() + "_expanded", false);
                if (isExpanded) {
                    currentActiveSectionId = section.getId();
                    viewModel.toggleSectionExpansion(section.getId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement états sauvegardés", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            if (viewModel != null) {
                // Le cleanup sera fait dans onCleared() du ViewModel
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur cleanup fragment", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentActiveSectionId != null) {
            saveSectionState(currentActiveSectionId, viewModel.isSectionExpanded(currentActiveSectionId));
        }
    }
}