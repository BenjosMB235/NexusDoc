package com.example.nexusdoc;


import static android.app.PendingIntent.getActivity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.nexusdoc.databinding.ActivityMainBinding;

import java.lang.reflect.Method;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Configuration du NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        navController = navHostFragment.getNavController();

        // Configuration pour le drawer
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.gedFragment,
                R.id.analyseFragment,
                R.id.notificationsFragment,
                R.id.teamFragment

        ).setOpenableLayout(binding.drawerLayout).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupWithNavController(binding.appBarMain.bottomNavigation, navController);

        // Gestion spécifique des items du drawer qui ne sont pas des fragments
        binding.navView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.nav_share) {
                shareApp();
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_evaluer) {
                rateApp();
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else {
                boolean handled = NavigationUI.onNavDestinationSelected(menuItem, navController);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void shareApp() {
        // Implémentation du partage
    }

    private void rateApp() {
        // Implémentation de l'évaluation
    }

    //Icone devant le menu de droite
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        try {
            Class<?> menuBuilderClass = Class.forName("androidx.appcompat.view.menu.MenuBuilder");
            Method setOptionalIconsVisible = menuBuilderClass.getMethod("setOptionalIconsVisible", boolean.class);
            setOptionalIconsVisible.invoke(menu, true);
        } catch (Exception e) {
            Log.e("MainActivity", "Error forcing menu icons to show", e);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId(); // Get the ID once

        if (itemId == R.id.action_profile) {
            if (navController.getCurrentDestination().getId() != R.id.profileFragment) {
                navController.navigate(R.id.profileFragment);
            }
            return true;
        } else if (itemId == R.id.action_logout) {
            // TODO: Ajoute ici ta logique de déconnexion
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
