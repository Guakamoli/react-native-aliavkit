package com.rncamerakit.editor

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.recorder.util.RecordCommon
import com.blankj.utilcode.util.SPUtils
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import com.google.gson.GsonBuilder
import com.liulishuo.filedownloader.BaseDownloadTask
import com.rncamerakit.crop.CropManager
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.db.MusicFileInfoDao
import com.rncamerakit.editor.manager.ColorFilterManager
import com.rncamerakit.recorder.CKCamera
import com.rncamerakit.recorder.manager.MediaPlayerManage
import com.rncamerakit.utils.AliFileUtils
import com.rncamerakit.utils.DownloadUtils
import com.rncamerakit.utils.MyFileDownloadCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.FileNameMap
import java.net.URLConnection
import java.util.ArrayList

@DelicateCoroutinesApi
class RNEditorKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {


    companion object {
        @SuppressLint("StaticFieldLeak")
        var mView: CKEditor? = null
    }

    override fun getName(): String {
        return "RNEditorKitModule"
    }

//    /**
//     * 获取音乐地址，本地存在返回本地地址；本地不存在，先下载后返回下载的地址
//     */
//    @ReactMethod
//    fun getMusicPath(songID: Int, promise: Promise) {
//        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID)
//        if (musicInfo?.isDbContain == 1 && FileUtils.fileIsExists((musicInfo.localPath))) {
//            promise.resolve(musicInfo.localPath)
//            return
//        }
//            DownloadUtils.downloadMusic(reactContext, songID, musicInfo?.url, promise, null)
//    }

    @ReactMethod
    fun getMusicList(name: String, songID: String, page: Int, pageSize: Int, promise: Promise) {
        if (!TextUtils.isEmpty(songID)) {
            val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID)
            if (musicInfo != null) {
                val list: MutableList<MusicFileBean> = ArrayList()
                list.add(musicInfo)
                promise.resolve(GsonBuilder().create().toJson(list))
                return
            }
        }

        val md5Value = SPUtils.getInstance().getString(DownloadUtils.spKey)
        if (TextUtils.isEmpty(md5Value)) {
            DownloadUtils.getMusicJsonInfo {
                val list = MusicFileInfoDao.instance.queryList(name, page, pageSize)
                promise.resolve(GsonBuilder().create().toJson(list))
            }
        } else {
            val list = MusicFileInfoDao.instance.queryList(name, page, pageSize)
            promise.resolve(GsonBuilder().create().toJson(list))
        }
    }

    @ReactMethod
    fun playMusic(songID: String?, promise: Promise) {
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID)
        if (musicInfo?.isDbContain == 1 && FileUtils.fileIsExists((musicInfo.localPath))) {
            musicInfo.localPath?.let {
                MediaPlayerManage.instance.start(it, null)
            }
            promise.resolve(GsonBuilder().create().toJson(musicInfo))
        }
        if (musicInfo != null) {
            DownloadUtils.downloadMusic(reactContext, musicInfo.songID, musicInfo.url, null,
                object : MyFileDownloadCallback() {
                    override fun completed(task: BaseDownloadTask) {
                        super.completed(task)
                        val musicBean: MusicFileBean? = MusicFileInfoDao.instance.query(musicInfo.songID)
                        MediaPlayerManage.instance.start(task.targetFilePath, null)
                        promise.resolve(GsonBuilder().create().toJson(musicBean))
                    }
                }
            )
        }
    }

    @ReactMethod
    fun stopMusic(songID: String, promise: Promise) {
        MediaPlayerManage.instance.stop()
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID)
        promise.resolve(GsonBuilder().create().toJson(musicInfo))
    }

    //获取滤镜列表
    @ReactMethod
    fun getColorFilterList(promise: Promise) {
        val context = reactContext
        ColorFilterManager.getColorFilter(context.applicationContext, promise)
    }


    /**
     * 视频裁剪，开始结束时间，软裁剪
     */
    @ReactMethod
    fun trimVideo(options: ReadableMap, viewTag: Int, promise: Promise) {
        mView?.trimVideo(options, promise)
//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.trimVideo(options, promise)
//        }
    }

    @ReactMethod
    fun pause(viewTag: Int, promise: Promise) {
        mView?.pause(promise)
//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.pause(promise)
//        }
    }

    @ReactMethod
    fun stop(viewTag: Int, promise: Promise) {
        mView?.stop(promise)
//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.stop(promise)
//        }
    }


    @ReactMethod
    fun play(viewTag: Int, promise: Promise) {
        mView?.play(promise)
//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.play(promise)
//        }
    }


    @ReactMethod
    fun seek(seekTime: Int, viewTag: Int, promise: Promise) {
        mView?.seek(seekTime, promise)
//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.seek(seekTime, promise)
//        }
    }


    //获取视频封面
    @ReactMethod
    fun videoCover(seekTime: Int, viewTag: Int, promise: Promise) {
        mView?.videoCover(seekTime, promise)
//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.videoCover(seekTime, promise)
//        }
    }


    @ReactMethod
    fun exportVideo(viewTag: Int, promise: Promise) {
        mView?.exportVideo(promise)

//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKEditor
//            view.exportVideo(promise)
//        }
    }


    //视频抽帧
    @ReactMethod
    fun corpVideoFrame(options: ReadableMap, promise: Promise) {
        val context = reactContext
        CropManager.corpVideoFrame(context.applicationContext, options, promise)
    }

    @ReactMethod
    fun crop(options: ReadableMap, promise: Promise) {
        val context = reactContext
        val filePath = if (options.hasKey("source")) options.getString("source") else ""
        if (TextUtils.isEmpty(filePath)) {
            promise.reject("crop", "error: source is empty")
        } else {
            if (isVideo(filePath)) {
                CropManager.cropVideo(context, options, promise)
            } else {
                CropManager.cropImage(context, options, promise)
            }
        }
    }

    @ReactMethod
    fun cropVideo(options: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        CropManager.cropVideo(context, options, promise)
    }


    /**
     * 将沙盒的图片\视频 保存到相册
     */
    @ReactMethod
    fun saveMediaStore(filePath: String, sourceType: String, promise: Promise) {
        val context = reactContext
//            if (sourceType == "video") {
//                AliFileUtils.saveVideoToMediaStore(context.applicationContext, filePath)
//            } else {
//                AliFileUtils.saveImageToMediaStore(context.applicationContext, filePath)
//            }
        if (isVideo(filePath)) {
            AliFileUtils.saveVideoToMediaStore(context.applicationContext, filePath)
        } else {
            AliFileUtils.saveImageToMediaStore(context.applicationContext, filePath)
        }
        promise.resolve(true)
    }


    private fun isVideo(fileName: String?): Boolean {
        val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
        val contentTypeFor: String = fileNameMap.getContentTypeFor(fileName)
        return contentTypeFor.contains("video")
    }


    @ReactMethod
    fun removeThumbnaiImages(promise: Promise) {
        doAsync {
            com.blankj.utilcode.util.FileUtils.deleteAllInDir(CropManager.getVideoFrameDirs(reactContext.applicationContext))
            uiThread {
                promise.resolve(true)
            }
        }
    }

    @ReactMethod
    fun clearResources(promise: Promise) {
        doAsync {
            com.blankj.utilcode.util.FileUtils.deleteAllInDir(CropManager.getMediaCacheDirs(reactContext.applicationContext))
            uiThread {
                promise.resolve(true)
            }
        }
    }

    @ReactMethod
    fun release(promise: Promise) {
        MediaPlayerManage.instance.release()
        mView?.onRelease()

//        val context = reactContext
//        val uiManager = context.getNativeModule(UIManagerModule::class.java)
//
//        Log.e("AAA", "CKEditor release：$viewTag")
//        context.runOnUiQueueThread {
//            if (uiManager?.resolveView(viewTag) != null && uiManager.resolveView(viewTag) is CKEditor) {
//                Log.e("AAA", "CKEditor release 222")
//                val view = uiManager?.resolveView(viewTag) as CKEditor
//                view.onRelease()
//            }
//        }
    }

}