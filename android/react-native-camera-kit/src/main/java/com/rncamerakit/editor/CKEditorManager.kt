package com.rncamerakit.editor

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class CKEditorManager : SimpleViewManager<CKEditor>() {

    override fun getName(): String {
        return "CKEditorManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): CKEditor {
        return CKEditor(reactContext)
    }

    @ReactProp(name = "videoPath")
    fun setVideoPath(view: CKEditor, videoPath: String?) {
        view.reactContext.runOnUiQueueThread {
            view.importVideo(videoPath)
        }
    }

    @ReactProp(name = "imagePath")
    fun setImagePath(view: CKEditor, imagePath: String?) {
        view.reactContext.runOnUiQueueThread {
            view.importImage(imagePath)
        }
    }
    @ReactProp(name = "audioSilence")
    fun setAudioSilence(view: CKEditor, audioSilence: Boolean?) {
        view.reactContext.runOnUiQueueThread {
            view.setAudioSilence(audioSilence)
        }
    }

}