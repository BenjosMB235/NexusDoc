package com.example.nexusdoc.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nexusdoc.ui.authenfication.AuthActivity;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.profile.viewmodel.ProfileViewModel;
import com.example.nexusdoc.ui.utilitaires.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;

    // UI Components
    private ImageView profileAvatar;
    private TextView tvFullName;
    private TextView tvUserFonction;
    private TextView tvUserEmail;
    private TextView tvUsername;
    private TextView tvFonction;
    private TextView tvEmail;
    private TextView tvTelephone;

    private MaterialButton btnModifierProfil;
    private MaterialButton btnChangerMotDePasse;
    private MaterialButton btnLogout;
    private FloatingActionButton fabEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        initViews(view);
        setupObservers();
        setupClickListeners();

        // Charger les données du profil
        viewModel.loadUserProfile();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    private void initViews(View view) {
        // Header views
        profileAvatar = view.findViewById(R.id.profile_avatar);
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvUserFonction = view.findViewById(R.id.tv_user_fonction);
        tvUserEmail = view.findViewById(R.id.tv_user_email);

        // Info views
        tvUsername = view.findViewById(R.id.tv_username);
        tvFonction = view.findViewById(R.id.tv_fonction);
        tvEmail = view.findViewById(R.id.tv_email);
        tvTelephone = view.findViewById(R.id.tv_telephone);

        // Buttons
        btnModifierProfil = view.findViewById(R.id.btn_modifier_profil);
        btnChangerMotDePasse = view.findViewById(R.id.btn_changer_mot_de_passe);
        btnLogout = view.findViewById(R.id.btn_logout);
        fabEdit = view.findViewById(R.id.fab_edit);
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                updateUI(userProfile);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            // Gérer l'état de chargement si nécessaire
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                DialogUtils.showErrorMessage(requireContext(), error);
            }
        });

        viewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                navigateToAuth();
            }
        });
    }

    private void setupClickListeners() {
        btnModifierProfil.setOnClickListener(v -> navigateToEditProfile());
        fabEdit.setOnClickListener(v -> navigateToEditProfile());

        btnChangerMotDePasse.setOnClickListener(v -> {
            DialogUtils.showInfoMessage(requireContext(),
                    "Fonctionnalité à venir : Changement de mot de passe");
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void updateUI(ProfileViewModel.UserProfile userProfile) {
        // Header
        tvFullName.setText(userProfile.getUsername());
        tvUserFonction.setText(userProfile.getFonction());
        tvUserEmail.setText(userProfile.getEmail());

        // Info cards
        tvUsername.setText(userProfile.getUsername());
        tvFonction.setText(userProfile.getFonction());
        tvEmail.setText(userProfile.getEmail());
        tvTelephone.setText(userProfile.getPhone());

        // Load profile image if available
        if (userProfile.getProfileImageBase64() != null) {
            android.graphics.Bitmap bitmap = com.example.nexusdoc.ui.profile.repository.ProfileRepository
                    .base64ToBitmap(userProfile.getProfileImageBase64());
            if (bitmap != null) {
                profileAvatar.setImageBitmap(bitmap);
            }
        }
    }

    private void navigateToEditProfile() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_profile_to_modifierProfile);
    }

    private void showLogoutDialog() {
        DialogUtils.showLogoutDialog(requireContext(), new DialogUtils.DialogCallback() {
            @Override
            public void onPositive() {
                viewModel.logout();
            }
        });
    }

    private void navigateToAuth() {
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}