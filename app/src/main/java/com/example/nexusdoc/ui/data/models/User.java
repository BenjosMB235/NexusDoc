package com.example.nexusdoc.ui.data.models;

public class User {
    private String id;
    private String email;
    private String username;
    private String fonction;
    private String telephone;
    private String profileImageBase64;
    private String status; // online, away, offline
    private long lastActivity;
    private boolean isManager;
    private boolean isAdmin;
    private long createdAt;
    private long updatedAt;
    private String provider;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String id, String username, String email, String fonction) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fonction = fonction;
        this.status = "offline";
        this.lastActivity = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isManager = false;
        this.isAdmin = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getLastActivity() { return lastActivity; }
    public void setLastActivity(long lastActivity) { this.lastActivity = lastActivity; }

    public boolean isManager() { return isManager; }
    public void setManager(boolean manager) { isManager = manager; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    // Méthodes utilitaires
    public String getFormattedLastActivity() {
        long diff = System.currentTimeMillis() - lastActivity;
        if (diff < 60000) return "En ligne";
        if (diff < 3600000) return "Actif il y a " + (diff / 60000) + "min";
        if (diff < 86400000) return "Actif il y a " + (diff / 3600000) + "h";
        return "Actif il y a " + (diff / 86400000) + "j";
    }

    public String getDisplayName() {
        return username != null ? username : email;
    }

    public String getRoleDisplay() {
        if (isAdmin) return "Administrateur";
        if (isManager) return "Manager";
        return fonction != null ? fonction : "Utilisateur";
    }
}