package com.rncamerakit.utils

import android.util.Log
import com.aliyun.common.utils.StorageUtils
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.rncamerakit.RNEventEmitter
import com.rncamerakit.db.MusicFileInfoDao
import java.io.File

class DownloadUtils {
    companion object {


        fun downloadMusic(context: ReactContext,songID:Int, musicUrl: String?, promise: Promise?) {
            val fileName: String? = musicUrl?.lastIndexOf("/")?.plus(1)?.let {
                musicUrl.substring(
                    it
                )
            }
            val savePath = File(
                StorageUtils.getFilesDirectory(context.applicationContext),
                "/music/download/" + songID + "_" + fileName
            ).absolutePath

            musicUrl?.let {
                downloadFile(it, savePath, object : MyFileDownloadCallback() {
                    override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                        super.progress(task, soFarBytes, totalBytes)
                        val progress = soFarBytes.toDouble() / totalBytes.toDouble() * 100
                        RNEventEmitter.downloadMusicProgress(context, progress.toInt())
                    }

                    override fun completed(task: BaseDownloadTask) {
                        super.completed(task)
                        RNEventEmitter.downloadMusicProgress(context, 100)
                        val filePath = task.targetFilePath
                        MusicFileInfoDao.instance.updateLocalPath(songID,filePath)
                        Log.e("DownloadUtils", "completed：$filePath")
                        promise?.resolve(filePath)
                    }

                    override fun error(task: BaseDownloadTask, e: Throwable) {
                        super.error(task, e)
                        promise?.reject("downloadMusic", "error：" + e.message)
                    }
                })
            }
        }

        /**
         * @param url     下载url
         * @param path    本地保存的目录
         */
        private fun downloadFile(url: String, path: String, downloadCallback: MyFileDownloadCallback?) {
            Log.e("DownloadUtils", "url:$url; path:$path")
            FileDownloader.getImpl().create(url)
                // 如果pathAsDirectory是true,path就是存储下载文件的文件目录(而不是路径)，
                // 此时默认情况下文件名filename将会默认从response#header中的contentDisposition中获得
                .setPath(path, false)
                //回调间隔 ms
                .setCallbackProgressMinInterval(100)
                .setListener(downloadCallback).start()
        }
    }

}
