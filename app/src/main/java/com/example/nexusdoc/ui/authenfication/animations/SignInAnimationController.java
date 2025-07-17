package com.example.nexusdoc.ui.authenfication.animations;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.example.nexusdoc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public class SignInAnimationController {

    private final View rootView;

    // UI Components for animation
    private final CardView logoContainer;
    private final ImageView logoImage;
    private final MaterialTextView appTitle;
    private final MaterialTextView connectionSubtitle;
    private final LinearLayout loginForm;
    private final MaterialButton loginButton;
    private final MaterialTextView createAccount;
    private final MaterialTextView forgotPassword;
    private final View backgroundShapeTop;
    private final View backgroundShapeBottom;

    // Input layouts
    private final TextInputLayout emailLayout;
    private final TextInputLayout passwordLayout;

    public SignInAnimationController(View rootView) {
        this.rootView = rootView;

        // Initialize views
        logoContainer = rootView.findViewById(R.id.logo_container);
        logoImage = rootView.findViewById(R.id.logo_image);
        appTitle = rootView.findViewById(R.id.app_title);
        connectionSubtitle = rootView.findViewById(R.id.connection_subtitle);
        loginForm = rootView.findViewById(R.id.login_form);
        loginButton = rootView.findViewById(R.id.login_button);
        createAccount = rootView.findViewById(R.id.create_account);
        forgotPassword = rootView.findViewById(R.id.forgot_password);
        backgroundShapeTop = rootView.findViewById(R.id.background_shape_top);
        backgroundShapeBottom = rootView.findViewById(R.id.background_shape_bottom);
        emailLayout = rootView.findViewById(R.id.email_layout);
        passwordLayout = rootView.findViewById(R.id.password_layout);

        setupInitialStates();
    }

    private void setupInitialStates() {
        // Logo
        logoContainer.setAlpha(0f);
        logoContainer.setScaleX(0.8f);
        logoContainer.setScaleY(0.8f);

        // Titles
        appTitle.setAlpha(0f);
        appTitle.setTranslationY(-30f);
        connectionSubtitle.setAlpha(0f);
        connectionSubtitle.setTranslationY(-20f);

        // Form
        loginForm.setAlpha(1f);
        loginForm.setTranslationY(50f);

        // Buttons
        loginButton.setAlpha(0f);
        loginButton.setTranslationY(20f);
        createAccount.setAlpha(0f);
        createAccount.setTranslationY(20f);
        forgotPassword.setAlpha(0f);
        forgotPassword.setTranslationY(20f);

        // Input fields
        emailLayout.setAlpha(1f);
        passwordLayout.setAlpha(1f);
    }

    public void startEntryAnimations() {
        setupBackgroundAnimations();
        setupFormAnimations();
    }

    private void setupBackgroundAnimations() {
        // Top shape animation
        ObjectAnimator topRotate = ObjectAnimator.ofFloat(backgroundShapeTop, "rotation", 0f, 360f);
        topRotate.setDuration(25000);
        topRotate.setInterpolator(new LinearInterpolator());
        topRotate.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator topMove = ObjectAnimator.ofFloat(backgroundShapeTop, "translationY",
                -30f, 30f, -30f);
        topMove.setDuration(12000);
        topMove.setInterpolator(new AccelerateDecelerateInterpolator());
        topMove.setRepeatCount(ObjectAnimator.INFINITE);

        // Bottom shape animation
        ObjectAnimator bottomRotate = ObjectAnimator.ofFloat(backgroundShapeBottom, "rotation", 360f, 0f);
        bottomRotate.setDuration(30000);
        bottomRotate.setInterpolator(new LinearInterpolator());
        bottomRotate.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator bottomMove = ObjectAnimator.ofFloat(backgroundShapeBottom, "translationX",
                40f, -40f, 40f);
        bottomMove.setDuration(15000);
        bottomMove.setInterpolator(new AccelerateDecelerateInterpolator());
        bottomMove.setRepeatCount(ObjectAnimator.INFINITE);
        bottomMove.setStartDelay(3000);

        // Start background animations
        AnimatorSet topSet = new AnimatorSet();
        topSet.playTogether(topRotate, topMove);
        topSet.start();

        AnimatorSet bottomSet = new AnimatorSet();
        bottomSet.playTogether(bottomRotate, bottomMove);
        bottomSet.start();
    }

    private void setupFormAnimations() {
        // Logo animation with bounce
        logoContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();

        // Title animations
        appTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .start();

        connectionSubtitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(300)
                .start();

        // Form animation (slide up)
        loginForm.animate()
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(400)
                .start();

        // Button animations with delay
        new Handler().postDelayed(() -> {
            loginButton.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();

            createAccount.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(100)
                    .start();

            forgotPassword.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(200)
                    .start();
        }, 600);
    }

    public void animateButtonClick(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    public void animateTextClick(View text) {
        text.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(150)
                .withEndAction(() -> text.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }
}