#import "CKCameraManager.h"
#import "CKCamera.h"


@interface CKCameraManager ()

@property (nonatomic, strong) CKCamera *camera;

@end

@implementation CKCameraManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
    if(!self.camera){
        self.camera = [[CKCamera alloc] init];
    }
    return self.camera;
}

RCT_EXPORT_VIEW_PROPERTY(cameraType, CKCameraType)
RCT_EXPORT_VIEW_PROPERTY(flashMode, CKCameraFlashMode)
RCT_EXPORT_VIEW_PROPERTY(focusMode, CKCameraFocusMode)
RCT_EXPORT_VIEW_PROPERTY(zoomMode, CKCameraZoomMode)

RCT_EXPORT_VIEW_PROPERTY(frameColor, UIColor)
RCT_EXPORT_VIEW_PROPERTY(saveToCameraRoll, BOOL)
RCT_EXPORT_VIEW_PROPERTY(saveToCameraRollWithPhUrl, BOOL)
RCT_EXPORT_VIEW_PROPERTY(onRecordingProgress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(facePasterInfo, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(mediaConfig, AliyunMediaConfig)

/// 0 10 20 30 40 50, default 30
RCT_EXPORT_VIEW_PROPERTY(normalBeautyLevel, NSUInteger)
RCT_EXPORT_VIEW_PROPERTY(cameraStyle, NSDictionary)

RCT_EXPORT_METHOD(capture:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [self.camera snapStillImage:options success:^(NSDictionary *imageObject) {
        resolve(imageObject);
    } onError:^(NSString* error) {
        reject(@"capture_error", error, nil);
    }];
}

RCT_EXPORT_METHOD(setPasterInfo:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    if (!options || [options isEqualToDictionary:@{}]) {
        reject(@"",@"params can't be null or empty",nil);
    }
    [self.camera applyFacePaster:options];
}


RCT_EXPORT_METHOD(startRecording:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [self.camera startRecording:options success:^(BOOL successStart) {
        resolve(@(successStart));
    } onError:^(NSString *error) {
        reject(@"record_error", error, nil);
    }];
}

RCT_EXPORT_METHOD(stopRecording:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [self.camera stopRecording:options success:^(NSString *path) {
        resolve(path);
    } onError:^(NSString *error) {
        reject(@"stop_record_error", error, nil);
    }];
}

RCT_EXPORT_METHOD(checkDeviceCameraAuthorizationStatus:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject)
{
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if(authStatus == AVAuthorizationStatusAuthorized) {
        resolve(@YES);
    } else if(authStatus == AVAuthorizationStatusNotDetermined) {
        resolve(@(-1));
    } else {
        resolve(@NO);
    }
}

RCT_EXPORT_METHOD(requestDeviceCameraAuthorization:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject)
{
    __block NSString *mediaType = AVMediaTypeVideo;

    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        if (resolve) {
            resolve(@(granted));
        }
    }];
}

RCT_EXPORT_METHOD(cameraStopPreview)
{
//    [self.camera recorderStopPreview];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

@end
