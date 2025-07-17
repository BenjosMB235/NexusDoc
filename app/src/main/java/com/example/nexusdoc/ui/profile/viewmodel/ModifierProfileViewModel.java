package com.example.nexusdoc.ui.profile.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.profile.model.UpdateProfileRequest;
import com.example.nexusdoc.ui.profile.model.UpdateProfileResult;
import com.example.nexusdoc.ui.profile.repository.ProfileRepository;

public class ModifierProfileViewModel extends AndroidViewModel {

    private final ProfileRepository profileRepository;

    private final MutableLiveData<UserData> currentUserData = new MutableLiveData<>();
    private final MutableLiveData<UpdateProfileResult> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ModifierProfileViewModel(Application application) {
        super(application);
        this.profileRepository = new ProfileRepository(application.getApplicationContext());
    }

    public LiveData<UserData> getCurrentUserData() {
        return currentUserData;
    }

    public LiveData<UpdateProfileResult> getUpdateResult() {
        return updateResult;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadCurrentUserData() {
        loading.setValue(true);

        profileRepository.getCurrentUserProfile(new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(ProfileViewModel.UserProfile profile) {
                loading.setValue(false);
                UserData userData = new UserData(
                        profile.getUsername(),
                        profile.getFonction(),
                        profile.getEmail(),
                        profile.getPhone(),
                        profile.getProfileImageBase64()
                );
                currentUserData.setValue(userData);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void updateProfile(UpdateProfileRequest request) {
        loading.setValue(true);

        profileRepository.updateUserProfile(request, new ProfileRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                updateResult.setValue(UpdateProfileResult.success());
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                updateResult.setValue(UpdateProfileResult.error(errorMessage));
            }
        });
    }

    public static class UserData {
        private final String username;
        private final String fonction;
        private final String email;
        private final String phone;
        private final String profileImageBase64;

        public UserData(String username, String fonction, String email, String phone, String profileImageBase64) {
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