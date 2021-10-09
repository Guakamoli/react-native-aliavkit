package com.manwei.libs.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;

import com.manwei.libs.R;

/**
 * @author : wuyq
 * Time : 2020/12/4 17:10
 * Description :
 */
public class LoadingDialog extends AppCompatDialog {

    private final AppCompatActivity mContext;

    public LoadingDialog(AppCompatActivity context) {
        this(context, R.style.LoadingDialogStyle);
    }

    public LoadingDialog(AppCompatActivity context, int theme) {
        super(context, theme);
        this.mContext = context;
        init();
    }

    private void init() {
        // dialog弹出后会点击屏幕或物理返回键，dialog不消失
        setCancelable(false);
    }


    private CharSequence mTipWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        LoadingView loadingView = findViewById(R.id.loadingView);
        TextView tvLoadingHint = findViewById(R.id.tvLoadingHint);

        if (TextUtils.isEmpty(mTipWord)) {
            if (tvLoadingHint != null) {
                tvLoadingHint.setVisibility(View.GONE);
            }
        } else {
            if (tvLoadingHint != null) {
                tvLoadingHint.setVisibility(View.VISIBLE);
                tvLoadingHint.setText(mTipWord);
            }
        }
    }

    public void show(CharSequence tipWord) {
        if (mContext != null && mContext.isFinishing()) {
            return;
        }
        this.mTipWord = tipWord;
        super.show();
    }

}