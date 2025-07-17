package com.example.nexusdoc.ui.data.models;

import java.util.List;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String content;
    private long timestamp;
    private MessageType type;
    private MessageStatus status;
    private String imageUrl;
    private List<FileAttachment> attachments;
    private ReplyMessage replyTo;

    public enum MessageType {
        TEXT, IMAGE, FILE, AUDIO
    }

    public enum MessageStatus {
        SENT, DELIVERED, READ
    }

    public static class FileAttachment {
        private String fileName;
        private String fileUrl;
        private String fileType;
        private long fileSize;

        public FileAttachment() {}

        public FileAttachment(String fileName, String fileUrl, String fileType, long fileSize) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.fileType = fileType;
            this.fileSize = fileSize;
        }

        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public String getFormattedFileSize() {
            if (fileSize < 1024) return fileSize + " B";
            if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    public static class ReplyMessage {
        private String messageId;
        private String senderName;
        private String content;

        public ReplyMessage() {}

        public ReplyMessage(String messageId, String senderName, String content) {
            this.messageId = messageId;
            this.senderName = senderName;
            this.content = content;
        }

        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }

        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public Message() {}

    public Message(String senderId, String senderName, String receiverId, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.type = MessageType.TEXT;
        this.status = MessageStatus.SENT;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<FileAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<FileAttachment> attachments) { this.attachments = attachments; }

    public ReplyMessage getReplyTo() { return replyTo; }
    public void setReplyTo(ReplyMessage replyTo) { this.replyTo = replyTo; }

    // SUPPRIMÉ: getFormattedTime() - cette méthode causait l'erreur Firestore
    // Utilisez plutôt cette méthode utilitaire statique
    public static String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    public boolean isSentByCurrentUser(String currentUserId) {
        return senderId.equals(currentUserId);
    }
}