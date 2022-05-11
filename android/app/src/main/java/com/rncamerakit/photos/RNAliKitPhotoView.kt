package com.rncamerakit.photos

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.FrameLayout
import com.aliyun.svideo.common.utils.ScreenUtils
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.rncamerakit.editor.manager.ColorFilterManager
import com.rncamerakit.editor.manager.ComposeManager
import com.rncamerakit.utils.DownloadUtils

class RNAliKitPhotoView(val reactContext: ThemedReactContext) : FrameLayout(reactContext.applicationContext) {

    private val mContext: Context = reactContext.applicationContext

    private var mWidth = 0
    private var mHeight = 0

    init {
        this.mWidth = ScreenUtils.getWidth(reactContext)
        this.mHeight = mWidth*16/9
        var mVideoContainer = FrameLayout(mContext)
        mVideoContainer.setBackgroundColor(Color.BLUE)
        val params = LayoutParams(mWidth, mHeight)
        params.gravity = Gravity.CENTER_HORIZONTAL
        addView(mVideoContainer, params)
    }

    fun setPageSize(pageSize: Int) {
    }

    fun setNumColumns(numColumns: Int) {
    }

    fun setMultiSelect(multiSelect: Boolean) {
    }

    fun setItemWidth(itemWidth: Int) {
    }

    fun setItemHeight(itemHeight: Int) {
    }

}