package com.rncamerakit.recorder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.aliyun.svideo.base.widget.beauty.BeautyConstants;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.BeautyRaceConstants;
import com.aliyun.svideo.base.widget.beauty.BeautyShapeConstants;
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams;
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface;
import com.aliyun.svideo.recorder.queen.QueenManager;
import com.aliyun.svideo.recorder.util.ActivityUtil;
import com.aliyun.svideo.recorder.util.OrientationDetector;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.aliyun.svideo.recorder.view.effects.face.BeautyService;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallBack;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallBack;
import com.aliyun.svideosdk.common.struct.recorder.CameraType;
import com.taobao.android.libqueen.Texture2D;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Queen
 */
public class RecorderQueenManage implements OnFrameCallBack, OnTextureIdCallBack {

    private final Context mContext;

    private final QueenManager mQueenManager;

    private OrientationDetector orientationDetector;
    private RecorderManage mRecorderManage;

    /**
     * 相机的原始NV21数据
     */
    private byte[] frameBytes;
    private int frameWidth, frameHeight;
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private boolean isQueenDrawed = false;
    private Texture2D texture2D;

    private List<BeautyParams> rememberRaceParamList = new ArrayList<>();

    private List<BeautyParams> rememberParamList = new ArrayList<>();

    private List<BeautyShapeParams> rememberShapeParamList = new ArrayList<>();

    public RecorderQueenManage(Context context, AlivcIMixRecorderInterface recorderInterface, RecorderManage recorderManage) {
        mContext = context;
        mRecorderManage = recorderManage;
        mQueenManager = QueenManager.getInstance(mContext);
        SharedPreferenceUtils.setIsQueenMode(mContext, true);
        initBeautyParam();
        recorderInterface.setBeautyStatus(false);
        recorderInterface.setOnFrameCallback(this);
        recorderInterface.setOnTextureIdCallback(this);

        orientationDetector = new OrientationDetector(mContext);
        orientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                recorderInterface.setRotation(getCameraRotation());
                Camera.getCameraInfo(mCameraInfo.facing, mCameraInfo);
                mQueenManager.setDeviceOrientation(0, ActivityUtil.getDegrees((Activity) mContext));
            }
        });
    }

    private int getCameraRotation() {
        int orientation = orientationDetector.getOrientation();
        int rotation = 90;
        if ((orientation >= 45) && (orientation < 135)) {
            rotation = 180;
        }
        if ((orientation >= 135) && (orientation < 225)) {
            rotation = 270;
        }
        if ((orientation >= 225) && (orientation < 315)) {
            rotation = 0;
        }
        CameraType cameraType = CameraType.FRONT;
        if (mRecorderManage.getCameraType() != null) {
            cameraType = mRecorderManage.getCameraType();
        }
        if (Camera.getNumberOfCameras() > cameraType.getType()) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraType.getType(), cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (rotation != 0) {
                    rotation = 360 - rotation;
                }
            }
        }
        return rotation;
    }

    private void initBeautyParam() {
        rememberRaceParamList.clear();
        for (int i = 0; i < BeautyRaceConstants.QUEEN_BEAUTY_MAP.size(); i++) {
            BeautyParams beautyParams = BeautyRaceConstants.QUEEN_BEAUTY_MAP.get(i);
            rememberRaceParamList.add(beautyParams);
        }

        rememberParamList.clear();
        for (int i = 0; i < BeautyConstants.BEAUTY_MAP.size(); i++) {
            BeautyParams beautyParams = BeautyConstants.BEAUTY_MAP.get(i);
            rememberParamList.add(beautyParams);
        }

        rememberShapeParamList.clear();
        for (int i = 0; i < BeautyShapeConstants.BEAUTY_MAP.size(); i++) {
            BeautyShapeParams beautyParams = BeautyShapeConstants.BEAUTY_MAP.get(i);
            rememberShapeParamList.add(beautyParams);
        }
    }

    @Override
    public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {
        frameBytes = bytes;
        frameWidth = width;
        frameHeight = height;
        mCameraInfo = info;
        mQueenManager.updateBytesBufPool(width, height, bytes);
    }

    @Override
    public Camera.Size onChoosePreviewSize(List<Camera.Size> supportedPreviewSizes, Camera.Size preferredPreviewSizeForVideo) {
        return null;
    }

    @Override
    public void openFailed() {

    }


    @Override
    public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
        isQueenDrawed = true;
        if (texture2D == null) {
            texture2D = mQueenManager.initEngine(false, textureId, textureWidth, textureHeight, true);
            if (mHandler != null) {
                mHandler.sendEmptyMessage(0);
            }
        }
        return mQueenManager.draw(frameBytes, frameWidth, frameHeight, mCameraInfo, matrix, texture2D);
    }

    @Override
    public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {
        return scaledId;
    }

    @Override
    public void onTextureDestroyed() {
        if (mQueenManager != null && isQueenDrawed) {
            mQueenManager.release();
        }
    }

    private BeautyService beautyService;

    /**
     * Queen
     * 美颜默认选中高级, 3档 美白: 0.6 红润: 0.6 磨皮: 6 大眼: 0.6 瘦脸: 0.6 * 1.5 (总范围0~1.5)
     * 使用默认参数前判断是那种美颜
     */
    private void queenDefaultParam() {
        beautyService = new BeautyService();
        beautyService.bindQueen(mContext, mQueenManager);
        initRememberParams();
    }

    private void initRememberParams() {
        //高级美颜
        int beautyFaceLevel = SharedPreferenceUtils.getBeautyFaceLevel(mContext);
        BeautyParams beautyFaceParams = rememberRaceParamList.get(beautyFaceLevel);
        beautyService.setBeautyParam(beautyFaceParams, BeautyService.BEAUTY_FACE);

        //美肌
        int beautySkinLevel = SharedPreferenceUtils.getBeautySkinLevel(mContext);
        BeautyParams beautyShinParams = rememberParamList.get(beautySkinLevel);
        beautyService.setBeautyParam(beautyShinParams, BeautyService.BEAUTY_SKIN);

        //美型
        int currentBeautyShapePosition = SharedPreferenceUtils.getBeautyShapeLevel(mContext);
        BeautyShapeParams shapeParams = rememberShapeParamList.get(currentBeautyShapePosition);
        mQueenManager.setShapeParam(shapeParams);
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {

        private WeakReference<RecorderQueenManage> weakReference;

        public MyHandler(RecorderQueenManage manage) {
            weakReference = new WeakReference<>(manage);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            RecorderQueenManage manage = weakReference.get();
            if (manage != null) {
                manage.queenDefaultParam();
            }
        }
    }


    public void setBeautyLevel(int level) {
        if (level < 0) {
            level = 0;
        }
        if (level > 5) {
            level = 5;
        }
        BeautyParams beautyFaceParams = rememberRaceParamList.get(level);
        beautyService.setBeautyParam(beautyFaceParams, BeautyService.BEAUTY_FACE);
        BeautyParams beautyShinParams = rememberParamList.get(level);
        beautyService.setBeautyParam(beautyShinParams, BeautyService.BEAUTY_SKIN);
        //美型
        int beautyShapePosition = SharedPreferenceUtils.getBeautyShapeLevel(mContext);
        if (beautyService != null) {
            beautyService.saveSelectParam(mContext, 0, level, level, beautyShapePosition);
        }
    }

    public void onRelease() {
        if (mQueenManager != null) {
            mQueenManager.release();
        }
    }

}
