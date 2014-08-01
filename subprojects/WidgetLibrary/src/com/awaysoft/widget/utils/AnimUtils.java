
package com.awaysoft.widget.utils;

import com.awaysoft.widget.library.R;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimUtils {

    public static void alphaAnim(final View view, boolean in) {
        alphaAnim(view, in, null, in ? View.VISIBLE : View.INVISIBLE);
    }

    public static void alphaAnim(final View view, boolean in, final int endVisibility) {
        alphaAnim(view, in, null, endVisibility);
    }

    public static void alphaAnim(final View view, boolean in, final AnimListener listener, final int endVisibility) {
        if (view != null) {
            final Animation alpha = AnimationUtils.loadAnimation(view.getContext(), in ? R.anim.anim_alpha_in : R.anim.anim_alpha_out);
            if (listener != null) {
                alpha.setAnimationListener(listener);
            } else {
                alpha.setAnimationListener(new AnimListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(endVisibility);
                    }
                });
            }

            view.setVisibility(View.VISIBLE);
            view.startAnimation(alpha);
        }
    }
}
