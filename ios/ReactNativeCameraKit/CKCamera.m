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

static void * CapturingStillImageContext = &CapturingStillImageContext;
static void * SessionRunningContext = &SessionRunningContext;

typedef NS_ENUM( NSInteger, CKSetupResult ) {
    CKSetupResultSuccess,
    CKSetupResultCameraNotAuthorized,
    CKSetupResultSessionConfigurationFailed
};

@implementation RCTConvert(CKCameraType)

RCT_ENUM_CONVERTER(CKCameraType, (@{
                                         @"back": @(AVCaptureDevicePositionBack),
                                         @"front": @(AVCaptureDevicePositionFront),
                                         }), AVCaptureDevicePositionBack, integerValue)
@end

@implementation RCTConvert(CKCameraTorchMode)

RCT_ENUM_CONVERTER(CKCameraTorchMode, (@{
                                         @"on": @(AVCaptureTorchModeOn),
                                         @"off": @(AVCaptureTorchModeOff)
                                         }), AVCaptureTorchModeAuto, integerValue)
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

// session management
@property (nonatomic) dispatch_queue_t sessionQueue;
@property (nonatomic) AVCaptureSession *session;
@property (nonatomic, readwrite) AVCaptureDeviceInput *videoDeviceInput;
@property (nonatomic) AVCaptureMovieFileOutput *movieFileOutput;
@property (nonatomic) AVCaptureStillImageOutput *stillImageOutput;
@property (nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property (nonatomic, strong) NSString *codeStringValue;


// utilities
@property (nonatomic) CKSetupResult setupResult;
@property (nonatomic, getter=isSessionRunning) BOOL sessionRunning;
@property (nonatomic) UIBackgroundTaskIdentifier backgroundRecordingID;

// scanner options
@property (nonatomic) BOOL showFrame;
@property (nonatomic) UIView *scannerView;
@property (nonatomic, strong) RCTDirectEventBlock onReadCode;
@property (nonatomic) CGFloat frameOffset;
@property (nonatomic) CGFloat frameHeight;
@property (nonatomic, strong) UIColor *laserColor;
@property (nonatomic, strong) UIColor *frameColor;
@property (nonatomic) UIView * dataReadingFrame;

// camera options
@property (nonatomic) AVCaptureDevicePosition cameraType;
@property (nonatomic) AVCaptureFlashMode flashMode;
@property (nonatomic) AVCaptureTorchMode torchMode;
@property (nonatomic) CKCameraFocusMode focusMode;
@property (nonatomic) CKCameraZoomMode zoomMode;
@property (nonatomic, strong) NSString* ratioOverlay;
@property (nonatomic, strong) UIColor *ratioOverlayColor;
@property (nonatomic, strong) RCTDirectEventBlock onOrientationChange;

@property (nonatomic) BOOL isAddedOberver;

@property (nonatomic, strong) AliCameraAction *cameraAction;
@property (nonatomic, weak) CKCameraManager *manager;
@property (nonatomic, weak) RCTBridge *bridge;

@end

@implementation CKCamera

#pragma mark - initializtion

- (void)dealloc
{
    [self removeObservers];
}

-(PHFetchOptions *)fetchOptions
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

- (void)removeReactSubview:(UIView *)subview
{
    [subview removeFromSuperview];
    [super removeReactSubview:subview];
}

- (void)removeFromSuperview
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:[UIDevice currentDevice]];
    [super removeFromSuperview];
}

- (AliCameraAction *)cameraAction {
    if (!_cameraAction) {
        _cameraAction = [AliCameraAction action];
    }
    return _cameraAction;
}

- (instancetype)initWithManager:(CKCameraManager*)manager bridge:(RCTBridge *)bridge
{
  if ((self = [super init])) {
    self.manager = manager;
    self.bridge = bridge;
    [self.cameraAction startFrontPreview];
  }
  return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    [self addSubview:self.cameraAction.cameraPreview];
}

- (void)setCameraType:(AVCaptureDevicePosition)cameraType
{
    if (cameraType != _cameraType) {
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
    [self.cameraAction switchCameraPosition];
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

- (void)setLaserColor:(UIColor *)color {
    if (color != nil) {
        _laserColor = color;
    }
}

- (void)setFrameColor:(UIColor *)color {
    if (color != nil) {
        _frameColor = color;
    }
}

- (void) orientationChanged:(NSNotification *)notification
{
    if (!self.onOrientationChange) {
        return;
    }

    // PORTRAIT: 0, // ⬆️
    // LANDSCAPE_LEFT: 1, // ⬅️
    // PORTRAIT_UPSIDE_DOWN: 2, // ⬇️
    // LANDSCAPE_RIGHT: 3, // ➡️
    
    UIDevice * device = notification.object;
    UIDeviceOrientation orientation = device.orientation;
    if (orientation == UIDeviceOrientationPortrait) {
        self.onOrientationChange(@{@"orientation": @0});
    } else if (orientation == UIDeviceOrientationLandscapeLeft) {
        self.onOrientationChange(@{@"orientation": @1});
    } else if (orientation ==  UIDeviceOrientationPortraitUpsideDown) {
        self.onOrientationChange(@{@"orientation": @2});
    } else if (orientation == UIDeviceOrientationLandscapeRight) {
        self.onOrientationChange(@{@"orientation": @3});
    }
}

- (void) setupCaptureSession {
    // Setup the capture session.
    // In general it is not safe to mutate an AVCaptureSession or any of its inputs, outputs, or connections from multiple threads at the same time.
    // Why not do all of this on the main queue?
    // Because -[AVCaptureSession startRunning] is a blocking call which can take a long time. We dispatch session setup to the sessionQueue
    // so that the main queue isn't blocked, which keeps the UI responsive.
    dispatch_async( self.sessionQueue, ^{
        if ( self.setupResult != CKSetupResultSuccess ) {
            return;
        }

        self.backgroundRecordingID = UIBackgroundTaskInvalid;
        NSError *error = nil;

        AVCaptureDevice *videoDevice = [CKCamera deviceWithMediaType:AVMediaTypeVideo preferringPosition:AVCaptureDevicePositionBack];
        AVCaptureDeviceInput *videoDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:videoDevice error:&error];

        [self.session beginConfiguration];

        if ( [self.session canAddInput:videoDeviceInput] ) {
            [self.session addInput:videoDeviceInput];
            self.videoDeviceInput = videoDeviceInput;
//            [CKCamera setFlashMode:self.flashMode forDevice:self.videoDeviceInput.device];
        }
        else {
            self.setupResult = CKSetupResultSessionConfigurationFailed;
        }

        AVCaptureMovieFileOutput *movieFileOutput = [[AVCaptureMovieFileOutput alloc] init];
        if ( [self.session canAddOutput:movieFileOutput] ) {
            [self.session addOutput:movieFileOutput];
            AVCaptureConnection *connection = [movieFileOutput connectionWithMediaType:AVMediaTypeVideo];
            if ( connection.isVideoStabilizationSupported ) {
                connection.preferredVideoStabilizationMode = AVCaptureVideoStabilizationModeAuto;
            }
            self.movieFileOutput = movieFileOutput;
        }
        else {
            self.setupResult = CKSetupResultSessionConfigurationFailed;
        }

        AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
        if ( [self.session canAddOutput:stillImageOutput] ) {
            stillImageOutput.outputSettings = @{AVVideoCodecKey : AVVideoCodecJPEG};
            [self.session addOutput:stillImageOutput];
            self.stillImageOutput = stillImageOutput;
        }
        else {
            self.setupResult = CKSetupResultSessionConfigurationFailed;
        }

        AVCaptureMetadataOutput * output = [[AVCaptureMetadataOutput alloc] init];
        if ([self.session canAddOutput:output]) {
            self.metadataOutput = output;
            [self.session addOutput:self.metadataOutput];
            [self.metadataOutput setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
            [self.metadataOutput setMetadataObjectTypes:[self.metadataOutput availableMetadataObjectTypes]];
        }

        [self.session commitConfiguration];

    } );
}

-(void)handleCameraPermission {

    switch ( [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo] )
    {
        case AVAuthorizationStatusAuthorized:
        {
            // The user has previously granted access to the camera.
            break;
        }
        case AVAuthorizationStatusNotDetermined:
        {
            // The user has not yet been presented with the option to grant video access.
            // We suspend the session queue to delay session setup until the access request has completed to avoid
            // asking the user for audio access if video access is denied.
            // Note that audio access will be implicitly requested when we create an AVCaptureDeviceInput for audio during session setup.
            dispatch_suspend( self.sessionQueue );
            [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^( BOOL granted ) {
                if ( ! granted ) {
                    self.setupResult = CKSetupResultCameraNotAuthorized;
                }
                dispatch_resume( self.sessionQueue );
            }];
            break;
        }
        default:
        {
            // The user has previously denied access.
            self.setupResult = CKSetupResultCameraNotAuthorized;
            break;
        }
    }
}

-(void)reactSetFrame:(CGRect)frame {
    [super reactSetFrame:frame];
    [self.cameraAction setCameraPreviewFrame:frame];
}

-(void)setRatioOverlay:(NSString *)ratioOverlay {
    _ratioOverlay = ratioOverlay;
    [self.cameraOverlayView setRatio:self.ratioOverlay];
}

-(void)setOverlayRatioView {
    if (self.ratioOverlay) {
        [self.cameraOverlayView removeFromSuperview];
        self.cameraOverlayView = [[CKCameraOverlayView alloc] initWithFrame:self.bounds ratioString:self.ratioOverlay overlayColor:self.ratioOverlayColor];
        [self addSubview:self.cameraOverlayView];
    }
}


#pragma mark -


+ (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position {
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:mediaType];
    AVCaptureDevice *captureDevice = devices.firstObject;

    for (AVCaptureDevice *device in devices) {
        if (device.position == position) {
            captureDevice = device;
            break;
        }
    }

    return captureDevice;
}


#pragma mark - actions

- (void)startRecording:(NSDictionary*)options
               success:(VideoRecordBlock)onSuccess
               onError:(void (^)(NSString*))onError
{
    BOOL isRecording = [self.cameraAction startRecordVideo:^(CGFloat duration) {
        NSDictionary *event = @{
            @"target": self.reactTag,
            @"duration": @(duration)
        };
        [self.bridge.eventDispatcher sendAppEventWithName:@"startVideoRecord" body:event];
    }];
    onSuccess(isRecording);
}

- (void)stopRecording:(NSDictionary*)options
               success:(VideoStopBlock)onSuccess
               onError:(void (^)(NSString*))onError
{
    NSString *path = [self.cameraAction stopRecordVideo];
    onSuccess(path);
}

- (void)snapStillImage:(NSDictionary*)options success:(CaptureBlock)onSuccess onError:(void (^)(NSString*))onError {
    
    #if TARGET_IPHONE_SIMULATOR
    [self capturePreviewLayer:options success:onSuccess onError:onError];
    return;
    #endif

    [self.cameraAction takePhotos:^(NSData *imageData) {
        [self writeCapturedImageData:imageData onSuccess:onSuccess onError:onError];
    }];
    
}

- (void)capturePreviewLayer:(NSDictionary*)options success:(CaptureBlock)onSuccess onError:(void (^)(NSString*))onError
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.mockPreview != nil) {
            UIImage *previewSnapshot = [self.mockPreview snapshotWithTimestamp:YES]; // Generate snapshot from main UI thread
            dispatch_async( self.sessionQueue, ^{ // write image async
                [self writeCapturedImageData:UIImagePNGRepresentation(previewSnapshot) onSuccess:onSuccess onError:onError];
            });
        } else {
            onError(@"Simulator image could not be captured from preview layer");
        }
    });
}

/*
 {
     "size": 1.0,
     "id": "xxxxxxxxxxxxxxx",
     "uri": "ph://",
     "name": "名字"
 }
 */
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
                    imageInfoDict[@"uri"] = [NSString stringWithFormat:@"ph://%@", localIdentifier];
                    dispatch_async(dispatch_get_main_queue(), ^{
                        onSuccess(imageInfoDict);
                    });
                } else {
                    PHContentEditingInputRequestOptions *options = [[PHContentEditingInputRequestOptions alloc] init];
                    [options setNetworkAccessAllowed:YES];
                    [firstAsset requestContentEditingInputWithOptions:options completionHandler:^(PHContentEditingInput * _Nullable contentEditingInput, NSDictionary * _Nonnull info) {
                        imageInfoDict[@"uri"] = contentEditingInput.fullSizeImageURL.absoluteString;

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

+(NSURL*)saveToTmpFolder:(NSData*)data
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

//#pragma mark - Frame for Scanner Settings
//
//- (void)didMoveToWindow {
//    [super didMoveToWindow];
//    if (self.sessionRunning && self.dataReadingFrame) {
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [self startAnimatingScanner:self.dataReadingFrame];
//        });
//    }
//}
//
//- (void)addFrameForScanner {
//    CGFloat frameWidth = self.bounds.size.width - 2 * self.frameOffset;
//    if (!self.dataReadingFrame) {
//        self.dataReadingFrame = [[UIView alloc] initWithFrame:CGRectMake(0, 0, frameWidth, self.frameHeight)]; //
//        self.dataReadingFrame.center = self.center;
//        self.dataReadingFrame.backgroundColor = [UIColor clearColor];
//        [self createCustomFramesForView:self.dataReadingFrame];
//        [self addSubview:self.dataReadingFrame];
//
//        [self startAnimatingScanner:self.dataReadingFrame];
//
//        [self addVisualEffects:self.dataReadingFrame.frame];
//
//        CGRect visibleRect = [self.previewLayer metadataOutputRectOfInterestForRect:self.dataReadingFrame.frame];
//        self.metadataOutput.rectOfInterest = visibleRect;
//    }
//}
//
//- (void)createCustomFramesForView:(UIView *)frameView {
//    CGFloat cornerSize = 20.f;
//    CGFloat cornerWidth = 2.f;
//    for (int i = 0; i < 8; i++) {
//        CGFloat x = 0.0;
//        CGFloat y = 0.0;
//        CGFloat width = 0.0;
//        CGFloat height = 0.0;
//        switch (i) {
//            case 0:
//                x = 0; y = 0; width = cornerWidth; height = cornerSize;
//                break;
//            case 1:
//                x = 0; y = 0; width = cornerSize; height = cornerWidth;
//                break;
//            case 2:
//                x = CGRectGetWidth(frameView.bounds) - cornerSize; y = 0; width = cornerSize; height = cornerWidth;
//                break;
//            case 3:
//                x = CGRectGetWidth(frameView.bounds) - cornerWidth; y = 0; width = cornerWidth; height = cornerSize;
//                break;
//            case 4:
//                x = CGRectGetWidth(frameView.bounds) - cornerWidth;
//                y = CGRectGetHeight(frameView.bounds) - cornerSize; width = cornerWidth; height = cornerSize;
//                break;
//            case 5:
//                x = CGRectGetWidth(frameView.bounds) - cornerSize;
//                y = CGRectGetHeight(frameView.bounds) - cornerWidth; width = cornerSize; height = cornerWidth;
//                break;
//            case 6:
//                x = 0; y = CGRectGetHeight(frameView.bounds) - cornerWidth; width = cornerSize; height = cornerWidth;
//                break;
//            case 7:
//                x = 0; y = CGRectGetHeight(frameView.bounds) - cornerSize; width = cornerWidth; height = cornerSize;
//                break;
//        }
//        UIView * cornerView = [[UIView alloc] initWithFrame:CGRectMake(x, y, width, height)];
//        cornerView.backgroundColor = self.frameColor;
//        [frameView addSubview:cornerView];
//    }
//}
//
//- (void)addVisualEffects:(CGRect)inputRect {
//    UIView *topView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, inputRect.origin.y)];
//    topView.backgroundColor = [UIColor colorWithRed:0.0/255.0 green:0.0/255.0 blue:0.0/255.0 alpha:0.4];
//    [self addSubview:topView];
//
//    UIView *leftSideView = [[UIView alloc] initWithFrame:CGRectMake(0, inputRect.origin.y, self.frameOffset, self.frameHeight)]; //paddingForScanner scannerHeight
//    leftSideView.backgroundColor = [UIColor colorWithRed:0.0/255.0 green:0.0/255.0 blue:0.0/255.0 alpha:0.4];
//    [self addSubview:leftSideView];
//
//    UIView *rightSideView = [[UIView alloc] initWithFrame:CGRectMake(inputRect.size.width + self.frameOffset, inputRect.origin.y, self.frameOffset, self.frameHeight)];
//    rightSideView.backgroundColor = [UIColor colorWithRed:0.0/255.0 green:0.0/255.0 blue:0.0/255.0 alpha:0.4];
//    [self addSubview:rightSideView];
//
//    UIView *bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, inputRect.origin.y + self.frameHeight, self.frame.size.width,
//                                                                  self.frame.size.height - inputRect.origin.y - self.frameHeight)];
//    bottomView.backgroundColor = [UIColor colorWithRed:0.0/255.0 green:0.0/255.0 blue:0.0/255.0 alpha:0.4];
//    [self addSubview:bottomView];
//}
//
//- (void)startAnimatingScanner:(UIView *)inputView {
//    if (!self.scannerView) {
//        self.scannerView = [[UIView alloc] initWithFrame:CGRectMake(2, 0, inputView.frame.size.width - 4, 2)];
//        self.scannerView.backgroundColor = self.laserColor;
//    }
//    if (self.scannerView.frame.origin.y != 0) {
//        [self.scannerView setFrame:CGRectMake(2, 0, inputView.frame.size.width - 4, 2)];
//    }
//    [inputView addSubview:self.scannerView];
//    [UIView animateWithDuration:3 delay:0 options:(UIViewAnimationOptionAutoreverse | UIViewAnimationOptionRepeat) animations:^{
//        CGFloat middleX = inputView.frame.size.width / 2;
//        self.scannerView.center = CGPointMake(middleX, inputView.frame.size.height - 1);
//    } completion:^(BOOL finished) {}];
//}
//
//- (void)stopAnimatingScanner {
//    [self.scannerView removeFromSuperview];
//}

//Observer actions

//- (void)didEnterBackground:(NSNotification *)notification {
//    [self stopAnimatingScanner];
//}
//
//- (void)willEnterForeground:(NSNotification *)notification {
//    [self startAnimatingScanner:self.dataReadingFrame];
//}

#pragma mark - observers


- (void)addObservers
{
    if (!self.isAddedOberver) {
        [self.session addObserver:self forKeyPath:@"running" options:NSKeyValueObservingOptionNew context:SessionRunningContext];
        [self.stillImageOutput addObserver:self forKeyPath:@"capturingStillImage" options:NSKeyValueObservingOptionNew context:CapturingStillImageContext];

        [self.videoDeviceInput.device addObserver:self forKeyPath:@"adjustingFocus" options:NSKeyValueObservingOptionNew context:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(subjectAreaDidChange:) name:AVCaptureDeviceSubjectAreaDidChangeNotification object:self.videoDeviceInput.device];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(sessionRuntimeError:) name:AVCaptureSessionRuntimeErrorNotification object:self.session];
        // A session can only run when the app is full screen. It will be interrupted in a multi-app layout, introduced in iOS 9,
        // see also the documentation of AVCaptureSessionInterruptionReason. Add observers to handle these session interruptions
        // and show a preview is paused message. See the documentation of AVCaptureSessionWasInterruptedNotification for other
        // interruption reasons.
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(sessionWasInterrupted:) name:AVCaptureSessionWasInterruptedNotification object:self.session];
        //Observers for re-usage animation when app go to the background and back
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(didEnterBackground:) name:UIApplicationDidEnterBackgroundNotification
                                                   object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(willEnterForeground:)
                                                     name:UIApplicationWillEnterForegroundNotification
                                                   object:nil];

        self.isAddedOberver = YES;
    }
}

//UIApplicationDidEnterBackgroundNotification       NS_AVAILABLE_IOS(4_0);
//UIKIT_EXTERN NSNotificationName const UIApplicationWillEnterForegroundNotification

- (void)sessionWasInterrupted:(NSNotification *)notification
{
    // In some scenarios we want to enable the user to resume the session running.
    // For example, if music playback is initiated via control center while using AVCam,
    // then the user can let AVCam resume the session running, which will stop music playback.
    // Note that stopping music playback in control center will not automatically resume the session running.
    // Also note that it is not always possible to resume, see -[resumeInterruptedSession:].
    BOOL showResumeButton = NO;

    // In iOS 9 and later, the userInfo dictionary contains information on why the session was interrupted.
    if ( &AVCaptureSessionInterruptionReasonKey ) {
        AVCaptureSessionInterruptionReason reason = [notification.userInfo[AVCaptureSessionInterruptionReasonKey] integerValue];
        //NSLog( @"Capture session was interrupted with reason %ld", (long)reason );

        if ( reason == AVCaptureSessionInterruptionReasonAudioDeviceInUseByAnotherClient ||
            reason == AVCaptureSessionInterruptionReasonVideoDeviceInUseByAnotherClient ) {
            showResumeButton = YES;
        }
    }
}


- (void)removeObservers
{
    if (self.isAddedOberver) {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        [self.session removeObserver:self forKeyPath:@"running" context:SessionRunningContext];
        [self.stillImageOutput removeObserver:self forKeyPath:@"capturingStillImage" context:CapturingStillImageContext];
        [self.videoDeviceInput.device removeObserver:self forKeyPath:@"adjustingFocus"];
        self.isAddedOberver = NO;
    }
}

- (void)sessionRuntimeError:(NSNotification *)notification
{
    NSError *error = notification.userInfo[AVCaptureSessionErrorKey];
    //NSLog( @"Capture session runtime error: %@", error );

    // Automatically try to restart the session running if media services were reset and the last start running succeeded.
    // Otherwise, enable the user to try to resume the session running.
    if ( error.code == AVErrorMediaServicesWereReset ) {
        dispatch_async( self.sessionQueue, ^{
            if ( self.isSessionRunning ) {
                [self.session startRunning];
                self.sessionRunning = self.session.isRunning;
            }
            else {
            }
        } );
    }
}


- (void)subjectAreaDidChange:(NSNotification *)notification
{
//    [self resetFocus];
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (context == CapturingStillImageContext)
    {
        // Flash/dim preview to indicate shutter action
//        BOOL isCapturingStillImage = [change[NSKeyValueChangeNewKey] boolValue];
//        if ( isCapturingStillImage )
//        {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                self.alpha = 0.0;
//                [UIView animateWithDuration:0.35 animations:^{
//                    self.alpha = 1.0;
//                }];
//            });
//        }
    }
    else if ([keyPath isEqualToString:@"adjustingFocus"])
    {
        // Note: oldKey is not available (value is always NO it seems) so we only check on newKey
//        BOOL isFocusing = [change[NSKeyValueChangeNewKey] boolValue];
//        if (self.startFocusResetTimerAfterFocusing == YES && !isFocusing && self.resetFocusTimeout > 0)
//        {
//            self.startFocusResetTimerAfterFocusing = NO;
//
//            // Disengage manual focus after focusTimeout milliseconds
//            NSTimeInterval focusTimeoutSeconds = self.resetFocusTimeout / 1000;
//            self.focusResetTimer = [NSTimer scheduledTimerWithTimeInterval:focusTimeoutSeconds repeats:NO block:^(NSTimer *timer) {
////                [self resetFocus];
//            }];
//        }
    }
    else if (context == SessionRunningContext)
    {
//        BOOL isSessionRunning = [change[NSKeyValueChangeNewKey] boolValue];
//
//        dispatch_async( dispatch_get_main_queue(), ^{
//            // Only enable the ability to change camera if the device has more than one camera.
//            self.cameraButton.enabled = isSessionRunning && ( [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo].count > 1 );
//            self.recordButton.enabled = isSessionRunning;
//            self.stillButton.enabled = isSessionRunning;
//        } );
    }
    else
    {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

#pragma mark - AVCaptureMetadataOutputObjectsDelegate

- (void)captureOutput:(AVCaptureOutput *)output
didOutputMetadataObjects:(NSArray<__kindof AVMetadataObject *> *)metadataObjects
       fromConnection:(AVCaptureConnection *)connection {

    for(AVMetadataObject *metadataObject in metadataObjects)
    {
        if ([metadataObject isKindOfClass:[AVMetadataMachineReadableCodeObject class]] && [self isSupportedBarCodeType:metadataObject.type]) {

            AVMetadataMachineReadableCodeObject *code = (AVMetadataMachineReadableCodeObject*)[self.previewLayer transformedMetadataObjectForMetadataObject:metadataObject];
            if (self.onReadCode && code.stringValue && ![code.stringValue isEqualToString:self.codeStringValue]) {
                self.onReadCode(@{@"codeStringValue": code.stringValue});
//                [self stopAnimatingScanner];
            }
        }
    }
}

- (BOOL)isSupportedBarCodeType:(NSString *)currentType {
    BOOL result = NO;
    NSArray *supportedBarcodeTypes = @[AVMetadataObjectTypeUPCECode,AVMetadataObjectTypeCode39Code,AVMetadataObjectTypeCode39Mod43Code,
                                       AVMetadataObjectTypeEAN13Code,AVMetadataObjectTypeEAN8Code, AVMetadataObjectTypeCode93Code,
                                       AVMetadataObjectTypeCode128Code, AVMetadataObjectTypePDF417Code, AVMetadataObjectTypeQRCode,
                                       AVMetadataObjectTypeAztecCode, AVMetadataObjectTypeDataMatrixCode];
    for (NSString* object in supportedBarcodeTypes) {
        if ([currentType isEqualToString:object]) {
            result = YES;
        }
    }
    return result;
}

#pragma mark - String Constants For Scanner

const NSString *offsetForScannerFrame     = @"offsetFrame";
const NSString *heightForScannerFrame     = @"frameHeight";
const NSString *colorForFrame             = @"colorForFrame";
const NSString *isNeedMultipleScanBarcode = @"isNeedMultipleScanBarcode";


@end

