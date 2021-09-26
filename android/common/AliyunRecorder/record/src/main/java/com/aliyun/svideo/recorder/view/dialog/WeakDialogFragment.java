package com.aliyun.svideo.recorder.view.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

//public class WeakDialogFragment extends androidx.fragment.app.DialogFragment {
public class WeakDialogFragment extends  androidx.appcompat.app.AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new WeakDialog(requireContext(), getTheme());
    }
}