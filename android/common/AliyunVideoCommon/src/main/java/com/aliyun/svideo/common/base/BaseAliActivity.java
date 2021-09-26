package com.aliyun.svideo.common.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

public abstract class BaseAliActivity extends AppCompatActivity {

    @Override
    protected void onRestoreInstanceState(@NonNull @NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected final String saveInstanceKey = "onSaveInstanceStateKey";

    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(saveInstanceKey, true);
        AppStatusManager.getInstance().setAppStatus(AppStatus.STATUS_RECYCLE);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null&&savedInstanceState.getBoolean(saveInstanceKey)){
            restartApp();
            return;
        }
    }

    protected void restartApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("restartApp",true);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
