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
#import "AliCameraAction.h"
#import "CKCameraManager.h"
#import "AliyunPasterInfo.h"
#import "AliyunPathManager.h"
#import "AliyunPhotoLibraryManager.h"
#import "ShortCut.h"
#import "BeautyEngineManager.h"

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
{
    BOOL _isPresented;
}

@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic) BOOL resetFocusWhenMotionDetected;
@property (nonatomic) BOOL saveToCameraRoll;
@property (nonatomic) BOOL saveToCameraRollWithPhUrl;

// camera options
@property (nonatomic) AVCaptureDevicePosition cameraType;
@property (nonatomic) AVCaptureFlashMode flashMode;
@property (nonatomic) CKCameraFocusMode focusMode;
@property (nonatomic) CKCameraZoomMode zoomMode;
@property (nonatomic, strong) NSString* ratioOverlay;
@property (nonatomic, strong) UIColor *ratioOverlayColor;

@property (nonatomic, strong) AliCameraAction *cameraAction;

@property (nonatomic) NSUInteger normalBeautyLevel;
@property (nonatomic, copy) RCTBubblingEventBlock onRecordingProgress;

@property (nonatomic, strong) NSDictionary *facePasterInfo;
@property (nonatomic, strong) NSDictionary *cameraStyle;
@end

@implementation CKCamera

#pragma mark - life cycle
- (void)didMoveToSuperview
{
    [super didMoveToSuperview];
    if (!self.superview && _isPresented) {
        if (self.cameraAction.isRecording) {
            [self.cameraAction stopRecordVideo:nil];
        }
        [self.cameraAction stopPreview];
        if ([self.subviews containsObject:self.cameraAction.cameraPreview]) {
            [self.cameraAction.cameraPreview removeFromSuperview];
        }
        _isPresented = NO;
        [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
    }
}

- (void)didMoveToWindow
{
    [super didMoveToWindow];
    if (!_isPresented && self.window) {
        AVDLog(@"----： 📷 ready to appear");
        if (self.cameraAction && !self.cameraAction.isRecording) {
            if (![self.subviews containsObject:self.cameraAction.cameraPreview]) {
                [self addSubview:self.cameraAction.cameraPreview];
            }
            [self.cameraAction startPreview];
            [self.cameraAction deletePreviousEffectPaster];
            [self setupDefault];
            [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
        }
        _isPresented = YES;
    }
    if (!self.window && _isPresented) {
        if (self.cameraAction.isRecording) {
            [self.cameraAction stopRecordVideo:nil];
        }
        [self.cameraAction stopPreview];
        if ([self.subviews containsObject:self.cameraAction.cameraPreview]) {
            [self.cameraAction.cameraPreview removeFromSuperview];
        }
        _isPresented = NO;
        [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
    }
}

- (instancetype)init
{
    if (self = [super init]) {
        _isPresented = NO;
    }
    return self;
}

- (void)setupDefault
{
    self.cameraAction.normalBeautyLevel = _normalBeautyLevel;
    [self changeCamera:_cameraType];
    if (self.cameraAction.devicePositon == AVCaptureDevicePositionBack) {
        [self.cameraAction switchFlashMode:_flashMode];
    }
    if (_focusMode == CKCameraFocusModeOn) {
        [self.cameraAction addFocusGesture];
    } else {
        [self.cameraAction removeFocusGesture];
    }
    if (_zoomMode == CKCameraZoomModeOn) {
        [self.cameraAction addZoomGesture];
    } else {
        [self.cameraAction removeZoomGesture];
    }
    
    if (_facePasterInfo && ![_facePasterInfo isEqualToDictionary:@{}]) {
        [self applyFacePaster:_facePasterInfo];
    }
}

#pragma mark - Setter

- (void)setCameraStyle:(NSDictionary *)cameraStyle
{
    if (cameraStyle != _cameraStyle && ![cameraStyle isEqualToDictionary:@{}]) {
        CGFloat previewWidth = [[cameraStyle valueForKey:@"width"] floatValue];
        CGFloat previewHeight = [[cameraStyle valueForKey:@"height"] floatValue];
        self.cameraAction = [[AliCameraAction alloc] initWithPreviewFrame:CGRectMake(0, 0, previewWidth, previewHeight)];
    }
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

    return;
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
        AVDLog(@"----- coverPath: %@",coverPath);
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
                    AVDLog(@"Could not save to camera roll");
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
        AVDLog(@"Error occured while writing image data to a temporary file: %@", error);
    }
    return temporaryFileURL;
}

@end

