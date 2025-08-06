package com.example.nexusdoc.ui.authenfication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import androidx.cardview.widget.CardView;
import com.example.nexusdoc.MainActivity;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.authenfication.animations.SignUpAnimationController;
import com.example.nexusdoc.ui.authenfication.validator.RegistrationValidator;
import com.example.nexusdoc.ui.authenfication.viewmodel.SignUpViewModel;
import com.example.nexusdoc.ui.utilitaires.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import android.widget.ImageView;

public class SignUpFragment extends Fragment {

    private SignUpViewModel viewModel;
    private SignUpAnimationController animationController;
    private RegistrationValidator validator;

    // Image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    // UI Components
    private MaterialButton registerButton;
    private MaterialTextView loginLink;
    private MaterialTextView addPhotoText;
    private CardView profileImageContainer;
    private ImageView profileImageView;

    // Input Fields
    private TextInputLayout usernameLayout, emailLayout, fonctionLayout,
            passwordLayout, confirmPasswordLayout, phoneLayout;
    private TextInputEditText usernameInput, emailInput, fonctionInput,
            passwordInput, confirmPasswordInput, phoneInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        setupImagePicker();
        initViews(view);
        setupObservers();
        setupClickListeners();
        animationController.startEntryAnimations();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        validator = new RegistrationValidator();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            profileImageView.setImageURI(selectedImageUri);
                            addPhotoText.setText("Changer la photo");
                            animationController.animateImageSelection();
                        }
                    }
                }
        );
    }

    private void initViews(View view) {
        // Initialize input layouts
        usernameLayout = view.findViewById(R.id.username_layout);
        emailLayout = view.findViewById(R.id.email_layout);
        fonctionLayout = view.findViewById(R.id.fonction_layout);
        passwordLayout = view.findViewById(R.id.password_layout);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);
        phoneLayout = view.findViewById(R.id.phone_layout);

        // Initialize input fields
        usernameInput = view.findViewById(R.id.username_input);
        emailInput = view.findViewById(R.id.email_input);
        fonctionInput = view.findViewById(R.id.fonction_input);
        passwordInput = view.findViewById(R.id.password_input);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);
        phoneInput = view.findViewById(R.id.phone_input);

        // Initialize buttons and texts
        registerButton = view.findViewById(R.id.register_button);
        loginLink = view.findViewById(R.id.login_link);
        addPhotoText = view.findViewById(R.id.add_photo_text);
        profileImageContainer = view.findViewById(R.id.profile_image_container);
        profileImageView = view.findViewById(R.id.profile_image);

        // Initialize animation controller
        animationController = new SignUpAnimationController(view);
    }

    private void setupObservers() {
        viewModel.getRegistrationResult().observe(getViewLifecycleOwner(), result -> {
            registerButton.setText("S'inscrire");
            registerButton.setEnabled(true);

            if (result.isSuccess()) {
                handleRegistrationSuccess();
            } else {
                handleRegistrationError(result.getErrorMessage());
            }
        });

        viewModel.getEmailVerificationSent().observe(getViewLifecycleOwner(), sent -> {
            if (sent) {
                DialogUtils.showSuccessMessage(requireContext(),
                        "Un email de vérification a été envoyé");
                viewModel.openEmailClient(requireActivity());
            }
        });

       /* viewModel.getEmailVerified().observe(getViewLifecycleOwner(), verified -> {
            if (verified) {
                navigateToMainActivity();
            }
        });*/

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            registerButton.setEnabled(!loading);
            if (loading) {
                registerButton.setText("Inscription...");
            } else {
                registerButton.setText("S'inscrire");
            }
        });
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> {
            animationController.animateButtonClick(registerButton);
            performRegistration();
        });

        loginLink.setOnClickListener(v -> {
            animationController.animateTextClick(loginLink);
            navigateToSignIn();
        });

        addPhotoText.setOnClickListener(v -> {
            animationController.animateTextClick(addPhotoText);
            openImagePicker();
        });

        profileImageContainer.setOnClickListener(v -> {
            animationController.animateImageClick(profileImageContainer);
            openImagePicker();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void performRegistration() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String phone = phoneInput.getText().toString().trim();
        String fonction = fonctionInput.getText().toString().trim();

        // Include selected image URI
        RegistrationValidator.ValidationResult validation = validator.validate(
                username, email, password, confirmPassword, phone, fonction
        );

        if (!validation.isValid()) {
            DialogUtils.showErrorMessage(requireContext(), validation.getErrorMessage());
            return;
        }

        viewModel.registerUser(username, email, password,confirmPassword, phone, fonction, selectedImageUri);
    }

    private void handleRegistrationSuccess() {
        DialogUtils.showSuccessMessage(requireContext(), "Inscription réussie !");
    }

    private void handleRegistrationError(String errorMessage) {
        DialogUtils.showErrorMessage(requireContext(), errorMessage);
    }

    private void navigateToSignIn() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_signUp_to_signIn);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}