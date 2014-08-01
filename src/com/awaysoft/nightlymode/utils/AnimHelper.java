package com.awaysoft.nightlymode.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * For animation helper
 *
 * @author kang
 */
public class AnimHelper {

    public static void matteSmoothIn(final View trackMatteView, AnimListener listener) {
        if (trackMatteView == null || trackMatteView.getParent() == null) {
            return;
        } else {
            trackMatteView.clearAnimation();
        }

        final Animation anim = new AlphaAnimation(0F, 1F);
        anim.setDuration(Constant.ANIMATION_DURATION);
        anim.setInterpolator(new DecelerateInterpolator(2));

        if (listener != null) {
            anim.setAnimationListener(listener);
        }

        trackMatteView.post(new Runnable() {

            @Override
            public void run() {
                trackMatteView.startAnimation(anim);
            }
        });
    }

    public static void matteSmoothOut(final View trackMatteView, AnimListener listener) {
        if (trackMatteView == null || trackMatteView.getParent() == null) {
            return;
        } else {
            trackMatteView.clearAnimation();
        }

        final Animation anim = new AlphaAnimation(1F, 0F);
        anim.setDuration(Constant.ANIMATION_DURATION);
        anim.setInterpolator(new DecelerateInterpolator(2));

        if (listener != null) {
            anim.setAnimationListener(listener);
        }

        trackMatteView.post(new Runnable() {

            @Override
            public void run() {
                trackMatteView.startAnimation(anim);
            }
        });
    }

    public static void statusFlagSmoothIn(final View view, AnimListener listener) {
        if (view == null || view.getParent() == null) {
            return;
        }

        AnimationSet anim = new AnimationSet(true);
        anim.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0));
        anim.addAnimation(new AlphaAnimation(0F, 1F));
        anim.setDuration(Constant.ANIMATION_DURATION);
        anim.setInterpolator(new DecelerateInterpolator(2));
        if (listener != null) {
            anim.setAnimationListener(listener);
        }

        view.startAnimation(anim);
    }

    public static void statusFlagSmoothOut(final View view, AnimListener listener) {
        if (view == null || view.getParent() == null) {
            return;
        }

        AnimationSet anim = new AnimationSet(true);
        anim.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1));
        anim.addAnimation(new AlphaAnimation(1F, 0F));
        anim.setDuration(Constant.ANIMATION_DURATION);
        anim.setInterpolator(new DecelerateInterpolator(2));
        if (listener != null) {
            anim.setAnimationListener(listener);
        }

        view.startAnimation(anim);
    }
}
