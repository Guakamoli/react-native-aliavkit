package com.rncamerakit.photos

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.rncamerakit.recorder.CKCamera

class RNAliKitPhotoViewModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {


    companion object {
        var mView: RNAliKitPhotoView? = null
    }

    override fun getName(): String {
        return "RNAliKitPhotoViewModule"
    }

    @ReactMethod
    fun release(promise: Promise) {

    }

}


