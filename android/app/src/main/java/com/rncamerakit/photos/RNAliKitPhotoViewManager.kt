package com.rncamerakit.photos

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
        view.setItemWidth(itemWidth)
    }

    @ReactProp(name = "itemHeight", defaultInt = 0)
    fun setItemHeight(view: RNAliKitPhotoView, itemHeight: Int) {
        view.setItemHeight(itemHeight)
    }

}