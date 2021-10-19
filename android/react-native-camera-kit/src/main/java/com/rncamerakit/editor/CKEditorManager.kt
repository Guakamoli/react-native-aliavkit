package com.rncamerakit.editor

import android.text.TextUtils
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

    //设置滤镜
    @ReactProp(name = "filterName")
    fun setColorFilter(view: CKEditor, filterName: String?) {
        view.reactContext.runOnUiQueueThread {
            view.setColorFilter(filterName)
        }
    }

    //文件地址
    @ReactProp(name = "videoPath")
    fun setVideoPath(view: CKEditor, videoPath: String?) {
        if(TextUtils.isEmpty(videoPath)){
            return
        }
        view.reactContext.runOnUiQueueThread {
            view.importVideo(videoPath,true)
        }
    }


    //文件地址
    @ReactProp(name = "imagePath")
    fun setImagePath(view: CKEditor, imagePath: String?) {
        if(TextUtils.isEmpty(imagePath)){
            return
        }
        view.reactContext.runOnUiQueueThread {
            view.importVideo(imagePath,false)
        }
    }

//    private fun isVideo(fileName: String?): Boolean {
//        val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
//        val contentTypeFor: String = fileNameMap.getContentTypeFor(fileName)
//        isVideo =  contentTypeFor.contains("video")
//        return isVideo
//    }

    //视频静音
    @ReactProp(name = "videoMute")
    fun setVideoMute(view: CKEditor, audioSilence: Boolean?) {
        view.reactContext.runOnUiQueueThread {
            view.setVideoMute(audioSilence)
        }
    }


    //导出时是否保存到相册
    @ReactProp(name = "saveToPhotoLibrary")
    fun saveToPhotoLibrary(view: CKEditor, save: Boolean?) {
        view.reactContext.runOnUiQueueThread {
            view.isSaveToPhotoLibrary(save)
        }
    }

    //是否开始导出，true 去导出视频
    @ReactProp(name = "startExportVideo")
    fun startExportVideo(view: CKEditor, save: Boolean?) {
        view.reactContext.runOnUiQueueThread {
            if(save == true){
                view.exportVideo(null)
            }
        }
    }



}