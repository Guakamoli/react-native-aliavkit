package com.rncamerakit.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.aliyun.svideo.downloader.FileDownloaderCallback
import com.aliyun.svideo.downloader.zipprocessor.DownloadFileUtils
import com.aliyun.svideo.recorder.view.effects.EffectBody
import com.aliyun.svideo.recorder.view.effects.manager.EffectLoader
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.liulishuo.filedownloader.BaseDownloadTask
import java.util.*

class EffectManage {

    private val mRemoteData: MutableList<EffectBody<PreviewPasterForm>> = ArrayList()

    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        val holder = EffectManage()
    }

    private var mContext: Context? = null
    private var mPaterLoader: EffectLoader? = null

    fun init(context: Context) {
        mContext = context;
        mPaterLoader = EffectLoader(context)
    }

    fun initGifEffectList() {
        mRemoteData.clear()
        val form = PreviewPasterForm()
        val effectBody = EffectBody(form, true)
        mRemoteData.add(0, effectBody)
        mPaterLoader?.loadAllPaster(null) { localInfos, remoteInfos, _ ->
            for (form in localInfos!!) {
                val body = EffectBody(form, true)
                mRemoteData.add(body)
                val name: String = body.data.name
                val path: String = body.data.path
                val icon: String = body.data.icon
                Log.e("AAA", "name:$name; path:$path; icon:$icon")
            }
            for (mv in remoteInfos!!) {
                val body = EffectBody(mv, false)
                mRemoteData.add(body)
                val name: String = body.data.name
                val url: String = body.data.url
                val icon: String = body.data.icon
                Log.e("AAA", "name:$name; url:$url; icon:$icon")
            }
        }
    }

    abstract class OnGifEffectPasterCallback {
        open fun onPath(path: String) {}
    }

    private var position: Int = 0;

    fun setGifEffect(callback: OnGifEffectPasterCallback) {
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
                }

                override fun onFinish(downloadId: Int, path: String) {
                    Log.e("AAA", "download path：$path")
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

}