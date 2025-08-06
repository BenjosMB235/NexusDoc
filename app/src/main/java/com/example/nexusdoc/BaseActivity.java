package com.example.nexusdoc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppSettings";
    private static final String PREF_LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateLanguage();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String language = prefs.getString(PREF_LANGUAGE, "fr");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

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

    private void updateLanguage() {
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
        } else {
            configuration.locale = locale;
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Informer l'Application du changement
        if (getApplication() instanceof MyApplication) {
            ((MyApplication) getApplication()).setAppLanguage(languageCode);
        }
    }

    public void changeLanguageAndRecreate(String languageCode) {
        // Sauvegarder la nouvelle langue
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(PREF_LANGUAGE, languageCode);
        editor.apply();

        // Appliquer immédiatement
        setAppLanguage(languageCode);

        // Recréer l'activité
        recreate();
    }
}