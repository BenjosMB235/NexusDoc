package com.example.nexusdoc.ui.profile.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.profile.repository.ProfileRepository;
import com.example.nexusdoc.ui.utilitaires.PreferenceManager;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository profileRepository;
    private final PreferenceManager preferenceManager;

    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();

    public ProfileViewModel(Application application) {
        super(application);
        this.profileRepository = new ProfileRepository(application.getApplicationContext());
        this.preferenceManager = new PreferenceManager(application.getApplicationContext());
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLogoutSuccess() {
        return logoutSuccess;
    }

    public void loadUserProfile() {
        loading.setValue(true);

        profileRepository.getCurrentUserProfile(new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                loading.setValue(false);
                userProfile.setValue(profile);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void logout() {
        profileRepository.logout(new ProfileRepository.LogoutCallback() {
            @Override
            public void onSuccess() {
                logoutSuccess.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
            }
        });
    }

    public static class UserProfile {
        private final String username;
        private final String fonction;
        private final String email;
        private final String phone;
        private final String profileImageBase64;

        public UserProfile(String username, String fonction, String email, String phone, String profileImageBase64) {
            this.username = username;
            this.fonction = fonction;
            this.email = email;
            this.phone = phone;
            this.profileImageBase64 = profileImageBase64;
        }

        public String getUsername() { return username; }
        public String getFonction() { return fonction; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getProfileImageBase64() { return profileImageBase64; }
    }
}