package com.example.nexusdoc.ui.authenfication.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.authenfication.model.LoginRequest;
import com.example.nexusdoc.ui.authenfication.model.LoginResult;
import com.example.nexusdoc.ui.authenfication.repository.AuthRepository;

public class SignInViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();

    public SignInViewModel(Application application) {
        super(application);
        this.authRepository = new AuthRepository(application.getApplicationContext());
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getPasswordResetSent() {
        return passwordResetSent;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loginUser(String email, String password) {
        loading.setValue(true);

        LoginRequest request = new LoginRequest(email, password);

        authRepository.loginUser(request, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                loginResult.setValue(LoginResult.success());
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                loginResult.setValue(LoginResult.error(errorMessage));
            }
        });
    }

    public void resetPassword(String email) {
        authRepository.resetPassword(email, new AuthRepository.PasswordResetCallback() {
            @Override
            public void onSent() {
                passwordResetSent.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                loginResult.setValue(LoginResult.error(errorMessage));
            }
        });
    }
}