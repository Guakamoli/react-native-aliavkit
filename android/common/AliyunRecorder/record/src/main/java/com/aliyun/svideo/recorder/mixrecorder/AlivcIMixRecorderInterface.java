package com.aliyun.svideo.recorder.mixrecorder;

import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallBack;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallBack;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.effect.EffectBean;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.EffectImage;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.recorder.CameraType;
import com.aliyun.svideosdk.common.struct.recorder.FlashType;
import com.aliyun.svideosdk.common.struct.recorder.MediaInfo;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;
import com.aliyun.svideosdk.recorder.RecordCallback;

/**
 *  整合录制接口，包含录制，合拍，如果需要修改@AliyunSvideoRecordView的的recorder请先修改本接口
 */
public interface AlivcIMixRecorderInterface {

    /**

     *  设置视频比特率 默认 15.
     * @param bitrate
     */
    void setVideoBitrate(int bitrate);

    AliyunIClipManager getClipManager();

    void setOutputPath(String var1);
    void setMediaInfo(MediaInfo var1);

    void setVideoQuality(VideoQuality var1);

    void setGop(int var1);

    void setCamera(CameraType var1);

    int getCameraCount();

    void setDisplayView(SurfaceView cameraPreviewView, SurfaceView playerView);

    void startPreview();

    void stopPreview();

    int addPaster(EffectPaster var1);

    void addPaster(EffectPaster var1, float var2, float var3, float var4, float var5, float var6, boolean var7);

    void setEffectView(float xRatio, float yRatio, float widthRatio, float heightRatio, EffectBase effectBase);

    void addImage(EffectImage effctImage);

    void removeImage(EffectImage effctImage);

    void removePaster(EffectPaster var1);

    void applyFilter(EffectFilter var1);

    void setMusic(String var1, long var2, long var4);

    int switchCamera();

    void setLight(FlashType var1);

    void setZoom(float var1);

    void setFocusMode(int var1);

    void setRate(float var1);

    void setFocus(float var1, float var2);

    void restartMv();

    void applyMv(EffectBean var1);

    void setBeautyLevel(int var1);

    void setBeautyStatus(boolean var1);

    void startRecording();

    void stopRecording();

    default void cancelRecording(){}
    int finishRecording();

    void setRecordCallback(RecordCallback var1);

    void setOnFrameCallback(OnFrameCallBack var1);

    void setRotation(int var1);

    void setOnTextureIdCallback(OnTextureIdCallBack var1);

    void needFaceTrackInternal(boolean var1);

    void setFaceTrackInternalModelPath(String var1);

    void setFaceTrackInternalMaxFaceCount(int var1);

    void setMute(boolean var1);

    void deleteLastPart();

    int getVideoWidth();

    int getVideoHeight();

    void setResolutionMode(int resolutionMode);

    /**
     * 获取当前录制类型（合拍true还是普通false）
     */
    boolean isMixRecorder();

    /**
     * 视频分辨率
     */
    void setRatioMode(int ratioMode);

    /**
     * 录制界面比例layout参数
     */
    FrameLayout.LayoutParams getLayoutParams();

    void takePhoto(boolean needBitmap);

    void applyAnimationFilter(EffectFilter effectFilter);

    void updateAnimationFilter(EffectFilter effectFilter);

    void removeAnimationFilter(EffectFilter effectFilter);

    void useFlip(boolean isUseFlip);

    void release();
    int getBackgroundColor();
    String getBackgroundImage();
    int getBackgroundImageDisplayMode();
    VideoDisplayParam getPlayDisplayParams();
    VideoDisplayParam getRecordDisplayParam();
    AlivcMixBorderParam getMixBorderParam();
    void setMixBorderParam(AlivcMixBorderParam param);
}
