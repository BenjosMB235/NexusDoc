package com.example.nexusdoc.ui.archives.model;

import java.util.Date;

public class ArchiveDocument {
    private String id;
    private String name;
    private String type;
    private String mimeType;
    private String folderId;
    private String folderPath;
    private long size;
    private Date dateCreated;
    private Date dateModified;
    private Date dateSynced;
    private String uniqueCode;
    private String localPath;
    private String cloudPath;
    private boolean isFavorite;
    private boolean isShared;
    private boolean isSynced;
    private boolean isOfflineAvailable;
    private String tags;
    private String description;
    private String thumbnailBase64;
    private String userId; // CORRECTION: Ajout du champ userId manquant

    // Constructors
    public ArchiveDocument() {}

    public ArchiveDocument(String name, String type, String folderId) {
        this.name = name;
        this.type = type;
        this.folderId = folderId;
        this.dateCreated = new Date();
        this.dateModified = new Date();
        this.isFavorite = false;
        this.isShared = false;
        this.isSynced = false;
        this.isOfflineAvailable = true;
        this.generateUniqueCode();
    }

    // Generate unique code: TYPE_YYYYMMDD_HHMMSS_RANDOM
    private void generateUniqueCode() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String random = String.valueOf((int)(Math.random() * 1000));
        this.uniqueCode = type.toUpperCase() + "_" + timestamp + "_" + random;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getMimeType() { return mimeType; }
    public String getFolderId() { return folderId; }
    public String getFolderPath() { return folderPath; }
    public long getSize() { return size; }
    public Date getDateCreated() { return dateCreated; }
    public Date getDateModified() { return dateModified; }
    public Date getDateSynced() { return dateSynced; }
    public String getUniqueCode() { return uniqueCode; }
    public String getLocalPath() { return localPath; }
    public String getCloudPath() { return cloudPath; }
    public boolean isFavorite() { return isFavorite; }
    public boolean isShared() { return isShared; }
    public boolean isSynced() { return isSynced; }
    public boolean isOfflineAvailable() { return isOfflineAvailable; }
    public String getTags() { return tags; }
    public String getDescription() { return description; }
    public String getThumbnailBase64() { return thumbnailBase64; }
    public String getUserId() { return userId; } // CORRECTION: Getter ajouté

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }
    public void setSize(long size) { this.size = size; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    public void setDateSynced(Date dateSynced) { this.dateSynced = dateSynced; }
    public void setUniqueCode(String uniqueCode) { this.uniqueCode = uniqueCode; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    public void setCloudPath(String cloudPath) { this.cloudPath = cloudPath; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public void setShared(boolean shared) { this.isShared = shared; }
    public void setSynced(boolean synced) { this.isSynced = synced; }
    public void setOfflineAvailable(boolean offlineAvailable) { this.isOfflineAvailable = offlineAvailable; }
    public void setTags(String tags) { this.tags = tags; }
    public void setDescription(String description) { this.description = description; }
    public void setThumbnailBase64(String thumbnailBase64) { this.thumbnailBase64 = thumbnailBase64; }
    public void setUserId(String userId) { this.userId = userId; } // CORRECTION: Setter ajouté

    // Utility methods
    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    public int getIconResource() {
        if (mimeType == null) return com.example.nexusdoc.R.drawable.ic_document;

        if (mimeType.startsWith("image/")) {
            return com.example.nexusdoc.R.drawable.ic_image;
        } else if (mimeType.equals("application/pdf")) {
            return com.example.nexusdoc.R.drawable.ic_pdf;
        } else if (mimeType.startsWith("video/")) {
            return com.example.nexusdoc.R.drawable.ic_video;
        } else if (mimeType.startsWith("audio/")) {
            return com.example.nexusdoc.R.drawable.ic_audio;
        } else {
            return com.example.nexusdoc.R.drawable.ic_document;
        }
    }

    public boolean needsSync() {
        return !isSynced && (dateSynced == null || dateModified.after(dateSynced));
    }
}