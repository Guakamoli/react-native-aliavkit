//
//  AliCameralController.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/18.
//

#import "AliCameraAction.h"
#import <AliyunVideoSDKPro/AliyunIRecorder.h>
#import <AliyunVideoSDKPro/AliyunErrorCode.h>
#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/UIView+React.h>
#import <React/RCTConvert.h>
#else
#import "UIView+React.h"
#import "RCTConvert.h"
#endif
#import "AliyunPathManager.h"
#import "AliyunMediaConfig.h"
#import "BeautyEngineManager.h"
#import "AlivcRecordFocusView.h"
#import "AliyunPasterInfo.h"
#import "AliyunDownloadManager.h"
#import "ShortCut.h"
#import "RNAVDeviceHelper.h"

@interface AliCameraAction ()<AliyunIRecorderDelegate>
{
    NSString *_videoSavePath;
    VideoRecordEndBlk_t _complete;
    AliyunIRecorderCameraPosition mRecorderCamerPosition;
}

@property (nonatomic, strong) AliyunIRecorder *recorder;
@property (nonatomic, strong) AlivcRecordFocusView *focusView;
@property (nonatomic, strong) UITapGestureRecognizer *focusGesture;
@property (nonatomic, strong) UIPinchGestureRecognizer *zoomGesture;
@property (nonatomic, copy) VideoRecordStartBlk_t recordStartHandler;
@property (nonatomic, copy) VideoRecordEndBlk_t recordEndHandler;
@property (nonatomic, readwrite) BOOL isRecording;
@property (nonatomic, strong) AliyunDownloadManager *downloadManager;
@property (nonatomic, strong) AliyunEffectPaster *previousEffectPaster;  //current face paster
@property (nonatomic) CGRect previewRect;

@end

@implementation AliCameraAction

- (instancetype)initWithPreviewFrame:(CGRect)previewFrame
{
    if (self = [super init]) {
        [self setupDefault];
        self.previewRect = previewFrame;
    }
    return self;
}

- (void)setupDefault
{
    self.normalBeautyLevel = 30;
    self.isRecording = NO;
}

- (UIView *)cameraPreview
{
    return self.recorder.preview;
}

#pragma mark - GET
- (AliyunIRecorder *)recorder
{
    if (!_recorder) {
        _recorder =[[AliyunIRecorder alloc] initWithDelegate:self videoSize:self.mediaConfig.outputSize];
        _recorder.preview = [[UIView alloc] initWithFrame:self.previewRect];
        _recorder.outputType = AliyunIRecorderVideoOutputPixelFormatType420f;//SDK only support YUV
        _recorder.useFaceDetect = YES;
        _recorder.faceDetectCount = 2;
        _recorder.faceDectectSync = NO;
        _recorder.encodeMode = 1;
        _recorder.GOP = self.mediaConfig.gop;
        if ([RNAVDeviceHelper isBelowIphone_11]) {
            _recorder.videoQuality = AliyunVideoQualityMedium;
            _recorder.frontCaptureSessionPreset = AVCaptureSessionPreset1280x720;
        } else {
            //iphone 11
            _recorder.bitrate = 15*1000*1000; // 15Mbps
            _recorder.frontCaptureSessionPreset = AVCaptureSessionPreset1920x1080;
        }
        _recorder.recordFps = self.mediaConfig.fps;
        
        _recorder.beautifyStatus = YES;
        _recorder.videoFlipH = self.mediaConfig.videoFlipH;
        _recorder.frontCameraSupportVideoZoomFactor = YES;
        [self recorder:_recorder setMaxDuration:self.mediaConfig.maxDuration];
        [self recorder:_recorder setMinDuration:self.mediaConfig.minDuration];
    }
    return _recorder;
}

- (AliyunMediaConfig *)mediaConfig
{
    if (!_mediaConfig) {
        _mediaConfig = [AliyunMediaConfig defaultConfig];
        _mediaConfig.minDuration = 0.5f;
        _mediaConfig.maxDuration = 180.f; //3min
        _mediaConfig.gop = 30;
        _mediaConfig.cutMode = AliyunMediaCutModeScaleAspectFill;
        _mediaConfig.videoOnly = YES;
        _mediaConfig.backgroundColor = [UIColor blackColor];
        _mediaConfig.videoQuality =  AliyunMediaQualityVeryHight;
        _mediaConfig.outputSize =  CGSizeMake(1080, 1920);
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

- (AliyunDownloadManager *)downloadManager
{
    if (!_downloadManager) {
        _downloadManager = [[AliyunDownloadManager alloc] init];
    }
    return _downloadManager;
}

- (void)addNotification
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appWillResignActive:) name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appDidBecomeActive:) name:UIApplicationDidBecomeActiveNotification object:nil];
}

- (void)appWillResignActive:(id)sender
{
    [self.recorder switchTorchWithMode:AliyunIRecorderTorchModeOff];
    if (self.recorder.isRecording) {
        [self.recorder stopRecording];
        [self.recorder stopPreview];
    }
}

- (void)appDidBecomeActive:(id)sender
{
    [self.recorder startPreview];
}

- (void)addFocusGesture
{
    if (self.recorder.preview && [self.recorder.preview.gestureRecognizers containsObject:self.zoomGesture]) {
        return;
    }
    self.focusGesture =
    [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(focusAndExposeTap:)];
    [self.recorder.preview addGestureRecognizer:self.focusGesture];
}

- (void)addZoomGesture
{
    if (self.recorder.preview && [self.recorder.preview.gestureRecognizers containsObject:self.zoomGesture]) {
        return;
    }
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
    NSString *recordDir = [AliyunPathManager createRecrodDir];
    [AliyunPathManager clearDir:recordDir];
    NSString *taskPath = [recordDir stringByAppendingPathComponent:[AliyunPathManager randomString]];
    NSString *videoSavePath = [[taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
    _videoSavePath = videoSavePath;
    
    self.recorder.outputPath = videoSavePath;
    self.mediaConfig.outputPath = self.recorder.outputPath;
    self.recorder.taskPath = taskPath;
    
    [self.recorder startPreviewWithPositon:AliyunIRecorderCameraPositionFront];
}
- (void)startPreview
{
    NSString *recordDir = [AliyunPathManager createRecrodDir];
    [AliyunPathManager clearDir:recordDir];
    NSString *taskPath = [recordDir stringByAppendingPathComponent:[AliyunPathManager randomString]];
    NSString *videoSavePath = [[taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
    _videoSavePath = videoSavePath;
    
    self.recorder.outputPath = videoSavePath;
    self.mediaConfig.outputPath = self.recorder.outputPath;
    self.recorder.taskPath = taskPath;
    
    [self.recorder startPreview];
}

- (void)stopPreview
{
//    if (self.recorder.cameraPosition == AliyunIRecorderCameraPositionFront) {
//        [self clearBeautyEngine];
//    }
    [_recorder stopPreview];
}

- (void)destroyRecorder
{
    //美颜引擎释放
    [[BeautyEngineManager shareManager] cleanQueenEngine];
    //录制释放
    [_recorder destroyRecorder];
    _recorder = nil;
  
}

- (void)switchCaptureDevicePosition:(AVCaptureDevicePosition)position
{
    AliyunIRecorderCameraPosition cameraPosition =
    (position == AVCaptureDevicePositionFront) ? AliyunIRecorderCameraPositionFront : AliyunIRecorderCameraPositionBack;
    if (cameraPosition != self.recorder.cameraPosition) {
        //previous front，now back then clear
//        if (self.recorder.cameraPosition == AliyunIRecorderCameraPositionFront && cameraPosition == AliyunIRecorderCameraPositionBack) {
//            [self clearBeautyEngine];
//        }
        mRecorderCamerPosition =cameraPosition;
        [self.recorder switchCameraPosition];
    }
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
    if (self.recorder.hasTorch) {
        return NO;
    }
    if (self.recorder.torchMode != tMode) {
        return [self.recorder switchTorchWithMode:tMode];
    }
    return NO;
}

- (BOOL)startRecordVideo:(VideoRecordStartBlk_t)handler;
{
    self.recordStartHandler = handler;
    return ([self.recorder startRecording]) == 0; // ==0 YES
}

- (void)stopRecordVideo:(VideoRecordEndBlk_t)complete;
{
    _complete = complete;
    [self.recorder stopRecording];
}

#pragma mark - face paster
- (void)prepearForAddPasterInfo:(AliyunPasterInfo *)pasterInfo
{
    if (self.recorder.cameraPosition == AliyunIRecorderCameraPositionBack) { //必须是前置摄像头才能添加
        return;
    }
    if (pasterInfo.eid <= 0 || [pasterInfo.bundlePath isEqualToString:@"icon"]) {//remove
        [self deletePreviousEffectPaster];
        return;
    }
    
    if (pasterInfo.bundlePath != nil) {
        [self deletePreviousEffectPaster]; //delete pre
        [self addPasterInfo:pasterInfo path:pasterInfo.bundlePath];
        return;
    }
    
    [self deletePreviousEffectPaster];
    
    if (![pasterInfo fileExist]) {
        AliyunDownloadTask *task = [[AliyunDownloadTask alloc] initWithInfo:pasterInfo];
        [self.downloadManager addTask:task];
        task.progressBlock = ^(NSProgress *progress) {
            CGFloat pgs = progress.completedUnitCount * 1.0 / progress.totalUnitCount;
//            AVDLog(@"------download progress: %lf",pgs);
        };
        __weak typeof(self) weakSelf = self;
        task.completionHandler = ^(NSString *path, NSError *err) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (err) {
                    AVDLog(@"---- download paster error:%@",err.localizedDescription);
                }else{
                    [weakSelf addPasterInfo:pasterInfo path:path];
                }
            });
        };
    } else {
        [self addPasterInfo:pasterInfo path:[pasterInfo filePath]];
    }
}

- (void)addPasterInfo:(AliyunPasterInfo *)info path:(NSString *)path
{
    if(self.recorder.isRecording){
        AVDLog(@"⚠️ recorder is recording, cant't add ⚠️");
        return;
    }
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL result = [fileManager fileExistsAtPath:path];
    if(result) {
        [self deletePreviousEffectPaster];
        AliyunEffectPaster *paster = [[AliyunEffectPaster alloc] initWithFile:path];
        [self.recorder applyPaster:paster];
        _previousEffectPaster = paster;
        //[_pasterView refreshUIWhenThePasterInfoApplyedWithPasterInfo:info];
    }
}

- (void)deletePreviousEffectPaster
{
    if (_previousEffectPaster) {
        [self.recorder deletePaster:_previousEffectPaster];
        AVDLog(@"delete previous paster：%@\n",_previousEffectPaster.path);
        
        _previousEffectPaster = nil;
    }
}

#pragma mark - AliyunIRecorderDelegate

// 设备权限
- (void)recorderDeviceAuthorization:(AliyunIRecorderDeviceAuthor)status
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (status == AliyunIRecorderDeviceAuthorAudioDenied) {
            AVDLog("AliyunIRecorderDeviceAuthorAudioDenied");
        } else if (status == AliyunIRecorderDeviceAuthorVideoDenied) {
            AVDLog("AliyunIRecorderDeviceAuthorVideoDenied");
        }
        //当权限有问题的时候，不会走startPreview，所以这里需要更新下UI
        
    });
}

/// record progress
- (void)recorderVideoDuration:(CGFloat)duration
{
    //    AVDLog(@"----- recording：%f",duration);
    self.isRecording = YES;
    if (self.recordStartHandler) {
        self.recordStartHandler(duration);
    }
}
/// recording stopped
- (void)recorderDidStopRecording
{
    AVDLog(@"recording stopped ");
    [self _recorderFinishRecording];
}

/// multi-part video finish record
- (void)recorderDidFinishRecording
{
    AVDLog(@"✅ finish all record ✅");
    if (_complete) {
        _complete(_videoSavePath);
    }
}

- (void)_recorderFinishRecording
{
    [self.recorder finishRecording];  //will call `recorderDidFinishRecording`
    //save for test
//    [[NSUserDefaults standardUserDefaults] setObject:outputPath forKey:@"videoSavePath"];
    self.recordStartHandler = nil;
}
///while recording time up to limit
- (void)recorderDidStopWithMaxDuration
{
    AVDLog(@" recording time up to limit");
    [self _recorderFinishRecording];
    
}

- (void)recorderDidStartPreview
{
    AVDLog(@"recorderDidStartPreview");
}

/// recorder error
- (void)recoderError:(NSError *)error
{
    AVDLog(@"recoderError%@",error);
}

- (void)destroyRender
{
    [self clearBeautyEngine];
}

- (void)clearBeautyEngine
{
    [[BeautyEngineManager shareManager] clear];
}


- (void)resumeCamera
{
    if(mRecorderCamerPosition == AliyunIRecorderCameraPositionFront){
       [self.recorder startPreviewWithPositon:AliyunIRecorderCameraPositionFront];
    }else if(mRecorderCamerPosition == AliyunIRecorderCameraPositionBack){
       [self.recorder startPreviewWithPositon:AliyunIRecorderCameraPositionBack];
    }else{
        [self.recorder startPreview];
    }
}

- (void)pauseCamera
{
    [self.recorder stopPreview];
}

///beautify  CVPixelBufferRef -> CVPixelBufferRef
- (CVPixelBufferRef)customRenderedPixelBufferWithRawSampleBuffer:(CMSampleBufferRef)sampleBuffer
{
//    if (self.recorder.cameraPosition == AliyunIRecorderCameraPositionBack) {
//        return CMSampleBufferGetImageBuffer(sampleBuffer);
//    }
    //beauty face
    CGFloat beautyBuffing = self.normalBeautyLevel * 0.01 * 2.0f;
    CGFloat beautyWhite = self.normalBeautyLevel * 0.01 * 2.0f;
    CGFloat beautySharpen = self.normalBeautyLevel * 0.01 * 0.2f;
    //beauty shape
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

@end
