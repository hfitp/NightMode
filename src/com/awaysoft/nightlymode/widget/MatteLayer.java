
package com.awaysoft.nightlymode.widget;

import com.awaysoft.nightlymode.utils.AnimHelper;
import com.awaysoft.nightlymode.utils.AnimListener;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.util.Locale;

public class MatteLayer extends FrameLayout {

    private int mMatterColor = Constant.DEFAULT_COLOR;

    private View mMatteView;
    private WindowManager mAttachedWindow;

    public MatteLayer(Context context) {
        this(context, null);
    }

    public MatteLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatteLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        mMatteView = new View(getContext());
        addView(mMatteView);
        setMatteAlpha(Preference.sMatteAlpha);
        setMatterColor(Preference.sMatteColor);
    }

    private WindowManager.LayoutParams generateWindowLayoutParams() {
        return new WindowManager.LayoutParams(WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSPARENT
        );
    }

    /**
     * Set matte's alpha value, rang from {0.0~1.0}
     *
     * @param alpha value{ 0.0 ~ 1.0}
     */
    public void setMatteAlpha(float alpha) {
        int r = Color.red(mMatterColor);
        int g = Color.green(mMatterColor);
        int b = Color.blue(mMatterColor);
        mMatterColor = Color.argb(Math.round(Math.min(alpha * 255F, 255F)), r, g, b);
        mMatteView.setBackgroundColor(mMatterColor);
    }

    public void setMatterColor(int color) {
        int a = Color.alpha(mMatterColor);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        mMatterColor = Color.argb(a, r, g, b);
        mMatteView.setBackgroundColor(mMatterColor);
    }

    public void setAttachedWindow(WindowManager window) {
        mAttachedWindow = window;
    }

    public void attachToWindows(WindowManager window) {
        if (getParent() == null) {
            mAttachedWindow = window;
            WindowManager.LayoutParams lParams = generateWindowLayoutParams();

            try {
                mAttachedWindow.addView(this, lParams);
            } catch (Exception e) {
                //ignore
            }
        }
    }

    public void detachFromWindow() {
        if (mAttachedWindow != null && getParent() != null) {
            mAttachedWindow.removeView(this);
        }
    }

    public void matteSmoothIn() {
        if (getParent() == null) {
            attachToWindows(mAttachedWindow);
            AnimHelper.INSTANCE.matteSmoothIn(mMatteView, null);
        }
        Log.d("NightMode", "matte in");
    }

    public void matteSmoothOut() {
        if (getParent() != null) {
            AnimHelper.INSTANCE.matteSmoothOut(mMatteView, new AnimListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    detachFromWindow();
                }
            });
        }
        Log.d("NightMode", "matte out");
    }
}
