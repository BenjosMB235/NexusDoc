package com.example.nexusdoc.ui.utilitaires;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class AnimationUtils {

    public static void animateExpansion(View view, boolean expand) {
        if (expand) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
            view.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            view.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    public static void animateChevronRotation(ImageView chevron, boolean expanded) {
        float rotation = expanded ? 180f : 0f;
        chevron.animate()
                .rotation(rotation)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void animateSlideDown(View view) {
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(-view.getHeight());
        view.animate()
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void animateSlideUp(View view) {
        view.animate()
                .translationY(-view.getHeight())
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    public static void animateScaleIn(View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}