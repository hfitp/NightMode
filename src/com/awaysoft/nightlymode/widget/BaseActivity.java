package com.awaysoft.nightlymode.widget;

import android.app.Activity;

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
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
