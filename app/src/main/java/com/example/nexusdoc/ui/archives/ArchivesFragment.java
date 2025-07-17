package com.example.nexusdoc.ui.archives;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.archives.adapter.DocumentAdapter;
import com.example.nexusdoc.ui.archives.adapter.FolderAdapter;
import com.example.nexusdoc.ui.archives.model.ArchiveDocument;
import com.example.nexusdoc.ui.archives.model.ArchiveFolder;
import com.example.nexusdoc.ui.archives.model.DocumentSection;
import com.example.nexusdoc.ui.archives.viewmodel.ArchivesViewModel;
import com.example.nexusdoc.ui.utilitaires.AnimationUtils;
import com.example.nexusdoc.ui.utilitaires.DialogUtils;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArchivesFragment extends Fragment implements
        DocumentAdapter.OnDocumentClickListener,
        FolderAdapter.OnFolderClickListener {

    private ArchivesViewModel viewModel;

    // File selection
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    // UI Components
    private EditText searchEditText;
    private ImageView btnClearSearch, btnViewMode;
    private ChipGroup filterChipGroup;
    private LinearLayout sectionsContainer;
    private TextView storageInfo;
    private FloatingActionButton fabAdd;

    // Sections
    private List<DocumentSection> sections;
    private Map<String, View> sectionViews;

    // View mode
    private boolean isGridView = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_archives, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        setupFilePickers();
        initViews(view);
        setupSections();
        setupObservers();
        setupClickListeners();
        loadSavedStates();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(ArchivesViewModel.class);

        sections = Arrays.asList(
                new DocumentSection("admin", "Documents Administratifs", R.drawable.ic_admin_panel_settings, "#3B82F6"),
                new DocumentSection("fournisseurs", "Fournisseurs", R.drawable.ic_local_shipping, "#10B981"),
                new DocumentSection("clients", "Clients", R.drawable.ic_business, "#F59E0B"),
                new DocumentSection("autres", "Autres Documents", R.drawable.ic_folder, "#8B5CF6")
        );

        sectionViews = new HashMap<>();
    }

    private void setupFilePickers() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            String currentSectionId = viewModel.getCurrentSectionId();
                            if (currentSectionId != null) {
                                viewModel.importFileInSection(currentSectionId, fileUri);
                            } else {
                                viewModel.importFile(fileUri);
                            }
                        }
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        String currentSectionId = viewModel.getCurrentSectionId();
                        if (currentSectionId != null) {
                            viewModel.processCameraImageInSection(currentSectionId, result.getData());
                        } else {
                            viewModel.processCameraImage(result.getData());
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            String currentSectionId = viewModel.getCurrentSectionId();
                            if (currentSectionId != null) {
                                viewModel.importImageInSection(currentSectionId, imageUri);
                            } else {
                                viewModel.importImage(imageUri);
                            }
                        }
                    }
                }
        );
    }

    private void initViews(View view) {
        // Search components
        searchEditText = view.findViewById(R.id.search_edit_text);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        btnViewMode = view.findViewById(R.id.btn_view_mode);

        // Filter chips
        filterChipGroup = view.findViewById(R.id.filter_chip_group);

        // Sections container
        sectionsContainer = view.findViewById(R.id.sections_container);

        // Storage info
        storageInfo = view.findViewById(R.id.storage_info);

        // FAB
        fabAdd = view.findViewById(R.id.fab_add);
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
                .inflate(R.layout.item_document_section, sectionsContainer, false);

        // Header components
        View headerContainer = sectionView.findViewById(R.id.section_header_container);
        ImageView sectionIcon = sectionView.findViewById(R.id.section_icon);
        TextView sectionTitle = sectionView.findViewById(R.id.section_title);
        TextView sectionCounter = sectionView.findViewById(R.id.section_counter);
        ImageView chevronIcon = sectionView.findViewById(R.id.chevron_icon);

        // Content components
        View contentContainer = sectionView.findViewById(R.id.section_content_container);
        LinearLayout breadcrumbContainer = sectionView.findViewById(R.id.breadcrumb_container);
        LinearLayout quickActionsContainer = sectionView.findViewById(R.id.quick_actions_container);
        RecyclerView itemsRecyclerView = sectionView.findViewById(R.id.items_recycler_view);

        // Set section info
        sectionIcon.setImageResource(section.getIconResource());
        sectionTitle.setText(section.getTitle());
        sectionCounter.setText("0");

        // Setup RecyclerView
        itemsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), isGridView ? 2 : 1));

        // Setup click listeners
        headerContainer.setOnClickListener(v -> toggleSection(section.getId()));

        // Setup quick actions
        setupQuickActions(quickActionsContainer, section.getId());

        return sectionView;
    }

    private void setupQuickActions(LinearLayout container, String sectionId) {
        View btnScan = container.findViewById(R.id.btn_scan_document);
        View btnCreateFolder = container.findViewById(R.id.btn_create_folder);
        View btnUploadFile = container.findViewById(R.id.btn_upload_file);

        btnScan.setOnClickListener(v -> openDocumentScanner(sectionId));
        btnCreateFolder.setOnClickListener(v -> showCreateFolderDialog(sectionId));
        btnUploadFile.setOnClickListener(v -> openFilePicker(sectionId));
    }

    private void setupObservers() {
        // Observer pour l'état d'expansion des sections
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

        // Observer pour les dossiers des sections
        viewModel.getSectionsFolders().observe(getViewLifecycleOwner(), sectionsFolders -> {
            if (sectionsFolders != null) {
                for (Map.Entry<String, List<ArchiveFolder>> entry : sectionsFolders.entrySet()) {
                    updateSectionFolders(entry.getKey(), entry.getValue());
                }
            }
        });

        // Observer pour les documents des sections
        viewModel.getSectionsDocuments().observe(getViewLifecycleOwner(), sectionsDocuments -> {
            if (sectionsDocuments != null) {
                for (Map.Entry<String, List<ArchiveDocument>> entry : sectionsDocuments.entrySet()) {
                    updateSectionDocuments(entry.getKey(), entry.getValue());
                }
            }
        });

        // Observer pour les compteurs de documents
        viewModel.getSectionsDocumentCounts().observe(getViewLifecycleOwner(), documentCounts -> {
            if (documentCounts != null) {
                for (Map.Entry<String, Integer> entry : documentCounts.entrySet()) {
                    updateSectionCounter(entry.getKey(), entry.getValue());
                }
            }
        });

        // Observer pour les chemins des sections
        viewModel.getSectionsPaths().observe(getViewLifecycleOwner(), sectionsPaths -> {
            if (sectionsPaths != null) {
                for (Map.Entry<String, List<String>> entry : sectionsPaths.entrySet()) {
                    updateSectionBreadcrumb(entry.getKey(), entry.getValue());
                }
            }
        });

        // Observers existants
        viewModel.getStorageInfo().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                storageInfo.setText(info);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            // Show/hide loading indicator
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                DialogUtils.showErrorMessage(requireContext(), error);
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                DialogUtils.showSuccessMessage(requireContext(), message);
            }
        });
    }

    private void updateSectionExpansion(String sectionId, boolean isExpanded) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            View contentContainer = sectionView.findViewById(R.id.section_content_container);
            ImageView chevronIcon = sectionView.findViewById(R.id.chevron_icon);

            AnimationUtils.animateExpansion(contentContainer, isExpanded);
            AnimationUtils.animateChevronRotation(chevronIcon, isExpanded);

            saveSectionState(sectionId, isExpanded);
        }
    }

    private void updateSectionFolders(String sectionId, List<ArchiveFolder> folders) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            RecyclerView recyclerView = sectionView.findViewById(R.id.items_recycler_view);
            FolderAdapter adapter = new FolderAdapter(this, isGridView);
            adapter.updateFolders(folders);
            recyclerView.setAdapter(adapter);
        }
    }

    private void updateSectionDocuments(String sectionId, List<ArchiveDocument> documents) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            RecyclerView recyclerView = sectionView.findViewById(R.id.items_recycler_view);
            DocumentAdapter adapter = new DocumentAdapter(this, isGridView);
            adapter.updateDocuments(documents);
            recyclerView.setAdapter(adapter);
        }
    }

    private void updateSectionCounter(String sectionId, int count) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            TextView counter = sectionView.findViewById(R.id.section_counter);
            counter.setText(String.valueOf(count));
        }
    }

    private void updateSectionBreadcrumb(String sectionId, List<String> path) {
        View sectionView = sectionViews.get(sectionId);
        if (sectionView != null) {
            LinearLayout breadcrumbContainer = sectionView.findViewById(R.id.breadcrumb_container);
            breadcrumbContainer.removeAllViews();

            if (path.isEmpty()) {
                breadcrumbContainer.setVisibility(View.GONE);
                return;
            }

            breadcrumbContainer.setVisibility(View.VISIBLE);

            // Add home button
            DocumentSection section = findSectionById(sectionId);
            if (section != null) {
                View homeButton = createBreadcrumbItem(section.getTitle(), true);
                homeButton.setOnClickListener(v -> viewModel.navigateToSectionRoot(sectionId));
                breadcrumbContainer.addView(homeButton);

                // Add path items
                for (int i = 0; i < path.size(); i++) {
                    if (i > 0 || !path.get(i).equals(section.getTitle())) {
                        // Add separator
                        breadcrumbContainer.addView(createBreadcrumbSeparator());

                        // Add path item
                        final int pathIndex = i;
                        View pathButton = createBreadcrumbItem(path.get(i), false);
                        pathButton.setOnClickListener(v -> viewModel.navigateBackInSection(sectionId, pathIndex));
                        breadcrumbContainer.addView(pathButton);
                    }
                }
            }
        }
    }

    private DocumentSection findSectionById(String sectionId) {
        for (DocumentSection section : sections) {
            if (section.getId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    private View createBreadcrumbItem(String text, boolean isHome) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(R.color.primary_color));
        textView.setPadding(16, 8, 16, 8);
        textView.setBackground(getResources().getDrawable(R.drawable.breadcrumb_item_background));

        if (isHome) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home, 0, 0, 0);
            textView.setCompoundDrawablePadding(8);
        }

        return textView;
    }

    private View createBreadcrumbSeparator() {
        ImageView separator = new ImageView(getContext());
        separator.setImageResource(R.drawable.ic_chevron_right);
        separator.setColorFilter(getResources().getColor(R.color.text_secondary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);
        separator.setLayoutParams(params);
        return separator;
    }

    private void setupClickListeners() {
        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                viewModel.searchItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search
        btnClearSearch.setOnClickListener(v -> {
            searchEditText.setText("");
            viewModel.clearSearch();
        });

        // View mode toggle
        btnViewMode.setOnClickListener(v -> toggleViewMode());

        // Filter chips
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String filterType = "";
            if (checkedId == R.id.chip_recent) {
                filterType = "recent";
            } else if (checkedId == R.id.chip_favorites) {
                filterType = "favorites";
            } else if (checkedId == R.id.chip_shared) {
                filterType = "shared";
            } else {
                filterType = "all";
            }
            viewModel.applyFilter(filterType);
        });

        // FAB
        fabAdd.setOnClickListener(v -> showAddOptions());
    }

    private void toggleSection(String sectionId) {
        viewModel.toggleSectionExpansion(sectionId);
    }

    private void toggleViewMode() {
        isGridView = !isGridView;
        int spanCount = isGridView ? 2 : 1;
        btnViewMode.setImageResource(isGridView ? R.drawable.ic_list_view : R.drawable.ic_grid_view);

        // Update all RecyclerViews
        for (View sectionView : sectionViews.values()) {
            RecyclerView recyclerView = sectionView.findViewById(R.id.items_recycler_view);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

            if (recyclerView.getAdapter() instanceof FolderAdapter) {
                ((FolderAdapter) recyclerView.getAdapter()).setGridView(isGridView);
            } else if (recyclerView.getAdapter() instanceof DocumentAdapter) {
                ((DocumentAdapter) recyclerView.getAdapter()).setGridView(isGridView);
            }
        }
    }

    // Quick Action Methods
    private void openDocumentScanner(String sectionId) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        }
    }

    private void showCreateFolderDialog(String sectionId) {
        DialogUtils.showInputDialog(requireContext(),
                "Créer un nouveau dossier",
                "Nom du dossier",
                folderName -> {
                    if (!folderName.trim().isEmpty()) {
                        viewModel.createFolderInSection(sectionId, folderName.trim());
                    }
                });
    }

    private void openFilePicker(String sectionId) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Sélectionner un fichier"));
    }

    private void showAddOptions() {
        String[] options = {"Scanner un document", "Prendre une photo", "Importer un fichier", "Créer un dossier"};

        DialogUtils.showSingleChoiceDialog(requireContext(),
                "Ajouter",
                options,
                -1,
                new DialogUtils.DialogCallback() {
                    @Override
                    public void onPositive() {
                        // Handle selection
                    }
                });
    }

    // State persistence
    private void saveSectionState(String sectionId, boolean isExpanded) {
        SharedPreferences prefs = requireContext().getSharedPreferences("sections_state", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(sectionId + "_expanded", isExpanded).apply();
    }

    private void loadSavedStates() {
        SharedPreferences prefs = requireContext().getSharedPreferences("sections_state", Context.MODE_PRIVATE);

        for (DocumentSection section : sections) {
            boolean isExpanded = prefs.getBoolean(section.getId() + "_expanded", false);
            if (isExpanded) {
                viewModel.toggleSectionExpansion(section.getId());
            }
        }
    }

    // Interface implementations
    @Override
    public void onDocumentClick(ArchiveDocument document) {
        viewModel.openDocument(document);
    }

    @Override
    public void onDocumentLongClick(ArchiveDocument document) {
        // Handle document long click
    }

    @Override
    public void onFolderClick(ArchiveFolder folder) {
        String currentSectionId = viewModel.getCurrentSectionId();
        if (currentSectionId != null) {
            viewModel.navigateToSectionFolder(currentSectionId, folder);
        } else {
            viewModel.navigateToFolder(folder);
        }
    }

    @Override
    public void onFolderLongClick(ArchiveFolder folder) {
        // Handle folder long click
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup if needed
    }
}