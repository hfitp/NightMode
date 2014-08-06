
package com.awaysoft.widget.component;

import com.awaysoft.widget.library.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View {
    private static final int SPLIT_RATE = 8;
    private static final int SELECTOR_SIZE = 10;

    private LinearGradient mPickerGradient;
    private LinearGradient mGradeGradient;
    private LinearGradient mAlphaGradient;
    private LinearGradient mPickerMatte;

    private Paint mPaint;
    private Paint mAlphaPaint;
    private Paint mSelectorPaint;

    private Rect mPickerRect;
    private Rect mGradeRect;
    private Rect mAlphaRect;

    private int mAlphaLoc = 0;
    private int mGradeLoc = 360;
    private int[] mPickerLoc = new int[]{
            120, 80
    };

    private boolean mIsAlphaMove;
    private boolean mIsPickerMove;
    private boolean mIsGradeMove;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    public class ColorObj {
        public int color;
        public int gradeLoc;
        public int[] pickerLoc;

        public ColorObj(int color, int grade, int[] pickerLoc) {
            this.color = color;
            gradeLoc = grade;
            this.pickerLoc = pickerLoc;
        }
    }

    public interface OnColorChangeListener {
        public void onColorChanged(ColorObj colorObj);
    }

    private OnColorChangeListener mColorChangeListener;

    public ColorPicker(Context context) {
        this(context, null);
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        mPaint.setStrokeWidth(0f);

        mSelectorPaint = new Paint();
        mSelectorPaint.setColor(0x7fffffff);
        mSelectorPaint.setStrokeWidth(2);
        mSelectorPaint.setAntiAlias(true);
        mSelectorPaint.setStyle(Style.STROKE);

        mAlphaPaint = new Paint();
        mAlphaPaint.setShader(new BitmapShader(BitmapFactory.decodeResource(getResources(), R.drawable.alpha_bg),
                TileMode.REPEAT, TileMode.REPEAT));

        post(new Runnable() {

            @Override
            public void run() {
                initializedRect();
                mGradeLoc = mGradeRect.top;
                mAlphaLoc = mAlphaRect.left;
                mPickerLoc = new int[]{
                        mPickerRect.left, mPickerRect.bottom - 2
                };

                mGradeLoc = 540;
                mPickerLoc = new int[]{
                        480, 254
                };

                mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
                changeColor(true);
            }
        });
    }

    public void setColorObj(ColorObj obj) {
        if (obj != null) {
            mGradeLoc = obj.gradeLoc;
            mAlphaLoc = getAlphaLoc(Color.alpha(obj.color));
            mPickerLoc = obj.pickerLoc;
            changeColor(true);
        }
    }

    public void setColorChangeListener(OnColorChangeListener listener) {
        mColorChangeListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX(0);
        int y = (int) event.getY(0);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mAlphaRect.contains(x, y)) {
                    mIsAlphaMove = true;
                } else if (mGradeRect.contains(x, y)) {
                    mIsGradeMove = true;
                } else if (mPickerRect.contains(x, y)) {
                    mIsPickerMove = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsAlphaMove) {
                    if (x <= mAlphaRect.left) {
                        mAlphaLoc = mAlphaRect.left;
                    } else if (x >= mAlphaRect.right) {
                        mAlphaLoc = mAlphaRect.right - 1;
                    } else {
                        mAlphaLoc = x;
                    }
                } else if (mIsGradeMove) {
                    if (y <= mGradeRect.top) {
                        mGradeLoc = mGradeRect.top;
                    } else if (y >= mGradeRect.bottom) {
                        mGradeLoc = mGradeRect.bottom - 1;
                    } else {
                        mGradeLoc = y;
                    }
                } else if (mIsPickerMove) {
                    if (x <= mPickerRect.left) {
                        mPickerLoc[0] = mPickerRect.left;
                    } else if (x >= mPickerRect.right) {
                        mPickerLoc[0] = mPickerRect.right - 1;
                    } else {
                        mPickerLoc[0] = x;
                    }

                    if (y <= mPickerRect.top) {
                        mPickerLoc[1] = mPickerRect.top;
                    } else if (y >= mPickerRect.bottom) {
                        mPickerLoc[1] = mPickerRect.bottom;
                    } else {
                        mPickerLoc[1] = y;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsAlphaMove = false;
                mIsGradeMove = false;
                mIsPickerMove = false;
                break;
        }

        postInvalidate();
        changeColor(false);

        return true;
    }

    private void sort(int[] a) {
        int i, j, t;
        for (i = 0; i < a.length - 1; i++) {
            for (j = 0; j < a.length - i - 1; j++) {
                if (a[j] < a[j + 1]) {
                    t = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = t;
                }
            }
        }
    }

    private int getAlphaLoc(int alpha) {
        return (255 - alpha) * mAlphaRect.width() / 255 + mAlphaRect.left;
    }

    private int getAlpha(int loc) {
        int alpha = 255 - (loc - mAlphaRect.left) * 255 / mAlphaRect.width();
        alpha = alpha > 255 ? 255 : alpha;
        alpha = alpha < 0 ? 0 : alpha;
        return alpha;
    }

    private void changeColor(final boolean init) {
        post(new Runnable() {

            @Override
            public void run() {
                if (mIsGradeMove || init) {
                    resetColorPicker(getColorIn(mGradeRect.left + mGradeRect.width() / 2, mGradeLoc));
                }

                if (mIsAlphaMove || mIsGradeMove || mIsPickerMove || init) {
                    int color = getColorIn(mPickerLoc[0], mPickerLoc[1]);
                    color = Color.argb(getAlpha(mAlphaLoc), Color.red(color), Color.green(color), Color.blue(color));

                    if (!init && mColorChangeListener != null) {
                        mColorChangeListener.onColorChanged(new ColorObj(getColorIn(mGradeRect.left, mGradeLoc), mGradeLoc, mPickerLoc));
                    }
                    setBackgroundColor(color);

                    if (init) {
                        vertexColor(color);
                    }
                }
            }
        });
    }

    private void initializedRect() {
        int left = getPaddingLeft();
        int right = getPaddingRight();
        int top = getPaddingTop();
        int bottom = getPaddingBottom();
        int w = getWidth();
        int h = getHeight();

        int ax = w - left - right - 20;
        int ay = h - top - bottom - 20;

        int splitX = ax / SPLIT_RATE;
        int splitY = ay / SPLIT_RATE;

        mPickerRect = new Rect(left, top, w - splitX - 20 - bottom, h - splitY - 20 - bottom);
        mPickerMatte = new LinearGradient(mPickerRect.left, mPickerRect.top, mPickerRect.left, mPickerRect.bottom, new int[]{
                0x000000, 0xff000000
        }, null, TileMode.CLAMP);

        mGradeRect = new Rect(w - right - splitX, top, w - right, h - splitY - 20 - bottom);
        mGradeGradient = new LinearGradient(mGradeRect.right, mGradeRect.top, mGradeRect.right, mGradeRect.bottom,
                buildHueColorArray(), null, TileMode.CLAMP);

        mAlphaRect = new Rect(left, h - bottom - splitY, w - left, h - bottom);
        mAlphaGradient = new LinearGradient(mAlphaRect.left, mAlphaRect.bottom,
                mAlphaRect.right, mAlphaRect.bottom, 0xff000000, 0x000000, TileMode.CLAMP);
    }

    private int[] buildHueColorArray() {
        int[] hue = new int[361];

        int count = 0;
        for (int i = hue.length - 1; i >= 0; i--, count++) {
            hue[count] = Color.HSVToColor(new float[]{
                    i, 1f, 1f
            });
        }
        return hue;
    }

    private void resetColorPicker(int color) {
        mPickerGradient = new LinearGradient(mPickerRect.left, mPickerRect.bottom, mPickerRect.right,
                mPickerRect.bottom, 0xffffffff, color, TileMode.CLAMP);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        innerDraw(canvas, true);
    }

    private void innerDraw(Canvas canvas, boolean toVisible) {
        mPaint.setShader(mPickerGradient);
        canvas.drawRect(mPickerRect, mPaint);

        mPaint.setShader(mPickerMatte);
        canvas.drawRect(mPickerRect, mPaint);

        mPaint.setShader(mGradeGradient);
        canvas.drawRect(mGradeRect, mPaint);

        if (toVisible) {
            canvas.drawRect(mAlphaRect, mAlphaPaint);
            mPaint.setShader(mAlphaGradient);
            canvas.drawRect(mAlphaRect, mPaint);

            drawAlphaPicker(canvas, mAlphaLoc);
            drawGradePicker(canvas, mGradeLoc);
            drawColorPicker(canvas, mPickerLoc[0], mPickerLoc[1]);
        }
    }

    private void vertexColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int[] s = new int[]{
                r, g, b
        };

        sort(s);

        float[] v = calCrossPoint(new float[]{
                s[1] - s[2], s[2] - s[0], s[0] - s[1]
        }, new float[]{
                s[0], s[1], s[2]
        }, new float[]{
                0, 1, 0
        }, new float[]{
                255, 0, 0
        });

        float[] c00 = calCrossPoint(new float[]{
                0, 0, 1
        }, new float[]{
                0, 255, 0
        }, new float[]{
                0, 255 - v[1], 255
        }, new float[]{
                s[0], s[1], s[2]
        });

        float[] c11 = calCrossPoint(new float[]{
                0, -1, 1
        }, new float[]{
                255, 255, 255
        }, new float[]{
                s[0] - c00[0], s[1] - c00[1], s[2] - c00[2]
        }, new float[]{
                s[0], s[1], s[2]
        });

        c11 = calCrossPoint(new float[]{
                1, 0, 0
        }, new float[]{
                255, 255, 255
        }, new float[]{
                255, v[1], 0
        }, c11);

        Log.i("PickerColor", String.format("C00(%f,%f,%f), C11(%f,%f,%f)", c00[0], c00[1], c00[2], c11[0], c11[1], c11[2]));
        float d00 = calDistance(c00, new float[]{
                0, 0, 0
        });

        float t00 = calDistance(v, new float[]{
                0, 0, 0
        });

        float d11 = calDistance(c11, new float[]{
                255, 255, 255
        });

        float t11 = calDistance(v, new float[]{
                255, 255, 255
        });

        Log.d("PickerColor", String.format("D11:%f, T11:%f", d11, t11));

        float x = mPickerRect.width() * d11 / t11 + mPickerRect.left;
        float y = mPickerRect.bottom - mPickerRect.height() * d00 / t00;

        Log.d("PickerColor", String.format("Rect:(%d,%d,%d,%d)", mPickerRect.left, mPickerRect.top, mPickerRect.right, mPickerRect.bottom));
        Log.d("PickerColor", String.format("P:(%d,%d), C:(%f,%f)", mPickerLoc[0], mPickerLoc[1], x, y));

        if (s[0] == r) {
            r = Math.round(v[0]);
            if (s[1] == g) {
                g = Math.round(v[1]);
                b = Math.round(v[2]);
            } else {
                b = Math.round(v[1]);
                g = Math.round(v[2]);
            }
        } else if (s[0] == g) {
            g = Math.round(v[0]);
            if (s[1] == r) {
                r = Math.round(v[1]);
                b = Math.round(v[2]);
            } else {
                b = Math.round(v[1]);
                r = Math.round(v[2]);
            }
        } else {
            b = Math.round(v[0]);
            if (s[1] == g) {
                g = Math.round(v[1]);
                r = Math.round(v[2]);
            } else {
                r = Math.round(v[1]);
                g = Math.round(v[2]);
            }
        }

        color = Color.argb(0xff, r, g, b);
        Log.i("PickerColor", Integer.toHexString(color));
    }

    private float[] calCrossPoint(float[] pv, float[] pp, float[] lv, float[] lp) {
        float vpt = lv[0] * pv[0] + lv[1] * pv[1] + lv[2] * pv[2];

        if (vpt == 0) {
            return new float[]{
                    255, 0, 0
            };
        }

        float t = ((pp[0] - lp[0]) * pv[0] + (pp[1] - lp[1]) * pv[1] + (pp[2] - lp[2]) * pv[2]) / vpt;
        float[] cp = new float[3];
        cp[0] = lp[0] + lv[0] * t;
        cp[1] = lp[1] + lv[1] * t;
        cp[2] = lp[2] + lv[2] * t;
        return cp;
    }

    private float calDistance(float[] p1, float[] p2) {
        float dx = p1[0] - p2[0];
        float dy = p1[1] - p2[1];
        float dz = p1[2] - p2[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private int getColorIn(int x, int y) {
        innerDraw(mCanvas, false);
        return mBitmap.getPixel(x, y);
    }

    private void drawAlphaPicker(Canvas canvas, int x) {
        int size = SELECTOR_SIZE / 2;
        mSelectorPaint.setColor(0x7fffffff);
        canvas.drawRect(x - size, mAlphaRect.top - size, x + size, mAlphaRect.bottom + size, mSelectorPaint);
        mSelectorPaint.setColor(0x7f000000);
        canvas.drawRect(x - size + 2, mAlphaRect.top - size + 2, x + size - 2, mAlphaRect.bottom + size - 2, mSelectorPaint);
    }

    private void drawColorPicker(Canvas canvas, int x, int y) {
        int size = SELECTOR_SIZE;
        mSelectorPaint.setColor(0x7fffffff);
        canvas.drawCircle(x, y, size, mSelectorPaint);
        mSelectorPaint.setColor(0x7f000000);
        canvas.drawCircle(x, y, size - 2, mSelectorPaint);
    }

    private void drawGradePicker(Canvas canvas, int y) {
        int size = SELECTOR_SIZE / 2;
        mSelectorPaint.setColor(0x7fffffff);
        canvas.drawRect(mGradeRect.left - size, y - size, mGradeRect.right + size, y + size, mSelectorPaint);
        mSelectorPaint.setColor(0x7f000000);
        canvas.drawRect(mGradeRect.left - size + 2, y - size + 2, mGradeRect.right + size - 2, y + size - 2, mSelectorPaint);
    }
}
