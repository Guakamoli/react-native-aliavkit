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

#define IS_IPHONEX (([[UIScreen mainScreen] bounds].size.height<812)?NO:YES)
#define NoStatusBarSafeTop (IS_IPHONEX ? 44 : 0)
#define ScreenWidth  [UIScreen mainScreen].bounds.size.width
#define ScreenHeight  [UIScreen mainScreen].bounds.size.height

@interface AliCameraAction ()<AliyunIRecorderDelegate>

@property (nonatomic, strong) AliyunIRecorder *recorder;        //录制
@property (nonatomic, assign) BOOL shouldStartPreviewWhenActive;    //跳转其他页面停止预览，返回开始预览，退后台进入前台则一直在预览。这2种情况通过此变量区别。
@end

@implementation AliCameraAction

static AliCameraAction *instance = nil;

+ (AliCameraAction *)action
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[AliCameraAction alloc] init];
    });
    return instance;
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
        _recorder =[[AliyunIRecorder alloc] initWithDelegate:self videoSize:self.mediaConfig.outputSize];
        _recorder.clipManager.deleteVideoClipsOnExit = self.mediaConfig.deleteVideoClipOnExit;
        _recorder.preview = [[UIView alloc] initWithFrame:[self previewFrame]];
        _recorder.outputType = AliyunIRecorderVideoOutputPixelFormatType420f;//SDK自带人脸识别只支持YUV格式
        _recorder.useFaceDetect = YES;
        _recorder.faceDetectCount = 2;
        _recorder.faceDectectSync = NO;
        _recorder.frontCaptureSessionPreset = AVCaptureSessionPreset1280x720;
        _recorder.encodeMode = (self.mediaConfig.encodeMode == AliyunEncodeModeSoftFFmpeg) ? 0 : 1;
        NSLog(@"录制编码方式：%d",_recorder.encodeMode);
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
        _mediaConfig.maxDuration = 3*60.0f;
        _mediaConfig.gop = 30;
        _mediaConfig.cutMode = AliyunMediaCutModeScaleAspectFill;
        _mediaConfig.videoOnly = YES;
        _mediaConfig.backgroundColor = [UIColor blackColor];
    }
    return _mediaConfig;
}

- (void)takePhotos:(void (^)(NSData *imageData))handler
{
    if([self.recorder respondsToSelector:@selector(takePhoto:)]) {
        [self.recorder takePhoto:^(UIImage *image, UIImage *rawImage) {
//            NSData *imageData = UIImagePNGRepresentation(rawImage);
            NSData *imageData = UIImageJPEGRepresentation(rawImage, 1.0);
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
    NSLog(@"录制中：%f",duration);
//    [self.progressView updateProgress:duration];
//    [self.bottomView refreshRecorderVideoDuration:duration];
//    self.recorderDuration = duration;
}
/// 录制停止
- (void)recorderDidStopRecording
{
    NSLog(@"----停止录制");
    //    _stopRecordActionUnfinished =NO;
//    _progressView.videoCount = [self partCount];
    //更新录制按钮下方的删除按钮状态
//    [self.bottomView updateViewsWithVideoPartCount:[self partCount]];
}

- (void)recorderDidFinishRecording
{
    NSLog(@"----完成录制");
    
//    [self updateViewsStatus];
    //停止预览
//    [self.recorder stopPreview];
//    self.shouldStartPreviewWhenActive = YES;
//    //跳转处理
//    NSString *outputPath = self.recorder.outputPath;
//    if (self.finishBlock) {
//        self.finishBlock(outputPath);
//    }else{
//        //如果没有编辑类证明是race的demo
//        if (isRace) {
//             UISaveVideoAtPathToSavedPhotosAlbum(self.recorder.outputPath, self, @selector(video:didFinishSavingWithError:contextInfo:), nil);
//            return;
//        }
//
//        if (self.editTaskPath) {
//            [[AlivcShortVideoRoute shared]registerEditVideoPath:nil];
//            [[AlivcShortVideoRoute shared]registerEditMediasPath:self.editTaskPath];
//        } else {
//            [[AlivcShortVideoRoute shared]registerEditVideoPath:outputPath];
//            [[AlivcShortVideoRoute shared]registerEditMediasPath:nil];
//        }
//
//        [[AlivcShortVideoRoute shared]registerMediaConfig:_quVideo];
//        if(_currentMusic && ![_currentMusic.name isEqualToString:NSLocalizedString(@"无音乐" , nil)]){
//            [[AlivcShortVideoRoute shared] registerHasRecordMusic:YES];
//        }else{
//            [[AlivcShortVideoRoute shared] registerHasRecordMusic:NO];
//        }
//        [[AlivcShortVideoRoute shared] registerIsMixedVideo:self.isMixedViedo];
//
//        UIViewController *editVC = [[AlivcShortVideoRoute shared]alivcViewControllerWithType:AlivcViewControlEdit];
//        if (editVC) {
//            // 再添加editVC
//            [self.navigationController pushViewController:editVC animated:YES];
//        }else{
//             UISaveVideoAtPathToSavedPhotosAlbum(self.recorder.outputPath, self, @selector(video:didFinishSavingWithError:contextInfo:), nil);
//        }
//    }
//    [MBProgressHUD hideHUDForView:self.view animated:YES];
}

- (void)finishRecording
{
//    if (self.mediaConfig.deleteVideoClipOnExit || self.isMixedViedo) {
//        [self.recorder finishRecording];
//    } else {
//        self.editTaskPath = [self.recorder finishRecordingForEdit];
//    }
}

///当录至最大时长时回调
- (void)recorderDidStopWithMaxDuration
{
    NSLog(@"录制到最大时长");
//    [MBProgressHUD showHUDAddedTo:self.view animated:YES];
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
//    [MBProgressHUD hideHUDForView:self.view animated:YES];
//    [MBProgressHUD showWarningMessage:[NSString stringWithFormat:@"录制异常：%@",error.localizedDescription] inView:self.view];
//    [self updateViewsStatus];
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
    CGFloat beautyBuffing = 0.60f;
    CGFloat beautyWhite = 0.60f;
    CGFloat beautySharpen = 0.06f; //race中，这个是锐化
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



@end
