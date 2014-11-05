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

import java.util.Calendar;
import java.util.Date;
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

    public int getGlobalScreenBrightness(Context context) {
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
     * @param context context
     */
    public void stopAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 开启亮度自动调节
     *
     * @param context context
     */
    public void startAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * 保存亮度设置状态
     *
     * @param resolver   to save
     * @param brightness value
     */
    public void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness", brightness);
        resolver.notifyChange(uri, null);
    }

    private PendingIntent generateAlarmIntent(Context context, String flag) {
        Intent intent = new Intent(context, NightRemindAct.class);
        intent.putExtra(Constant.ALARM_INTENT_FLAG, flag);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * 设置指定时间的闹钟
     *
     * @param context context
     * @param hours   小时：00 ~ 23
     * @param minutes 分钟：00 ~ 59
     */
    public void startNightAlarm(Context context, int hours, int minutes, String flag) {
        //safety check
        if (hours < 0 || hours >= 24) {
            hours = 0;
        }

        if (minutes < 0 || hours >= 60) {
            minutes = 0;
        }

        Log.d("night alarm", "Flag: " + flag + " @ " + hours + ":" + minutes);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getMillisFromNow(hours, minutes),
                Constant.MILLISECOND_OF_DAY, generateAlarmIntent(context, flag));
    }

    public void stopNightAlarm(Context context, String flag) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(generateAlarmIntent(context, flag));
    }

    /**
     * 设置延迟闹钟
     *
     * @param context      context
     * @param milliseconds 延迟时间
     */
    public void delayedAlarm(Context context, long milliseconds, String flag) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + milliseconds, generateAlarmIntent(context, flag));
    }

    public long getMillisFromNow(int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();

        int nowH = calendar.get(Calendar.HOUR_OF_DAY);
        int nowM = calendar.get(Calendar.MINUTE);

        if (nowH > hours || (nowH == hours && nowM >= minutes)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    /**
     * 判断是否开启了自动亮度调节
     */
    public static boolean isAutoBrightness(ContentResolver aContentResolver) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    public boolean isServiceRunning(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);

        if (serviceList == null || serviceList.isEmpty()) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo aServiceList : serviceList) {
            if (aServiceList.service.getClassName().equals(className)) {
                return true;
            }
        }

        return false;
    }
}
