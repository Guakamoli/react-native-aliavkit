package com.rncamerakit

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ThemedReactContext

class RNEventEmitter {

    companion object {
        /**
         * 录制进度
         */
        fun startVideoRecord(reactContext: ReactApplicationContext?,duration: Long){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoRecord", "" + duration)
        }

        /**
         * 下载贴纸
         */
        fun downloadPasterProgress(reactContext: ThemedReactContext?, progress: Int){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("downloadPaster", "" + progress)
        }

        /**
         * 播放进度
         */
        fun startVideoPlay(reactContext: ThemedReactContext?, currentPlayTime: Long){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoPlay", "" + currentPlayTime/1000)
        }

        /**
         * 视频合成
         */
        fun startVideoCompose(reactContext: ThemedReactContext?, progress: Int){
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("startVideoCompose", "" + progress)
        }

    }

}