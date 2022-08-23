package com.rncamerakit.editor

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.base.http.EffectService
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.downloader.FileDownloaderModel
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.font.FontManager
import com.rncamerakit.recorder.CKCamera
import com.rncamerakit.utils.DownloadUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import org.jetbrains.anko.dip

@DelicateCoroutinesApi
class CKEditorManager : SimpleViewManager<CKEditor>() {

    private var mWidth = 0
    private var mHeight = 0
    private var mFilePath: String? = null
    private var isVideo = false

    override fun getName(): String {
        return "CKEditorManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): CKEditor {
        this.mWidth = 0
        this.mHeight = 0
        this.mFilePath = null
        this.isVideo = false
        val view = CKEditor(reactContext)
        RNEditorKitModule.mView = view
        return view
    }

    //设置滤镜
    @ReactProp(name = "filterName")
    fun setColorFilter(view: CKEditor, filterName: String?) {
//        if (TextUtils.isEmpty(filterName)) {
//            return
//        }
//        view.reactContext.runOnUiQueueThread {
        view.setColorFilter(filterName)
//        }
    }

    override fun onDropViewInstance(view: CKEditor) {
        super.onDropViewInstance(view)
        Log.e("AAA", "onDropViewInstance")
    }

    //设置Editor宽高
    @ReactProp(name = "editStyle")
    fun setEditorLayout(view: CKEditor, readableMap: ReadableMap?) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            this.mWidth = if (readableMap.hasKey("width")) readableMap.getInt("width") else ScreenUtils.getWidth(view.context)
            this.mHeight = if (readableMap.hasKey("height")) readableMap.getInt("height") else this.mWidth*16/9
            view.setLayout(mWidth, mHeight)
//            if (TextUtils.isEmpty(mFilePath)) {
//                return
//            }
//            view.reactContext.runOnUiQueueThread {
//                Log.e("BBB", "setEditorLayout")
//                view.setLayout(mWidth, mHeight)
//                if (!TextUtils.isEmpty(mFilePath)) {
//                    view.importVideo(mFilePath, isVideo)
//                }
//            }
        }
    }

    //文件地址
    @ReactProp(name = "videoPath")
    fun setVideoPath(view: CKEditor, videoPath: String?) {
        if (TextUtils.isEmpty(videoPath)) {
            return
        }
        if (videoPath != null && (videoPath.startsWith("content://") || videoPath.startsWith("file://"))) {
            this.mFilePath = com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(videoPath)).absolutePath
        } else {
            this.mFilePath = videoPath
        }
        this.isVideo = true
        view.importVideo(this.mFilePath, true)
    }


    //文件地址
    @ReactProp(name = "imagePath")
    fun setImagePath(view: CKEditor, imagePath: String?) {
        if (TextUtils.isEmpty(imagePath)) {
            return
        }
        if (imagePath != null && (imagePath.startsWith("content://") || imagePath.startsWith("file://"))) {
            this.mFilePath = com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(imagePath)).absolutePath
        } else {
            this.mFilePath = imagePath
        }
        this.isVideo = false
        view.importVideo(this.mFilePath, false)
    }


    //视频静音
    @ReactProp(name = "videoMute")
    fun setVideoMute(view: CKEditor, audioSilence: Boolean?) {
        view.setVideoMute(audioSilence)
    }


    //导出时是否保存到相册
    @ReactProp(name = "saveToPhotoLibrary")
    fun saveToPhotoLibrary(view: CKEditor, save: Boolean?) {
        view.isSaveToPhotoLibrary(save)
    }

    //是否开始导出，true 去导出视频
    @ReactProp(name = "startExportVideo")
    fun startExportVideo(view: CKEditor, save: Boolean?) {
        if (save == true) {
            view.exportVideo(null)
        }
    }


    //设置背景音乐
    @ReactProp(name = "musicInfo")
    fun setMusicInfo(view: CKEditor, readableMap: ReadableMap?) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            val songID = if (readableMap.hasKey("songID")) readableMap.getString("songID") else ""
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

            view.setMusicInfo(bean)
        }else{
            view.removeMusic()
        }

    }


    //设置字幕
    @ReactProp(name = "captionInfo")
    fun setCaptionInfo(view: CKEditor, readableMap: ReadableMap?) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            readableMap.let {  view.setCaptionInfo(it) }
        } else {
            //清除字幕
            view.clearCaptionInfo()
        }
    }

    //设置字幕字体
    @ReactProp(name = "captionFont")
    fun setCaptionFont(view: CKEditor, readableMap: ReadableMap?) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            val taskId = if (readableMap.hasKey("taskId")) readableMap.getInt("taskId") else 0
            val id = if (readableMap.hasKey("id")) readableMap.getInt("id") else 0
            val name = if (readableMap.hasKey("name")) readableMap.getString("name") else null
            val nameEn = if (readableMap.hasKey("nameEn")) readableMap.getString("nameEn") else null
            val url = if (readableMap.hasKey("url")) readableMap.getString("url") else null
            val path = if (readableMap.hasKey("path")) readableMap.getString("path") else null
            val isunzip = if (readableMap.hasKey("isunzip")) readableMap.getInt("isunzip") else 0

            val icon = if (readableMap.hasKey("icon")) readableMap.getString("icon") else null
            val level = if (readableMap.hasKey("level")) readableMap.getInt("level") else 0
            val sort = if (readableMap.hasKey("sort")) readableMap.getInt("sort") else 0
            val md5 = if (readableMap.hasKey("md5")) readableMap.getString("md5") else null
            val banner = if (readableMap.hasKey("banner")) readableMap.getString("banner") else null

            val model = FileDownloaderModel()

            model.taskId = taskId
            model.id = id
            model.name = name
            model.nameEn = nameEn
            model.url = url
            model.path = path
            model.setIsunzip(isunzip)

            model.effectType = EffectService.EFFECT_TEXT

            model.icon = icon
            model.level = level
            model.sort = sort
            model.md5 = md5
            model.banner = banner

            view.setCaptionFont(model)
        }
    }

}