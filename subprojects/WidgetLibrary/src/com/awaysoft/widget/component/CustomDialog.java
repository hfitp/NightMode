
package com.awaysoft.widget.component;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.awaysoft.widget.library.R;

public class CustomDialog extends Dialog {
    private Button mOpsLeft;
    private Button mOpsRight;
    private ActionBar mActionBar;
    private LinearLayout mDecorView;
    private LinearLayout mOpsButton;
    private LinearLayout mDialogContent;
    private ViewStub mTitleStub;
    private ViewStub mOpsButtonStub;

    public CustomDialog(Context context) {
        super(context);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mDecorView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.wl_dialog_layout, null);

        mTitleStub = (ViewStub) mDecorView.findViewById(R.id.dialog_titlebar);
        mOpsButtonStub = (ViewStub) mDecorView.findViewById(R.id.dialog_opsbutton);
        mDialogContent = (LinearLayout) mDecorView.findViewById(R.id.dialog_content);
    }

    private void inflateTitleBar() {
        if (mTitleStub != null) {
            mTitleStub.inflate();
            mActionBar = (ActionBar) mDecorView.findViewById(R.id.dialg_actionbar);
            mActionBar.setBackEnable(false);
            mTitleStub = null;
        }
    }

    public void setTitle(int resId) {
        inflateTitleBar();
        mActionBar.setTitle(resId);
    }

    public void setTitle(CharSequence title) {
        inflateTitleBar();
        mActionBar.setTitle(title);
    }

    public void setTitleIconResource(int resId) {
        inflateTitleBar();
        mActionBar.setTitleIconResource(resId);
    }

    public void setTitleIconDrawable(Drawable drawable) {
        inflateTitleBar();
        mActionBar.setTitleIconDrawable(drawable);
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater.from(getContext()).inflate(layoutResID, mDialogContent);
        super.setContentView(mDecorView);
    }

    @Override
    public void setContentView(View view) {
        if (view != null) {
            ViewParent vp = view.getParent();
            if (vp instanceof ViewGroup) {
                ((ViewGroup) vp).removeView(view);
            }
            mDialogContent.addView(view);
            super.setContentView(mDecorView);
        } else {
            throw new IllegalArgumentException("@CustomDialog: content view can't be null...");
        }
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        if (view != null) {
            ViewParent vp = view.getParent();
            if (vp instanceof ViewGroup) {
                ((ViewGroup) vp).removeView(view);
            }
            mDialogContent.addView(view);
            super.setContentView(mDecorView, params);
        } else {
            throw new IllegalArgumentException("@CustomDialog: content view can't be null...");
        }
    }

    private void inflateOpsButton() {
        if (mOpsButtonStub != null) {
            mOpsButtonStub.inflate();
            mOpsButton = (LinearLayout) mDecorView.findViewById(R.id.ops_button);
            mOpsButtonStub = null;
        }
    }

    public interface OnOpsBtnClickListener {
        public void onClick(View opsBtn);
    }

    public void setLeftBtn(CharSequence label, final OnOpsBtnClickListener listener) {
        if (TextUtils.isEmpty(label)) {
            return;
        }

        inflateOpsButton();
        mOpsLeft = (Button) mOpsButton.findViewById(R.id.ops_left_btn);
        mOpsLeft.setText(label);
        mOpsLeft.setVisibility(View.VISIBLE);
        mOpsLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }

                if (isShowing()) {
                    dismiss();
                }
            }
        });

        if (mOpsRight != null) {
            mOpsButton.findViewById(R.id.ops_split).setVisibility(View.VISIBLE);
        }
    }

    public void setRightBtn(CharSequence label, final OnOpsBtnClickListener listener) {
        if (TextUtils.isEmpty(label)) {
            return;
        }

        inflateOpsButton();
        mOpsRight = (Button) mOpsButton.findViewById(R.id.ops_right_btn);
        mOpsRight.setText(label);
        mOpsRight.setVisibility(View.VISIBLE);
        mOpsRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }

                if (isShowing()) {
                    dismiss();
                }
            }
        });

        if (mOpsLeft != null) {
            mOpsButton.findViewById(R.id.ops_split).setVisibility(View.VISIBLE);
        }
    }
}
