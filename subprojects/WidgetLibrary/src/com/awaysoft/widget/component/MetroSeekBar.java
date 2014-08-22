
package com.awaysoft.widget.component;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.awaysoft.widget.library.R;

public class MetroSeekBar extends View {
    private int W = 0;
    private int H = 0;
    private int Ox = 0;
    private int mMinValue = 10;
    private int mMaxValue = 90;
    private int mMinPadding = 10;

    private Paint mSecondBarP;
    private Paint mProgressP;
    private Paint mTextPain;
    private RectF mThumbRect;
    private String mValueLabel;
    private Bitmap mThumbDrawable;
    private DecimalFormat mDecimalFormat;
    private OnProgressChangedListener mProgressChangedListener;

    private float mLastX;
    private float mThumbX = -1;
    private float mPxToDip = 1;
    private float mRealValue;
    private float mThumbWidth = 12;
    private float mThumbHeight = 24;
    private float mSeekBarWidth = 4;
    private float mValueTextHeight = 12;

    private boolean mShowLabel = true;
    private boolean mShowValue = true;
    private boolean mIsDragging = false;

    public static interface OnProgressChangedListener {
        public void onProgressChanged(float value);

        public void onTouchEnd(float value);
    }

    public MetroSeekBar(Context context) {
        this(context, null);
    }

    public MetroSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MetroSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPxToDip = context.getResources().getDimensionPixelSize(R.dimen.px_to_dip);
        mSeekBarWidth *= mPxToDip;
        mValueTextHeight *= mPxToDip;
        mThumbWidth *= mPxToDip;
        mThumbHeight *= mPxToDip;
        mMinPadding = (int) (mMinPadding * mPxToDip);

        mSecondBarP = new Paint();
        mSecondBarP.setStrokeWidth(mSeekBarWidth);

        mProgressP = new Paint();
        mProgressP.setStrokeWidth(mSeekBarWidth);

        mTextPain = new Paint();
        mTextPain.setTextSize(mValueTextHeight);
        mTextPain.setAntiAlias(true);

        mDecimalFormat = new DecimalFormat("#0");
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MetroSeekBar, 0, 0);

        mShowLabel = a.getBoolean(R.styleable.MetroSeekBar_showLabel, true);
        mValueLabel = a.getString(R.styleable.MetroSeekBar_label);
        mShowValue = a.getBoolean(R.styleable.MetroSeekBar_showValue, true);
        mMinValue = a.getInteger(R.styleable.MetroSeekBar_min, 0);
        mMaxValue = a.getInteger(R.styleable.MetroSeekBar_max, 100);
        mRealValue = a.getInteger(R.styleable.MetroSeekBar_progress, 50);

        int color = a.getColor(R.styleable.MetroSeekBar_progressColor, Color.parseColor("#CF007130"));
        mProgressP.setColor(color);

        color = a.getColor(R.styleable.MetroSeekBar_trackColor, Color.parseColor("#CF313131"));
        mSecondBarP.setColor(color);

        color = a.getColor(R.styleable.MetroSeekBar_textColor, Color.parseColor("#AEAEAE"));
        mTextPain.setColor(color);

        int ref = a.getResourceId(R.styleable.MetroSeekBar_thumb, 0);
        if (ref == 0) {
            mThumbDrawable = decodeBitmap(R.drawable.metro_seek_bar_top_thumb);
        } else {
            mThumbDrawable = decodeBitmap(ref);
        }

        a.recycle();
    }

    public void setProgressChangedListener(OnProgressChangedListener progressChangedListener) {
        mProgressChangedListener = progressChangedListener;
    }

    public void setRealValue(final float realValue) {
        post(new Runnable() {
            @Override
            public void run() {
                float temp = (realValue - mMinValue) * W * 1f / (mMaxValue - mMinValue);
                if (temp < 0 || temp > W) {
                    return;
                }

                mRealValue = realValue;
                invalidate();
            }
        });
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        postInvalidate();
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
        postInvalidate();
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
            float height = mValueTextHeight + mSeekBarWidth + mThumbWidth + getPaddingBottom() + getPaddingTop() +
                    3 * mPxToDip + mTextPain.getFontMetrics().bottom / 2;
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saveCount = canvas.save();

        Ox = getPaddingLeft();
        W = getMeasuredWidth() - Ox - getPaddingRight();
        H = getMeasuredHeight();

        if (mThumbX < 0) {
            mThumbX = (mRealValue - mMinValue) * W * 1f / (mMaxValue - mMinValue);
        }

        drawSecondBar(canvas);
        drawProgress(canvas);
        drawThumb(canvas);

        if (mShowValue) {
            drawValueText(canvas);
        }

        if (mShowLabel) {
            drawMinLabel(canvas);
            drawMaxLabel(canvas);
            drawValueLabel(canvas);
        }

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
                if (!mIsDragging) {
                    mIsDragging = mThumbRect.contains(x, y);
                    mLastX = x;
                    needInvalidate = true;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mIsDragging) {
                    float dx = x - mLastX;
                    mThumbX += dx;
                    mLastX = x;
                    mThumbX = Math.max(0, mThumbX);
                    mThumbX = Math.min(W, mThumbX);
                    needInvalidate = true;
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mIsDragging) {
                    mLastX = mThumbX;
                    mIsDragging = false;
                    needInvalidate = true;

                    if (mProgressChangedListener != null) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressChangedListener.onTouchEnd(mRealValue);
                            }
                        });
                    }
                }
                break;
            }
        }

        if (needInvalidate) {
            invalidate();
            if (mProgressChangedListener != null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mRealValue = (mMaxValue - mMinValue) * mThumbX / W + mMinValue;
                        mProgressChangedListener.onProgressChanged(mRealValue);
                    }
                });
            }
        }

        return true;
    }

    private void drawSecondBar(Canvas canvas) {
        canvas.drawLine(Ox, H / 2, Ox + W, H / 2, mSecondBarP);
    }

    private void drawThumb(Canvas canvas) {
        float y = (H - mSeekBarWidth) / 2 - mThumbHeight;
        float x = Ox + mThumbX - mThumbWidth / 2;
        mThumbRect = new RectF();
        mThumbRect.left = x;
        mThumbRect.top = y;
        mThumbRect.right = x + mThumbWidth;
        mThumbRect.bottom = y + mThumbHeight;
        canvas.drawBitmap(mThumbDrawable, null, mThumbRect, null);
    }

    private void drawMinLabel(Canvas canvas) {
        String text = String.valueOf(mMinValue);
        float x = Ox;
        float y = H - mTextPain.getFontMetrics().bottom - 6 * mPxToDip;
        canvas.drawText(text, x, y, mTextPain);
    }

    private void drawMaxLabel(Canvas canvas) {
        String text = String.valueOf(mMaxValue);
        float width = mTextPain.measureText(text);
        float x = Ox + W - width;
        float y = H - mTextPain.getFontMetrics().bottom - 6 * mPxToDip;
        canvas.drawText(text, x, y, mTextPain);
    }

    private void drawValueLabel(Canvas canvas) {
        if (!TextUtils.isEmpty(mValueLabel)) {
            float width = mTextPain.measureText(mValueLabel);
            float x = (getMeasuredWidth() - width) / 2;
            float y = H - mTextPain.getFontMetrics().bottom - 6 * mPxToDip;
            canvas.drawText(mValueLabel, x, y, mTextPain);
        }
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawLine(Ox, H / 2, Ox + mThumbX, H / 2, mProgressP);
    }

    private void drawValueText(Canvas canvas) {
        String text = mDecimalFormat.format((mMaxValue - mMinValue) * mThumbX / W + mMinValue);
        float width = mTextPain.measureText(text);
        float x = Ox + mThumbX + mThumbWidth * 2 / 3;
        float y = (H - mSeekBarWidth) / 2 - mTextPain.getFontMetrics().bottom / 2 - 6 * mPxToDip;

        if (Ox + W - x < width) {
            x = Ox + mThumbX - width - mThumbWidth * 2 / 3;
        }

        mThumbRect.left = Math.min(mThumbRect.left, x);
        mThumbRect.right = Math.max(mThumbRect.right, x + width);
        mThumbRect.bottom += mThumbHeight;

        canvas.drawText(text, x, y, mTextPain);
    }
}
