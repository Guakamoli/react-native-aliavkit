package com.rncamerakit.cropimage

import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter

class RNKitCropImageView(val reactContext: ThemedReactContext) : FrameLayout(reactContext.applicationContext) {

    private var mEventEmitter: RCTEventEmitter? = null

    init {
        mEventEmitter = reactContext.getJSModule(RCTEventEmitter::class.java)
    }

    enum class EventEmitterKeys(private val mName: String) {
        EVENT_EMITTER_CROPPED("onCropped");
        override fun toString(): String {
            return mName
        }
    }

    /**
     * 裁剪图片地址
     */
    fun setImageUri(imageUri: String) {

    }

    /**
     *  设置图片旋转角度：angle：0；90；180；270
     */
    fun setAngle(angle: Int) {

    }

    /**
     * 开始裁剪
     */
    fun setStartCrop() {

    }

    /**
     * 裁剪完成,回调到RN
     */
    fun onCropped(imagePath: String) {
        val map: WritableMap = Arguments.createMap()
        map.putString("path", imagePath)
        mEventEmitter?.receiveEvent(id, EventEmitterKeys.EVENT_EMITTER_CROPPED.toString(), map)
    }

    /**
     * 组件销毁，资源回收
     */
    fun onDestroy() {

    }

}