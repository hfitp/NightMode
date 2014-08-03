/*
 * Copyright (C) 2014 Ruikye's open source project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.awaysoft.nightlymode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.awaysoft.nightlymode.adapter.PreferenceAdapter;
import com.awaysoft.nightlymode.adapter.PreferenceConfig;
import com.awaysoft.nightlymode.adapter.PreferenceItemHolder;
import com.awaysoft.nightlymode.services.NightlyService;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.nightlymode.utils.Utils;
import com.awaysoft.nightlymode.widget.BaseActivity;
import com.awaysoft.widget.Switch;
import com.awaysoft.widget.component.ColorPicker;
import com.awaysoft.widget.component.ColorPicker.ColorObj;
import com.awaysoft.widget.component.ColorPicker.OnColorChangeListener;
import com.awaysoft.widget.component.CustomDialog;
import com.awaysoft.widget.component.CustomDialog.OnOpsBtnClickListener;
import com.umeng.analytics.MobclickAgent;

/**
 * Preference Activity.
 *
 * @author ruikye
 * @since 2014
 */
public class PreferenceActivity extends BaseActivity implements OnItemClickListener {
    private PreferenceAdapter mPreferenceAdapter;

    private BroadcastReceiver mPreferenceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(Constant.BDC_SWITCH_MODE, action)) {
                mPreferenceAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nightly_mode_preference);
        mPreferenceAdapter = new PreferenceAdapter(this);

        // 读取配置
        Preference.read(this);
        PreferenceConfig.build(this);
        Preference.sActivityRunning = true;

        Switch switcher = (Switch) findViewById(R.id.nightly_switch);
        switcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked) {
                    if (!NightlyService.sIsRunning) {
                        startService(new Intent(PreferenceActivity.this, NightlyService.class));
                        MobclickAgent.onEvent(PreferenceActivity.this, "start_service");
                    }
                } else {
                    MobclickAgent.onEvent(PreferenceActivity.this, "stop_service");
                    stopService(new Intent(PreferenceActivity.this, NightlyService.class));
                }
            }
        });
        switcher.setChecked(Preference.sServiceRunning);

        ListView listView = (ListView) findViewById(R.id.nighlty_listview);
        listView.setAdapter(mPreferenceAdapter);
        listView.setOnItemClickListener(this);

        registerReceiver(mPreferenceChangedReceiver, new IntentFilter(Constant.BDC_SWITCH_MODE));
    }

    @Override
    public void onPause() {
        super.onPause();
        Preference.save(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPreferenceChangedReceiver);
        Preference.sActivityRunning = false;
        Preference.save(this);
        PreferenceConfig.destroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PreferenceItemHolder itemHolder = (PreferenceItemHolder) mPreferenceAdapter.getItem(position);
        itemHolder.performClick(view);
        int targetId = itemHolder.getTargetId();
        switch (targetId) {
            case Constant.TAG_ID_MODE: {
                MobclickAgent.onEvent(this, "main_change_mode");
                configureNightMode();
                break;
            }
            case Constant.TAG_ID_ALPHA: {
                MobclickAgent.onEvent(this, "main_change_alpha");
                configureMatteAlpha();
                break;
            }
            /*case Constant.TAG_ID_COLOR: {
                configureMatteColor();
                break;
            }
            case Constant.TAG_ID_AUTO_TIME: {
                break;
            }*/
            case Constant.TAG_ID_WHITE_LIST: {
                MobclickAgent.onEvent(this, "main_set_white_list");
                startActivity(new Intent(this, AppSelectActivity.class));
                break;
            }
            /*case Constant.TAG_ID_FEEDBACK: {
                break;
            }
            case Constant.TAG_ID_ABOUT: {
                break;
            }*/
        }
    }

    /**
     * Popup dialog to set night mode
     */
    private void configureNightMode() {
        CustomDialog nightModeDialog = new CustomDialog(this);
        ListView listView = (ListView) LayoutInflater.from(this).inflate(R.layout.nightly_listview, null);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.nightly_radio_check,
                getResources().getStringArray(R.array.night_mode)));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(Preference.sNightlyMode >> Constant.MODE_MASK, true);
        listView.setPadding(0, 0, 0, 0);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Preference.sNightlyMode = (int) Math.pow(2, position);
                mPreferenceAdapter.notifyDataSetChanged();
                Preference.saveKey(view.getContext(), Constant.KEY_SERVICES_NIGHTLY_MODE, Preference.sNightlyMode);
                PreferenceConfig.onPreferenceChanged(PreferenceActivity.this, Constant.TAG_ID_MODE);
            }
        });

        nightModeDialog.setTitle(getString(R.string.preference_nightly_mode));
        nightModeDialog.setContentView(listView);
        nightModeDialog.setRightBtn(getString(R.string.opsbtn_right), null);
        nightModeDialog.show();
    }

    /**
     * Popup dialog to set brightness
     */
    private void configureMatteAlpha() {
        CustomDialog alphaSetterDialog = new CustomDialog(this);

        Rect padding = Utils.getNinePadding(this, R.drawable.dialog_full_holo_dark);
        int[] size = Utils.getScreenSize(getWindowManager());
        int dialogWidth = size[0] - (padding.left + padding.right) * 2;
        if (dialogWidth <= 0) {
            dialogWidth = LayoutParams.MATCH_PARENT;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.nightly_seekbar_layout, null);
        alphaSetterDialog.setTitle(getString(R.string.preference_mask_alpha));
        alphaSetterDialog.setContentView(view, new LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT));
        alphaSetterDialog.setRightBtn(getString(R.string.opsbtn_right), null);
        alphaSetterDialog.setLeftBtn(getString(R.string.opsbtn_default), new OnOpsBtnClickListener() {

            @Override
            public void onClick(View opsBtn) {
                // Reset to default
                Preference.sMatteAlpha = Constant.DEFAULT_ALPHA;
            }
        });

        SeekBar bar = (SeekBar) view.findViewById(R.id.nightly_alpha_seekbar);
        bar.setMax(100);
        bar.setProgress((int) ((Preference.sMatteAlpha - 0.1f) * 100 / 0.8f));
        bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Preference.sMatteAlpha = 0.1f + (0.8f * progress / 100f);
                PreferenceConfig.onPreferenceChanged(PreferenceActivity.this, Constant.TAG_ID_ALPHA);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Ignore
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Ignore
            }
        });

        final float cacheAlpha = Preference.sMatteAlpha;
        alphaSetterDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (cacheAlpha != Preference.sMatteAlpha) {
                    mPreferenceAdapter.notifyDataSetChanged();
                    Preference.saveKey(PreferenceActivity.this, Constant.KEY_MATTE_LAYER_ALPHA, Preference.sMatteAlpha);
                }
            }
        });

        alphaSetterDialog.show();
    }

    /**
     * Popup dialog to set screen color
     */
    private void configureMatteColor() {
        final CustomDialog colorSetterDialog = new CustomDialog(this);
        ColorPicker picker = new ColorPicker(this);
        picker.setColorChangeListener(new OnColorChangeListener() {

            @Override
            public void onColorChanged(ColorObj colorObj) {
                colorSetterDialog.setLeftBtn("" + Integer.toHexString(colorObj.color), null);
            }
        });
        picker.setPadding(20, 20, 20, 20);
        picker.setLayoutParams(new LayoutParams(720, 720));
        colorSetterDialog.setTitle("颜色选择");
        colorSetterDialog.setContentView(picker);
        colorSetterDialog.setLeftBtn(getString(R.string.opsbtn_left), null);
        colorSetterDialog.setRightBtn(getString(R.string.opsbtn_right), null);
        colorSetterDialog.show();
    }
}
