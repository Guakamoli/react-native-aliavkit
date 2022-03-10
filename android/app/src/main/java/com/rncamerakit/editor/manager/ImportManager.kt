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

    private fun getVideoParam(isVideo: Boolean, rotation: Int): AliyunVideoParam {
        if (isVideo) {
            var videoWidth: Int = VideoConst.mVideoWidth
            var videoHeight: Int = VideoConst.mVideoHeight
//            if (rotation == 90 || rotation == 270) {
//                videoWidth = VideoConst.mVideoHeight
//                videoHeight = VideoConst.mVideoWidth
//            } else {
//                videoWidth = VideoConst.mVideoWidth
//                videoHeight = VideoConst.mVideoHeight
//            }
            return AliyunVideoParam.Builder()
                .bitrate(VideoConst.mVideoBitrate)
                .frameRate(30)
                .gop(30)
                .crf(23)
                .scaleRate(1.0f)
                .outputWidth(videoWidth)
                .outputHeight(videoHeight)
                .videoQuality(VideoQuality.SSD)
                .scaleMode(VideoDisplayMode.FILL)
                .videoCodec(VideoCodecs.H264_HARDWARE)
                .build()
        } else {
            return AliyunVideoParam.Builder()
                .bitrate(VideoConst.mVideoBitrate)
                .frameRate(30)
                .gop(30)
                .crf(23)
                .scaleRate(1.0f)
                .outputWidth(VideoConst.mVideoWidth)
                .outputHeight(VideoConst.mVideoHeight)
                .videoQuality(VideoQuality.SSD)
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
        val nativeParser = NativeParser()
        nativeParser.init(filePath)
        val rotation = nativeParser.getValue(NativeParser.VIDEO_ROTATION).toInt()
        Log.e("AAA", "duration：$duration")
        mAliyunIImport?.setVideoParam(getVideoParam(true, rotation))
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
        mAliyunIImport?.setVideoParam(getVideoParam(false,0))
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