package com.awaysoft.nightlymode.utils;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class Utils {
    public static WindowManager.LayoutParams generateWindowLayoutParams() {
        return new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
    }

    public static int[] getScrenSize(WindowManager window) {
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
}
