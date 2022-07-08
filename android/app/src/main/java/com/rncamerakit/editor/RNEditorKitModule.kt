package com.rncamerakit.editor

import android.annotation.SuppressLint
import android.text.TextUtils
import com.aliyun.svideo.base.http.EffectService
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.downloader.FileDownloaderModel
import com.aliyun.svideosdk.crop.AliyunICrop
import com.facebook.react.bridge.*
import com.google.gson.GsonBuilder
import com.liulishuo.filedownloader.BaseDownloadTask
import com.manwei.libs.utils.GsonManage
import com.rncamerakit.crop.CropManager
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.db.MusicFileInfoDao
import com.rncamerakit.editor.manager.ComposeManager
import com.rncamerakit.font.FontManager
import com.rncamerakit.recorder.manager.MediaPlayerManage
import com.rncamerakit.utils.AliFileUtils
import com.rncamerakit.utils.DownloadUtils
import com.rncamerakit.utils.MyFileDownloadCallback
import com.rncamerakit.watermark.WatermarkManager
import kotlinx.coroutines.DelicateCoroutinesApi
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.FileNameMap
import java.net.URLConnection
import java.util.ArrayList

@DelicateCoroutinesApi
class RNEditorKitModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {


    companion object {
        @SuppressLint("StaticFieldLeak")
        var mView: CKEditor? = null

        var mPostAliyunICrop: AliyunICrop? = null

        var mComposeManager: ComposeManager? = null

        var mPostCropPromise: Promise? = null
        var mStoryComposePromise: Promise? = null

    }

    override fun getName(): String {
        return "RNEditorKitModule"
    }

    @ReactMethod
    fun saveToSandBox(uri: String, promise: Promise) {
        val context = reactContext
        val boxPath = AliFileUtils.saveToSandBox(context, uri)
        promise.resolve(boxPath)
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
            val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID, reactContext.applicationContext)
            if (musicInfo != null) {
                val list: MutableList<MusicFileBean> = ArrayList()
                list.add(musicInfo)
                promise.resolve(GsonBuilder().create().toJson(list))
                return
            }
        }

        val musicAll = MusicFileInfoDao.instance.queryAll()
        if (musicAll == null || musicAll.isEmpty()) {
            DownloadUtils.getMusicJsonInfo {
                val list = MusicFileInfoDao.instance.queryList(name, page, pageSize, reactContext.applicationContext)
                promise.resolve(GsonBuilder().create().toJson(list))
            }
        } else {
            val list = MusicFileInfoDao.instance.queryList(name, page, pageSize, reactContext.applicationContext)
            promise.resolve(GsonBuilder().create().toJson(list))
        }
    }

    @ReactMethod
    fun playMusic(songID: String?, promise: Promise) {
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID, reactContext.applicationContext)
        if (musicInfo?.isDbContain == 1 && FileUtils.fileIsExists((musicInfo.localPath))) {
            musicInfo.localPath?.let {
                MediaPlayerManage.instance.start(it, null)
            }
            promise.resolve(GsonBuilder().create().toJson(musicInfo))
            return
        }
        if (musicInfo != null) {
            DownloadUtils.downloadMusic(reactContext, musicInfo.songID, musicInfo.url, null,
                object : MyFileDownloadCallback() {
                    override fun completed(task: BaseDownloadTask) {
                        super.completed(task)
                        val musicBean: MusicFileBean? = MusicFileInfoDao.instance.query(musicInfo.songID, reactContext.applicationContext)
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
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID, reactContext.applicationContext)
        promise.resolve(GsonBuilder().create().toJson(musicInfo))
    }

    @ReactMethod
    fun resumeMusic(songID: String, promise: Promise) {
        MediaPlayerManage.instance.resume()
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID, reactContext.applicationContext)
        promise.resolve(GsonBuilder().create().toJson(musicInfo))
    }

    @ReactMethod
    fun pauseMusic(songID: String, promise: Promise) {
        MediaPlayerManage.instance.pause()
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID, reactContext.applicationContext)
        promise.resolve(GsonBuilder().create().toJson(musicInfo))
    }

    //获取滤镜列表
    @ReactMethod
    fun getColorFilterList(promise: Promise) {
        reactContext.runOnUiQueueThread {
            mView?.getColorFilterList(promise)
        }
    }


    /**
     * 视频裁剪，开始结束时间，软裁剪
     */
    @ReactMethod
    fun trimVideo(options: ReadableMap, promise: Promise) {
        mView?.trimVideo(options, promise)
    }

    @ReactMethod
    fun pause(promise: Promise) {
        mView?.pause(promise)
    }

    @ReactMethod
    fun stop(promise: Promise) {
        mView?.stop(promise)
    }


    @ReactMethod
    fun play(promise: Promise) {
        mView?.play(promise)
    }


    @ReactMethod
    fun seek(seekTime: Int, promise: Promise) {
        mView?.seek(seekTime, promise)
    }


    //获取视频封面
    @ReactMethod
    fun videoCover(seekTime: Int, promise: Promise) {
        mView?.videoCover(seekTime, promise)
    }


    @ReactMethod
    fun exportVideo(promise: Promise) {
        mView?.exportVideo(promise)
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
    fun cropVideo(options: ReadableMap, promise: Promise) {
        val context = reactContext
        CropManager.cropVideo(context, options, promise)
    }

    @ReactMethod
    fun postCancelCrop(promise: Promise) {
        reactContext.runOnUiQueueThread {
            val cropMap: HashMap<String, Any> = HashMap<String, Any>()
//            cropMap["path"] = ""
//            cropMap["isCroped"] = false
            val jsonString = GsonBuilder().create().toJson(cropMap)
            mPostCropPromise?.resolve(jsonString)
        }
        mPostAliyunICrop?.cancel()
        mPostAliyunICrop?.dispose()
        mPostAliyunICrop = null
        promise.resolve(true)
    }

    @ReactMethod
    fun postCropVideo(videoPath: String, promise: Promise) {
        mPostCropPromise = promise
        val context = reactContext
        mPostAliyunICrop = CropManager.postCropVideo(context, videoPath, promise)
    }

    @ReactMethod
    fun getVideoEditorJsonPath(promise: Promise) {
        val jsonPath = mView?.getVideoEditorJsonPath()
        promise.resolve(jsonPath)
    }

    @ReactMethod
    fun stopEdit(promise: Promise) {
        val isStop = mView?.stopEdit()
        if (isStop == true) {
            promise.resolve(isStop)
        } else {
            promise.resolve(false)
        }
    }

    @ReactMethod
    fun storyComposeVideo(jsonPath: String, promise: Promise) {
        mView?.saveEffects()
        mStoryComposePromise = promise
        mComposeManager = ComposeManager(reactContext)
        mComposeManager?.startCompose(jsonPath, promise, isVideo = true, isSaveToPhotoLibrary = false, isStoryCompose = true)
    }

    @ReactMethod
    fun storyCancelCompose(promise: Promise) {
        reactContext.runOnUiQueueThread {
            val composeParamMap: HashMap<String, Any> = HashMap<String, Any>()
            mStoryComposePromise?.resolve(GsonBuilder().create().toJson(composeParamMap))
            mStoryComposePromise = null
        }
        mComposeManager?.onRelease()
        promise.resolve(true)
    }

    /**
     * 将沙盒的图片\视频 保存到相册
     */
    @ReactMethod
    fun saveMediaStore(filePath: String, sourceType: String, promise: Promise) {
        val context = reactContext
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

    /**
     * 获取所有字体
     */
    @ReactMethod
    fun getFontList(promise: Promise) {
        val fonts = FontManager.instance.getDownloadFontList()
        promise.resolve(GsonBuilder().create().toJson(fonts))
    }

    /**
     * 下载字体
     */
    @ReactMethod
    fun downloadFont(options: ReadableMap, promise: Promise) {
        val bundle = Arguments.toBundle(options)
        var model = FileDownloaderModel()
        val jsonObject = GsonBuilder().create().toJsonTree(model).asJsonObject
        bundle?.keySet()?.let { set ->
            set.forEach {
                jsonObject.add(it, GsonBuilder().create().toJsonTree(bundle.get(it)))
            }
        }
        model = GsonManage.fromJson(jsonObject, FileDownloaderModel::class.java)
        model.isunzip = 1
        model.effectType = EffectService.EFFECT_TEXT
        FontManager.instance.downloadFont(reactContext.applicationContext, model, promise)
    }

    @ReactMethod
    fun release(promise: Promise) {
        MediaPlayerManage.instance.release()
        mView?.onRelease()
    }


    @ReactMethod
    fun exportWaterMarkVideo(options: ReadableMap, promise: Promise) {
        val videoPath = if (options.hasKey("videoPath")) options.getString("videoPath") else ""
        val revoId = if (options.hasKey("revoId")) options.getString("revoId") else ""
        WatermarkManager.exportWaterMarkVideo(reactContext, videoPath, revoId, promise)
    }

}