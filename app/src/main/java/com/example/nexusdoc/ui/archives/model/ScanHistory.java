package com.example.nexusdoc.ui.archives.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Modèle pour l'historique des scans
public class ScanHistory {
    @SerializedName("id")
    private String id;

    @SerializedName("document_id")
    private String documentId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("scan_type")
    private String scanType; // camera, scanner, import

    @SerializedName("ocr_text")
    private String ocrText;

    @SerializedName("ocr_confidence")
    private Float ocrConfidence;

    @SerializedName("detected_language")
    private String detectedLanguage;

    @SerializedName("original_size")
    private Long originalSize;

    @SerializedName("processed_size")
    private Long processedSize;

    @SerializedName("processing_time_ms")
    private Integer processingTimeMs;

    @SerializedName("auto_tags")
    private List<String> autoTags;

    @SerializedName("created_at")
    private Date createdAt;

    // Types de scan
    public static final String TYPE_CAMERA = "camera";
    public static final String TYPE_SCANNER = "scanner";
    public static final String TYPE_IMPORT = "import";

    // Constructeurs
    public ScanHistory() {
        this.autoTags = new ArrayList<>();
        this.createdAt = new Date();
    }

    public ScanHistory(String documentId, String userId, String scanType) {
        this();
        this.documentId = documentId;
        this.userId = userId;
        this.scanType = scanType;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public Float getOcrConfidence() { return ocrConfidence; }
    public void setOcrConfidence(Float ocrConfidence) { this.ocrConfidence = ocrConfidence; }

    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }

    public Long getOriginalSize() { return originalSize; }
    public void setOriginalSize(Long originalSize) { this.originalSize = originalSize; }

    public Long getProcessedSize() { return processedSize; }
    public void setProcessedSize(Long processedSize) { this.processedSize = processedSize; }

    public Integer getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Integer processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public List<String> getAutoTags() { return autoTags != null ? autoTags : new ArrayList<>(); }
    public void setAutoTags(List<String> autoTags) { this.autoTags = autoTags; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Méthodes utilitaires
    public boolean hasOcr() {
        return ocrText != null && !ocrText.trim().isEmpty();
    }

    public double getCompressionRatio() {
        if (originalSize == null || processedSize == null || originalSize == 0) {
            return 0.0;
        }
        return (double) processedSize / originalSize;
    }

    public String getFormattedProcessingTime() {
        if (processingTimeMs == null) return "N/A";
        if (processingTimeMs < 1000) return processingTimeMs + "ms";
        return String.format("%.1fs", processingTimeMs / 1000.0);
    }
}