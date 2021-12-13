package com.rncamerakit.recorder

abstract class OnRecorderCallbacks {
    open fun onTakePhoto(photoPath: String?) {}
    open fun onProgress(duration: Long) {}
    open fun onComplete(validClip: Boolean, clipDuration: Long) {}
    open fun onFinish(outputPath: String?) {}
    open fun onError(errorCode: Int) {}
}