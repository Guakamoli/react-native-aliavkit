package com.aliyun.svideo.recorder.view.dialog;

import android.content.DialogInterface;

import java.lang.ref.WeakReference;

public class WeakOnCancelListener implements DialogInterface.OnCancelListener {
    private WeakReference<DialogInterface.OnCancelListener> mRef;

    public WeakOnCancelListener(DialogInterface.OnCancelListener real) {
        this.mRef = new WeakReference<>(real);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        DialogInterface.OnCancelListener real = mRef.get();
        if (real != null) {
            real.onCancel(dialog);
        }
    }
}