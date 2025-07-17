package com.example.nexusdoc.ui.chat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.ui.chat.adapter.MessageAdapter;
import com.example.nexusdoc.ui.profile.repository.ProfileRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.data.models.Message;
import com.example.nexusdoc.ui.data.models.User;

public class ChatFragment extends Fragment implements MessageAdapter.OnMessageClickListener {
    private ChatViewModel viewModel;
    private MessageAdapter adapter;
    private RecyclerView recyclerMessages;
    private TextInputEditText editMessage;
    private MaterialButton btnSendVoice;
    private MaterialButton btnAttach;
    private MaterialButton btnCall;
    private MaterialButton btnVideoCall;
    private MaterialButton btnMore;
    private FloatingActionButton fabScrollDown;
    private LinearLayout typingIndicator;
    private TextView contactName;
    private TextView contactStatus;
    private ShapeableImageView avatarImage;
    private MaterialToolbar toolbar;

    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private Handler typingHandler;
    private Runnable typingRunnable;
    private boolean isTyping = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupérer les arguments
        if (getArguments() != null) {
            otherUserId = getArguments().getString("userId");
            otherUserName = getArguments().getString("userName");
            otherUserAvatar = getArguments().getString("userAvatar");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupListeners();

        // Initialiser le chat
        if (otherUserId != null) {
            viewModel.initChat(otherUserId);
            setupUserInfo();
        }
    }

    private void initViews(View view) {
        recyclerMessages = view.findViewById(R.id.recycler_messages);
        editMessage = view.findViewById(R.id.edit_message);
        btnSendVoice = view.findViewById(R.id.btn_send_voice);
        btnAttach = view.findViewById(R.id.btn_attach);
        btnCall = view.findViewById(R.id.btn_call);
        btnVideoCall = view.findViewById(R.id.btn_video_call);
        btnMore = view.findViewById(R.id.btn_more);
        fabScrollDown = view.findViewById(R.id.fab_scroll_down);
        typingIndicator = view.findViewById(R.id.typing_indicator);
        contactName = view.findViewById(R.id.contact_name);
        contactStatus = view.findViewById(R.id.contact_status);
        avatarImage = view.findViewById(R.id.avatar_image);
        toolbar = view.findViewById(R.id.toolbar);

        typingHandler = new Handler();
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(viewModel.getCurrentUserId(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(adapter);

        // Faire défiler automatiquement vers le bas lors de nouveaux messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (positionStart >= adapter.getItemCount() - 2) {
                    recyclerMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });

        // Gérer l'affichage du FAB pour faire défiler vers le bas
        recyclerMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = adapter.getItemCount();

                    if (lastVisiblePosition < totalItemCount - 3) {
                        fabScrollDown.show();
                    } else {
                        fabScrollDown.hide();
                    }
                }
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Observer les messages
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                adapter.setMessages(messages);
            }
        });

        // Observer les erreurs
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observer l'état de frappe
        viewModel.getIsTyping().observe(getViewLifecycleOwner(), isTyping -> {
            if (isTyping != null) {
                typingIndicator.setVisibility(isTyping ? View.VISIBLE : View.GONE);
            }
        });

        // Observer les utilisateurs
        viewModel.getOtherUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserInfo(user);
            }
        });
    }

    private void setupListeners() {
        // Bouton retour
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Gestion de la saisie
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButton(s.toString());
                handleTypingStatus(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bouton d'envoi/micro
        btnSendVoice.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                editMessage.setText("");
            } else {
                // Enregistrement audio
                startVoiceRecording();
            }
        });

        // Bouton pièce jointe
        btnAttach.setOnClickListener(v -> {
            showAttachmentOptions();
        });

        // Boutons d'appel
        btnCall.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Appel audio", Toast.LENGTH_SHORT).show();
        });

        btnVideoCall.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Appel vidéo", Toast.LENGTH_SHORT).show();
        });

        btnMore.setOnClickListener(v -> {
            showMoreOptions(v);
        });

        // FAB pour faire défiler vers le bas
        fabScrollDown.setOnClickListener(v -> {
            recyclerMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        });
    }

    private void loadBase64Avatar(String base64Image, ImageView target) {
        if (base64Image != null && !base64Image.isEmpty()) {
            Bitmap bitmap = ProfileRepository.base64ToBitmap(base64Image);
            if (bitmap != null) {
                target.setImageBitmap(bitmap);
            } else {
                target.setImageResource(R.drawable.default_avatar);
            }
        } else {
            target.setImageResource(R.drawable.default_avatar);
        }
    }


    private void setupUserInfo() {
        if (otherUserName != null) {
            contactName.setText(otherUserName);
        }

        if (otherUserAvatar != null) {
            // Charger l'avatar avec Glide
            /*com.bumptech.glide.Glide.with(this)
                    .load(otherUserAvatar)
                    .placeholder(R.drawable.default_avatar)
                    .into(avatarImage);*/
            // Si l'avatar est en base64, charge-le en Bitmap
            loadBase64Avatar(otherUserAvatar, avatarImage);
        } else {
            avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    private void updateUserInfo(User user) {
        contactName.setText(user.getUsername());
        contactStatus.setText(getStatusText(user.getStatus()));

        if (user.getProfileImageBase64() != null) {
           /* com.bumptech.glide.Glide.with(this)
                    .load(user.getProfileImageBase64())
                    .placeholder(R.drawable.default_avatar)
                    .into(avatarImage);*/
            loadBase64Avatar(user.getProfileImageBase64(), avatarImage);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "online":
                return "En ligne";
            case "away":
                return "Absent";
            case "offline":
                return "Hors ligne";
            default:
                return "Statut inconnu";
        }
    }

    private void updateSendButton(String message) {
        if (message.trim().isEmpty()) {
            btnSendVoice.setIcon(getContext().getDrawable(R.drawable.ic_mic));
        } else {
            btnSendVoice.setIcon(getContext().getDrawable(R.drawable.ic_send));
        }
    }

    private void handleTypingStatus(String message) {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        if (!message.trim().isEmpty() && !isTyping) {
            isTyping = true;
            viewModel.setTypingStatus(true);
        }

        typingRunnable = () -> {
            if (isTyping) {
                isTyping = false;
                viewModel.setTypingStatus(false);
            }
        };

        typingHandler.postDelayed(typingRunnable, 1000);
    }

    private void sendMessage(String message) {
        viewModel.sendMessage(message);

        // Arrêter le statut de frappe
        if (isTyping) {
            isTyping = false;
            viewModel.setTypingStatus(false);
        }
    }

    private void startVoiceRecording() {
        // Implémenter l'enregistrement audio
        Toast.makeText(getContext(), "Fonction d'enregistrement audio à implémenter", Toast.LENGTH_SHORT).show();
    }

    private void showAttachmentOptions() {
        // Afficher les options de pièce jointe
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_attachment, null);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            dialog.dismiss();
            // Ouvrir l'appareil photo
            Toast.makeText(getContext(), "Ouvrir l'appareil photo", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            // Ouvrir la galerie
            Toast.makeText(getContext(), "Ouvrir la galerie", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_document).setOnClickListener(v -> {
            dialog.dismiss();
            // Ouvrir le sélecteur de fichiers
            Toast.makeText(getContext(), "Sélectionner un fichier", Toast.LENGTH_SHORT).show();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showMoreOptions(View view) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.chat_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_view_profile) {
                // Voir le profil
                Toast.makeText(getContext(), "Voir le profil", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_clear_chat) {
                // Effacer le chat
                showClearChatConfirmation();
                return true;
            } else if (id == R.id.action_block_user) {
                // Bloquer l'utilisateur
                showBlockUserConfirmation();
                return true;
            } else {
                return false;
            }
        });

        popup.show();
    }
    private void showClearChatConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Effacer le chat")
                .setMessage("Êtes-vous sûr de vouloir effacer tous les messages ?")
                .setPositiveButton("Effacer", (dialog, which) -> {
                    // Implémenter l'effacement du chat
                    Toast.makeText(getContext(), "Chat effacé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showBlockUserConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Bloquer l'utilisateur")
                .setMessage("Êtes-vous sûr de vouloir bloquer " + otherUserName + " ?")
                .setPositiveButton("Bloquer", (dialog, which) -> {
                    // Implémenter le blocage
                    Toast.makeText(getContext(), "Utilisateur bloqué", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Implémentation des callbacks de MessageAdapter
    @Override
    public void onMessageClick(Message message) {
        // Gérer le clic sur un message
    }

    @Override
    public void onMessageLongClick(Message message) {
        // Afficher le menu contextuel du message
        showMessageContextMenu(message);
    }

    @Override
    public void onImageClick(Message message) {
        // Ouvrir l'image en plein écran
        Toast.makeText(getContext(), "Ouvrir l'image", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileClick(Message message) {
        // Ouvrir le fichier
        Toast.makeText(getContext(), "Ouvrir le fichier", Toast.LENGTH_SHORT).show();
    }

    private void showMessageContextMenu(Message message) {
        String[] options = {"Répondre", "Copier", "Supprimer", "Transférer"};

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Répondre
                            // Implémenter la réponse
                            Toast.makeText(getContext(), "Répondre au message", Toast.LENGTH_SHORT).show();
                            break;
                        case 1: // Copier
                            // Copier le message
                            android.content.ClipboardManager clipboard =
                                    (android.content.ClipboardManager) getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Message", message.getContent());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getContext(), "Message copié", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Supprimer
                            // Supprimer le message
                            Toast.makeText(getContext(), "Supprimer le message", Toast.LENGTH_SHORT).show();
                            break;
                        case 3: // Transférer
                            // Transférer le message
                            Toast.makeText(getContext(), "Transférer le message", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (typingHandler != null && typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        // Arrêter le statut de frappe
        if (isTyping) {
            viewModel.setTypingStatus(false);
        }
    }
}