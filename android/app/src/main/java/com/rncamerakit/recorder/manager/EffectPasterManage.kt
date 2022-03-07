package com.rncamerakit.recorder.manager

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.downloader.FileDownloaderCallback
import com.aliyun.svideo.recorder.view.effects.manager.EffectLoader
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.google.gson.GsonBuilder
import com.liulishuo.filedownloader.BaseDownloadTask
import com.manwei.libs.utils.FileUtils
import com.rncamerakit.RNEventEmitter
import java.util.*


/**
 * 人脸贴纸
 */
class EffectPasterManage private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = EffectPasterManage()
    }

    private lateinit var mContext: Context
    fun init(context: Context) {
        mContext = context
        mPaterLoader = EffectLoader(mContext)
    }

    val mPasterList: MutableList<PreviewPasterForm> = ArrayList()

    private var mPaterLoader: EffectLoader? = null


    /**
     * 获取贴纸列表
     */
    fun getPasterInfos(promise: Promise?, reactContext: ReactContext?) {
        mPasterList.clear()
//        val emptyPaster = PreviewPasterForm()
//        emptyPaster.isLocalRes = true
//        mPasterList.add(emptyPaster)
        if (mPaterLoader == null && reactContext != null) {
            mPaterLoader = EffectLoader(reactContext?.applicationContext)
        }
        mPaterLoader?.loadAllPaster(null) { localInfos, remoteInfos, _ ->
            for (form in localInfos!!) {
                if (form.id == 150) {
                    form.icon = "file://" + form.icon
                }
                form.isLocalRes = FileUtils.fileIsExists(form.path)
                mPasterList.add(form)
            }
            for (mv in remoteInfos!!) {
                mv.isLocalRes = false
                mPasterList.add(mv)
            }
            val jsonList = GsonBuilder().create().toJson(mPasterList)
            promise?.resolve(jsonList)
        }
    }

    abstract class OnGifEffectPasterCallback {
        open fun onPath(path: String) {}
    }

    fun setEffectPaster(paster: PreviewPasterForm, mReactContext: ReactContext?, callback: OnGifEffectPasterCallback) {
        val path = if (paster.id == 150) {
            paster.path
        } else {
            EffectLoader.getEffectPasterDir(
                mContext,
                paster.name,
                paster.id
            ).absolutePath
//                DownloadFileUtils.getAssetPackageDir(
//                    mContext,
//                    paster.name,
//                    paster.id.toLong()
//                ).absolutePath
        }
        if (FileUtils.dirNotEmpty(path)) {
            Log.e("AAA", "local effectPaster progress：$path")
            callback.onPath(path)
        } else {
            if (TextUtils.isEmpty(paster?.url)) {
                callback.onPath("")
                return
            }
            //需要下载
            mPaterLoader?.downloadPaster(paster, object : FileDownloaderCallback() {
                override fun onProgress(
                    downloadId: Int,
                    soFarBytes: Long,
                    totalBytes: Long,
                    speed: Long,
                    progress: Int
                ) {
                    Log.e("AAA", "download effectPaster progress：$progress")
                    RNEventEmitter.downloadPasterProgress(mReactContext, progress)
                }

                override fun onFinish(downloadId: Int, path: String) {
                    Log.e("AAA", "下载完成 path：$path")
                    RNEventEmitter.downloadPasterProgress(mReactContext, 100)
                    callback.onPath(path)
                }

                override fun onError(task: BaseDownloadTask, e: Throwable) {
                    super.onError(task, e)
                    Log.e("AAA", "download effectPaster error：${e.message}")
                }
            })
        }
    }

    fun downloadPaster(paster: PreviewPasterForm?, mReactContext: ReactContext?, promise: Promise) {
        mPaterLoader?.downloadPaster(paster, object : FileDownloaderCallback() {
            override fun onProgress(
                downloadId: Int,
                soFarBytes: Long,
                totalBytes: Long,
                speed: Long,
                progress: Int
            ) {
//                Log.e("AAA", "download progress：$progress")
                RNEventEmitter.downloadPasterProgress(mReactContext, progress)
            }

            override fun onFinish(downloadId: Int, path: String) {
//                Log.e("AAA", "download path：$path")
                RNEventEmitter.downloadPasterProgress(mReactContext, 100)
                paster?.isLocalRes = true
                paster?.path = path
                promise.resolve(paster)
            }

            override fun onError(task: BaseDownloadTask, e: Throwable) {
                super.onError(task, e)
//                Log.e("AAA", "download error：${e.message}")
                promise.reject("downloadPaster", "error:" + (e.message))
            }
        })
    }

}