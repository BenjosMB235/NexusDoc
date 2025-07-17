package com.example.nexusdoc.ui.about;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.nexusdoc.R;
import com.example.nexusdoc.ui.about.AboutViewModel;

public class AboutFragment extends Fragment {

    private AboutViewModel viewModel;

    // UI Components
    private TextView appNameTextView;
    private TextView appVersionTextView;
    private TextView appDescriptionTextView;
    private TextView developerNameTextView;
    private TextView developerEmailTextView;
    private TextView developerPhoneTextView;
    private TextView developerWebsiteTextView;
    private TextView appVersionDetailedTextView;
    private TextView buildDateTextView;

    // Containers cliquables
    private LinearLayout emailContainer;
    private LinearLayout phoneContainer;
    private LinearLayout websiteContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        initializeViews(view);
        initializeViewModel();
        setupClickListeners();
        observeViewModel();

        return view;
    }

    private void initializeViews(View view) {
        // TextViews
        appNameTextView = view.findViewById(R.id.app_name);
        appVersionTextView = view.findViewById(R.id.app_version);
        appDescriptionTextView = view.findViewById(R.id.app_description);
        developerNameTextView = view.findViewById(R.id.developer_name);
        developerEmailTextView = view.findViewById(R.id.developer_email);
        developerPhoneTextView = view.findViewById(R.id.developer_phone);
        developerWebsiteTextView = view.findViewById(R.id.developer_website);
        appVersionDetailedTextView = view.findViewById(R.id.app_version_detailed);
        buildDateTextView = view.findViewById(R.id.build_date);

        // Containers cliquables
        emailContainer = view.findViewById(R.id.email_container);
        phoneContainer = view.findViewById(R.id.phone_container);
        websiteContainer = view.findViewById(R.id.website_container);
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(AboutViewModel.class);
    }

    private void setupClickListeners() {
        // Email - Déléguer au ViewModel
        emailContainer.setOnClickListener(v -> viewModel.onEmailClicked());

        // Téléphone - Déléguer au ViewModel
        phoneContainer.setOnClickListener(v -> viewModel.onPhoneClicked());

        // Site web - Déléguer au ViewModel
        websiteContainer.setOnClickListener(v -> viewModel.onWebsiteClicked());
    }

    private void observeViewModel() {
        // Observer les informations de l'application
        viewModel.appInfo.observe(getViewLifecycleOwner(), this::updateUI);

        // Observer les événements d'action
        viewModel.actionEvent.observe(getViewLifecycleOwner(), this::handleActionEvent);
    }

    private void updateUI(AboutViewModel.AppInfo appInfo) {
        if (appInfo != null) {
            // Mettre à jour les TextViews avec les informations
            appNameTextView.setText(appInfo.getAppName());
            appVersionTextView.setText(appInfo.getVersionFormatted());
            appVersionDetailedTextView.setText(appInfo.getVersionDetailed());
            buildDateTextView.setText(appInfo.getBuildDate());

            // Informations du développeur
            developerNameTextView.setText(appInfo.getDeveloperName());
            developerEmailTextView.setText(appInfo.getDeveloperEmail());
            developerPhoneTextView.setText(appInfo.getDeveloperPhone());
            developerWebsiteTextView.setText(appInfo.getDeveloperWebsite());
        }
    }

    private void handleActionEvent(AboutViewModel.ActionEvent event) {
        if (event == null) return;

        switch (event.getAction()) {
            case AboutViewModel.ActionEvent.ACTION_EMAIL:
                handleEmailAction(event.getData());
                break;
            case AboutViewModel.ActionEvent.ACTION_PHONE:
                handlePhoneAction(event.getData());
                break;
            case AboutViewModel.ActionEvent.ACTION_WEBSITE:
                handleWebsiteAction(event.getData());
                break;
        }
    }

    private void handleEmailAction(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.email_subject, getString(R.string.app_name)));

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException e) {
            showToast(getString(R.string.no_email_app));
        }
    }

    private void handlePhoneAction(String phone) {
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + phone));

        try {
            startActivity(phoneIntent);
        } catch (android.content.ActivityNotFoundException e) {
            showToast(getString(R.string.no_phone_app));
        }
    }

    private void handleWebsiteAction(String website) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(website));

        try {
            startActivity(webIntent);
        } catch (android.content.ActivityNotFoundException e) {
            showToast(getString(R.string.no_browser_app));
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Méthodes publiques pour obtenir les informations (utilisables depuis l'Activity)
    public String getAppVersion() {
        AboutViewModel.AppInfo appInfo = viewModel.appInfo.getValue();
        return appInfo != null ? appInfo.getVersionName() : "Unknown";
    }

    public String getDeveloperEmail() {
        return viewModel.getDeveloperEmail();
    }

    public String getDeveloperName() {
        return viewModel.getDeveloperName();
    }
}