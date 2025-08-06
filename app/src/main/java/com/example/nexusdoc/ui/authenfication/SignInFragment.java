package com.example.nexusdoc.ui.authenfication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nexusdoc.MainActivity;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.authenfication.animations.SignInAnimationController;
import com.example.nexusdoc.ui.authenfication.validator.LoginValidator;
import com.example.nexusdoc.ui.authenfication.viewmodel.SignInViewModel;
import com.example.nexusdoc.ui.utilitaires.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public class SignInFragment extends Fragment {

    private SignInViewModel viewModel;
    private SignInAnimationController animationController;
    private LoginValidator validator;

    // UI Components
    private MaterialButton loginButton;
    private MaterialTextView createAccount;
    private MaterialTextView forgotPassword;

    // Input Fields
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        initViews(view);
        setupObservers();
        setupClickListeners();
        animationController.startEntryAnimations();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(SignInViewModel.class);
        validator = new LoginValidator();
    }

    private void initViews(View view) {
        // Initialize input layouts
        emailLayout = view.findViewById(R.id.email_layout);
        passwordLayout = view.findViewById(R.id.password_layout);

        // Initialize input fields
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);

        // Initialize buttons and texts
        loginButton = view.findViewById(R.id.login_button);
        createAccount = view.findViewById(R.id.create_account);
        forgotPassword = view.findViewById(R.id.forgot_password);

        // Initialize animation controller
        animationController = new SignInAnimationController(view);
    }

    private void setupObservers() {
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            loginButton.setText("Se connecter");
            loginButton.setEnabled(true);

            if (result.isSuccess()) {
                handleLoginSuccess();
            } else {
                handleLoginError(result.getErrorMessage());
            }
        });

        viewModel.getPasswordResetSent().observe(getViewLifecycleOwner(), sent -> {
            if (sent) {
                DialogUtils.showSuccessMessage(requireContext(),
                        "Un email de réinitialisation a été envoyé");
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            loginButton.setEnabled(!loading);
            if (loading) {
                loginButton.setText("Connexion...");
            } else {
                loginButton.setText("Se connecter");
            }
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            animationController.animateButtonClick(loginButton);
            performLogin();
        });

        createAccount.setOnClickListener(v -> {
            animationController.animateTextClick(createAccount);
            navigateToSignUp();
        });

        forgotPassword.setOnClickListener(v -> {
            animationController.animateTextClick(forgotPassword);
            handleForgotPassword();
        });
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        LoginValidator.ValidationResult validation = validator.validate(email, password);

        if (!validation.isValid()) {
            DialogUtils.showErrorMessage(requireContext(), validation.getErrorMessage());
            return;
        }

        viewModel.loginUser(email, password);
    }

    private void handleForgotPassword() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            DialogUtils.showInfoMessage(requireContext(),
                    "Veuillez entrer votre email pour réinitialiser le mot de passe");
            return;
        }

        DialogUtils.showConfirmationDialog(requireContext(),
                "Réinitialiser le mot de passe",
                "Envoyer un email de réinitialisation à " + email + " ?",
                "Envoyer",
                "Annuler",
                new DialogUtils.DialogCallback() {
                    @Override
                    public void onPositive() {
                        viewModel.resetPassword(email);
                    }
                });
    }

    private void handleLoginSuccess() {
        DialogUtils.showSuccessMessage(requireContext(), "Connexion réussie !");
        navigateToMainActivity();
    }

    private void handleLoginError(String errorMessage) {
        DialogUtils.showErrorMessage(requireContext(), errorMessage);
    }

    private void navigateToSignUp() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_signIn_to_signUp);
    }

    private void navigateToMainActivity() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_signIn_to_main);
    }
}