
package com.awaysoft.widget.utils;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 滑动辅助
 *
 * @author kangyonggen
 */
public class ScrollerHelper {
    public int distanceX;
    public int distanceY;
    public int scrollX;
    public int scrollY;
    public final Scroller scroller;

    public ScrollerHelper(Context context) {
        scroller = new Scroller(context);
        distanceX = 0;
    }

    public ScrollerHelper(Context context, Interpolator interpolator) {
        scroller = new Scroller(context, interpolator);
        distanceX = 0;
    }

    public boolean isFinished() {
        return scroller.isFinished();
    }

    public void startScroll(int x, int y, int dx, int dy, int duration) {
        scrollX = 0;
        scrollY = 0;
        distanceX = dx;
        distanceY = dy;
        scroller.startScroll(x, y, dx, dy, duration);
    }

    public boolean computeScrollOffset() {
        return scroller.computeScrollOffset();
    }

    public int getCurrX() {
        return scroller.getCurrX();
    }

    public void abortAnimation() {
        scrollX = 0;
        scrollY = 0;
        distanceX = 0;
        distanceY = 0;
        scroller.abortAnimation();
    }
}
