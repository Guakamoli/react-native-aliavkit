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
import com.manwei.libs.utils.GsonManage
import com.rncamerakit.RNEventEmitter
import java.util.*

/**
 * 人脸贴纸
 */
class EffectPasterManage(private val mContext: ThemedReactContext) {

    private val mPasterList: MutableList<PreviewPasterForm> = ArrayList()

    private var mPaterLoader: EffectLoader? = null

    init {
        mPaterLoader = EffectLoader(mContext.applicationContext)
    }

    fun getPasterInfos(promise: Promise?) {
        mPasterList.clear()
        mPasterList.add(PreviewPasterForm())
        mPaterLoader?.loadAllPaster(null) { localInfos, remoteInfos, _ ->
            for (form in localInfos!!) {
                form.isLocalRes = true
                mPasterList.add(form)
            }
            for (mv in remoteInfos!!) {
                mv.isLocalRes = false
                mPasterList.add(mv)
            }
            promise?.resolve(GsonManage.toJson(mPasterList))
        }
    }

    abstract class OnGifEffectPasterCallback {
        open fun onPath(path: String) {}
    }

    fun setEffectPaster(paster: PreviewPasterForm?,callback: OnGifEffectPasterCallback) {
        val path: String
        if (paster?.isLocalRes == true) {
            path = if (paster.id == 150) {
                paster.path
            } else {
                DownloadFileUtils.getAssetPackageDir(
                    mContext,
                    paster.name,
                    paster.id.toLong()
                ).absolutePath
            }
            Log.e("AAA", "local progress：$path")
            callback.onPath(path)
        } else {
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
                RNEventEmitter.downloadPasterProgress(mContext, progress);
            }

            override fun onFinish(downloadId: Int, path: String) {
                Log.e("AAA", "download path：$path")
                RNEventEmitter.downloadPasterProgress(mContext, 100);
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