package com.rncamerakit.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.aliyun.svideo.common.utils.MD5Utils
import com.aliyun.svideo.downloader.DownloaderManager
import com.aliyun.svideo.downloader.FileDownloaderCallback
import com.aliyun.svideo.downloader.FileDownloaderModel
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.util.FileDownloadUtils
import com.manwei.libs.utils.GsonManage
import com.rncamerakit.RNAliavkitEventEmitter
import com.rncamerakit.db.MusicFileBaseInfo
import com.rncamerakit.db.MusicFileInfoDao
import com.rncamerakit.font.FontManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.net.URL

class DownloadUtils {
    companion object {

        /**
         * 下载字体
         */
        fun downloadFont(context: Context, model: FileDownloaderModel, callback: FileDownloaderCallback?) {
            //下载完后的解压路径
            val fontPath = FontManager.getFonPath(context, model.name, model.id)
            model.path = fontPath.absolutePath
            //下载文件的路径，重新设置一次
            FileDownloadUtils.setDefaultSaveRootPath(FontManager.getFontDir(context).absolutePath)
            val task = DownloaderManager.getInstance().addTask(model, model.url)
            DownloaderManager.getInstance().startTask(task.taskId, callback)
        }

        fun downloadMusic(
            context: ReactContext,
            songID: String?,
            musicUrl: String?,
            promise: Promise?,
            callback: MyFileDownloadCallback?
        ) {
            val fileName: String? = musicUrl?.lastIndexOf("/")?.plus(1)?.let {
                musicUrl.substring(
                    it
                )
            }
            val savePath = File(
                com.aliyun.svideo.common.utils.FileUtils.getFilesPath(context.applicationContext),
                "/downloads/music/" + songID + "_" + fileName
            ).absolutePath

            musicUrl?.let {
                downloadFile(it, savePath, object : MyFileDownloadCallback() {
                    override fun progress(
                        task: BaseDownloadTask,
                        soFarBytes: Int,
                        totalBytes: Int
                    ) {
                        super.progress(task, soFarBytes, totalBytes)
                        val progress = soFarBytes.toDouble()/totalBytes.toDouble()*100
                        RNAliavkitEventEmitter.downloadMusicProgress(context, progress.toInt())
                    }

                    override fun completed(task: BaseDownloadTask) {
                        super.completed(task)
                        RNAliavkitEventEmitter.downloadMusicProgress(context, 100)
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


        fun getMusicJsonInfo() {
            getMusicJsonInfo(null)
        }

        private const val spKey = "MUSIC_JSON_FILE_MD5_KEY"

        fun getMusicJsonInfo(callback: Callback?) {
            doAsync {
                val text = URL("https://static.paiyaapp.com/music/songs.json").readText()
                val md5Text = MD5Utils.getMD5(text)

                var baseInfo: MusicFileBaseInfo? = null

                val md5Value = SPUtils.getInstance().getString(spKey)
                if (md5Text != md5Value) {
                    baseInfo = GsonManage.fromJson(text, MusicFileBaseInfo::class.java)
                    MusicFileInfoDao.instance.insertList(baseInfo?.songs)
                    SPUtils.getInstance().put(spKey, md5Text)
                }
                uiThread {
//                    if (md5Text != md5Value) {
//                        val baseInfo: MusicFileBaseInfo =GsonManage.fromJson(text, MusicFileBaseInfo::class.java)
//                        MusicFileInfoDao.instance.insertList(baseInfo.songs)
//                        SPUtils.getInstance().put(spKey, md5Text)
//                        callback?.invoke(baseInfo.songs)
//                    }
                    if (md5Text != md5Value) {
                        callback?.invoke(baseInfo?.songs)
                    }
                }
            }
        }


    }

}
