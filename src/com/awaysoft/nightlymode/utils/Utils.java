package com.awaysoft.nightlymode.utils;

import android.annotation.TargetApi;
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
import android.view.WindowManager;

import java.util.List;

public final class Utils {
    @TargetApi(VERSION_CODES.HONEYCOMB_MR2)
    public static int[] getScreenSize(WindowManager window) {
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

    public static Rect getNinePadding(Context context, int resId) {
        Drawable drawable = context.getResources().getDrawable(resId);
        Rect padding = new Rect();
        drawable.getPadding(padding);
        return padding;
    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     */
    public static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        return packageManager.queryIntentActivities(mainIntent, 0);
    }

    public static int getGlobalScrennBrightness(Context context) {
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
    public static void stopAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 开启亮度自动调节
     *
     * @param context
     */
    public static void startAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * 保存亮度设置状态
     *
     * @param resolver
     * @param brightness
     */
    public static void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness",brightness);
        // resolver.registerContentObserver(uri, true, myContentObserver);
        resolver.notifyChange(uri, null);
    }
}
