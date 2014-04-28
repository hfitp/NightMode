
package com.awaysoft.nightlymode.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.awaysoft.nightlymode.R;
import com.awaysoft.nightlymode.utils.Constant;

public class PreferenceConfig {
    public static final int HEADER = PreferenceItemHolder.ITEM_TYPE_HEADER;
    public static final int NORMAL = PreferenceItemHolder.ITEM_TYPE_NORMAL;
    public static final int SWITCHER = PreferenceItemHolder.ITEM_TYPE_SWITCHER;
    public static final int CHECKBOX = PreferenceItemHolder.ITEM_TYPE_CHECKBOX;

    private static final ArrayList<PreferenceItemHolder> sPreferenceItems;
    static {
        sPreferenceItems = new ArrayList<PreferenceItemHolder>();
    }

    public static final void build(Context context) {
        sPreferenceItems.clear();
        {
            /** Preference classify */
            sPreferenceItems.add(new PreferenceItemHolder(HEADER, R.string.preference, -1, -1));
            sPreferenceItems.add(new PreferenceItemHolder(SWITCHER, R.string.preference_auto_start, R.string.preference_autostart_tips, Constant.TAG_ID_ATUO_START));
            sPreferenceItems.add(new PreferenceItemHolder(SWITCHER, R.string.preference_notification, R.string.preference_notification_tips, Constant.TAG_ID_NOTIFICATION));
            sPreferenceItems.add(new PreferenceItemHolder(SWITCHER, R.string.preference_floatwidget, R.string.preference_floatwidget_tips, Constant.TAG_ID_FLOATWIDGET));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.preference_nightly_mode, -1, Constant.TAG_ID_MODE));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.preference_mask_alpha, R.string.preference_mask_alpha_tips, Constant.TAG_ID_ALPHA));
            //sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.preference_mask_color, R.string.preference_mask_color_tips, Constant.TAG_ID_COLOR));

            /** AutoNightly classify */
            sPreferenceItems.add(new PreferenceItemHolder(HEADER, R.string.auto_nightly, -1, -1));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.auto_nightly_time_buckets, R.string.auto_nightly_time_tips, Constant.TAG_ID_AUTO_TIME));
            //sPreferenceItems.add(new PreferenceItemHolder(CHECKBOX, R.string.auto_nightly_for_all, R.string.auto_nightly_for_all_tips, Constant.TAG_ID_GLOBAL));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.auto_nightly_white_list, -1, Constant.TAG_ID_WHITE_LIST));

            /** Information classify */
            sPreferenceItems.add(new PreferenceItemHolder(HEADER, R.string.information, -1, -1));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.information_feedback, R.string.information_feedback_email, Constant.TAG_ID_FEEDBACK));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.information_about, R.string.information_about_author, Constant.TAG_ID_ABOUT));
        }
    }

    public static final void destory() {
        if (sPreferenceItems != null) {
            sPreferenceItems.clear();
        }
    }

    public static final PreferenceItemHolder get(int position) {
        return sPreferenceItems.get(position);
    }

    public static final int getCount() {
        return sPreferenceItems.size();
    }

    public static final int getTypeCount() {
        return PreferenceItemHolder.ITEM_TYPE_COUNT;
    }

    public static final boolean isEnable(int position) {
        return sPreferenceItems.get(position).isEnable();
    }

    public static final View getView(Context context, int position) {
        return sPreferenceItems.get(position).obtainView(context);
    }

    public static void onPreferenceChanged(Context context) {
        Intent intent = new Intent(Constant.BDC_PREFERENCE_CHENGED);
        context.sendBroadcast(intent);
    }
}
