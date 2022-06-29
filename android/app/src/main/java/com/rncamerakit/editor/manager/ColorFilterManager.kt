package com.rncamerakit.editor.manager

import android.content.Context
import android.util.Log
import com.aliyun.svideo.common.utils.LanguageUtils
import com.aliyun.svideo.editor.util.AlivcResUtil
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.recorder.util.RecordCommon
import com.aliyun.svideosdk.common.struct.effect.EffectBean
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.ThemedReactContext
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

class ColorFilterManager(private val reactContext: ThemedReactContext) {

    private var mContext = reactContext.applicationContext

    companion object {
        fun getColorFilter(mContext: Context, promise: Promise) {
            val mColorFilterList: MutableList<ColorFilter> = ArrayList()
//            mColorFilterList.add(ColorFilter("无效果", "ic_color_filter_empty", ""))
            EditorCommon.getColorFilterList(mContext).forEach { path ->
                val name = File(path).name
                val displayName = getFilterName(mContext, path)
                val icon = "file://$path/icon.png"
                mColorFilterList.add(ColorFilter(displayName, name, icon, path))
            }
            promise.resolve(GsonBuilder().create().toJson(mColorFilterList))
        }

        /**
         * 获取录制滤镜
         */
        fun getRecordColorFilter(mContext: Context, promise: Promise) {
            val mColorFilterList: MutableList<ColorFilter> = ArrayList()
//            mColorFilterList.add(ColorFilter("无效果", "ic_color_filter_empty", ""))
            RecordCommon.getColorFilterList(mContext).forEach { path ->
                val name = File(path).name
                val displayName = getFilterName(mContext, path)
                val icon = "file://$path/icon.png"
                mColorFilterList.add(ColorFilter(displayName, name, icon, path))
            }
            Log.e("AAA", "mColorFilterList:" + mColorFilterList.size)
//            val obj = Arguments.fromList(mColorFilterList)
//            promise.resolve(obj)
            promise.resolve(GsonBuilder().create().toJson(mColorFilterList))
        }

        /**
         * 获取滤镜名称 适配系统语言/中文或其他
         * @param path 滤镜文件目录
         * @return name
         */
        private fun getFilterName(context: Context, path: String): String {
            var path = path
            path = if (LanguageUtils.isCHEN(context)) {
                "$path/config.json"
            } else {
                "$path/configEn.json"
            }
            var name = ""
            val var2 = StringBuffer()
            val var3 = File(path)
            try {
                val var4 = FileReader(var3)
                var var7: Int
                while (var4.read().also { var7 = it } != -1) {
                    var2.append(var7.toChar())
                }
                var4.close()
            } catch (var6: IOException) {
                var6.printStackTrace()
            }
            try {
                val var4 = JSONObject(var2.toString())
                name = var4.optString("name")
            } catch (var5: JSONException) {
                var5.printStackTrace()
            }
            return name
        }
    }


    fun setColorFilter(filterName: String?, mAliyunIEditor: AliyunIEditor?) {
        if (filterName == null || filterName == "无效果" || filterName == "") {
            val effect = EffectBean()
            effect.path = null
            mAliyunIEditor?.applyFilter(effect)
            return
        }
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


    internal class ColorFilter(var displayName: String, var filterName: String, var iconPath: String, var path: String)
}