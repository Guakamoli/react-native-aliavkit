package com.rncamerakit

import com.aliyun.svideo.editor.effects.control.UIEditorPage
import com.duanqu.transcode.NativeParser
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.io.File

class RNEventEmitter {

    companion object {
        /**
         * 录制进度
         */
        fun startMultiRecording(reactContext: ReactContext?, duration: Long) {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startMultiRecording", "" + duration)
        }

        /**
         * 录制进度
         */
        fun startVideoRecord(reactContext: ReactContext?, duration: Long) {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoRecord", "" + duration)
        }

        /**
         * 下载贴纸
         */
        fun downloadPasterProgress(reactContext: ReactContext?, progress: Int) {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("downloadPaster", "" + progress.toDouble()/100)
        }


        /**
         * 下载音乐
         */
        fun downloadMusicProgress(reactContext: ReactContext?, progress: Int) {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("downloadMusic", "" + progress.toDouble()/100)
        }

        /**
         * Player 播放进度
         */
        fun startVideoPlay(reactContext: ReactContext?, currentPlayTime: Long) {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoPlay", "" + currentPlayTime/1000)
        }

        /**
         * Editor 播放进度
         */
        fun startVideoEditor(reactContext: ReactContext?, currentPlayTime: Long, currentStreamPlayTime: Long) {
            val obj = Arguments.createMap()
            obj.putDouble("playProgress", currentPlayTime.toDouble()/1000/1000)
            obj.putDouble("streamProgress", currentStreamPlayTime.toDouble()/1000/1000)
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoEditor", obj)
        }

        /**
         * 视频裁剪进度
         */
        fun startVideoCrop(reactContext: ReactContext?, progress: Int) {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoCrop", "" + progress.toDouble()/100)
        }

        /**
         * 视频合成
         */
        fun startVideoCompose(reactContext: ReactContext?, progress: Int, outputPath: String, isStoryCompose: Boolean): HashMap<String, Any> {
            val videoParamMap: HashMap<String, Any> = HashMap<String, Any>()
            val obj = Arguments.createMap()
            if(isStoryCompose){
                obj.putDouble("progress", progress.toDouble()/100)
            }else{
                obj.putDouble("exportProgress", progress.toDouble()/100)
            }
            if (progress == 100) {
                obj.putString("outputPath", outputPath)
                val response: WritableMap = WritableNativeMap()
                response.putString("path", outputPath)
                videoParamMap["path"] = outputPath
                if (outputPath != null) {
                    val videoFile = File(outputPath)
                    response.putDouble("size", videoFile.length().toDouble())
                    response.putString("type", "video/" + videoFile.extension)
                    response.putString("name", videoFile.name)

                    videoParamMap["size"] = videoFile.length()
                    videoParamMap["type"] = "video/" + videoFile.extension
                    videoParamMap["name"] = videoFile.name
                }
                try {
                    val nativeParser = NativeParser()
                    nativeParser.init(outputPath)
                    val rotation = nativeParser.getValue(NativeParser.VIDEO_ROTATION).toInt()
                    val videoWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH)
                    val videoHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT)
                    if (rotation == 90 || rotation == 270) {
                        response.putInt("height", videoWidth.toInt())
                        response.putInt("width", videoHeight.toInt())

                        videoParamMap["height"] = videoWidth
                        videoParamMap["width"] = videoHeight
                    } else {
                        response.putInt("width", videoWidth.toInt())
                        response.putInt("height", videoHeight.toInt())

                        videoParamMap["width"] = videoWidth
                        videoParamMap["height"] = videoHeight
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                obj.putMap("videoParams", response)
            }
            if (isStoryCompose) {
                reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    ?.emit("storyComposeVideo", obj)
            } else {
                reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    ?.emit("startVideoCompose", obj)
            }
            return videoParamMap
        }


        fun postVideoCrop(reactContext: ReactContext?, progress: Int) {
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("postVideoCrop", "" + progress.toDouble()/100)
        }

    }

}