package com.rncamerakit.crop

import com.facebook.react.bridge.*

class RNCropKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "RNCropKitModule"
    }

    @ReactMethod
    fun corpVideoFrame(options: ReadableMap, promise: Promise) {
        val context = reactContext
        context.runOnUiQueueThread {
            CropManager.corpVideoFrame(context.applicationContext, options, promise)
        }
    }

    @ReactMethod
    fun cropImager(options: ReadableMap, promise: Promise) {
        val context = reactContext
        context.runOnUiQueueThread {
            CropManager.cropImager(context, options, promise)
        }
    }

    @ReactMethod
    fun cropVideo(options: ReadableMap, promise: Promise) {
        val context = reactContext
        context.runOnUiQueueThread {
            CropManager.cropVideo(context, options, promise)
        }
    }

}