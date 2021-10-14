package com.rncamerakit.recorder

import android.app.Activity
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule

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


    //设置滤镜
    @ReactMethod
    fun setColorFilter(position: Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        val view = uiManager?.resolveView(viewTag) as CKCamera
        view.mRecorderManage?.setColorFilter()
    }

    //去拍照
    @ReactMethod
    fun capture(options: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.mRecorderManage?.takePhoto(context,promise)
        }
    }

    @ReactMethod
    fun startRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            if(!view.isPermissions()){
                view.getPermissions()
                return@runOnUiQueueThread
            }
            view.mRecorderManage?.startRecording(context,promise)
        }
    }

    @ReactMethod
    fun stopRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.mRecorderManage?.stopRecording(context,promise)
        }
    }

    @ReactMethod
    fun downloadPaster(viewTag: Int, promise: Promise){
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.mRecorderManage?.downloadPaster(null,promise)
        }
    }

    @ReactMethod
    fun release(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.onRelease()
        }
    }

}
