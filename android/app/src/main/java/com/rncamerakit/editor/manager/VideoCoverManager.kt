package com.rncamerakit.editor.manager

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.aliyun.svideo.base.Constants
import com.aliyun.svideo.common.utils.BitmapUtils
import com.aliyun.svideo.common.utils.DateTimeUtils
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.facebook.react.bridge.Promise
import com.google.gson.GsonBuilder
import com.rncamerakit.crop.CropManager
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resume

class VideoCoverManager {

    companion object {

        fun getVideoCover(
            context: Context,
            videoPath: String,
            coverWidth: Int,
            coverHeight: Int,
            seekTime: Long,
            promise: Promise?
        ) {
            val thumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher()
            thumbnailFetcher.addVideoSource(videoPath, 0, Int.MAX_VALUE.toLong(), 0)
            thumbnailFetcher.setParameters(
                coverWidth, coverHeight, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1
            )
            val timeName = System.currentTimeMillis().toString()
            thumbnailFetcher.requestThumbnailImage(longArrayOf(seekTime), object : AliyunIThumbnailFetcher.OnThumbnailCompletion {
                override fun onThumbnailReady(bitmap: Bitmap?, longTime: Long, index: Int) {
                    if (bitmap != null && !bitmap.isRecycled) {
                        val videoFramePath = FileUtils.createFile(
                            CropManager.getVideoCoverDirs(context),
                            "VideoCover-$timeName-$longTime.jpg"
                        ).absolutePath
                        BitmapUtils.saveBitmap(bitmap, videoFramePath)
                        bitmap.recycle()
                        val videoFrameList: MutableList<String?> = ArrayList()
                        videoFrameList.add(videoFramePath)
                        promise?.resolve(GsonBuilder().create().toJson(videoFrameList))
                    } else {
                        promise?.reject("getVideoCover", "cover is null")
                    }
                }

                override fun onError(errorCode: Int) {
                    promise?.reject("getVideoCover", "errorCode:${errorCode}")
                }
            })
        }

        fun getVideoCover(
            context: Context,
            projectConfigure: String?,
            _seekTime: Long,
            promise: Promise?
        ) {

            val mVideoWidth = 720
            val mVideoHeight = 1280

            val thumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher()

            thumbnailFetcher.fromConfigJson(projectConfigure)
            thumbnailFetcher.setParameters(
                mVideoWidth,
                mVideoHeight,
                AliyunIThumbnailFetcher.CropMode.Mediate,
                VideoDisplayMode.SCALE,
                1
            )

            var seekTime = _seekTime
            val duration = thumbnailFetcher.totalDuration
            if (seekTime > duration) {
                seekTime = duration
            }

            thumbnailFetcher.requestThumbnailImage(longArrayOf(seekTime), object :
                AliyunIThumbnailFetcher.OnThumbnailCompletion {
                override fun onThumbnailReady(frameBitmap: Bitmap?, time: Long, index: Int) {
                    val timeMillis =
                        DateTimeUtils.getDateTimeFromMillisecond(System.currentTimeMillis())
                    val thumbnailPath = FileUtils.createFile(
                        Constants.SDCardConstants.getDir(context),
                        "VideoCover-$timeMillis.jpg"
                    ).path
                    var fileOutputStream: FileOutputStream? = null
                    try {
                        fileOutputStream = FileOutputStream(thumbnailPath)
                        frameBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    } catch (e: java.lang.Exception) {
                        promise?.reject("getVideoCover", "errorCode:${e.message}")
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
                    promise?.reject("getVideoCover", "errorCode:$errorCode")
                }
            })

        }

    }
}