package com.example.nexusdoc.ui.archives.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.archives.model.ArchiveDocument;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<ArchiveDocument> documents;
    private OnDocumentClickListener listener;
    private boolean isGridView;

    public interface OnDocumentClickListener {
        void onDocumentClick(ArchiveDocument document);
        void onDocumentLongClick(ArchiveDocument document);
    }

    public DocumentAdapter(OnDocumentClickListener listener, boolean isGridView) {
        this.documents = new ArrayList<>();
        this.listener = listener;
        this.isGridView = isGridView;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isGridView ? R.layout.item_document_grid : R.layout.item_document_list;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        ArchiveDocument document = documents.get(position);
        holder.bind(document);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public void updateDocuments(List<ArchiveDocument> newDocuments) {
        this.documents = newDocuments != null ? newDocuments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setGridView(boolean isGridView) {
        this.isGridView = isGridView;
        notifyDataSetChanged();
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        private ImageView documentIcon, favoriteIcon, syncIcon;
        private TextView documentName, documentDate, documentSize, documentType;
        private View itemContainer, syncIndicator;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            documentIcon = itemView.findViewById(R.id.document_icon);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            syncIcon = itemView.findViewById(R.id.sync_icon);
            documentName = itemView.findViewById(R.id.document_name);
            documentDate = itemView.findViewById(R.id.document_date);
            documentSize = itemView.findViewById(R.id.document_size);
            documentType = itemView.findViewById(R.id.document_type);
            itemContainer = itemView.findViewById(R.id.item_container);
            syncIndicator = itemView.findViewById(R.id.sync_indicator);
        }

        public void bind(ArchiveDocument document) {
            // Set icon
            documentIcon.setImageResource(document.getIconResource());

            // Set document info
            documentName.setText(document.getName());
            documentSize.setText(document.getFormattedSize());

            if (documentType != null) {
                documentType.setText(document.getType().toUpperCase());
            }

            // Format and set date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            documentDate.setText(dateFormat.format(document.getDateModified()));

            // Set favorite state
            favoriteIcon.setImageResource(document.isFavorite() ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            // Set sync state
            if (syncIcon != null) {
                if (document.isSynced()) {
                    syncIcon.setImageResource(R.drawable.ic_cloud_done);
                    syncIcon.setVisibility(View.VISIBLE);
                } else if (document.needsSync()) {
                    syncIcon.setImageResource(R.drawable.ic_cloud_upload);
                    syncIcon.setVisibility(View.VISIBLE);
                } else {
                    syncIcon.setVisibility(View.GONE);
                }
            }

            // Set sync indicator
            if (syncIndicator != null) {
                syncIndicator.setVisibility(document.needsSync() ? View.VISIBLE : View.GONE);
            }

            // Set click listeners
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDocumentClick(document);
                }
            });

            itemContainer.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDocumentLongClick(document);
                }
                return true;
            });

            favoriteIcon.setOnClickListener(v -> toggleFavorite(document));
        }

        private void toggleFavorite(ArchiveDocument document) {
            document.setFavorite(!document.isFavorite());
            favoriteIcon.setImageResource(document.isFavorite() ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            // Notify repository to update the document
            // This would typically be handled through the ViewModel
        }
    }
}