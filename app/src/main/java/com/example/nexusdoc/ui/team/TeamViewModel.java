package com.example.nexusdoc.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nexusdoc.ui.data.models.User;
import com.example.nexusdoc.ui.team.repository.TeamRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeamViewModel extends ViewModel {
    private TeamRepository teamRepository;
    private MutableLiveData<List<User>> filteredMembers;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<List<String>> activeStatusFilters;
    private MutableLiveData<List<String>> activeRoleFilters;
    private MutableLiveData<String> sortBy;
    private MutableLiveData<TeamStats> teamStats;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    private List<User> originalMembers;

    public TeamViewModel() {
        teamRepository = TeamRepository.getInstance();
        filteredMembers = new MutableLiveData<>();
        searchQuery = new MutableLiveData<>("");
        activeStatusFilters = new MutableLiveData<>(new ArrayList<>());
        activeRoleFilters = new MutableLiveData<>(new ArrayList<>());
        sortBy = new MutableLiveData<>("name");
        teamStats = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        originalMembers = new ArrayList<>();

        // Observer les changements dans les membres de l'équipe
        teamRepository.getTeamMembers().observeForever(members -> {
            if (members != null) {
                originalMembers = members;
                updateTeamStats(members);
                applyFiltersAndSort();
            }
        });
    }

    public LiveData<List<User>> getTeamMembers() {
        return teamRepository.getTeamMembers();
    }

    public LiveData<List<User>> getFilteredMembers() {
        return filteredMembers;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<TeamStats> getTeamStats() {
        return teamStats;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void loadTeamMembers() {
        isLoading.setValue(true);
        teamRepository.loadTeamMembers();
    }

    public void searchMembers(String query) {
        searchQuery.setValue(query);
        applyFiltersAndSort();
    }

    public void addStatusFilter(String status) {
        List<String> currentFilters = activeStatusFilters.getValue();
        if (currentFilters != null && !currentFilters.contains(status)) {
            currentFilters.add(status);
            activeStatusFilters.setValue(currentFilters);
            applyFiltersAndSort();
        }
    }

    public void removeStatusFilter(String status) {
        List<String> currentFilters = activeStatusFilters.getValue();
        if (currentFilters != null && currentFilters.contains(status)) {
            currentFilters.remove(status);
            activeStatusFilters.setValue(currentFilters);
            applyFiltersAndSort();
        }
    }

    public void addRoleFilter(String role) {
        List<String> currentFilters = activeRoleFilters.getValue();
        if (currentFilters != null && !currentFilters.contains(role)) {
            currentFilters.add(role);
            activeRoleFilters.setValue(currentFilters);
            applyFiltersAndSort();
        }
    }

    public void removeRoleFilter(String role) {
        List<String> currentFilters = activeRoleFilters.getValue();
        if (currentFilters != null && currentFilters.contains(role)) {
            currentFilters.remove(role);
            activeRoleFilters.setValue(currentFilters);
            applyFiltersAndSort();
        }
    }

    public void setSortBy(String sort) {
        sortBy.setValue(sort);
        applyFiltersAndSort();
    }

    public void clearAllFilters() {
        searchQuery.setValue("");
        activeStatusFilters.setValue(new ArrayList<>());
        activeRoleFilters.setValue(new ArrayList<>());
        sortBy.setValue("name");
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        List<User> result = new ArrayList<>(originalMembers);

        // Exclure l'utilisateur actif
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        result.removeIf(user -> user.getId().equals(currentUserId));

        // Appliquer la recherche
        String query = searchQuery.getValue();
        if (query != null && !query.isEmpty()) {
            result = result.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                            user.getFonction().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Appliquer les filtres de statut
        List<String> statusFilters = activeStatusFilters.getValue();
        if (statusFilters != null && !statusFilters.isEmpty()) {
            result = result.stream()
                    .filter(user -> statusFilters.contains(user.getStatus()))
                    .collect(Collectors.toList());
        }

        // Appliquer les filtres de rôle
        List<String> roleFilters = activeRoleFilters.getValue();
        if (roleFilters != null && !roleFilters.isEmpty()) {
            result = result.stream()
                    .filter(user -> roleFilters.contains(user.getFonction()))
                    .collect(Collectors.toList());
        }

        // Appliquer le tri
        String sort = sortBy.getValue();
        if (sort != null) {
            switch (sort) {
                case "name":
                    result.sort((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()));
                    break;
                case "role":
                    result.sort((a, b) -> a.getFonction().compareToIgnoreCase(b.getFonction()));
                    break;
                case "activity":
                    result.sort((a, b) -> Long.compare(b.getLastActivity(), a.getLastActivity()));
                    break;
                case "joined":
                    //result.sort((a, b) -> Long.compare(b.getJoinedAt(), a.getJoinedAt()));
                    break;
            }
        }

        filteredMembers.setValue(result);
    }

    private void updateTeamStats(List<User> members) {
        int totalMembers = members.size();
        int activeMembers = 0;
        int managers = 0;
        int admins = 0;

        for (User user : members) {
            if ("online".equals(user.getStatus()) || "away".equals(user.getStatus())) {
                activeMembers++;
            }
            if (user.isManager()) {
                managers++;
            }
            if (user.isAdmin()) {
                admins++;
            }
        }

        teamStats.setValue(new TeamStats(totalMembers, activeMembers, managers, admins));
    }

    public void updateActivityStatus() {
        for (User user : originalMembers) {
            if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                user.setStatus("online");
            } else {
                user.setStatus("offline");
            }
        }
        applyFiltersAndSort();
    }

    public void addTeamMember(User user) {
        //teamRepository.addTeamMember(user);
    }

    public void removeTeamMember(String userId) {
        //teamRepository.removeTeamMember(userId);
    }

    public void updateUserStatus(String userId, String status) {
        teamRepository.updateUserStatus(userId, status);
    }

    public static class TeamStats {
        private int totalMembers;
        private int activeMembers;
        private int managers;
        private int admins;

        public TeamStats(int totalMembers, int activeMembers, int managers, int admins) {
            this.totalMembers = totalMembers;
            this.activeMembers = activeMembers;
            this.managers = managers;
            this.admins = admins;
        }

        public int getTotalMembers() { return totalMembers; }
        public int getActiveMembers() { return activeMembers; }
        public int getManagers() { return managers; }
        public int getAdmins() { return admins; }
    }
}