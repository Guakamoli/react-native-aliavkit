package com.rncamerakit

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.View
import com.aliyun.common.httpfinal.QupaiHttpFinal
import com.aliyun.svideo.base.http.EffectService
import com.aliyun.svideo.base.ui.SdkVersionActivity
import com.aliyun.svideo.downloader.DownloaderManager
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.recorder.util.RecordCommon
import com.aliyun.sys.AlivcSdkCore
import com.blankj.utilcode.util.SPUtils
import com.liulishuo.filedownloader.FileDownloader
import com.rncamerakit.db.MusicFileInfoDao
import com.rncamerakit.font.FontManager
import com.rncamerakit.recorder.manager.EffectPasterManage
import com.rncamerakit.utils.DownloadUtils
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AliAVkitManager {

    companion object {
        @JvmStatic
        fun init(context: Application) {
            initVideo(context);
            //下载初始化
            DownloaderManager.getInstance().init(context)
            //下载管理
            FileDownloader.setupOnApplicationOnCreate(context)
            //初始化贴纸管理
            EffectPasterManage.instance.init(context)
            //音乐库初始化
            MusicFileInfoDao.instance.init(context)
            DownloadUtils.getMusicJsonInfo()
            //字体
            FontManager.instance.init(context)


//            //TODO
//            SPUtils.getInstance().put(FontManager.FONT_SP_KEY, "")
//            //清空字体库数据
//            val fonts = FontManager.instance.getDownloadFontList()
//            fonts?.forEach { font ->
//                font?.let {
//                    DownloaderManager.getInstance().dbController.deleteTask(it.taskId)
//                }
//            }
//            //TODO

            doAsync {
                //提前解压
                RecordCommon.copyAll(context)
                EditorCommon.copyAll(context, View(context))
                uiThread {
                    //提前下载字体库json数据,将json数据保存到本地数据库
                    FontManager.instance.initFontJson()
                }
            }
        }

        private var mLogPath: String? = null
        private fun initVideo(application: Application) {
            QupaiHttpFinal.getInstance().initOkHttpFinal()

            DownloaderManager.getInstance().init(application)
//            DownloaderManager.getInstance().dbController

            AlivcSdkCore.register(application.applicationContext)
//            if (BuildConfig.DEBUG) {
//                AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogWarn)
//                AlivcSdkCore.setDebugLoggerLevel(AlivcSdkCore.AlivcDebugLoggerLevel.AlivcDLClose)
//            } else {
//                AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogDebug)
//                AlivcSdkCore.setDebugLoggerLevel(AlivcSdkCore.AlivcDebugLoggerLevel.AlivcDLAll)
//            }
            setSdkDebugParams(application)
            if (TextUtils.isEmpty(mLogPath)) {
                //保证每次运行app生成一个新的日志文件
                val time = System.currentTimeMillis()
                mLogPath = application.getExternalFilesDir("Log")!!.absolutePath.toString() + "/log_" + time + ".log"
                //                mLogPath = application.getExternalFilesDir("Log").getAbsolutePath() + "/log_" + time + ".log"
//                AlivcSdkCore.setLogPath(mLogPath)
            }

            EffectService.setAppInfo(
                application.resources.getString(R.string.app_name),
                application.packageName,
                getVersion(application),
                getVersionCode(application)
            )
        }

        private fun setSdkDebugParams(application: Application) {
            //Demo 调试用，外部客户请勿使用
            val mySharedPreferences = application.getSharedPreferences(
                SdkVersionActivity.DEBUG_PARAMS,
                Activity.MODE_PRIVATE
            )
            val hostType = mySharedPreferences.getInt(SdkVersionActivity.DEBUG_DEVELOP_URL, 0)
            //AlivcSdkCore.setDebugHostType(hostType);
        }


        private fun getVersion(context: Context): String {
            return try {
                val pi = context.packageManager.getPackageInfo(context.packageName, 0)
                pi.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                "1.0.0"
            }
        }

        private fun getVersionCode(context: Context): Long {
            return try {
                val pi = context.packageManager.getPackageInfo(context.packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pi.longVersionCode
                } else {
                    pi.versionCode.toLong()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                0
            }
        }

    }


}