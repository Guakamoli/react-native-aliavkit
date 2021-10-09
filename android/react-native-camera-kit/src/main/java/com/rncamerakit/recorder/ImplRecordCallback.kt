package com.rncamerakit.recorder

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import com.aliyun.common.utils.BitmapUtil
import com.aliyun.svideo.base.Constants
import com.aliyun.svideo.common.utils.ThreadUtils
import com.aliyun.svideo.common.utils.ToastUtils
import com.aliyun.svideo.common.utils.UriUtils
import com.aliyun.svideosdk.recorder.RecordCallback
import java.io.File
import java.io.IOException

class ImplRecordCallback (private val mContext: Context): RecordCallback {

    private var mCallbacks: OnRecorderCallbacks? = null

    fun setOnRecorderCallbacks(callback: OnRecorderCallbacks?) {
        mCallbacks = callback
    }

    override fun onComplete(validClip: Boolean, clipDuration: Long) {
        mCallbacks!!.onComplete(validClip, clipDuration)
    }

    override fun onFinish(outputPath: String?) {
        mCallbacks!!.onFinish(outputPath)
    }

    override fun onProgress(duration: Long) {
        mCallbacks!!.onProgress(duration)
    }

    override fun onMaxDuration() {
    }

    override fun onError(errorCode: Int) {
        mCallbacks!!.onError(errorCode)
    }

    override fun onInitReady() {
    }

    override fun onDrawReady() {
    }

    override fun onPictureBack(bitmap: Bitmap?) {
        ThreadUtils.runOnSubThread {
            val imgPath =
                Constants.SDCardConstants.getDir(mContext.applicationContext) + File.separator + System.currentTimeMillis() + "-photo.jpg"
            try {
                BitmapUtil.generateFileFromBitmap(bitmap, imgPath, "jpg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //适配android Q
                    ThreadUtils.runOnSubThread {
                        UriUtils.saveImgToMediaStore(
                            mContext.applicationContext,
                            imgPath
                        )
                    }
                } else {
                    MediaScannerConnection.scanFile(
                        mContext.applicationContext,
                        arrayOf(imgPath),
                        arrayOf("image/jpeg"),
                        null
                    )
                }
                ThreadUtils.runOnUiThread {
                    ToastUtils.show(mContext, "图片已保存到相册")
                    if (mCallbacks != null) {
                        mCallbacks!!.onTakePhoto(imgPath)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPictureDataBack(data: ByteArray?) {
    }

}