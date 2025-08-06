package com.example.nexusdoc.ui.profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.example.nexusdoc.ui.data.models.User;
import com.example.nexusdoc.ui.team.repository.TeamRepository;
import com.example.nexusdoc.ui.chat.repository.ChatRepository;

public class ProfileExpediteurViewModel extends ViewModel {
    private TeamRepository teamRepository;
    private ChatRepository chatRepository;
    private FirebaseAuth auth;

    private MutableLiveData<User> userProfile;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;

    public ProfileExpediteurViewModel() {
        teamRepository = TeamRepository.getInstance();
        chatRepository = ChatRepository.getInstance();
        auth = FirebaseAuth.getInstance();

        userProfile = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
    }

    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadUserProfile(String userId) {
        isLoading.setValue(true);

        teamRepository.getUserById(userId, new TeamRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                isLoading.setValue(false);
                userProfile.setValue(user);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Erreur lors du chargement du profil: " + error);
            }
        });
    }

    public void blockUser(String userId) {
        chatRepository.blockUser(userId);
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}