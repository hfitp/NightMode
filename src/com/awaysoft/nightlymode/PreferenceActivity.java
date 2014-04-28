
package com.awaysoft.nightlymode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.awaysoft.nightlymode.adapter.PreferenceAdapter;
import com.awaysoft.nightlymode.adapter.PreferenceConfig;
import com.awaysoft.nightlymode.adapter.PreferenceItemHolder;
import com.awaysoft.nightlymode.services.NightlyServices;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.widget.Switch;
import com.awaysoft.widget.component.ColorPicker;
import com.awaysoft.widget.component.ColorPicker.ColorObj;
import com.awaysoft.widget.component.ColorPicker.OnColorChangeListener;
import com.awaysoft.widget.component.CustomDialog;
import com.awaysoft.widget.component.CustomDialog.OnOpsBtnClickListener;

public class PreferenceActivity extends Activity implements OnItemClickListener {
    private PreferenceAdapter mPreferenceAdapter;

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
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(PreferenceActivity.this, NightlyServices.class));
                    Preference.sServiceRunning = true;
                } else {
                    Preference.sServiceRunning = false;
                    stopService(new Intent(PreferenceActivity.this, NightlyServices.class));
                }

            }
        });
        switcher.setChecked(Preference.sServiceRunning);

        ListView listView = (ListView) findViewById(R.id.nighlty_listview);
        listView.setAdapter(mPreferenceAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceConfig.onPreferenceChanged(this);
    }

    @Override
    protected void onDestroy() {
        Preference.sActivityRunning = false;
        Preference.save(this);
        PreferenceConfig.destory();
        PreferenceConfig.onPreferenceChanged(this);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PreferenceItemHolder itemHolder = (PreferenceItemHolder) mPreferenceAdapter.getItem(position);
        itemHolder.performClick(view);
        int targetId = itemHolder.getTargetId();
        switch (targetId) {
            case Constant.TAG_ID_MODE: {
                configureNightMode();
                break;
            }
            case Constant.TAG_ID_ALPHA: {
                configureMatteAlpha();
                break;
            }
            case Constant.TAG_ID_COLOR: {
                configureMatteColor();
                break;
            }
            case Constant.TAG_ID_AUTO_TIME: {
                break;
            }
            case Constant.TAG_ID_WHITE_LIST: {
                break;
            }
            case Constant.TAG_ID_FEEDBACK: {
                break;
            }
            case Constant.TAG_ID_ABOUT: {
                break;
            }
        }
    }

    private void configureNightMode() {
        CustomDialog nightModeDialog = new CustomDialog(this);
        ListView listView = (ListView) LayoutInflater.from(this).inflate(R.layout.nightly_listview, null);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.nightly_radio_check, getResources().getStringArray(R.array.night_mode)));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(Preference.sNightlyMode >> Constant.MODE_MASK, true);
        listView.setPadding(0, 0, 0, 0);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Preference.sNightlyMode = (int) Math.pow(2, position);
                mPreferenceAdapter.notifyDataSetChanged();
                Preference.saveKey(view.getContext(), Constant.KEY_SERVICES_NIGHTLY_MODE, Integer.valueOf(Preference.sNightlyMode));
                PreferenceConfig.onPreferenceChanged(PreferenceActivity.this);
            }
        });

        nightModeDialog.setTitle(getString(R.string.preference_nightly_mode));
        nightModeDialog.setContentView(listView);
        nightModeDialog.setRightBtn(getString(R.string.opsbtn_right), null);
        nightModeDialog.show();
    }

    private void configureMatteAlpha() {
        CustomDialog alphaSetterDialog = new CustomDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.nightly_seekbar_layout, null);
        alphaSetterDialog.setTitle(getString(R.string.preference_mask_alpha));
        alphaSetterDialog.setContentView(view, new LayoutParams(720, LayoutParams.WRAP_CONTENT));
        alphaSetterDialog.setRightBtn(getString(R.string.opsbtn_right), null);
        alphaSetterDialog.setLeftBtn(getString(R.string.opsbtn_default), new OnOpsBtnClickListener() {

            @Override
            public void onClick(View opsBtn) {
                // Reset to default
            }
        });
        alphaSetterDialog.show();
    }

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
