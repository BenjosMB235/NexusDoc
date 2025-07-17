package com.example.nexusdoc.ui.authenfication.animations;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import com.example.nexusdoc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public class SignUpAnimationController {

    private final View rootView;

    // UI Components for animation
    private final CardView logoContainer;
    private final CardView profileImageContainer;
    private final MaterialTextView appTitle;
    private final MaterialTextView registrationSubtitle;
    private final MaterialTextView addPhotoText;
    private final android.widget.LinearLayout registerForm;
    private final MaterialButton registerButton;
    private final MaterialTextView loginLink;
    private final NestedScrollView scrollContainer;
    private final android.widget.ProgressBar scrollProgress;

    // Input layouts
    private final TextInputLayout usernameLayout;
    private final TextInputLayout emailLayout;
    private final TextInputLayout fonctionLayout;
    private final TextInputLayout passwordLayout;
    private final TextInputLayout confirmPasswordLayout;
    private final TextInputLayout phoneLayout;

    public SignUpAnimationController(View rootView) {
        this.rootView = rootView;

        // Initialize views
        logoContainer = rootView.findViewById(R.id.logo_container);
        profileImageContainer = rootView.findViewById(R.id.profile_image_container);
        appTitle = rootView.findViewById(R.id.app_title);
        registrationSubtitle = rootView.findViewById(R.id.registration_subtitle);
        addPhotoText = rootView.findViewById(R.id.add_photo_text);
        registerForm = rootView.findViewById(R.id.register_form);
        registerButton = rootView.findViewById(R.id.register_button);
        loginLink = rootView.findViewById(R.id.login_link);
        scrollContainer = rootView.findViewById(R.id.scroll_container);
        scrollProgress = rootView.findViewById(R.id.scroll_progress);

        usernameLayout = rootView.findViewById(R.id.username_layout);
        emailLayout = rootView.findViewById(R.id.email_layout);
        fonctionLayout = rootView.findViewById(R.id.fonction_layout);
        passwordLayout = rootView.findViewById(R.id.password_layout);
        confirmPasswordLayout = rootView.findViewById(R.id.confirm_password_layout);
        phoneLayout = rootView.findViewById(R.id.phone_layout);

        setupInitialStates();
        setupScrollListener();
    }

    private void setupInitialStates() {
        // Logo
        logoContainer.setAlpha(0f);
        logoContainer.setTranslationY(-100f);

        // Profile image
        profileImageContainer.setAlpha(0f);
        profileImageContainer.setScaleX(0.5f);
        profileImageContainer.setScaleY(0.5f);

        // Texts
        appTitle.setAlpha(0f);
        appTitle.setTranslationY(-50f);
        registrationSubtitle.setAlpha(0f);
        registrationSubtitle.setTranslationY(-30f);
        addPhotoText.setAlpha(0f);
        addPhotoText.setTranslationY(20f);

        // Form
        registerForm.setAlpha(0f);
        registerForm.setTranslationY(100f);
        registerButton.setAlpha(0f);
        registerButton.setTranslationY(50f);
        loginLink.setAlpha(0f);
        loginLink.setTranslationY(30f);

        // Input fields
        TextInputLayout[] layouts = {usernameLayout, emailLayout, fonctionLayout, passwordLayout, confirmPasswordLayout, phoneLayout};
        for (TextInputLayout layout : layouts) {
            layout.setAlpha(0f);
            layout.setTranslationX(-60f);
        }
    }

    public void startEntryAnimations() {
        new Handler().postDelayed(() -> {
            // Create and start all animations
            createLogoAnimation().start();
            createProfileAnimation().start();
            createTitleAnimation().start();
            createSubtitleAnimation().start();
            createAddPhotoAnimation().start();
            createFormAnimation().start();
            createFieldsAnimation().start();
            createButtonAnimation().start();
            createLinkAnimation().start();
        }, 300);
    }

    private AnimatorSet createLogoAnimation() {
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(
                ObjectAnimator.ofFloat(logoContainer, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(logoContainer, "translationY", -100f, 0f)
        );
        logoAnim.setDuration(800);
        logoAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        return logoAnim;
    }

    private AnimatorSet createProfileAnimation() {
        AnimatorSet profileAnim = new AnimatorSet();
        profileAnim.playTogether(
                ObjectAnimator.ofFloat(profileImageContainer, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(profileImageContainer, "scaleX", 0.5f, 1.1f, 1f),
                ObjectAnimator.ofFloat(profileImageContainer, "scaleY", 0.5f, 1.1f, 1f)
        );
        profileAnim.setDuration(1000);
        profileAnim.setStartDelay(300);
        return profileAnim;
    }

    private AnimatorSet createTitleAnimation() {
        AnimatorSet titleAnim = new AnimatorSet();
        titleAnim.playTogether(
                ObjectAnimator.ofFloat(appTitle, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(appTitle, "translationY", -50f, 0f)
        );
        titleAnim.setDuration(600);
        titleAnim.setStartDelay(400);
        return titleAnim;
    }

    private AnimatorSet createSubtitleAnimation() {
        AnimatorSet subtitleAnim = new AnimatorSet();
        subtitleAnim.playTogether(
                ObjectAnimator.ofFloat(registrationSubtitle, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(registrationSubtitle, "translationY", -30f, 0f)
        );
        subtitleAnim.setDuration(600);
        subtitleAnim.setStartDelay(600);
        return subtitleAnim;
    }

    private AnimatorSet createAddPhotoAnimation() {
        AnimatorSet addPhotoAnim = new AnimatorSet();
        addPhotoAnim.playTogether(
                ObjectAnimator.ofFloat(addPhotoText, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(addPhotoText, "translationY", 20f, 0f)
        );
        addPhotoAnim.setDuration(500);
        addPhotoAnim.setStartDelay(800);
        return addPhotoAnim;
    }

    private AnimatorSet createFieldsAnimation() {
        AnimatorSet fieldAnim = new AnimatorSet();
        fieldAnim.playTogether(
                ObjectAnimator.ofFloat(usernameLayout, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(usernameLayout, "translationX", -60f, 0f),
                ObjectAnimator.ofFloat(emailLayout, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(emailLayout, "translationX", -60f, 0f),
                ObjectAnimator.ofFloat(fonctionLayout, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(fonctionLayout, "translationX", -60f, 0f),
                ObjectAnimator.ofFloat(passwordLayout, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(passwordLayout, "translationX", -60f, 0f),
                ObjectAnimator.ofFloat(confirmPasswordLayout, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(confirmPasswordLayout, "translationX", -60f, 0f),
                ObjectAnimator.ofFloat(phoneLayout, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(phoneLayout, "translationX", -60f, 0f)
        );
        fieldAnim.setDuration(600);
        fieldAnim.setStartDelay(950);
        return fieldAnim;
    }

    private AnimatorSet createFormAnimation() {
        AnimatorSet formAnim = new AnimatorSet();
        formAnim.playTogether(
                ObjectAnimator.ofFloat(registerForm, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(registerForm, "translationY", 100f, 0f)
        );
        formAnim.setDuration(700);
        formAnim.setStartDelay(900);
        return formAnim;
    }

    private AnimatorSet createButtonAnimation() {
        AnimatorSet buttonAnim = new AnimatorSet();
        buttonAnim.playTogether(
                ObjectAnimator.ofFloat(registerButton, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(registerButton, "translationY", 50f, 0f)
        );
        buttonAnim.setDuration(600);
        buttonAnim.setStartDelay(1150);
        return buttonAnim;
    }

    private AnimatorSet createLinkAnimation() {
        AnimatorSet linkAnim = new AnimatorSet();
        linkAnim.playTogether(
                ObjectAnimator.ofFloat(loginLink, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(loginLink, "translationY", 30f, 0f)
        );
        linkAnim.setDuration(600);
        linkAnim.setStartDelay(1300);
        return linkAnim;
    }

    public void animateButtonClick(View button) {
        AnimatorSet clickAnim = new AnimatorSet();
        clickAnim.playTogether(
                ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f),
                ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f, 1f)
        );
        clickAnim.setDuration(200);
        clickAnim.start();
    }

    public void animateTextClick(View text) {
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(text, "scaleX", 1f, 1.1f, 1f),
                ObjectAnimator.ofFloat(text, "scaleY", 1f, 1.1f, 1f)
        );
        animSet.setDuration(200);
        animSet.start();
    }

    public void animateImageClick(View imageContainer) {
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(imageContainer, "scaleX", 1f, 0.95f, 1f),
                ObjectAnimator.ofFloat(imageContainer, "scaleY", 1f, 0.95f, 1f)
        );
        animSet.setDuration(150);
        animSet.start();
    }

    public void animateImageSelection() {
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(profileImageContainer, "scaleX", 1f, 1.1f, 1f),
                ObjectAnimator.ofFloat(profileImageContainer, "scaleY", 1f, 1.1f, 1f),
                ObjectAnimator.ofFloat(profileImageContainer, "rotation", 0f, 5f, -5f, 0f)
        );
        animSet.setDuration(600);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();
    }

    private void setupScrollListener() {
        scrollContainer.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                View child = scrollContainer.getChildAt(0);
                if (child != null) {
                    int scrollHeight = child.getHeight() - scrollContainer.getHeight();
                    int progress = (int) ((scrollY * 100.0f) / scrollHeight);
                    scrollProgress.setProgress(Math.max(0, Math.min(progress, 100)));
                }
            }
        });

    }
}