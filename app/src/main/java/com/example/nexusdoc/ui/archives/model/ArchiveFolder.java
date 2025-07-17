package com.example.nexusdoc.ui.archives.model;

import java.util.Date;
import java.util.List;

public class ArchiveFolder {
    private String id;
    private String name;
    private String parentId;
    private String path;
    private String type; // "admin", "fournisseurs", "clients", "autres", "subfolder"
    private Date dateCreated;
    private Date dateModified;
    private Date dateSynced;
    private boolean isSynced; // CORRECTION: Ajout du getter/setter manquant
    private String userId;
    private int documentCount;
    private int subfolderCount;
    private long totalSize;
    private String description;
    private String color;
    private boolean isExpanded;

    // Predefined folder types - CORRECTION: Mise à jour des types
    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_FOURNISSEURS = "fournisseurs";
    public static final String TYPE_CLIENTS = "clients";
    public static final String TYPE_AUTRES = "autres";
    public static final String TYPE_SUBFOLDER = "subfolder";

    // Constructors
    public ArchiveFolder() {}

    public ArchiveFolder(String name, String type, String parentId) {
        this.name = name;
        this.type = type;
        this.parentId = parentId;
        this.dateCreated = new Date();
        this.dateModified = new Date();
        this.isSynced = false;
        this.documentCount = 0;
        this.subfolderCount = 0;
        this.totalSize = 0;
        this.isExpanded = false;
        this.generatePath();
    }

    private void generatePath() {
        if (parentId == null || parentId.isEmpty()) {
            this.path = "/" + name;
        } else {
            // Path will be set by the parent folder
            this.path = "/" + name;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getParentId() { return parentId; }
    public String getPath() { return path; }
    public String getType() { return type; }
    public Date getDateCreated() { return dateCreated; }
    public Date getDateModified() { return dateModified; }
    public Date getDateSynced() { return dateSynced; }
    public boolean isSynced() { return isSynced; } // CORRECTION: Getter corrigé
    public int getDocumentCount() { return documentCount; }
    public int getSubfolderCount() { return subfolderCount; }
    public long getTotalSize() { return totalSize; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public boolean isExpanded() { return isExpanded; }
    public String getUserId() { return userId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setPath(String path) { this.path = path; }
    public void setType(String type) { this.type = type; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    public void setDateSynced(Date dateSynced) { this.dateSynced = dateSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; } // CORRECTION: Setter corrigé
    public void setDocumentCount(int documentCount) { this.documentCount = documentCount; }
    public void setSubfolderCount(int subfolderCount) { this.subfolderCount = subfolderCount; }
    public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
    public void setDescription(String description) { this.description = description; }
    public void setColor(String color) { this.color = color; }
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }
    public void setUserId(String userId) { this.userId = userId; }

    // Utility methods
    public String getFormattedSize() {
        if (totalSize < 1024) return totalSize + " B";
        if (totalSize < 1024 * 1024) return String.format("%.1f KB", totalSize / 1024.0);
        if (totalSize < 1024 * 1024 * 1024) return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", totalSize / (1024.0 * 1024.0 * 1024.0));
    }

    public int getIconResource() {
        switch (type) {
            case TYPE_CLIENTS:
                return com.example.nexusdoc.R.drawable.ic_business;
            case TYPE_FOURNISSEURS:
                return com.example.nexusdoc.R.drawable.ic_local_shipping;
            case TYPE_ADMIN:
                return com.example.nexusdoc.R.drawable.ic_admin_panel_settings;
            default:
                return com.example.nexusdoc.R.drawable.ic_folder;
        }
    }

    public String getDisplayInfo() {
        StringBuilder info = new StringBuilder();

        if (documentCount > 0) {
            info.append(documentCount).append(" document");
            if (documentCount > 1) info.append("s");
        }

        if (subfolderCount > 0) {
            if (info.length() > 0) info.append(", ");
            info.append(subfolderCount).append(" dossier");
            if (subfolderCount > 1) info.append("s");
        }

        if (info.length() == 0) {
            info.append("Vide");
        }

        return info.toString();
    }

    public boolean isRootFolder() {
        return parentId == null || parentId.isEmpty();
    }

    public boolean needsSync() {
        return !isSynced && (dateSynced == null || dateModified.after(dateSynced));
    }

    // Static methods for creating predefined folders - CORRECTION: Types mis à jour
    public static ArchiveFolder createClientFolder(String clientName) {
        return new ArchiveFolder(clientName, TYPE_CLIENTS, null);
    }

    public static ArchiveFolder createFournisseurFolder(String fournisseurName) {
        return new ArchiveFolder(fournisseurName, TYPE_FOURNISSEURS, null);
    }

    public static ArchiveFolder createAdminFolder(String folderName) {
        return new ArchiveFolder(folderName, TYPE_ADMIN, null);
    }

    public static ArchiveFolder createOtherFolder(String folderName) {
        return new ArchiveFolder(folderName, TYPE_AUTRES, null);
    }
}