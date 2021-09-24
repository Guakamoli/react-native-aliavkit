//
//  AliCameralController.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/18.
//

#import "AliCameraAction.h"
#import <AliyunVideoSDKPro/AliyunIRecorder.h>
#import <AliyunVideoSDKPro/AliyunErrorCode.h>
#import "AliyunPathManager.h"
#import "AliyunMediaConfig.h"
#import "BeautyEngineManager.h"
#import "AlivcRecordFocusView.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/UIView+React.h>
#import <React/RCTConvert.h>
#else
#import "UIView+React.h"
#import "RCTConvert.h"
#endif

#define IS_IPHONEX (([[UIScreen mainScreen] bounds].size.height<812)?NO:YES)
#define NoStatusBarSafeTop (IS_IPHONEX ? 44 : 0)
#define ScreenWidth  [UIScreen mainScreen].bounds.size.width
#define ScreenHeight  [UIScreen mainScreen].bounds.size.height

@interface AliCameraAction ()<AliyunIRecorderDelegate>
{
    NSString *_videoSavePath;
}
@property (nonatomic, strong) AliyunIRecorder *recorder;        //录制
@property (nonatomic, assign) BOOL shouldStartPreviewWhenActive;    //跳转其他页面停止预览，返回开始预览，退后台进入前台则一直在预览。这2种情况通过此变量区别。
@property (nonatomic, readwrite) AVCaptureDevicePosition devicePositon;
@property (nonatomic, strong) AlivcRecordFocusView *focusView;
@property (nonatomic, strong) UITapGestureRecognizer *focusGesture;
@property (nonatomic, strong) UIPinchGestureRecognizer *zoomGesture;
@property (nonatomic, strong) VideoRecordStartBlk_t recordStartHandler;
@property (nonatomic, strong) VideoRecordEndBlk_t recordEndHandler;
@property (nonatomic, readwrite) BOOL isRecording;
@end

@implementation AliCameraAction

static AliCameraAction *_instance = nil;

+ (AliCameraAction *)action
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _instance = [[AliCameraAction alloc] init];
    });
    return _instance;
}

- (instancetype)init
{
    self = [super init];
    if(self){
        [self setupDefault];
    }
    return self;
}

- (void)setupDefault
{
    self.normalBeautyLevel = 30;
    self.devicePositon = AVCaptureDevicePositionFront;
    self.isRecording = NO;
    [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
}

- (UIView *)cameraPreview
{
    return self.recorder.preview;
}

#pragma mark - GET
- (AliyunIRecorder *)recorder
{
    if (!_recorder) {
        //清除之前生成的录制路径
        NSString *recordDir = [AliyunPathManager createRecrodDir];
        [AliyunPathManager makeDirExist:recordDir];
        //生成这次的存储路径
        NSString *taskPath = [recordDir stringByAppendingPathComponent:[AliyunPathManager randomString]];
        //视频存储路径
        NSString *videoSavePath = [[taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
        _videoSavePath = videoSavePath;
        NSLog(@"------ :%@",videoSavePath);
        _recorder =[[AliyunIRecorder alloc] initWithDelegate:self videoSize:self.mediaConfig.outputSize];
        _recorder.clipManager.deleteVideoClipsOnExit = self.mediaConfig.deleteVideoClipOnExit;
        _recorder.preview = [[UIView alloc] initWithFrame:[self previewFrame]];
        _recorder.outputType = AliyunIRecorderVideoOutputPixelFormatType420f;//SDK自带人脸识别只支持YUV格式
        _recorder.useFaceDetect = YES;
        _recorder.faceDetectCount = 2;
        _recorder.faceDectectSync = NO;
        _recorder.frontCaptureSessionPreset = AVCaptureSessionPreset1280x720;
        _recorder.encodeMode = (self.mediaConfig.encodeMode == AliyunEncodeModeSoftFFmpeg) ? 0 : 1;
        _recorder.GOP = self.mediaConfig.gop;
        _recorder.videoQuality = (AliyunVideoQuality)self.mediaConfig.videoQuality;
        _recorder.recordFps = self.mediaConfig.fps;
        _recorder.outputPath = self.mediaConfig.outputPath ? self.mediaConfig.outputPath : videoSavePath;
        self.mediaConfig.outputPath = _recorder.outputPath;
        _recorder.taskPath = taskPath;
        _recorder.beautifyStatus = YES;
        _recorder.videoFlipH = self.mediaConfig.videoFlipH;
        _recorder.frontCameraSupportVideoZoomFactor = YES;
        //录制片段设置
        [self recorder:_recorder setMaxDuration:self.mediaConfig.maxDuration];
        [self recorder:_recorder setMinDuration:self.mediaConfig.minDuration];
    }
    return _recorder;
}

- (AliyunMediaConfig *)mediaConfig
{
    //{ mediaType: 'video', cameraType: 'front', allowsEditing: true, videoQuality: 'high' },
    if (!_mediaConfig) {//默认配置
        _mediaConfig = [AliyunMediaConfig defaultConfig];
        //录制时长，最小2s,最多3min
        _mediaConfig.minDuration = 2.0f;
        _mediaConfig.maxDuration = 15.f;
        _mediaConfig.gop = 30;
        _mediaConfig.cutMode = AliyunMediaCutModeScaleAspectFill;
        _mediaConfig.videoOnly = YES;
        _mediaConfig.backgroundColor = [UIColor blackColor];
    }
    return _mediaConfig;
}

#pragma mark - focus and scale
- (AlivcRecordFocusView *)focusView
{
    if (!_focusView) {
        CGFloat size = 150;
        _focusView = [[AlivcRecordFocusView alloc]initWithFrame:CGRectMake(0, 0, size, size)];
        _focusView.animation = YES;
        [self.recorder.preview addSubview:_focusView];
    }
    return _focusView;
}

 - (void)addFocusGesture
{
    self.focusGesture =
    [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(focusAndExposeTap:)];
    [self.recorder.preview addGestureRecognizer:self.focusGesture];
}

- (void)addZoomGesture
{
    self.zoomGesture =
    [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(pinchGesture:)];
    [self.recorder.preview addGestureRecognizer:self.zoomGesture];
}

- (void)focusAndExposeTap:(UITapGestureRecognizer *)tapGesture
{
    UIView *tapView = tapGesture.view;
    CGPoint point = [tapGesture locationInView:tapView];
    self.recorder.focusPoint = point;
    self.focusView.center = point;
    [self.recorder.preview bringSubviewToFront:self.focusView];
}

- (void)pinchGesture:(UIPinchGestureRecognizer *)pinchGesture
{
    if (isnan(pinchGesture.velocity) || pinchGesture.numberOfTouches != 2) {
        return;
    }
    self.recorder.videoZoomFactor = pinchGesture.velocity;
    pinchGesture.scale = 1;
}

- (void)removeFocusGesture
{
    if (!self.focusGesture) {
        return;
    }
    [self.recorder.preview removeGestureRecognizer:self.focusGesture];
    self.focusGesture = nil;
}

- (void)removeZoomGesture
{
    if (!self.zoomGesture) {
        return;
    }
    [self.recorder.preview removeGestureRecognizer:self.zoomGesture];
    self.zoomGesture = nil;
}

- (void)setCameraPreviewFrame:(CGRect)frame
{
    
}

- (void)takePhotos:(void (^)(NSData *imageData))handler
{
    if([self.recorder respondsToSelector:@selector(takePhoto:)]) {
        ///image 采集的渲染后图片
        ///rawImage 采集的原始图片
        [self.recorder takePhoto:^(UIImage *image, UIImage *rawImage) {
//            NSData *imageData = UIImageJPEGRepresentation(rawImage, 1.0);
            NSData *imageData = UIImageJPEGRepresentation(image, 1.0);
            handler ? handler(imageData) : nil;
        }];
    }
}

- (void)recorder:(AliyunIRecorder *)recorder setMaxDuration:(CGFloat)maxDuration
{
    recorder.clipManager.maxDuration = maxDuration;
}

- (void)recorder:(AliyunIRecorder *)recorder setMinDuration:(CGFloat)minDuration
{
    recorder.clipManager.minDuration = minDuration;
}

- (void)startFrontPreview
{
    [self.recorder startPreviewWithPositon:AliyunIRecorderCameraPositionFront];
}
- (void)startPreview
{
    [self.recorder startPreview];
}

- (void)stopPreview
{
    [self.recorder stopPreview];
}

- (AVCaptureDevicePosition)switchCameraPosition
{
    AliyunIRecorderCameraPosition position = [self.recorder switchCameraPosition];
    switch (position) {
        case AliyunIRecorderCameraPositionFront:
            self.devicePositon = AVCaptureDevicePositionFront;
            break;
        case AliyunIRecorderCameraPositionBack:
            self.devicePositon = AVCaptureDevicePositionBack;
            break;
    }
    return self.devicePositon;
}

- (BOOL)switchFlashMode:(AVCaptureFlashMode)mode;
{
    AliyunIRecorderTorchMode tMode;
    switch (mode) {
        case AVCaptureFlashModeOn:
            tMode = AliyunIRecorderTorchModeOn;
            break;
        case AVCaptureFlashModeOff:
            tMode = AliyunIRecorderTorchModeOff;
            break;
        case AVCaptureFlashModeAuto:
            tMode = AliyunIRecorderTorchModeAuto;
            break;
    }
    return [self.recorder switchTorchWithMode:tMode];
}

- (BOOL)startRecordVideo:(VideoRecordStartBlk_t)handler;
{
    self.recordStartHandler = handler;
    int num = [self.recorder startRecording];
//    NSLog(@"----: %d", num);
    return num == 0; // ==0 YES
}

- (NSString *)stopRecordVideo
{
    [self finishRecording];
    return _videoSavePath;
}

#pragma mark - AliyunIRecorderDelegate

/// 设备权限
- (void)recorderDeviceAuthorization:(AliyunIRecorderDeviceAuthor)status
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (status == AliyunIRecorderDeviceAuthorAudioDenied) {
//            [self showAVAuthorizationAlertWithMediaType:AVMediaTypeAudio];
        } else if (status == AliyunIRecorderDeviceAuthorVideoDenied) {
//            [self showAVAuthorizationAlertWithMediaType:AVMediaTypeVideo];
        }
        //当权限有问题的时候，不会走startPreview，所以这里需要更新下UI
//        [self.sliderButtonsView setSwitchRationButtonEnabled:(self.recorderDuration == 0)];
    });
}
/// 录制进度
- (void)recorderVideoDuration:(CGFloat)duration
{
//    NSLog(@"----- 录制中：%f",duration);
    self.isRecording = YES;
    if (self.recordStartHandler) {
        self.recordStartHandler(duration);
    }
}
/// 录制停止
- (void)recorderDidStopRecording
{
    NSLog(@"---- 停止录制 ");
    [self finishRecording];
}

- (void)recorderDidFinishRecording
{
    NSLog(@"----完成录制");
    self.recordStartHandler = nil;
}

- (void)finishRecording
{
    NSLog(@"--- %s",__PRETTY_FUNCTION__);
    [self.recorder stopRecording];
    self.isRecording = NO;
}

///当录至最大时长时回调
- (void)recorderDidStopWithMaxDuration
{
    NSLog(@"录制到最大时长");
    [self finishRecording];
}

- (void)recorderDidStartPreview
{
//    [self.sliderButtonsView setSwitchRationButtonEnabled:(self.recorderDuration == 0)];
    NSLog(@"-------->开始预览");
}

/// 录制异常
- (void)recoderError:(NSError *)error
{
    NSLog(@"recoderError%@",error);
}

- (void)destroyRender
{
    [[BeautyEngineManager shareManager] clear];
}

///用户自定义渲染 CVPixelBufferRef -> CVPixelBufferRef
- (CVPixelBufferRef)customRenderedPixelBufferWithRawSampleBuffer:(CMSampleBufferRef)sampleBuffer
{
    //注意这里美颜美型的参数是分开的beautyParams和beautySkinParams
    //美颜参数设置(这里用的是beautyParams)
    CGFloat beautyBuffing = self.normalBeautyLevel * 0.01 * 2.0f;
    CGFloat beautyWhite = self.normalBeautyLevel * 0.01 * 2.0f;
    CGFloat beautySharpen = self.normalBeautyLevel * 0.01 * 0.2f; //race中，这个是锐化
    //美型参数设置(这里用的是beautySkinParams)
    CGFloat beautyBigEye = 0.12f;
    CGFloat beautyThinFace = 0.23f;
    CGFloat longFace = 0.11f;
    CGFloat cutFace = 0.27f;
    CGFloat lowerJaw = 0.50f;
    CGFloat mouthWidth = 0.61f;
    CGFloat thinNose = .0f;
    CGFloat thinMandible = .0f;
    CGFloat cutCheek = .0f;
    
    return [[BeautyEngineManager shareManager] customRenderWithBuffer:sampleBuffer
                                                               rotate:self.mediaConfig.videoRotate
                                                          skinBuffing:beautyBuffing
                                                        skinWhitening:beautyWhite
                                                              sharpen:beautySharpen
                                                               bigEye:beautyBigEye
                                                             longFace:longFace
                                                              cutFace:cutFace
                                                             thinFace:beautyThinFace
                                                             lowerJaw:lowerJaw
                                                           mouthWidth:mouthWidth
                                                             thinNose:thinNose
                                                         thinMandible:thinMandible
                                                             cutCheek:cutCheek];
}

///预览view的坐标大小计算
- (CGRect)previewFrame
{
    CGFloat ratio = self.mediaConfig.outputSize.width / self.mediaConfig.outputSize.height;
    CGRect finalFrame = CGRectMake(0, NoStatusBarSafeTop+44+10, ScreenWidth, ScreenWidth /ratio);
    if ([self.mediaConfig mediaRatio] == AliyunMediaRatio9To16){
        finalFrame =CGRectMake((ScreenWidth - ScreenHeight * ratio)/2.f , 0, ScreenHeight * ratio, ScreenHeight);
    }
    return finalFrame;
}

- (void)dealloc
{
    [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
}

@end
