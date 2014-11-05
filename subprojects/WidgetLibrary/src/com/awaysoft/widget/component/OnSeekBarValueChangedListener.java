
package com.awaysoft.widget.component;

import android.view.View;

/**
 * Value changed listener for {@link IntervalSeekBar} and {@link MetroSeekBar}
 * 
 * @author kang
 */
public interface OnSeekBarValueChangedListener {
    /**
     * on changing
     * 
     * @param percentage current percentage
     * @return the text to show on seek bar
     */
    public String onChanging(float percentage);

    /**
     * on changed, on MotionEvent.ACTION_UP or MotionEvent.ACTION_CANCEL
     * 
     * @param percentage current percentage
     */
    public void onChanged(float percentage, View seekBar);
}
