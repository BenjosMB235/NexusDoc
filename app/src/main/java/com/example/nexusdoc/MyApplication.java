package com.example.nexusdoc;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String PREFS_NAME = "AppSettings";
    private static final String PREF_LANGUAGE = "language";

    @Override
    public void onCreate() {
        super.onCreate();
        // Appliquer la langue sauvegardée au démarrage
        applySavedLanguage();
    }

    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String language = prefs.getString(PREF_LANGUAGE, "fr");
        setAppLanguage(language);
    }

    public void setAppLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            configuration.setLocales(new android.os.LocaleList(locale));
        } else {
            configuration.locale = locale;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            createConfigurationContext(configuration);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String language = prefs.getString(PREF_LANGUAGE, "fr");
        Locale locale = new Locale(language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Configuration configuration = new Configuration();
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}