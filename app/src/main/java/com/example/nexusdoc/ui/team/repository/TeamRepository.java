package com.example.nexusdoc.ui.team.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexusdoc.ui.utilitaires.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.nexusdoc.ui.data.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TeamRepository {
    private static TeamRepository instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference usersRef;
    private MutableLiveData<List<User>> teamMembers;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;

    private TeamRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        usersRef = db.collection(Constants.COLLECTION_USERS);
        teamMembers = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
    }

    public static TeamRepository getInstance() {
        if (instance == null) {
            instance = new TeamRepository();
        }
        return instance;
    }

    public LiveData<List<User>> getTeamMembers() {
        return teamMembers;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadTeamMembers() {
        isLoading.setValue(true);

        usersRef.orderBy("username")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    isLoading.setValue(false);
                    if (e != null) {
                        errorMessage.setValue("Erreur lors du chargement des membres: " + e.getMessage());
                        return;
                    }

                    List<User> members = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            User user = convertDocumentToUser(doc);
                            if (user != null) {
                                members.add(user);
                            }
                        }
                    }
                    teamMembers.setValue(members);
                });
    }

    private User convertDocumentToUser(DocumentSnapshot doc) {
        try {
            User user = new User();
            user.setId(doc.getId());
            user.setUsername(doc.getString("username"));
            user.setEmail(doc.getString("email"));
            user.setFonction(doc.getString("fonction"));
            user.setTelephone(doc.getString("telephone"));
            user.setProfileImageBase64(doc.getString("profileImageBase64"));
            user.setProvider(doc.getString("provider"));

            // Gestion des timestamps
            if (doc.contains("createdAt") && doc.get("createdAt") != null) {
                Object createdAt = doc.get("createdAt");
                if (createdAt instanceof com.google.firebase.Timestamp) {
                    user.setCreatedAt(((com.google.firebase.Timestamp) createdAt).toDate().getTime());
                } else if (createdAt instanceof Long) {
                    user.setCreatedAt((Long) createdAt);
                }
            }

            if (doc.contains("updatedAt") && doc.get("updatedAt") != null) {
                Object updatedAt = doc.get("updatedAt");
                if (updatedAt instanceof com.google.firebase.Timestamp) {
                    user.setUpdatedAt(((com.google.firebase.Timestamp) updatedAt).toDate().getTime());
                } else if (updatedAt instanceof Long) {
                    user.setUpdatedAt((Long) updatedAt);
                }
            }

            // Statut par défaut si non défini
            user.setStatus(doc.getString("status") != null ? doc.getString("status") : "offline");
            user.setLastActivity(doc.getLong("lastActivity") != null ? doc.getLong("lastActivity") : System.currentTimeMillis());

            // Rôles par défaut
            user.setManager(doc.getBoolean("isManager") != null ? doc.getBoolean("isManager") : false);
            user.setAdmin(doc.getBoolean("isAdmin") != null ? doc.getBoolean("isAdmin") : false);

            return user;
        } catch (Exception e) {
            errorMessage.setValue("Erreur lors de la conversion des données utilisateur: " + e.getMessage());
            return null;
        }
    }

    public void filterTeamMembers(String query, List<String> statusFilters, List<String> roleFilters) {
        isLoading.setValue(true);

        Query baseQuery = usersRef.orderBy("username");

        baseQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> members = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        User user = convertDocumentToUser(doc);
                        if (user != null) {
                            // Filtrer par nom/username
                            if (!query.isEmpty() &&
                                    !user.getUsername().toLowerCase().contains(query.toLowerCase()) &&
                                    !user.getFonction().toLowerCase().contains(query.toLowerCase())) {
                                continue;
                            }

                            // Filtrer par statut
                            if (!statusFilters.isEmpty() && !statusFilters.contains(user.getStatus())) {
                                continue;
                            }

                            // Filtrer par rôle/fonction
                            if (!roleFilters.isEmpty()) {
                                boolean matchesRole = false;
                                for (String roleFilter : roleFilters) {
                                    if (roleFilter.equals("Administrateur") && user.isAdmin()) {
                                        matchesRole = true;
                                        break;
                                    } else if (roleFilter.equals("Manager") && user.isManager()) {
                                        matchesRole = true;
                                        break;
                                    } else if (user.getFonction() != null && user.getFonction().equals(roleFilter)) {
                                        matchesRole = true;
                                        break;
                                    }
                                }
                                if (!matchesRole) continue;
                            }

                            members.add(user);
                        }
                    }
                    isLoading.setValue(false);
                    teamMembers.setValue(members);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Erreur lors du filtrage: " + e.getMessage());
                });
    }

    public void updateUserStatus(String userId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("lastActivity", System.currentTimeMillis());
        updates.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        usersRef.document(userId).update(updates)
                .addOnFailureListener(e -> errorMessage.setValue("Erreur lors de la mise à jour du statut: " + e.getMessage()));
    }

    public void getCurrentUser(OnUserLoadedListener listener) {
        String currentUserId = auth.getCurrentUser().getUid();
        usersRef.document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = convertDocumentToUser(documentSnapshot);
                    if (user != null) {
                        listener.onUserLoaded(user);
                    } else {
                        listener.onError("Utilisateur introuvable");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Erreur lors du chargement de l'utilisateur: " + e.getMessage()));
    }

    public void getUserById(String userId, OnUserLoadedListener listener) {
        usersRef.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = convertDocumentToUser(documentSnapshot);
                    if (user != null) {
                        listener.onUserLoaded(user);
                    } else {
                        listener.onError("Utilisateur introuvable");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Erreur lors du chargement de l'utilisateur: " + e.getMessage()));
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onError(String error);
    }
}