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
import com.awaysoft.widget.component.ColorPickerView;
import com.awaysoft.widget.component.CustomDialog;
import com.awaysoft.widget.component.CustomDialog.OnOpsBtnClickListener;
import com.awaysoft.widget.component.MetroSeekBar;
import com.umeng.analytics.MobclickAgent;

/**
 * Preference Activity.
 *
 * @author ruikye
 * @since 2014
 */
public class PreferenceActivity extends BaseActivity implements OnItemClickListener {
    private PreferenceAdapter mPreferenceAdapter;
    private Switch mServiceStatSwitch;

    private BroadcastReceiver mPreferenceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(Constant.BDC_PREFERENCE_CHANGED_FOR_ACT, action)) {
                mPreferenceAdapter.notifyDataSetChanged();
            } else if (TextUtils.equals(Constant.BDC_SERVICE_CLOSED, action)) {
                if (mServiceStatSwitch != null) {
                    mServiceStatSwitch.setChecked(false);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nightly_mode_preference);
        mPreferenceAdapter = new PreferenceAdapter(this);

        // 读取配置
        Preference.INSTANCE.read(this);
        PreferenceConfig.INSTANCE.build(this);

        mServiceStatSwitch = (Switch) findViewById(R.id.nightly_switch);
        mServiceStatSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked) {
                    if (!Utils.INSTANCE.isServiceRunning(PreferenceActivity.this, NightlyService.class.getName())) {
                        startService(new Intent(PreferenceActivity.this, NightlyService.class));
                        MobclickAgent.onEvent(PreferenceActivity.this, "start_service");
                    }
                    Preference.sServiceRunning = true;
                    Preference.INSTANCE.saveKey(PreferenceActivity.this, Constant.KEY_SERVICES_RUNNING, true);
                } else {
                    Preference.sServiceRunning = false;
                    Preference.INSTANCE.saveKey(PreferenceActivity.this, Constant.KEY_SERVICES_RUNNING, false);
                    MobclickAgent.onEvent(PreferenceActivity.this, "stop_service");
                    stopService(new Intent(PreferenceActivity.this, NightlyService.class));
                }
            }
        });
        mServiceStatSwitch.setChecked(Preference.sServiceRunning);

        ListView listView = (ListView) findViewById(R.id.nighlty_listview);
        listView.setAdapter(mPreferenceAdapter);
        listView.setOnItemClickListener(this);

        registerReceiver(mPreferenceChangedReceiver, new IntentFilter(Constant.BDC_PREFERENCE_CHANGED_FOR_ACT));
    }

    @Override
    public void onPause() {
        super.onPause();
        Preference.INSTANCE.save(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPreferenceChangedReceiver);
        Preference.INSTANCE.save(this);
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
            case Constant.TAG_ID_COLOR: {
                configureMatteColor();
                break;
            }
            case Constant.TAG_ID_AUTO_TIME: {
                configureNightTime();
                break;
            }
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

    private void configureNightTime() {
        CustomDialog intervalDialog = new CustomDialog(this);

        Rect padding = Utils.INSTANCE.getNinePadding(this, R.drawable.dialog_full_holo_dark);
        int[] size = Utils.INSTANCE.getScreenSize(getWindowManager());
        int dialogWidth = size[0] - (padding.left + padding.right) * 2;
        if (dialogWidth <= 0) {
            dialogWidth = LayoutParams.MATCH_PARENT;
        }

        View view = View.inflate(this, R.layout.nightly_interval_layout, null);
        intervalDialog.setTitle("夜间时段");
        intervalDialog.setContentView(view, new LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT));
        intervalDialog.setLeftBtn("Cancel", null);
        intervalDialog.setRightBtn("Okay", null);
        intervalDialog.show();
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
                Preference.INSTANCE.saveKey(view.getContext(), Constant.KEY_SERVICES_NIGHTLY_MODE, Preference.sNightlyMode);
                PreferenceConfig.INSTANCE.onPreferenceChanged(PreferenceActivity.this, Constant.TAG_ID_MODE);
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

        Rect padding = Utils.INSTANCE.getNinePadding(this, R.drawable.dialog_full_holo_dark);
        int[] size = Utils.INSTANCE.getScreenSize(getWindowManager());
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

        MetroSeekBar seekBar = (MetroSeekBar) view.findViewById(R.id.nightly_alpha_seekbar);
        seekBar.setProgressChangedListener(new MetroSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float value) {
                Preference.sMatteAlpha = value / 100f;
                PreferenceConfig.INSTANCE.onPreferenceChanged(PreferenceActivity.this, Constant.TAG_ID_ALPHA);
            }

            @Override
            public void onTouchEnd(float value) {
                //Ignore
            }
        });

        final float cacheAlpha = Preference.sMatteAlpha;
        alphaSetterDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (cacheAlpha != Preference.sMatteAlpha) {
                    mPreferenceAdapter.notifyDataSetChanged();
                    Preference.INSTANCE.saveKey(PreferenceActivity.this, Constant.KEY_MATTE_LAYER_ALPHA, Preference.sMatteAlpha);
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
        final ColorPickerView picker = new ColorPickerView(this);
        picker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                picker.setBackgroundColor(color);
            }
        });

        Rect padding = Utils.INSTANCE.getNinePadding(this, R.drawable.dialog_full_holo_dark);
        int[] size = Utils.INSTANCE.getScreenSize(getWindowManager());
        int dialogSize = size[0] - (padding.left + padding.right) * 2;
        if (dialogSize <= 0) {
            dialogSize = LayoutParams.MATCH_PARENT;
        } else {
            dialogSize = dialogSize * 4 / 5;
        }

        picker.setColor(Preference.sMatteColor, true);
        picker.setLayoutParams(new LayoutParams(dialogSize, dialogSize));
        colorSetterDialog.setTitle("颜色选择");
        colorSetterDialog.setContentView(picker);
        colorSetterDialog.setLeftBtn(getString(R.string.opsbtn_left), null);
        colorSetterDialog.setRightBtn(getString(R.string.opsbtn_right), new OnOpsBtnClickListener() {
            @Override
            public void onClick(View opsBtn) {
                Preference.sMatteColor = picker.getColor();
                mPreferenceAdapter.notifyDataSetChanged();
                Preference.INSTANCE.saveKey(PreferenceActivity.this, Constant.KEY_MATTE_LAYER_ALPHA, Preference.sMatteColor);
                PreferenceConfig.INSTANCE.onPreferenceChanged(PreferenceActivity.this, Constant.TAG_ID_COLOR);
            }
        });
        colorSetterDialog.show();
    }
}
