@import Foundation;
@import Photos;

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/UIView+React.h>
#import <React/RCTConvert.h>
#else
#import "UIView+React.h"
#import "RCTConvert.h"
#endif

#import "CKCamera.h"
#import "CKCameraOverlayView.h"
#import "CKMockPreview.h"

#import "AliCameraAction.h"
#import "CKCameraManager.h"
#import "AliyunPasterInfo.h"
#import "AliyunPathManager.h"
#import "AliyunPhotoLibraryManager.h"

@implementation RCTConvert(CKCameraType)

RCT_ENUM_CONVERTER(CKCameraType, (@{
    @"back": @(AVCaptureDevicePositionBack),
    @"front": @(AVCaptureDevicePositionFront),
}), AVCaptureDevicePositionFront, integerValue)
@end

@implementation RCTConvert(CKCameraTorchMode)

RCT_ENUM_CONVERTER(CKCameraTorchMode, (@{
    @"on": @(AVCaptureTorchModeOn),
    @"off": @(AVCaptureTorchModeOff)
}), AVCaptureTorchModeOff, integerValue)
@end

@implementation RCTConvert(CKCameraFlashMode)

RCT_ENUM_CONVERTER(CKCameraFlashMode, (@{
    @"auto": @(AVCaptureFlashModeAuto),
    @"on": @(AVCaptureFlashModeOn),
    @"off": @(AVCaptureFlashModeOff)
}), AVCaptureFlashModeAuto, integerValue)

@end

@implementation RCTConvert(CKCameraFocusMode)

RCT_ENUM_CONVERTER(CKCameraFocusMode, (@{
    @"on": @(CKCameraFocusModeOn),
    @"off": @(CKCameraFocusModeOff)
}), CKCameraFocusModeOn, integerValue)

@end

@implementation RCTConvert(CKCameraZoomMode)

RCT_ENUM_CONVERTER(CKCameraZoomMode, (@{
    @"on": @(CKCameraZoomModeOn),
    @"off": @(CKCameraZoomModeOff)
}), CKCameraZoomModeOn, integerValue)

@end

@interface CKCamera () <AVCaptureMetadataOutputObjectsDelegate>


@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, strong) CKMockPreview *mockPreview;
@property (nonatomic, strong) CKCameraOverlayView *cameraOverlayView;


@property (nonatomic) BOOL resetFocusWhenMotionDetected;

@property (nonatomic) BOOL saveToCameraRoll;
@property (nonatomic) BOOL saveToCameraRollWithPhUrl;

// camera options
@property (nonatomic) AVCaptureDevicePosition cameraType;
@property (nonatomic) AVCaptureFlashMode flashMode;
@property (nonatomic) AVCaptureTorchMode torchMode;
@property (nonatomic) CKCameraFocusMode focusMode;
@property (nonatomic) CKCameraZoomMode zoomMode;
@property (nonatomic, strong) NSString* ratioOverlay;
@property (nonatomic, strong) UIColor *ratioOverlayColor;
@property (nonatomic, strong) RCTDirectEventBlock onOrientationChange;

//@property (nonatomic) BOOL isAddedOberver;

@property (nonatomic, strong) AliCameraAction *cameraAction;

@property (nonatomic) NSUInteger normalBeautyLevel;
@property (nonatomic, copy) RCTBubblingEventBlock onRecordingProgress;

@property (nonatomic, strong) NSDictionary *facePasterInfo;

@end

@implementation CKCamera

#pragma mark - initializtion

- (void)dealloc
{
    
}

- (void)willMoveToSuperview:(UIView *)newSuperview
{
    if (!newSuperview) {
        NSLog(@"---✅ %s",__PRETTY_FUNCTION__);
//        [self.cameraAction stopRecordVideo];
//        [self.cameraAction stopPreview];
    } else {
        NSLog(@"--- %s",__PRETTY_FUNCTION__);
        if (self.cameraAction) {
            [self.cameraAction startFrontPreview];
        }
    }
}

- (AliCameraAction *)cameraAction {
    if (!_cameraAction) {
        _cameraAction = [AliCameraAction action];
    }
    return _cameraAction;
}

- (instancetype)init
{
    if (self = [super init]) {
//        [self.cameraAction startFrontPreview];
    }
    return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    [self addSubview:self.cameraAction.cameraPreview];
}

- (void)setNormalBeautyLevel:(NSUInteger)normalBeautyLevel
{
    if (normalBeautyLevel != _normalBeautyLevel) {
        _normalBeautyLevel = normalBeautyLevel;
        self.cameraAction.normalBeautyLevel = normalBeautyLevel;
    }
}

- (void)setCameraType:(AVCaptureDevicePosition)cameraType
{
    if (_cameraType != cameraType) {
        _cameraType = cameraType;
        [self changeCamera:cameraType];
    }
}

- (void)changeCamera:(AVCaptureDevicePosition)preferredPosition
{
#if TARGET_IPHONE_SIMULATOR
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.mockPreview randomize];
    });
    return;
#endif
    
    [self.cameraAction switchCaptureDevicePosition:preferredPosition];
}

- (void)setFlashMode:(AVCaptureFlashMode)flashMode {
    if (flashMode != _flashMode) {
        _flashMode = flashMode;
        if (self.cameraAction.devicePositon == AVCaptureDevicePositionBack) {
            [self.cameraAction switchFlashMode:flashMode];
        }
    }
}

-(void)setTorchMode:(AVCaptureTorchMode)torchMode {
    _torchMode = torchMode;
    if (self.videoDeviceInput && [self.videoDeviceInput.device isTorchModeSupported:torchMode] && self.videoDeviceInput.device.hasTorch) {
        NSError* err = nil;
        if ( [self.videoDeviceInput.device lockForConfiguration:&err] ) {
            [self.videoDeviceInput.device setTorchMode:torchMode];
            [self.videoDeviceInput.device unlockForConfiguration];
        }
    }
}

- (void)setFocusMode:(CKCameraFocusMode)focusMode {
    _focusMode = focusMode;
    if (self.focusMode == CKCameraFocusModeOn) {
        [self.cameraAction addFocusGesture];
    } else {
        [self.cameraAction removeFocusGesture];
    }
}

- (void)setZoomMode:(CKCameraZoomMode)zoomMode {
    _zoomMode = zoomMode;
    if (zoomMode == CKCameraZoomModeOn) {
        [self.cameraAction addZoomGesture];
    } else {
        [self.cameraAction removeZoomGesture];
    }
}

- (void)setRatio:(NSString*)ratio {
    if (ratio && ![ratio isEqualToString:@""]) {
        self.ratioOverlay = ratio;
    }
}

- (void)setOnRecordingProgress:(RCTBubblingEventBlock)onRecordingProgress
{
    if (_onRecordingProgress != onRecordingProgress) {
        _onRecordingProgress = onRecordingProgress;
    }
}

- (void)setFacePasterInfo:(NSDictionary *)facePasterInfo
{
    if (_facePasterInfo != facePasterInfo) {
        _facePasterInfo = facePasterInfo;
        if (![facePasterInfo isEqualToDictionary:@{}]) {
            [self applyFacePaster:facePasterInfo];
        }
    }
}

-(void)reactSetFrame:(CGRect)frame
{
    [super reactSetFrame:frame];
}

-(void)setRatioOverlay:(NSString *)ratioOverlay {
    _ratioOverlay = ratioOverlay;
    [self.cameraOverlayView setRatio:self.ratioOverlay];
}

-(void)setOverlayRatioView
{
    if (self.ratioOverlay) {
        [self.cameraOverlayView removeFromSuperview];
        self.cameraOverlayView = [[CKCameraOverlayView alloc] initWithFrame:self.bounds ratioString:self.ratioOverlay overlayColor:self.ratioOverlayColor];
        [self addSubview:self.cameraOverlayView];
    }
}

#pragma mark - actions

- (void)applyFacePaster:(NSDictionary *)options
{
    AliyunPasterInfo *info = [[AliyunPasterInfo alloc] initWithDict:options];
    
    //handle for local resource
    NSString *bundlePath = [options valueForKey:@"bundlePath"];
    if (bundlePath) {
        info = [[AliyunPasterInfo alloc] initWithBundleFile:bundlePath];
    }

    [self.cameraAction prepearForAddPasterInfo:info];
}

- (void)startRecording:(NSDictionary *)options
               success:(VideoRecordBlock)onSuccess
               onError:(void (^)(NSString *))onError
{
    __weak typeof(self) weakSelf = self;
    BOOL isRecording = [self.cameraAction startRecordVideo:^(CGFloat duration) {
        NSDictionary *event = @{
            @"target": self.reactTag,
            @"duration": @(duration)
        };
        weakSelf.onRecordingProgress(event);
    }];
    onSuccess(isRecording);
}

- (void)stopRecording:(NSDictionary *)options
              success:(VideoStopBlock)onSuccess
              onError:(void (^)(NSString *))onError
{
//    NSString *path = [self.cameraAction stopRecordVideo];
//    onSuccess(path);
    
    [self.cameraAction stopRecordVideo:^(NSString *videoSavePath) {
        if (videoSavePath) {
            onSuccess(videoSavePath);
        } else {
            onError(@"no path exist");
        }
    }];
}

- (void)snapStillImage:(NSDictionary*)options
               success:(CaptureBlock)onSuccess
               onError:(void (^)(NSString*))onError
{
#if TARGET_IPHONE_SIMULATOR

    
#endif
    
    [self.cameraAction takePhotos:^(NSData *imageData) {
        [self writeCapturedImageData:imageData onSuccess:onSuccess onError:onError];
    }];
}

- (void)saveImageDataToSandBox:(NSData *)imageData
{
    NSString *editDir = [AliyunPathManager compositionRootDir];
    NSString *taskPath = [editDir stringByAppendingPathComponent:[AliyunPathManager randomString]];
    if ([[NSFileManager defaultManager] fileExistsAtPath:taskPath]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:taskPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    NSString *coverPath = [taskPath stringByAppendingPathComponent:@"cover.jpg"];
//    NSData *data = UIImagePNGRepresentation(_image);
   BOOL success = [imageData writeToFile:coverPath atomically:YES];
    
    if (success) {
        NSLog(@"----- coverPath: %@",coverPath);
    }
}

- (void)saveAsset:(PHAsset *)asset
{
    NSString *tmpPhotoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"jpg"];
    [[AliyunPhotoLibraryManager sharedManager] savePhotoWithAsset:asset
                                                          maxSize:CGSizeMake(1080, 1920)
                                                       outputPath:tmpPhotoPath
                                                       completion:^(NSError *error, UIImage * _Nullable result) {
        [[NSUserDefaults standardUserDefaults] setObject:tmpPhotoPath forKey:@"photoPath"];
    }];
}

- (void)writeCapturedImageData:(NSData *)imageData
                     onSuccess:(CaptureBlock)onSuccess
                       onError:(void (^)(NSString *))onError
{
    NSMutableDictionary *imageInfoDict = [[NSMutableDictionary alloc] init];
    
    NSNumber *length = [NSNumber numberWithInteger:imageData.length];
    if (length) {
        imageInfoDict[@"size"] = length;
    }
    
    if (self.saveToCameraRoll) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            if (status != PHAuthorizationStatusAuthorized) {
                onError(@"Photo library permission is not authorized.");
                return;
            }
            [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
                // To preserve the metadata, we create an asset from the JPEG NSData representation.
                // Note that creating an asset from a UIImage discards the metadata.
                // In iOS 9, we can use -[PHAssetCreationRequest addResourceWithType:data:options].
                [[PHAssetCreationRequest creationRequestForAsset] addResourceWithType:PHAssetResourceTypePhoto data:imageData options:nil];
            } completionHandler:^(BOOL success, NSError *error) {
                if (!success) {
                    NSLog(@"Could not save to camera roll");
                    onError(@"Photo library asset creation failed");
                    return;
                }
                
                // Get local identifier
                PHFetchResult *fetchResult = [PHAsset fetchAssetsWithMediaType:PHAssetMediaTypeImage options:self.fetchOptions];
                PHAsset *firstAsset = [fetchResult firstObject];
                NSString *localIdentifier = firstAsset.localIdentifier;
                
                if (localIdentifier) {
                    imageInfoDict[@"id"] = localIdentifier;
                }
                
                // 'ph://' is a rnc/cameraroll URL scheme for loading PHAssets by localIdentifier
                // which are loaded via RNCAssetsLibraryRequestHandler module that conforms to RCTURLRequestHandler
                if (self.saveToCameraRollWithPhUrl) {
                    NSString *photoURI = [NSString stringWithFormat:@"ph://%@", localIdentifier];
                    imageInfoDict[@"uri"] = photoURI;
                    
                    NSString *tmpPhotoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"jpg"];
                    [[AliyunPhotoLibraryManager sharedManager] savePhotoWithAsset:firstAsset maxSize:CGSizeMake(1080, 1920) outputPath:tmpPhotoPath completion:^(NSError *error, UIImage * _Nullable result) {
                        imageInfoDict[@"photoPath"] = tmpPhotoPath;
                        dispatch_async(dispatch_get_main_queue(), ^{
                            onSuccess(imageInfoDict);
                        });
                    }];
                    
                } else {
                    PHContentEditingInputRequestOptions *options = [[PHContentEditingInputRequestOptions alloc] init];
                    [options setNetworkAccessAllowed:YES];
                    [firstAsset requestContentEditingInputWithOptions:options completionHandler:^(PHContentEditingInput * _Nullable contentEditingInput, NSDictionary * _Nonnull info) {
                        imageInfoDict[@"uri"] = contentEditingInput.fullSizeImageURL.absoluteString;
                    }];
                    
                    NSString *tmpPhotoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"jpg"];
                    [[AliyunPhotoLibraryManager sharedManager] savePhotoWithAsset:firstAsset maxSize:CGSizeMake(1080, 1920) outputPath:tmpPhotoPath completion:^(NSError *error, UIImage * _Nullable result) {
                        imageInfoDict[@"photoPath"] = tmpPhotoPath;
//                        [[NSUserDefaults standardUserDefaults] setObject:tmpPhotoPath forKey:@"photoPath"]; //for test only
                        dispatch_async(dispatch_get_main_queue(), ^{
                            onSuccess(imageInfoDict);
                        });
                    }];
                }
            }];
        }];
    } else {
        NSURL *temporaryFileURL = [CKCamera saveToTmpFolder:imageData];
        if (temporaryFileURL) {
            imageInfoDict[@"uri"] = temporaryFileURL.description;
            imageInfoDict[@"name"] = temporaryFileURL.lastPathComponent;
        }
        
        onSuccess(imageInfoDict);
    }
}

- (PHFetchOptions *)fetchOptions
{
    PHFetchOptions *fetchOptions = [PHFetchOptions new];
    fetchOptions.sortDescriptors = @[[NSSortDescriptor sortDescriptorWithKey:@"creationDate" ascending:NO]];
    fetchOptions.predicate = [NSPredicate predicateWithFormat:@"mediaType = %d && creationDate <= %@",PHAssetMediaTypeImage, [NSDate date]];
    // iOS 9+
    if ([fetchOptions respondsToSelector:@selector(fetchLimit)]) {
        fetchOptions.fetchLimit = 1;
    }
    
    return fetchOptions;
}

+ (NSURL*)saveToTmpFolder:(NSData*)data
{
    NSString *temporaryFileName = [NSProcessInfo processInfo].globallyUniqueString;
    NSString *temporaryFilePath = [NSTemporaryDirectory() stringByAppendingPathComponent:[temporaryFileName stringByAppendingPathExtension:@"jpg"]];
    NSURL *temporaryFileURL = [NSURL fileURLWithPath:temporaryFilePath];
    
    NSError *error = nil;
    [data writeToURL:temporaryFileURL options:NSDataWritingAtomic error:&error];
    if (error) {
        NSLog(@"Error occured while writing image data to a temporary file: %@", error);
    }
    return temporaryFileURL;
}

@end

