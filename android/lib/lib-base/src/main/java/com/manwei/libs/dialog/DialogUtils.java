package com.manwei.libs.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.manwei.libs.R;


/**
 * @author : wuyq
 * Time : 2020/11/1 17:24
 * Description :
 */
public class DialogUtils {

    public static void showDialog(AppCompatActivity context, @NonNull String content, String leftStr, String rightStr, final OnDialogListener listener) {
        if (context == null || context.isFinishing()) {
            return;
        }
        final AppCompatDialog dialog = new AppCompatDialog(context, R.style.style_custom_dialog);
        View view = View.inflate(context, R.layout.layout_custom_dialog, null);
        ConstraintLayout mConstraintLayout = view.findViewById(R.id.mConstraintLayout);
        TextView tvContent = view.findViewById(R.id.tvContent);
        View vDivider = view.findViewById(R.id.v_divider);
        TextView tvLeft = view.findViewById(R.id.tvLeft);
        TextView tvRight = view.findViewById(R.id.tvRight);
        tvContent.setText(content);

        if (TextUtils.isEmpty(leftStr) && TextUtils.isEmpty(rightStr)) {
            tvLeft.setVisibility(View.GONE);
            tvRight.setVisibility(View.GONE);
            view.findViewById(R.id.viewHorizontalDivider).setVisibility(View.GONE);
            vDivider.setVisibility(View.GONE);
        } else {
            if (TextUtils.isEmpty(leftStr)) {
                tvLeft.setVisibility(View.GONE);
                vDivider.setVisibility(View.GONE);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mConstraintLayout);
                constraintSet.connect(tvRight.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.applyTo(mConstraintLayout);
            } else {
                tvLeft.setText(leftStr);
                tvLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onLeftClick();
                        }
                        if (!context.isFinishing()) {
                            dialog.cancel();
                        }
                    }
                });
            }
            if (TextUtils.isEmpty(rightStr)) {
                tvRight.setVisibility(View.GONE);
                vDivider.setVisibility(View.GONE);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mConstraintLayout);
                constraintSet.connect(tvLeft.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.applyTo(mConstraintLayout);
            } else {
                tvRight.setText(rightStr);
                tvRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onRightClick();
                        }
                        if (!context.isFinishing()) {
                            dialog.cancel();
                        }
                    }
                });
            }
        }
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null && !context.isFinishing()) {
                    listener.onCancel();
                }
            }
        });

        dialog.setContentView(view);
//        dialog.setCancelable(false);//弹出后会点击屏幕或物理返回键，dialog不消失
        dialog.setCanceledOnTouchOutside(false);//弹出后会点击屏幕，dialog不消失；点击物理返回键dialog消失
        dialog.show();
    }

}
