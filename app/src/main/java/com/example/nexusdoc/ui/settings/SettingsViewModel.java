package com.example.nexusdoc.ui.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class SettingsViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "AppSettings";
    private static final String PREF_THEME = "theme";
    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_NOTIFICATIONS = "notifications";

    private SharedPreferences sharedPreferences;

    // LiveData for UI state
    private MutableLiveData<Boolean> _isDarkTheme = new MutableLiveData<>();
    private MutableLiveData<Boolean> _notificationsEnabled = new MutableLiveData<>();
    private MutableLiveData<String> _currentLanguage = new MutableLiveData<>();
    private MutableLiveData<Boolean> _shouldRecreateActivity = new MutableLiveData<>();

    // Public LiveData getters
    public LiveData<Boolean> isDarkTheme() { return _isDarkTheme; }
    public LiveData<Boolean> notificationsEnabled() { return _notificationsEnabled; }
    public LiveData<String> currentLanguage() { return _currentLanguage; }
    public LiveData<Boolean> shouldRecreateActivity() { return _shouldRecreateActivity; }

    public SettingsViewModel(Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadSettings();
    }

    private void loadSettings() {
        // Load theme setting
        boolean isDarkTheme = sharedPreferences.getBoolean(PREF_THEME, false);
        _isDarkTheme.setValue(isDarkTheme);

        // Load notifications setting
        boolean notificationsEnabled = sharedPreferences.getBoolean(PREF_NOTIFICATIONS, true);
        _notificationsEnabled.setValue(notificationsEnabled);

        // Load language setting
        String language = sharedPreferences.getString(PREF_LANGUAGE, "fr");
        _currentLanguage.setValue(language);
    }

    public void toggleTheme() {
        Boolean currentTheme = _isDarkTheme.getValue();
        boolean newTheme = currentTheme == null ? true : !currentTheme;

        // Save to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_THEME, newTheme);
        editor.apply();

        // Update LiveData
        _isDarkTheme.setValue(newTheme);

        // Apply theme
        applyTheme(newTheme);
    }

    public void toggleNotifications() {
        Boolean currentNotifications = _notificationsEnabled.getValue();
        boolean newNotifications = currentNotifications == null ? false : !currentNotifications;

        // Save to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_NOTIFICATIONS, newNotifications);
        editor.apply();

        // Update LiveData
        _notificationsEnabled.setValue(newNotifications);
    }

    public void setLanguage(String languageCode) {
        // Save to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_LANGUAGE, languageCode);
        editor.apply();

        // Update LiveData
        _currentLanguage.setValue(languageCode);

        // Update app language
        updateAppLanguage(languageCode);
    }

    private void updateAppLanguage(String languageCode) {
        // Update locale configuration
        Resources resources = getApplication().getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Signal that activity should be recreated
        _shouldRecreateActivity.setValue(true);
    }

    private void applyTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void onActivityRecreated() {
        _shouldRecreateActivity.setValue(false);
    }

    // Getters for current values (for initial setup)
    public boolean getCurrentTheme() {
        Boolean value = _isDarkTheme.getValue();
        return value != null ? value : false;
    }

    public boolean getCurrentNotifications() {
        Boolean value = _notificationsEnabled.getValue();
        return value != null ? value : true;
    }

    public String getCurrentLanguageCode() {
        String value = _currentLanguage.getValue();
        return value != null ? value : "fr";
    }
}