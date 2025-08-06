package com.example.nexusdoc.ui.chat.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.data.models.Message;
import com.example.nexusdoc.ui.profile.repository.ProfileRepository;
import com.example.nexusdoc.ui.team.repository.TeamRepository;
import com.example.nexusdoc.ui.data.models.User;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MESSAGE_SENT = 1;
    private static final int TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;
    private OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
        void onMessageLongClick(Message message);
        void onImageClick(Message message);
        void onFileClick(Message message);
        void onAudioClick(Message message);
    }

    public MessageAdapter(String currentUserId, OnMessageClickListener listener) {
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.isSentByCurrentUser(currentUserId) ? TYPE_MESSAGE_SENT : TYPE_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MESSAGE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder pour les messages envoyés
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private MaterialTextView messageText;
        private MaterialTextView messageTime;
        private ShapeableImageView messageStatus;
        private ShapeableImageView messageImage;
        private MaterialCardView fileContainer;
        private MaterialTextView fileName;
        private MaterialTextView fileSize;
        private MaterialCardView replyContainer;
        private MaterialTextView replySender;
        private MaterialTextView replyMessage;
        private MaterialCardView audioContainer;
        private MaterialButton audioPlayButton;
        private MaterialTextView audioDuration;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            messageStatus = itemView.findViewById(R.id.message_status);
            messageImage = itemView.findViewById(R.id.message_image);
            fileContainer = itemView.findViewById(R.id.file_container);
            fileName = itemView.findViewById(R.id.file_name);
            fileSize = itemView.findViewById(R.id.file_size);
            replyContainer = itemView.findViewById(R.id.reply_container);
            replySender = itemView.findViewById(R.id.reply_sender);
            replyMessage = itemView.findViewById(R.id.reply_message);
            audioContainer = itemView.findViewById(R.id.audio_container);
            audioPlayButton = itemView.findViewById(R.id.audio_play_button);
            audioDuration = itemView.findViewById(R.id.audio_duration);
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
            messageTime.setText(Message.formatTime(message.getTimestamp()));

            // Définir l'icône de statut
            switch (message.getStatus()) {
                case SENT:
                    messageStatus.setImageResource(R.drawable.ic_done);
                    break;
                case DELIVERED:
                    messageStatus.setImageResource(R.drawable.ic_done_all);
                    break;
                case READ:
                    messageStatus.setImageResource(R.drawable.ic_done_all);
                    messageStatus.setColorFilter(itemView.getContext().getColor(R.color.colorSuccess));
                    break;
            }

            // Gérer les différents types de messages
            handleMessageType(message);

            // Gérer les réponses
            handleReplyMessage(message);

            // Listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(message);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMessageLongClick(message);
                }
                return true;
            });
        }

        private void handleMessageType(Message message) {
            // Masquer tous les conteneurs par défaut
            messageImage.setVisibility(View.GONE);
            fileContainer.setVisibility(View.GONE);
            audioContainer.setVisibility(View.GONE);

            switch (message.getType()) {
                case IMAGE:
                    messageImage.setVisibility(View.VISIBLE);
                    if (message.getImageUrl() != null) {
                        // Si c'est une image Base64
                        if (message.getImageUrl().startsWith("data:image") || !message.getImageUrl().startsWith("http")) {
                            Bitmap bitmap = ProfileRepository.base64ToBitmap(message.getImageUrl());
                            if (bitmap != null) {
                                messageImage.setImageBitmap(bitmap);
                            }
                        }

                        messageImage.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onImageClick(message);
                            }
                        });
                    }
                    break;

                case FILE:
                    fileContainer.setVisibility(View.VISIBLE);
                    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                        Message.FileAttachment attachment = message.getAttachments().get(0);
                        fileName.setText(attachment.getFileName());
                        fileSize.setText(attachment.getFormattedFileSize());

                        fileContainer.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onFileClick(message);
                            }
                        });
                    }
                    break;

                case AUDIO:
                    audioContainer.setVisibility(View.VISIBLE);
                    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                        Message.FileAttachment audioAttachment = message.getAttachments().get(0);
                        audioDuration.setText("Audio");

                        audioPlayButton.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onAudioClick(message);
                            }
                        });
                    }
                    break;

                default:
                    // Message texte - rien à faire de spécial
                    break;
            }
        }

        private void handleReplyMessage(Message message) {
            if (message.getReplyTo() != null) {
                replyContainer.setVisibility(View.VISIBLE);
                replySender.setText("Répondre à " + message.getReplyTo().getSenderName());
                replyMessage.setText(message.getReplyTo().getContent());
            } else {
                replyContainer.setVisibility(View.GONE);
            }
        }
    }

    // ViewHolder pour les messages reçus
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView senderAvatar;
        private MaterialTextView senderName;
        private MaterialTextView messageText;
        private MaterialTextView messageTime;
        private ShapeableImageView messageImage;
        private MaterialCardView fileContainer;
        private MaterialTextView fileName;
        private MaterialTextView fileSize;
        private MaterialCardView replyContainer;
        private MaterialTextView replySender;
        private MaterialTextView replyMessage;
        private MaterialCardView audioContainer;
        private MaterialButton audioPlayButton;
        private MaterialTextView audioDuration;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderAvatar = itemView.findViewById(R.id.sender_avatar);
            senderName = itemView.findViewById(R.id.sender_name);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            messageImage = itemView.findViewById(R.id.message_image);
            fileContainer = itemView.findViewById(R.id.file_container);
            fileName = itemView.findViewById(R.id.file_name);
            fileSize = itemView.findViewById(R.id.file_size);
            replyContainer = itemView.findViewById(R.id.reply_container);
            replySender = itemView.findViewById(R.id.reply_sender);
            replyMessage = itemView.findViewById(R.id.reply_message);
            audioContainer = itemView.findViewById(R.id.audio_container);
            audioPlayButton = itemView.findViewById(R.id.audio_play_button);
            audioDuration = itemView.findViewById(R.id.audio_duration);
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
            messageTime.setText(Message.formatTime(message.getTimestamp()));
            senderName.setText(message.getSenderName());

            // Charger l'avatar de l'expéditeur
            loadSenderAvatar(message.getSenderId());

            // Gérer les différents types de messages
            handleMessageType(message);

            // Gérer les réponses
            handleReplyMessage(message);

            // Listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(message);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMessageLongClick(message);
                }
                return true;
            });
        }

        private void loadSenderAvatar(String senderId) {
            TeamRepository.getInstance().getUserById(senderId, new TeamRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    if (user.getProfileImageBase64() != null) {
                        Bitmap bitmap = ProfileRepository.base64ToBitmap(user.getProfileImageBase64());
                        if (bitmap != null) {
                            senderAvatar.setImageBitmap(bitmap);
                        } else {
                            senderAvatar.setImageResource(R.drawable.default_avatar);
                        }
                    } else {
                        senderAvatar.setImageResource(R.drawable.default_avatar);
                    }
                }

                @Override
                public void onError(String error) {
                    senderAvatar.setImageResource(R.drawable.default_avatar);
                }
            });
        }

        private void handleMessageType(Message message) {
            // Masquer tous les conteneurs par défaut
            messageImage.setVisibility(View.GONE);
            fileContainer.setVisibility(View.GONE);
            audioContainer.setVisibility(View.GONE);

            switch (message.getType()) {
                case IMAGE:
                    messageImage.setVisibility(View.VISIBLE);
                    if (message.getImageUrl() != null) {
                        // Si c'est une image Base64
                        if (message.getImageUrl().startsWith("data:image") || !message.getImageUrl().startsWith("http")) {
                            Bitmap bitmap = ProfileRepository.base64ToBitmap(message.getImageUrl());
                            if (bitmap != null) {
                                messageImage.setImageBitmap(bitmap);
                            }
                        }

                        messageImage.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onImageClick(message);
                            }
                        });
                    }
                    break;

                case FILE:
                    fileContainer.setVisibility(View.VISIBLE);
                    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                        Message.FileAttachment attachment = message.getAttachments().get(0);
                        fileName.setText(attachment.getFileName());
                        fileSize.setText(attachment.getFormattedFileSize());

                        fileContainer.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onFileClick(message);
                            }
                        });
                    }
                    break;

                case AUDIO:
                    audioContainer.setVisibility(View.VISIBLE);
                    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                        Message.FileAttachment audioAttachment = message.getAttachments().get(0);
                        audioDuration.setText("Audio");

                        audioPlayButton.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onAudioClick(message);
                            }
                        });
                    }
                    break;

                default:
                    // Message texte - rien à faire de spécial
                    break;
            }
        }

        private void handleReplyMessage(Message message) {
            if (message.getReplyTo() != null) {
                replyContainer.setVisibility(View.VISIBLE);
                replySender.setText("Répondre à " + message.getReplyTo().getSenderName());
                replyMessage.setText(message.getReplyTo().getContent());
            } else {
                replyContainer.setVisibility(View.GONE);
            }
        }
    }
}