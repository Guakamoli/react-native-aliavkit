package com.rncamerakit.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.common.utils.BitmapUtils
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.editor.Transcoder
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
import com.rncamerakit.VideoConst
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume


class CropManager {
    companion object {


        fun getMediaCacheDirs(context: Context): String {
            return FileUtils.getDiskCachePath(context) + File.separator + "media" + File.separator
        }

        fun getCameraDirs(context: Context): String {
            return FileUtils.getDiskCachePath(context) + File.separator + "media/camera" + File.separator
        }

        /**
         * 编辑目录
         */
        fun getEditorDirs(context: Context): String {
            return FileUtils.getDiskCachePath(context) + File.separator + "media/editor" + File.separator
        }

        /**
         * 视频帧提取目录
         */
        fun getVideoFrameDirs(context: Context): String {
            return FileUtils.getDiskCachePath(context) + File.separator + "media/videoFrame" + File.separator
        }

        private const val TAG = "CropManager"

        private fun getCropParam(
            context: Context,
            videoPath: String,
            videoWidth: Int,
            videoHeight: Int,
            videoDuration: Long,
            bitrate: Int,
            fps: Int,
            rect: Rect?
        ): CropParam {
            val param = CropParam()

            param.inputPath = videoPath
            val file = File(videoPath)
            val fileName = "Crop_" + System.currentTimeMillis() + "_" + file.name
            val outputPath = FileUtils.createFile(getEditorDirs(context), fileName).path
            Log.e("AAA", "视频输出路径：$outputPath")
            param.outputPath = outputPath

            //4M Bit/s
            param.setVideoBitrate(bitrate)
            param.frameRate = fps
            param.gop = 5
            param.crf = 23
            param.quality = VideoQuality.HD
            //视频编码方式
            param.videoCodec = VideoCodecs.H264_HARDWARE

            param.mediaType = MediaType.ANY_VIDEO_TYPE
            param.scaleMode = VideoDisplayMode.SCALE
            param.fillColor = Color.BLACK

            param.outputWidth = videoWidth
            param.outputHeight = videoHeight
            param.cropRect = rect

            //视频时长  单位：us
            param.startTime = 0L
            param.endTime = videoDuration

            return param
        }


        fun cropPostVideo(reactContext: ReactContext, videoPath: String, promise: Promise) {
            if (TextUtils.isEmpty(videoPath)) {
                promise?.reject("corpVideoFrame", "error: videoPath is empty")
                return
            }
            var videoPath = videoPath
            if (videoPath != null) {
                if (videoPath.startsWith("content://") || videoPath.startsWith("file://")) {
                    videoPath = com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(videoPath)).absolutePath
                }
            }

            var mVideoWidth = VideoConst.mCropVideoWidth
            var mVideoHeight = VideoConst.mCropVideoHeight
            var mDuration = 0L
            //设置一个默认码率
            var mBitrate = VideoConst.mCropBitrate
            var mFps = 60
            var mRect: Rect? = null
            try {
                val nativeParser = NativeParser()
                nativeParser.init(videoPath)
                try {
                    val rotation = nativeParser.getValue(NativeParser.VIDEO_ROTATION).toInt()
                    val bitRate = nativeParser.getValue(NativeParser.VIDEO_BIT_RATE).toFloat()
                    mDuration = nativeParser.getValue(NativeParser.VIDEO_DURATION).toLong()
                    val fps = nativeParser.getValue(NativeParser.VIDEO_FPS).toFloat()
                    val frameWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toInt()
                    val frameHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toInt()
//                    //是否有B帧
//                    val isHasBFrame = nativeParser.checkBFrame()

                    if (mFps > fps) {
                        mFps = fps.toInt()
                    }

                    //宽高过大需要裁剪
                    if (frameWidth*frameHeight > mVideoWidth*mVideoHeight) {
                        if (frameWidth > frameHeight) {
                            //视频宽度大于视频高度，横屏视频。
                            mVideoWidth = mVideoHeight
                            mVideoHeight = (mVideoWidth*frameHeight.toFloat()/frameWidth).toInt()
                        } else {
                            //视频宽度小于视频高度，竖屏竖屏。
//                            mVideoHeight = mVideoWidth
                            mVideoWidth = (mVideoHeight*frameWidth.toFloat()/frameHeight).toInt()
                        }
                        //沿用原视频码率
                        if (bitRate < mBitrate*1000) {
                            mBitrate = (bitRate/1000).toInt()
                        }
                    } else {
                        //宽高比特率都比设定值小时，不需要裁剪，直接返回原视频路径
                        if (bitRate < mBitrate*1000) {
                            promise.resolve(videoPath)
                            return
                        }
                        mVideoWidth = frameWidth
                        mVideoHeight = frameHeight
                    }
                    //保证宽高为偶数
                    if (mVideoWidth%2 == 1) {
                        mVideoWidth += 1
                    }
                    if (mVideoHeight%2 == 1) {
                        mVideoHeight += 1
                    }
                    val temp: Int
                    if (rotation == 90 || rotation == 270) {
                        temp = mVideoWidth
                        mVideoWidth = mVideoHeight
                        mVideoHeight = temp
                    }
                    mRect = if (rotation == 90 || rotation == 270) {
                        Rect(0, 0, frameHeight, frameWidth)
                    } else {
                        Rect(0, 0, frameWidth, frameHeight)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                nativeParser.release()
                nativeParser.dispose()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val videoParam = getCropParam(reactContext.applicationContext, videoPath, mVideoWidth, mVideoHeight, mDuration, mBitrate, mFps, mRect)

            val aliyunCrop = AliyunCropCreator.createCropInstance(reactContext.applicationContext)
            aliyunCrop.setCropParam(videoParam)
            aliyunCrop.setCropCallback(object : CropCallback {
                override fun onProgress(percent: Int) {
                    Log.e("AAA", "onProgress：$percent")
                    RNEventEmitter.postVideoCrop(reactContext, percent)
                }

                override fun onError(code: Int) {
                    Log.e("AAA", "onError：$code")
                    promise.reject("cropVideo", "onError:$code")
                }

                override fun onComplete(duration: Long) {
                    RNEventEmitter.postVideoCrop(reactContext, 100)
                    aliyunCrop.dispose()
                    promise.resolve(videoParam.outputPath)
                }

                override fun onCancelComplete() {
                    aliyunCrop.dispose()
                    promise.reject("cropVideo", "onCancel")
                }

            })
            aliyunCrop.startCrop()
        }


        /**
         * 图片裁剪
         */
        fun cropImage(reactContext: ReactContext, readableMap: ReadableMap, promise: Promise) {

            val context = reactContext.applicationContext

            var imagePath =
                if (readableMap.hasKey("source")) readableMap.getString("source") else ""
            if (TextUtils.isEmpty(imagePath)) {
                promise.reject("cropImager", "error: imagePath is empty")
                return
            }

            if (imagePath != null) {
                if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
                    imagePath = com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(imagePath)).absolutePath
                }
            }


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
            var endX = startX + outputWidth
            var endY = startY + outputHeight

            //todo
            if (outputWidth > width) {
                endX = width
                endY -= (outputWidth - width)
            }

            val aliyunCrop = AliyunCropCreator.createCropInstance(context)

            val file = File(imagePath)
            val fileName = "crop_" + System.currentTimeMillis() + "_" + file.name

            val outputPath = FileUtils.createFile(getEditorDirs(context), fileName).path

            //设置裁剪参数
            val param = CropParam()
            param.mediaType = MediaType.ANY_IMAGE_TYPE
            param.scaleMode = VideoDisplayMode.SCALE
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
                    promise.resolve("file://$outputPath")
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
        fun cropVideo(reactContext: ReactContext, readableMap: ReadableMap, promise: Promise) {

            val context = reactContext.applicationContext

            var videoPath =
                if (readableMap.hasKey("source")) readableMap.getString("source") else ""
            if (TextUtils.isEmpty(videoPath)) {
                promise.reject("cropVideo", "error: videoPath is empty")
                return
            }

            if (videoPath != null) {
                if (videoPath.startsWith("content://") || videoPath.startsWith("file://")) {
                    videoPath = com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(videoPath)).absolutePath
                }
            }


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
            var startY =
                if (readableMap.hasKey("cropOffsetY")) readableMap.getInt("cropOffsetY") else 0
            var endX = startX + outputWidth
            var endY = startY + outputHeight


            //todo
            //裁剪区域不会超出原视频区域。
            //例如： 原视频宽高：720*1280 ，裁剪输出宽高：1000*1000；原视频截取的区域是 宽：0~720 高：0~1000，宽度会从 720 拉伸成 1000
            if (outputWidth > mVideoWidth) {
                endX = mVideoWidth
                //应该裁剪的高度偏移
                val cropY = ((endY - startY) - (endX - startX))/2
                startY += cropY
                endY -= cropY
            }

            Log.e("AAA", "裁剪区域 ~ x：$startX~$endX；y：$startY~$endY")

            val aliyunCrop = AliyunCropCreator.createCropInstance(context)
            val duration = aliyunCrop.getVideoDuration(videoPath)

            val startTime =
                if (readableMap.hasKey("startTime")) readableMap.getInt("startTime")*1000 else 0
            val endTime =
                if (readableMap.hasKey("endTime")) readableMap.getInt("endTime")*1000 else duration

            val file = File(videoPath)
            val fileName = "crop_" + file.name

            val outputPath = FileUtils.createFile(getEditorDirs(context), fileName).path

            //设置裁剪参数
            val param = CropParam()
            //10mbit/s
            param.setVideoBitrate(10*1000)
            param.mediaType = MediaType.ANY_VIDEO_TYPE
            param.scaleMode = VideoDisplayMode.SCALE
            //填充颜色
            param.fillColor = Color.BLACK

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

                        Log.e("AAA", "裁剪后宽高 ~ videoWidth：$videoWidth；videoHeight：$videoHeight")
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
            var videoPath =
                if (options.hasKey("videoPath")) options.getString("videoPath")
                else null
            if (TextUtils.isEmpty(videoPath)) {
                promise?.reject("corpVideoFrame", "error: videoPath is empty")
                return
            }


            if (videoPath != null) {
                if (videoPath.startsWith("content://") || videoPath.startsWith("file://")) {
                    videoPath = com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(videoPath)).absolutePath
                }
            }

            //开始时间：ms
            val startTime = if (options.hasKey("startTime")) options.getInt("startTime") else 0
            //间隔时间 ms
            val intervalTime = if (options.hasKey("itemPerTime")) options.getInt("itemPerTime") else 1000


            var videoWidth = 1080
            var videoHeight = 1920
            var cacheSize = 10

            var duration = 0L
            try {
                val nativeParser = NativeParser()
                nativeParser.init(videoPath)
                try {
                    duration = nativeParser.getValue(NativeParser.VIDEO_DURATION).toLong()/1000
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                nativeParser.release()
                nativeParser.dispose()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (cacheSize > 0) {
                cacheSize = (duration/intervalTime).toInt()
            }

            val coverTimes: MutableList<Long> = ArrayList()
            for (i in 0 until cacheSize) {
                var coverTime: Int = intervalTime*i + startTime
                if (coverTime > duration) {
                    coverTime = duration.toInt()
                }
                coverTimes.add(coverTime.toLong())
            }


            var cropWidth =
                if (options.hasKey("cropWidth")) options.getInt("cropWidth") else videoWidth
            var cropHeight =
                if (options.hasKey("cropHeight")) options.getInt("cropHeight") else videoHeight

            val startX =
                if (options.hasKey("cropOffsetX")) options.getInt("cropOffsetX") else 0
            val startY =
                if (options.hasKey("cropOffsetY")) options.getInt("cropOffsetY") else 0


            if (cropWidth > videoWidth - startX) {
                cropWidth = videoWidth - startX
            }

            if (cropHeight > videoHeight - startY) {
                cropHeight = videoHeight - startY
            }

            val rect = Rect(startX, startY, cropWidth, cropHeight)

            getVideoFrame(context, videoPath, videoWidth, videoHeight, coverTimes, promise, rect)
        }


        private fun getVideoFrame(
            context: Context,
            videoPath: String?,
            videoWidth: Int,
            videoHeight: Int,
            coverTimes: List<Long>,
            promise: Promise?,
            rect: Rect
        ) {
            val videoFrameList: MutableList<String?> = ArrayList()
            GlobalScope.launch(Dispatchers.IO) {
                val jobList: MutableList<Deferred<String?>> = ArrayList()
                coverTimes.forEach {
                    jobList.add(async { getJoinedGroupList(context, videoPath, videoWidth, videoHeight, it, rect) })
                }
                jobList.forEach {
                    videoFrameList.add("file://" + it.await())
                }
                GlobalScope.launch(Dispatchers.Main) {
                    promise?.resolve(GsonBuilder().create().toJson(videoFrameList))
                }
            }
        }

        private suspend fun getJoinedGroupList(
            context: Context,
            videoPath: String?,
            videoWidth: Int,
            videoHeight: Int,
            time: Long,
            rect: Rect
        ): String? =
            suspendCancellableCoroutine { continuation ->

                val scale = 180.0/1080.0

                val thumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher()
                thumbnailFetcher.addVideoSource(videoPath, 0, Int.MAX_VALUE.toLong(), 0)
                thumbnailFetcher.setParameters(
                    (videoWidth*scale).toInt(),
                    (videoHeight*scale).toInt(), AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1
                )

                thumbnailFetcher.requestThumbnailImage(longArrayOf(time), object : AliyunIThumbnailFetcher.OnThumbnailCompletion {
                    override fun onThumbnailReady(bitmap: Bitmap, longTime: Long, index: Int) {
                        if (!bitmap.isRecycled) {

                            val name = File(videoPath).nameWithoutExtension
                            var videoFramePath = FileUtils.createFile(
                                getVideoFrameDirs(context),
                                "VideoFrame-$name-$longTime.jpg"
                            ).absolutePath

                            val cropBitmap = Bitmap.createBitmap(
                                bitmap,
                                (rect.left*scale).toInt(),
                                (rect.top*scale).toInt(),
                                (rect.right*scale).toInt(),
                                (rect.bottom*scale).toInt()
                            )
                            BitmapUtils.saveBitmap(cropBitmap, videoFramePath)
                            bitmap.recycle()
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


//        fun getVideoFrame(context: Context, videoPath: String, longTime: Long): String? {
//            var bitmap: Bitmap? = null
//            try {
//                val retriever = MediaMetadataRetriever()
//                retriever.setDataSource(videoPath)
//                bitmap =
//                    retriever.getFrameAtTime(longTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//                retriever.release()
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//            var videoFramePath =
//                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator + "videoFrame" + File.separator
//            val name = File(videoPath).nameWithoutExtension
//            videoFramePath = FileUtils.createFile(
//                videoFramePath,
//                "VideoFrame-$name-$longTime.jpg"
//            ).path
//            BitmapUtils.saveBitmap(bitmap, videoFramePath)
//            return videoFramePath
//        }

    }

}