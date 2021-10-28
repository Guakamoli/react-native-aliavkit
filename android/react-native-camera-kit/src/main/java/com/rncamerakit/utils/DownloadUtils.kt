package com.rncamerakit.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.aliyun.common.utils.StorageUtils
import com.blankj.utilcode.util.LogUtils
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.rncamerakit.RNEventEmitter
import com.rncamerakit.db.MusicFileInfoDao
import java.io.File

class DownloadUtils {
    companion object {

        fun downloadMusic(
            context: ReactContext,
            songID: Int,
            musicUrl: String?,
            promise: Promise?,
            callback:MyFileDownloadCallback?
        ) {
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
                    override fun progress(
                        task: BaseDownloadTask,
                        soFarBytes: Int,
                        totalBytes: Int
                    ) {
                        super.progress(task, soFarBytes, totalBytes)
                        val progress = soFarBytes.toDouble() / totalBytes.toDouble() * 100
                        RNEventEmitter.downloadMusicProgress(context, progress.toInt())
                    }

                    override fun completed(task: BaseDownloadTask) {
                        super.completed(task)
                        RNEventEmitter.downloadMusicProgress(context, 100)
                        val filePath = task.targetFilePath
                        val duration = getAudioDuration(context.applicationContext, filePath)
                        MusicFileInfoDao.instance.updateLocalPath(songID, filePath, duration)
                        LogUtils.e("DownloadUtils", "completed：$filePath")
                        callback?.completed(task)
                        promise?.resolve(filePath)
                    }

                    override fun error(task: BaseDownloadTask, e: Throwable) {
                        super.error(task, e)
                        promise?.reject("downloadMusic", "error：" + e.message)
                    }
                })
            }
        }

        private fun getAudioDuration(context: Context, uriString: String): Int {
            var audioTime = 0
            val mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(context, Uri.parse(uriString))
                mediaPlayer.prepare();
                audioTime = mediaPlayer.duration
            } catch (e: Exception) {
                e.printStackTrace();
            } finally {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
            return audioTime
        }

        /**
         * @param url     下载url
         * @param path    本地保存的目录
         */
        private fun downloadFile(
            url: String,
            path: String,
            downloadCallback: MyFileDownloadCallback?
        ) {
            LogUtils.e("DownloadUtils", "url:$url; path:$path")
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