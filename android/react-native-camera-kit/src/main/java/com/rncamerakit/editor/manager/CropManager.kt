package com.rncamerakit.editor.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.base.Constants
import com.aliyun.svideo.common.utils.BitmapUtils
import com.aliyun.svideo.common.utils.FileUtils
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
import com.facebook.react.bridge.ReadableMap
import com.google.gson.GsonBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class CropManager {
    companion object {
        fun cropImager(context: Context?, imagePath: String?, outputWidth: Int, outputHeight: Int) {
            val tag = "cropImager"
            BitmapUtils.checkAndAmendImgOrientation(imagePath)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)

            val mimeType = options.outMimeType
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight

            val aliyunCrop = AliyunCropCreator.createCropInstance(context)

            val file = File(imagePath)
            val fileName = "crop_" + file.name
            val pathDis =
                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator
            val outputPath = FileUtils.createFile(pathDis, fileName).path

            //设置裁剪参数
            val param = CropParam()
            param.mediaType = MediaType.ANY_IMAGE_TYPE
            param.scaleMode = VideoDisplayMode.SCALE
            param.inputPath = imagePath
            param.outputPath = outputPath

            //裁剪矩阵
            val startCropPosX = 0
            val startCropPoxY = 0
            val cropRect = Rect(
                startCropPosX,
                startCropPoxY,
                imageWidth,
                imageWidth
            )
            param.cropRect = cropRect

            param.outputWidth = outputWidth
            param.outputHeight = outputHeight

            //视频编码方式
            param.videoCodec = VideoCodecs.H264_HARDWARE;
            //填充颜色
            param.fillColor = Color.BLACK;

            aliyunCrop.setCropParam(param)

            aliyunCrop.setCropCallback(object : CropCallback {
                override fun onProgress(progress: Int) {
                    Log.e(tag, "progress：$progress")
                }

                override fun onError(code: Int) {
                    Log.e(tag, "onError：$code")
                }

                override fun onComplete(duration: Long) {
                    Log.e(tag, "onComplete：$duration; $outputPath")
                    aliyunCrop.dispose()
                }

                override fun onCancelComplete() {
                    Log.e(tag, "onCancelComplete")
                    aliyunCrop.dispose()
                }
            })
            aliyunCrop.startCrop()
        }


        fun cropVideo(
            context: Context,
            videoPath: String?,
        ) {
            val tag = "cropVideo"

            val aliyunCrop = AliyunCropCreator.createCropInstance(context)

            var mVideoWidth = 0
            var frameHeight = 0
            var duration: Long = 0
            try {
                val nativeParser = NativeParser()
                nativeParser.init(videoPath)
                try {
                    mVideoWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toInt()
                    frameHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                nativeParser.release()
                nativeParser.dispose()
                duration = aliyunCrop.getVideoDuration(videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val file = File(videoPath)
            val fileName = "crop_" + file.name
            val pathDis =
                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator
            val outputPath = FileUtils.createFile(pathDis, fileName).path

            //设置裁剪参数
            val param = CropParam()
            param.mediaType = MediaType.ANY_VIDEO_TYPE
            param.scaleMode = VideoDisplayMode.SCALE

            param.inputPath = videoPath
            param.outputPath = outputPath

            param.cropRect = Rect(0, 0, mVideoWidth, mVideoWidth)
            param.outputWidth = 720
            param.outputHeight = 720

            param.startTime = 1000
            param.endTime = duration - param.startTime

            param.quality = VideoQuality.SSD
            param.gop = 5
            param.frameRate = 30
            param.crf = 23
            //视频编码方式
            param.videoCodec = VideoCodecs.H264_HARDWARE;
            //填充颜色
            param.fillColor = Color.BLACK;

            aliyunCrop.setCropParam(param)
            aliyunCrop.setCropCallback(object : CropCallback {
                override fun onProgress(progress: Int) {
                    Log.e(tag, "progress：$progress")
                }

                override fun onError(code: Int) {
                    Log.e(tag, "onError：$code")
                }

                override fun onComplete(duration: Long) {
                    Log.e(tag, "onComplete：$duration; $outputPath")
                    aliyunCrop.dispose()
                }

                override fun onCancelComplete() {
                    Log.e(tag, "onCancelComplete")
                    aliyunCrop.dispose()
                }
            })
            aliyunCrop.startCrop()

        }


        /**
         * 视频抽帧
         */
        fun corpVideoFrame(context: Context, options: ReadableMap, promise: Promise) {
            val videoPath =
                if (options.hasKey("videoPath")) options.getString("videoPath")
//                else null
                else Constants.SDCardConstants.getDir(context.applicationContext) + File.separator + "paiya-record.mp4"
            if (TextUtils.isEmpty(videoPath)) {
                promise.reject("corpVideoFrame", "error: videoPath is empty")
                return
            }
            //ms
            var intervalTime =
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
            val coverTimes: MutableList<Long> = ArrayList<Long>()
            for (i in 0 until cacheSize) {
                var coverTime: Int = intervalTime * i + startTime
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
            promise: Promise
        ) {
            val thumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher()
            thumbnailFetcher.addVideoSource(videoPath, 0, Int.MAX_VALUE.toLong(), 0)
            thumbnailFetcher.setParameters(
                videoWidth,
                videoHeight,
                AliyunIThumbnailFetcher.CropMode.Mediate,
                VideoDisplayMode.SCALE,
                coverTimes.size
            )

            val videoFramePaths: MutableList<String?> = ArrayList<String?>()
            Observable.fromIterable(coverTimes).flatMap { longs ->
                Log.e("BBB", "apply:$longs")
                Observable.create<String?> { emitter ->
                    thumbnailFetcher.requestThumbnailImage(longArrayOf(longs), object :
                        AliyunIThumbnailFetcher.OnThumbnailCompletion {
                        override fun onThumbnailReady(bitmap: Bitmap, longTime: Long) {
                            if (bitmap != null && !bitmap.isRecycled) {
                                val videoFramePath = saveBitmap(context, videoPath, bitmap, longs)
                                if (!TextUtils.isEmpty(videoFramePath)) {
                                    emitter!!.onNext(videoFramePath)
                                    emitter.onComplete()
                                } else {
                                    emitter!!.onError(Throwable("errorMsg:video Frame is empty"))
                                }
                            }
                        }

                        override fun onError(errorCode: Int) {
                            emitter!!.onError(Throwable("errorCode:$errorCode"))
                        }
                    })
                }.subscribeOn(Schedulers.io())
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String?> {
                    override fun onSubscribe(d: @NonNull Disposable?) {
                        Log.e("BBB", "onSubscribe:")
                    }

                    override fun onNext(path: @NonNull String?) {
                        Log.e("BBB", "onNext:$path")
                        videoFramePaths.add(path)
                    }

                    override fun onError(e: @NonNull Throwable?) {
                        Log.e("BBB", "nError:" + e?.message)
                    }

                    override fun onComplete() {
                        Log.e("BBB", "onComplete:")
                        promise.resolve(GsonBuilder().create().toJson(videoFramePaths))
                    }
                })
        }


        private fun saveBitmap(
            context: Context,
            videoPath: String?,
            bitmap: Bitmap,
            longTime: Long
        ): String? {
            var path =
                FileUtils.getDiskCachePath(context) + File.separator + "Media" + File.separator + "videoFrame" + File.separator

            val name = File(videoPath).nameWithoutExtension

            path = FileUtils.createFile(path, "VideoFrame-$name-$longTime.jpg").path

            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(path)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            } catch (e: java.lang.Exception) {
                return ""
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return path
        }

    }


}