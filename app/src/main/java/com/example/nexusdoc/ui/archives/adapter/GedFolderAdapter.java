package com.example.nexusdoc.ui.archives.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.archives.model.GedFolder;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class GedFolderAdapter extends RecyclerView.Adapter<GedFolderAdapter.FolderViewHolder> {

    private List<GedFolder> folders;
    private OnFolderClickListener listener;
    private boolean isGridView;
    private boolean isSelectionMode = false;
    private List<String> selectedItems = new ArrayList<>();

    public interface OnFolderClickListener {
        void onFolderClick(GedFolder folder);
        void onFolderLongClick(GedFolder folder);
        void onMenuClick(GedFolder folder, View view);
    }

    public GedFolderAdapter(OnFolderClickListener listener) {
        this.folders = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ged_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        GedFolder folder = folders.get(position);
        holder.bind(folder);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public void updateFolders(List<GedFolder> newFolders) {
        if (newFolders == null) newFolders = new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FolderDiffCallback(this.folders, newFolders));
        this.folders = new ArrayList<>(newFolders);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) selectedItems.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(String folderId) {
        if (selectedItems.contains(folderId)) {
            selectedItems.remove(folderId);
        } else {
            selectedItems.add(folderId);
        }
        notifyDataSetChanged();
    }

    public List<String> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        private ImageView folderIcon, folderCustomIcon, syncStatus, sharedIndicator, selectionIcon, btnMore;
        private MaterialTextView folderName, folderDocumentCount, folderSubfolderCount, folderPath;
        // ShapeableImageView btnMore;
        private View itemContainer, separatorDot, selectionOverlay;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderIcon = itemView.findViewById(R.id.folder_icon);
            folderCustomIcon = itemView.findViewById(R.id.folder_custom_icon);
            folderName = itemView.findViewById(R.id.folder_name);
            folderDocumentCount = itemView.findViewById(R.id.folder_document_count);
            folderSubfolderCount = itemView.findViewById(R.id.folder_subfolder_count);
            folderPath = itemView.findViewById(R.id.folder_path);
            btnMore = itemView.findViewById(R.id.btn_more);
            sharedIndicator = itemView.findViewById(R.id.shared_indicator);
            syncStatus = itemView.findViewById(R.id.sync_status);
            selectionIcon = itemView.findViewById(R.id.selection_icon);
            itemContainer = itemView.findViewById(R.id.item_container);
            separatorDot = itemView.findViewById(R.id.separator_dot);
            selectionOverlay = itemView.findViewById(R.id.selection_overlay);
        }

        public void bind(GedFolder folder) {
            // Nom du dossier
            folderName.setText(folder.getName());

            // Compteur de documents
            String docText = folder.getDocumentCount() + " file";
            if (folder.getDocumentCount() > 1) docText += "s";
            folderDocumentCount.setText(docText);

            // Compteur de sous-dossiers
            if (folderSubfolderCount != null) {
                if (folder.getSubfolderCount() > 0) {
                    String subText = folder.getSubfolderCount() + " folder";
                    if (folder.getSubfolderCount() > 1) subText += "s";
                    folderSubfolderCount.setText(subText);
                    folderSubfolderCount.setVisibility(View.VISIBLE);
                    if (separatorDot != null) separatorDot.setVisibility(View.VISIBLE);
                } else {
                    folderSubfolderCount.setText("0 folder");
                    folderSubfolderCount.setVisibility(View.VISIBLE);
                    if (separatorDot != null) separatorDot.setVisibility(View.VISIBLE);
                }
            }

            // Chemin du dossier
            if (folderPath != null && folder.getPath() != null) {
                folderPath.setText(folder.getPath());
                folderPath.setVisibility(View.VISIBLE);
            }

            // Icône personnalisée si disponible
            if (folderCustomIcon != null) {
                folderCustomIcon.setVisibility(View.GONE); // Par défaut masqué
            }

            // Indicateur de partage
            if (sharedIndicator != null) {
                sharedIndicator.setVisibility(View.GONE); // À implémenter selon vos besoins
            }

            updateSyncState(folder);
            updateSelectionState(folder);
            setupClickListeners(folder);
        }

        private void updateSyncState(GedFolder folder) {
            if (syncStatus != null) {
                if (folder.isSynced()) {
                    syncStatus.setImageResource(R.drawable.ic_cloud_done);
                    syncStatus.setVisibility(View.VISIBLE);
                } else {
                    syncStatus.setImageResource(R.drawable.ic_cloud_upload);
                    syncStatus.setVisibility(View.VISIBLE);
                }
            }
        }

        private void updateSelectionState(GedFolder folder) {
            boolean isSelected = selectedItems.contains(folder.getId());

            if (selectionIcon != null) {
                selectionIcon.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
                selectionIcon.setImageResource(isSelected ?
                        R.drawable.ic_check_circle : R.drawable.ic_radio_button_unchecked);
            }

            if (selectionOverlay != null) {
                selectionOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }

            itemContainer.setSelected(isSelected);

            // Masquer le bouton menu en mode sélection
            if (btnMore != null) {
                btnMore.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            }
        }

        private void setupClickListeners(GedFolder folder) {
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    if (isSelectionMode) {
                        toggleSelection(folder.getId());
                    } else {
                        // Animation de clic
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                                .start();
                        listener.onFolderClick(folder);
                    }
                }
            });

            itemContainer.setOnLongClickListener(v -> {
                if (listener != null && !isSelectionMode) {
                    listener.onFolderLongClick(folder);
                }
                return true;
            });

            if (btnMore != null) {
                btnMore.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMenuClick(folder, v);
                    }
                });
            }
        }
    }

    private static class FolderDiffCallback extends DiffUtil.Callback {
        private final List<GedFolder> oldList;
        private final List<GedFolder> newList;

        public FolderDiffCallback(List<GedFolder> oldList, List<GedFolder> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            GedFolder oldItem = oldList.get(oldItemPosition);
            GedFolder newItem = newList.get(newItemPosition);
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDocumentCount() == newItem.getDocumentCount() &&
                    oldItem.getSubfolderCount() == newItem.getSubfolderCount() &&
                    oldItem.isSynced() == newItem.isSynced();
        }
    }
}