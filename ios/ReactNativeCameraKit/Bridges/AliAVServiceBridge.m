//
//  FacePasterBridge.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/29.
//

#import "AliAVServiceBridge.h"
#import <React/RCTBridge.h>
#import <AliyunVideoSDKPro/AliyunHttpClient.h>
#import <Photos/Photos.h>
#import "AVAsset+VideoInfo.h"
#import <AVFoundation/AVFoundation.h>
#import "AliyunPathManager.h"
#import "AliyunPhotoLibraryManager.h"

static NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";

@interface AliAVServiceBridge ()

@end

@implementation AliAVServiceBridge

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(saveResourceToPhotoLibrary:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *sourcePath = [options valueForKey:@"sourcePath"];
    if (!sourcePath || [sourcePath isEqualToString:@""]) {
        reject(@"404", @"sourcePath is null", nil);
    }
    NSURL *pathURL = nil;
    if ([sourcePath containsString:@"file://"]) {
        pathURL = [NSURL URLWithString:sourcePath];
    } else {
        pathURL = [NSURL fileURLWithPath:sourcePath];
    }
    NSString *typeStr = [options valueForKey:@"resourceType"];
    if (!typeStr || [typeStr isEqualToString:@""]) {
        reject(@"404", @"no specyfic resource type", nil);
    }
    if ([typeStr isEqualToString:@"photo"]) {
        [self requestAuthorization:^(BOOL authorized){
            if (!authorized) {
                reject(@"404", @"Photo Library not allowed ", nil);
                return;
            }
            [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
                PHAssetCreationRequest *cr = [PHAssetCreationRequest creationRequestForAsset];
                [cr addResourceWithType:PHAssetResourceTypePhoto fileURL:pathURL options:nil];
            } completionHandler:^(BOOL success, NSError * _Nullable error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (success) {
                        NSLog(@"save success");
                        resolve(@(success));
                    } else {
                        NSLog(@"save failure:%@", error);
                        reject(@"save fail", @"save fail", error);
                    }
                });
            }];
        }];
    }
    else if ([typeStr isEqualToString:@"video"]) {
        [self requestAuthorization:^(BOOL authorized){
            [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
                PHAssetCreationRequest *cr = [PHAssetCreationRequest creationRequestForAsset];
                [cr addResourceWithType:PHAssetResourceTypeVideo fileURL:pathURL options:nil];
            } completionHandler:^(BOOL success, NSError * _Nullable error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (success) {
                        NSLog(@"save success");
                        resolve(@(success));
                    } else {
                        reject(@"save fail", @"save fail", error);
                    }
                });
            }];
        }];
    } else {
        reject(@"",@"type string not matched",nil);
    }
}

RCT_EXPORT_METHOD(getFacePasterInfos:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    AliyunHttpClient *httpClient = [[AliyunHttpClient alloc] initWithBaseUrl:kAlivcQuUrlString];
    NSDictionary *param = @{@"type":@(1)};
    [httpClient GET:@"resource/getFrontPasterList"
         parameters:param
  completionHandler:^(NSURLResponse *response, id responseObject, NSError *error) {
        if (error) {
            reject(@"fetch remote paster fail", error.localizedDescription, nil);
        } else {
            NSArray *pastList = responseObject[@"data"];
            NSMutableArray *arr = [pastList mutableCopy];
            [arr insertObject:[self _localFacePaster] atIndex:0];
            resolve(arr);
        }
    }];
}

- (NSDictionary *)_localFacePaster
{
    NSString *filterName = [NSString stringWithFormat:@"Face_Sticker/hanfumei-800"];
    NSString *path = [[NSBundle mainBundle] pathForResource:filterName ofType:nil];
    NSString *lastComponent = [path lastPathComponent];
    NSArray *comp = [lastComponent componentsSeparatedByString:@"-"];
    NSDictionary *localPaster = @{
        @"name": comp.firstObject,
        @"id": @([comp.lastObject integerValue]),
        @"icon": [path stringByAppendingPathComponent:@"icon.png"],
        @"type": @2,
        @"bundlePath": path
    };
    return localPaster;
}

- (void)requestAuthorization:(void(^)(BOOL))completion
{
    PHAuthorizationStatus authStatus = [PHPhotoLibrary authorizationStatus];
    if (authStatus == AVAuthorizationStatusNotDetermined) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (status == PHAuthorizationStatusAuthorized) {
                    completion(true);
                }
            });
        }];
    }else if (authStatus == PHAuthorizationStatusAuthorized) {
        completion(true);
    } else {
        completion(false);
    }
}

RCT_EXPORT_METHOD(crop:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *source = [options valueForKey:@"source"];
    if (!source || ![source containsString:@"ph://"]) {
        reject(@"",@"source scheme no ph://",nil);
    }
    CGFloat cropOffsetX = [[options valueForKey:@"cropOffsetX"] floatValue];
    CGFloat cropOffsetY = [[options valueForKey:@"cropOffsetY"] floatValue];
    CGFloat cropWidth = [[options valueForKey:@"cropWidth"] floatValue];
    CGFloat cropHeight = [[options valueForKey:@"cropHeight"] floatValue];
    
    NSString *quality = [options valueForKey:@"quality"] ? : @"";
    
    if (cropWidth == 0.0 ) {
        reject(@"",@"Invalid cropWidth",nil);
    }
    
    if ( cropHeight == 0.0 ) {
        reject(@"",@"Invalid cropHeight",nil);
    }
    
    //save resource to sandbox from photo library
    NSString *path = [NSURL URLWithString:source].path;
    PHAsset *phAsset = [[PHAsset fetchAssetsWithLocalIdentifiers:@[path] options:nil] firstObject];
    UIImage *phImage = [AliAVServiceBridge _getImageForAsset:phAsset withSize:CGSizeMake(phAsset.pixelWidth, phAsset.pixelHeight)];
    NSString *imageUri = [[AliyunPathManager createResourceDir] stringByAppendingPathComponent:[AliyunPathManager randomString]];
    NSData *imageData = UIImageJPEGRepresentation(phImage, 1.0 / sqrt(2.0));
    [imageData writeToFile:imageUri atomically:YES];

    NSURL *sourceURL = [NSURL URLWithString:imageUri];
    
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:sourceURL options:nil];
    
    NSString *outputURL = [[AliyunPathManager createResourceDir] stringByAppendingPathComponent:[AliyunPathManager randomString]];
    
    NSString *useQuality = [self _getQualityForAsset:quality asset:asset];
    
    AVAssetExportSession *exportSession = [[AVAssetExportSession alloc] initWithAsset:asset presetName:useQuality];
    if (!exportSession) {
        reject(@"",@"Error creating AVAssetExportSession",nil);
    }
    exportSession.outputURL = [NSURL fileURLWithPath:outputURL];
    exportSession.outputFileType = AVFileTypeMPEG4;
    exportSession.shouldOptimizeForNetworkUse = YES;
    
    AVMutableVideoComposition *videoComposition = [AVMutableVideoComposition videoCompositionWithPropertiesOfAsset:asset];
    AVAssetTrack *clipVideoTrack = [[asset tracksWithMediaType:AVMediaTypeVideo] firstObject];
    
    UIImageOrientation videoOrientation = [self _getVideoOrientationFromAsset:asset];
    
    CGFloat videoWidth = 0.0;
    CGFloat videoHeight = 0.0;
    
    if (videoOrientation == UIImageOrientationUp || videoOrientation == UIImageOrientationDown) {
        videoWidth = clipVideoTrack.naturalSize.height;
        videoHeight = clipVideoTrack.naturalSize.width;
    } else {
        videoWidth = clipVideoTrack.naturalSize.width;
        videoHeight = clipVideoTrack.naturalSize.height;
    }
    
    videoComposition.frameDuration = CMTimeMake(1, 30);
    while (fmod(cropWidth, 2.0) > 0 && cropWidth < videoWidth) {
        cropWidth += 1.0;
    }
    while (fmod(cropWidth, 2.0) > 0 && cropWidth > 0.0) {
        cropWidth -= 1.0;
    }
    while (fmod(cropHeight, 2.0) > 0 && cropHeight < videoHeight) {
        cropHeight += 1.0;
    }
    while (fmod(cropHeight, 2.0) > 0 && cropHeight > 0.0) {
        cropHeight -= 1.0;
    }
    
    videoComposition.renderSize = CGSizeMake(cropWidth, cropHeight);
    
    AVMutableVideoCompositionInstruction *instruction = [AVMutableVideoCompositionInstruction new];
    instruction.timeRange = CMTimeRangeMake(kCMTimeZero, asset.duration);
    
    CGAffineTransform t1 = CGAffineTransformIdentity;
    CGAffineTransform t2 = CGAffineTransformIdentity;
    
    AVMutableVideoCompositionLayerInstruction *transformer =
    [AVMutableVideoCompositionLayerInstruction videoCompositionLayerInstructionWithAssetTrack:clipVideoTrack];
    
    switch (videoOrientation) {
        case UIImageOrientationUp:
            t1 = CGAffineTransformMakeTranslation(clipVideoTrack.naturalSize.height - cropOffsetX,
                                                  0-cropOffsetY);
            t2 = CGAffineTransformRotate(t1, M_PI_2);
            break;
        case UIImageOrientationLeft:
            t1 = CGAffineTransformMakeTranslation(clipVideoTrack.naturalSize.width - cropOffsetX,
                                                  clipVideoTrack.naturalSize.height - cropOffsetY);
            t2 = CGAffineTransformRotate(t1, M_PI);
            break;
        case UIImageOrientationRight:
            t1 = CGAffineTransformMakeTranslation(0 - cropOffsetX, 0-cropOffsetY);
            t2 = CGAffineTransformRotate(t1, 0);
            break;
        case UIImageOrientationDown:
            t1 = CGAffineTransformMakeTranslation(0 - cropOffsetX, clipVideoTrack.naturalSize.width - cropOffsetY);
            t2 = CGAffineTransformRotate(t1, -M_PI_2);
            break;
            
        default:
            break;
    }
    
    CGAffineTransform finalTransform = t2;
    [transformer setTransform:finalTransform atTime:kCMTimeZero];
    
    instruction.layerInstructions = @[transformer];
    videoComposition.instructions = @[instruction];
    
    exportSession.videoComposition = videoComposition;
    
    [exportSession exportAsynchronouslyWithCompletionHandler:^{
        if (exportSession.status == AVAssetExportSessionStatusCompleted) {
            resolve(outputURL);
        }
        else if (exportSession.status == AVAssetExportSessionStatusFailed) {
            reject(@"",@"Failed",[exportSession error]);
        }
        else if (exportSession.status == AVAssetExportSessionStatusCancelled) {
            reject(@"",@"Cancelled",[exportSession error]);
        }
    }];
}

- (NSURL *)_getSourceURL:(NSString *)source
{
    NSURL *sourceURL = nil;
    if ([source containsString:@"assets-library"]) {
        sourceURL = [NSURL URLWithString:source];
    } else {
        NSURL *bundleURL = [[NSBundle mainBundle] resourceURL];
        sourceURL = [NSURL URLWithString:source relativeToURL:bundleURL];
    }
    
    return sourceURL;
}

- (NSString *)_getQualityForAsset:(NSString *)quality asset:(AVAsset *)asset
{
    NSString *useQuality = AVAssetExportPresetPassthrough;
    if ([quality containsString:@"low"]) {
        useQuality = AVAssetExportPresetLowQuality;
    }
    else if ([quality containsString:@"medium"]) {
        useQuality = AVAssetExportPresetMediumQuality;
    }
    else if ([quality containsString:@"highest"]) {
        useQuality = AVAssetExportPresetHighestQuality;
    }
    else if ([quality containsString:@"640x480"]) {
        useQuality = AVAssetExportPreset640x480;
    }
    else if ([quality containsString:@"960x540"]) {
        useQuality = AVAssetExportPreset960x540;
    }
    else if ([quality containsString:@"1280x720"]) {
        useQuality = AVAssetExportPreset1280x720;
    }
    else if ([quality containsString:@"1920x1080"]) {
        useQuality = AVAssetExportPreset1920x1080;
    }
    
    NSArray<NSString *> *arrary = [AVAssetExportSession exportPresetsCompatibleWithAsset:asset];
    if (![arrary containsObject:useQuality]) {
        useQuality = AVAssetExportPresetPassthrough;
    }
    return useQuality;
}

- (UIImageOrientation)_getVideoOrientationFromAsset:(AVAsset *)asset
{
    AVAssetTrack *videoTrack = [[asset tracksWithMediaType:AVMediaTypeVideo] firstObject];
    CGSize size = videoTrack.naturalSize;
    
    CGAffineTransform txf = videoTrack.preferredTransform;
    
    if (size.width == txf.tx && size.height == txf.ty) {
        return UIImageOrientationLeft;
    }
    else if (txf.tx == 0 && txf.ty == 0) {
        return UIImageOrientationRight;
    }
    else if (txf.tx == 0 && txf.ty == size.width) {
        return UIImageOrientationDown;
    }
    else {
        return  UIImageOrientationUp;
    }
    
}

RCT_EXPORT_METHOD(saveToSandBox:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *path = [options valueForKey:@"path"];
    if (!path) {
        reject(@"",@"no path param",nil);
    }
    if (![path containsString:@"ph://"]) {
        reject(@"",@"no ph:// scheme",nil);
    }
    [self _saveImageToSandBox:path complete:^(NSString *path) {
        resolve(path);
    }];
}

- (void)_saveImageToSandBox:(NSString *)path complete:(void(^)(NSString *path))complete
{
    NSString *_assetId = [path stringByReplacingOccurrencesOfString:@"ph://" withString:@""];
    PHAsset *asset = [PHAsset fetchAssetsWithLocalIdentifiers:@[_assetId] options:nil].firstObject;
    if (asset.mediaType == PHAssetMediaTypeVideo) {
        [[AliyunPhotoLibraryManager sharedManager] getVideoWithAsset:asset completion:^(AVAsset *avAsset, NSDictionary *info) {
            AVURLAsset *urlAsset = (AVURLAsset *)avAsset;
            NSString *sourcePath = [urlAsset.URL path];
            complete(sourcePath);
        }];
    } else if (asset.mediaType == PHAssetResourceTypePhoto) {
        NSString *tmpPhotoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"jpg"];
        [[AliyunPhotoLibraryManager sharedManager] savePhotoWithAsset:asset
                                                              maxSize:CGSizeMake(1080, 1920)
                                                           outputPath:tmpPhotoPath
                                                           completion:^(NSError *error, UIImage * _Nullable result) {
            complete(tmpPhotoPath);
        }];
    }
}

+ (BOOL)_saveImage:(UIImage *)image toFileName:(NSString *)fileName
{
    NSError *error;
    NSFileManager *fileMgr = [NSFileManager defaultManager];
    NSString *documentsDirectory = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    NSArray<NSString *> *directoryFiles = [fileMgr contentsOfDirectoryAtPath:documentsDirectory error:&error];
    
    if (error == nil && ![directoryFiles containsObject:fileName]) {
        NSString *imageUri = [documentsDirectory stringByAppendingPathComponent:fileName];
        NSData *imageData = UIImageJPEGRepresentation(image, 1.0 / sqrt(2.0));
    
        [imageData writeToFile:imageUri atomically:YES];
        
        return YES;
    }
    
    return NO;
}

+ (UIImage *)_getImageForAsset:(PHAsset *)asset withSize:(CGSize)size
{
    PHImageManager *phManager = [PHImageManager defaultManager];
    __block UIImage *img;
    
    // Requesting image for asset
    PHImageRequestOptions *requestOptions = [PHImageRequestOptions new];
    requestOptions.synchronous = YES;

    [phManager requestImageForAsset:asset
                         targetSize:size
                        contentMode:PHImageContentModeAspectFill
                            options:requestOptions
                      resultHandler:^void(UIImage *image, NSDictionary *info) {
        img = image;
    }];
    
    return img;
}

@end
