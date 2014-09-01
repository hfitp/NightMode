package com.awaysoft.widget.component;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.awaysoft.widget.library.R;

public class IntervalSeekBar extends View {
    private int W = 0;
    private int H = 0;
    private int Ox = 0;

    private int mMinPadding = 10;

    private boolean mMinIsDraging = false;
    private boolean mMaxIsDraging = false;

    private Paint mSecondBarP;
    private Paint mProgressP;
    private Paint mTextPain;

    private RectF mMinThumbRect;
    private RectF mMaxThumbRect;

    private Bitmap mMinThumbDrawble;
    private Bitmap mMaxThumbDrawble;

    private DecimalFormat mDecimalFormat;

    private float mMinPercentage = 0f;
    private float mMaxPercentage = 1f;

    private float mLastX;

    private float mSeekBarWidth = 4;

    private float mMinValue = 0;
    private float mMaxValue = 0;

    private float mValueTextHeight = 12;
    private float mThumbWidth = 12;
    private float mThumbHeight = 24;

    private float mPxToDip = 1;

    private String mMinValueText;
    private String mMaxValueText;

    private OnSeekBarValueChangedListenter mChangedListenter;

    public IntervalSeekBar(Context context) {
        this(context, null);
    }

    public IntervalSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntervalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPxToDip = context.getResources().getDimensionPixelSize(R.dimen.px_to_dip);
        mSeekBarWidth *= mPxToDip;
        mValueTextHeight *= mPxToDip;
        mThumbWidth *= mPxToDip;
        mThumbHeight *= mPxToDip;
        mMinPadding = (int) (mMinPadding * mPxToDip);

        mSecondBarP = new Paint();
        mSecondBarP.setColor(Color.DKGRAY);
        mSecondBarP.setStrokeWidth(mSeekBarWidth);

        mProgressP = new Paint();
        mProgressP.setColor(Color.parseColor("#CF229A22"));
        mProgressP.setStrokeWidth(mSeekBarWidth);

        mTextPain = new Paint();
        mTextPain.setTextSize(mValueTextHeight);
        mTextPain.setColor(Color.GRAY);
        mTextPain.setAntiAlias(true);

        mDecimalFormat = new DecimalFormat("#0.00");
        mMinThumbDrawble = decodeBitmap(R.drawable.metro_seek_bar_bottom_thumb);
        mMaxThumbDrawble = decodeBitmap(R.drawable.metro_seek_bar_top_thumb);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    public void setValueChangedListener(OnSeekBarValueChangedListenter changeListener) {
        mChangedListenter = changeListener;
        getShowText();
        invalidate();
    }

    /**
     * set current min percentage
     *
     * @param minpercentage, rage: 0f ~ 1.0f
     */
    public final void setMinPercentage(float minpercentage) {
        minpercentage = Math.max(0, minpercentage);
        minpercentage = Math.min(1, minpercentage);
        mMinPercentage = minpercentage;
        getShowText();
        invalidate();
    }

    /**
     * Get min percentage, range: 0f ~ 1.0f
     *
     * @return current min percentage
     */
    public final float getMinPercentage() {
        return mMinPercentage;
    }

    /**
     * set current max percentage
     *
     * @param maxpercentage, rage: 0f ~ 1.0f
     */
    public final void setMaxPercentage(float maxpercentage) {
        maxpercentage = Math.max(0, maxpercentage);
        maxpercentage = Math.min(1, maxpercentage);
        mMaxPercentage = maxpercentage;
        getShowText();
        invalidate();
    }

    /**
     * Get max percentage, range: 0f ~ 1.0f
     *
     * @return current max percentage
     */
    public final float getMaxPercentage() {
        return mMaxPercentage;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        left = Math.max(left, mMinPadding);
        right = Math.max(right, mMinPadding);
        top = Math.max(top, mMinPadding);
        bottom = Math.max(bottom, mMinPadding);
        super.setPadding(left, top, right, bottom);
    }

    private Bitmap decodeBitmap(int resId) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(), resId, null);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
        }

        return bitmap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            float height = mValueTextHeight * 2 + mSeekBarWidth + mThumbHeight * 2 + getPaddingBottom() + getPaddingTop() +
                    6 * mPxToDip + mTextPain.getFontMetrics().bottom;
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY));
        }

        Ox = getPaddingLeft();
        W = getMeasuredWidth() - Ox - getPaddingRight();
        H = getMeasuredHeight();

        mMinValue = mMinPercentage * W;
        mMaxValue = mMaxPercentage * W;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saveCount = canvas.save();
        drawSecondBar(canvas);
        drawProgress(canvas);
        drawMinThumb(canvas);
        drawMaxThumb(canvas);
        drawMinValueText(canvas);
        drawMaxValueText(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean needInvalidate = false;
        int action = event.getAction();
        float x = event.getX(0);
        float y = event.getY(0);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (!mMinIsDraging && !mMaxIsDraging) {
                    mMinIsDraging = mMinThumbRect.contains(x, y);
                    if (!mMinIsDraging) {
                        mMaxIsDraging = mMaxThumbRect.contains(x, y);
                    }

                    if (mMaxIsDraging || mMinIsDraging) {
                        mLastX = x;
                        needInvalidate = true;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mMinIsDraging || mMaxIsDraging) {
                    float dx = x - mLastX;
                    mLastX = x;

                    if (mMinIsDraging) {
                        mMinValue += dx;
                        mMinValue = Math.max(0, mMinValue);
                        mMinValue = Math.min(mMaxValue, mMinValue);
                        mMinPercentage = mMinValue / W;
                    } else if (mMaxIsDraging) {
                        mMaxValue += dx;
                        mMaxValue = Math.max(mMinValue, mMaxValue);
                        mMaxValue = Math.min(W, mMaxValue);
                        mMaxPercentage = mMaxValue / W;
                    }

                    needInvalidate = true;
                }

                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mMinIsDraging) {
                    mLastX = mMinValue;
                    mMinIsDraging = false;
                    needInvalidate = true;
                }

                if (mMaxIsDraging) {
                    mLastX = mMaxValue;
                    mMaxIsDraging = false;
                    needInvalidate = true;
                }
                break;
            }
        }

        if (needInvalidate) {
            if (mMinIsDraging || mMaxIsDraging) {
                getShowText();
            } else {
                if (mChangedListenter != null) {
                    post(new Runnable() {

                        @Override
                        public void run() {
                            if (mMinIsDraging) {
                                mChangedListenter.onChanged(mMinPercentage);
                            } else {
                                mChangedListenter.onChanged(mMaxPercentage);
                            }
                        }
                    });
                }
            }

            invalidate();
        }

        return true;
    }

    private void getShowText() {
        if (mChangedListenter != null) {
            mMinValueText = mChangedListenter.onChanging(mMinPercentage);
            mMaxValueText = mChangedListenter.onChanging(mMaxPercentage);
        }
    }

    private void drawSecondBar(Canvas canvas) {
        canvas.drawLine(Ox, H / 2, Ox + W, H / 2, mSecondBarP);
    }

    private void drawMinThumb(Canvas canvas) {
        float y = (H - mSeekBarWidth) / 2 + mSeekBarWidth;
        float x = Ox + mMinValue - mThumbWidth / 2;

        if (mMinThumbRect == null) {
            mMinThumbRect = new RectF();
        }

        mMinThumbRect.left = x;
        mMinThumbRect.top = y;
        mMinThumbRect.right = x + mThumbWidth;
        mMinThumbRect.bottom = y + mThumbHeight;
        canvas.drawBitmap(mMinThumbDrawble, null, mMinThumbRect, null);
    }

    private void drawMaxThumb(Canvas canvas) {
        float y = (H - mSeekBarWidth) / 2 - mThumbHeight;
        float x = Ox + mMaxValue - mThumbWidth / 2;
        if (mMaxThumbRect == null) {
            mMaxThumbRect = new RectF();
        }

        mMaxThumbRect.left = x;
        mMaxThumbRect.top = y;
        mMaxThumbRect.right = x + mThumbWidth;
        mMaxThumbRect.bottom = y + mThumbHeight;
        canvas.drawBitmap(mMaxThumbDrawble, null, mMaxThumbRect, null);
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawLine(Ox + mMinValue, H / 2, Ox + mMaxValue, H / 2, mProgressP);
    }

    private void drawMinValueText(Canvas canvas) {
        String text;
        if (mChangedListenter == null) {
            text = mDecimalFormat.format(mMinPercentage * 100) + "%";
        } else {
            text = mMinValueText;
        }

        float width = mTextPain.measureText(text);
        float x = Ox + mMinValue - width / 2;
        float y = (H + mSeekBarWidth) / 2 + mThumbHeight + mTextPain.getFontMetrics().bottom / 2 + 6 * mPxToDip;

        x = Math.max(x, getPaddingLeft());
        x = Math.min(x, getMeasuredWidth() - width - getPaddingRight());

        mMinThumbRect.left = Math.min(mMinThumbRect.left, x);
        mMinThumbRect.right = Math.max(mMinThumbRect.right, x + width);
        mMinThumbRect.bottom = y + mMinPadding;

        canvas.drawText(text, x, y, mTextPain);
    }

    private void drawMaxValueText(Canvas canvas) {
        String text;
        if (mChangedListenter == null) {
            text = mDecimalFormat.format(mMaxPercentage * 100) + "%";
        } else {
            text = mMaxValueText;
        }

        float width = mTextPain.measureText(text);
        float x = Ox + mMaxValue - width / 2;
        float y = (H - mSeekBarWidth) / 2 - mThumbHeight + mTextPain.getFontMetrics().bottom / 2;

        x = Math.max(x, getPaddingLeft());
        x = Math.min(x, getMeasuredWidth() - width - getPaddingRight());

        mMaxThumbRect.left = Math.min(mMaxThumbRect.left, x);
        mMaxThumbRect.right = Math.max(mMaxThumbRect.right, x + width);
        mMaxThumbRect.top = y - mValueTextHeight - mMinPadding;

        canvas.drawText(text, x, y, mTextPain);
    }
}
