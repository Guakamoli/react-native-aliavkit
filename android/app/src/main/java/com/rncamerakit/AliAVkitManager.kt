package com.rncamerakit

import android.app.Application
import android.content.Context
import android.view.View
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.recorder.util.RecordCommon
import com.liulishuo.filedownloader.FileDownloader
import com.rncamerakit.db.MusicFileInfoDao
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AliAVkitManager  {
    companion object {

        @JvmStatic
        fun init(context: Application?) {
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
    }

}