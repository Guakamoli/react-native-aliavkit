package com.rncamerakit.recorder.manager

import android.text.TextUtils
import android.util.Log
import com.aliyun.common.utils.UriUtil
import com.aliyun.svideo.common.utils.UriUtils
import com.aliyun.svideo.downloader.FileDownloaderCallback
import com.aliyun.svideo.downloader.zipprocessor.DownloadFileUtils
import com.aliyun.svideo.recorder.view.effects.manager.EffectLoader
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.google.gson.GsonBuilder
import com.liulishuo.filedownloader.BaseDownloadTask
import com.manwei.libs.utils.GsonManage
import com.rncamerakit.RNEventEmitter
import java.io.File
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

    private lateinit var mReactContext: ReactContext

    fun init(context: ReactContext) {
        mReactContext = context
        mPaterLoader = EffectLoader(mReactContext.applicationContext)
    }

    private val mPasterList: MutableList<PreviewPasterForm> = ArrayList()

    private var mPaterLoader: EffectLoader? = null


    fun getPasterInfos(promise: Promise?) {
        mPasterList.clear()
        val emptyPaster = PreviewPasterForm()
        emptyPaster.isLocalRes = true
        mPasterList.add(emptyPaster)
        mPaterLoader?.loadAllPaster(null) { localInfos, remoteInfos, _ ->
            for (form in localInfos!!) {
                form.isLocalRes = true
                form.icon = "file://"+form.icon
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

    fun setEffectPaster(paster: PreviewPasterForm?, callback: OnGifEffectPasterCallback) {
        val path: String
        if (paster?.isLocalRes == true) {
            path = if (paster.id == 150) {
                paster.path
            } else {
                DownloadFileUtils.getAssetPackageDir(
                    mReactContext.applicationContext,
                    paster.name,
                    paster.id.toLong()
                ).absolutePath
            }
            Log.e("AAA", "local progress：$path")
            callback.onPath(path)
        } else {
            if (TextUtils.isEmpty(paster?.url)) {
                callback.onPath("")
                return
            }
            //需要下载
            mPaterLoader!!.downloadPaster(paster, object : FileDownloaderCallback() {
                override fun onProgress(
                    downloadId: Int,
                    soFarBytes: Long,
                    totalBytes: Long,
                    speed: Long,
                    progress: Int
                ) {
                    Log.e("AAA", "download progress：$progress")
                    RNEventEmitter.downloadPasterProgress(mReactContext, progress)
                }

                override fun onFinish(downloadId: Int, path: String) {
                    Log.e("AAA", "download path：$path")
                    RNEventEmitter.downloadPasterProgress(mReactContext, 100)
                    callback.onPath(path)
                }

                override fun onError(task: BaseDownloadTask, e: Throwable) {
                    super.onError(task, e)
                    Log.e("AAA", "download error：${e.message}")
                }
            })
        }
    }

    fun downloadPaster(paster: PreviewPasterForm?, promise: Promise) {
        mPaterLoader!!.downloadPaster(paster, object : FileDownloaderCallback() {
            override fun onProgress(
                downloadId: Int,
                soFarBytes: Long,
                totalBytes: Long,
                speed: Long,
                progress: Int
            ) {
                Log.e("AAA", "download progress：$progress")
                RNEventEmitter.downloadPasterProgress(mReactContext, progress)
            }

            override fun onFinish(downloadId: Int, path: String) {
                Log.e("AAA", "download path：$path")
                RNEventEmitter.downloadPasterProgress(mReactContext, 100)
                paster?.isLocalRes = true
                paster?.path = path
                promise.resolve(paster)
            }

            override fun onError(task: BaseDownloadTask, e: Throwable) {
                super.onError(task, e)
                Log.e("AAA", "download error：${e.message}")
                promise.reject("downloadPaster", "error:" + (e.message))
            }
        })
    }

}