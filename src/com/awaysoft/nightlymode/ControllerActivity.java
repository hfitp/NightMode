package com.awaysoft.nightlymode;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.awaysoft.nightlymode.adapter.PreferenceConfig;
import com.awaysoft.nightlymode.services.NightlyService;
import com.awaysoft.nightlymode.utils.AnimListener;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.nightlymode.widget.BaseActivity;
import com.awaysoft.widget.component.MetroSeekBar;

/**
 * ControllerActivity.
 *
 * @author kang
 * @since 14/8/20.
 */
public class ControllerActivity extends BaseActivity implements View.OnClickListener, ViewSwitcher.ViewFactory {
    private View mControllerView;
    private ImageSwitcher mNightStatImg;
    private MetroSeekBar mBrightnessSeekBar;

    private static final int[] MODE_ICONs = {
            R.drawable.night_auto_drawable,
            R.drawable.night_moon_drawable,
            R.drawable.night_sub_drawable
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.nightly_controller);

        mNightStatImg = (ImageSwitcher) findViewById(R.id.night_controller_mode);

        AlphaAnimation in = new AlphaAnimation(0f, 1f);
        in.setDuration(300);
        mNightStatImg.setInAnimation(in);

        AlphaAnimation out = new AlphaAnimation(1f, 0f);
        out.setDuration(300);
        mNightStatImg.setOutAnimation(out);
        mNightStatImg.setFactory(this);
        mNightStatImg.setImageResource(MODE_ICONs[Preference.sNightlyMode >> Constant.MODE_MASK]);

        MetroSeekBar alphaSeekBar = (MetroSeekBar) findViewById(R.id.night_controller_alpha);
        alphaSeekBar.setRealValue(Preference.sMatteAlpha * 100F);
        alphaSeekBar.setProgressChangedListener(new MetroSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float value) {
                Preference.sMatteAlpha = value / 100f;
                PreferenceConfig.INSTANCE.onPreferenceChanged(ControllerActivity.this, Constant.TAG_ID_ALPHA);
            }

            @Override
            public void onTouchEnd(float value) {
                sendBroadcast(new Intent(Constant.BDC_PREFERENCE_CHANGED_FOR_ACT));
                Preference.INSTANCE.saveKey(ControllerActivity.this, Constant.KEY_MATTE_LAYER_ALPHA, Preference.sMatteAlpha);
            }
        });

        mBrightnessSeekBar = (MetroSeekBar) findViewById(R.id.night_controller_brightness);
        findViewById(R.id.night_controller_settings).setOnClickListener(this);
        findViewById(R.id.night_controller_mode).setOnClickListener(this);
        findViewById(R.id.night_controller_quit).setOnClickListener(this);

        mControllerView = findViewById(R.id.night_controller_main);
        mControllerView.setClickable(true);

        ViewParent vp = mControllerView.getParent();
        if (vp instanceof View) {
            ((View) vp).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    overridePendingTransition(0, 0);
                    exit();
                }
            });
        }

        mControllerView.post(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(180);
                mControllerView.setVisibility(View.VISIBLE);
                mControllerView.startAnimation(alphaAnimation);
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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.night_controller_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                exit();
                break;
            case R.id.night_controller_mode:
                Preference.sNightlyMode = switchStatus(Preference.sNightlyMode);
                mNightStatImg.setImageResource(MODE_ICONs[Preference.sNightlyMode >> Constant.MODE_MASK]);
                PreferenceConfig.INSTANCE.onPreferenceChanged(this, Constant.TAG_ID_MODE);
                sendBroadcast(new Intent(Constant.BDC_PREFERENCE_CHANGED_FOR_ACT));
                break;
            case R.id.night_controller_quit:
                Preference.sServiceRunning = false;
                Preference.INSTANCE.saveKey(this, Constant.KEY_SERVICES_RUNNING, false);
                stopService(new Intent(this, NightlyService.class));
                sendBroadcast(new Intent(Constant.BDC_SERVICE_CLOSED));
                overridePendingTransition(0, 0);
                exit();
                break;
        }
    }

    private void exit() {
        PreferenceConfig.INSTANCE.onPreferenceChanged(ControllerActivity.this, Constant.TAG_ID_FLOAT_WIDGET);
        Animation anim = new AlphaAnimation(1f, 0f);
        anim.setDuration(180);
        anim.setAnimationListener(new AnimListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mControllerView.setVisibility(View.INVISIBLE);
                finish();
            }
        });
        mControllerView.startAnimation(anim);
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public View makeView() {
        return new ImageView(this);
    }
}
