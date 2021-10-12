package com.rncamerakit.editor

import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import com.rncamerakit.editor.manager.CropManager

class RNPlayerKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {


    override fun getName(): String {
        return "RNPlayerKitModule"
    }


    //设置滤镜
    @ReactMethod
    fun setColorFilter(position: Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKPlayer
            view.setColorFilter(position)
        }
    }


    @ReactMethod
    fun exportVideo(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKPlayer
            view.exportVideo(promise)
        }
    }


    @ReactMethod
    fun exportImage(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKPlayer
            view.exportImage(promise)
        }
    }

    @ReactMethod
    fun corpVideoFrame(options: ReadableMap,viewTag: Int, promise: Promise) {
        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKPlayer
            CropManager.corpVideoFrame(context,options,promise)
        }
    }


    @ReactMethod
    fun release(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKPlayer
            view.onRelease()
        }
    }

}