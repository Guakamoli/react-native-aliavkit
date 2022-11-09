package com.rncamerakit.recorder

import android.annotation.SuppressLint
import android.util.Log
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import com.google.gson.GsonBuilder
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.db.MusicFileInfoDao
import com.rncamerakit.editor.CKEditor
import com.rncamerakit.editor.RNEditorKitModule
import com.rncamerakit.recorder.manager.EffectPasterManage
import com.rncamerakit.recorder.manager.MediaPlayerManage
import com.rncamerakit.utils.DownloadUtils

class RNCameraKitModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var mView: CKCamera? = null
        var mPreviewPasterForm: PreviewPasterForm? = null
    }

    override fun getName(): String {
        return "RNCameraKitModule"
    }

    //恢复录制
    @ReactMethod
    fun resumeCamera(promise: Promise) {
        mView?.resumeCamera()
    }

    //暂停录制
    @ReactMethod
    fun pauseCamera(promise: Promise) {
        mView?.pauseCamera()
    }


    //获取滤镜列表
    @ReactMethod
    fun getRecordColorFilter(promise: Promise) {
        reactContext.runOnUiQueueThread {
            mView?.getRecordColorFilter(promise)
        }
    }

    //设置滤镜
    @ReactMethod
    fun setColorFilter(position: Int, promise: Promise) {
        mView?.mRecorderManage?.setColorFilter()
    }

    //去拍照
    @ReactMethod
    fun capture(promise: Promise) {
        mView?.mRecorderManage?.takePhoto(reactContext, promise)
    }

    @ReactMethod
    fun startRecording(promise: Promise) {
        if (mView?.isPermissions() == false) {
            mView?.getPermissions()
            return
        }
        mView?.mRecorderManage?.startRecording(reactContext, promise)
    }

    @ReactMethod
    fun stopRecording(promise: Promise) {
        mView?.mRecorderManage?.stopRecording(reactContext, promise)
    }

    @ReactMethod
    fun startMultiRecording(promise: Promise) {
        mView?.mRecorderManage?.startMultiRecording(reactContext, promise)
    }

    @ReactMethod
    fun stopMultiRecording(promise: Promise) {
        mView?.mRecorderManage?.stopMultiRecording(reactContext, promise)
    }

    @ReactMethod
    fun finishMultiRecording(promise: Promise) {
        mView?.mRecorderManage?.finishMultiRecording(reactContext, promise)
    }

    @ReactMethod
    fun deleteLastMultiRecording(promise: Promise) {
        mView?.mRecorderManage?.deleteLastMultiRecording(reactContext, promise)
    }

    @ReactMethod
    fun deleteAllMultiRecording(promise: Promise) {
        mView?.mRecorderManage?.deleteAllMultiRecording(reactContext, promise)
    }

    /**
     * 获取贴纸列表
     */
    @ReactMethod
    fun getPasterInfos(promise: Promise) {
        reactContext.runOnUiQueueThread {
            EffectPasterManage.instance.getPasterInfos(promise, reactContext)
        }
    }

    @ReactMethod
    fun getMusicList(name: String, page: Int, pageSize: Int, promise: Promise) {
        val list = MusicFileInfoDao.instance.queryList(name, page, pageSize, reactContext.applicationContext)
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
    fun getMusicPath(songID: String, promise: Promise) {
        val musicInfo: MusicFileBean? = MusicFileInfoDao.instance.query(songID, reactContext.applicationContext)
        if (musicInfo?.isDbContain == 1 && FileUtils.fileIsExists((musicInfo.localPath))) {
            promise.resolve(musicInfo.localPath)
            return
        }
//        reactContext.runOnUiQueueThread {
        DownloadUtils.downloadMusic(reactContext, songID, musicInfo?.url, promise, null)
//        }
    }


    /**
     * 下载贴纸
     */
    @ReactMethod
    fun downloadPaster(readableMap: ReadableMap, viewTag: Int, promise: Promise) {
        if (readableMap.toHashMap().size > 0) {
            val context = reactContext
//            context.runOnUiQueueThread {
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
            EffectPasterManage.instance.downloadPaster(previewPaster, reactContext, promise)
//            }
        }
    }


    @ReactMethod
    fun setFacePasterInfo(readableMap: ReadableMap, promise: Promise) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            val previewPaster = PreviewPasterForm()

            previewPaster.icon =
                if (readableMap.hasKey("icon")) readableMap.getString("icon") else ""
            previewPaster.type =
                if (readableMap.hasKey("type")) readableMap.getInt("type") else 0
            previewPaster.id = if (readableMap.hasKey("id")) readableMap.getInt("id") else 0
            previewPaster.sort = if (readableMap.hasKey("sort")) readableMap.getInt("sort") else 0

            previewPaster.url = if (readableMap.hasKey("url")) readableMap.getString("url") else ""
            previewPaster.md5 = if (readableMap.hasKey("md5")) readableMap.getString("md5") else ""
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
            RNCameraKitModule.mPreviewPasterForm = previewPaster
            reactContext.runOnUiQueueThread {
                mView?.mRecorderManage?.setFaceEffectPaster(previewPaster, reactContext)
            }
        }
    }


    @ReactMethod
    fun release(promise: Promise) {
        mView?.onRelease()
    }

    @ReactMethod
    fun releaseCamera(promise: Promise) {
        mView?.onRelease()
    }

}
