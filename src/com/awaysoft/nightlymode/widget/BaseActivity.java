package com.awaysoft.nightlymode.widget;

import android.app.Activity;

import com.awaysoft.nightlymode.adapter.PreferenceConfig;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.umeng.analytics.MobclickAgent;

/**
 * BaseActivity.
 *
 * @author kang
 * @since 14/7/31.
 */
public class BaseActivity extends Activity {
    public void onResume() {
        super.onResume();
        Preference.sPreferenceRunning = true;
        PreferenceConfig.INSTANCE.onPreferenceChanged(this, Constant.TAG_ID_FLOAT_WIDGET);
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        Preference.sPreferenceRunning = false;
        PreferenceConfig.INSTANCE.onPreferenceChanged(this, Constant.TAG_ID_FLOAT_WIDGET);
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
