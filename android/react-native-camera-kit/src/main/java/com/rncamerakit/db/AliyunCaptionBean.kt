package com.rncamerakit.db

import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import androidx.annotation.ColorInt
import com.aliyun.svideo.crop.bean.AlivcCropInputParam
import com.aliyun.svideosdk.common.AliyunColor
import com.aliyun.svideosdk.common.AliyunFontStyle
import com.aliyun.svideosdk.common.ISource

/**
 * 阿里云字幕类
 */
class AliyunCaptionBean {

    companion object {
        fun build(): Builder {
            return Builder()
        }
    }

    /**
     * 起始时间 (单位：ms）
     */
    var startTime: Long = 0

    /**
     * 播放时长 (单位：ms）
     */
    var duration: Long = 2*1000

    /**
     * 字幕文本
     */
    var text: String? = null

    /**
     * 字幕中心点位置(x,y) x和y的值在[0-1]之间
     */
    var point: PointF? = null

    /**
     * 字幕布局大小
     */
    var size: RectF? = null

    /**
     * 字幕旋转度数 角度值(0~360度）
     */
    var rotate: Float = 0F

    /**
     * 字幕缩放值 scale > 0
     */
    var scale: Float = 1F

    /**
     * 文字的排列方式
     * AlignLeft = 1;
     * AlignRight = 2;
     * AlignHCenter = 4;
     */
    var textAlignment: Int = 4

    /**
     * 字幕颜色
     */
    var color: AliyunColor? = null

    /**
     * 字幕背景颜色
     */
    var backgroundColor: AliyunColor? = null

    /**
     * 字幕描边颜色
     */
    var outlineColor: AliyunColor? = null

    /**
     * 描边宽度，单位：像素
     */
    var outlineWidth: Float? = null

    /**
     * 字幕阴影颜色
     */
    var shadowColor: AliyunColor? = null

    /**
     * 阴影的偏移值，包含(x,y)两个方向，单位：像素
     */
    var shadowOffset: PointF? = null

    /**
     * 字体样式
     */
    var aliyunFontStyle: AliyunFontStyle? = null

    /**
     * 气泡文字的模板资源
     */
    var bubbleSource: ISource? = null


    /**
     * 花字模板资源
     */
    var fontEffectSource: ISource? = null

    class Builder {
        private var mBean = AliyunCaptionBean()

        fun setStartTime(startTime: Long): Builder {
            mBean.startTime = startTime
            return this
        }

        fun setDuration(duration: Long?): Builder {
            if (duration == null) {
                return this
            }
            mBean.duration = duration
            return this
        }

        fun setText(text: String?): Builder {
            mBean.text = text
            return this
        }

        /**
         * 字幕中心点坐标，最终设置时，需要转成 0~1 的参数，相对于 AliyunPasterRender?.setDisplaySize(mWidth, mHeight) 设置的范围
         */
        fun setPosition(x: Float, y: Float): Builder {
            mBean.point = PointF(x, y)
            return this
        }

        fun setPosition(point: PointF?): Builder {
            mBean.point = point
            return this
        }


        /**
         * 字幕的布局大小
         */
        fun setSize(width: Float, height: Float): Builder {
            mBean.size = RectF(0f, 0f, width, height)
            return this
        }

        fun setSize(size: RectF?): Builder {
            mBean.size = size
            return this
        }

        fun setRotate(rotate: Float): Builder {
            mBean.rotate = rotate
            return this
        }

        fun setScale(scale: Float): Builder {
            if (scale < 0) {
                return this
            }
            mBean.scale = scale
            return this
        }

        fun setTextAlignment(textAlignment: Int): Builder {
            mBean.textAlignment = textAlignment
            return this
        }

        fun setTextAlignment(txtAlign: TextAlign?): Builder {
            mBean.textAlignment = when (txtAlign) {
                TextAlign.ALIGN_LEFT -> 1
                TextAlign.ALIGN_RIGHT -> 2
                TextAlign.ALIGN_CENTER -> 4
                else -> {
                    4
                }
            }
            return this
        }

        fun setColor(@ColorInt colorInt: Int): Builder {
            mBean.color = AliyunColor(colorInt)
            return this
        }

        fun setBackgroundColor(@ColorInt colorInt: Int): Builder {
            mBean.backgroundColor = AliyunColor(colorInt)
            return this
        }


        fun setOutlineColor(@ColorInt outlineColor: Int): Builder {
            mBean.outlineColor = AliyunColor(outlineColor)
            return this
        }

        fun setOutlineWidth(outlineWidth: Float?): Builder {
            mBean.outlineWidth = outlineWidth
            return this
        }

        fun setShadowColor(@ColorInt shadowColor: Int): Builder {
            mBean.shadowColor = AliyunColor(shadowColor)
            return this
        }

        /**
         * 阴影的偏移值，包含(x,y)两个方向，单位：像素
         */
        fun setShadowOffset(x: Float, y: Float): Builder {
            mBean.shadowOffset = PointF(x, y)
            return this
        }

        fun build(): AliyunCaptionBean {
            return mBean
        }

    }

    enum class TextAlign {
        ALIGN_LEFT,
        ALIGN_RIGHT,
        ALIGN_CENTER
    }

}