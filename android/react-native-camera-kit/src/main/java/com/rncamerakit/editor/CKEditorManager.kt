package com.rncamerakit.editor

import android.text.TextUtils
import com.aliyun.svideo.common.utils.FileUtils
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.recorder.CKCamera
import com.rncamerakit.utils.DownloadUtils

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
        if(TextUtils.isEmpty(filterName)){
            return
        }
        view.reactContext.runOnUiQueueThread {
            view.setColorFilter(filterName)
        }
    }

    //文件地址
    @ReactProp(name = "videoPath")
    fun setVideoPath(view: CKEditor, videoPath: String?) {
        if (TextUtils.isEmpty(videoPath)) {
            return
        }
        view.reactContext.runOnUiQueueThread {
            view.importVideo(videoPath, true)
        }
    }


    //文件地址
    @ReactProp(name = "imagePath")
    fun setImagePath(view: CKEditor, imagePath: String?) {
        if (TextUtils.isEmpty(imagePath)) {
            return
        }
        view.reactContext.runOnUiQueueThread {
            view.importVideo(imagePath, false)
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
            if (save == true) {
                view.exportVideo(null)
            }
        }
    }

//    //设置背景音乐
//    @ReactProp(name = "backgroundMusic")
//    fun setBackgroundMusic(view: CKEditor, bgm: String?) {
//        view.reactContext.runOnUiQueueThread {
//            view.setBackgroundMusic(bgm)
//        }
//    }

    //设置背景音乐
    @ReactProp(name = "musicInfo")
    fun setMusicInfo(view: CKEditor, readableMap: ReadableMap?) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            val songID = if (readableMap.hasKey("songID")) readableMap.getInt("songID") else 0
            val name = if (readableMap.hasKey("name")) readableMap.getString("name") else ""
            val artist = if (readableMap.hasKey("artist")) readableMap.getString("artist") else ""
            val isDbContain =
                if (readableMap.hasKey("isDbContain")) readableMap.getInt("isDbContain") else 0
            val duration = if (readableMap.hasKey("duration")) readableMap.getInt("duration") else 0
            val localPath =
                if (readableMap.hasKey("localPath")) readableMap.getString("localPath") else ""
            val cover = if (readableMap.hasKey("cover")) readableMap.getString("cover") else ""
            val url = if (readableMap.hasKey("url")) readableMap.getString("url") else ""

            val bean = MusicFileBean()
            bean.songID = songID
            bean.name = name
            bean.artist = artist
            bean.isDbContain = isDbContain
            bean.duration = duration
            bean.localPath = localPath
            bean.cover = cover
            bean.url = url

            view.reactContext.runOnUiQueueThread {
                view.setMusicInfo(bean)
            }
        }

    }

}