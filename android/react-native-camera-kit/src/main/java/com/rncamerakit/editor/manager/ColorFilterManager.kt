package com.rncamerakit.editor.manager

import com.aliyun.svideo.editor.util.AlivcResUtil
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideosdk.common.struct.effect.EffectBean
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.ThemedReactContext
import com.google.gson.GsonBuilder
import java.io.File
import java.util.*

class ColorFilterManager (private val reactContext: ThemedReactContext){

    private var mContext = reactContext.applicationContext

    private val mColorFilterList: MutableList<ColorFilter> = ArrayList()

    fun getColorFilter(promise: Promise){
        mColorFilterList.clear()
        EditorCommon.getColorFilterList(mContext).forEach { path ->
            val name = File(path).name
            val icon = "file://$path/icon.png"
            mColorFilterList.add(ColorFilter(name,icon))
        }
        promise.resolve(GsonBuilder().create().toJson(mColorFilterList))
    }


    fun setColorFilter(filterName: String?,mAliyunIEditor : AliyunIEditor?) {
       val path = File(
            EditorCommon.SD_DIR + EditorCommon.QU_NAME + File.separator + EditorCommon.QU_COLOR_FILTER,
            filterName
        ).absolutePath

        val source = Source(path)
        val effect = EffectBean()
        effect.id = 0;
        effect.source = source
        if (source.path != null && source.path.contains(File.separator)) {
            val name = source.path.substring(source.path.lastIndexOf(File.separator) + 1)
            source.url = AlivcResUtil.getAppResUri(AlivcResUtil.TYPE_FILTER, name)
        }
        effect.path = source.path
        mAliyunIEditor?.applyFilter(effect)
    }


    internal class ColorFilter(var filterName: String, var iconPath: String)
}