package com.rncamerakit.recorder

import android.app.Activity
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
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
            view.mRecorderManage?.takePhoto(context, promise)
        }
    }

    @ReactMethod
    fun startRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            if (!view.isPermissions()) {
                view.getPermissions()
                return@runOnUiQueueThread
            }
            view.mRecorderManage?.startRecording(context, promise)
        }
    }

    @ReactMethod
    fun stopRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.mRecorderManage?.stopRecording(context, promise)
        }
    }

    /**
     * 获取贴纸列表
     */
    @ReactMethod
    fun getPasterInfos(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            try {
                val view = uiManager?.resolveView(viewTag) as CKCamera
                view.mRecorderManage?.getPasterInfos(promise)
            } catch (e: Exception) {
//                println("程序出现了未知异常。${e.message}")
                promise.resolve(null)
            }
        }
    }


    /**
     * 下载贴纸
     */
    @ReactMethod
    fun downloadPaster(readableMap: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera

            val previewPaster = PreviewPasterForm()

            previewPaster.icon =
                if (readableMap.hasKey("icon")) readableMap.getString("icon") else ""

            previewPaster.type =
                if (readableMap.hasKey("type")) readableMap.getInt("isLocalRes") else 0
            previewPaster.id = if (readableMap.hasKey("id")) readableMap.getInt("id") else 0
            previewPaster.sort = if (readableMap.hasKey("sort")) readableMap.getInt("sort") else 0

            previewPaster.url = if (readableMap.hasKey("url")) readableMap.getString("url") else ""
            previewPaster.md5 = if (readableMap.hasKey("md5")) readableMap.getString("md5") else ""
            previewPaster.preview =
                if (readableMap.hasKey("preview")) readableMap.getString("preview") else ""
            previewPaster.name =
                if (readableMap.hasKey("name")) readableMap.getString("name") else ""

            previewPaster.fontId =
                if (readableMap.hasKey("fontId")) readableMap.getInt("fontId") else 0
            previewPaster.level =
                if (readableMap.hasKey("level")) readableMap.getInt("level") else 0

            previewPaster.isLocalRes =
                if (readableMap.hasKey("isLocalRes")) readableMap.getBoolean("isLocalRes") else false

            previewPaster.path =
                if (readableMap.hasKey("path")) readableMap.getString("path") else ""

            view.mRecorderManage?.downloadPaster(previewPaster, promise)
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
