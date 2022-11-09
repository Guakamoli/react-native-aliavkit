package com.rncamerakit.editor.manager

import android.graphics.Color
import android.graphics.PointF
import android.text.TextUtils
import androidx.annotation.ColorInt
import com.aliyun.common.utils.Size
import com.aliyun.svideosdk.common.AliyunCaption
import com.aliyun.svideosdk.common.AliyunColor
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.AliyunPasterRender
import com.facebook.react.uimanager.ThemedReactContext

class CaptionManager(
    private val mContext: ThemedReactContext,
    private val mPasterRender: AliyunPasterRender?,
    private val mAliyunIEditor: AliyunIEditor?
) {

    var mCaptionBuilder: AliyunCaptionBean.Builder = AliyunCaptionBean.build()

    init {
        mCaptionBuilder.setStartTime(0)
            .setDuration(mAliyunIEditor?.duration?.div(1000))
            .setRotate(0f)
            .setScale(1f)
            .setPosition(PointF(0.5f, 0.5f))
            .setTextAlignment(AliyunCaptionBean.TextAlign.ALIGN_CENTER)
            .setColor(Color.WHITE)
            .setBackgroundColor(Color.TRANSPARENT)
    }

    /**
     * 设置字幕文本内容
     */
    fun setText(text: String?) {
        mCaptionBuilder.setText(text)
    }

    /**
     * 设置字幕的中心点位置(x,y) 屏幕坐标 px
     */
    fun setPosition(x: Float, y: Float) {
        val newPos = PointF()
        if (mPasterRender != null) {
            val size: Size = mPasterRender.displaySize
            newPos.x = x/size.width.toFloat()
            newPos.y = y/size.height.toFloat()
        } else {
            newPos.x = 0.5f
            newPos.y = 0.5f
        }
        mCaptionBuilder.setPosition(newPos)
    }

    /**
     * 设置字幕当前旋转的度数 (0~360度）
     */
    fun setRotate(degree: Float) {
        mCaptionBuilder.setRotate(degree)
    }

    /**
     * 设置字幕缩放值
     */
    fun setScale(scale: Float) {
        mCaptionBuilder.setScale(scale)
    }

    /**
     * 设置文字的排列方式
     * AlignLeft = 1;
     * AlignRight = 2;
     * AlignHCenter = 4;
     */
    fun setTextAlignment(align: AliyunCaptionBean.TextAlign) {
        mCaptionBuilder.setTextAlignment(align)
    }


    /**
     * 设置字幕颜色  AliyunColor(int r, int g, int b, int a)
     */
    fun setColor(@ColorInt colorInt: Int) {
        mCaptionBuilder.setColor(colorInt)
    }

    /**
     * 设置字幕背景颜色  AliyunColor(int r, int g, int b, int a)
     */
    fun setBackgroundColor(@ColorInt colorInt: Int) {
        mCaptionBuilder.setBackgroundColor(colorInt)
    }

    /**
     * 设置字幕描边颜色  AliyunColor(int r, int g, int b, int a)
     */
    fun setOutlineColor(@ColorInt colorInt: Int) {
        mCaptionBuilder.setOutlineColor(colorInt)
    }


    /**
     * 设置描边宽度，单位：像素
     * @param outlineWidth 描边宽度，单位：像素， 取值: [0-64]
     */
    fun setOutlineWidth(outlineWidth: Float) {
        mCaptionBuilder.setOutlineWidth(outlineWidth)
    }

    /**
     * 设置阴影颜色
     */
    fun setShadowColor(@ColorInt colorInt: Int) {
        mCaptionBuilder.setShadowColor(colorInt)
    }

    /**
     * 设置阴影的偏移值，包含(x,y)两个方向，单位：像素，取值[0-32]
     */
    fun setShadowOffset(x: Float, y: Float) {
        mCaptionBuilder.setShadowOffset(x, y)
    }

    /**
     * 确认添加字幕
     */
    fun apply(): AliyunCaption {
        val caption = getCaption(mCaptionBuilder.build())
        mPasterRender?.addCaption(caption)
        mAliyunIEditor?.saveEffectToLocal()
//        mAliyunIEditor?.applySourceChange()
        return caption
    }


    private fun getCaption(bena: AliyunCaptionBean?): AliyunCaption {
        val caption = AliyunCaption()
        if (bena != null) {
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


    /**
     * 添加一个默认样式的视频字幕
     *
     * @param text   字幕内容
     */
    fun addDefaultStyleCaption(text: String?) {
        if (TextUtils.isEmpty(text)) {
            return
        }
        setText(text)
//        setPosition(x, y)
        setRotate(45f)
        setScale(2.0f)
        setTextAlignment(AliyunCaptionBean.TextAlign.ALIGN_CENTER)
        setColor(Color.WHITE)
        setBackgroundColor(Color.BLACK)
        setOutlineColor(Color.RED)
        setOutlineWidth(9f)
        setShadowColor(Color.GREEN)
        setShadowOffset(9f, 9f)
        //添加字幕
        apply()

    }


}