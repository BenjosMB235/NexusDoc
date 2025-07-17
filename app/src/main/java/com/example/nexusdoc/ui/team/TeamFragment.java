package com.example.nexusdoc.ui.team;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.ui.team.adapter.TeamAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.data.models.User;
import com.example.nexusdoc.ui.chat.ChatFragment;
import java.util.ArrayList;
import java.util.List;

public class TeamFragment extends Fragment implements TeamAdapter.OnMemberClickListener {
    private TeamViewModel viewModel;
    private TeamAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvTotalMembers;
    private TextView tvActiveMembers;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabAddMember;
    private View bottomSheetFilters;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    // Bottom sheet components
    private ChipGroup chipGroupSort;
    private ChipGroup chipGroupStatus;
    private ChipGroup chipGroupRoles;
    private MaterialButton btnResetFilters;
    private MaterialButton btnApplyFilters;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupSearchAndFilters();
        setupBottomSheet();
        setupListeners();

        // Charger les données
        viewModel.loadTeamMembers();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_team);
        tvTotalMembers = view.findViewById(R.id.tv_total_members);
        tvActiveMembers = view.findViewById(R.id.tv_active_members);
        etSearch = view.findViewById(R.id.et_search);
        btnFilter = view.findViewById(R.id.btn_filter);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        fabAddMember = view.findViewById(R.id.fab_add_member);
        bottomSheetFilters = view.findViewById(R.id.bottom_sheet_filters);

        // Bottom sheet components
        chipGroupSort = view.findViewById(R.id.chip_group_sort);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        chipGroupRoles = view.findViewById(R.id.chip_group_roles);
        btnResetFilters = view.findViewById(R.id.btn_reset_filters);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
    }

    private void setupRecyclerView() {
        adapter = new TeamAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        // Observer les membres de l'équipe
        viewModel.getFilteredMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                adapter.setMembers(members);
            }
        });

        // Observer les statistiques
        viewModel.getTeamStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                tvTotalMembers.setText(String.valueOf(stats.getTotalMembers()));
                tvActiveMembers.setText(String.valueOf(stats.getActiveMembers()));
            }
        });

        // Observer les erreurs
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observer l'état de chargement
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Vous pouvez afficher/masquer un indicateur de chargement ici
        });
    }

    private void setupSearchAndFilters() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchMembers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configuration des chips de filtres rapides
        setupFilterChips();
    }

    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    String filterText = chip.getText().toString();
                    applyQuickFilter(filterText);
                }
            }
        });
    }

    private void applyQuickFilter(String filter) {
        switch (filter) {
            case "Tous":
                viewModel.clearAllFilters();
                break;
            case "Actifs":
                viewModel.addStatusFilter("online");
                viewModel.addStatusFilter("away");
                break;
            case "Managers":
                viewModel.addRoleFilter("Manager");
                break;
            case "Récents":
                viewModel.setSortBy("joined");
                break;
        }
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetFilters);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setupListeners() {
        btnFilter.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        fabAddMember.setOnClickListener(v -> {
            // Naviguer vers l'écran d'ajout de membre
            showAddMemberDialog();
        });

        // Bottom sheet listeners
        btnResetFilters.setOnClickListener(v -> {
            viewModel.clearAllFilters();
            resetBottomSheetFilters();
        });

        btnApplyFilters.setOnClickListener(v -> {
            applyBottomSheetFilters();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        setupBottomSheetChips();
    }

    private void setupBottomSheetChips() {
        // Configuration des chips de tri
        chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    String sortType = getSortTypeFromChip(selectedChip.getText().toString());
                    viewModel.setSortBy(sortType);
                }
            }
        });

        // Configuration des chips de statut
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> statusFilters = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    String status = getStatusFromChip(chip.getText().toString());
                    statusFilters.add(status);
                }
            }
            // Appliquer les filtres de statut
        });

        // Configuration des chips de rôles
        chipGroupRoles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> roleFilters = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    String role = getRoleFromChip(chip.getText().toString());
                    roleFilters.add(role);
                }
            }
            // Appliquer les filtres de rôles
        });
    }

    private String getSortTypeFromChip(String chipText) {
        switch (chipText) {
            case "Nom": return "name";
            case "Rôle": return "role";
            case "Activité récente": return "activity";
            case "Date d'ajout": return "joined";
            default: return "name";
        }
    }

    private String getStatusFromChip(String chipText) {
        switch (chipText) {
            case "En ligne": return "online";
            case "Absent": return "away";
            case "Hors ligne": return "offline";
            default: return "offline";
        }
    }

    private String getRoleFromChip(String chipText) {
        switch (chipText) {
            case "Administrateur": return "Administrateur";
            case "Manager": return "Manager";
            case "Développeur": return "Développeur";
            case "Designer": return "Designer";
            case "Analyste": return "Analyste";
            default: return chipText;
        }
    }

    private void resetBottomSheetFilters() {
        chipGroupSort.check(R.id.chip_sort_name);
        chipGroupStatus.clearCheck();
        chipGroupRoles.clearCheck();
    }

    private void applyBottomSheetFilters() {
        // Les filtres sont appliqués en temps réel via les listeners
        Toast.makeText(getContext(), "Filtres appliqués", Toast.LENGTH_SHORT).show();
    }

    private void showAddMemberDialog() {
        // Implémenter une boîte de dialogue pour ajouter un membre
        // Ou naviguer vers un autre fragment
        Toast.makeText(getContext(), "Fonction d'ajout de membre à implémenter", Toast.LENGTH_SHORT).show();
    }

    // Implémentation des callbacks de l'adapter
    @Override
    public void onMemberClick(User user) {
        // Afficher les détails du membre ou naviguer vers son profil
        Toast.makeText(getContext(), "Profil de " + user.getUsername(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageClick(User user) {
        // Naviguer vers l'écran de chat
        Bundle args = new Bundle();
        args.putString("userId", user.getId());
        args.putString("userName", user.getUsername());
        args.putString("userAvatar", user.getProfileImageBase64());
        Navigation.findNavController(getView()).navigate(R.id.action_team_to_chat, args);
    }

    @Override
    public void onCallClick(User user) {
        // Initier un appel
        Toast.makeText(getContext(), "Appel à " + user.getUsername(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMenuClick(User user, View view) {
        // Afficher un menu contextuel
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.member_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_view_profile) {
                // Voir le profil
                return true;
            }
            if (id == R.id.action_send_message) {
                onMessageClick(user);
                return true;
            }
            if (id == R.id.action_call) {
                onCallClick(user);
                return true;
            }
            if (id == R.id.action_remove) {
                // Supprimer le membre (avec confirmation)
                showRemoveConfirmation(user);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showRemoveConfirmation(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Supprimer le membre")
                .setMessage("Êtes-vous sûr de vouloir supprimer " + user.getUsername() + " de l'équipe ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    viewModel.removeTeamMember(user.getId());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}