package com.rncamerakit.watermark

import android.Manifest
import android.content.Context
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import com.aliyun.svideo.common.utils.*
import com.aliyun.svideosdk.common.struct.common.AliyunVideoClip
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.common.struct.common.VideoQuality
import com.aliyun.svideosdk.common.struct.effect.ActionTranslate
import com.aliyun.svideosdk.common.struct.effect.EffectPicture
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.editor.AliyunIComposeCallBack
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory
import com.aliyun.svideosdk.importer.impl.AliyunImportCreator
import com.duanqu.transcode.NativeParser
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.liulishuo.filedownloader.BaseDownloadTask
import com.rncamerakit.R
import com.rncamerakit.RNAliavkitEventEmitter
import com.rncamerakit.utils.DownloadUtils
import com.rncamerakit.utils.MyFileDownloadCallback
import java.io.File


class WatermarkManager {

    companion object {

        var mAliyunIEditor: AliyunIEditor? = null

        fun cancelExportWaterMarkVideo() {
            mAliyunIEditor?.cancelCompose()
        }

        fun exportWaterMarkVideoByUrl(
            reactContext: ReactApplicationContext,
            videoUrl: String,
            watermarkText: String?,
            watermarkImagePath: String?,
            isDeleteVideo: Boolean,
            promise: Promise?
        ) {
            if (TextUtils.isEmpty(videoUrl)) {
                return
            }
            //下载进度占比 20%，合成进度占比 80%
            val downloadProgressProportion = 0.2F

            DownloadUtils.downloadVideo(reactContext.applicationContext, videoUrl, object : MyFileDownloadCallback() {
                override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                    super.progress(task, soFarBytes, totalBytes)
                    val progress = (soFarBytes.toDouble()/totalBytes.toDouble()*100*downloadProgressProportion).toInt()
                    Log.e("AAA", "video download progress：$progress")
                    RNAliavkitEventEmitter.onExportWaterMarkVideo(reactContext, progress)
                }

                override fun completed(task: BaseDownloadTask) {
                    super.completed(task)
                    val filePath = task.targetFilePath
                    exportWaterMarkVideo(
                        reactContext,
                        filePath,
                        watermarkText,
                        watermarkImagePath,
                        1F - downloadProgressProportion,
                        isDeleteVideo,
                        promise
                    )
                }
            })
        }

        fun exportWaterMarkVideo(
            reactContext: ReactApplicationContext,
            videoPath: String?,
            watermarkText: String?,
            watermarkImagePath: String?,
            progressProportion: Float,
            isDeleteVideo: Boolean,
            promise: Promise?
        ) {
            if (TextUtils.isEmpty(videoPath)) {
                promise?.reject("exportWaterMarkVideo", "Video path is empty")
                return
            }

            if(!FileUtils.fileIsExists(videoPath)){
                promise?.reject("exportWaterMarkVideo", "Video path is empty")
                return
            }

            var videoPath = videoPath
            val context = reactContext.applicationContext
            videoPath?.let {
                if (videoPath!!.startsWith("content://") || videoPath!!.startsWith("file://")) {
                    videoPath = com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(videoPath)).absolutePath
                }
            }
            /**
             * 获取视频信息
             */
            val videoParam = getVideoParam(context, videoPath)

            val videoConfigPath = importVideo(context, videoParam)

            mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(Uri.parse(videoConfigPath), null)
            // 初始化
            mAliyunIEditor?.init(null, context)

            var bitmap: Bitmap? = null
            if (watermarkImagePath != null && watermarkImagePath != "") {
                /**
                 * 添加 path 水印
                 */
                val effectPicture: EffectPicture = getPathWaterMark(context, videoParam, watermarkImagePath)
                mAliyunIEditor?.addImage(effectPicture)
            } else {
                /**
                 * 添加 bitmap 水印
                 */
                bitmap = textToImage(context, watermarkText)
                val effectPicture: EffectPicture = getBitmapWaterMark(videoParam, bitmap)
                mAliyunIEditor?.addImage(effectPicture)
            }

            mAliyunIEditor?.saveEffectToLocal()
            mAliyunIEditor?.applySourceChange()

            mAliyunIEditor?.compose(videoParam, videoParam.videoOutputPath, object : AliyunIComposeCallBack {
                override fun onComposeError(errorCode: Int) {
                    mAliyunIEditor?.cancelCompose()
                    bitmap?.let {
                        if (!bitmap.isRecycled) {
                            bitmap.recycle()
                        }
                    }
                    promise?.reject("onComposeError", "onError:$errorCode")
                }

                override fun onComposeProgress(progress: Int) {
                    val progress = (100*(1F - progressProportion) + progress*progressProportion).toInt()
                    Log.e("AAA", "video compose progress：$progress")
                    RNAliavkitEventEmitter.onExportWaterMarkVideo(reactContext, progress)
                }

                override fun onComposeCompleted() {
                    RNAliavkitEventEmitter.onExportWaterMarkVideo(reactContext, 100)
                    bitmap?.let {
                        if (!bitmap.isRecycled) {
                            bitmap.recycle()
                        }
                    }
                    //合成成功，删除下载的视频，并且将合成后的视频保存到相册中
//                    if (isDeleteVideo) {
//                        com.aliyun.common.utils.FileUtils.deleteFile(videoPath)
//                    }
                    promise?.resolve(videoParam.videoOutputPath)
                }
            })
        }


        private fun saveToPhotos(mContext: Context, videoPath: String?) {
            if (PermissionUtils.checkPermissionsGroup(
                    mContext, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //适配android Q
                    ThreadUtils.runOnSubThread {
                        UriUtils.saveImgToMediaStore(
                            mContext.applicationContext,
                            videoPath
                        )
                    }
                } else {
                    MediaScannerConnection.scanFile(
                        mContext.applicationContext,
                        arrayOf(videoPath),
                        arrayOf("video/mp4"),
                        null
                    )
                }
            }
        }

        private fun addVideoEndWatermark() {
            //片尾水印
        }


        /**
         * 图片水印
         */
        private fun getPathWaterMark(context: Context, videoParam: VideoParamBean, imagePath: String): EffectPicture {
            val scale: Float = videoParam.outputWidth/1080.0F
            val watermarkLogoWidth = 45F
            val watermarkLogoHeight = 66F


            val opts = BitmapFactory.Options()
            //只请求图片宽高，不解析图片像素(请求图片属性但不申请内存，解析bitmap对象，该对象不占内存)
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, opts)
            //获取图片的宽和高
            val imageWidth: Int = opts.outWidth    //30
            val imageHeight: Int = opts.outHeight  //44
            /**
             * 水印宽高
             */
            val watermarkBitmapWidth = imageWidth*watermarkLogoHeight/imageHeight
            val watermarkBitmapHeight = watermarkLogoHeight
            videoParam.watermarkWidth = watermarkBitmapWidth/videoParam.outputWidth*scale
            videoParam.watermarkHeight = watermarkLogoHeight/videoParam.outputHeight*scale
            val effectPicture = EffectPicture(imagePath)
            effectPicture.start = 0
            effectPicture.end = videoParam.videoDuration*1000
            effectPicture.x = 0.5f
            effectPicture.y = 0.95f
            effectPicture.width = videoParam.watermarkWidth
            effectPicture.height = videoParam.watermarkHeight
            return effectPicture
        }

        /**
         * 图片水印
         */
        private fun getBitmapWaterMark(videoParam: VideoParamBean, bitmap: Bitmap): EffectPicture {
            val scale: Float = videoParam.outputWidth/1080.0F
            videoParam.watermarkWidth = (bitmap.width).toFloat()/videoParam.outputWidth*scale
            videoParam.watermarkHeight = (bitmap.height).toFloat()/videoParam.outputHeight*scale
            val effectPicture = EffectPicture(bitmap)
            effectPicture.start = 0
            effectPicture.end = videoParam.videoDuration*1000
            effectPicture.x = 0.5f
            effectPicture.y = 0.95f
            effectPicture.width = videoParam.watermarkWidth
            effectPicture.height = videoParam.watermarkHeight
            return effectPicture
        }

        /**
         * 图片动画
         */
        private fun getActionTranslate(viewId: Int, duration: Long): ActionTranslate {
            val actionTranslate = ActionTranslate()
            actionTranslate.toPointX = 1f
            actionTranslate.startTime = 0
            actionTranslate.duration = 3000*1000
            actionTranslate.targetId = viewId
            val x = -0.1f
            val y = 1 - 0.12f
            //入场1s结束
            actionTranslate.toPointX = x
            actionTranslate.toPointY = y
            //向右平移
            actionTranslate.fromPointY = y
            actionTranslate.fromPointX = -1f
            return actionTranslate
        }

        private fun importVideo(context: Context, videoParam: VideoParamBean): String {
            val aliyunIImport = AliyunImportCreator.getImportInstance(context)
            aliyunIImport.setVideoParam(videoParam)
            aliyunIImport.addMediaClip(
                AliyunVideoClip.Builder()
                    .source(videoParam.videoPath)
                    .startTime(0)
                    .endTime(videoParam.videoDuration)
                    .build()
            )
            val jsonPath = aliyunIImport.generateProjectConfigure()
            aliyunIImport.release()
            return jsonPath
        }

        private fun getVideoParam(context: Context, videoPath: String?): VideoParamBean {
            val nativeParser = NativeParser()
            nativeParser.init(videoPath)
            val bitRate = nativeParser.getValue(NativeParser.VIDEO_BIT_RATE).toInt()/1000
            val rotation = nativeParser.getValue(NativeParser.VIDEO_ROTATION).toInt()
            var frameWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toInt()
            var frameHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toInt()
            nativeParser.release()
            if (rotation == 90 || rotation == 270) {
                val temp = frameWidth
                frameWidth = frameHeight
                frameHeight = temp
            }

            val videoParamBean = VideoParamBean()
            videoParamBean.bitrate = bitRate
            videoParamBean.frameRate = 30
            videoParamBean.gop = 30
            videoParamBean.crf = 23
            videoParamBean.scaleRate = 1.0f
            videoParamBean.outputWidth = frameWidth
            videoParamBean.outputHeight = frameHeight
            videoParamBean.videoQuality = VideoQuality.HD
            videoParamBean.scaleMode = VideoDisplayMode.FILL
            videoParamBean.videoCodec = VideoCodecs.H264_HARDWARE

            val videoDuration = nativeParser.getValue(NativeParser.VIDEO_DURATION).toLong()/1000
            val fileName = "download_water_mart_video_" + System.currentTimeMillis() + ".mp4"
            val outputPath = FileUtils.getDiskCachePath(context) + File.separator + "media/download" + File.separator + fileName

            videoParamBean.videoPath = videoPath
            videoParamBean.videoOutputPath = outputPath
            videoParamBean.videoDuration = videoDuration
            return videoParamBean
        }


        /**
         * 文字合成图片
         */
        private fun textToImage(context: Context, text: String?): Bitmap {
            val watermarkLogoWidth = 45F
            val watermarkLogoHeight = 66F
            var text = text
            if (text == null) {
                text = ""
            }
            val textSize = DensityUtils.sp2px(context, 15).toFloat()
            //文字：15sp
            val textPaint = TextPaint()
            textPaint.textSize = textSize
            val textWidth = textPaint.measureText(text).toInt()
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_water_mark_logo)
            //间隔，Logo 和文字之间的间隔
            val intervalWidth = DensityUtils.dip2px(context, 8F)
            // 创建一个你需要尺寸的Bitmap. Logo 图片的宽度+间隔+文字的宽度
            val bitmap =
                Bitmap.createBitmap((watermarkLogoWidth + intervalWidth + textWidth).toInt(), watermarkLogoHeight.toInt(), Bitmap.Config.ARGB_8888)
            // 用这个Bitmap生成一个Canvas,然后canvas就会把内容绘制到上面这个bitmap中
            val canvas = Canvas(bitmap)
            //画Logo
            val logoPaint = Paint()
            val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
            val dstRect = Rect(0, 0, watermarkLogoWidth.toInt(), watermarkLogoHeight.toInt())
            canvas.drawBitmap(logoBitmap, srcRect, dstRect, logoPaint)
            // 画笔－－写字
            val mTextPaint = Paint()
            mTextPaint.isAntiAlias = true
            // 绘制文字
            mTextPaint.color = Color.WHITE // 白色画笔
            mTextPaint.textSize = textPaint.textSize // 设置字体大小
            canvas.drawText(text, (watermarkLogoWidth + intervalWidth), textSize, mTextPaint)
            // 保存绘图为本地图片
            canvas.save()
            canvas.restore()
//            val fileName = "logo_video_watermark_" + System.currentTimeMillis() + ".png"
//            val outputPath = FileUtils.getDiskCachePath(context) + File.separator + "media/save" + File.separator + fileName
//            BitmapUtils.saveBitmap(bitmap, outputPath)
            return bitmap
        }

    }

}