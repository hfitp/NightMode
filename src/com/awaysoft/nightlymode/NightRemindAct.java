package com.awaysoft.nightlymode;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.awaysoft.nightlymode.services.NightlyService;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.nightlymode.utils.Utils;
import com.awaysoft.nightlymode.widget.BaseActivity;

/**
 * NightRemindAct.
 *
 * @author kang
 * @since 14/8/9.
 */
public class NightRemindAct extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boolean serviceIsRunning = Utils.INSTANCE.isServiceRunning(this, NightlyService.class.getName());
        int layoutId = !serviceIsRunning || Preference.sNightlyMode == Constant.MODE_NORMAL ?
                R.layout.nightly_alarm_night : R.layout.nightly_alarm_sunshine;

        final View rootView = View.inflate(this, layoutId, null);
        setContentView(rootView);
        findViewById(R.id.night_remind_sure).setOnClickListener(this);
        rootView.post(new Runnable() {
            @Override
            public void run() {
                rootView.startAnimation(AnimationUtils.loadAnimation(rootView.getContext(), R.anim.anim_alpha_in));
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.night_remind_sure:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_alpha_in, R.anim.anim_alpha_out);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "请点击按钮选择", Toast.LENGTH_SHORT).show();
    }
}
