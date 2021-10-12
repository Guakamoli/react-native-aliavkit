package com.rncamerakit.compose

import android.content.Context
import android.graphics.Bitmap
import com.aliyun.svideo.base.Constants
import com.aliyun.svideo.common.utils.DateTimeUtils
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.common.utils.ToastUtils
import com.aliyun.svideo.editor.R
import com.aliyun.svideosdk.common.AliyunErrorCode
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.editor.AliyunIComposeCallBack
import com.aliyun.svideosdk.editor.AliyunIVodCompose
import com.aliyun.svideosdk.editor.impl.AliyunComposeFactory
import com.duanqu.transcode.NativeParser
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.RNEventEmitter
import java.io.FileOutputStream
import java.io.IOException

class ComposeManager(private val mContext: ThemedReactContext) {

    private var mVodCompose: AliyunIVodCompose? = null
    private var mOutputPath = ""

    init {
        mVodCompose = AliyunComposeFactory.createAliyunVodCompose();
        mVodCompose?.init(mContext.applicationContext)

    }

    private fun getCoverImager(promise: Promise?) {
        var mVideoWidth = 720
        var frameHeight = 1280
        try {
            val nativeParser = NativeParser()
            nativeParser.init(mOutputPath)
            try {
                mVideoWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toInt()
                frameHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            nativeParser.release()
            nativeParser.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val aliyunIThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher()
        aliyunIThumbnailFetcher.addVideoSource(mOutputPath, 0, Int.MAX_VALUE.toLong(), 0)
        aliyunIThumbnailFetcher.setParameters(
            mVideoWidth,
            frameHeight,
            AliyunIThumbnailFetcher.CropMode.Mediate,
            VideoDisplayMode.SCALE,
            1
        )
        aliyunIThumbnailFetcher.requestThumbnailImage(longArrayOf(0), object :
            AliyunIThumbnailFetcher.OnThumbnailCompletion {
            override fun onThumbnailReady(frameBitmap: Bitmap?, time: Long) {
                val timeMillis =
                    DateTimeUtils.getDateTimeFromMillisecond(System.currentTimeMillis())

                val thumbnailPath = FileUtils.createFile(
                    Constants.SDCardConstants.getDir(mContext),
                    "thumbnail-$timeMillis.jpg"
                ).path

                var fileOutputStream: FileOutputStream? = null
                try {
                    fileOutputStream = FileOutputStream(thumbnailPath)
                    frameBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                } catch (e: java.lang.Exception) {
                    ToastUtils.show(mContext, R.string.alivc_editor_cover_fetch_cover_error)
                    promise?.reject("exportImage", "errorCode:${e.message}")
                    return
                } finally {
                    try {
                        fileOutputStream?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                promise?.resolve(thumbnailPath)
            }

            override fun onError(errorCode: Int) {
                promise?.reject("exportImage", "errorCode:$errorCode")
            }

        })
    }

    fun startCompose(configPath: String?, promise: Promise?, isVideo: Boolean) {
        val time = DateTimeUtils.getDateTimeFromMillisecond(System.currentTimeMillis())
        mOutputPath =
            Constants.SDCardConstants.getDir(mContext) + time + Constants.SDCardConstants.COMPOSE_SUFFIX

        val errorCode: Int? =
            mVodCompose?.compose(configPath, mOutputPath, object : AliyunIComposeCallBack {
                override fun onComposeError(errorCode: Int) {
                    if (isVideo) {
                        promise?.reject("exportVideo", "errorCode:$errorCode")
                    } else {
                        promise?.reject("exportImage", "errorCode:$errorCode")
                    }
                }

                override fun onComposeProgress(progress: Int) {
                    RNEventEmitter.startVideoCompose(mContext, progress)
                }

                override fun onComposeCompleted() {
                    if (isVideo) {
                        promise?.resolve(mOutputPath)
                    } else {
                        getCoverImager(promise)
                    }
                }
            })

        if (errorCode != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            //合成失败
            if (isVideo) {
                promise?.reject("exportVideo", "errorCode:$errorCode")
            } else {
                promise?.reject("exportImage", "errorCode:$errorCode")
            }
            return
        }
    }

    fun onRelease() {
        mVodCompose?.cancelCompose();
        mVodCompose?.release();
    }

}