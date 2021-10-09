package com.aliyun.svideo.editor.effects.captions;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aliyun.svideo.common.widget.BaseBottomDialog;
import com.aliyun.svideo.editor.R;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入提词器
 */
public class ImportCaptionDialog extends BaseBottomDialog {

    public ImportCaptionDialog(@NonNull @NotNull AppCompatActivity context) {
        super(context);
        setSkipCollapsed(true);
        setState(BottomSheetBehavior.STATE_EXPANDED);
        setDraggable(false);
        setCanceledOnTouchOutside(false);
    }

    private void getBlessingList() {
        String mBlessingContent = editCaption.getText().toString().trim();
        if (TextUtils.isEmpty(mBlessingContent)) {
            return;
        }
        Layout layout = editCaption.getLayout();
        String text = editCaption.getText().toString();
        int start = 0;
        int end;
        //循环遍历打印每一行
        for (int i = 0; i < editCaption.getLineCount(); i++) {
            end = layout.getLineEnd(i);
            String line = text.substring(start, end); //指定行的内容
            start = end;
            if (!TextUtils.isEmpty(line.trim())) {
                mBlessingList.add(line.trim());
            }
        }
//        String[] split = mBlessingContent.split("\n");
//        mBlessingList.clear();
//        int maxCount = 20;
//        for (String strContent : split) {
//            if (TextUtils.isEmpty(strContent) || TextUtils.isEmpty(strContent.trim())) {
//                continue;
//            }
//            while ( strContent.trim().length() > maxCount) {
//                String tmp = strContent.substring(0, maxCount);
//                if (!TextUtils.isEmpty(tmp.trim())) {
//                    mBlessingList.add(tmp.trim());
//                }
//                strContent = strContent.substring(maxCount);
//            }
//            if (!TextUtils.isEmpty(strContent.trim())) {
//                mBlessingList.add(strContent.trim());
//            }
//        }
    }


    private List<String> mBlessingList = new ArrayList<>();


    public void setBlessingContent(String blessingContent) {
        if (editCaption != null) {
            editCaption.setText(blessingContent);
        }
    }

    @Override
    protected int getContentView(Context context) {
        return R.layout.paiya_improt_caption_dialog;
    }

    private ConstraintLayout captionLayout;
    private EditText editCaption;
    private TextView tvConfirmCaption;

    @Override
    protected void initView(View view) {
        captionLayout = view.findViewById(R.id.captionLayout);
        editCaption = view.findViewById(R.id.editCaption);

        tvConfirmCaption = view.findViewById(R.id.tvConfirmCaption);

        editCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputValue = editCaption.getText().toString().trim();
                if (TextUtils.isEmpty(inputValue)) {
                    tvConfirmCaption.setTextColor(Color.parseColor("#99ffffff"));
                    tvConfirmCaption.setBackgroundResource(R.drawable.improt_caption_confirm_empty_dialog_bg);
                    tvConfirmCaption.setEnabled(false);
                } else {
                    tvConfirmCaption.setTextColor(Color.parseColor("#ffffff"));
                    tvConfirmCaption.setBackgroundResource(R.drawable.improt_caption_confirm_dialog_bg);
                    tvConfirmCaption.setEnabled(true);
                }
            }
        });

        tvConfirmCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDismissCallback != null) {
                    getBlessingList();
                    if (mBlessingList == null || mBlessingList.isEmpty()) {
                        ToastUtils.showShort("导入文案为空");
                        return;
                    }
                    mDismissCallback.onConfirm(mBlessingList);
                }
                cancel();
                KeyboardUtils.hideSoftInput(editCaption);
            }
        });
        view.findViewById(R.id.tvCleanCaption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.hideSoftInput(editCaption);
                editCaption.setFocusable(false);
                editCaption.setFocusableInTouchMode(false);

                if (mDismissCallback != null) {
                    mDismissCallback.onClean();
                }
                setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mDismissCallback != null) {
            if (editCaption != null) {
                KeyboardUtils.hideSoftInput(editCaption);
                editCaption.setFocusable(false);
                editCaption.setFocusableInTouchMode(false);
            }
            mDismissCallback.onClean();
        }
    }

    public void setStateExpanded() {
        setState(BottomSheetBehavior.STATE_EXPANDED);
        if (editCaption != null) {
            editCaption.setFocusable(true);
            editCaption.setFocusableInTouchMode(true);
        }
    }

    private OnDialogDismissCallback mDismissCallback;

    public interface OnDialogDismissCallback {
        void onClean();

        void onConfirm(List<String> mBlessingList);
    }

    public void setOnDialogDismissCallback(OnDialogDismissCallback callback) {
        mDismissCallback = callback;
    }

}
