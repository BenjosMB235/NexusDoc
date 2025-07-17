package com.example.nexusdoc.ui.about;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AboutViewModel extends AndroidViewModel {

    // Informations de l'application (à personnaliser)
    private static final String DEVELOPER_EMAIL = "mbaibenjsos@gmail.com";
    private static final String DEVELOPER_PHONE = "+235 60 59 65 58";
    private static final String DEVELOPER_WEBSITE = "https://www.example.com";
    private static final String DEVELOPER_NAME = "Mbaïram Benjamin Beassoum";

    // LiveData pour les informations de l'application
    private final MutableLiveData<AppInfo> _appInfo = new MutableLiveData<>();
    public final LiveData<AppInfo> appInfo = _appInfo;

    // LiveData pour les événements d'action
    private final MutableLiveData<ActionEvent> _actionEvent = new MutableLiveData<>();
    public final LiveData<ActionEvent> actionEvent = _actionEvent;

    public AboutViewModel(@NonNull Application application) {
        super(application);
        loadAppInformation();
    }

    private void loadAppInformation() {
        Context context = getApplication().getApplicationContext();

        try {
            // Obtenir les informations du package
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

            // Nom de l'application
            String appName = context.getString(context.getApplicationInfo().labelRes);

            // Version de l'application
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            // Date de compilation (approximative basée sur la date d'installation)
            long firstInstallTime = packageInfo.firstInstallTime;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            String buildDate = dateFormat.format(new Date(firstInstallTime));

            // Créer l'objet AppInfo
            AppInfo info = new AppInfo(
                    appName,
                    versionName,
                    versionCode,
                    buildDate,
                    DEVELOPER_NAME,
                    DEVELOPER_EMAIL,
                    DEVELOPER_PHONE,
                    DEVELOPER_WEBSITE
            );

            _appInfo.setValue(info);

        } catch (PackageManager.NameNotFoundException e) {
            // Valeurs par défaut en cas d'erreur
            AppInfo errorInfo = new AppInfo(
                    "Unknown App",
                    "Unknown",
                    0,
                    "Unknown",
                    DEVELOPER_NAME,
                    DEVELOPER_EMAIL,
                    DEVELOPER_PHONE,
                    DEVELOPER_WEBSITE
            );
            _appInfo.setValue(errorInfo);
        }
    }

    // Méthodes pour gérer les actions
    public void onEmailClicked() {
        _actionEvent.setValue(new ActionEvent(ActionEvent.ACTION_EMAIL, DEVELOPER_EMAIL));
    }

    public void onPhoneClicked() {
        _actionEvent.setValue(new ActionEvent(ActionEvent.ACTION_PHONE, DEVELOPER_PHONE));
    }

    public void onWebsiteClicked() {
        _actionEvent.setValue(new ActionEvent(ActionEvent.ACTION_WEBSITE, DEVELOPER_WEBSITE));
    }

    // Méthodes publiques pour obtenir les informations
    public String getDeveloperEmail() {
        return DEVELOPER_EMAIL;
    }

    public String getDeveloperName() {
        return DEVELOPER_NAME;
    }

    // Classe pour encapsuler les informations de l'application
    public static class AppInfo {
        private final String appName;
        private final String versionName;
        private final int versionCode;
        private final String buildDate;
        private final String developerName;
        private final String developerEmail;
        private final String developerPhone;
        private final String developerWebsite;

        public AppInfo(String appName, String versionName, int versionCode, String buildDate,
                       String developerName, String developerEmail, String developerPhone, String developerWebsite) {
            this.appName = appName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.buildDate = buildDate;
            this.developerName = developerName;
            this.developerEmail = developerEmail;
            this.developerPhone = developerPhone;
            this.developerWebsite = developerWebsite;
        }

        // Getters
        public String getAppName() { return appName; }
        public String getVersionName() { return versionName; }
        public int getVersionCode() { return versionCode; }
        public String getBuildDate() { return buildDate; }
        public String getDeveloperName() { return developerName; }
        public String getDeveloperEmail() { return developerEmail; }
        public String getDeveloperPhone() { return developerPhone; }
        public String getDeveloperWebsite() { return developerWebsite; }

        public String getVersionFormatted() {
            return "Version " + versionName;
        }

        public String getVersionDetailed() {
            return "Version " + versionName + " (" + versionCode + ")";
        }
    }

    // Classe pour les événements d'action
    public static class ActionEvent {
        public static final String ACTION_EMAIL = "email";
        public static final String ACTION_PHONE = "phone";
        public static final String ACTION_WEBSITE = "website";

        private final String action;
        private final String data;

        public ActionEvent(String action, String data) {
            this.action = action;
            this.data = data;
        }

        public String getAction() { return action; }
        public String getData() { return data; }
    }
}