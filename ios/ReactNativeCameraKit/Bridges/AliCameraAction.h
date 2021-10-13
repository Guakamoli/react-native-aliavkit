//
//  AliCameralController.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/18.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
@class AVEngineConfig;
@class AliyunPasterInfo;

typedef void(^VideoRecordStartBlk_t)(CGFloat duration);
typedef void(^VideoRecordEndBlk_t)(NSString *videoSavePath);

@interface AliCameraAction : NSObject


@property (nonatomic, strong) AVEngineConfig *recordConfig;

@property (nonatomic, strong, readonly) UIView *cameraPreview;

/// 0 10 20 30 40 50, default 30
@property (nonatomic, assign) NSUInteger normalBeautyLevel;

@property (nonatomic, readonly) AVCaptureDevicePosition devicePositon;
@property (nonatomic, readonly) BOOL isRecording;
+ (AliCameraAction *)action;

/// take still image
- (void)takePhotos:(void (^)(NSData *imageData))handler;
- (void)startFrontPreview;
- (void)startPreview;
- (void)stopPreview;


- (void)switchCaptureDevicePosition:(AVCaptureDevicePosition)position;
- (BOOL)switchFlashMode:(AVCaptureFlashMode)mode;

- (BOOL)startRecordVideo:(VideoRecordStartBlk_t)handler;
- (NSString *)stopRecordVideo;

- (void)addFocusGesture;
- (void)removeFocusGesture;

- (void)addZoomGesture;
- (void)removeZoomGesture;

- (void)prepearForAddPasterInfo:(AliyunPasterInfo *)pasterInfo;

@end



