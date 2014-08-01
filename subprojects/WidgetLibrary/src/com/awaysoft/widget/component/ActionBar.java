
package com.awaysoft.widget.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awaysoft.widget.library.R;
import com.awaysoft.widget.utils.AnimUtils;

public class ActionBar extends LinearLayout {
    private boolean mBackEnable;
    private boolean mTitleClickable;

    private TextView mTitle;
    private LinearLayout mActionTitle;
    private OnClickListener mTitleClickListener;

    public ActionBar(Context context) {
        this(context, null);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutParams params;
        setClickable(false);
        setOrientation(HORIZONTAL);

        mActionTitle = (LinearLayout) inflate(context, R.layout.wl_actionbar_title, null);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        addView(mActionTitle, params);

        mTitle = (TextView) mActionTitle.findViewById(R.id.actionbar_title);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar, 0, 0);

        mBackEnable = a.getBoolean(R.styleable.ActionBar_backable, false);
        boolean titleClickable = a.getBoolean(R.styleable.ActionBar_titleClickable, false);

        if (mBackEnable) {
            Drawable backDrawable = a.getDrawable(R.styleable.ActionBar_backDrawable);
            setBackDrawable(backDrawable);
        }

        setTitleClickable(titleClickable);

        if (mTitleClickable) {
            Drawable titleBackground = a.getDrawable(R.styleable.ActionBar_titleBackgroud);
            setTitleBackground(titleBackground);
        }

        Drawable icon = a.getDrawable(R.styleable.ActionBar_icon);
        setTitleIconDrawable(icon);

        String title = a.getString(R.styleable.ActionBar_title);
        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(Html.fromHtml(title));
        }

        int titleSize = a.getDimensionPixelSize(R.styleable.ActionBar_titleTextSize, 0);
        if (titleSize != 0) {
            mTitle.getPaint().setTextSize(titleSize);
        }

        int titleStyle = a.getInt(R.styleable.ActionBar_titleTextStyle, -1);
        if (titleStyle != -1) {
            mTitle.setTypeface(mTitle.getTypeface(), titleStyle);
        }

        ColorStateList colors = a.getColorStateList(R.styleable.ActionBar_titleTextColor);
        if (colors != null) {
            mTitle.setTextColor(colors);
        }

        int appearance = a.getResourceId(R.styleable.ActionBar_titleTextAppearance, 0);
        if (appearance != 0) {
            mTitle.setTextAppearance(context, appearance);
        }

        int optionLayout = a.getResourceId(R.styleable.ActionBar_optionLayout, 0);
        if (optionLayout != 0) {
            // give a space between title and options
            addView(new View(context), new LayoutParams(0, 0, 1));
            inflate(context, optionLayout, this);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mActionTitle.setOnClickListener(l);
    }

    public void setBackEnable(boolean backEnable) {
        if (mBackEnable != backEnable) {
            mBackEnable = backEnable;
            setTitleClickable(mBackEnable || mTitleClickable);
            AnimUtils.alphaAnim(getStubImageView(R.id.actionbar_back_img), mBackEnable);
        }
    }

    public void setTitleClickable(boolean clickable) {
        mTitleClickable = clickable || mBackEnable;
        mActionTitle.setClickable(mTitleClickable);
        mActionTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTitleClickListener != null) {
                    mTitleClickListener.onClick(v);
                }
            }
        });
    }

    public void setTitleClickLinstener(OnClickListener linstener) {
        mTitleClickListener = linstener;
    }

    public void setTitleBackground(int resId) {
        setTitleBackground(getResources().getDrawable(resId));
    }

    public void setTitleBackground(Drawable drawable) {
        int padding = -1;
        if (drawable == null) {
            drawable = getResources().getDrawable(R.drawable.list_item_drawable);
            padding = getResources().getDimensionPixelSize(R.dimen.default_padding);
        }

        mActionTitle.setBackgroundDrawable(drawable);
        if (padding != -1) {
            mActionTitle.setPadding(0, 0, padding, 0);
        }
    }

    public void setBackDrawable(int resId) {
        setBackDrawable(getResources().getDrawable(resId));
    }

    public void setBackDrawable(Drawable drawable) {
        if (drawable == null) {
            drawable = getResources().getDrawable(R.drawable.ic_ab_back_holo_dark);
        }

        ImageView iconImg = getStubImageView(R.id.actionbar_back_img);
        if (iconImg != null) {
            iconImg.setImageDrawable(drawable);
        }
    }

    public void setTitleIconResource(int resId) {
        setTitleIconDrawable(getResources().getDrawable(resId));
    }

    public void setTitleIconDrawable(Drawable drawable) {
        ImageView iconImg = getStubImageView(R.id.actionbar_icon_img);
        if (iconImg != null) {
            iconImg.setImageDrawable(drawable);
            if (drawable == null) {
                iconImg.setVisibility(GONE);
            } else {
                iconImg.setVisibility(VISIBLE);
            }
        }
    }

    public void setTitle(int resId) {
        mTitle.setText(getResources().getText(resId));
    }

    public void setTitle(CharSequence text) {
        mTitle.setText(text);
    }

    private ImageView getStubImageView(int id) {
        ImageView iconImg = null;
        View view = findViewById(id);
        if (view instanceof ViewStub) {
            iconImg = (ImageView) ((ViewStub) view).inflate();
            iconImg.setId(id);
            view.setId(0);
        } else if (view instanceof ImageView) {
            iconImg = (ImageView) view;
        }

        return iconImg;
    }
}
