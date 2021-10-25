package com.rncamerakit.recorder

import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import com.google.gson.GsonBuilder
import com.rncamerakit.db.MusicFileInfo
import com.rncamerakit.db.MusicFileInfoDao
import com.rncamerakit.recorder.manager.EffectPasterManage
import com.rncamerakit.recorder.manager.MediaPlayerManage
import com.rncamerakit.utils.DownloadUtils

class RNCameraKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val PORTRAIT = 0 // ⬆️
        const val LANDSCAPE_LEFT = 1 // ⬅️
        const val PORTRAIT_UPSIDE_DOWN = 2 // ⬇️
        const val LANDSCAPE_RIGHT = 3 // ➡️
    }

    override fun getName(): String {
        return "RNCameraKitModule"
    }

    override fun getConstants(): Map<String, Any> {
        return hashMapOf(
            "PORTRAIT" to PORTRAIT,
            "PORTRAIT_UPSIDE_DOWN" to PORTRAIT_UPSIDE_DOWN,
            "LANDSCAPE_LEFT" to LANDSCAPE_LEFT,
            "LANDSCAPE_RIGHT" to LANDSCAPE_RIGHT
        )
    }

    //设置滤镜
    @ReactMethod
    fun setColorFilter(position: Int, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        val view = uiManager?.resolveView(viewTag) as CKCamera
        view.mRecorderManage?.setColorFilter()
    }

    //去拍照
    @ReactMethod
    fun capture(options: ReadableMap, viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.mRecorderManage?.takePhoto(context, promise)
        }
    }

    @ReactMethod
    fun startRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            if (!view.isPermissions()) {
                view.getPermissions()
                return@runOnUiQueueThread
            }
            view.mRecorderManage?.startRecording(context, promise)
        }
    }

    @ReactMethod
    fun stopRecording(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
            val view = uiManager?.resolveView(viewTag) as CKCamera
            view.mRecorderManage?.stopRecording(context, promise)
        }
    }

    /**
     * 获取贴纸列表
     */
    @ReactMethod
    fun getPasterInfos(promise: Promise) {
        reactContext.runOnUiQueueThread {
            EffectPasterManage.instance.getPasterInfos(promise)
        }
    }

//    @ReactMethod
//    fun getMusicList(promise: Promise) {
//        val list = MusicFileInfoDao.instance.queryAll()
//        promise.resolve(GsonBuilder().create().toJson(list))
//    }


    @ReactMethod
    fun getMusicList(name: String, page: Int, pageSize: Int, promise: Promise) {
        val list = MusicFileInfoDao.instance.queryList(name, page, pageSize)
        promise.resolve(GsonBuilder().create().toJson(list))
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

    /**
     * 获取音乐地址，本地存在返回本地地址；本地不存在，先下载后返回下载的地址
     */
    @ReactMethod
    fun getMusicPath(songID: Int, promise: Promise) {
        val musicInfo: MusicFileInfo? = MusicFileInfoDao.instance.query(songID)
        if (musicInfo?.isDbContain == 1 && FileUtils.fileIsExists((musicInfo.localPath))) {
            promise.resolve(musicInfo.localPath)
            return
        }
        reactContext.runOnUiQueueThread {
            DownloadUtils.downloadMusic(reactContext, songID, musicInfo?.url, promise)
        }
    }


    /**
     * 下载贴纸
     */
    @ReactMethod
    fun downloadPaster(readableMap: ReadableMap, viewTag: Int, promise: Promise) {
        if (readableMap.toHashMap().size > 0) {
            val context = reactContext
            context.runOnUiQueueThread {
                val previewPaster = PreviewPasterForm()
                previewPaster.icon =
                    if (readableMap.hasKey("icon")) readableMap.getString("icon") else ""
                previewPaster.type =
                    if (readableMap.hasKey("type")) readableMap.getInt("type") else 0
                previewPaster.id = if (readableMap.hasKey("id")) readableMap.getInt("id") else 0
                previewPaster.sort =
                    if (readableMap.hasKey("sort")) readableMap.getInt("sort") else 0
                previewPaster.url =
                    if (readableMap.hasKey("url")) readableMap.getString("url") else ""
                previewPaster.md5 =
                    if (readableMap.hasKey("md5")) readableMap.getString("md5") else ""
                previewPaster.preview =
                    if (readableMap.hasKey("preview")) readableMap.getString("preview") else ""
                previewPaster.name =
                    if (readableMap.hasKey("name")) readableMap.getString("name") else ""
                previewPaster.fontId =
                    if (readableMap.hasKey("fontId")) readableMap.getInt("fontId") else 0
                previewPaster.level =
                    if (readableMap.hasKey("level")) readableMap.getInt("level") else 0
                previewPaster.isLocalRes =
                    if (readableMap.hasKey("isLocalRes")) readableMap.getBoolean("isLocalRes") else false
                previewPaster.path =
                    if (readableMap.hasKey("path")) readableMap.getString("path") else ""
                EffectPasterManage.instance.downloadPaster(previewPaster, promise)
            }
        }
    }

    @ReactMethod
    fun release(viewTag: Int, promise: Promise) {
        val context = reactContext
        val uiManager = context.getNativeModule(UIManagerModule::class.java)
        context.runOnUiQueueThread {
//            val view = uiManager?.resolveView(viewTag) as CKCamera
//            view.onRelease()
        }
    }

}
