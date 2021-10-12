package com.rncamerakit.editor

import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import com.rncamerakit.editor.manager.CropManager

class RNEditorKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "RNEditorKitModule"
    }

    //获取滤镜列表
    @ReactMethod
    fun getColorFilterList(viewTag: Int, promise: Promise){
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.getColorFilterList(promise)
        }
    }

    //设置滤镜
    @ReactMethod
    fun setColorFilter(filterName: String?, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.setColorFilter(filterName)
        }
    }


    @ReactMethod
    fun exportVideo(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.exportVideo(promise)
        }
    }


    @ReactMethod
    fun exportImage(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.exportImage(promise)
        }
    }

    @ReactMethod
    fun release(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.onRelease()
        }
    }

}