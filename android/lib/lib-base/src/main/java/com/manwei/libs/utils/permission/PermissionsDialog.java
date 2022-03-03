package com.manwei.libs.utils.permission;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manwei.libs.dialog.DialogUtils;
import com.manwei.libs.dialog.OnDialogListener;


public class PermissionsDialog {

    public static void showDialog(AppCompatActivity context, @NonNull String permissionsName, final OnDialogListener listener) {
        DialogUtils.showDialog(context, "您尚未获取" + permissionsName + "权限，是否现在去设置?", "取消", "前往", listener);
    }


    public static void showDialogTitle(AppCompatActivity context, @NonNull String title, final OnDialogListener listener) {
        DialogUtils.showDialog(context, title, "暂不设置", "去设置", listener);
    }

}
