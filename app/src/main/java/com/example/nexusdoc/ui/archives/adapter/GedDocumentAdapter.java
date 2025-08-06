package com.example.nexusdoc.ui.archives.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.archives.model.GedDocument;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GedDocumentAdapter extends RecyclerView.Adapter<GedDocumentAdapter.DocumentViewHolder> {

    private List<GedDocument> documents;
    private OnDocumentClickListener listener;
    private boolean isSelectionMode = false;
    private List<String> selectedItems = new ArrayList<>();

    public interface OnDocumentClickListener {
        void onDocumentClick(GedDocument document);
        void onDocumentLongClick(GedDocument document);
        void onFavoriteToggle(GedDocument document);
    }

    public GedDocumentAdapter(OnDocumentClickListener listener) {
        this.documents = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ged_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        GedDocument document = documents.get(position);
        holder.bind(document);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public void updateDocuments(List<GedDocument> newDocuments) {
        if (newDocuments == null) newDocuments = new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DocumentDiffCallback(this.documents, newDocuments));
        this.documents = new ArrayList<>(newDocuments);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) selectedItems.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(String documentId) {
        if (selectedItems.contains(documentId)) {
            selectedItems.remove(documentId);
        } else {
            selectedItems.add(documentId);
        }
        notifyDataSetChanged();
    }

    public List<String> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        private ImageView documentIcon, syncStatus, ocrIndicator, selectionIcon;
        private MaterialButton favoriteIcon;
        private MaterialTextView documentName, documentDate, documentSize;
        private Chip documentType;
        private View itemContainer, selectionOverlay;
        private LinearProgressIndicator progressIndicator;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            documentIcon = itemView.findViewById(R.id.document_icon);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            syncStatus = itemView.findViewById(R.id.sync_status);
            ocrIndicator = itemView.findViewById(R.id.ocr_indicator);
            selectionIcon = itemView.findViewById(R.id.selection_icon);
            documentName = itemView.findViewById(R.id.document_name);
            documentDate = itemView.findViewById(R.id.document_date);
            documentSize = itemView.findViewById(R.id.document_size);
            documentType = itemView.findViewById(R.id.document_type);
            itemContainer = itemView.findViewById(R.id.item_container);
            selectionOverlay = itemView.findViewById(R.id.selection_overlay);
            progressIndicator = itemView.findViewById(R.id.progress_indicator);
        }

        public void bind(GedDocument document) {
            // Icône du document selon le type
            documentIcon.setImageResource(getDocumentIcon(document.getMimeType()));

            // Nom du document
            documentName.setText(document.getName());

            // Taille du document
            documentSize.setText(document.getFormattedSize());

            // Type de document
            if (documentType != null) {
                String type = getFileExtension(document.getName());
                documentType.setText(type.toUpperCase(Locale.getDefault()));
            }

            // Date de création/modification
            if (documentDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                documentDate.setText(dateFormat.format(document.getCreatedAt()));
            }

            // État des favoris
            if (favoriteIcon != null) {
                favoriteIcon.setIconResource(document.isFavorite() ?
                        R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            }

            // Indicateur OCR
            if (ocrIndicator != null) {
                ocrIndicator.setVisibility(document.hasOcr() ? View.VISIBLE : View.GONE);
            }

            // État de synchronisation
            updateSyncState(document);
            updateSelectionState(document);
            setupClickListeners(document);
        }

        private void updateSyncState(GedDocument document) {
            if (syncStatus != null) {
                if (document.isSynced()) {
                    syncStatus.setImageResource(R.drawable.ic_cloud_done);
                    syncStatus.setVisibility(View.VISIBLE);
                } else {
                    syncStatus.setImageResource(R.drawable.ic_cloud_upload);
                    syncStatus.setVisibility(View.VISIBLE);
                }
            }

            if (progressIndicator != null) {
                progressIndicator.setVisibility(document.needsSync() ? View.VISIBLE : View.GONE);
            }
        }

        private void updateSelectionState(GedDocument document) {
            boolean isSelected = selectedItems.contains(document.getId());

            if (selectionIcon != null) {
                selectionIcon.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
                selectionIcon.setImageResource(isSelected ?
                        R.drawable.ic_check_circle : R.drawable.ic_radio_button_unchecked);
            }

            if (selectionOverlay != null) {
                selectionOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }

            itemContainer.setSelected(isSelected);
        }

        private void setupClickListeners(GedDocument document) {
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    if (isSelectionMode) {
                        toggleSelection(document.getId());
                    } else {
                        listener.onDocumentClick(document);
                    }
                }
            });

            itemContainer.setOnLongClickListener(v -> {
                if (listener != null && !isSelectionMode) {
                    listener.onDocumentLongClick(document);
                }
                return true;
            });

            if (favoriteIcon != null) {
                favoriteIcon.setOnClickListener(v -> {
                    // Animation de clic
                    favoriteIcon.animate()
                            .scaleX(1.2f).scaleY(1.2f).setDuration(100)
                            .withEndAction(() -> favoriteIcon.animate()
                                    .scaleX(1f).scaleY(1f).setDuration(100).start())
                            .start();

                    if (listener != null) {
                        listener.onFavoriteToggle(document);
                    }
                });
            }
        }

        private int getDocumentIcon(String mimeType) {
            if (mimeType == null) return R.drawable.ic_description;

            if (mimeType.startsWith("image/")) {
                return R.drawable.ic_image;
            } else if (mimeType.equals("application/pdf")) {
                return R.drawable.ic_document_pdf;
            } else if (mimeType.contains("word")) {
                return R.drawable.ic_word;
            } else if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) {
                return R.drawable.ic_excel;
            } else if (mimeType.contains("powerpoint") || mimeType.contains("presentation")) {
                return R.drawable.ic_powerpoint;
            } else if (mimeType.startsWith("text/")) {
                return R.drawable.ic_document;
            } else {
                return R.drawable.ic_description;
            }
        }

        private String getFileExtension(String fileName) {
            if (fileName == null || !fileName.contains(".")) {
                return "FILE";
            }
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
    }

    private static class DocumentDiffCallback extends DiffUtil.Callback {
        private final List<GedDocument> oldList;
        private final List<GedDocument> newList;

        public DocumentDiffCallback(List<GedDocument> oldList, List<GedDocument> newList) {
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
            GedDocument oldItem = oldList.get(oldItemPosition);
            GedDocument newItem = newList.get(newItemPosition);
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.isFavorite() == newItem.isFavorite() &&
                    oldItem.isSynced() == newItem.isSynced() &&
                    oldItem.getSize() == newItem.getSize();
        }
    }
}