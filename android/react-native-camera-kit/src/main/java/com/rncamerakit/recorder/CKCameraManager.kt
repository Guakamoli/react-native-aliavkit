package com.rncamerakit.recorder

import android.util.Log
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.common.MapBuilder
import com.facebook.react.common.ReactConstants.TAG
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.rncamerakit.db.MusicFileBean


class CKCameraManager : SimpleViewManager<CKCamera>() {

    override fun getName(): String {
        return "CKCameraManager"
    }

    override fun createViewInstance(context: ThemedReactContext): CKCamera {
        //初始化贴纸管理
        return CKCamera(context)
    }

    override fun receiveCommand(view: CKCamera, commandId: String?, args: ReadableArray?) {
        var logCommand = "CameraManager received command $commandId("
        for (i in 0..(args?.size() ?: 0)) {
            if (i > 0) {
                logCommand += ", "
            }
            logCommand += when (args?.getType(0)) {
                ReadableType.Null -> "Null"
                ReadableType.Array -> "Array"
                ReadableType.Boolean -> "Boolean"
                ReadableType.Map -> "Map"
                ReadableType.Number -> "Number"
                ReadableType.String -> "String"
                else -> ""
            }
        }
        logCommand += ")"
        Log.d(TAG, logCommand)
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return MapBuilder.of(
            "onOrientationChange", MapBuilder.of("registrationName", "onOrientationChange"),
            "onReadCode", MapBuilder.of("registrationName", "onReadCode"),
            "onPictureTaken", MapBuilder.of("registrationName", "onPictureTaken")
        )
    }


    /**
     * 设置人脸贴纸
     */
    @ReactProp(name = "facePasterInfo")
    fun setFacePasterInfo(view: CKCamera, readableMap: ReadableMap?) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            val previewPaster = PreviewPasterForm()
            previewPaster.icon =
                if (readableMap.hasKey("icon")) readableMap.getString("icon") else ""
            previewPaster.type =
                if (readableMap.hasKey("type")) readableMap.getInt("type") else 0
            previewPaster.id = if (readableMap.hasKey("id")) readableMap.getInt("id") else 0
            previewPaster.sort = if (readableMap.hasKey("sort")) readableMap.getInt("sort") else 0

            previewPaster.url = if (readableMap.hasKey("url")) readableMap.getString("url") else ""
            previewPaster.md5 = if (readableMap.hasKey("md5")) readableMap.getString("md5") else ""
            previewPaster.preview =
                if (readableMap.hasKey("preview")) readableMap.getString("preview") else ""
            previewPaster.name =
                if (readableMap.hasKey("name")) readableMap.getString("name") else ""

            previewPaster.fontId =
                if (readableMap.hasKey("fontId")) readableMap.getInt("fontId") else 0
            previewPaster.level =
                if (readableMap.hasKey("level")) readableMap.getInt("level") else 0

            previewPaster.isLocalRes =
                if (readableMap.hasKey("isLocalRes")) readableMap.getBoolean("isLocalRes") else false

            previewPaster.path =
                if (readableMap.hasKey("path")) readableMap.getString("path") else ""
            view.mRecorderManage?.setFaceEffectPaster(previewPaster)
        }

    }

    /**
     *
     */
    @ReactProp(name = "normalBeautyLevel")
    fun setBeautyLevel(view: CKCamera, normalBeautyLevel: Int?) {
        val beautyLevel = when (normalBeautyLevel) {
            0 -> 0
            10 -> 1
            20 -> 2
            30 -> 3
            40 -> 4
            50 -> 5
            else -> {
                3
            }
        }
        view.mRecorderManage?.setBeautyLevel(beautyLevel)
    }

    @ReactProp(name = "cameraType")
    fun setCameraType(view: CKCamera, type: String?) {
        view.mRecorderManage?.setCameraType(type)

    }

    //是否开启闪光灯
    @ReactProp(name = "flashMode")
    fun setFlashMode(view: CKCamera, mode: String?) {
        view.mRecorderManage?.setLight(mode)
    }

    //手电筒
    @ReactProp(name = "torchMode")
    fun setTorchMode(view: CKCamera, mode: String?) {
        view.mRecorderManage?.setTorchMode(mode)
    }


    //设置背景音乐
    @ReactProp(name = "backgroundMusic")
    fun setBackgroundMusic(view: CKCamera, bgm: String?) {
        view.mRecorderManage?.setBackgroundMusic(bgm)
    }

    //设置背景音乐
    @ReactProp(name = "musicInfo")
    fun setMusicInfo(view: CKCamera, readableMap: ReadableMap?) {
        if (readableMap != null && readableMap.toHashMap().size > 0) {
            val songID = if (readableMap.hasKey("songID")) readableMap.getInt("songID") else 0
            val name = if (readableMap.hasKey("name")) readableMap.getString("name") else ""
            val artist = if (readableMap.hasKey("artist")) readableMap.getString("artist") else ""
            val isDbContain =
                if (readableMap.hasKey("isDbContain")) readableMap.getInt("isDbContain") else 0
            val duration = if (readableMap.hasKey("duration")) readableMap.getInt("duration") else 0
            val localPath =
                if (readableMap.hasKey("localPath")) readableMap.getString("localPath") else ""
            val cover = if (readableMap.hasKey("cover")) readableMap.getString("cover") else ""
            val url = if (readableMap.hasKey("url")) readableMap.getString("url") else ""

            val bean = MusicFileBean()
            bean.songID = songID
            bean.name = name
            bean.artist = artist
            bean.isDbContain = isDbContain
            bean.duration = duration
            bean.localPath = localPath
            bean.cover = cover
            bean.url = url

            view.reactContext.runOnUiQueueThread {
                view.mRecorderManage?.setMusicInfo(bean)
            }
        }

    }


}
