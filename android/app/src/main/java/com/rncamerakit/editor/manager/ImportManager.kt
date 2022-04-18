package com.rncamerakit.editor.manager

import android.content.Context
import android.util.Log
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideosdk.common.struct.common.*
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator
import com.aliyun.svideosdk.importer.AliyunIImport
import com.aliyun.svideosdk.importer.impl.AliyunImportCreator
import com.duanqu.transcode.NativeParser
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.VideoConst

class ImportManager(val reactContext: ThemedReactContext) {

    private val mContext: Context = reactContext.applicationContext

    private var mAliyunIImport: AliyunIImport? = null

    private var mProjectConfigure: String? = null


    init {
        mAliyunIImport = AliyunImportCreator.getImportInstance(mContext)
    }

    private fun getVideoParam(isVideo: Boolean, filePath: String?): AliyunVideoParam {

        var videoWidth: Int = VideoConst.mVideoWidth
        var videoHeight: Int = VideoConst.mVideoHeight
        var videoBitrate: Int = VideoConst.mVideoBitrate
        val videoFPS: Int = VideoConst.mVideoFps

        if (isVideo) {
            val nativeParser = NativeParser()
            nativeParser.init(filePath)
            val bitRate = nativeParser.getValue(NativeParser.VIDEO_BIT_RATE).toLong()
            val frameWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toInt()
            val frameHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toInt()
            if (frameWidth < videoWidth) {
                videoWidth = frameWidth
            }
            if (frameHeight < videoHeight) {
                videoHeight = frameHeight
            }
            if (bitRate < videoBitrate*1000) {
                videoBitrate = (bitRate/1000).toInt()
            }
            return AliyunVideoParam.Builder()
                .bitrate(videoBitrate)
                .frameRate(videoFPS)
                .gop(30)
                .crf(23)
                .scaleRate(1.0f)
                .outputWidth(videoWidth)
                .outputHeight(videoHeight)
//                .videoQuality(VideoQuality.SSD)
                .scaleMode(VideoDisplayMode.FILL)
                .videoCodec(VideoCodecs.H264_HARDWARE)
                .build()
        } else {
            return AliyunVideoParam.Builder()
                .bitrate(videoBitrate)
                .frameRate(videoFPS)
                .gop(30)
                .crf(23)
                .scaleRate(1.0f)
                .outputWidth(videoWidth)
                .outputHeight(videoHeight)
//                .videoQuality(VideoQuality.SSD)
                .scaleMode(VideoDisplayMode.FILL)
                .videoCodec(VideoCodecs.H264_HARDWARE)
                .build()
        }
    }


    /**
     * 导入视频
     */
    fun importVideo(filePath: String?): String? {
        val aliyunCrop = AliyunCropCreator.createCropInstance(mContext)
        val duration = aliyunCrop.getVideoDuration(filePath)

//        val nativeParser = NativeParser()
//        nativeParser.init(filePath)
//        val rotation = nativeParser.getValue(NativeParser.VIDEO_ROTATION).toInt()
//        val bitRate = nativeParser.getValue(NativeParser.VIDEO_BIT_RATE).toFloat()

//        Log.e("AAA", "duration：$duration")
        mAliyunIImport?.setVideoParam(getVideoParam(true, filePath))
        mAliyunIImport?.addMediaClip(
            AliyunVideoClip.Builder()
                .source(filePath)
                .startTime(0)
                .endTime(duration/1000)
                .build()
        )
        mProjectConfigure = mAliyunIImport?.generateProjectConfigure()
        mAliyunIImport?.release()
        return mProjectConfigure
    }


    /**
     * 导入图片
     */
    fun importImage(filePath: String?): String? {
        mAliyunIImport?.setVideoParam(getVideoParam(false, filePath))
        mAliyunIImport?.addMediaClip(
            AliyunImageClip.Builder()
                .source(filePath)
                .duration(5000)
                .build()
        )
        mProjectConfigure = mAliyunIImport?.generateProjectConfigure()
        mAliyunIImport?.release()
        return mProjectConfigure
    }

}