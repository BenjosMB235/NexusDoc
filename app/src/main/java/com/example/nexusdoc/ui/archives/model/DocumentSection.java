package com.example.nexusdoc.ui.archives.model;

import java.util.ArrayList;
import java.util.List;

public class DocumentSection {
    private String id;
    private String title;
    private int iconResource;
    private String color;
    private boolean isExpanded;
    private List<String> currentPath;
    private ArchiveFolder currentFolder;
    private int documentCount;

    public DocumentSection(String id, String title, int iconResource, String color) {
        this.id = id;
        this.title = title;
        this.iconResource = iconResource;
        this.color = color;
        this.isExpanded = false;
        this.currentPath = new ArrayList<>();
        this.documentCount = 0;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getIconResource() { return iconResource; }
    public String getColor() { return color; }
    public boolean isExpanded() { return isExpanded; }
    public List<String> getCurrentPath() { return currentPath; }
    public ArchiveFolder getCurrentFolder() { return currentFolder; }
    public int getDocumentCount() { return documentCount; }

    // Setters
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }
    public void setCurrentPath(List<String> currentPath) { this.currentPath = currentPath; }
    public void setCurrentFolder(ArchiveFolder currentFolder) { this.currentFolder = currentFolder; }
    public void setDocumentCount(int documentCount) { this.documentCount = documentCount; }

    public void toggleExpansion() {
        this.isExpanded = !this.isExpanded;
    }

    public boolean isInRootFolder() {
        return currentFolder == null;
    }

    public String getBreadcrumbPath() {
        if (currentPath.isEmpty()) {
            return title;
        }
        StringBuilder path = new StringBuilder(title);
        for (String pathItem : currentPath) {
            if (!pathItem.equals(title)) {
                path.append(" > ").append(pathItem);
            }
        }
        return path.toString();
    }
}