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
import com.liulishuo.filedownloader.FileDownloader
import com.rncamerakit.db.MusicFileInfoDao
import org.jetbrains.anko.doAsync

class AliAVkitManager {

    companion object {
        @JvmStatic
        fun init(context: Application) {
            initVideo(context);
            MusicFileInfoDao.instance.init(context)
            //下载管理
            FileDownloader.setupOnApplicationOnCreate(context)
            doAsync {
                //提前解压
                RecordCommon.copyAll(context)
                EditorCommon.copyAll(context, View(context))
//                uiThread {
//                }
            }
        }

        private var mLogPath: String? = null
        private fun initVideo(application: Application) {
            QupaiHttpFinal.getInstance().initOkHttpFinal()
            DownloaderManager.getInstance().init(application)
            AlivcSdkCore.register(application.applicationContext)
            if (BuildConfig.DEBUG) {
                AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogWarn)
                AlivcSdkCore.setDebugLoggerLevel(AlivcSdkCore.AlivcDebugLoggerLevel.AlivcDLClose)
            } else {
                AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogDebug)
                AlivcSdkCore.setDebugLoggerLevel(AlivcSdkCore.AlivcDebugLoggerLevel.AlivcDLAll)
            }
            setSdkDebugParams(application)
            if (TextUtils.isEmpty(mLogPath)) {
                //保证每次运行app生成一个新的日志文件
                val time = System.currentTimeMillis()
                mLogPath = application.getExternalFilesDir("Log")!!.absolutePath.toString() + "/log_" + time + ".log"
                //                mLogPath = application.getExternalFilesDir("Log").getAbsolutePath() + "/log_" + time + ".log"
                AlivcSdkCore.setLogPath(mLogPath)
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