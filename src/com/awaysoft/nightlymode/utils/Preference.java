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
package com.awaysoft.nightlymode.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

/**
 * Nightly manager preference
 *
 * @author ruikye
 * @since 2014
 */
public enum Preference {
    INSTANCE;

    /** Global flag */
    public volatile static int sNightlyMode = Constant.MODE_AUTO;
    /** Mask color */
    public static int sMatteColor = Constant.DEFAULT_COLOR;
    /** Mask alpha */
    public static float sMatteAlpha = Constant.DEFAULT_ALPHA;
    /** Auto start */
    public static boolean sAutoStart = false;
    /** Show notification */
    public static boolean sNotification = true;
    /** Show float widget */
    public static boolean sFloatWidget = false;
    /** Apply for all APPs */
    public static boolean sApplyAll = false;
    /** Service status */
    public static boolean sServiceRunning = false;
    /** Nighttime remind */
    public static boolean sNighttimeRemind = false;
    /** Float widget location */
    public static String sFloatLocation = "";
    /** Auto night time buckets */
    public static String sTimeBuckets = Constant.DEFAULT_TIME_BUCKETS;
    /** Auto night white list */
    public static String sWhiteList = Constant.DEFAULT_WHITE_LIST;
    /** Memory white list pool */
    private static ArrayList<Integer> sWhiteListPool = new ArrayList<Integer>();

    public void save(Context context) {
        SharedPreferences sp = getPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Constant.KEY_MATTE_LAYER_COLOR, sMatteColor);
        editor.putFloat(Constant.KEY_MATTE_LAYER_ALPHA, sMatteAlpha);
        editor.putBoolean(Constant.KEY_SERVICES_RUNNING, sServiceRunning);
        editor.putInt(Constant.KEY_SERVICES_NIGHTLY_MODE, sNightlyMode);
        editor.putBoolean(Constant.KEY_SERVICES_STARTUP, sAutoStart);
        editor.putBoolean(Constant.KEY_SERVICES_SHOW_NOTIFICATION, sNotification);
        editor.putBoolean(Constant.KEY_SERVICES_SHOW_FLOAT_WIDGET, sFloatWidget);
        editor.putBoolean(Constant.KEY_NIGHTLY_FOR_ALL_APP, sApplyAll);
        editor.putString(Constant.KEY_NIGHTLY_TIME_BUCKETS, sTimeBuckets);
        editor.putString(Constant.KEY_FLOAT_WIDGET_LOCATION, sFloatLocation);
        convertWhiteList();
        editor.putString(Constant.KEY_NIGHTLY_WHITE_LIST, sWhiteList);

        /*if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {*/
            editor.commit();
        //}
    }

    public void read(Context context) {
        SharedPreferences sp = getPreference(context);

        sMatteColor = sp.getInt(Constant.KEY_MATTE_LAYER_COLOR, Constant.DEFAULT_COLOR);
        sMatteAlpha = sp.getFloat(Constant.KEY_MATTE_LAYER_ALPHA, Constant.DEFAULT_ALPHA);

        sServiceRunning = sp.getBoolean(Constant.KEY_SERVICES_RUNNING, false);
        sAutoStart = sp.getBoolean(Constant.KEY_SERVICES_STARTUP, false);
        sNotification = sp.getBoolean(Constant.KEY_SERVICES_SHOW_NOTIFICATION, true);
        sFloatWidget = sp.getBoolean(Constant.KEY_SERVICES_SHOW_FLOAT_WIDGET, true);
        sNightlyMode = sp.getInt(Constant.KEY_SERVICES_NIGHTLY_MODE, Constant.MODE_AUTO);

        sApplyAll = sp.getBoolean(Constant.KEY_NIGHTLY_FOR_ALL_APP, false);
        sTimeBuckets = sp.getString(Constant.KEY_NIGHTLY_TIME_BUCKETS, Constant.DEFAULT_TIME_BUCKETS);

        sFloatLocation = sp.getString(Constant.KEY_FLOAT_WIDGET_LOCATION, "");

        sWhiteList = sp.getString(Constant.KEY_NIGHTLY_WHITE_LIST, Constant.DEFAULT_WHITE_LIST);
        parseWhiteList();
    }

    public void saveKey(Context context, String key, Object value) {
        SharedPreferences sp = getPreference(context);
        if (value instanceof Boolean) {
            sp.edit().putBoolean(key, (Boolean) value).commit();
        } else if (value instanceof Integer) {
            sp.edit().putInt(key, (Integer) value).commit();
        } else if (value instanceof String) {
            sp.edit().putString(key, String.valueOf(value)).commit();
        } else if (value instanceof Float) {
            sp.edit().putFloat(key, (Float) value).commit();
        }
    }

    public boolean inWhiteList(String pkg) {
        return sWhiteListPool.contains(pkg.hashCode());
    }

    private void parseWhiteList() {
        if (!TextUtils.isEmpty(sWhiteList)) {
            sWhiteListPool.clear();
            String[] array = sWhiteList.split("\\|");
            for (String key : array) {
                sWhiteListPool.add(Integer.valueOf(key));
            }
        }
    }

    private void convertWhiteList() {
        if (sWhiteListPool != null) {
            StringBuilder sb = new StringBuilder();
            for (Integer key : sWhiteListPool) {
                if (key != null) {
                    sb.append(key).append("|");
                }
            }

            sWhiteList = sb.toString();
        }
    }

    public void enableInWhiteList(String pkg, boolean enable) {
        if (TextUtils.isEmpty(pkg)) {
            return;
        }

        if (enable) {
            if (!inWhiteList(pkg)) {
                sWhiteListPool.add(pkg.hashCode());
            }
        } else {
            sWhiteListPool.remove(Integer.valueOf(pkg.hashCode()));
        }
    }

    private SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(Constant.KEY_PREFERENCE_FILE, Context.MODE_PRIVATE);
    }
}
