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
package com.awaysoft.nightlymode.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.awaysoft.nightlymode.R;
import com.awaysoft.nightlymode.utils.Constant;

import java.util.ArrayList;

/**
 * Preference configuration.
 *
 * @author ruikye
 * @since 2014
 */
public class PreferenceConfig {
    public static final int HEADER = PreferenceItemHolder.ITEM_TYPE_HEADER;
    public static final int NORMAL = PreferenceItemHolder.ITEM_TYPE_NORMAL;
    public static final int SWITCHER = PreferenceItemHolder.ITEM_TYPE_SWITCHER;
    public static final int CHECKBOX = PreferenceItemHolder.ITEM_TYPE_CHECKBOX;

    private static final ArrayList<PreferenceItemHolder> sPreferenceItems;

    static {
        sPreferenceItems = new ArrayList<PreferenceItemHolder>();
    }

    public static void build(Context context) {
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
            //sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.auto_nightly_time_buckets, R.string.auto_nightly_time_tips, Constant.TAG_ID_AUTO_TIME));
            //sPreferenceItems.add(new PreferenceItemHolder(CHECKBOX, R.string.auto_nightly_for_all, R.string.auto_nightly_for_all_tips, Constant.TAG_ID_GLOBAL));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.auto_nightly_white_list, R.string.auto_nightly_white_list_tips, Constant.TAG_ID_WHITE_LIST));

            /** Information classify */
            sPreferenceItems.add(new PreferenceItemHolder(HEADER, R.string.information, -1, -1));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.information_feedback, R.string.information_feedback_email, Constant.TAG_ID_FEEDBACK));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.information_about, R.string.information_about_author, Constant.TAG_ID_ABOUT));
            sPreferenceItems.add(new PreferenceItemHolder(NORMAL, R.string.license, R.string.open_source, Constant.TAG_ID_LICENSE));
        }
    }

    public static void destroy() {
        if (sPreferenceItems != null) {
            sPreferenceItems.clear();
        }
    }

    public static PreferenceItemHolder get(int position) {
        return sPreferenceItems.get(position);
    }

    public static int getCount() {
        return sPreferenceItems.size();
    }

    public static int getTypeCount() {
        return PreferenceItemHolder.ITEM_TYPE_COUNT;
    }

    public static boolean isEnable(int position) {
        return sPreferenceItems.get(position).isEnable();
    }

    public static View getView(Context context, int position) {
        return sPreferenceItems.get(position).obtainView(context);
    }

    public static void onPreferenceChanged(Context context, int tag) {
        Intent intent = new Intent(Constant.BDC_PREFERENCE_CHENGED);
        intent.putExtra(Constant.PREFERENCE_TARGET_KEY, tag);
        context.sendBroadcast(intent);
    }
}
