package com.rncamerakit.editor.manager

import android.content.Context
import android.util.Log
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideosdk.common.struct.common.*
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator
import com.aliyun.svideosdk.importer.AliyunIImport
import com.aliyun.svideosdk.importer.impl.AliyunImportCreator
import com.facebook.react.uimanager.ThemedReactContext

class ImportManager(val reactContext: ThemedReactContext) {

    private val mContext: Context = reactContext.applicationContext

    private var mAliyunIImport: AliyunIImport? = null

    private var mProjectConfigure: String? = null

    private var mWidth = 0
    private var mHeight = 0

    init {
        mWidth = ScreenUtils.getWidth(mContext)
        mHeight = mWidth * 16 / 9
        mAliyunIImport = AliyunImportCreator.getImportInstance(mContext)
    }

    private fun getVideoParam(isVideo:Boolean): AliyunVideoParam {
        if(isVideo){
            return  AliyunVideoParam.Builder()
                .bitrate(10 * 1000)
                .frameRate(30)
                .gop(30)
                .crf(23)
                .scaleRate(1.0f)
                .outputWidth(720)
                .outputHeight(1280)
                .videoQuality(VideoQuality.SSD)
                .scaleMode(VideoDisplayMode.FILL)
                .videoCodec(VideoCodecs.H264_HARDWARE)
                .build()
        }else{
            return  AliyunVideoParam.Builder()
                .frameRate(10)
                .gop(5)
                .crf(1)
                .scaleRate(1.0f)
                .outputWidth(720)
                .outputHeight(1280)
                .videoQuality(VideoQuality.SSD)
                .scaleMode(VideoDisplayMode.FILL)
                .videoCodec(VideoCodecs.H264_SOFT_OPENH264)
                .build()
        }
    }


    /**
     * 导入视频
     */
    fun importVideo(filePath: String?)  : String?{
        val aliyunCrop = AliyunCropCreator.createCropInstance(mContext)
        val duration = aliyunCrop.getVideoDuration(filePath)
        Log.e("AAA", "duration：$duration")
        mAliyunIImport?.setVideoParam(getVideoParam(true))
        mAliyunIImport?.addMediaClip(
            AliyunVideoClip.Builder()
                .source(filePath)
                .startTime(0)
                .endTime(duration / 1000)
                .build()
        )
        mProjectConfigure = mAliyunIImport?.generateProjectConfigure()
        mAliyunIImport?.release()
        return mProjectConfigure
    }


    /**
     * 导入图片
     */
    fun importImage(filePath: String?) : String?  {
        mAliyunIImport?.setVideoParam(getVideoParam(false))
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