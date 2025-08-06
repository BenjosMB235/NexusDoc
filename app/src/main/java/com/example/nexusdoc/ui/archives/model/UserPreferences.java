package com.example.nexusdoc.ui.archives.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

// Modèle pour les préférences utilisateur
public class UserPreferences {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("default_view_mode")
    private String defaultViewMode = "grid";

    @SerializedName("items_per_page")
    private int itemsPerPage = 20;

    @SerializedName("auto_ocr")
    private boolean autoOcr = true;

    @SerializedName("auto_tagging")
    private boolean autoTagging = true;

    @SerializedName("image_quality")
    private String imageQuality = "medium";

    @SerializedName("auto_sync")
    private boolean autoSync = true;

    @SerializedName("offline_storage_limit_mb")
    private int offlineStorageLimitMb = 500;

    @SerializedName("notify_scan_complete")
    private boolean notifyScanComplete = true;

    @SerializedName("notify_sync_errors")
    private boolean notifySyncErrors = true;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    // Constantes
    public static final String VIEW_MODE_GRID = "grid";
    public static final String VIEW_MODE_LIST = "list";

    public static final String QUALITY_LOW = "low";
    public static final String QUALITY_MEDIUM = "medium";
    public static final String QUALITY_HIGH = "high";

    // Constructeurs
    public UserPreferences() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public UserPreferences(String userId) {
        this();
        this.userId = userId;
    }

    // Getters et Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDefaultViewMode() { return defaultViewMode; }
    public void setDefaultViewMode(String defaultViewMode) { this.defaultViewMode = defaultViewMode; }

    public int getItemsPerPage() { return itemsPerPage; }
    public void setItemsPerPage(int itemsPerPage) { this.itemsPerPage = itemsPerPage; }

    public boolean isAutoOcr() { return autoOcr; }
    public void setAutoOcr(boolean autoOcr) { this.autoOcr = autoOcr; }

    public boolean isAutoTagging() { return autoTagging; }
    public void setAutoTagging(boolean autoTagging) { this.autoTagging = autoTagging; }

    public String getImageQuality() { return imageQuality; }
    public void setImageQuality(String imageQuality) { this.imageQuality = imageQuality; }

    public boolean isAutoSync() { return autoSync; }
    public void setAutoSync(boolean autoSync) { this.autoSync = autoSync; }

    public int getOfflineStorageLimitMb() { return offlineStorageLimitMb; }
    public void setOfflineStorageLimitMb(int offlineStorageLimitMb) { this.offlineStorageLimitMb = offlineStorageLimitMb; }

    public boolean isNotifyScanComplete() { return notifyScanComplete; }
    public void setNotifyScanComplete(boolean notifyScanComplete) { this.notifyScanComplete = notifyScanComplete; }

    public boolean isNotifySyncErrors() { return notifySyncErrors; }
    public void setNotifySyncErrors(boolean notifySyncErrors) { this.notifySyncErrors = notifySyncErrors; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Méthodes utilitaires
    public boolean isGridView() {
        return VIEW_MODE_GRID.equals(defaultViewMode);
    }

    public int getImageQualityPercent() {
        switch (imageQuality) {
            case QUALITY_LOW: return 60;
            case QUALITY_HIGH: return 95;
            default: return 80; // medium
        }
    }
}