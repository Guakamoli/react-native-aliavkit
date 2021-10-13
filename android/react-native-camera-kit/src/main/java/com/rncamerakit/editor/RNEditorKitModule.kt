package com.rncamerakit.editor

import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule

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
    fun videoTrim(options: ReadableMap, viewTag: Int, promise: Promise){
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.videoTrim(options,promise)
        }
    }

    @ReactMethod
    fun pause(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.pause(promise)
        }
    }

    @ReactMethod
    fun stop(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.stop(promise)
        }
    }


    @ReactMethod
    fun play(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.play(promise)
        }
    }


    @ReactMethod
    fun seek(seekTime:Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.seek(seekTime,promise)
        }
    }


    @ReactMethod
    fun videoCover(seekTime:Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.videoCover(seekTime,promise)
        }
    }



    @ReactMethod
    fun exportVideo(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.exportVideo(promise)
        }
    }

    @ReactMethod
    fun exportImage(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.exportImage(promise)
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