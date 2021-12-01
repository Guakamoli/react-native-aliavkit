package com.rncamerakit.font

import android.content.Context
import android.util.Log
import com.aliyun.svideo.base.Form.FontForm
import com.aliyun.svideo.base.http.EffectService
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.common.utils.MD5Utils
import com.aliyun.svideo.downloader.DownloaderManager
import com.aliyun.svideo.downloader.FileDownloaderCallback
import com.aliyun.svideo.downloader.FileDownloaderModel
import com.blankj.utilcode.util.SPUtils
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.util.FileDownloadUtils
import com.manwei.libs.utils.GsonManage
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.net.URL

class FontManager {
    companion object {

        private const val FONT_SP_KEY = "FONT_JSON_FILE_MD5_KEY"

        @JvmField
        val instance = SingletonHolder.holder


        fun getFonPath(context: Context, name: String, id: Int): File {
            return File(
                FileUtils.getFilesPath(context.applicationContext),
//                "downloads/fonts"
                "downloads/fonts/$id-$name"
            )
        }
        fun getFontDir(context: Context): File {
            return File(
                FileUtils.getFilesPath(context.applicationContext),
//                "downloads/fonts"
                "downloads/fonts"
            )
        }

    }

    private object SingletonHolder {
        val holder = FontManager()
    }


    var mFontList: List<FontForm>? = null


    /**
     *
     */
    fun getFountList(): List<FontForm>? {
        return mFontList
    }

    fun initFontJson() {
        getFontJsonInfo(null)
    }

    private fun getFontJsonInfo(callback: IFontCallback?) {
        doAsync {
            val text = URL("https://static.paiyaapp.com/font/fonts.json").readText()
            val md5Text = MD5Utils.getMD5(text)
            val md5Value = SPUtils.getInstance().getString(FontManager.FONT_SP_KEY)

            mFontList = GsonManage.fromJsonList(text, FontForm::class.java)

            if (md5Text != md5Value) {
                SPUtils.getInstance().put(FontManager.FONT_SP_KEY, md5Text)
            }

            uiThread {
                callback?.onFontJsonInfo(mFontList)
            }
        }
    }


    /**
     * 下载所有字体
     */
    fun downloadAllFont(context: Context) {
        if (mFontList == null || mFontList?.isEmpty() == true) {
            return
        }
        Log.e("AAA", "downloadAllFont:" + mFontList?.size)
        val fontPathList: MutableList<String?> = ArrayList()
//        GlobalScope.launch(Dispatchers.IO) {
//            val jobList: MutableList<Deferred<String?>> = ArrayList()
//            mFontList?.forEach {
//
//                jobList.add(
//                    async { downloadFont(it) }
//                )
//            }
//            jobList.forEach {
//                fontPathList.add("file://" + it.await())
//            }
//            GlobalScope.launch(Dispatchers.Main) {
//                fontPathList.forEach {
//                    Log.e("AAA", "下载完成2:$it")
//                }
//            }
//        }
        mFontList?.forEach {
            downloadFont(context, it)
        }
    }


    private fun downloadFont(
        context: Context,
        fontForm: FontForm,
    ) {
//    : String? =
//        suspendCancellableCoroutine { continuation ->

        val model = FileDownloaderModel()
        model.effectType = EffectService.EFFECT_TEXT
        model.name = fontForm.name
        model.icon = fontForm.icon
        model.id = fontForm.id
        model.level = fontForm.level
        model.sort = fontForm.sort
        model.url = fontForm.url
        model.md5 = fontForm.md5
        model.banner = fontForm.banner
        model.setIsunzip(1)

        //下载完后的解压路径
        val fontPath = getFonPath(context, model.name, model.id)
        model.path = fontPath.absolutePath

        //下载文件的路径，重新设置一次
        FileDownloadUtils.setDefaultSaveRootPath(getFontDir(context).absolutePath)

        DownloaderManager.getInstance().getDbController()
        val task = DownloaderManager.getInstance().addTask(model, model.url)
        Log.d("AAA", "Downloader start:" + task.path)
        DownloaderManager.getInstance().startTask(task.taskId, object : FileDownloaderCallback() {
            override fun onFinish(downloadId: Int, path: String) {
                super.onFinish(downloadId, path)
                Log.e("AAA", "onFinish")
            }

            override fun onProgress(downloadId: Int, soFarBytes: Long, totalBytes: Long, speed: Long, progress: Int) {
                var progress = progress
                super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress)
                Log.d("AAA", "当前下载了" + soFarBytes*1.0f/totalBytes)
            }

            override fun onError(task: BaseDownloadTask, e: Throwable) {
                super.onError(task, e)
                Log.e("AAA", "onError")
            }
        })

//        //必须重新设置下载保存目录，为 downloadDirFile 的 父目录
//        FileDownloadUtils.setDefaultSaveRootPath(fontPath.absolutePath)
//        val fileName: String? = model.url?.lastIndexOf("/")?.plus(1)?.let {
//            model.url.substring(
//                it
//            )
//        }
//        model.path = fontPath.absolutePath + "/$fileName"
//        Log.e("AAA", "model.path:" + model.path)
//        DownloadUtils.downloadFile(model.url, model.path, object : MyFileDownloadCallback() {
//            override fun progress(
//                task: BaseDownloadTask,
//                soFarBytes: Int,
//                totalBytes: Int
//            ) {
//                super.progress(task, soFarBytes, totalBytes)
//                val progress = soFarBytes.toDouble()/totalBytes.toDouble()*100
//                Log.e("AAA", "progress:$progress")
//            }
//
//            override fun completed(task: BaseDownloadTask) {
//                super.completed(task)
//                val filePath = task.targetFilePath
//                Log.e("AAA", "下载完成:$filePath")
//            }
//
//            override fun error(task: BaseDownloadTask, e: Throwable) {
//                super.error(task, e)
//                Log.e("AAA", "error")
//            }
//        })
    }


}