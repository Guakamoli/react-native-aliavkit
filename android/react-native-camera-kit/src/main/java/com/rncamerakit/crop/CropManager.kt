package com.rncamerakit.crop

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import com.aliyun.svideo.common.utils.BitmapUtils
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideosdk.common.struct.common.MediaType
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.common.struct.common.VideoQuality
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.crop.CropCallback
import com.aliyun.svideosdk.crop.CropParam
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator
import com.duanqu.transcode.NativeParser
import java.io.File

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

            param.startTime =  1000
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

    }
}