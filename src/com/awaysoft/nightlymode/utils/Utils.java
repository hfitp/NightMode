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

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import com.awaysoft.nightlymode.NightRemindAct;

import java.util.List;

/**
 * Utils.
 *
 * @author ruikye
 * @since 2014
 */
public enum Utils {
    INSTANCE;

    @TargetApi(VERSION_CODES.HONEYCOMB_MR2)
    public int[] getScreenSize(WindowManager window) {
        int[] size = new int[2];

        if (Build.VERSION_CODES.HONEYCOMB_MR2 <= Build.VERSION.SDK_INT) {
            Point point = new Point();
            window.getDefaultDisplay().getSize(point);
            size[0] = point.x;
            size[1] = point.y;
        } else {
            size[0] = window.getDefaultDisplay().getWidth();
            size[1] = window.getDefaultDisplay().getHeight();
        }

        return size;
    }

    public Rect getNinePadding(Context context, int resId) {
        Drawable drawable = context.getResources().getDrawable(resId);
        Rect padding = new Rect();
        drawable.getPadding(padding);
        return padding;
    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     */
    public List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        return packageManager.queryIntentActivities(mainIntent, 0);
    }

    public int getGlobalScrennBrightness(Context context) {
        int nowBrightnessValue = -1;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            // Ignore
        }
        return nowBrightnessValue;
    }

    /**
     * 停止自动亮度调节
     *
     * @param context
     */
    public void stopAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 开启亮度自动调节
     *
     * @param context
     */
    public void startAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * 保存亮度设置状态
     *
     * @param resolver
     * @param brightness
     */
    public void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness", brightness);
        // resolver.registerContentObserver(uri, true, myContentObserver);
        resolver.notifyChange(uri, null);
    }

    private PendingIntent genarateAlarmIntent(Context context) {
        Intent intent = new Intent(context, NightRemindAct.class);
        return PendingIntent.getActivity(context, 0, intent, Intent.FILL_IN_ACTION);
    }

    public void startNightAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, genarateAlarmIntent(context));
    }

    public void stopNightAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(genarateAlarmIntent(context));
    }

    public boolean isServiceRunning(Context context, String className) {
        Log.d("NightMode", "--------" + className);

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);

        if (serviceList == null || serviceList.isEmpty()) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo aServiceList : serviceList) {
            Log.d("NightMode", aServiceList.service.getClassName());
            if (aServiceList.service.getClassName().equals(className)) {
                return true;
            }
        }

        return false;
    }
}
