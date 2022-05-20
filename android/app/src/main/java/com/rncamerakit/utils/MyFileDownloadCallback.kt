package com.rncamerakit.utils

import com.blankj.utilcode.util.LogUtils
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener


/**
 * pending -> started -> connected -> (progress <->progress) -> blockComplete -> completed
 *
 * paused / completed / error / warn
 */
open class MyFileDownloadCallback : FileDownloadListener() {

    override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
        LogUtils.e("DownloadUtils", "pending:$soFarBytes/$totalBytes")
    }

    override fun started(task: BaseDownloadTask) {
        super.started(task)
    }

    override fun connected(
        task: BaseDownloadTask,
        etag: String,
        isContinue: Boolean,
        soFarBytes: Int,
        totalBytes: Int
    ) {
        LogUtils.e("DownloadUtils", "connected:$soFarBytes/$totalBytes")
    }

    public override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
        LogUtils.e(
            "DownloadUtils",
            "progress:" + soFarBytes.toFloat() / totalBytes.toFloat() + " ~ " + soFarBytes + "/" + totalBytes
        )
    }

    override fun blockComplete(task: BaseDownloadTask) {
        LogUtils.e("DownloadUtils", "blockComplete:" + task.targetFilePath)
    }

    override fun retry(
        task: BaseDownloadTask,
        ex: Throwable,
        retryingTimes: Int,
        soFarBytes: Int
    ) {
    }

    public override fun completed(task: BaseDownloadTask) {
        LogUtils.e("DownloadUtils", "completed:" + task.targetFilePath)
    }

    override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

    public override fun error(task: BaseDownloadTask, e: Throwable) {}

    override fun warn(task: BaseDownloadTask) {}

}
