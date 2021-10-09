package com.rncamerakit.recorder

import android.content.Context
import com.aliyun.common.utils.CommonUtil
import com.aliyun.svideo.base.Constants
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface
import com.aliyun.svideo.recorder.mixrecorder.AlivcRecorder
import com.aliyun.svideo.recorder.util.FixedToastUtils
import com.aliyun.svideo.recorder.util.RecordCommon
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam
import com.aliyun.svideosdk.common.struct.common.VideoQuality
import com.aliyun.svideosdk.common.struct.effect.EffectFilter
import com.aliyun.svideosdk.common.struct.effect.EffectPaster
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.common.struct.recorder.CameraParam
import com.aliyun.svideosdk.common.struct.recorder.CameraType
import com.aliyun.svideosdk.common.struct.recorder.FlashType
import com.aliyun.svideosdk.common.struct.recorder.MediaInfo
import com.aliyun.svideosdk.recorder.AliyunIClipManager
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.R
import com.rncamerakit.crop.CropManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.*

class RecorderManage(mContext: ThemedReactContext) {

    val recorder: AlivcIMixRecorderInterface?
    var cameraType: CameraType? = null

    private val mClipManager: AliyunIClipManager?
    private val mRecordCallback: ImplRecordCallback?
    private val mRecorderQueenManage: RecorderQueenManage?
    private var mDisposableObserver: DisposableObserver<List<String>>? = null
    private val mColorFilterList: MutableList<String> = ArrayList()

    /**
     * 贴纸
     */
    private var effectPaster: EffectPaster? = null

    fun initColorFilterAssets() {
        mDisposableObserver = object : DisposableObserver<List<String>>() {
            override fun onNext(list: List<String>) {
                mColorFilterList.clear()
                mColorFilterList.addAll(list)
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        }
        Observable.create<List<String>> { emitter ->
            try {
                emitter.onNext(RecordCommon.getColorFilterList())
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mDisposableObserver)
    }


    /**
     * 切换摄像头
     */
    fun setCameraType(mode: String?) {
        if (cameraType != null && recorder != null) {
            val cameraId = recorder.switchCamera()
            for (type in CameraType.values()) {
                if (type.type == cameraId) {
                    cameraType = type
                }
            }
            //切换摄像头后重新设置一次闪光灯模式，否则闪光灯无效.使用系统的拍照接口且后置摄像头
            recorder.setLight(mFlashType)
            return
        }
        cameraType = CameraType.FRONT
    }

    private var mFlashType = FlashType.AUTO

    /**
     * 设置闪光灯模式
     */
    fun setLight(mode: String?) {
        mFlashType = when (mode) {
            "on" -> {
                FlashType.ON
            }
            "off" -> {
                FlashType.OFF
            }
            else -> {
                FlashType.AUTO
            }
        }
        recorder?.setLight(mFlashType)
    }

    /**
     * 手电筒，仅后置摄像头有效
     */
    fun setTorchMode(mode: String?) {
        recorder?.setLight(if ("on" == mode) FlashType.TORCH else mFlashType)
    }


    /**
     * 带特效拍照
     */
    fun takePhoto(context :ReactApplicationContext ,promise: Promise) {
        mRecordCallback?.setOnRecorderCallbacks(object : OnRecorderCallbacks() {
            override fun onTakePhoto(photoPath: String?) {
                promise.resolve(photoPath)
            }

            override fun onError(errorCode: Int) {
                promise.reject("takePhoto", "errorCode:$errorCode")
            }
        })
        recorder?.takePhoto(true)
    }

    /**
     * 开始录制
     */
    fun startRecording(reactContext: ReactApplicationContext, promise: Promise) {
        if (CommonUtil.SDFreeSize() < 50 * 1000 * 1000) {
            promise.reject(
                "startRecording",
                "error:" + reactContext.resources.getString(R.string.alivc_music_no_free_memory)
            )
            FixedToastUtils.show(
                reactContext,
                reactContext.resources.getString(R.string.alivc_music_no_free_memory)
            )
            return
        }

        if (recorder != null) {
            mClipManager?.deleteAllPart()
            mRecordCallback?.setOnRecorderCallbacks(object : OnRecorderCallbacks() {
                override fun onProgress(duration: Long) {
                    reactContext.getJSModule(RCTDeviceEventEmitter::class.java) .emit("video-recording", "" + duration)
                }
            })
            recorder.startRecording()
            promise.resolve(true)
        } else {
            promise.reject("startRecording", "recorder is null")
        }
    }

    /**
     * 停止录制
     */
    fun stopRecording(context:Context,promise: Promise) {
        mRecordCallback?.setOnRecorderCallbacks(object : OnRecorderCallbacks() {
            override fun onComplete(validClip: Boolean, clipDuration: Long) {
                if (clipDuration < 2000) {
                    recorder?.cancelRecording()
                    promise.reject("startRecording", "recording duration" + clipDuration + "ms")
                    return
                }
                recorder?.finishRecording()
                mClipManager?.deleteAllPart()
            }

            override fun onFinish(outputPath: String?) {
                promise.resolve(outputPath)
            }

            override fun onError(errorCode: Int) {
                promise.reject("startRecording", "errorCode:$errorCode")
            }
        })
        recorder?.stopRecording()
    }

    private var colorFilterPosition = 0
    fun setColorFilter() {
        if (mColorFilterList.isEmpty() || colorFilterPosition >= mColorFilterList.size) {
            colorFilterPosition = 0
            return
        }
        val source = Source(mColorFilterList[colorFilterPosition])
        val filterEffect = EffectFilter(source)
        recorder?.applyFilter(filterEffect)
        colorFilterPosition++
        if (colorFilterPosition > mColorFilterList.size) {
            colorFilterPosition = 0
        }
    }

    fun getBeautyLevel(context: Context?): Int {
        return SharedPreferenceUtils.getBeautyFaceLevel(context)
    }

    fun setBeautyLevel(level: Int) {
        mRecorderQueenManage?.setBeautyLevel(level)
    }


    fun setGifEffect() {
        EffectManage.instance.setGifEffect(object : EffectManage.OnGifEffectPasterCallback() {
            override fun onPath(path: String) {
                super.onPath(path)
                if (effectPaster != null) {
                    recorder?.removePaster(effectPaster)
                }
                val source = Source(path)
                effectPaster = EffectPaster(source)
                recorder?.addPaster(effectPaster)
            }
        })
    }

    fun onRelease() {
        if (mDisposableObserver != null) {
            mDisposableObserver!!.dispose()
        }
        mRecorderQueenManage?.onRelease()
    }

    init {
        recorder = AlivcRecorder(mContext)
        val mWidth = ScreenUtils.getWidth(mContext)
        val mHeight = mWidth * 16 / 9
        val outputInfo = MediaInfo()
        outputInfo.fps = 35
        outputInfo.videoWidth = mWidth
        outputInfo.videoHeight = mHeight
        outputInfo.videoCodec = VideoCodecs.H264_HARDWARE
        recorder.setMediaInfo(outputInfo)
        val videoPath =
            Constants.SDCardConstants.getDir(mContext.applicationContext) + File.separator + "paiya-record.mp4"
        recorder.setOutputPath(videoPath)
        recorder.setVideoQuality(VideoQuality.SSD)
        recorder.setVideoBitrate(10 * 1000 * 1000)
        recorder.setGop(30)
        recorder.setResolutionMode(AliyunSnapVideoParam.RESOLUTION_720P)
        recorder.setCamera(CameraType.FRONT)
        recorder.setFocusMode(CameraParam.FOCUS_MODE_CONTINUE)
        mClipManager = recorder.getClipManager()
        mRecordCallback = ImplRecordCallback(mContext)
        recorder.setRecordCallback(mRecordCallback)
        mRecorderQueenManage = RecorderQueenManage(mContext, recorder, this)
    }


}
