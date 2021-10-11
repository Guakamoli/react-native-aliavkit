package com.rncamerakit

import com.aliyun.svideo.base.Constants
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import java.io.File

class CKPlayerManager : SimpleViewManager<CKPlayer>() {

    override fun getName(): String {
        return "CKPlayerManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): CKPlayer {
        return CKPlayer(reactContext)
    }

    @ReactProp(name = "videoPath")
    fun setVideoPath(view: CKPlayer, videoPath: String?) {
        view.importVideo(videoPath)
    }

    @ReactProp(name = "imagePath")
    fun setImagePath(view: CKPlayer, imagePath: String?) {
        view.importImage(imagePath)
    }
}