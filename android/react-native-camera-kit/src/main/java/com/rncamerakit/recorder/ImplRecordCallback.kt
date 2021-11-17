package com.rncamerakit.recorder

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.aliyun.common.utils.BitmapUtil
import com.aliyun.svideo.base.Constants
import com.aliyun.svideo.common.utils.ThreadUtils
import com.aliyun.svideosdk.recorder.RecordCallback
import java.io.File
import java.io.IOException

class ImplRecordCallback(private val mContext: Context) : RecordCallback {

    private var mCallbacks: OnRecorderCallbacks? = null

    fun setOnRecorderCallbacks(callback: OnRecorderCallbacks?) {
        mCallbacks = callback
    }

    override fun onComplete(validClip: Boolean, clipDuration: Long) {
        Log.e("ImplRecordCallback","onComplete")
        mCallbacks?.onComplete(validClip, clipDuration)
    }

    override fun onFinish(outputPath: String?) {
        Log.e("ImplRecordCallback","onFinish")
        mCallbacks?.onFinish(outputPath)
    }

    override fun onProgress(duration: Long) {
        Log.e("ImplRecordCallback", "onProgress:$duration")
        mCallbacks?.onProgress(duration)
    }

    override fun onMaxDuration() {
        Log.e("ImplRecordCallback", "onMaxDuration")
    }

    override fun onError(errorCode: Int) {
        Log.e("ImplRecordCallback", "errorCode:$errorCode")
        mCallbacks?.onError(errorCode)
    }

    override fun onInitReady() {
        Log.e("ImplRecordCallback", "onInitReady")
    }

    override fun onDrawReady() {
        Log.e("ImplRecordCallback", "onDrawReady")
    }

    override fun onPictureBack(bitmap: Bitmap?) {
        Log.e("ImplRecordCallback", "onPictureBack")
        ThreadUtils.runOnSubThread {
            val imgPath = File(
                Constants.SDCardConstants.getDir(mContext.applicationContext),
                System.currentTimeMillis().toString() + "-photo.jpg"
            ).absolutePath
            try {
                BitmapUtil.generateFileFromBitmap(bitmap, imgPath, "jpg")
//                if(PermissionUtils.checkPermissionsGroup(mContext, arrayOf(
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ))){
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        //适配android Q
//                        ThreadUtils.runOnSubThread {
//                            UriUtils.saveImgToMediaStore(
//                                mContext.applicationContext,
//                                imgPath
//                            )
//                        }
//                    } else {
//                        MediaScannerConnection.scanFile(
//                            mContext.applicationContext,
//                            arrayOf(imgPath),
//                            arrayOf("image/jpeg"),
//                            null
//                        )
//                    }
//                }
                if (mCallbacks != null) {
                    mCallbacks!!.onTakePhoto(imgPath)
                }
//                ThreadUtils.runOnUiThread {
////                    ToastUtils.show(mContext, "图片已保存到相册")
//                    if (mCallbacks != null) {
//                        mCallbacks!!.onTakePhoto(imgPath)
//                    }
//                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPictureDataBack(data: ByteArray?) {
        Log.e("ImplRecordCallback", "onPictureDataBack")
    }

}