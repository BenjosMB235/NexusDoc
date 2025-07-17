package com.example.nexusdoc.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.profile.model.UpdateProfileRequest;
import com.example.nexusdoc.ui.profile.validator.ProfileValidator;
import com.example.nexusdoc.ui.profile.viewmodel.ModifierProfileViewModel;
import com.example.nexusdoc.ui.utilitaires.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ModifierProfileFragment extends Fragment {

    private ModifierProfileViewModel viewModel;
    private ProfileValidator validator;

    // Image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    // UI Components
    private CardView profileImageContainer;
    private ImageView profileImage;
    private TextView changePhotoText;
    private View loadingOverlay;

    // Input fields
    private TextInputLayout usernameLayout, fonctionLayout, emailLayout, phoneLayout;
    private TextInputEditText usernameInput, fonctionInput, emailInput, phoneInput;

    // Buttons
    private MaterialButton btnCancel, btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modifier_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        setupImagePicker();
        initViews(view);
        setupObservers();
        setupClickListeners();

        // Charger les données actuelles
        viewModel.loadCurrentUserData();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(ModifierProfileViewModel.class);
        validator = new ProfileValidator();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            profileImage.setImageURI(selectedImageUri);
                            changePhotoText.setText("Photo sélectionnée");
                        }
                    }
                }
        );
    }

    private void initViews(View view) {

        // Image
        profileImageContainer = view.findViewById(R.id.profile_image_container);
        profileImage = view.findViewById(R.id.profile_image);
        changePhotoText = view.findViewById(R.id.change_photo_text);

        // Loading
        loadingOverlay = view.findViewById(R.id.loading_overlay);

        // Input layouts
        usernameLayout = view.findViewById(R.id.username_layout);
        fonctionLayout = view.findViewById(R.id.fonction_layout);
        emailLayout = view.findViewById(R.id.email_layout);
        phoneLayout = view.findViewById(R.id.phone_layout);

        // Input fields
        usernameInput = view.findViewById(R.id.username_input);
        fonctionInput = view.findViewById(R.id.fonction_input);
        emailInput = view.findViewById(R.id.email_input);
        phoneInput = view.findViewById(R.id.phone_input);

        // Buttons
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);
    }

    private void setupObservers() {
        viewModel.getCurrentUserData().observe(getViewLifecycleOwner(), userData -> {
            if (userData != null) {
                populateFields(userData);
            }
        });

        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                DialogUtils.showSuccessMessage(requireContext(), "Profil mis à jour avec succès");
                navigateBack();
            } else {
                DialogUtils.showErrorMessage(requireContext(), result.getErrorMessage());
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!loading);
            btnCancel.setEnabled(!loading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                DialogUtils.showErrorMessage(requireContext(), error);
            }
        });
    }

    private void setupClickListeners() {

        profileImageContainer.setOnClickListener(v -> openImagePicker());

        btnCancel.setOnClickListener(v -> {
            DialogUtils.showConfirmationDialog(requireContext(),
                    "Annuler les modifications",
                    "Êtes-vous sûr de vouloir annuler ? Les modifications non sauvegardées seront perdues.",
                    "Oui, annuler",
                    "Continuer l'édition",
                    new DialogUtils.DialogCallback() {
                        @Override
                        public void onPositive() {
                            navigateBack();
                        }
                    });
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void populateFields(ModifierProfileViewModel.UserData userData) {
        usernameInput.setText(userData.getUsername());
        fonctionInput.setText(userData.getFonction());
        emailInput.setText(userData.getEmail());
        phoneInput.setText(userData.getPhone());

        // Load current profile image if available
        if (userData.getProfileImageBase64() != null) {
            android.graphics.Bitmap bitmap = com.example.nexusdoc.ui.profile.repository.ProfileRepository
                    .base64ToBitmap(userData.getProfileImageBase64());
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProfile() {
        String username = usernameInput.getText().toString().trim();
        String fonction = fonctionInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        ProfileValidator.ValidationResult validation = validator.validate(username, fonction, email, phone);

        if (!validation.isValid()) {
            DialogUtils.showErrorMessage(requireContext(), validation.getErrorMessage());
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(username, fonction, email, phone, selectedImageUri);
        viewModel.updateProfile(request);
    }

    private void navigateBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
    }
}