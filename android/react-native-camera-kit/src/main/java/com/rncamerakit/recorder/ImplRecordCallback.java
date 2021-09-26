package com.rncamerakit.recorder;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.aliyun.common.utils.BitmapUtil;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideosdk.recorder.RecordCallback;

import java.io.File;
import java.io.IOException;

/**
 * 录制回调
 */
public class ImplRecordCallback implements RecordCallback {


    private OnRecorderCallbacks mCallbacks;

    private Context mContext;

    public ImplRecordCallback(Context context) {
        mContext = context;
    }

    public ImplRecordCallback(Context context, OnRecorderCallbacks callback) {
        mContext = context;
        mCallbacks = callback;
    }

    public void setOnRecorderCallbacks(OnRecorderCallbacks callback) {
        mCallbacks = callback;
    }

    @Override
    public void onComplete(boolean validClip, long clipDuration) {
        Log.e("AAA", "onComplete：" + validClip + "；clipDuration：" + clipDuration);
        if (mCallbacks != null) {
            mCallbacks.onComplete(validClip, clipDuration);
        }
    }

    @Override
    public void onFinish(String outputPath) {
        Log.e("AAA", "outputPath：" + outputPath);
        if (mCallbacks != null) {
            mCallbacks.onFinish(outputPath);
        }
    }

    @Override
    public void onProgress(long duration) {
        Log.e("AAA", "duration：" + duration);
        if (mCallbacks != null) {
            mCallbacks.onProgress(duration);
        }
    }

    @Override
    public void onMaxDuration() {

    }

    @Override
    public void onError(int errorCode) {
        Log.e("AAA", "onError：" + errorCode);
        if (mCallbacks != null) {
            mCallbacks.onError(errorCode);
        }
    }

    @Override
    public void onInitReady() {

    }

    @Override
    public void onDrawReady() {

    }

    @Override
    public void onPictureBack(Bitmap bitmap) {
        ThreadUtils.runOnSubThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                final String imgPath = Constants.SDCardConstants.getDir(mContext.getApplicationContext()) + File.separator + System.currentTimeMillis() + "-photo.jpg";
                try {
                    BitmapUtil.generateFileFromBitmap(bitmap, imgPath, "jpg");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //适配android Q
                        ThreadUtils.runOnSubThread(new Runnable() {
                            @Override
                            public void run() {
                                UriUtils.saveImgToMediaStore(mContext.getApplicationContext(), imgPath);
                            }
                        });
                    } else {
                        MediaScannerConnection.scanFile(mContext.getApplicationContext(),
                                new String[]{imgPath}, new String[]{"image/jpeg"}, null);
                    }

                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.show(mContext, "图片已保存到相册");
                            if (mCallbacks != null) {
                                mCallbacks.onTakePhoto(imgPath);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPictureDataBack(byte[] data) {

    }
}
