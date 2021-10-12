package com.rncamerakit.recorder.manager

import android.hardware.Camera
import com.aliyun.svideo.base.widget.beauty.BeautyConstants
import com.aliyun.svideo.base.widget.beauty.BeautyParams
import com.aliyun.svideo.base.widget.beauty.BeautyRaceConstants
import com.aliyun.svideo.base.widget.beauty.BeautyShapeConstants
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface
import com.aliyun.svideo.recorder.queen.QueenManager
import com.aliyun.svideo.recorder.util.ActivityUtil
import com.aliyun.svideo.recorder.util.OrientationDetector
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils
import com.aliyun.svideo.recorder.view.effects.face.BeautyService
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallBack
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallBack
import com.aliyun.svideosdk.common.struct.recorder.CameraType
import com.facebook.react.uimanager.ThemedReactContext
import com.taobao.android.libqueen.Texture2D
import java.util.*

class RecorderQueenManage(
    private val mContext: ThemedReactContext,
    recorderInterface: AlivcIMixRecorderInterface,
    private val mRecorderManage: RecorderManage
) :
    OnFrameCallBack, OnTextureIdCallBack {
    private val mQueenManager: QueenManager? = QueenManager.getInstance(mContext)
    private val orientationDetector: OrientationDetector

    /**
     * 相机的原始NV21数据
     */
    private var frameBytes: ByteArray? = null
    private var frameWidth = 0
    private var frameHeight = 0
    private var mCameraInfo = Camera.CameraInfo()
    private var isQueenDrawed = false
    private var texture2D: Texture2D? = null
    private val rememberRaceParamList: MutableList<BeautyParams?> = ArrayList()
    private val rememberParamList: MutableList<BeautyParams?> = ArrayList()
    private val rememberShapeParamList: MutableList<BeautyShapeParams?> = ArrayList()


    private fun initBeautyParam() {
        rememberRaceParamList.clear()
        BeautyRaceConstants.QUEEN_BEAUTY_MAP.forEach {
            rememberRaceParamList.add(it.value)
        }
        rememberParamList.clear()
        BeautyConstants.BEAUTY_MAP.forEach {
            rememberParamList.add(it.value)
        }
        rememberShapeParamList.clear()
        BeautyShapeConstants.BEAUTY_MAP.forEach {
            rememberShapeParamList.add(it.value)
        }
    }

    override fun onFrameBack(bytes: ByteArray, width: Int, height: Int, info: Camera.CameraInfo) {
        frameBytes = bytes
        frameWidth = width
        frameHeight = height
        mCameraInfo = info
        mQueenManager!!.updateBytesBufPool(width, height, bytes)
    }

    override fun onChoosePreviewSize(
        supportedPreviewSizes: List<Camera.Size>,
        preferredPreviewSizeForVideo: Camera.Size
    ): Camera.Size? {
        return null
    }

    override fun openFailed() {}

    override fun onTextureIdBack(
        textureId: Int,
        textureWidth: Int,
        textureHeight: Int,
        matrix: FloatArray?
    ): Int {
        isQueenDrawed = true
        if (texture2D == null) {
            texture2D =
                mQueenManager!!.initEngine(false, textureId, textureWidth, textureHeight, true)
        }
        return mQueenManager!!.draw(
            frameBytes,
            frameWidth,
            frameHeight,
            mCameraInfo,
            matrix,
            texture2D
        )
    }

    override fun onScaledIdBack(
        scaledId: Int,
        textureWidth: Int,
        textureHeight: Int,
        matrix: FloatArray?
    ): Int {
        return scaledId
    }

    override fun onTextureDestroyed() {
        if (isQueenDrawed) {
            mQueenManager?.release()
        }
    }

    private var beautyService: BeautyService? = null

    private fun queenDefaultParam() {
        beautyService = BeautyService()
        beautyService!!.bindQueen(mContext, mQueenManager)
        initRememberParams()
    }

    private fun initRememberParams() {
        //高级美颜
        val beautyFaceLevel = SharedPreferenceUtils.getBeautyFaceLevel(mContext)
        val beautyFaceParams = rememberRaceParamList[beautyFaceLevel]
        beautyService!!.setBeautyParam(beautyFaceParams, BeautyService.BEAUTY_FACE)

        //美肌
        val beautySkinLevel = SharedPreferenceUtils.getBeautySkinLevel(mContext)
        val beautyShinParams = rememberParamList[beautySkinLevel]
        beautyService!!.setBeautyParam(beautyShinParams, BeautyService.BEAUTY_SKIN)

        //美型
        val currentBeautyShapePosition = SharedPreferenceUtils.getBeautyShapeLevel(mContext)
        val shapeParams = rememberShapeParamList[currentBeautyShapePosition]
        mQueenManager!!.setShapeParam(shapeParams)
    }

    fun setBeautyLevel(beautyLevel: Int) {
        var level = beautyLevel
        if (beautyLevel < 0) {
            level = 0
        }
        if (beautyLevel > 5) {
            level = 5
        }
        val beautyFaceParams = rememberRaceParamList[level]
        beautyService!!.setBeautyParam(beautyFaceParams, BeautyService.BEAUTY_FACE)
        val beautyShinParams = rememberParamList[level]
        beautyService!!.setBeautyParam(beautyShinParams, BeautyService.BEAUTY_SKIN)
        //美型
        val beautyShapePosition = SharedPreferenceUtils.getBeautyShapeLevel(mContext)
        if (beautyService != null) {
            beautyService!!.saveSelectParam(mContext, 0, level, level, beautyShapePosition)
        }
    }

    private fun getCameraRotation(): Int {
        val orientation = orientationDetector.orientation
        var rotation = 90
        if (orientation in 45..134) {
            rotation = 180
        }
        if (orientation in 135..224) {
            rotation = 270
        }
        if (orientation in 225..314) {
            rotation = 0
        }
        var cameraType = CameraType.FRONT
        if (mRecorderManage.cameraType != null) {
            cameraType = mRecorderManage.cameraType!!
        }
        if (Camera.getNumberOfCameras() > cameraType.type) {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(cameraType.type, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (rotation != 0) {
                    rotation = 360 - rotation
                }
            }
        }
        return rotation
    }


    fun onRelease() {
        mQueenManager?.release()
        orientationDetector.disable()
    }

    init {
        SharedPreferenceUtils.setIsQueenMode(mContext, true)
        initBeautyParam()
        queenDefaultParam()
        recorderInterface.setBeautyStatus(false)
        recorderInterface.setOnFrameCallback(this)
        recorderInterface.setOnTextureIdCallback(this)
        orientationDetector = OrientationDetector(mContext)
        orientationDetector.setOrientationChangedListener {
            val rotation = getCameraRotation();
            recorderInterface.setRotation(rotation)
            Camera.getCameraInfo(mCameraInfo.facing, mCameraInfo)
            mQueenManager?.setDeviceOrientation(0, ActivityUtil.getDegrees(mContext.currentActivity))
        }
        orientationDetector.enable()
    }

}
