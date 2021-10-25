package com.rncamerakit.editor

import android.text.TextUtils
import com.aliyun.svideo.common.utils.FileUtils
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import com.google.gson.GsonBuilder
import com.rncamerakit.crop.CropManager
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.db.MusicFileInfoDao
import com.rncamerakit.recorder.manager.MediaPlayerManage
import com.rncamerakit.utils.AliFileUtils
import com.rncamerakit.utils.DownloadUtils
import java.net.FileNameMap
import java.net.URLConnection

class RNEditorKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "RNEditorKitModule"
    }

    @ReactMethod
    fun getMusicList(name: String, page: Int, pageSize: Int, promise: Promise) {
        val list = MusicFileInfoDao.instance.queryList(name, page, pageSize)
        promise.resolve(GsonBuilder().create().toJson(list))
    }

    /**
     * 获取音乐地址，本地存在返回本地地址；本地不存在，先下载后返回下载的地址
     */
    @ReactMethod
    fun getMusicPath(songID: Int, promise: Promise) {
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID)
        if (musicInfo?.isDbContain == 1 && FileUtils.fileIsExists((musicInfo.localPath))) {
            promise.resolve(musicInfo.localPath)
            return
        }
        reactContext.runOnUiQueueThread {
            DownloadUtils.downloadMusic(reactContext, songID, musicInfo?.url, promise,null)
        }
    }

    @ReactMethod
    fun playMusic(musicPath: String, promise: Promise) {
        MediaPlayerManage.instance.start(musicPath, promise)
    }

    @ReactMethod
    fun stopMusic(promise: Promise) {
        MediaPlayerManage.instance.release()
        promise.resolve(true)
    }

    //获取滤镜列表
    @ReactMethod
    fun getColorFilterList(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.getColorFilterList(promise)
        }
    }


    /**
     * 视频裁剪，开始结束时间，软裁剪
     */
    @ReactMethod
    fun videoTrim(options: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.videoTrim(options, promise)
        }
    }

    @ReactMethod
    fun pause(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.pause(promise)
        }
    }

    @ReactMethod
    fun stop(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.stop(promise)
        }
    }


    @ReactMethod
    fun play(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.play(promise)
        }
    }


    @ReactMethod
    fun seek(seekTime: Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.seek(seekTime, promise)
        }
    }


    @ReactMethod
    fun videoCover(seekTime: Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.videoCover(seekTime, promise)
        }
    }


    @ReactMethod
    fun exportVideo(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.exportVideo(promise)
        }
    }


    @ReactMethod
    fun corpVideoFrame(options: ReadableMap, promise: Promise) {
        val context = reactContext
        context.runOnUiQueueThread {
            CropManager.corpVideoFrame(context.applicationContext, options, promise)
        }
    }

    @ReactMethod
    fun crop(options: ReadableMap, promise: Promise) {
        val context = reactContext
        context.runOnUiQueueThread {
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
    }

    @ReactMethod
    fun cropVideo(options: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        context.runOnUiQueueThread {
            CropManager.cropVideo(context, options, promise)
        }
    }


    /**
     * 将沙盒的图片\视频 保存到相册
     */
    @ReactMethod
    fun saveMediaStore(filePath: String, sourceType: String, promise: Promise) {
        val context = reactContext
        context.runOnUiQueueThread {
            if (isVideo(filePath)) {
                AliFileUtils.saveVideoToMediaStore(context.applicationContext, filePath)
            } else {
                AliFileUtils.saveImageToMediaStore(context.applicationContext, filePath)
            }
        }
    }


    private fun isVideo(fileName: String?): Boolean {
        val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
        val contentTypeFor: String = fileNameMap.getContentTypeFor(fileName)
        return contentTypeFor.contains("video")
    }

    @ReactMethod
    fun release(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            MediaPlayerManage.instance.release()
            val view = uiManager?.resolveView(viewTag) as CKEditor
            view.onRelease()
        }
    }

}