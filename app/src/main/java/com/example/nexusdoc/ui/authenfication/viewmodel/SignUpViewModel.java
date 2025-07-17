package com.example.nexusdoc.ui.authenfication.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.nexusdoc.ui.authenfication.repository.AuthRepository;
import com.example.nexusdoc.ui.authenfication.model.RegistrationRequest;
import com.example.nexusdoc.ui.authenfication.model.RegistrationResult;

public class SignUpViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailVerificationSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailVerified = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();

    public SignUpViewModel(Application application) {
        super(application);
        this.authRepository = new AuthRepository(application.getApplicationContext());
    }

    public LiveData<RegistrationResult> getRegistrationResult() {
        return registrationResult;
    }

    public LiveData<Boolean> getEmailVerificationSent() {
        return emailVerificationSent;
    }

    public LiveData<Boolean> getEmailVerified() {
        return emailVerified;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void registerUser(String username, String email, String password, String phone, String fonction, Uri profileImageUri) {
        loading.setValue(true);

        RegistrationRequest request = new RegistrationRequest(username, email, password, phone, fonction, profileImageUri);

        authRepository.registerUser(request, new AuthRepository.RegistrationCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                registrationResult.setValue(RegistrationResult.success());
                sendEmailVerification();
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                registrationResult.setValue(RegistrationResult.error(errorMessage));
            }
        });
    }

    private void sendEmailVerification() {
        authRepository.sendEmailVerification(new AuthRepository.EmailVerificationCallback() {
            @Override
            public void onSent() {
                emailVerificationSent.setValue(true);
                checkEmailVerification();
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error silently or show message
            }
        });
    }

    private void checkEmailVerification() {
        authRepository.checkEmailVerification(new AuthRepository.EmailVerificationCallback() {
            @Override
            public void onSent() {
                emailVerified.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                // Email not verified yet - could implement periodic checking
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
}