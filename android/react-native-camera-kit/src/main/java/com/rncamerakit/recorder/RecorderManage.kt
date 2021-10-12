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
import com.aliyun.svideo.recorder.view.effects.EffectBody
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam
import com.aliyun.svideosdk.common.struct.common.VideoQuality
import com.aliyun.svideosdk.common.struct.effect.EffectFilter
import com.aliyun.svideosdk.common.struct.effect.EffectPaster
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.common.struct.recorder.CameraParam
import com.aliyun.svideosdk.common.struct.recorder.CameraType
import com.aliyun.svideosdk.common.struct.recorder.FlashType
import com.aliyun.svideosdk.common.struct.recorder.MediaInfo
import com.aliyun.svideosdk.recorder.AliyunIClipManager
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.R
import com.rncamerakit.RNEventEmitter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.*

class RecorderManage(mContext: ThemedReactContext) {

    var cameraType: CameraType? = null
    var mRecorder: AlivcIMixRecorderInterface?= null

    private var mClipManager: AliyunIClipManager? = null
    private var mRecordCallback: ImplRecordCallback? = null
    private var mRecorderQueenManage: RecorderQueenManage? = null
    private var mEffectPasterManage: EffectPasterManage? = null

    private var mDisposableObserver: DisposableObserver<List<String>>? = null
    private val mColorFilterList: MutableList<String> = ArrayList()

    /**
     * 贴纸
     */
    private var mEffectPaster: EffectPaster? = null

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
        if (cameraType != null) {
            val cameraId = mRecorder?.switchCamera()
            for (type in CameraType.values()) {
                if (type.type == cameraId) {
                    cameraType = type
                }
            }
            //切换摄像头后重新设置一次闪光灯模式，否则闪光灯无效.使用系统的拍照接口且后置摄像头
            mRecorder?.setLight(mFlashType)
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
        mRecorder?.setLight(mFlashType)
    }

    /**
     * 手电筒，仅后置摄像头有效
     */
    fun setTorchMode(mode: String?) {
        mRecorder?.setLight(if ("on" == mode) FlashType.TORCH else mFlashType)
    }


    companion object{
        var photoPath:String? = null;
    }

    /**
     * 带特效拍照
     */
    fun takePhoto(context :ReactApplicationContext ,promise: Promise) {
        mRecordCallback?.setOnRecorderCallbacks(object : OnRecorderCallbacks() {
            override fun onTakePhoto(photoPath: String?) {
                RecorderManage.photoPath = photoPath
                promise.resolve(photoPath)
            }

            override fun onError(errorCode: Int) {
                promise.reject("takePhoto", "errorCode:$errorCode")
            }
        })
        mRecorder?.takePhoto(true)
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

        if (mRecorder != null) {
            mClipManager?.deleteAllPart()
            mRecordCallback?.setOnRecorderCallbacks(object : OnRecorderCallbacks() {
                override fun onProgress(duration: Long) {
                    RNEventEmitter.startVideoRecord(reactContext,duration)
                }
            })
            mRecorder?.startRecording()
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
                    mRecorder?.cancelRecording()
                    promise.reject("startRecording", "recording duration" + clipDuration + "ms")
                    return
                }
                mRecorder?.finishRecording()
                mClipManager?.deleteAllPart()
            }

            override fun onFinish(outputPath: String?) {
                promise.resolve(outputPath)
            }

            override fun onError(errorCode: Int) {
                promise.reject("startRecording", "errorCode:$errorCode")
            }
        })
        mRecorder?.stopRecording()
    }

    private var colorFilterPosition = 0

    /**
     * 设置颜色滤镜
     */
    fun setColorFilter() {
        if (mColorFilterList.isEmpty() || colorFilterPosition >= mColorFilterList.size) {
            colorFilterPosition = 0
            return
        }
        val source = Source(mColorFilterList[colorFilterPosition])
        val filterEffect = EffectFilter(source)
        mRecorder?.applyFilter(filterEffect)
        colorFilterPosition++
        if (colorFilterPosition > mColorFilterList.size) {
            colorFilterPosition = 0
        }
    }


    fun getBeautyLevel(context: Context?): Int {
        return SharedPreferenceUtils.getBeautyFaceLevel(context)
    }

    /**
     * 设置美颜等级
     */
    fun setBeautyLevel(level: Int) {
        mRecorderQueenManage?.setBeautyLevel(level)
    }

    /**
     * 设置贴纸
     */
    fun setEffectPaster() {
        mEffectPasterManage?.setEffectPaster(object : EffectPasterManage.OnGifEffectPasterCallback() {
            override fun onPath(path: String) {
                super.onPath(path)
                if (mEffectPaster != null) {
                    mRecorder?.removePaster(mEffectPaster)
                }
                val source = Source(path)
                mEffectPaster = EffectPaster(source)
                mRecorder?.addPaster(mEffectPaster)
            }
        })
    }

    fun downloadPaster(effectBody: EffectBody<PreviewPasterForm>?,promise: Promise){
        mEffectPasterManage?.downloadPaster(effectBody,promise)
    }

    /**
     *
     */
    fun onRelease() {
        if (mDisposableObserver != null) {
            mDisposableObserver!!.dispose()
        }
        mRecorderQueenManage?.onRelease()
    }

    init {
        mRecorder = AlivcRecorder(mContext)
        val mWidth = ScreenUtils.getWidth(mContext)
        val mHeight = mWidth * 16 / 9
        val outputInfo = MediaInfo()
        outputInfo.fps = 35
        outputInfo.videoWidth = mWidth
        outputInfo.videoHeight = mHeight
        outputInfo.videoCodec = VideoCodecs.H264_HARDWARE
        mRecorder?.setMediaInfo(outputInfo)
        val videoPath =
            Constants.SDCardConstants.getDir(mContext.applicationContext) + File.separator + "paiya-record.mp4"
        mRecorder?.setOutputPath(videoPath)
        mRecorder?.setVideoQuality(VideoQuality.SSD)
        mRecorder?.setVideoBitrate(10 * 1000 * 1000)
        mRecorder?.setGop(30)
        mRecorder?.setResolutionMode(AliyunSnapVideoParam.RESOLUTION_720P)
        mRecorder?.setCamera(CameraType.FRONT)
        mRecorder?.setFocusMode(CameraParam.FOCUS_MODE_CONTINUE)
        mClipManager = mRecorder?.getClipManager()
        mRecordCallback = ImplRecordCallback(mContext)
        mRecorder?.setRecordCallback(mRecordCallback)
        mRecorderQueenManage = RecorderQueenManage(mContext, mRecorder as AlivcRecorder, this)

        mEffectPasterManage = EffectPasterManage(mContext)
        mEffectPasterManage?.initEffectPasterList()

    }


}
