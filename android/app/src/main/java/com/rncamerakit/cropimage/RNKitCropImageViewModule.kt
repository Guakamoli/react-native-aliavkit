package com.rncamerakit.cropimage

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class RNKitCropImageViewModule (private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
    }

    override fun getName(): String {
        return "RNKitCropImageViewModule"
    }

    @ReactMethod
    fun release(promise: Promise) {

    }
}