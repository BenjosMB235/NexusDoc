package com.example.nexusdoc.ui.authenfication.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.authenfication.model.AuthRegistrationRequest;
import com.example.nexusdoc.ui.authenfication.model.AuthResult;
import com.example.nexusdoc.ui.authenfication.repository.DualAuthRepository;
import com.example.nexusdoc.ui.authenfication.repository.FirebaseAuthRepository;
import com.example.nexusdoc.ui.authenfication.validator.AuthValidator;
import com.example.nexusdoc.ui.authenfication.validator.ValidationResult;

public class SignUpViewModel extends AndroidViewModel {

    private final DualAuthRepository authRepository;
    private final AuthValidator validator;

    private final MutableLiveData<AuthResult> registrationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailVerificationSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public SignUpViewModel(Application application) {
        super(application);
        this.authRepository = new DualAuthRepository(application.getApplicationContext());
        this.validator = new AuthValidator();
    }

    public LiveData<AuthResult> getRegistrationResult() { return registrationResult; }
    public LiveData<Boolean> getEmailVerificationSent() { return emailVerificationSent; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public void registerUser(String username, String email, String password,
                             String confirmPassword, String phone, String fonction, Uri profileImageUri) {

        ValidationResult validation = validator.validateRegistration(
                username, email, password, confirmPassword, phone, fonction
        );

        if (!validation.isValid()) {
            registrationResult.setValue(AuthResult.error(validation.getErrorMessage()));
            return;
        }

        loading.setValue(true);
        statusMessage.setValue("Inscription en cours...");

        AuthRegistrationRequest request = new AuthRegistrationRequest(
                username, email, password, phone, fonction, profileImageUri
        );

        authRepository.signUp(request, new DualAuthRepository.DualAuthCallback() {
            @Override
            public void onSuccess(AuthResult.AuthProvider successfulProvider) {
                loading.setValue(false);
                statusMessage.setValue("Inscription réussie sur " + getProviderName(successfulProvider));
                registrationResult.setValue(AuthResult.success(successfulProvider));
                sendEmailVerification();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                statusMessage.setValue("Échec de l'inscription");
                registrationResult.setValue(AuthResult.error(errorMessage));
            }

            @Override
            public void onPartialSuccess(AuthResult.AuthProvider successfulProvider, String failedProviderError) {
                loading.setValue(false);
                statusMessage.setValue("Inscription partielle sur " + getProviderName(successfulProvider));
                registrationResult.setValue(AuthResult.success(successfulProvider));
                sendEmailVerification();
            }
        });
    }

    private void sendEmailVerification() {
        authRepository.sendEmailVerification(new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                emailVerificationSent.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                // Erreur silencieuse pour la vérification email
                statusMessage.setValue("Inscription réussie (vérification email échouée)");
            }
        });
    }

    public void openEmailClient(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://mail.google.com"));
            activity.startActivity(intent);
        }
    }

    private String getProviderName(AuthResult.AuthProvider provider) {
        switch (provider) {
            case FIREBASE: return "Firebase";
            case SUPABASE: return "Supabase";
            case BOTH: return "Firebase et Supabase";
            default: return "Inconnu";
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        authRepository.cleanup();
    }
}