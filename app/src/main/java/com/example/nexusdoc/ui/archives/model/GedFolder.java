package com.example.nexusdoc.ui.archives.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

// Modèle unifié pour les dossiers
public class GedFolder {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("parent_id")
    private String parentId;

    @SerializedName("path")
    private String path;

    @SerializedName("type")
    private String type;

    @SerializedName("description")
    private String description;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("document_count")
    private int documentCount;

    @SerializedName("subfolder_count")
    private int subfolderCount;

    @SerializedName("total_size")
    private long totalSize;

    @SerializedName("is_shared")
    private boolean isShared;

    @SerializedName("is_synced")
    private boolean isSynced;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    // Types de dossiers
    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_FOURNISSEURS = "fournisseurs";
    public static final String TYPE_CLIENTS = "clients";
    public static final String TYPE_AUTRES = "autres";
    public static final String TYPE_SUBFOLDER = "subfolder";

    // Constructeurs
    public GedFolder() {}

    public GedFolder(String name, String type, String parentId) {
        this.name = name;
        this.type = type;
        this.parentId = parentId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isSynced = false;
        this.documentCount = 0;
        this.subfolderCount = 0;
        this.totalSize = 0;
        this.isShared = false;
        generatePath();
    }

    private void generatePath() {
        this.path = "/" + name;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name != null ? name : "Dossier sans nom"; }
    public void setName(String name) { this.name = name; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getDocumentCount() { return documentCount; }
    public void setDocumentCount(int documentCount) { this.documentCount = documentCount; }

    public int getSubfolderCount() { return subfolderCount; }
    public void setSubfolderCount(int subfolderCount) { this.subfolderCount = subfolderCount; }

    public long getTotalSize() { return totalSize; }
    public void setTotalSize(long totalSize) { this.totalSize = totalSize; }

    public boolean isShared() { return isShared; } // Ajoute ce getter
    public void setShared(boolean shared) { isShared = shared; } // Ajoute ce setter

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Méthodes utilitaires
    public String getFormattedSize() {
        if (totalSize < 1024) return totalSize + " B";
        if (totalSize < 1024 * 1024) return String.format("%.1f KB", totalSize / 1024.0);
        if (totalSize < 1024 * 1024 * 1024) return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", totalSize / (1024.0 * 1024.0 * 1024.0));
    }

    public boolean isEmpty() {
        return documentCount == 0 && subfolderCount == 0;
    }

    public boolean isRootFolder() {
        return parentId == null || parentId.isEmpty();
    }

    public boolean needsSync() {
        return !isSynced;
    }

    // Méthodes statiques pour créer des dossiers typés
    public static GedFolder createAdminFolder(String name) {
        return new GedFolder(name, TYPE_ADMIN, null);
    }

    public static GedFolder createClientFolder(String name) {
        return new GedFolder(name, TYPE_CLIENTS, null);
    }

    public static GedFolder createFournisseurFolder(String name) {
        return new GedFolder(name, TYPE_FOURNISSEURS, null);
    }

    public static GedFolder createOtherFolder(String name) {
        return new GedFolder(name, TYPE_AUTRES, null);
    }

}