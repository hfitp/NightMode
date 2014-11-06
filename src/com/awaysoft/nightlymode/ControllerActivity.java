package com.awaysoft.nightlymode;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.awaysoft.nightlymode.utils.Utils;
import com.awaysoft.nightlymode.widget.BaseActivity;
import com.awaysoft.widget.component.MetroSeekBar;

/**
 * ControllerActivity.
 *
 * @author kang
 * @since 14/8/20.
 */
public class ControllerActivity extends BaseActivity implements View.OnClickListener, ViewSwitcher.ViewFactory {
    private ImageSwitcher mNightStatImg;

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

        final AlphaAnimation in = new AlphaAnimation(0f, 1f);
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

        MetroSeekBar brightnessSeekBar = (MetroSeekBar) findViewById(R.id.night_controller_brightness);

        if (Utils.isAutoBrightness(getContentResolver())) {
            Utils.INSTANCE.stopAutoBrightness(this);
        }

        int brightness = Utils.INSTANCE.getGlobalScreenBrightness(this);
        brightnessSeekBar.setRealValue(brightness);
        brightnessSeekBar.setProgressChangedListener(new MetroSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float value) {
                previewBrightness(value);
            }

            @Override
            public void onTouchEnd(float value) {
                Utils.INSTANCE.saveBrightness(getContentResolver(), (int) value);
            }
        });

        findViewById(R.id.night_controller_settings).setOnClickListener(this);
        findViewById(R.id.night_controller_mode).setOnClickListener(this);
        findViewById(R.id.night_controller_quit).setOnClickListener(this);
        findViewById(R.id.night_controller_main).setClickable(true);
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
                finish();
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
                finish();
                break;
        }
    }

    private void previewBrightness(float brightness) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams wLParams = getWindow().getAttributes();
        wLParams.screenBrightness = brightness / 255F;
        wLParams.buttonBrightness = brightness / 255F;
        getWindow().setAttributes(wLParams);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceConfig.INSTANCE.onPreferenceChanged(ControllerActivity.this, Constant.TAG_ID_FLOAT_WIDGET);
        Preference.INSTANCE.saveKey(this, Constant.KEY_SERVICES_NIGHTLY_MODE, Preference.sNightlyMode);
        finish();
    }

    @Override
    public View makeView() {
        return new ImageView(this);
    }
}
