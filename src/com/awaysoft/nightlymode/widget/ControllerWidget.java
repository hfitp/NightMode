
package com.awaysoft.nightlymode.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.awaysoft.nightlymode.R;
import com.awaysoft.nightlymode.utils.AnimHelper;
import com.awaysoft.nightlymode.utils.AnimListener;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.nightlymode.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class ControllerWidget extends FrameLayout implements OnClickListener, OnLongClickListener {
    private static final int FLAG_NORMAL = 1;
    private static final int FLAG_MENU = 2;
    private static final int FLAG_MOVING = 4;

    private int mFlag;
    private int mCacheMode;
    private Point mDisplay;
    private Point mPointer;
    private Handler mHandler;
    private ImageView mFlagIcon;
    private ImageView mFlagIconCache;
    private WindowManager mAttachedWindow;
    private WindowManager.LayoutParams mWLParams;

    private static final int[] sIcon = new int[]{
            R.drawable.flag_auto,
            R.drawable.flag_night,
            R.drawable.flag_normal
    };

    public ControllerWidget(Context context) {
        this(context, null);
    }

    public ControllerWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControllerWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundResource(R.drawable.controller_bg);
        setOnClickListener(this);
        setOnLongClickListener(this);

        initialize();
    }

    private void initialize() {
        mFlag = FLAG_NORMAL;
        mDisplay = new Point();
        mPointer = new Point();

        mFlagIconCache = new ImageView(getContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mFlagIconCache.setAlpha(0.5F);
        }

        LayoutParams fLParams = new LayoutParams(48, 48);
        fLParams.gravity = Gravity.CENTER;
        addView(mFlagIconCache, fLParams);
        mFlagIconCache.setVisibility(View.GONE);

        mFlagIcon = new ImageView(getContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mFlagIconCache.setAlpha(0.5F);
        }

        fLParams = new LayoutParams(48, 48);
        fLParams.gravity = Gravity.CENTER;
        addView(mFlagIcon, fLParams);
    }

    public void bindHandler(Handler handler) {
        mHandler = handler;
    }

    public void attachToWindow(WindowManager window) {
        if (!isShown()) {
            mAttachedWindow = window;
            int[] tmp = Utils.getScreenSize(window);
            mDisplay.set(tmp[0], tmp[1]);
            mWLParams = generateWindowLayoutParams();
            mWLParams.width = 96;
            mWLParams.height = 96;
            mWLParams.gravity = Gravity.TOP | Gravity.LEFT;

            if (TextUtils.isEmpty(Preference.sFloatLocation)) {
                mWLParams.x = mDisplay.x - 96;
                mWLParams.y = 4 * 96;
            } else {
                String[] loc = Preference.sFloatLocation.split("\\|");
                mWLParams.x = Integer.valueOf(loc[0]);
                mWLParams.y = Integer.valueOf(loc[1]);
            }

            mFlagIconCache.setImageResource(sIcon[switchStatus(Preference.sNightlyMode) >> Constant.MODE_MASK]);
            mFlagIcon.setImageResource(sIcon[Preference.sNightlyMode >> Constant.MODE_MASK]);

            mAttachedWindow.addView(this, mWLParams);
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

    public void onStatusChanged() {
        mFlagIcon.clearAnimation();
        AnimHelper.statusFlagSmoothIn(mFlagIcon, new AnimListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFlagIcon.setImageResource(sIcon[Preference.sNightlyMode >> Constant.MODE_MASK]);
            }
        });

        mFlagIconCache.clearAnimation();
        AnimHelper.statusFlagSmoothOut(mFlagIconCache, new AnimListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFlagIconCache.setVisibility(View.VISIBLE);
                mFlagIconCache.setImageResource(sIcon[mCacheMode >> Constant.MODE_MASK]);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFlagIconCache.setVisibility(GONE);
            }
        });
    }

    private int switchStatus(int src) {
        int tmp = src;

        if ((tmp ^ Constant.MODE_NORMAL) != 0) {
            tmp <<= Constant.MODE_MASK;
        } else {
            tmp >>= Constant.MODE_MASK << Constant.MODE_MASK;
        }

        return tmp;
    }

    @Override
    public boolean onLongClick(View v) {
        mFlag <<= Constant.MODE_MASK;
        return true;
    }

    @Override
    public void onClick(View v) {
        if (mFlag == FLAG_MENU) {
            mFlag >>= Constant.MODE_MASK;
        } else {
            mCacheMode = Preference.sNightlyMode;
            Preference.sNightlyMode = switchStatus(Preference.sNightlyMode);
            mHandler.sendEmptyMessage(Constant.MSG_STATUS_CHANGED);
            getContext().sendBroadcast(new Intent(Constant.BDC_SWITCH_MODE));
            MobclickAgent.onEvent(getContext(), "float_change_mode");
        }
    }
}
