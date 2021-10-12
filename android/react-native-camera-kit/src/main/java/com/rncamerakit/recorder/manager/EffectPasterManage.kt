package com.rncamerakit.recorder.manager

import android.util.Log
import com.aliyun.svideo.downloader.FileDownloaderCallback
import com.aliyun.svideo.downloader.zipprocessor.DownloadFileUtils
import com.aliyun.svideo.recorder.view.effects.EffectBody
import com.aliyun.svideo.recorder.view.effects.manager.EffectLoader
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.ThemedReactContext
import com.liulishuo.filedownloader.BaseDownloadTask
import com.rncamerakit.RNEventEmitter
import java.util.*

/**
 * 人脸贴纸
 */
class EffectPasterManage(private val mContext: ThemedReactContext) {

    private val mRemoteData: MutableList<EffectBody<PreviewPasterForm>> = ArrayList()

    private var mPaterLoader: EffectLoader? = null

    init {
        mPaterLoader = EffectLoader(mContext.applicationContext)
    }

    fun initEffectPasterList() {
        mRemoteData.clear()
        val form = PreviewPasterForm()
        val effectBody = EffectBody(form, true)
        mRemoteData.add(0, effectBody)
        mPaterLoader?.loadAllPaster(null) { localInfos, remoteInfos, _ ->
            for (form in localInfos!!) {
                val body = EffectBody(form, true)
                mRemoteData.add(body)
            }
            for (mv in remoteInfos!!) {
                val body = EffectBody(mv, false)
                mRemoteData.add(body)
            }
        }
    }

    abstract class OnGifEffectPasterCallback {
        open fun onPath(path: String) {}
    }

    private var position: Int = 0;

    fun setEffectPaster(callback: OnGifEffectPasterCallback) {
        if (mRemoteData.isEmpty() || position >= mRemoteData.size) {
            position = 0
            return
        }
        val effectBody = mRemoteData[position]
        var path: String
        if (effectBody.isLocal) {
            effectBody.data?.let {
                path = if (it.id == 150) {
                    it.path
                } else {
                    DownloadFileUtils.getAssetPackageDir(
                        mContext,
                        it.name,
                        it.id.toLong()
                    ).absolutePath
                }
                Log.e("AAA", "local progress：$path")
                callback.onPath(path)
            }
        } else {
            //需要下载
            mPaterLoader!!.downloadPaster(effectBody.data, object : FileDownloaderCallback() {
                override fun onProgress(
                    downloadId: Int,
                    soFarBytes: Long,
                    totalBytes: Long,
                    speed: Long,
                    progress: Int
                ) {
                    Log.e("AAA", "download progress：$progress")
                    RNEventEmitter.downloadPasterProgress(mContext, progress);
                }

                override fun onFinish(downloadId: Int, path: String) {
                    Log.e("AAA", "download path：$path")
                    RNEventEmitter.downloadPasterProgress(mContext, 100);
                    callback.onPath(path)
                }

                override fun onError(task: BaseDownloadTask, e: Throwable) {
                    super.onError(task, e)
                    Log.e("AAA", "download error：${e.message}")
                }
            })
        }
        position++;
    }

    fun downloadPaster(effectBody: EffectBody<PreviewPasterForm>?, promise: Promise) {
        mPaterLoader!!.downloadPaster(effectBody?.data, object : FileDownloaderCallback() {
            override fun onProgress(
                downloadId: Int,
                soFarBytes: Long,
                totalBytes: Long,
                speed: Long,
                progress: Int
            ) {
                Log.e("AAA", "download progress：$progress")
                RNEventEmitter.downloadPasterProgress(mContext, progress);
            }

            override fun onFinish(downloadId: Int, path: String) {
                Log.e("AAA", "download path：$path")
                RNEventEmitter.downloadPasterProgress(mContext, 100);
                promise.resolve(path)
            }

            override fun onError(task: BaseDownloadTask, e: Throwable) {
                super.onError(task, e)
                Log.e("AAA", "download error：${e.message}")
                promise.reject("downloadPaster", "error:" + (e.message))
            }
        })
    }

}