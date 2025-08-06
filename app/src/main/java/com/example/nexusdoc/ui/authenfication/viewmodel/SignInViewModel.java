package com.example.nexusdoc.ui.authenfication.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.authenfication.model.AuthLoginRequest;
import com.example.nexusdoc.ui.authenfication.model.AuthResult;
import com.example.nexusdoc.ui.authenfication.repository.DualAuthRepository;
import com.example.nexusdoc.ui.authenfication.repository.FirebaseAuthRepository;
import com.example.nexusdoc.ui.authenfication.validator.AuthValidator;
import com.example.nexusdoc.ui.authenfication.validator.ValidationResult;

public class SignInViewModel extends AndroidViewModel {

    private final DualAuthRepository authRepository;
    private final AuthValidator validator;

    private final MutableLiveData<AuthResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public SignInViewModel(Application application) {
        super(application);
        this.authRepository = new DualAuthRepository(application.getApplicationContext());
        this.validator = new AuthValidator();
    }

    public LiveData<AuthResult> getLoginResult() { return loginResult; }
    public LiveData<Boolean> getPasswordResetSent() { return passwordResetSent; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public void loginUser(String email, String password) {
        ValidationResult validation = validator.validateLogin(email, password);

        if (!validation.isValid()) {
            loginResult.setValue(AuthResult.error(validation.getErrorMessage()));
            return;
        }

        loading.setValue(true);
        statusMessage.setValue("Connexion en cours...");

        AuthLoginRequest request = new AuthLoginRequest(email, password);

        authRepository.signIn(request, new DualAuthRepository.DualAuthCallback() {
            @Override
            public void onSuccess(AuthResult.AuthProvider successfulProvider) {
                loading.setValue(false);
                statusMessage.setValue("Connexion réussie sur " + getProviderName(successfulProvider));
                loginResult.setValue(AuthResult.success(successfulProvider));
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                statusMessage.setValue("Échec de la connexion");
                loginResult.setValue(AuthResult.error(errorMessage));
            }

            @Override
            public void onPartialSuccess(AuthResult.AuthProvider successfulProvider, String failedProviderError) {
                loading.setValue(false);
                statusMessage.setValue("Connexion partielle sur " + getProviderName(successfulProvider));
                loginResult.setValue(AuthResult.success(successfulProvider));
            }
        });
    }

    public void resetPassword(String email) {
        if (email.trim().isEmpty()) {
            loginResult.setValue(AuthResult.error("Veuillez entrer votre email"));
            return;
        }

        authRepository.resetPassword(email, new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                passwordResetSent.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                loginResult.setValue(AuthResult.error(errorMessage));
            }
        });
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