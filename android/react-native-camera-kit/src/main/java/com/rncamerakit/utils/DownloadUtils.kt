package com.rncamerakit.utils

import android.util.Log
import com.liulishuo.filedownloader.FileDownloader

class DownloadUtils {
    companion object {

        /**
         * @param url     下载url
         * @param path    本地保存的目录
         */
        fun downloadFile(url: String, path: String, downloadCallback: MyFileDownloadCallback?) {
            Log.e("DownloadUtils", "url:$url; path:$path")
            //        FileDownloader.setup(context);
            FileDownloader.getImpl().create(url) // 如果pathAsDirectory是true,path就是存储下载文件的文件目录(而不是路径)，
                // 此时默认情况下文件名filename将会默认从response#header中的contentDisposition中获得
                .setPath(path, true) //回调间隔 ms
                .setCallbackProgressMinInterval(100)
                .setListener(downloadCallback).start()
        }
    }

}
