package com.rncamerakit.recorder;

public interface OnRecorderCallbacks {
    default void  onTakePhoto(String photoPath){}
    default void  onProgress(long duration){}
    default void  onComplete(boolean validClip, long clipDuration){}
    default void  onFinish(String outputPath){}
    default void  onError(int errorCode){}
}
