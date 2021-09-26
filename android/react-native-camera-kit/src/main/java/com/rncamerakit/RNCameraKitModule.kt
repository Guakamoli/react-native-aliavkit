package com.rncamerakit

import android.app.Activity
import com.aliyun.svideo.recorder.activity.AlivcSvideoRecordActivity
import com.aliyun.svideo.recorder.bean.AlivcRecordInputParam
import com.aliyun.svideo.recorder.bean.RenderingMode
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam
import com.aliyun.svideosdk.common.struct.common.VideoQuality
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import java.lang.ref.WeakReference

class RNCameraKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val PORTRAIT = 0 // ⬆️
        const val LANDSCAPE_LEFT = 1 // ⬅️
        const val PORTRAIT_UPSIDE_DOWN = 2 // ⬇️
        const val LANDSCAPE_RIGHT = 3 // ➡️
    }

    override fun getName(): String {
        return "RNCameraKitModule"
    }

    override fun getConstants(): Map<String, Any> {
        return hashMapOf(
            "PORTRAIT" to PORTRAIT,
            "PORTRAIT_UPSIDE_DOWN" to PORTRAIT_UPSIDE_DOWN,
            "LANDSCAPE_LEFT" to LANDSCAPE_LEFT,
            "LANDSCAPE_RIGHT" to LANDSCAPE_RIGHT
        )
    }

    private var mActivity: Activity? = null

    //获取美颜等级
    @ReactMethod
    fun getBeautyLevel(options: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        val view = uiManager?.resolveView(viewTag) as CKCamera
        promise.resolve(view.recorderManage.getBeautyLevel(context))
    }

    //设置美颜等级
    @ReactMethod
    fun setBeautyLevel(beautyLevel: Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        val view = uiManager?.resolveView(viewTag) as CKCamera
        view.recorderManage.setBeautyLevel(beautyLevel)
    }

    //设置滤镜
    @ReactMethod
    fun setColorFilter(beautyLevel: Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        val view = uiManager?.resolveView(viewTag) as CKCamera
        view.recorderManage.setColorFilter(beautyLevel)
    }

    //去拍照
    @ReactMethod
    fun capture(options: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.recorderManage.takePhoto(promise)
        }
    }

    @ReactMethod
    fun startRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            if(!view.isPermissions){
                view.getPermissions()
                return@runOnUiQueueThread
            }
            view.recorderManage.startRecording(context,promise)
        }
    }

    @ReactMethod
    fun stopRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.recorderManage.stopRecording(promise)
        }
    }

}