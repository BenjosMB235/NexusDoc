package com.example.nexusdoc.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.example.nexusdoc.ui.data.models.Message;
import com.example.nexusdoc.ui.data.models.User;
import com.example.nexusdoc.ui.chat.repository.ChatRepository;
import com.example.nexusdoc.ui.team.repository.TeamRepository;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private ChatRepository chatRepository;
    private TeamRepository teamRepository;
    private MutableLiveData<User> currentUser;
    private MutableLiveData<User> otherUser;
    private String otherUserId;
    private FirebaseAuth auth;

    public ChatViewModel() {
        chatRepository = ChatRepository.getInstance();
        teamRepository = TeamRepository.getInstance();
        currentUser = new MutableLiveData<>();
        otherUser = new MutableLiveData<>();
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Message>> getMessages() {
        return chatRepository.getMessages();
    }

    public LiveData<String> getErrorMessage() {
        return chatRepository.getErrorMessage();
    }

    public LiveData<Boolean> getIsLoading() {
        return chatRepository.getIsLoading();
    }

    public LiveData<Boolean> getIsTyping() {
        return chatRepository.getIsTyping();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<User> getOtherUser() {
        return otherUser;
    }

    public void initChat(String otherUserId) {
        this.otherUserId = otherUserId;

        // Charger les messages
        chatRepository.loadMessages(otherUserId);

        // Charger les informations des utilisateurs
        loadCurrentUser();
        loadOtherUser(otherUserId);

        // Écouter le statut de frappe
        chatRepository.listenForTypingStatus(otherUserId);

        // Marquer tous les messages comme lus
        chatRepository.markAllMessagesAsRead(otherUserId);
    }

    private void loadCurrentUser() {
        teamRepository.getCurrentUser(new TeamRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser.setValue(user);
            }

            @Override
            public void onError(String error) {
                // Gérer l'erreur
            }
        });
    }

    private void loadOtherUser(String userId) {
        teamRepository.getUserById(userId, new TeamRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                otherUser.setValue(user);
            }

            @Override
            public void onError(String error) {
                // Gérer l'erreur
            }
        });
    }

    public void sendMessage(String content) {
        User current = currentUser.getValue();
        if (current != null && otherUserId != null) {
            Message message = new Message(
                    current.getId(),
                    current.getUsername(),
                    otherUserId,
                    content
            );
            chatRepository.sendMessage(message);
        }
    }

    public void sendImageMessage(String imageUrl, String content) {
        User current = currentUser.getValue();
        if (current != null && otherUserId != null) {
            Message message = new Message(
                    current.getId(),
                    current.getUsername(),
                    otherUserId,
                    content != null ? content : ""
            );
            message.setType(Message.MessageType.IMAGE);
            message.setImageUrl(imageUrl);
            chatRepository.sendMessage(message);
        }
    }

    public void sendFileMessage(List<Message.FileAttachment> attachments, String content) {
        User current = currentUser.getValue();
        if (current != null && otherUserId != null) {
            Message message = new Message(
                    current.getId(),
                    current.getUsername(),
                    otherUserId,
                    content != null ? content : ""
            );
            message.setType(Message.MessageType.FILE);
            message.setAttachments(attachments);
            chatRepository.sendMessage(message);
        }
    }

    public void sendAudioMessage(List<Message.FileAttachment> attachments) {
        User current = currentUser.getValue();
        if (current != null && otherUserId != null) {
            Message message = new Message(
                    current.getId(),
                    current.getUsername(),
                    otherUserId,
                    ""
            );
            message.setType(Message.MessageType.AUDIO);
            message.setAttachments(attachments);
            chatRepository.sendMessage(message);
        }
    }

    public void replyToMessage(Message originalMessage, String content) {
        User current = currentUser.getValue();
        if (current != null && otherUserId != null) {
            Message message = new Message(
                    current.getId(),
                    current.getUsername(),
                    otherUserId,
                    content
            );

            Message.ReplyMessage reply = new Message.ReplyMessage(
                    originalMessage.getId(),
                    originalMessage.getSenderName(),
                    originalMessage.getContent()
            );
            message.setReplyTo(reply);
            chatRepository.sendMessage(message);
        }
    }

    public void deleteMessage(String messageId) {
        chatRepository.deleteMessage(messageId);
    }

    public void clearChat(String otherUserId) {
        chatRepository.clearChat(otherUserId);
    }

    public void blockUser(String userId) {
        chatRepository.blockUser(userId);
    }

    public void setTypingStatus(boolean isTyping) {
        if (otherUserId != null) {
            chatRepository.setTypingStatus(otherUserId, isTyping);
        }
    }

    public void markMessageAsRead(String messageId) {
        chatRepository.markMessageAsRead(messageId);
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}