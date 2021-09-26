package com.aliyun.svideo.recorder.view.dialog;

import android.content.DialogInterface;

public class Weak {
    public static WeakOnCancelListener proxy(DialogInterface.OnCancelListener real) {
        return new WeakOnCancelListener(real);
    }

    public static WeakOnDismissListener proxy(DialogInterface.OnDismissListener real) {
        return new WeakOnDismissListener(real);
    }

    public static WeakOnShowListener proxy(DialogInterface.OnShowListener real) {
        return new WeakOnShowListener(real);
    }
}
