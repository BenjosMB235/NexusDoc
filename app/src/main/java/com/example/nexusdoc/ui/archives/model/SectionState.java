package com.example.nexusdoc.ui.archives.model;

import java.util.ArrayList;
import java.util.List;

public class SectionState {
    private boolean isExpanded;
    private List<String> currentPath;
    private ArchiveFolder currentFolder;

    public SectionState() {
        this.isExpanded = false;
        this.currentPath = new ArrayList<>();
        this.currentFolder = null;
    }

    public SectionState(boolean isExpanded, List<String> currentPath, ArchiveFolder currentFolder) {
        this.isExpanded = isExpanded;
        this.currentPath = currentPath != null ? currentPath : new ArrayList<>();
        this.currentFolder = currentFolder;
    }

    // Getters
    public boolean isExpanded() { return isExpanded; }
    public List<String> getCurrentPath() { return currentPath; }
    public ArchiveFolder getCurrentFolder() { return currentFolder; }

    // Setters
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }
    public void setCurrentPath(List<String> currentPath) { this.currentPath = currentPath; }
    public void setCurrentFolder(ArchiveFolder currentFolder) { this.currentFolder = currentFolder; }
}