package com.example.nexusdoc.ui.profile;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nexusdoc.ui.profile.viewmodel.ProfileExpediteurViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.data.models.User;
import com.example.nexusdoc.ui.profile.repository.ProfileRepository;

public class ProfileExpediteurFragment extends Fragment {
    private ProfileExpediteurViewModel viewModel;
    private MaterialToolbar toolbar;
    private ShapeableImageView profileImage;
    private TextView userName;
    private TextView userEmail;
    private TextView userFunction;
    private TextView userPhone;
    private TextView userStatus;
    private TextView userLastActivity;
    private TextView userRole;
    private TextView userJoinDate;
    private MaterialButton btnSendMessage;
    private MaterialButton btnCall;
    private MaterialButton btnVideoCall;
    private MaterialButton btnBlock;
    private MaterialCardView cardContactInfo;
    private MaterialCardView cardAccountInfo;

    private String userId;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupérer l'ID utilisateur depuis les arguments
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_expediteur, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupListeners();

        if (userId != null) {
            viewModel.loadUserProfile(userId);
        }
    }

    private void initViews(View view) {
       // toolbar = view.findViewById(R.id.toolbar);
        profileImage = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);
        userFunction = view.findViewById(R.id.user_function);
        userPhone = view.findViewById(R.id.user_phone);
        userStatus = view.findViewById(R.id.user_status);
        userLastActivity = view.findViewById(R.id.user_last_activity);
        userRole = view.findViewById(R.id.user_role);
        userJoinDate = view.findViewById(R.id.user_join_date);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
        btnCall = view.findViewById(R.id.btn_call);
        btnVideoCall = view.findViewById(R.id.btn_video_call);
        btnBlock = view.findViewById(R.id.btn_block);
        cardContactInfo = view.findViewById(R.id.card_contact_info);
        cardAccountInfo = view.findViewById(R.id.card_account_info);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileExpediteurViewModel.class);
        currentUserId = viewModel.getCurrentUserId();

        // Observer le profil utilisateur
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserInterface(user);
            }
        });

        // Observer les erreurs
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observer l'état de chargement
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Afficher/masquer le loader si nécessaire
        });
    }

    private void setupListeners() {
        // Bouton retour
        /*toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });*/

        // Bouton envoyer un message
        btnSendMessage.setOnClickListener(v -> {
            openChatWithUser();
        });

        // Bouton appel
        btnCall.setOnClickListener(v -> {
            initiateCall(false);
        });

        // Bouton appel vidéo
        btnVideoCall.setOnClickListener(v -> {
            initiateCall(true);
        });

        // Bouton bloquer
        btnBlock.setOnClickListener(v -> {
            showBlockUserDialog();
        });
    }

    private void updateUserInterface(User user) {
        // Nom d'utilisateur
        userName.setText(user.getDisplayName());
        //toolbar.setTitle(user.getDisplayName());

        // Email
        userEmail.setText(user.getEmail());

        // Fonction
        if (user.getFonction() != null && !user.getFonction().isEmpty()) {
            userFunction.setText(user.getFonction());
            userFunction.setVisibility(View.VISIBLE);
        } else {
            userFunction.setVisibility(View.GONE);
        }

        // Téléphone
        if (user.getTelephone() != null && !user.getTelephone().isEmpty()) {
            userPhone.setText(user.getTelephone());
            userPhone.setVisibility(View.VISIBLE);
        } else {
            userPhone.setVisibility(View.GONE);
        }

        // Statut
        updateUserStatus(user);

        // Dernière activité
        userLastActivity.setText(user.getFormattedLastActivity());

        // Rôle
        userRole.setText(user.getRoleDisplay());

        // Date d'inscription
        userJoinDate.setText(formatJoinDate(user.getCreatedAt()));

        // Photo de profil
        loadProfileImage(user);

        // Masquer les boutons d'action si c'est le profil de l'utilisateur actuel
        if (user.getId().equals(currentUserId)) {
            btnSendMessage.setVisibility(View.GONE);
            btnCall.setVisibility(View.GONE);
            btnVideoCall.setVisibility(View.GONE);
            btnBlock.setVisibility(View.GONE);
        }
    }

    private void updateUserStatus(User user) {
        String status = user.getStatus();
        int statusColor;
        String statusText;

        switch (status) {
            case "online":
                statusColor = getResources().getColor(R.color.colorSuccess, null);
                statusText = "En ligne";
                break;
            case "away":
                statusColor = getResources().getColor(R.color.colorWarning, null);
                statusText = "Absent";
                break;
            case "offline":
            default:
                statusColor = getResources().getColor(R.color.black, null);
                statusText = "Hors ligne";
                break;
        }

        userStatus.setText(statusText);
        userStatus.setTextColor(statusColor);
    }

    private void loadProfileImage(User user) {
        if (user.getProfileImageBase64() != null && !user.getProfileImageBase64().isEmpty()) {
            Bitmap bitmap = ProfileRepository.base64ToBitmap(user.getProfileImageBase64());
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
            } else {
                profileImage.setImageResource(R.drawable.default_avatar);
            }
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }
    }

    private String formatJoinDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault());
        return "Membre depuis le " + sdf.format(new java.util.Date(timestamp));
    }

    private void openChatWithUser() {
        User user = viewModel.getUserProfile().getValue();
        if (user != null) {
            Bundle args = new Bundle();
            args.putString("userId", user.getId());
            args.putString("userName", user.getDisplayName());
            args.putString("userAvatar", user.getProfileImageBase64());

            // Navigation vers le fragment de chat
            androidx.navigation.NavController navController =
                    androidx.navigation.Navigation.findNavController(requireView());
            navController.navigate(R.id.action_profile_to_chat, args);
        }
    }

    private void initiateCall(boolean isVideoCall) {
        User user = viewModel.getUserProfile().getValue();
        if (user != null) {
            String callType = isVideoCall ? "vidéo" : "audio";
            Toast.makeText(getContext(),
                    "Appel " + callType + " vers " + user.getDisplayName(),
                    Toast.LENGTH_SHORT).show();

            // Ici vous pouvez implémenter la logique d'appel
            // Par exemple, intégration avec WebRTC, Agora, etc.
        }
    }

    private void showBlockUserDialog() {
        User user = viewModel.getUserProfile().getValue();
        if (user != null) {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Bloquer l'utilisateur")
                    .setMessage("Êtes-vous sûr de vouloir bloquer " + user.getDisplayName() + " ?")
                    .setPositiveButton("Bloquer", (dialog, which) -> {
                        viewModel.blockUser(user.getId());
                        Toast.makeText(getContext(),
                                user.getDisplayName() + " a été bloqué",
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        }
    }
}