package com.rncamerakit

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ThemedReactContext

class RNEventEmitter {

    companion object {
        /**
         * 录制进度
         */
        fun startVideoRecord(reactContext: ReactContext?,duration: Long){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoRecord", "" + duration)
        }

        /**
         * 下载贴纸
         */
        fun downloadPasterProgress(reactContext: ReactContext?, progress: Int){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("downloadPaster", "" + progress)
        }

        /**
         * Player 播放进度
         */
        fun startVideoPlay(reactContext: ReactContext?, currentPlayTime: Long){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoPlay", "" + currentPlayTime/1000)
        }

        /**
         * Editor 播放进度
         */
        fun startVideoEditor(reactContext: ReactContext?, currentPlayTime: Long){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoEditor", "" + currentPlayTime/1000)
        }

        /**
         * 视频裁剪进度
         */
        fun startVideoCrop(reactContext: ReactContext?, progress: Int){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoCrop", "" + progress)
        }

        /**
         * 视频合成
         */
        fun startVideoCompose(reactContext: ReactContext?, progress: Int){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoCompose", "" + progress)
        }

    }

}