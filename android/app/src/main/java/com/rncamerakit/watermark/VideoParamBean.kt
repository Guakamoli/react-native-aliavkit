package com.rncamerakit.watermark

import com.aliyun.svideosdk.common.struct.common.AliyunVideoParam

class VideoParamBean : AliyunVideoParam() {
    /**
     * 视频地址
     */
    var videoPath: String? = null

    /**
     * 视频输出路径
     */
    var videoOutputPath: String? = null

    /**
     * 视频时长
     */
    var videoDuration: Long = 0

    /**
     * 水印宽 watermarkWidth = 0.1f ，水印显示宽度 = 视频宽度*0.1f
     */
    var watermarkWidth = 0f

    /**
     * 水印高 watermarkHeight = 0.1f ，水印显示高度 = 视频宽度*0.1f
     */
    var watermarkHeight = 0f
}