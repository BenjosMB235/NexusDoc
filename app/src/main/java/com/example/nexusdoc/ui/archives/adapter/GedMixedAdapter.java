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
import com.example.nexusdoc.ui.archives.model.GedFolder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class GedMixedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOLDER = 0;
    private static final int TYPE_DOCUMENT = 1;

    private List<Object> items = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean isSelectionMode = false;
    private List<String> selectedItems = new ArrayList<>();

    public interface OnItemClickListener {
        void onFolderClick(GedFolder folder);
        void onFolderLongClick(GedFolder folder);
        void onDocumentClick(GedDocument document);
        void onDocumentLongClick(GedDocument document);
        void onFavoriteToggle(GedDocument document);
    }

    public GedMixedAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateItems(List<GedFolder> folders, List<GedDocument> documents) {
        List<Object> newItems = new ArrayList<>();
        if (folders != null) {
            newItems.addAll(folders);
        }
        if (documents != null) {
            newItems.addAll(documents);
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MixedDiffCallback(this.items, newItems));
        this.items = newItems;
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) selectedItems.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(String itemId) {
        if (selectedItems.contains(itemId)) {
            selectedItems.remove(itemId);
        } else {
            selectedItems.add(itemId);
        }
        notifyDataSetChanged();
    }

    public List<String> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        return item instanceof GedFolder ? TYPE_FOLDER : TYPE_DOCUMENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_FOLDER) {
            View view = inflater.inflate(R.layout.item_ged_folder, parent, false);
            return new FolderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_ged_document, parent, false);
            return new DocumentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof FolderViewHolder && item instanceof GedFolder) {
            ((FolderViewHolder) holder).bind((GedFolder) item);
        } else if (holder instanceof DocumentViewHolder && item instanceof GedDocument) {
            ((DocumentViewHolder) holder).bind((GedDocument) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // FolderViewHolder - basé sur GedFolderAdapter
    class FolderViewHolder extends RecyclerView.ViewHolder {
        private ImageView folderIcon, folderCustomIcon, syncStatus, sharedIndicator, selectionIcon, btnMore;
        private MaterialTextView folderName, folderDocumentCount, folderSubfolderCount, folderPath;
        private View itemContainer, separatorDot, selectionOverlay;

        FolderViewHolder(@NonNull View itemView) {
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

        void bind(GedFolder folder) {
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
                folderCustomIcon.setVisibility(View.GONE);
            }

            // Indicateur de partage
            if (sharedIndicator != null) {
                sharedIndicator.setVisibility(View.GONE);
            }

            updateFolderSyncState(folder);
            updateFolderSelectionState(folder);
            setupFolderClickListeners(folder);
        }

        private void updateFolderSyncState(GedFolder folder) {
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

        private void updateFolderSelectionState(GedFolder folder) {
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

            if (btnMore != null) {
                btnMore.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            }
        }

        private void setupFolderClickListeners(GedFolder folder) {
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    if (isSelectionMode) {
                        toggleSelection(folder.getId());
                    } else {
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
                        listener.onFolderLongClick(folder);
                    }
                });
            }
        }
    }

    // DocumentViewHolder - basé sur GedDocumentAdapter
    class DocumentViewHolder extends RecyclerView.ViewHolder {
        private ImageView documentIcon, syncStatus, ocrIndicator, selectionIcon;
        private MaterialButton favoriteIcon;
        private MaterialTextView documentName, documentDate, documentSize;
        private Chip documentType;
        private View itemContainer, selectionOverlay;
        private LinearProgressIndicator progressIndicator;

        DocumentViewHolder(@NonNull View itemView) {
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

        void bind(GedDocument document) {
            // Icône du document selon le type
            documentIcon.setImageResource(getDocumentIcon(document.getMimeType()));

            // Nom du document
            documentName.setText(document.getName());

            // Taille du document
            documentSize.setText(document.getFormattedSize());

            // Type de document avec logique améliorée
            if (documentType != null) {
                String type = getDocumentTypeForDisplay(document);
                documentType.setText(type);

                // Couleur du chip selon le type
                setChipColorByType(documentType, type);
            }

            // Date de création/modification avec format amélioré
            if (documentDate != null) {
                documentDate.setText(getFormattedDate(document.getCreatedAt()));
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

            updateDocumentSyncState(document);
            updateDocumentSelectionState(document);
            setupDocumentClickListeners(document);
        }

        private void updateDocumentSyncState(GedDocument document) {
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

        private void updateDocumentSelectionState(GedDocument document) {
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

        private void setupDocumentClickListeners(GedDocument document) {
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

        private String getFormattedDate(Date date) {
            if (date == null) return "";

            Calendar now = Calendar.getInstance();
            Calendar docDate = Calendar.getInstance();
            docDate.setTime(date);

            long diffInMillis = now.getTimeInMillis() - docDate.getTimeInMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

            // Si c'est aujourd'hui
            if (diffInDays == 0) {
                if (diffInHours == 0) {
                    if (diffInMinutes <= 1) {
                        return "À l'instant";
                    } else if (diffInMinutes < 60) {
                        return "Il y a " + diffInMinutes + " min";
                    }
                }
                // Format heure sans secondes pour aujourd'hui
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return "Aujourd'hui, " + timeFormat.format(date);
            }
            // Si c'est hier
            else if (diffInDays == 1) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return "Hier, " + timeFormat.format(date);
            }
            // Si c'est cette semaine (moins de 7 jours)
            else if (diffInDays < 7) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, HH:mm", Locale.getDefault());
                return dayFormat.format(date);
            }
            // Si c'est cette année
            else if (now.get(Calendar.YEAR) == docDate.get(Calendar.YEAR)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
                return dateFormat.format(date);
            }
            // Pour les dates plus anciennes
            else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return dateFormat.format(date);
            }
        }

        /**
         * Détermine le type de document à afficher avec une logique améliorée
         */
        private String getDocumentTypeForDisplay(GedDocument document) {
            String fileName = document.getName();
            String mimeType = document.getMimeType();

            // D'abord essayer avec le mimeType pour plus de précision
            if (mimeType != null) {
                switch (mimeType.toLowerCase()) {
                    case "application/pdf":
                        return "PDF";
                    case "application/msword":
                    case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                        return "DOC";
                    case "application/vnd.ms-excel":
                    case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                        return "XLS";
                    case "application/vnd.ms-powerpoint":
                    case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                        return "PPT";
                    case "text/plain":
                        return "TXT";
                    case "application/zip":
                        return "ZIP";
                    case "application/x-rar-compressed":
                        return "RAR";
                }

                // Pour les images
                if (mimeType.startsWith("image/")) {
                    if (mimeType.contains("jpeg") || mimeType.contains("jpg")) return "JPG";
                    if (mimeType.contains("png")) return "PNG";
                    if (mimeType.contains("gif")) return "GIF";
                    if (mimeType.contains("svg")) return "SVG";
                    return "IMG";
                }

                // Pour les vidéos
                if (mimeType.startsWith("video/")) {
                    return "VID";
                }

                // Pour l'audio
                if (mimeType.startsWith("audio/")) {
                    return "AUD";
                }
            }

            // Fallback sur l'extension du fichier
            if (fileName != null && fileName.contains(".")) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

                // Normaliser certaines extensions communes
                switch (extension) {
                    case "JPEG":
                        return "JPG";
                    case "DOCX":
                        return "DOC";
                    case "XLSX":
                        return "XLS";
                    case "PPTX":
                        return "PPT";
                    default:
                        // Limiter la longueur pour l'affichage
                        return extension.length() > 4 ? extension.substring(0, 4) : extension;
                }
            }

            return "FILE";
        }

        /**
         * Définit la couleur du chip selon le type de document
         */
        private void setChipColorByType(Chip chip, String type) {
            int colorRes;

            switch (type.toUpperCase()) {
                case "PDF":
                    colorRes = R.color.chip_pdf; // Rouge/Orange
                    break;
                case "DOC":
                case "DOCX":
                    colorRes = R.color.chip_word; // Bleu
                    break;
                case "XLS":
                case "XLSX":
                    colorRes = R.color.chip_excel; // Vert
                    break;
                case "PPT":
                case "PPTX":
                    colorRes = R.color.chip_powerpoint; // Orange/Rouge
                    break;
                case "JPG":
                case "PNG":
                case "GIF":
                case "SVG":
                case "IMG":
                    colorRes = R.color.chip_image; // Violet
                    break;
                case "VID":
                    colorRes = R.color.chip_video; // Bleu foncé
                    break;
                case "AUD":
                    colorRes = R.color.chip_audio; // Vert foncé
                    break;
                case "ZIP":
                case "RAR":
                    colorRes = R.color.chip_archive; // Gris
                    break;
                default:
                    colorRes = R.color.chip_default; // Couleur par défaut
                    break;
            }

            try {
                chip.setChipBackgroundColorResource(colorRes);
            } catch (Exception e) {
                // Fallback vers la couleur par défaut si la ressource n'existe pas
                chip.setChipBackgroundColor(chip.getContext().getResources()
                        .getColorStateList(R.color.chip_default, chip.getContext().getTheme()));
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

    // DiffCallback pour optimiser les mises à jour
    private static class MixedDiffCallback extends DiffUtil.Callback {
        private final List<Object> oldList;
        private final List<Object> newList;

        public MixedDiffCallback(List<Object> oldList, List<Object> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Object oldItem = oldList.get(oldItemPosition);
            Object newItem = newList.get(newItemPosition);

            if (oldItem.getClass() != newItem.getClass()) {
                return false;
            }

            if (oldItem instanceof GedFolder && newItem instanceof GedFolder) {
                return ((GedFolder) oldItem).getId().equals(((GedFolder) newItem).getId());
            } else if (oldItem instanceof GedDocument && newItem instanceof GedDocument) {
                return ((GedDocument) oldItem).getId().equals(((GedDocument) newItem).getId());
            }

            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Object oldItem = oldList.get(oldItemPosition);
            Object newItem = newList.get(newItemPosition);

            if (oldItem instanceof GedFolder && newItem instanceof GedFolder) {
                GedFolder oldFolder = (GedFolder) oldItem;
                GedFolder newFolder = (GedFolder) newItem;
                return oldFolder.getName().equals(newFolder.getName()) &&
                        oldFolder.getDocumentCount() == newFolder.getDocumentCount() &&
                        oldFolder.getSubfolderCount() == newFolder.getSubfolderCount() &&
                        oldFolder.isSynced() == newFolder.isSynced();
            } else if (oldItem instanceof GedDocument && newItem instanceof GedDocument) {
                GedDocument oldDoc = (GedDocument) oldItem;
                GedDocument newDoc = (GedDocument) newItem;
                return oldDoc.getName().equals(newDoc.getName()) &&
                        oldDoc.isFavorite() == newDoc.isFavorite() &&
                        oldDoc.isSynced() == newDoc.isSynced() &&
                        oldDoc.getSize() == newDoc.getSize();
            }

            return false;
        }
    }
}