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
import com.google.android.material.imageview.ShapeableImageView;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.data.models.Message;
import com.example.nexusdoc.ui.profile.repository.ProfileRepository;
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
        private TextView messageText;
        private TextView messageTime;
        private ImageView messageStatus;
        private ShapeableImageView messageImage;
        private LinearLayout fileContainer;
        private TextView fileName;
        private TextView fileSize;
        private LinearLayout replyContainer;
        private TextView replySender;
        private TextView replyMessage;

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
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
            // CORRECTION: Utiliser la méthode statique au lieu de getFormattedTime()
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
            switch (message.getType()) {
                case IMAGE:
                    messageImage.setVisibility(View.VISIBLE);
                    fileContainer.setVisibility(View.GONE);

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
                    messageImage.setVisibility(View.GONE);
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

                default:
                    messageImage.setVisibility(View.GONE);
                    fileContainer.setVisibility(View.GONE);
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
        private TextView senderName;
        private TextView messageText;
        private TextView messageTime;
        private ShapeableImageView messageImage;
        private LinearLayout fileContainer;
        private TextView fileName;
        private TextView fileSize;
        private LinearLayout replyContainer;
        private TextView replySender;
        private TextView replyMessage;

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
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
            // CORRECTION: Utiliser la méthode statique au lieu de getFormattedTime()
            messageTime.setText(Message.formatTime(message.getTimestamp()));
            senderName.setText(message.getSenderName());

            // Avatar par défaut pour les messages reçus
            senderAvatar.setImageResource(R.drawable.default_avatar);

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
            switch (message.getType()) {
                case IMAGE:
                    messageImage.setVisibility(View.VISIBLE);
                    fileContainer.setVisibility(View.GONE);

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
                    messageImage.setVisibility(View.GONE);
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

                default:
                    messageImage.setVisibility(View.GONE);
                    fileContainer.setVisibility(View.GONE);
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