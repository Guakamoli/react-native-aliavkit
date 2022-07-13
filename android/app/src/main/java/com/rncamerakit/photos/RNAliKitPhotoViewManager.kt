package com.rncamerakit.photos

import android.content.Context
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp


class RNAliKitPhotoViewManager : SimpleViewManager<RNAliKitPhotoView>() {

    override fun getName(): String {
        return "RNAliKitPhotoViewManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): RNAliKitPhotoView {
        val view = RNAliKitPhotoView(reactContext)
        RNAliKitPhotoViewModule.mView = view
        return view
    }

    override fun onDropViewInstance(view: RNAliKitPhotoView) {
        super.onDropViewInstance(view)
        view.onDestroy()
    }

    @ReactProp(name = "pageSize", defaultInt = 40)
    fun setPageSize(view: RNAliKitPhotoView, pageSize: Int) {
        view.setPageSize(pageSize)
    }

    @ReactProp(name = "numColumns", defaultInt = 4)
    fun setNumColumns(view: RNAliKitPhotoView, numColumns: Int) {
        view.setNumColumns(numColumns)
    }

    @ReactProp(name = "multiSelect", defaultBoolean = false)
    fun setMultiSelect(view: RNAliKitPhotoView, multiSelect: Boolean) {
        view.setMultiSelect(multiSelect)
    }

    @ReactProp(name = "itemWidth", defaultInt = 0)
    fun setItemWidth(view: RNAliKitPhotoView, itemWidth: Int) {
        view.setItemWidth(dp2px(view.context.applicationContext, itemWidth))
    }

    @ReactProp(name = "itemHeight", defaultInt = 0)
    fun setItemHeight(view: RNAliKitPhotoView, itemHeight: Int) {
        view.setItemHeight(dp2px(view.context.applicationContext, itemHeight))
    }

    @ReactProp(name = "defaultSelectedPosition", defaultInt = 0)
    fun setDefaultSelectedPosition(view: RNAliKitPhotoView, itemHeight: Int) {
        view.setDefaultSelectedPosition(itemHeight)
    }

    @ReactProp(name = "sortMode")
    fun setSortMode(view: RNAliKitPhotoView, sortMode: String?) {
        view.setSortMode(sortMode)
    }

    @ReactProp(name = "keepSelected", defaultBoolean = true)
    fun setKeepSelected(view: RNAliKitPhotoView, keepSelected: Boolean) {
        view.setKeepSelected(keepSelected)
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        val builder: MapBuilder.Builder<String, Any> = MapBuilder.builder<String, Any>()
        for (event in RNAliKitPhotoView.EventEmitterKeys.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()))
        }
        return builder.build()
    }

    private fun dp2px(context: Context, dp: Int): Int {
        return (context.resources.displayMetrics.density*dp + 0.5).toInt()
    }

}