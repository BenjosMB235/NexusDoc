package com.example.nexusdoc.ui.chat.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.nexusdoc.ui.data.models.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {
    private static ChatRepository instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference messagesRef;
    private CollectionReference typingStatusRef;
    private CollectionReference blockedUsersRef;
    private MutableLiveData<List<Message>> messages;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<Boolean> isTyping;

    private ChatRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        messagesRef = db.collection("messages");
        typingStatusRef = db.collection("typing_status");
        blockedUsersRef = db.collection("blocked_users");
        messages = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        isTyping = new MutableLiveData<>();
    }

    public static ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsTyping() {
        return isTyping;
    }

    public void loadMessages(String otherUserId) {
        isLoading.setValue(true);
        String currentUserId = auth.getCurrentUser().getUid();

        // Requête pour les messages envoyés par l'utilisateur actuel
        Query sentMessages = messagesRef
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", otherUserId);

        // Requête pour les messages reçus par l'utilisateur actuel
        Query receivedMessages = messagesRef
                .whereEqualTo("senderId", otherUserId)
                .whereEqualTo("receiverId", currentUserId);

        // Écouter les messages envoyés
        sentMessages.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                isLoading.setValue(false);
                errorMessage.setValue("Erreur lors du chargement des messages: " + e.getMessage());
                return;
            }
            combineAndSortMessages(currentUserId, otherUserId);
        });

        // Écouter les messages reçus
        receivedMessages.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                isLoading.setValue(false);
                errorMessage.setValue("Erreur lors du chargement des messages: " + e.getMessage());
                return;
            }
            combineAndSortMessages(currentUserId, otherUserId);
        });
    }

    private void combineAndSortMessages(String currentUserId, String otherUserId) {
        // Récupérer tous les messages de la conversation
        messagesRef.whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", otherUserId)
                .get()
                .addOnSuccessListener(sentSnapshot -> {
                    messagesRef.whereEqualTo("senderId", otherUserId)
                            .whereEqualTo("receiverId", currentUserId)
                            .get()
                            .addOnSuccessListener(receivedSnapshot -> {
                                List<Message> messageList = new ArrayList<>();

                                // Ajouter les messages envoyés
                                for (DocumentSnapshot doc : sentSnapshot.getDocuments()) {
                                    Message message = doc.toObject(Message.class);
                                    if (message != null) {
                                        message.setId(doc.getId());
                                        messageList.add(message);
                                    }
                                }

                                // Ajouter les messages reçus
                                for (DocumentSnapshot doc : receivedSnapshot.getDocuments()) {
                                    Message message = doc.toObject(Message.class);
                                    if (message != null) {
                                        message.setId(doc.getId());
                                        messageList.add(message);
                                    }
                                }

                                // Trier par timestamp
                                messageList.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

                                isLoading.setValue(false);
                                messages.setValue(messageList);
                            })
                            .addOnFailureListener(e -> {
                                isLoading.setValue(false);
                                errorMessage.setValue("Erreur lors du chargement des messages: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Erreur lors du chargement des messages: " + e.getMessage());
                });
    }

    public void sendMessage(Message message) {
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    message.setId(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de l'envoi du message: " + e.getMessage());
                });
    }

    public void deleteMessage(String messageId) {
        messagesRef.document(messageId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Message supprimé avec succès
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de la suppression: " + e.getMessage());
                });
    }

    public void clearChat(String otherUserId) {
        String currentUserId = auth.getCurrentUser().getUid();

        // Supprimer tous les messages de la conversation
        messagesRef.whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", otherUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de l'effacement: " + e.getMessage());
                });

        messagesRef.whereEqualTo("senderId", otherUserId)
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de l'effacement: " + e.getMessage());
                });
    }

    public void blockUser(String userId) {
        String currentUserId = auth.getCurrentUser().getUid();

        Map<String, Object> blockData = new HashMap<>();
        blockData.put("blockerId", currentUserId);
        blockData.put("blockedUserId", userId);
        blockData.put("timestamp", System.currentTimeMillis());

        blockedUsersRef.document(currentUserId + "_" + userId)
                .set(blockData)
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors du blocage: " + e.getMessage());
                });
    }

    public void markMessageAsRead(String messageId) {
        messagesRef.document(messageId)
                .update("status", Message.MessageStatus.READ)
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de la mise à jour du statut: " + e.getMessage());
                });
    }

    public void markAllMessagesAsRead(String otherUserId) {
        String currentUserId = auth.getCurrentUser().getUid();

        messagesRef.whereEqualTo("senderId", otherUserId)
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().update("status", Message.MessageStatus.READ);
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de la mise à jour des messages: " + e.getMessage());
                });
    }

    public void setTypingStatus(String otherUserId, boolean isTyping) {
        String currentUserId = auth.getCurrentUser().getUid();

        TypingStatus status = new TypingStatus(currentUserId, otherUserId, isTyping, System.currentTimeMillis());

        typingStatusRef.document(currentUserId + "_" + otherUserId)
                .set(status)
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de la mise à jour du statut de frappe: " + e.getMessage());
                });
    }

    public void listenForTypingStatus(String otherUserId) {
        String currentUserId = auth.getCurrentUser().getUid();

        typingStatusRef.document(otherUserId + "_" + currentUserId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        TypingStatus status = documentSnapshot.toObject(TypingStatus.class);
                        if (status != null) {
                            // Vérifier si le statut n'est pas trop ancien (plus de 5 secondes)
                            boolean isCurrentlyTyping = status.isTyping() &&
                                    (System.currentTimeMillis() - status.getTimestamp()) < 5000;
                            isTyping.setValue(isCurrentlyTyping);
                        }
                    } else {
                        isTyping.setValue(false);
                    }
                });
    }

    public static class TypingStatus {
        private String senderId;
        private String receiverId;
        private boolean isTyping;
        private long timestamp;

        public TypingStatus() {}

        public TypingStatus(String senderId, String receiverId, boolean isTyping, long timestamp) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.isTyping = isTyping;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

        public String getReceiverId() { return receiverId; }
        public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

        public boolean isTyping() { return isTyping; }
        public void setTyping(boolean typing) { isTyping = typing; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}