package com.rncamerakit.editor.manager

import android.graphics.Color
import android.graphics.PointF
import android.text.TextUtils
import com.aliyun.common.utils.Size
import com.aliyun.svideosdk.common.AliyunCaption
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.AliyunPasterRender
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.db.AliyunCaptionBean

class CaptionManager(
    private val mContext: ThemedReactContext,
    private val mPasterRender: AliyunPasterRender?
) {

    init {

    }


    /**
     * 确认添加字幕
     */
    fun apply() {
    }

    /**
     * 移除字幕
     */
    fun removeCaption() {

    }


    /**
     * 添加一个默认样式的视频字幕
     *
     * @param text   字幕内容
     * @param duration 字幕持续时长  ms
     * @param x  文字中心点坐标
     * @param y 文字中心点坐标
     */
    fun addDefaultStyleCaption(text: String?, duration: Long?, x: Int, y: Int) {
        if (TextUtils.isEmpty(text)) {
            return
        }
        //字幕中心点位置(x,y) x和y的值在[0-1]之间
        val newPos = PointF()
        if (mPasterRender != null) {
            val size: Size = mPasterRender.displaySize
            newPos.x = x/size.width.toFloat()
            newPos.y = y/size.height.toFloat()
        } else {
            newPos.x = 0.5f
            newPos.y = 0.5f
        }

        val captionBean = AliyunCaptionBean.build()
            .setStartTime(0)
            .setDuration(duration)
            .setText(text)
            .setPosition(newPos)
            .setRotate(45f)

            //android 默认字体大小 25sp
            .setScale(2.0f)

            .setTextAlignment(AliyunCaptionBean.TextAlign.ALIGN_CENTER)
            .setColor(Color.WHITE)
            .setBackgroundColor(Color.BLACK)
            .setOutlineColor(Color.RED)
            .setOutlineWidth(9f)
            .setShadowColor(Color.GREEN)
            .setShadowOffset(9f, 9f)
            .build()

        mPasterRender?.addCaption(getCaption(captionBean))
    }


    private fun getCaption(bena: AliyunCaptionBean?): AliyunCaption {
        val caption = AliyunCaption()
        if(bena!=null){
            caption.startTime = bena.startTime
            caption.duration = bena.duration
            caption.text = bena.text
            caption.position = bena.point
            caption.size = bena.size
            caption.rotate = bena.rotate
            caption.scale = bena.scale
            caption.textAlignment = bena.textAlignment
            caption.color = bena.color
            caption.backgroundColor = bena.backgroundColor
            caption.outlineColor = bena.outlineColor
        }




        return caption
    }


}