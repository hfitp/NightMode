
package com.awaysoft.nightlymode.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.awaysoft.nightlymode.ControllerActivity;
import com.awaysoft.nightlymode.R;
import com.awaysoft.nightlymode.utils.AnimListener;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.nightlymode.utils.Utils;

public class FloatTouch extends FrameLayout implements OnClickListener, OnLongClickListener {
    private static final int FLAG_NORMAL = 1;
    private static final int FLAG_MENU = 2;
    private static final int FLAG_MOVING = 4;

    private int mFlag;
    private Point mDisplay;
    private Point mPointer;
    private ImageView mFlagIcon;
    private WindowManager mAttachedWindow;
    private WindowManager.LayoutParams mWLParams;

    public FloatTouch(Context context) {
        this(context, null);
    }

    public FloatTouch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatTouch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
        setOnLongClickListener(this);

        initialize();
    }

    @TargetApi(11)
    private void initialize() {
        mFlag = FLAG_NORMAL;
        mDisplay = new Point();
        mPointer = new Point();

        LayoutParams fLParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fLParams.gravity = Gravity.CENTER;
        mFlagIcon = new ImageView(getContext());
        addView(mFlagIcon, fLParams);
    }

    public void setAttachedWindow(WindowManager window) {
        mAttachedWindow = window;
    }

    public void attachToWindow(WindowManager window) {
        if (!isShown()) {
            int size = getResources().getDimensionPixelSize(R.dimen.nightly_float_size);
            mAttachedWindow = window;
            int[] tmp = Utils.INSTANCE.getScreenSize(window);
            mDisplay.set(tmp[0], tmp[1]);
            mWLParams = generateWindowLayoutParams();
            mWLParams.width = size;
            mWLParams.height = size;
            mWLParams.gravity = Gravity.TOP | Gravity.LEFT;

            if (TextUtils.isEmpty(Preference.sFloatLocation)) {
                mWLParams.x = mDisplay.x - size;
                mWLParams.y = 4 * size;
            } else {
                String[] loc = Preference.sFloatLocation.split("\\|");
                mWLParams.x = Integer.valueOf(loc[0]);
                mWLParams.y = Integer.valueOf(loc[1]);
            }

            mFlagIcon.setImageResource(R.drawable.night_touch_drawable);

            try {
                mAttachedWindow.addView(this, mWLParams);
            } catch (Exception e) {
                //ignore
            }

            setVisibility(INVISIBLE);
            post(new Runnable() {
                @Override
                public void run() {
                    setVisibility(VISIBLE);
                    AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
                    alpha.setDuration(180);
                    alpha.setInterpolator(new DecelerateInterpolator(2));
                    mFlagIcon.startAnimation(alpha);
                }
            });
        }
    }

    public void updateLocation(int dx, int dy) {
        if (mAttachedWindow != null) {
            mWLParams.x += dx;
            mWLParams.y += dy;
            mAttachedWindow.updateViewLayout(this, mWLParams);
        }
    }

    public void detachFromWindow() {
        if (isShown()) {
            if (mAttachedWindow != null) {
                mAttachedWindow.removeView(this);
            }

            Preference.sFloatLocation = mWLParams.x + "|" + mWLParams.y;
            mAttachedWindow = null;
        }
    }

    private WindowManager.LayoutParams generateWindowLayoutParams() {
        return new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = Math.round(event.getRawX());
        int y = Math.round(event.getRawY());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPointer.set(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if ((mFlag ^ FLAG_NORMAL) != 0) {
                    mFlag = FLAG_MOVING;
                    updateLocation(x - mPointer.x, y - mPointer.y);
                    mPointer.set(x, y);
                }

                break;
            case MotionEvent.ACTION_UP:
                if ((mFlag ^ FLAG_MOVING) == 0) {
                    mFlag >>= Constant.MODE_MASK << Constant.MODE_MASK;
                }

                mPointer.set(mWLParams.x, mWLParams.y);
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onLongClick(View v) {
        mFlag <<= Constant.MODE_MASK;
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getContext(), ControllerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);

        AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
        alpha.setDuration(180);
        alpha.setAnimationListener(new AnimListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                detachFromWindow();
            }
        });
        mFlagIcon.startAnimation(alpha);
    }
}
