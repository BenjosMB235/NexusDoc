package com.example.nexusdoc.ui.team.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nexusdoc.ui.profile.repository.ProfileRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.data.models.User;
import java.util.ArrayList;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
    private List<User> members;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(User user);
        void onMessageClick(User user);
        void onCallClick(User user);
        void onMenuClick(User user, View view);
    }

    public TeamAdapter(OnMemberClickListener listener) {
        this.members = new ArrayList<>();
        this.listener = listener;
    }

    public void setMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_membre_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        User user = members.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class TeamViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView ivAvatar;
        private View statusIndicator;
        private TextView tvName;
        private TextView tvRole;
        private TextView tvLastActivity;
        private MaterialButton btnMenu;
        private ChipGroup layoutBadges;
        private LinearLayout layoutActions;
        private Chip chipManager;
        private Chip chipAdmin;
        private MaterialButton btnMessage;
        private MaterialButton btnCall;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvLastActivity = itemView.findViewById(R.id.tv_last_activity);
            btnMenu = itemView.findViewById(R.id.btn_menu);
            layoutBadges = itemView.findViewById(R.id.layout_badges);
            layoutActions = itemView.findViewById(R.id.layout_actions);
            chipManager = itemView.findViewById(R.id.chip_manager);
            chipAdmin = itemView.findViewById(R.id.chip_admin);
            btnMessage = itemView.findViewById(R.id.btn_message);
            btnCall = itemView.findViewById(R.id.btn_call);
        }

        public void bind(User user) {
            // Nom d'affichage
            tvName.setText(user.getDisplayName());

            // Fonction/Rôle
            tvRole.setText(user.getRoleDisplay());

            // Dernière activité
            tvLastActivity.setText(user.getFormattedLastActivity());

            // Charger l'avatar depuis Base64
            loadAvatarFromBase64(user.getProfileImageBase64());

            // Définir la couleur du statut
            int statusColor;
            switch (user.getStatus()) {
                case "online":
                    statusColor = itemView.getContext().getColor(R.color.status_online);
                    break;
                case "away":
                    statusColor = itemView.getContext().getColor(R.color.status_away);
                    break;
                default:
                    statusColor = itemView.getContext().getColor(R.color.status_offline);
                    break;
            }
            statusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(statusColor));

            // Gérer les badges
            boolean showBadges = user.isManager() || user.isAdmin();
            layoutBadges.setVisibility(showBadges ? View.VISIBLE : View.GONE);

            if (showBadges) {
                chipManager.setVisibility(user.isManager() ? View.VISIBLE : View.GONE);
                chipAdmin.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);
            }

            // Gérer les actions rapides (les afficher au clic)
            layoutActions.setVisibility(View.GONE);

            // Listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMemberClick(user);
                }
                // Basculer l'affichage des actions
                layoutActions.setVisibility(
                        layoutActions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });

            btnMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(user, v);
                }
            });

            btnMessage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(user);
                }
            });

            btnCall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallClick(user);
                }
            });
        }

        private void loadAvatarFromBase64(String base64Image) {
            if (base64Image != null && !base64Image.isEmpty()) {
                Bitmap bitmap = ProfileRepository.base64ToBitmap(base64Image);
                if (bitmap != null) {
                    ivAvatar.setImageBitmap(bitmap);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_person);
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person);
            }
        }
    }
}