package com.rncamerakit.font

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.base.Form.FontForm
import com.aliyun.svideo.base.http.EffectService
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.common.utils.MD5Utils
import com.aliyun.svideo.downloader.DownloaderManager
import com.aliyun.svideo.downloader.FileDownloaderCallback
import com.aliyun.svideo.downloader.FileDownloaderModel
import com.aliyun.svideo.editor.contant.CaptionConfig
import com.aliyun.svideo.editor.util.AlivcResUtil
import com.aliyun.svideosdk.common.struct.project.Source
import com.blankj.utilcode.util.SPUtils
import com.facebook.react.bridge.Promise
import com.facebook.react.views.text.ReactFontManager
import com.google.gson.GsonBuilder
import com.liulishuo.filedownloader.BaseDownloadTask
import com.manwei.libs.utils.GsonManage
import com.rncamerakit.utils.DownloadUtils
import kotlinx.coroutines.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.net.URL
import kotlin.coroutines.resume

class FontManager {
    private object SingletonHolder {
        val holder = FontManager()
    }

    private var mContext: Context? = null
    fun init(context: Context) {
        mContext = context
    }

    companion object {

        var FONT_PATH = CaptionConfig.SYSTEM_FONT

        private const val EFFECT_TYPE = EffectService.EFFECT_TEXT

        public const val FONT_SP_KEY = "FONT_JSON_FILE_MD5_KEY"

        @JvmField
        val instance = SingletonHolder.holder

        //解压路径
        fun getFonPath(context: Context?, name: String, id: Int): File {
            return File(FileUtils.getFilesPath(context?.applicationContext), "downloads/fonts/$id-$name")
        }

        //下载保存路径
        fun getFontDir(context: Context): File {
            return File(FileUtils.getFilesPath(context.applicationContext), "downloads/fonts")
        }

    }

    fun initFontJson() {
        getFontJsonInfo(null)
    }

    private fun getFontJsonInfo(callback: IFontCallback?) {
        doAsync {
            val text = URL("https://static.paiyaapp.com/font/fonts.json").readText()
            val md5Text = MD5Utils.getMD5(text)
            val md5Value = SPUtils.getInstance().getString(FontManager.FONT_SP_KEY)

            if (md5Text != md5Value) {
                val fontList = GsonManage.fromJsonList(text, FontForm::class.java)
                SPUtils.getInstance().put(FontManager.FONT_SP_KEY, md5Text)
                fontList?.forEach {
                    val fontPath = FontManager.getFonPath(mContext, it.name, it.id).absolutePath
                    DownloaderManager.getInstance().dbController.saveFontModel(getFileDownloaderModel(it), fontPath)
                }
            }
            uiThread {
            }
        }
    }


    /**
     * 下载所有字体
     */
    fun downloadAllFont(context: Context) {
        val downloadFontList = getDownloadFontList();
        if (downloadFontList == null || downloadFontList.isEmpty()) {
            return
        }
        val fontPathList: MutableList<String?> = ArrayList()
        GlobalScope.launch(Dispatchers.IO) {
            val jobList: MutableList<Deferred<String?>> = ArrayList()
            downloadFontList.forEach {
                jobList.add(
                    async { downloadFont(context, it) }
                )
            }
            jobList.forEach {
                fontPathList.add("file://" + it.await())
            }
            GlobalScope.launch(Dispatchers.Main) {
                fontPathList.forEach {
                    Log.e("AAA", "下载完成2:$it")
                }
            }
        }
    }

    private fun getFileDownloaderModel(fontForm: FontForm): FileDownloaderModel {
        val model = FileDownloaderModel()
        model.effectType = EFFECT_TYPE
        model.name = fontForm.name
        model.icon = fontForm.icon
        model.id = fontForm.id
        model.level = fontForm.level
        model.sort = fontForm.sort
        model.url = fontForm.url
        model.md5 = fontForm.md5
        model.banner = fontForm.banner
        model.setIsunzip(1)
        return model
    }


    private suspend fun downloadFont(
        context: Context,
        model: FileDownloaderModel?,
    )
            : String? =
        suspendCancellableCoroutine { continuation ->
            if (model == null) {
                continuation.resume("")
                return@suspendCancellableCoroutine
            }

            DownloadUtils.downloadFont(context, model, object : FileDownloaderCallback() {
                override fun onFinish(downloadId: Int, path: String) {
                    super.onFinish(downloadId, path)
                    if (!TextUtils.isEmpty(path)) {
                        continuation.resume(path)
                    } else {
                        continuation.resume("")
                    }
                }

                override fun onProgress(downloadId: Int, soFarBytes: Long, totalBytes: Long, speed: Long, progress: Int) {
                    var progress = progress
                    super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress)
//                  Log.d("AAA", "当前下载了" + soFarBytes*1.0f/totalBytes)
                }

                override fun onError(task: BaseDownloadTask, e: Throwable) {
                    super.onError(task, e)
                    Log.e("AAA", "onError")
                    continuation.resume("")
                }
            })

        }


    /**
     * 获取所有已字体
     */
    fun getDownloadFontList(): List<FileDownloaderModel?>? {
        var fileDownloaderModels = DownloaderManager.getInstance().dbController.getResourceByType(EFFECT_TYPE)
        if (fileDownloaderModels == null) {
            fileDownloaderModels = java.util.ArrayList()
        }
//        val fileDownloaderModel = FileDownloaderModel()
//        fileDownloaderModel.icon = CaptionConfig.SYSTEM_FONT
//        fileDownloaderModel.url = CaptionConfig.SYSTEM_FONT
//        fileDownloaderModel.path = CaptionConfig.SYSTEM_FONT
//        fileDownloaderModels.add(0, fileDownloaderModel)
        fileDownloaderModels.forEach {
            it.path = it.path + "/font.ttf"
            if (isDownloadFontByUrl(it.url)) {
                it.isDbContain = 1
                it.fontName = "AliCustomFont"+it.id
                val typeface: Typeface = Typeface.createFromFile(File(it.path))
                ReactFontManager.getInstance().setTypeface(it.fontName, typeface.style, typeface)
            }
        }
        return fileDownloaderModels
    }

    /**
     * 通过URL判断该文件是否下载到了本地
     */
    private fun isDownloadFontByUrl(url: String?): Boolean {
        if (url == null) {
            return false
        }
        val path = DownloaderManager.getInstance().dbController.getPathByUrl(url)
        return (path != null && path.isNotEmpty())
    }


    /**
     * 设置字体
     */
    fun setFont(context: Context, model: FileDownloaderModel?, callback: IFontCallback?) {
        if (model == null) {
            return
        }
        if (CaptionConfig.SYSTEM_FONT == model.icon) {
            //系统字体
            val source = Source(CaptionConfig.SYSTEM_FONT)
            callback?.onFontSource(source)
        } else {
            val path = DownloaderManager.getInstance().dbController.getPathByUrl(model.url)
            if (path != null && path.isNotEmpty()) {
                val fontSource = Source("$path/font.ttf")
                fontSource.url = AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_FONT, model.id.toString())
                callback?.onFontSource(fontSource)
            } else {
                DownloadUtils.downloadFont(context, model, object : FileDownloaderCallback() {
                    override fun onFinish(downloadId: Int, path: String) {
                        super.onFinish(downloadId, path)
                        val fontSource = Source("$path/font.ttf")
                        fontSource.url = AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_FONT, model.id.toString())
                        callback?.onFontSource(fontSource)
                    }

                    override fun onProgress(downloadId: Int, soFarBytes: Long, totalBytes: Long, speed: Long, progress: Int) {
                        var progress = progress
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress)
                    }

                    override fun onError(task: BaseDownloadTask, e: Throwable) {
                        super.onError(task, e)
                    }
                })
            }

        }
    }

    fun downloadFont(context: Context?, model: FileDownloaderModel?, promise: Promise?) {
        if (model == null || context == null) {
            return
        }
        if (CaptionConfig.SYSTEM_FONT == model.url) {
            //系统字体
            promise?.resolve(GsonBuilder().create().toJson(model))
            return
        }
        DownloadUtils.downloadFont(context, model, object : FileDownloaderCallback() {
            override fun onFinish(downloadId: Int, path: String) {
                super.onFinish(downloadId, path)
//                val fontSource = Source("$path/font.ttf")
//                fontSource.url = AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_FONT, model.id.toString())
                model.path = "$path/font.ttf"
                model.isDbContain = 1
                model.fontName = "AliCustomFont"+model.id
                val typeface: Typeface = Typeface.createFromFile(File(model.path))
                ReactFontManager.getInstance().setTypeface(model.fontName, typeface.style, typeface)
                promise?.resolve(GsonBuilder().create().toJson(model))
            }

            override fun onProgress(downloadId: Int, soFarBytes: Long, totalBytes: Long, speed: Long, progress: Int) {
                var progress = progress
                super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress)
            }

            override fun onError(task: BaseDownloadTask, e: Throwable) {
                super.onError(task, e)
                promise?.reject("downloadFont", e.message)
            }
        })
    }

}