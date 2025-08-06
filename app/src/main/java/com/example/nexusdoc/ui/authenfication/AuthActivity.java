package com.example.nexusdoc.ui.authenfication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nexusdoc.BaseActivity;
import com.example.nexusdoc.R;
import com.google.android.material.card.MaterialCardView;

public class AuthActivity extends BaseActivity {

    private MaterialCardView logoCard;
    private ImageView logoImage;
    private View circleTop;
    private View circleBottom;
    private TextView appName;
    private TextView tagline;
    private LinearLayout progressContainer;
    private View navHostView;

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialisation des vues
        logoCard = findViewById(R.id.logo_card);
        logoImage = findViewById(R.id.logo_image);
        circleTop = findViewById(R.id.circle_top);
        circleBottom = findViewById(R.id.circle_bottom);
        appName = findViewById(R.id.app_name);
        tagline = findViewById(R.id.tagline);
        progressContainer = findViewById(R.id.progress_container);
        navHostView = findViewById(R.id.nav_host_fragment);
        navHostView.setVisibility(View.INVISIBLE);

        // Initialisation du NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Lancer les animations
        startIntroAnimations();
    }

    private void startIntroAnimations() {
        // Animation d'apparition du logo
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoCard, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoCard, "scaleY", 0f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoCard, "alpha", 0f, 1f);

        AnimatorSet logoAnimation = new AnimatorSet();
        logoAnimation.playTogether(scaleX, scaleY, alpha);
        logoAnimation.setDuration(800);
        logoAnimation.setInterpolator(new DecelerateInterpolator());

        // Animation du nom de l'app
        ObjectAnimator textFadeIn = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f);
        ObjectAnimator textSlideUp = ObjectAnimator.ofFloat(appName, "translationY", 50f, 0f);

        AnimatorSet textAnimation = new AnimatorSet();
        textAnimation.playTogether(textFadeIn, textSlideUp);
        textAnimation.setDuration(600);
        textAnimation.setStartDelay(400);

        // Tagline
        ObjectAnimator taglineFadeIn = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f);
        taglineFadeIn.setStartDelay(800);
        taglineFadeIn.setDuration(400);

        // Cercles en rotation
        ObjectAnimator circleRotation = ObjectAnimator.ofFloat(circleTop, "rotation", 0f, 360f);
        circleRotation.setDuration(20000);
        circleRotation.setRepeatCount(ValueAnimator.INFINITE);
        circleRotation.setInterpolator(new LinearInterpolator());

        // Pulse logo
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(logoImage, "scaleX", 1f, 1.05f, 1f);
        pulseX.setRepeatCount(ValueAnimator.INFINITE);
        pulseX.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator pulseY = ObjectAnimator.ofFloat(logoImage, "scaleY", 1f, 1.05f, 1f);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet pulseAnimation = new AnimatorSet();
        pulseAnimation.playTogether(pulseX, pulseY);
        pulseAnimation.setDuration(2000);
        pulseAnimation.setStartDelay(1000);

        // Progress
        ObjectAnimator progressFadeIn = ObjectAnimator.ofFloat(progressContainer, "alpha", 0f, 1f);
        progressFadeIn.setStartDelay(1200);
        progressFadeIn.setDuration(600);

        // Démarrage
        logoAnimation.start();
        textAnimation.start();
        taglineFadeIn.start();
        circleRotation.start();
        pulseAnimation.start();
        progressFadeIn.start();

        // Transition après 3s
        new Handler().postDelayed(this::transitionToSignInFragment, 3000);
    }

    private void transitionToSignInFragment() {
        navHostView.setBackgroundColor(Color.WHITE);
        navHostView.setVisibility(View.VISIBLE);
        navHostView.setAlpha(0f);

        ObjectAnimator fadeInContainer = ObjectAnimator.ofFloat(navHostView, "alpha", 0f, 1f);
        fadeInContainer.setDuration(500);
        fadeInContainer.setInterpolator(new DecelerateInterpolator());

        // Fade out intro
        AnimatorSet exitAnimations = new AnimatorSet();
        exitAnimations.playTogether(
                ObjectAnimator.ofFloat(logoCard, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(appName, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(tagline, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(progressContainer, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(circleTop, "alpha", 0.1f, 0f),
                ObjectAnimator.ofFloat(circleBottom, "alpha", 0.08f, 0f)
        );
        exitAnimations.setDuration(500);
        exitAnimations.setInterpolator(new DecelerateInterpolator());

        // Lancer l'affichage du SignInFragment via navigation
        navController.navigate(R.id.signInFragment);

        // Lancer les animations
        fadeInContainer.start();
        exitAnimations.start();
    }

    public void showInitialScreen() {
        navHostView.setVisibility(View.INVISIBLE);

        logoCard.setAlpha(1f);
        appName.setAlpha(1f);
        tagline.setAlpha(1f);
        progressContainer.setAlpha(1f);
        circleTop.setAlpha(0.1f);
        circleBottom.setAlpha(0.08f);

        startIntroAnimations();
    }
}
