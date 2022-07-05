package com.rncamerakit.cropimage

import android.text.TextUtils
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class RNKitCropImageViewManager : SimpleViewManager<RNKitCropImageView>() {

    override fun getName(): String {
        return "RNKitCropImageViewManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): RNKitCropImageView {
        return RNKitCropImageView(reactContext)
    }

    override fun onDropViewInstance(view: RNKitCropImageView) {
        super.onDropViewInstance(view)
        view.onDestroy()
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        val builder: MapBuilder.Builder<String, Any> = MapBuilder.builder<String, Any>()
        for (event in RNKitCropImageView.EventEmitterKeys.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()))
        }
        return builder.build()
    }

    //文件地址
    @ReactProp(name = "imageUri")
    fun setImageUri(view: RNKitCropImageView, imageUri: String?) {
        if (!TextUtils.isEmpty(imageUri)) {
            view.setImageUri(imageUri)
        }

    }

    @ReactProp(name = "angle", defaultInt = 0)
    fun setAngle(view: RNKitCropImageView, angle: Int) {
        if (angle%90 == 0) {
            view.setAngle(angle.toFloat())
        }
    }

    @ReactProp(name = "startCrop", defaultBoolean = false)
    fun setStartCrop(view: RNKitCropImageView, isStartCrop: Boolean) {
        if (isStartCrop) {
            view.setStartCrop()
        }
    }

    @ReactProp(name = "reset", defaultBoolean = false)
    fun setReset(view: RNKitCropImageView, isReset: Boolean) {
        if (isReset) {
            view.setReset()
        }
    }

}