package com.rncamerakit.photos

import com.facebook.react.bridge.*
import com.rncamerakit.recorder.CKCamera

class RNAliKitPhotoViewModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {


    companion object {
        var mView: RNAliKitPhotoView? = null
    }

    override fun getName(): String {
        return "RNAliKitPhotoViewModule"
    }

    //取消选中
    @ReactMethod
    fun uncheckPhoto(options: ReadableMap, promise: Promise) {
        if (options.toHashMap().size > 0) {
            if (options.hasKey("index")) {
                val uncheckIndex = options.getInt("id")
            }
        }
    }

    @ReactMethod
    fun release(promise: Promise) {

    }

}


