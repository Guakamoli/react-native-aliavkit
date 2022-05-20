package com.rncamerakit.editor.manager

import android.graphics.Color
import android.graphics.PointF
import com.aliyun.svideosdk.common.AliyunColor
import com.aliyun.svideosdk.common.AliyunTypeface
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.AliyunPasterManager
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption
import com.facebook.react.uimanager.ThemedReactContext

class CaptionManager2(
    private val mContext: ThemedReactContext,
    private val mPasterManager: AliyunPasterManager?,
    private val mAliyunIEditor: AliyunIEditor?
) {

    private var mCaptionController: AliyunPasterControllerCompoundCaption? = null

    /**
     * 添加文字
     * v3.22.0 新增
     * @deprecated 使用 {@link #addCaptionWithStartTime(String,Source,Source,long,long)}替代
     * @param text 显示的文字
     * @param bubbleEffectPath 气泡资源地址
     * @param font 文字字体
     * @param startTime 开始时间
     * @param duration 时长
     * @return 返回贴图控制器
     */
    fun addCaptionWithStartTime(
        text: String?,
        bubbleEffectPath: String?,
        font: String?,
        startTime: Long,
        duration: Long
    ) {
        mCaptionController = mPasterManager?.addCaptionWithStartTime(text, bubbleEffectPath, font, startTime, duration)
    }

    /**
     * 添加文字
     * v3.23.0 新增
     * @param text 显示的文字
     * @param bubbleEffectSource 气泡资源
     * @param fontSource 文字字资源
     * @param startTime 开始时间
     * @param duration 时长
     * @return 返回贴图控制器
     */
    fun addCaptionWithStartTimeSource(
        text: String?,
        bubbleEffectSource: Source?,
        fontSource: Source?,
        startTime: Long,
        duration: Long
    ) {
        mCaptionController = mPasterManager?.addCaptionWithStartTime(text, bubbleEffectSource, fontSource, startTime, duration)
    }


    /**
     * 设置字幕的中心点位置(x,y) 屏幕坐标 px
     */
    fun setPosition(x: Float, y: Float) {
        mCaptionController?.position = PointF(x, y)
    }

    /**
     * 设置字幕当前旋转的度数 (0~360度）
     */
    fun setRotate(degree: Float) {
        mCaptionController?.rotate = degree
    }

    /**
     * 设置字幕文本内容
     */
    fun setText(text: String?) {
        mCaptionController?.text = text
    }

    /**
     * 设置字幕缩放值
     */
    fun setScale(scale: Float) {
        mCaptionController?.scale = scale
    }


    /**
     * 设置文字的样式
     *
     * AliyunTypeface.NORMAL
     * AliyunTypeface.BOLD
     * AliyunTypeface.ITALIC
     * AliyunTypeface.BOLD_ITALIC
     */
    fun setFontTypeface(fontTypeface: AliyunTypeface?) {
        mCaptionController?.fontTypeface = fontTypeface
    }


    /**
     * 设置文字的排列方式
     * AlignLeft = 1;
     * AlignRight = 2;
     * AlignHCenter = 4;
     */
    fun setTextAlignment(textAlignment: Int) {
        mCaptionController?.textAlignment = textAlignment
    }


    /**
     * 设置字幕颜色  AliyunColor(int r, int g, int b, int a)
     */
    fun setColor(color: AliyunColor?) {
        mCaptionController?.color = color
    }

    /**
     * 设置字幕背景颜色  AliyunColor(int r, int g, int b, int a)
     */
    fun setBackgroundColor(color: AliyunColor?) {
        mCaptionController?.backgroundColor = color
    }


    /**
     * 设置字幕描边颜色  AliyunColor(int r, int g, int b, int a)
     */
    fun setOutlineColor(color: AliyunColor?) {
        mCaptionController?.outlineColor = color
    }

    /**
     * 设置描边宽度，单位：像素
     * @param outlineWidth 描边宽度，单位：像素， 取值: [0-64]
     */
    fun setOutlineWidth(outlineWidth: Float) {
        mCaptionController?.outlineWidth = outlineWidth
    }

    /**
     * 设置阴影颜色
     */
    fun setShadowColor(color: AliyunColor?) {
        mCaptionController?.shadowColor = color
    }

    /**
     * 设置阴影的偏移值，包含(x,y)两个方向，单位：像素，取值[0-32]
     */
    fun setShadowOffset(x: Float, y: Float) {
        mCaptionController?.shadowOffset = PointF(x, y)
    }


    fun setFontPath(fontSource: Source?) {
        if(fontSource!=null){
            mCaptionController?.setFontPath(fontSource)
        }
    }

    /**
     * 确认添加字幕
     */
    fun apply() {
        mCaptionController?.apply()
    }

    /**
     * 移除字幕
     */
    fun removeCaption() {
        if (mCaptionController == null) {
            return
        }
        mPasterManager?.remove(mCaptionController)
    }


    fun addCaption(text: String, scale: Float, rotate: Float, x: Float, y: Float) {
        var endTime = mAliyunIEditor?.duration
        if (endTime == null) {
            endTime = 30*1000*1000L
        }
        addCaptionWithStartTime(text, null, null, 0, endTime)
        setScale(scale)
        setRotate(-rotate)
        setPosition(x, y)
        setColor(AliyunColor(Color.WHITE))
        setBackgroundColor(AliyunColor(Color.BLACK))
        apply()
//        mAliyunIEditor?.saveEffectToLocal()
//        mAliyunIEditor?.applySourceChange()
    }

//    /**
//     * 添加一个默认样式的视频字幕
//     *
//     * @param text
//     */
//    fun addDefaultStyleCaption(text: String, width: Int, height: Int) {
//        if (TextUtils.isEmpty(text)) {
//            return
//        }
//        //视频时长 us
//        var endTime = mAliyunIEditor?.duration
//        if (endTime == null) {
//            endTime = 30*1000*1000L
//        }
//        addCaptionWithStartTime(text, null, null, 0, endTime)
//
//        setPosition((width/2).toFloat(), (height/2).toFloat())
//        setRotate(45F)
//        //android 默认字体大小 25sp
//        setScale(1F)
//        setFontTypeface(AliyunTypeface.BOLD_ITALIC)
//        setTextAlignment(4)// AlignHCenter = 4;
//        setColor(AliyunColor(Color.WHITE))
//        setBackgroundColor(AliyunColor(Color.BLACK))
//        setOutlineColor(AliyunColor(Color.RED))
//        setOutlineWidth(10F)
//        setShadowColor(AliyunColor(Color.GREEN))
//        setShadowOffset(10F, 10F)
//
//        apply()
//    }


}