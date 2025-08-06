package com.example.nexusdoc.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.ui.chat.adapter.MessageAdapter;
import com.example.nexusdoc.ui.chat.utils.AudioRecorderManager;
import com.example.nexusdoc.ui.chat.utils.FileUtils;
import com.example.nexusdoc.ui.profile.repository.ProfileRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.data.models.Message;
import com.example.nexusdoc.ui.data.models.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements MessageAdapter.OnMessageClickListener {
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_DOCUMENT = 102;
    private static final int REQUEST_PERMISSIONS = 103;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

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

    // Reply functionality
    private LinearLayout replyPreviewContainer;
    private TextView replyPreviewSender;
    private TextView replyPreviewMessage;
    private ImageView btnCancelReply;
    private Message replyToMessage = null;

    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private Handler typingHandler;
    private Runnable typingRunnable;
    private boolean isTyping = false;

    // Audio recording
    private AudioRecorderManager audioManager;
    private boolean isRecordingAudio = false;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupérer les arguments
        if (getArguments() != null) {
            otherUserId = getArguments().getString("userId");
            otherUserName = getArguments().getString("userName");
            otherUserAvatar = getArguments().getString("userAvatar");
        }

        audioManager = new AudioRecorderManager(getContext());
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
        checkPermissions();

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

        // Reply preview views
        replyPreviewContainer = view.findViewById(R.id.reply_preview_container);
        replyPreviewSender = view.findViewById(R.id.reply_preview_sender);
        replyPreviewMessage = view.findViewById(R.id.reply_preview_message);
        btnCancelReply = view.findViewById(R.id.btn_cancel_reply);

        typingHandler = new Handler(Looper.getMainLooper());
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
                handleVoiceRecording();
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

        // Reply cancel button
        if (btnCancelReply != null) {
            btnCancelReply.setOnClickListener(v -> cancelReplyMode());
        }
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(),
                    permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
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
            loadBase64Avatar(otherUserAvatar, avatarImage);
        } else {
            avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    private void updateUserInfo(User user) {
        contactName.setText(user.getUsername());
        contactStatus.setText(getStatusText(user.getStatus()));

        if (user.getProfileImageBase64() != null) {
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
        if (replyToMessage != null) {
            viewModel.replyToMessage(replyToMessage, message);
            cancelReplyMode();
        } else {
            viewModel.sendMessage(message);
        }

        // Arrêter le statut de frappe
        if (isTyping) {
            isTyping = false;
            viewModel.setTypingStatus(false);
        }
    }

    private void handleVoiceRecording() {
        if (!isRecordingAudio) {
            startVoiceRecording();
        } else {
            stopVoiceRecording();
        }
    }

    private void startVoiceRecording() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission d'enregistrement audio requise", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            audioManager.startRecording();
            isRecordingAudio = true;
            btnSendVoice.setIcon(getContext().getDrawable(R.drawable.ic_stop));
            btnSendVoice.setBackgroundTintList(getContext().getColorStateList(R.color.colorError));
            Toast.makeText(getContext(), "Enregistrement en cours...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoiceRecording() {
        try {
            String audioPath = audioManager.stopRecording();
            isRecordingAudio = false;
            btnSendVoice.setIcon(getContext().getDrawable(R.drawable.ic_mic));
            btnSendVoice.setBackgroundTintList(getContext().getColorStateList(R.color.primary));

            if (audioPath != null) {
                sendAudioMessage(audioPath);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de l'arrêt de l'enregistrement", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendAudioMessage(String audioPath) {
        try {
            String base64Audio = FileUtils.fileToBase64(audioPath);
            long fileSize = FileUtils.getFileSize(audioPath);

            Message.FileAttachment audioAttachment = new Message.FileAttachment(
                    "audio_" + System.currentTimeMillis() + ".3gp",
                    base64Audio,
                    "audio/3gp",
                    fileSize
            );

            List<Message.FileAttachment> attachments = new ArrayList<>();
            attachments.add(audioAttachment);

            viewModel.sendAudioMessage(attachments);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de l'envoi de l'audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAttachmentOptions() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_attachment, null);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });

        view.findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        view.findViewById(R.id.btn_document).setOnClickListener(v -> {
            dialog.dismiss();
            openDocumentPicker();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission caméra requise", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_DOCUMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        String base64Image = bitmapToBase64(bitmap);
                        viewModel.sendImageMessage(base64Image, "");
                    }
                    break;

                case REQUEST_GALLERY:
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        String base64Image = uriToBase64(imageUri);
                        if (base64Image != null) {
                            viewModel.sendImageMessage(base64Image, "");
                        }
                    }
                    break;

                case REQUEST_DOCUMENT:
                    if (data != null && data.getData() != null) {
                        Uri fileUri = data.getData();
                        sendFileMessage(fileUri);
                    }
                    break;
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmapToBase64(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendFileMessage(Uri fileUri) {
        try {
            String fileName = FileUtils.getFileName(getContext(), fileUri);
            String fileType = getContext().getContentResolver().getType(fileUri);
            long fileSize = FileUtils.getFileSize(getContext(), fileUri);

            // Convertir le fichier en base64 pour Firebase
            String base64File = FileUtils.uriToBase64(getContext(), fileUri);

            if (base64File != null) {
                Message.FileAttachment attachment = new Message.FileAttachment(
                        fileName, base64File, fileType, fileSize
                );

                List<Message.FileAttachment> attachments = new ArrayList<>();
                attachments.add(attachment);

                viewModel.sendFileMessage(attachments, "");
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de l'envoi du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMoreOptions(View view) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.chat_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_view_profile) {
                openUserProfile();
                return true;
            } else if (id == R.id.action_clear_chat) {
                showClearChatConfirmation();
                return true;
            } else if (id == R.id.action_block_user) {
                showBlockUserConfirmation();
                return true;
            } else {
                return false;
            }
        });

        popup.show();
    }

    private void openUserProfile() {
        try {
            Bundle args = new Bundle();
            args.putString("userId", otherUserId);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_chat_to_profile, args);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Impossible d'ouvrir le profil", Toast.LENGTH_SHORT).show();
        }
    }

    private void showClearChatConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Effacer le chat")
                .setMessage("Êtes-vous sûr de vouloir effacer tous les messages ?")
                .setPositiveButton("Effacer", (dialog, which) -> {
                    viewModel.clearChat(otherUserId);
                    Toast.makeText(getContext(), "Chat effacé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showBlockUserConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Bloquer l'utilisateur")
                .setMessage("Êtes-vous sûr de vouloir bloquer " + otherUserName + " ?")
                .setPositiveButton("Bloquer", (dialog, which) -> {
                    viewModel.blockUser(otherUserId);
                    Toast.makeText(getContext(), "Utilisateur bloqué", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Reply functionality
    private void startReplyMode(Message message) {
        replyToMessage = message;

        if (replyPreviewContainer != null) {
            replyPreviewContainer.setVisibility(View.VISIBLE);
            replyPreviewSender.setText("Répondre à " + message.getSenderName());
            replyPreviewMessage.setText(message.getContent());
        }

        // Focus sur le champ de saisie
        editMessage.requestFocus();
    }

    private void cancelReplyMode() {
        replyToMessage = null;
        if (replyPreviewContainer != null) {
            replyPreviewContainer.setVisibility(View.GONE);
        }
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
        openImageFullScreen(message);
    }

    @Override
    public void onFileClick(Message message) {
        // Ouvrir le fichier
        openFile(message);
    }

    @Override
    public void onAudioClick(Message message) {
        // Jouer l'audio
        playAudio(message);
    }

    private void openImageFullScreen(Message message) {
        // Implémenter l'ouverture d'image en plein écran
        Toast.makeText(getContext(), "Ouvrir l'image en plein écran", Toast.LENGTH_SHORT).show();
    }

    private void openFile(Message message) {
        // Implémenter l'ouverture de fichier
        Toast.makeText(getContext(), "Ouvrir le fichier", Toast.LENGTH_SHORT).show();
    }

    private void playAudio(Message message) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                Message.FileAttachment audioAttachment = message.getAttachments().get(0);
                String audioPath = FileUtils.base64ToTempFile(getContext(),
                        audioAttachment.getFileUrl(), "temp_audio.3gp");

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioPath);
                mediaPlayer.prepare();
                mediaPlayer.start();

                Toast.makeText(getContext(), "Lecture audio...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de la lecture audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessageContextMenu(Message message) {
        String[] options = {"Répondre", "Copier", "Supprimer", "Transférer"};

        new AlertDialog.Builder(getContext())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Répondre
                            startReplyMode(message);
                            break;
                        case 1: // Copier
                            copyMessageToClipboard(message);
                            break;
                        case 2: // Supprimer
                            showDeleteConfirmation(message);
                            break;
                        case 3: // Transférer
                            // Implémenter le transfert
                            Toast.makeText(getContext(), "Transférer le message", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }

    private void copyMessageToClipboard(Message message) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Message", message.getContent());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Message copié", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation(Message message) {
        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer le message")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce message ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    viewModel.deleteMessage(message.getId());
                    Toast.makeText(getContext(), "Message supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
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

        // Libérer les ressources audio
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (audioManager != null) {
            audioManager.release();
        }
    }
}