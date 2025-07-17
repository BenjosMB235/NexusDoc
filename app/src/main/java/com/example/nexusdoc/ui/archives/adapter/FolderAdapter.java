package com.example.nexusdoc.ui.archives.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.archives.model.ArchiveFolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<ArchiveFolder> folders;
    private OnFolderClickListener listener;
    private boolean isGridView;

    public interface OnFolderClickListener {
        void onFolderClick(ArchiveFolder folder);
        void onFolderLongClick(ArchiveFolder folder);
    }

    public FolderAdapter(OnFolderClickListener listener, boolean isGridView) {
        this.folders = new ArrayList<>();
        this.listener = listener;
        this.isGridView = isGridView;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isGridView ? R.layout.item_folder_grid : R.layout.item_folder_list;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        ArchiveFolder folder = folders.get(position);
        holder.bind(folder);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public void updateFolders(List<ArchiveFolder> newFolders) {
        this.folders = newFolders != null ? newFolders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setGridView(boolean isGridView) {
        this.isGridView = isGridView;
        notifyDataSetChanged();
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        private ImageView folderIcon, syncIcon;
        private TextView folderName, folderInfo, folderDate, folderSize;
        private View itemContainer, syncIndicator;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderIcon = itemView.findViewById(R.id.folder_icon);
            syncIcon = itemView.findViewById(R.id.sync_icon);
            folderName = itemView.findViewById(R.id.folder_name);
            folderInfo = itemView.findViewById(R.id.folder_info);
            folderDate = itemView.findViewById(R.id.folder_date);
            folderSize = itemView.findViewById(R.id.folder_size);
            itemContainer = itemView.findViewById(R.id.item_container);
            syncIndicator = itemView.findViewById(R.id.sync_indicator);
        }

        public void bind(ArchiveFolder folder) {
            // Set icon
            folderIcon.setImageResource(folder.getIconResource());

            // Set folder info
            folderName.setText(folder.getName());
            folderInfo.setText(folder.getDisplayInfo());

            if (folderSize != null) {
                folderSize.setText(folder.getFormattedSize());
            }

            // Format and set date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (folderDate != null) {
                folderDate.setText(dateFormat.format(folder.getDateModified()));
            }

            // Set sync state
            if (syncIcon != null) {
                if (folder.isSynced()) {
                    syncIcon.setImageResource(R.drawable.ic_cloud_done);
                    syncIcon.setVisibility(View.VISIBLE);
                } else if (folder.needsSync()) {
                    syncIcon.setImageResource(R.drawable.ic_cloud_upload);
                    syncIcon.setVisibility(View.VISIBLE);
                } else {
                    syncIcon.setVisibility(View.GONE);
                }
            }

            // Set sync indicator
            if (syncIndicator != null) {
                syncIndicator.setVisibility(folder.needsSync() ? View.VISIBLE : View.GONE);
            }

            // Set click listeners
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFolderClick(folder);
                }
            });

            itemContainer.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onFolderLongClick(folder);
                }
                return true;
            });
        }
    }
}