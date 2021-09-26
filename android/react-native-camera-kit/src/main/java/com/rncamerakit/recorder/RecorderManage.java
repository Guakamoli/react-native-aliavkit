package com.rncamerakit.recorder;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface;
import com.aliyun.svideo.recorder.mixrecorder.AlivcRecorder;
import com.aliyun.svideo.recorder.util.RecordCommon;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.struct.recorder.CameraParam;
import com.aliyun.svideosdk.common.struct.recorder.CameraType;
import com.aliyun.svideosdk.common.struct.recorder.FlashType;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 *
 */
public class RecorderManage {

    private final AlivcIMixRecorderInterface mRecorder;
    private final AliyunIClipManager mClipManager;
    private final ImplRecordCallback mRecordCallback;
    private final RecorderQueenManage mRecorderQueenManage;

    private DisposableObserver mDisposableObserver;

    private final List<String> mColorFilterList = new ArrayList<>();

    public RecorderManage(Context context) {
        mRecorder = new AlivcRecorder(context);
        int mWidth = ScreenUtils.getWidth(context);
        int mHeight = mWidth * 16 / 9;
        com.aliyun.svideosdk.common.struct.recorder.MediaInfo outputInfo = new com.aliyun.svideosdk.common.struct.recorder.MediaInfo();
        outputInfo.setFps(35);
        outputInfo.setVideoWidth(mWidth);
        outputInfo.setVideoHeight(mHeight);
        outputInfo.setVideoCodec(VideoCodecs.H264_HARDWARE);
        mRecorder.setMediaInfo(outputInfo);
        String videoPath = Constants.SDCardConstants.getDir(context.getApplicationContext()) + File.separator + "paiya-record.mp4";
        mRecorder.setOutputPath(videoPath);

        mRecorder.setVideoQuality(VideoQuality.SSD);
        mRecorder.setVideoBitrate(10 * 1000 * 1000);
        mRecorder.setGop(30);
        mRecorder.setResolutionMode(AliyunSnapVideoParam.RESOLUTION_720P);
        mRecorder.setCamera(CameraType.FRONT);
        mRecorder.setFocusMode(CameraParam.FOCUS_MODE_CONTINUE);

        mClipManager = mRecorder.getClipManager();
        mClipManager.setMinDuration(10);
        mClipManager.setMaxDuration(1000 * 15);

        mRecordCallback = new ImplRecordCallback(context);
        mRecorder.setRecordCallback(mRecordCallback);
        mRecorderQueenManage = new RecorderQueenManage(context, mRecorder, this);


    }

    public void initColorFilterAssets() {
        mDisposableObserver = new DisposableObserver<List<String>>() {
            @Override
            public void onNext(@NonNull List<String> list) {
                mColorFilterList.clear();
                mColorFilterList.addAll(list);
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {

            }
        };
        Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) throws Exception {
                try {
                    emitter.onNext(RecordCommon.getColorFilterList());
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())// 子线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mDisposableObserver);
    }


    public AlivcIMixRecorderInterface getRecorder() {
        return mRecorder;
    }


    private CameraType mCameraType = null;

    public CameraType getCameraType() {
        return mCameraType;
    }

    /**
     * 切换摄像头
     */
    public void setCameraType(String mode) {
        if (mCameraType != null && mRecorder != null) {
            int cameraId = mRecorder.switchCamera();
            for (com.aliyun.svideosdk.common.struct.recorder.CameraType type : com.aliyun.svideosdk.common.struct.recorder.CameraType.values()) {
                if (type.getType() == cameraId) {
                    mCameraType = type;
                }
            }
            //切换摄像头后重新设置一次闪光灯模式，否则闪光灯无效.使用系统的拍照接口且后置摄像头
            mRecorder.setLight(mFlashType);
            return;
        }
        mCameraType = CameraType.FRONT;
    }


    private FlashType mFlashType = FlashType.AUTO;


    /**
     * 设置闪光灯模式
     */
    public void setLight(String mode) {
        if ("on".equals(mode)) {
            mFlashType = FlashType.ON;
        } else if ("off".equals(mode)) {
            mFlashType = FlashType.OFF;
        } else {
            mFlashType = FlashType.AUTO;
        }
        if (mRecorder != null) {
            mRecorder.setLight(mFlashType);
        }
    }


    /**
     * 手电筒，仅后置摄像头有效
     */
    public void setTorchMode(String mode) {
        if (mRecorder != null) {
            mRecorder.setLight("on".equals(mode) ? FlashType.TORCH : mFlashType);
        }
    }


    /**
     * 带特效拍照
     */
    public void takePhoto(Promise promise) {
        if (mRecorder != null) {
            if (mRecordCallback != null) {
                mRecordCallback.setOnRecorderCallbacks(new OnRecorderCallbacks() {
                    @Override
                    public void onTakePhoto(String photoPath) {
                        promise.resolve(photoPath);
                    }

                    @Override
                    public void onError(int errorCode) {
                        promise.reject("takePhoto", "errorCode:" + errorCode);
                    }
                });
            }
            mRecorder.takePhoto(true);
        }
    }


    /**
     * 开始录制
     */
    public void startRecording(ReactApplicationContext reactContext, Promise promise) {
        if (mRecorder != null) {
            if (mRecordCallback != null) {
                mRecordCallback.setOnRecorderCallbacks(new OnRecorderCallbacks() {
                    @Override
                    public void onProgress(long duration) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("video-recording", "" + duration);
                    }

                    @Override
                    public void onComplete(boolean validClip, long clipDuration) {
                        if (clipDuration < 2000) {
                            mRecorder.cancelRecording();
                            promise.reject("startRecording", "recording duration" + clipDuration + "ms");
                            return;
                        }
                        if (mRecorder != null) {
                            //片段合成视频
                            mRecorder.finishRecording();
                        }
                        if (mClipManager != null) {
                            mClipManager.deleteAllPart();
                        }
                    }

                    @Override
                    public void onFinish(String outputPath) {
                        promise.resolve(outputPath);
                    }

                    @Override
                    public void onError(int errorCode) {
                        promise.reject("startRecording", "errorCode:" + errorCode);
                    }
                });
            }
            if (mClipManager != null) {
                mClipManager.deleteAllPart();
            }
            mRecorder.startRecording();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording(Promise promise) {
        if (mRecorder != null) {
            if (mRecordCallback != null) {
                mRecordCallback.setOnRecorderCallbacks(new OnRecorderCallbacks() {
                    @Override
                    public void onComplete(boolean validClip, long clipDuration) {
                        if (clipDuration < 2000) {
                            mRecorder.cancelRecording();
                            promise.reject("startRecording", "recording duration" + clipDuration + "ms");
                            return;
                        }
                        if (mRecorder != null) {
                            mRecorder.finishRecording();
                        }
                        if (mClipManager != null) {
                            mClipManager.deleteAllPart();
                        }
                    }

                    @Override
                    public void onFinish(String outputPath) {
                        promise.resolve(outputPath);
                    }

                    @Override
                    public void onError(int errorCode) {
                        promise.reject("startRecording", "errorCode:" + errorCode);
                    }
                });
            }
            mRecorder.stopRecording();
        }
    }


    private int colorFilterPosition = 0;

    public void setColorFilter(int position) {
        EffectFilter filterEffect = new EffectFilter(mColorFilterList.get(colorFilterPosition));
        mRecorder.applyFilter(filterEffect);
        colorFilterPosition++;
        if (colorFilterPosition > mColorFilterList.size()) {
            colorFilterPosition = 0;
        }
    }


    public int getBeautyLevel(Context context) {
        return SharedPreferenceUtils.getBeautyFaceLevel(context);
    }

    public void setBeautyLevel(int level) {
        if (mRecorderQueenManage != null) {
            mRecorderQueenManage.setBeautyLevel(level);
        }
    }


    public void onRelease() {
        if (mDisposableObserver != null) {
            mDisposableObserver.dispose();
        }
        if (mRecorderQueenManage != null) {
            mRecorderQueenManage.onRelease();
        }
    }

}
