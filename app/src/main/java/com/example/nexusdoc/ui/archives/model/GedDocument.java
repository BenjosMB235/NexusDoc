package com.example.nexusdoc.ui.archives.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Modèle unifié pour les documents avec OCR
public class GedDocument {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("mime_type")
    private String mimeType;

    @SerializedName("folder_id")
    private String folderId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("size")
    private long size;

    @SerializedName("unique_code")
    private String uniqueCode;

    @SerializedName("content_url")
    private String contentUrl;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("description")
    private String description;

    // CORRECTION: Exclure le champ tags de la sérialisation pour l'API
    @Expose(serialize = false, deserialize = false)
    private List<String> tags;

    @SerializedName("ocr_text")
    private String ocrText;

    @SerializedName("ocr_confidence")
    private Float ocrConfidence;

    @SerializedName("is_favorite")
    private boolean isFavorite;

    @SerializedName("is_shared")
    private boolean isShared;

    @SerializedName("is_synced")
    private boolean isSynced;

    @SerializedName("is_offline_available")
    private boolean isOfflineAvailable;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("last_accessed_at")
    private Date lastAccessedAt;

    @SerializedName("ged_document_tags")
    private List<DocumentTag> documentTags;

    // Constructeurs
    public GedDocument() {
        this.tags = new ArrayList<>();
    }

    public GedDocument(String name, String type, String folderId) {
        this();
        this.name = name;
        this.type = type;
        this.folderId = folderId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isFavorite = false;
        this.isShared = false;
        this.isSynced = false;
        this.isOfflineAvailable = false;
        generateUniqueCode();
    }

    private void generateUniqueCode() {
        this.uniqueCode = type.toUpperCase() + "_" + System.currentTimeMillis();
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name != null ? name : "Document sans nom"; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getUniqueCode() { return uniqueCode; }
    public void setUniqueCode(String uniqueCode) { this.uniqueCode = uniqueCode; }

    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public Float getOcrConfidence() { return ocrConfidence; }
    public void setOcrConfidence(Float ocrConfidence) { this.ocrConfidence = ocrConfidence; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isShared() { return isShared; }
    public void setShared(boolean shared) { isShared = shared; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public boolean isOfflineAvailable() { return isOfflineAvailable; }
    public void setOfflineAvailable(boolean offlineAvailable) { isOfflineAvailable = offlineAvailable; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Date lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    // Méthodes utilitaires
    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    public boolean needsSync() {
        return !isSynced;
    }

    public boolean hasOcr() {
        return ocrText != null && !ocrText.trim().isEmpty();
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }

    public void addTag(String tag) {
        if (tags == null) tags = new ArrayList<>();
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    public List<String> getTags() {
        List<String> tagNames = new ArrayList<>();
        if (documentTags != null) {
            for (DocumentTag docTag : documentTags) {
                try {
                    if (docTag != null && docTag.getTag() != null) {
                        String name = docTag.getTag().getName();
                        if (name != null && !name.isEmpty()) {
                            tagNames.add(name);
                        }
                    }
                } catch (Exception e) {
                    Log.e("GedDocument", "Error processing tag", e);
                }
            }
        }
        return tagNames;
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && size > 0;
    }
}