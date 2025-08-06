package com.example.nexusdoc.ui.utilitaires;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.nexusdoc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtils {

    public interface DialogCallback {
        void onPositive();
        default void onNegative() {}
        default void onPositive(int which) {} // Pour les choix simples
    }

    public interface InputDialogCallback {
        void onInput(String input);
    }

    // Méthodes existantes inchangées
    public static void showSuccessMessage(Context context, String message) {
        showCustomDialog(context, "Succès", message, R.drawable.ic_success,
                ContextCompat.getColor(context, R.color.colorSuccess), null);
    }

    public static void showErrorMessage(Context context, String message) {
        showCustomDialog(context, "Erreur", message, R.drawable.ic_error,
                ContextCompat.getColor(context, R.color.colorError), null);
    }

    public static void showInfoMessage(Context context, String message) {
        showCustomDialog(context, "Information", message, R.drawable.ic_info,
                ContextCompat.getColor(context, R.color.info_color), null);
    }

    public static void showWarningMessage(Context context, String message) {
        showCustomDialog(context, "Attention", message, R.drawable.ic_warning,
                ContextCompat.getColor(context, R.color.colorWarning), null);
    }

    // Nouvelle méthode pour les dialogues d'information avec titre
    public static void showInfoDialog(Context context, String title, String message) {
        showCustomDialog(context, title, message, R.drawable.ic_info,
                ContextCompat.getColor(context, R.color.info_color), null);
    }

    // Méthode de confirmation simplifiée
    public static void showConfirmationDialog(Context context, String title, String message,
                                              Runnable positiveAction) {
        showConfirmationDialog(context, title, message, "Confirmer", "Annuler",
                new DialogCallback() {
                    @Override
                    public void onPositive() {
                        positiveAction.run();
                    }
                });
    }

    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveText, String negativeText,
                                              DialogCallback callback) {
        new MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> {
                    if (callback != null) {
                        callback.onPositive();
                    }
                })
                .setNegativeButton(negativeText, (dialog, which) -> {
                    if (callback != null) {
                        callback.onNegative();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Affiche un message d'erreur avec option de réessayer
     * @param context Contexte
     * @param title Titre du dialogue
     * @param message Message d'erreur
     * @param retryAction Action à exécuter quand on clique sur "Réessayer"
     */
    public static void showErrorWithRetryDialog(Context context,
                                                String title,
                                                String message,
                                                Runnable retryAction) {
        new MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.retry), (dialog, which) -> {
                    if (retryAction != null) {
                        retryAction.run();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .setIcon(R.drawable.ic_error)
                .show();
    }

    public static Dialog showLoadingDialog(Context context, String message) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        TextView messageText = view.findViewById(R.id.loading_message);
        messageText.setText(message);

        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
        return dialog;
    }

    public static void showSingleChoiceDialog(Context context, String title, String[] items,
                                              int checkedItem, DialogCallback callback) {
        new MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog)
                .setTitle(title)
                .setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                    if (callback != null) {
                        callback.onPositive(which);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                    if (callback != null) {
                        callback.onNegative();
                    }
                })
                .show();
    }

    public static void showInputDialog(Context context, String title, String hint,
                                       InputDialogCallback callback) {
        showInputDialog(context, title, hint, "", callback);
    }

    public static void showInputDialog(Context context, String title, String hint,
                                       String initialValue, InputDialogCallback callback) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);

        com.google.android.material.textfield.TextInputLayout inputLayout =
                view.findViewById(R.id.input_layout);
        com.google.android.material.textfield.TextInputEditText inputText =
                view.findViewById(R.id.input_text);

        inputLayout.setHint(hint);
        inputText.setText(initialValue);

        new MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    String input = inputText.getText().toString().trim();
                    if (callback != null) {
                        callback.onInput(input);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private static void showCustomDialog(Context context, String title, String message,
                                         int iconRes, int iconColor, DialogCallback callback) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null);

        ImageView icon = view.findViewById(R.id.dialog_icon);
        TextView titleText = view.findViewById(R.id.dialog_title);
        TextView messageText = view.findViewById(R.id.dialog_message);
        MaterialButton okButton = view.findViewById(R.id.dialog_ok_button);

        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);
        titleText.setText(title);
        messageText.setText(message);

        okButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onPositive();
            }
            dialog.dismiss();
        });

        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);

            Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
            view.startAnimation(slideIn);
        }

        dialog.show();

        if (callback == null) {
            new android.os.Handler().postDelayed(dialog::dismiss, 3000);
        }
    }

    // Méthodes spécifiques
    public static void showLogoutDialog(Context context, DialogCallback callback) {
        showConfirmationDialog(context,
                "Déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter ?",
                "Déconnecter",
                "Annuler",
                callback);
    }

    public static void showDeleteDialog(Context context, String itemName, DialogCallback callback) {
        showConfirmationDialog(context,
                "Supprimer",
                "Êtes-vous sûr de vouloir supprimer " + itemName + " ?",
                "Supprimer",
                "Annuler",
                callback);
    }

    public static void showNetworkErrorDialog(Context context, DialogCallback retryCallback) {
        showCustomDialog(context,
                "Erreur de connexion",
                "Vérifiez votre connexion internet et réessayez.",
                R.drawable.ic_network_error,
                ContextCompat.getColor(context, R.color.colorError),
                retryCallback);
    }

    public static void showPermissionRequiredDialog(Context context, DialogCallback retryCallback) {
        showCustomDialog(context,
                "Permissions requises",
                "L'application a besoin des permissions pour fonctionner correctement",
                R.drawable.ic_warning,
                ContextCompat.getColor(context, R.color.colorWarning),
                retryCallback);
    }

    public static void showUpdateDialog(Context context, DialogCallback callback) {
        showConfirmationDialog(context,
                "Mise à jour disponible",
                "Une nouvelle version de l'application est disponible. Voulez-vous la télécharger ?",
                "Mettre à jour",
                "Plus tard",
                callback);
    }
}