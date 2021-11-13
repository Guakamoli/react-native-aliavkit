package com.rncamerakit.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.common.utils.BitmapUtils
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.recorder.util.FixedToastUtils
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory
import com.aliyun.svideosdk.common.struct.common.MediaType
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.common.struct.common.VideoQuality
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.crop.CropCallback
import com.aliyun.svideosdk.crop.CropParam
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator
import com.duanqu.transcode.NativeParser
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.google.gson.GsonBuilder
import com.rncamerakit.RNEventEmitter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume


class CropManager {
    companion object {

        private const val TAG = "CropManager"


        /**
         * 图片裁剪
         */
        fun cropImage(reactContext: ReactContext?, readableMap: ReadableMap, promise: Promise) {

            val context = reactContext?.applicationContext

            var imagePath =
                if (readableMap.hasKey("source")) readableMap.getString("source") else ""
            if (TextUtils.isEmpty(imagePath)) {
                promise.reject("cropImager", "error: imagePath is empty")
                return
            }
            imagePath =
                com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(imagePath)).absolutePath
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val width = bitmap.width
            val height = bitmap.height

            val outputWidth =
                if (readableMap.hasKey("cropWidth")) readableMap.getInt("cropWidth") else width
            val outputHeight =
                if (readableMap.hasKey("cropHeight")) readableMap.getInt("cropHeight") else height

            val startX =
                if (readableMap.hasKey("cropOffsetX")) readableMap.getInt("cropOffsetX") else 0
            val startY =
                if (readableMap.hasKey("cropOffsetY")) readableMap.getInt("cropOffsetY") else 0
            val endX = startX + outputWidth
            val endY = startY + outputHeight

            val aliyunCrop = AliyunCropCreator.createCropInstance(context)

            val file = File(imagePath)
            val fileName = "crop_" + file.name
            val pathDis =
                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator
            val outputPath = FileUtils.createFile(pathDis, fileName).path

            //设置裁剪参数
            val param = CropParam()
            param.mediaType = MediaType.ANY_IMAGE_TYPE
//            param.scaleMode = VideoDisplayMode.FILL
            param.inputPath = imagePath
            param.outputPath = outputPath

            //裁剪矩阵
            param.cropRect = Rect(startX, startY, endX, endY)

            param.outputWidth = outputWidth
            param.outputHeight = outputHeight

            //视频编码方式
            param.videoCodec = VideoCodecs.H264_HARDWARE
            //填充颜色
            param.fillColor = Color.WHITE

            aliyunCrop.setCropParam(param)

            aliyunCrop.setCropCallback(object : CropCallback {
                override fun onProgress(progress: Int) {
                    Log.e(TAG, "progress：$progress")
                    RNEventEmitter.startVideoCrop(reactContext, progress)
                }

                override fun onError(code: Int) {
                    Log.e(TAG, "onError：$code")
                    promise.reject("cropImager", "onError:$code")
                }

                override fun onComplete(duration: Long) {
                    Log.e(TAG, "onComplete：$duration; $outputPath")
                    RNEventEmitter.startVideoCrop(reactContext, 100)
                    aliyunCrop.dispose()
                    promise.resolve(outputPath)
                }

                override fun onCancelComplete() {
                    Log.e(TAG, "onCancelComplete")
                    aliyunCrop.dispose()
                }
            })
            aliyunCrop.startCrop()
        }


        /**
         * 视频裁剪
         */
        fun cropVideo(reactContext: ReactContext?, readableMap: ReadableMap, promise: Promise) {

            val context = reactContext?.applicationContext

            var videoPath =
                if (readableMap.hasKey("source")) readableMap.getString("source") else ""
            if (TextUtils.isEmpty(videoPath)) {
                promise.reject("cropVideo", "error: videoPath is empty")
                return
            }

            videoPath =
                com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(videoPath)).absolutePath

            var mVideoWidth = 720
            var mVideoHeight = 1280
            try {
                val nativeParser = NativeParser()
                nativeParser.init(videoPath)
                try {
                    mVideoWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toInt()
                    mVideoHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                nativeParser.release()
                nativeParser.dispose()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val outputWidth =
                if (readableMap.hasKey("cropWidth")) readableMap.getInt("cropWidth") else mVideoWidth
            val outputHeight =
                if (readableMap.hasKey("cropHeight")) readableMap.getInt("cropHeight") else mVideoHeight

            val startX =
                if (readableMap.hasKey("cropOffsetX")) readableMap.getInt("cropOffsetX") else 0
            val startY =
                if (readableMap.hasKey("cropOffsetY")) readableMap.getInt("cropOffsetY") else 0
            val endX = startX + outputWidth
            var endY = startY + outputHeight


            //默认1：1 如果裁剪宽度大于视频宽度，会导致视频变形
            //例如： 原视频宽高：720*1280 ，裁剪输出宽高：1000 * 1000；最终视频输出宽高1000*1000，但是视频范围是原视频的 宽：0~720 高：0~1000
            if (outputWidth > mVideoWidth) {
                endY -= (outputWidth - mVideoWidth);
            }

            val aliyunCrop = AliyunCropCreator.createCropInstance(context)
            val duration = aliyunCrop.getVideoDuration(videoPath)

            val startTime =
                if (readableMap.hasKey("startTime")) readableMap.getInt("startTime")*1000 else 0
            val endTime =
                if (readableMap.hasKey("endTime")) readableMap.getInt("endTime")*1000 else duration

            val file = File(videoPath)
            val fileName = "crop_" + file.name
            val pathDis =
                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator
            val outputPath = FileUtils.createFile(pathDis, fileName).path

            //设置裁剪参数
            val param = CropParam()
            //10mbit/s
            param.setVideoBitrate(10*1000)
            param.mediaType = MediaType.ANY_VIDEO_TYPE
            param.scaleMode = VideoDisplayMode.SCALE
            //填充颜色
            param.fillColor = Color.BLUE

            param.inputPath = videoPath
            param.outputPath = outputPath

            //裁剪矩阵
            param.cropRect = Rect(startX, startY, endX, endY)
            param.outputWidth = outputWidth
            param.outputHeight = outputHeight

            //startTime 单位：us
            param.startTime = startTime.toLong()
            param.endTime = endTime.toLong()

            param.quality = VideoQuality.SSD
            param.gop = 15
            param.frameRate = 30
            param.crf = 23
            //视频编码方式
            param.videoCodec = VideoCodecs.H264_HARDWARE


            aliyunCrop.setCropParam(param)
            aliyunCrop.setCropCallback(object : CropCallback {
                override fun onProgress(progress: Int) {
                    Log.e(TAG, "progress：$progress")
                    RNEventEmitter.startVideoCrop(reactContext, progress)
                }

                override fun onError(code: Int) {
                    Log.e(TAG, "onError：$code")
                    promise.reject("cropVideo", "onError:$code")
                }

                override fun onComplete(duration: Long) {
                    Log.e(TAG, "onComplete：$duration; $outputPath")
                    RNEventEmitter.startVideoCrop(reactContext, 100)
                    aliyunCrop.dispose()

                    try {
                        val nativeParser = NativeParser()
                        nativeParser.init(outputPath)

                        val videoWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toInt()
                        val videoHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toInt()

                        nativeParser.release()
                        nativeParser.dispose()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    promise.resolve(outputPath)
                }

                override fun onCancelComplete() {
                    Log.e(TAG, "onCancelComplete")
                    aliyunCrop.dispose()
                }
            })
            aliyunCrop.startCrop()

        }


        /**
         * 视频抽帧
         */
        fun corpVideoFrame(context: Context, options: ReadableMap, promise: Promise?) {
            val videoPath =
                if (options.hasKey("videoPath")) options.getString("videoPath")
                else null
            if (TextUtils.isEmpty(videoPath)) {
                promise?.reject("corpVideoFrame", "error: videoPath is empty")
                return
            }
            //ms
            val intervalTime =
                if (options.hasKey("intervalTime")) options.getInt("intervalTime") else 1000
            val startTime = if (options.hasKey("startTime")) options.getInt("startTime") else 0
            val videoWidth = if (options.hasKey("videoWidth")) options.getInt("videoWidth") else 200
            val videoHeight =
                if (options.hasKey("videoHeight")) options.getInt("videoHeight") else 200
            val cacheSize = if (options.hasKey("cacheSize")) options.getInt("cacheSize") else 10

            var duration = 0L
            try {
                val nativeParser = NativeParser()
                nativeParser.init(videoPath)
                try {
                    duration = nativeParser.getValue(NativeParser.VIDEO_DURATION).toLong()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                nativeParser.release()
                nativeParser.dispose()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val coverTimes: MutableList<Long> = ArrayList()
            for (i in 0 until cacheSize) {
                var coverTime: Int = intervalTime*i + startTime
                if (coverTime > duration) {
                    coverTime = duration.toInt()
                }
                coverTimes.add(coverTime.toLong())
            }
            getVideoFrame(context, videoPath, videoWidth, videoHeight, coverTimes, promise)
        }


        private fun getVideoFrame(
            context: Context,
            videoPath: String?,
            videoWidth: Int,
            videoHeight: Int,
            coverTimes: List<Long>,
            promise: Promise?
        ) {
            val videoFrameList: MutableList<String?> = ArrayList()
            GlobalScope.launch(Dispatchers.IO) {
                val jobList: MutableList<Deferred<String?>> = ArrayList()
                coverTimes.forEach {
                    jobList.add(async { getJoinedGroupList(context, videoPath, videoWidth, videoHeight, it) })
                }
                jobList.forEach {
                    videoFrameList.add(it.await())
                }
                GlobalScope.launch(Dispatchers.Main) {
                    videoFrameList.forEach {
                        Log.e("BBB ", "sync：$it")
                        promise?.resolve(GsonBuilder().create().toJson(videoFrameList))
                    }
                }
            }
        }

        private suspend fun getJoinedGroupList(context: Context, videoPath: String?, videoWidth: Int, videoHeight: Int, time: Long): String? =
            suspendCancellableCoroutine { continuation ->
                val thumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher()
                thumbnailFetcher.addVideoSource(videoPath, 0, Int.MAX_VALUE.toLong(), 0)
                thumbnailFetcher.setParameters(videoWidth, videoHeight, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1)

                thumbnailFetcher.requestThumbnailImage(longArrayOf(time), object : AliyunIThumbnailFetcher.OnThumbnailCompletion {
                    override fun onThumbnailReady(bitmap: Bitmap, longTime: Long, index: Int) {
                        if (!bitmap.isRecycled) {
                            var videoFramePath =
                                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator + "videoFrame" + File.separator
                            val name = File(videoPath).nameWithoutExtension
                            videoFramePath = FileUtils.createFile(
                                videoFramePath,
                                "VideoFrame-$name-$longTime.jpg"
                            ).path
                            BitmapUtils.saveBitmap(bitmap, videoFramePath)
                            Log.e("BBB ", "Async：$videoFramePath")
                            if (!TextUtils.isEmpty(videoFramePath)) {
                                continuation.resume(videoFramePath)
                            } else {
                                continuation.resume("")
                            }
                        }
                    }

                    override fun onError(errorCode: Int) {
                        continuation.resume("")
                    }
                })
            }


        fun getVideoFrame(context: Context, videoPath: String, longTime: Long): String? {
            var bitmap: Bitmap? = null
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoPath)
                bitmap =
                    retriever.getFrameAtTime(longTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                retriever.release()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            var videoFramePath =
                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator + "videoFrame" + File.separator
            val name = File(videoPath).nameWithoutExtension
            videoFramePath = FileUtils.createFile(
                videoFramePath,
                "VideoFrame-$name-$longTime.jpg"
            ).path
            BitmapUtils.saveBitmap(bitmap, videoFramePath)
            return videoFramePath
        }

    }

}