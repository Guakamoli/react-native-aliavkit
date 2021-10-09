package com.aliyun.svideo.recorder.queen;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;
import android.view.OrientationEventListener;

import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.BeautyRaceConstants;
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.Texture2D;
import com.taobao.android.libqueen.exception.InitializationException;
import com.taobao.android.libqueen.models.Flip;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Queen 管理类
 */

public class QueenManager {
    private static final String TAG = "QueenManager";
    private static QueenManager mQueenManager = null;
    private static Context mContext;
    private QueenEngine mQueenEngine;
    private SimpleBytesBufPool mBytesBufPool;
    private int mDeviceOrientation;
    private int mDisplayOrientation;
    private final Queue<Integer> mQueenParamUpdateQueue = new LinkedList<Integer>();

    private QueenManager() {
    }

    public static QueenManager getInstance(Context context) {
        mContext = context.getApplicationContext();
        if (mQueenManager != null) {
            return mQueenManager;
        }

        synchronized (QueenManager.class) {
            mQueenManager = new QueenManager();
            return mQueenManager;
        }
    }

    public void addTask() {
        mQueenParamUpdateQueue.offer(1);
    }

    /**
     * 更新数据
     */
    public void updateParamToEngine() {
        Integer poll = mQueenParamUpdateQueue.poll();
        if (poll != null) {
            writeParamToEngine();
        }
    }

    /**
     * SDK初始化,
     */
    public Texture2D initEngine(boolean toScreen, int textureId, int textureWidth, int textureHeight, boolean isEOS) {
        Texture2D texture2D = null;
        try {
            if (mQueenEngine == null) {
                mQueenEngine = new QueenEngine(mContext, toScreen);
                mQueenEngine.setInputTexture(textureId, textureHeight, textureWidth, isEOS);
                texture2D = updateOutTexture(textureHeight, textureWidth);

                mQueenEngine.setScreenViewport(0, 0, textureHeight, textureWidth);

                //获取之前选择的美颜点击

//                //普通美颜等级
//                int currentNormalFacePosition = SharedPreferenceUtils.getBeautyNormalFaceLevel(mContext);
                //高级美颜等级
                int currentBeautyFacePosition = SharedPreferenceUtils.getBeautyFaceLevel(mContext);

//                //美型等级
//                int currentBeautyShapePosition = SharedPreferenceUtils.getBeautyShapeLevel(mContext);
//                //美肌等级
//                int currentBeautySkinPosition = SharedPreferenceUtils.getBeautySkinLevel(mContext);

//                Log.e("aaa", "currentNormalFacePosition：" + currentNormalFacePosition);
//                Log.e("aaa", "currentBeautyFacePosition：" + currentBeautyFacePosition);
//                Log.e("aaa", "currentBeautyShapePosition：" + currentBeautyShapePosition);
//                Log.e("aaa", "currentBeautySkinPosition：" + currentBeautySkinPosition);

                BeautyParams beautyParams = BeautyRaceConstants.QUEEN_BEAUTY_MAP.get(currentBeautyFacePosition);
                QueenParam.setBeautyParam(beautyParams);
                QueenParam.setBeautySkinParam(beautyParams);

                writeParamToEngine();
            }
        } catch (InitializationException e) {
            e.printStackTrace();
        }

        return texture2D;
    }

    public Texture2D updateOutTexture(int textureWidth, int textureHeight) {
        // 非必要步骤：获得美颜输出纹理，可以在用于其他扩展业务
        Texture2D outTexture = mQueenEngine.autoGenOutTexture(true);
        mQueenEngine.updateOutTexture(outTexture.getTextureId(), textureWidth, textureHeight, true);
        return outTexture;
    }

    public void writeParamToEngine() {
        QueenParamHolder.writeParamToEngine(mQueenEngine);
    }

    public void updateBytesBufPool(int width, int height, byte[] bytes) {
        if (mBytesBufPool == null) {
            int byteSize = width * height * android.graphics.ImageFormat.getBitsPerPixel(android.graphics.ImageFormat.NV21) / 8;
            mBytesBufPool = new SimpleBytesBufPool(3, byteSize);
        }
        updateBytesBufPool(bytes);
    }

    private void updateBytesBufPool(byte[] bytes) {
        mBytesBufPool.updateBuffer(bytes);
        mBytesBufPool.reusedBuffer();
    }

    public int draw(byte[] frameBytes, int frameWidth, int frameHeight, Camera.CameraInfo mCameraInfo, float[] matrix, Texture2D texture2D) {
        byte[] lastBuffer = null;
        if (mBytesBufPool != null) {
            updateBytesBufPool(frameBytes);
            lastBuffer = mBytesBufPool.getLastBuffer();
        }
        if (lastBuffer != null) {
//            mQueenEngine.updateInputDataAndRunAlg(frameBytes, ImageFormat.NV21, frameWidth, frameHeight, 0,
//                    getInputAngle(mCameraInfo), getOutputAngle(mCameraInfo),
//                    getFlipAxis(mCameraInfo));
            mQueenEngine.updateInputTextureBufferAndRunAlg(getInputAngle(mCameraInfo), getOutputAngle(mCameraInfo),
                    getFlipAxis(mCameraInfo), false);
            mBytesBufPool.releaseBuffer(lastBuffer);
        }
        updateParamToEngine();
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mQueenEngine.renderTexture(matrix);

        return texture2D.getTextureId();
    }

    /**
     * 美型参数
     *
     * @param shapeParam 美型参数
     */
    public void setShapeParam(BeautyShapeParams shapeParam) {
        Log.d(TAG, "setShapeParam: " + shapeParam.toString());
        //颧骨
        QueenParamHolder.getQueenParam().faceShapeRecord.cutCheekParam = shapeParam.beautyCutCheek / 100 ;
        //削脸
        QueenParamHolder.getQueenParam().faceShapeRecord.cutFaceParam = shapeParam.beautyCutFace / 100 ;
        //瘦脸
        QueenParamHolder.getQueenParam().faceShapeRecord.thinFaceParam = shapeParam.beautyThinFace / 100 * 1.5f ;
        //脸长
        QueenParamHolder.getQueenParam().faceShapeRecord.longFaceParam = shapeParam.beautyLongFace / 100 ;
        //下巴缩短
        QueenParamHolder.getQueenParam().faceShapeRecord.lowerJawParam = shapeParam.beautyLowerJaw / 100 * -1f ;
        //大眼
        QueenParamHolder.getQueenParam().faceShapeRecord.bigEyeParam = shapeParam.beautyBigEye / 100 ;
        //瘦鼻
        QueenParamHolder.getQueenParam().faceShapeRecord.thinNoseParam = shapeParam.beautyThinNose / 100 ;
        //鼻翼
        QueenParamHolder.getQueenParam().faceShapeRecord.nosewingParam = shapeParam.beautyNoseWing / 100 ;
        //唇宽
        QueenParamHolder.getQueenParam().faceShapeRecord.mouthWidthParam = shapeParam.beautyMouthWidth / 100 * -1f ;
        //下颌
        QueenParamHolder.getQueenParam().faceShapeRecord.thinMandibleParam = shapeParam.beautyThinMandible / 100 ;

        addTask();
    }

    /**
     * 设置滤镜资源路径
     *
     * @param path 基于assets的相对路径，如“/lookups/lookup_1.png”
     */
    public QueenManager setFilter(String path) {
        if (mQueenEngine != null) {
            mQueenEngine.setFilter(path);
        }
        return this;
    }

    public int getFlipAxis(Camera.CameraInfo cameraInfo) {
        int mFlipAxis;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mFlipAxis = Flip.kNone;
        } else {
            mFlipAxis = Flip.kFlipY;
        }
        return mFlipAxis;
    }

    public int getOutputAngle(Camera.CameraInfo cameraInfo) {
        boolean isFont = cameraInfo.facing != Camera.CameraInfo.CAMERA_FACING_BACK;
        int angle = isFont ? (360 - mDeviceOrientation) % 360 : mDeviceOrientation % 360;
        return (angle - mDisplayOrientation + 360) % 360;
    }


    public int getInputAngle(Camera.CameraInfo cameraInfo) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 + cameraInfo.orientation - mDeviceOrientation) % 360;
        } else {
            return (cameraInfo.orientation + mDeviceOrientation) % 360;
        }
    }


    public void setDeviceOrientation(int deviceOrientation, int displayOrientation) {
        if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        this.mDeviceOrientation = deviceOrientation;
        this.mDisplayOrientation = displayOrientation;
    }

    public void release() {
        mQueenManager = null;
        if (mQueenEngine != null) {
            mQueenEngine.release();
            mQueenEngine = null;
        }
    }
}
