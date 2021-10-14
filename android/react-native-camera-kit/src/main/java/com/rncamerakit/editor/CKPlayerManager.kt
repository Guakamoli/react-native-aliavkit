package com.rncamerakit.editor

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.rncamerakit.recorder.manager.RecorderManage

class CKPlayerManager : SimpleViewManager<CKPlayer>() {

    override fun getName(): String {
        return "CKPlayerManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): CKPlayer {
        return CKPlayer(reactContext)
    }

    @ReactProp(name = "videoPath")
    fun setVideoPath(view: CKPlayer, videoPath: String?) {
        view.reactContext.runOnUiQueueThread {
            view.importVideo(videoPath)
        }
    }

    @ReactProp(name = "imagePath")
    fun setImagePath(view: CKPlayer, imagePath: String?) {
        view.reactContext.runOnUiQueueThread {
            view.importImage(RecorderManage.photoPath)
        }
    }

}