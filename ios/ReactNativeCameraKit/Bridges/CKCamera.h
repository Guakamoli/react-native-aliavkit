#import <UIKit/UIKit.h>
#if __has_include(<React/RCTBridge.h>)
#import <React/RCTConvert.h>
#else
#import "RCTConvert.h"
#endif
@import AVFoundation;
@class CKCameraManager;
@class RCTBridge;
#import "AliyunMediaConfig.h"

typedef void (^CaptureBlock)(NSDictionary *imageObject);
typedef void (^VideoRecordBlock)(BOOL successStart);
typedef void (^VideoStopBlock)(NSString *path);
typedef void (^CallbackBlock)(BOOL success);

typedef NS_ENUM(NSInteger, CKCameraType) {
    CKCameraTypeBack,
    CKCameraTypeFront,
};

@interface RCTConvert(CKCameraType)

+ (CKCameraType)CKCameraType:(id)json;

@end

typedef NS_ENUM(NSInteger, CKCameraFlashMode) {
    CKCameraFlashModeAuto,
    CKCameraFlashModeOn,
    CKCameraFlashModeOff
};

@interface RCTConvert(CKCameraFlashMode)

+ (CKCameraFlashMode)CKCameraFlashMode:(id)json;

@end

typedef NS_ENUM(NSInteger, CKCameraTorchMode) {
    CKCameraTorchModeOn,
    CKCameraTorchModeOff
};

@interface RCTConvert(CKCameraTorchMode)

+ (CKCameraTorchMode)CKCameraTorchMode:(id)json;

@end

typedef NS_ENUM(NSInteger, CKCameraFocusMode) {
    CKCameraFocusModeOn,
    CKCameraFocusModeOff,
};

@interface RCTConvert(CKCameraFocusMode)

+ (CKCameraFocusMode)CKCameraFocusMode:(id)json;

@end

typedef NS_ENUM(NSInteger, CKCameraZoomMode) {
    CKCameraZoomModeOn,
    CKCameraZoomModeOff,
};

@interface RCTConvert(CKCameraZoomMode)

+ (CKCameraZoomMode)CKCameraZoomMode:(id)json;

@end



@interface CKCamera : UIView

@property (nonatomic, readonly) AVCaptureDeviceInput *videoDeviceInput;

/// take photo
- (void)snapStillImage:(NSDictionary*)options
               success:(CaptureBlock)block
               onError:(void (^)(NSString*))onError;

+ (NSURL *)saveToTmpFolder:(NSData*)data;

///start record video
- (void)startRecording:(NSDictionary*)options
               success:(VideoRecordBlock)onSuccess
               onError:(void (^)(NSString*))onError;

///stop record video
- (void)stopRecording:(NSDictionary*)options
               success:(VideoStopBlock)onSuccess
              onError:(void (^)(NSString*))onError;

- (void)applyFacePaster:(NSDictionary *)options;
@end
