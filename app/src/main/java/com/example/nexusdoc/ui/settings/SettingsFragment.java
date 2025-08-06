package com.example.nexusdoc.ui.settings;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.example.nexusdoc.BaseActivity;
import com.example.nexusdoc.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    // UI Components
    private MaterialSwitch themeSwitch;
    private MaterialSwitch notificationsSwitch;
    private RadioButton radioFrench, radioEnglish, radioSpanish;
    private TextView themeStatus, notificationsStatus;
    private ImageView themeIcon, notificationsIcon, helpArrow;
    private LinearLayout helpContent;

    private boolean isHelpExpanded = false;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        initializeViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initializeViews(View view) {
        // Theme components
        themeSwitch = view.findViewById(R.id.theme_switch);
        themeStatus = view.findViewById(R.id.theme_status);
        themeIcon = view.findViewById(R.id.theme_icon);

        // Notifications components
        notificationsSwitch = view.findViewById(R.id.notifications_switch);
        notificationsStatus = view.findViewById(R.id.notifications_status);
        notificationsIcon = view.findViewById(R.id.notifications_icon);

        // Language components
        radioFrench = view.findViewById(R.id.radio_french);
        radioEnglish = view.findViewById(R.id.radio_english);
        radioSpanish = view.findViewById(R.id.radio_spanish);

        // Help components
        helpArrow = view.findViewById(R.id.help_arrow);
        helpContent = view.findViewById(R.id.help_content);

        // Clickable containers
        LinearLayout themeContainer = view.findViewById(R.id.theme_container);
        LinearLayout notificationsContainer = view.findViewById(R.id.notifications_container);
        LinearLayout helpHeader = view.findViewById(R.id.help_header);
        LinearLayout frenchOption = view.findViewById(R.id.french_option);
        LinearLayout englishOption = view.findViewById(R.id.english_option);
        LinearLayout spanishOption = view.findViewById(R.id.spanish_option);

        // Set click listeners for containers
        themeContainer.setOnClickListener(v -> viewModel.toggleTheme());
        notificationsContainer.setOnClickListener(v -> viewModel.toggleNotifications());
        helpHeader.setOnClickListener(v -> toggleHelpSection());

        // Language selection listeners
        frenchOption.setOnClickListener(v -> selectLanguage("fr"));
        englishOption.setOnClickListener(v -> selectLanguage("en"));
        spanishOption.setOnClickListener(v -> selectLanguage("es"));
    }

    private void setupListeners() {
        // Plus besoin de listeners sur RadioGroup puisqu'on le supprime
        // La gestion se fait maintenant via les LinearLayout containers
    }

    private void selectLanguage(String languageCode) {
        // Décocher tous les radio buttons
        radioFrench.setChecked(false);
        radioEnglish.setChecked(false);
        radioSpanish.setChecked(false);

        // Cocher le bon radio button
        switch (languageCode) {
            case "en":
                radioEnglish.setChecked(true);
                break;
            case "es":
                radioSpanish.setChecked(true);
                break;
            default:
                radioFrench.setChecked(true);
                break;
        }

        // Appliquer le changement via le ViewModel
        viewModel.setLanguage(languageCode);
    }

    private void observeViewModel() {
        // Observe theme changes
        viewModel.isDarkTheme().observe(getViewLifecycleOwner(), isDarkTheme -> {
            updateThemeUI(isDarkTheme);
            // Update switch without triggering listener
            themeSwitch.setOnCheckedChangeListener(null);
            themeSwitch.setChecked(isDarkTheme);
            themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.toggleTheme());
        });

        // Observe notifications changes
        viewModel.notificationsEnabled().observe(getViewLifecycleOwner(), enabled -> {
            updateNotificationsUI(enabled);
            // Update switch without triggering listener
            notificationsSwitch.setOnCheckedChangeListener(null);
            notificationsSwitch.setChecked(enabled);
            notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.toggleNotifications());
        });

        // Observe language changes
        viewModel.currentLanguage().observe(getViewLifecycleOwner(), language -> {
            updateLanguageUI(language);
        });

        // Observe language change signal
        viewModel.languageChanged().observe(getViewLifecycleOwner(), newLanguage -> {
            if (newLanguage != null && getActivity() instanceof BaseActivity) {
                // Use BaseActivity method for proper language change
                ((BaseActivity) getActivity()).changeLanguageAndRecreate(newLanguage);
                viewModel.onLanguageChangeHandled();
            }
        });
    }

    private void updateThemeUI(boolean isDarkTheme) {
        if (isDarkTheme) {
            themeStatus.setText(R.string.enabled);
            themeIcon.setImageResource(R.drawable.ic_moon);
        } else {
            themeStatus.setText(R.string.disabled);
            themeIcon.setImageResource(R.drawable.ic_sun);
        }
    }

    private void updateNotificationsUI(boolean enabled) {
        if (enabled) {
            notificationsStatus.setText(R.string.enabled);
            notificationsIcon.setImageResource(R.drawable.ic_notification);
        } else {
            notificationsStatus.setText(R.string.disabled);
            notificationsIcon.setImageResource(R.drawable.ic_bell_off);
        }
    }

    private void updateLanguageUI(String language) {
        // Décocher tous les radio buttons
        radioFrench.setChecked(false);
        radioEnglish.setChecked(false);
        radioSpanish.setChecked(false);

        // Cocher le bon selon la langue
        switch (language) {
            case "en":
                radioEnglish.setChecked(true);
                break;
            case "es":
                radioSpanish.setChecked(true);
                break;
            default:
                radioFrench.setChecked(true);
                break;
        }
    }

    private void toggleHelpSection() {
        if (isHelpExpanded) {
            // Collapse
            helpContent.setVisibility(View.GONE);
            rotateArrow(90, 0);
        } else {
            // Expand
            helpContent.setVisibility(View.VISIBLE);
            rotateArrow(0, 90);
        }
        isHelpExpanded = !isHelpExpanded;
    }

    private void rotateArrow(float fromDegrees, float toDegrees) {
        RotateAnimation rotate = new RotateAnimation(
                fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(200);
        rotate.setFillAfter(true);
        helpArrow.startAnimation(rotate);
    }

    // Public methods to access current settings (if needed by parent Activity)
    public boolean isDarkThemeEnabled() {
        return viewModel.getCurrentTheme();
    }

    public boolean areNotificationsEnabled() {
        return viewModel.getCurrentNotifications();
    }

    public String getCurrentLanguage() {
        return viewModel.getCurrentLanguageCode();
    }
}