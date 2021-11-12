//
//  FacePasterBridge.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/29.
//

#import "AliAVServiceBridge.h"
#import <React/RCTBridge.h>
#import <Photos/Photos.h>
#import <AVFoundation/AVFoundation.h>
#import "AVAsset+VideoInfo.h"
#import "AliyunPathManager.h"
#import "AliyunPhotoLibraryManager.h"
#import <AliyunVideoSDKPro/AliyunHttpClient.h>
#import <AliyunVideoSDKPro/AliyunCrop.h>
#import <AliyunVideoSDKPro/AliyunImageCrop.h>
#import <AliyunVideoSDKPro/AliyunErrorCode.h>
#import <AliyunVideoSDKPro/AliyunNativeParser.h>
#import "ShortCut.h"
#import "RNAVDeviceHelper.h"

static NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";

@interface AliAVServiceBridge ()<AliyunCropDelegate>
{
    BOOL _hasListeners;
    RCTPromiseResolveBlock _videoCropResolve;
    NSString *_videoCropOutputPath;
}

@property (nonatomic, strong) AliyunCrop *cutPanel;

@end

@implementation AliAVServiceBridge

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(enableHapticIfExist)
{
  if (@available(iOS 10.0, *)) {
      if (@available(iOS 13.0, *)) {
          if (![[AVAudioSession sharedInstance] allowHapticsAndSystemSoundsDuringRecording]) {
              NSError *error = nil;
              [[AVAudioSession sharedInstance] setAllowHapticsAndSystemSoundsDuringRecording:YES error:&error];
              if (error) {
                AVDLog(@"%@",error.localizedDescription);
              }
          }
      }
  }
}

RCT_EXPORT_METHOD(getFilterIcons:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSArray *names = @[@"柔柔",@"优雅",@"红润",@"阳光",@"海蓝",@"炽黄",@"浓烈",@"闪耀",@"朝阳",@"经典",@"粉桃",@"雪梨",@"鲜果",@"麦茶",@"灰白",@"波普",@"光圈",@"海盐",@"黑白",@"胶片",@"焦黄",@"蓝调",@"迷糊",@"思念",@"素描",@"鱼眼",@"马赛克",@"模糊"];
    NSMutableArray *infos = [NSMutableArray array];
    for (NSString *name in names) {
        NSString *iconPath = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:[NSString stringWithFormat:@"Filter/%@/icon.png",name]];
        [infos addObject:@{@"iconPath":iconPath,@"filterName":name}];
    }
    if (infos.count) {
        resolve(infos);
    } else {
        reject(@"",@"filter resource doesn't exist",nil);
    }
}

RCT_EXPORT_METHOD(saveResourceToPhotoLibrary:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *sourcePath = [options valueForKey:@"sourcePath"];
    if (!sourcePath || [sourcePath isEqualToString:@""]) {
        reject(@"404", @"sourcePath is null", nil);
        return;
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
        return;
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
                        AVDLog(@"save success");
                        resolve(@(success));
                    } else {
                        AVDLog(@"save failure:%@", error);
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
                        AVDLog(@"save success");
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

- (void)startObserving
{
    _hasListeners = YES;
}

- (void)stopObserving
{
    _hasListeners = NO;
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[
        @"cropProgress",
        @"icloudImageDownloadProgress",
        @"icloudVideoDownloadProgress"
    ];
}

- (void)cropVideo:(NSString *)sourcePath
         duration:(CGFloat)duration
             rect:(CGRect)cropRect
          resolve:(RCTPromiseResolveBlock)resolve
           reject:(RCTPromiseRejectBlock)reject
{
    if (![sourcePath hasPrefix:@"file://"]) {
        reject(@"",@"path not valid",nil);
        return;
    }
    if (![sourcePath hasSuffix:@".mp4"]) {
        reject(@"",@"Only support mp4 crop",nil);
        return;
    }
    NSString *inputPath = [sourcePath stringByReplacingOccurrencesOfString:@"file://" withString:@""];
    if (![[NSFileManager defaultManager] fileExistsAtPath:inputPath]) {
        reject(@"",@"File doesn't exist at source path",nil);
        return;
    }
    NSURL *assetURL = [NSURL URLWithString:inputPath];
    AVURLAsset *urlAsset = [[AVURLAsset alloc] initWithURL:assetURL options:nil];
    if (!urlAsset) {
        reject(@"",@"Asset init failed",nil);
        return;
    }
    NSString *outputPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
    self.cutPanel = [[AliyunCrop alloc] initWithDelegate:(id<AliyunCropDelegate>)self];
    self.cutPanel.inputPath = inputPath;
    self.cutPanel.outputSize = cropRect.size;
    self.cutPanel.outputPath = outputPath;
    self.cutPanel.startTime = 0;
    self.cutPanel.endTime = duration;
    
    // cut mode
    self.cutPanel.cropMode = 1;
    self.cutPanel.rect = cropRect;
    if ([RNAVDeviceHelper isBelowIphone_11]) {
        self.cutPanel.videoQuality = AliyunVideoQualityMedium;
    }
    else {
        self.cutPanel.fps = 30;
        self.cutPanel.gop = 30;
        self.cutPanel.bitrate = 10*1000*1000;   // 15Mbps
    }
    self.cutPanel.encodeMode = 1;           // Force hardware encoding
    self.cutPanel.shouldOptimize = NO;
    
    int res =[self.cutPanel startCrop];//20003004
    if (res == ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO){
        reject(@"",@"NOT_SUPPORTED_VIDEO",nil);
        return;
    }
    else if (res == ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO){
        reject(@"",@"NOT_SUPPORTED_VIDEO",nil);
        return;
    }
    else if (res <0 && res != -314){
        reject(@"",[NSString stringWithFormat:@"%d",res],nil);
        return;
    }
    else if (res == 0) {
//        resolve(outputPath);
        _videoCropOutputPath = outputPath;
        _videoCropResolve = resolve;
    }
}

RCT_EXPORT_METHOD(crop:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *source = [options valueForKey:@"source"];
    if (!source || [source isEqualToString:@""]) {
        reject(@"",@"source must contain a path value",nil);
        return;
    }
    CGFloat cropOffsetX = [[options valueForKey:@"cropOffsetX"] floatValue];
    CGFloat cropOffsetY = [[options valueForKey:@"cropOffsetY"] floatValue];
    CGFloat cropWidth = [[options valueForKey:@"cropWidth"] floatValue];
    CGFloat cropHeight = [[options valueForKey:@"cropHeight"] floatValue];
    
    
    if (cropWidth == 0.0 ) {
        reject(@"",@"Invalid cropWidth",nil);
        return;
    }
    
    if ( cropHeight == 0.0 ) {
        reject(@"",@"Invalid cropHeight",nil);
        return;
    }
    
    CGRect cropRect = CGRectMake(cropOffsetX, cropOffsetY, cropWidth, cropHeight);
    
    if ([source hasPrefix:@"file://"]) {
        if ([source hasSuffix:@".mp4"]) {
            CGFloat duration = [[options valueForKey:@"duration"] floatValue];
            if (!duration) {
                reject(@"",@"duration can't be zero",nil);
                return;
            }
            [self cropVideo:source duration:duration rect:cropRect resolve:resolve reject:reject];
        }
         else if ([source hasSuffix:@".png"]) {
            [self cropImage:source rect:cropRect resolve:resolve reject:reject];
        }
         else {
             reject(@"",@"file path suffix must contain mp4 or png",nil);
         }
        return;
    }
    
    if (![source hasPrefix:@"ph://"]) {
        return;
    }
    NSString *_assetId = [source stringByReplacingOccurrencesOfString:@"ph://" withString:@""];
    PHAsset *phAsset = [PHAsset fetchAssetsWithLocalIdentifiers:@[_assetId] options:nil].firstObject;
    
    __weak typeof(self) weakSelf = self;
    if (phAsset.mediaType == PHAssetMediaTypeVideo) {
        [[AliyunPhotoLibraryManager sharedManager] getVideoWithAsset:phAsset
                                                          completion:^(AVAsset *avAsset, NSDictionary *info) {
            AVURLAsset *urlAsset = (AVURLAsset *)avAsset;
            NSString *sourcePath = [urlAsset.URL path];
            
            NSString *outputPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
            weakSelf.cutPanel = [[AliyunCrop alloc] initWithDelegate:(id<AliyunCropDelegate>)self];
            weakSelf.cutPanel.inputPath = sourcePath;
            weakSelf.cutPanel.outputSize = cropRect.size;
            weakSelf.cutPanel.outputPath = outputPath;
            weakSelf.cutPanel.startTime = 0;
            CGFloat endTime = [avAsset avAssetVideoTrackDuration];
            weakSelf.cutPanel.endTime = endTime;

            // cut mode
            weakSelf.cutPanel.cropMode = 1;
            weakSelf.cutPanel.rect = cropRect;
            if ([RNAVDeviceHelper isBelowIphone_11]) {
                weakSelf.cutPanel.videoQuality = AliyunVideoQualityMedium;
            }
            else {
                weakSelf.cutPanel.fps = 30;
                weakSelf.cutPanel.gop = 30;
                weakSelf.cutPanel.bitrate = 10*1000*1000;   // 15Mbps
            }
            weakSelf.cutPanel.encodeMode = 1;           // Force hardware encoding
            weakSelf.cutPanel.shouldOptimize = NO;
            
            int res =[weakSelf.cutPanel startCrop];
            if (res == ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO){
                reject(@"",@"NOT_SUPPORTED_VIDEO",nil);
                return;
            }
            else if (res == ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO){
                reject(@"",@"NOT_SUPPORTED_VIDEO",nil);
                return;
            }
            else if (res <0 && res != -314){
                reject(@"",@"",nil);
                return;
            }
            else if (res == 0) {
                self->_videoCropOutputPath = outputPath;
                self->_videoCropResolve = resolve;
            }
        }];
    } else if (phAsset.mediaType == PHAssetResourceTypePhoto) {
        NSString *rootDirPath = [AliyunPathManager compositionRootDir];
        if (![[NSFileManager defaultManager] fileExistsAtPath:rootDirPath]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:rootDirPath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        
        NSString *tmpPhotoPath = [[rootDirPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"jpg"];
        
        
        [[AliyunPhotoLibraryManager sharedManager] savePhotoWithAsset:phAsset
                                                              maxSize:cropRect.size
                                                           outputPath:tmpPhotoPath
                                                           completion:^(NSError *error, UIImage * _Nullable sourceImage) {
            if (sourceImage == nil) {
                return;
            }
            AliyunImageCrop *imageCrop = [[AliyunImageCrop alloc] init];
            imageCrop.originImage = sourceImage;
            imageCrop.cropMode = AliyunImageCropModeAspectCut;
            imageCrop.cropRect = cropRect;
            imageCrop.outputSize = cropRect.size;
            UIImage *generatedImage = [imageCrop generateImage];
            NSData *data = UIImagePNGRepresentation(generatedImage);
            BOOL writeSuccess = [data writeToFile:tmpPhotoPath atomically:YES];
            if (writeSuccess) {
                resolve(tmpPhotoPath);
            } else {
                reject(@"",@"write file failure",nil);
            }
        }];
    }
}

- (void)cropImage:(NSString *)sourcePath
             rect:(CGRect)cropRect
          resolve:(RCTPromiseResolveBlock)resolve
           reject:(RCTPromiseRejectBlock)reject
{
    NSString *rootDirPath = [AliyunPathManager compositionRootDir];
    if (![[NSFileManager defaultManager] fileExistsAtPath:rootDirPath]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:rootDirPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    NSString *outPhotoPath = [[rootDirPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"jpg"];
    
    AliyunImageCrop *imageCrop = [[AliyunImageCrop alloc] init];
    imageCrop.originImage = [[UIImage alloc] initWithContentsOfFile:sourcePath];
    imageCrop.cropMode = AliyunImageCropModeAspectCut;
    imageCrop.cropRect = cropRect;
    imageCrop.outputSize = cropRect.size;
    UIImage *generatedImage = [imageCrop generateImage];
    NSData *data = UIImagePNGRepresentation(generatedImage);
    BOOL writeSuccess = [data writeToFile:outPhotoPath atomically:YES];
    if (writeSuccess) {
        resolve(outPhotoPath);
    } else {
        reject(@"",@"write file failure",nil);
    }
}

RCT_EXPORT_METHOD(clearResources:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    if (!options || ![options isEqualToDictionary:@{}]) {
        reject(@"",@"params can't be null or empty",nil);
        return;
    }
    BOOL removeTmp = [[options objectForKey:@"tmp"] boolValue];
    BOOL removeComposition = [[options objectForKey:@"composition"] boolValue];
    BOOL removeRecord = [[options objectForKey:@"record"] boolValue];
    if (removeTmp) {
        [AliyunPathManager clearDir:NSTemporaryDirectory()];
    }
    if (removeComposition) {
        [AliyunPathManager clearDir:[AliyunPathManager compositionRootDir]];
    }
    if (removeRecord) {
        [AliyunPathManager clearDir:[[AliyunPathManager aliyunRootPath] stringByAppendingPathComponent:@"record"]];
    }
}

#pragma mark - AliyunCropDelegate
- (void)cropOnError:(int)error
{
    AVDLog(@"--- %s",__PRETTY_FUNCTION__);
    [self.cutPanel cancel];
    _videoCropOutputPath = nil;
    _videoCropResolve = nil;
}

- (void)cropTaskOnProgress:(float)progress
{
//    AVDLog(@"---🚀 %s :%f",__PRETTY_FUNCTION__, progress);
    if (_hasListeners) {
        [self sendEventWithName:@"cropProgress" body:@{@"progress":@(progress)}];
    }
}

- (void)cropTaskOnComplete
{
    AVDLog(@"--- ✅ %s ✅",__PRETTY_FUNCTION__);
    if (_hasListeners) {
        if (_videoCropOutputPath && _videoCropOutputPath) {
            _videoCropResolve(_videoCropOutputPath);
        }
        [self sendEventWithName:@"cropProgress" body:@{@"progress":@(1.0)}];
    }
    [self.cutPanel cancel];
    _videoCropOutputPath = nil;
    _videoCropResolve = nil;
}

- (void)cropTaskOnCancel
{
    AVDLog(@"--- %s",__PRETTY_FUNCTION__);
    _videoCropOutputPath = nil;
    _videoCropResolve = nil;
}

RCT_EXPORT_METHOD(saveToSandBox:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *path = [options valueForKey:@"path"];
    if (!path) {
        reject(@"",@"no path param",nil);
        return;
    }
    if (![path containsString:@"ph://"]) {
        reject(@"",@"no ph:// scheme",nil);
        return;
    }
    [self _saveImageToSandBox:path resolve:resolve reject:reject];
}

- (void)_saveImageToSandBox:(NSString *)sourcePath resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    NSString *_assetId = [sourcePath stringByReplacingOccurrencesOfString:@"ph://" withString:@""];
    PHAsset *asset = [PHAsset fetchAssetsWithLocalIdentifiers:@[_assetId] options:nil].firstObject;
    if (asset.mediaType == PHAssetMediaTypeVideo) {
        NSString *compositionRootDir = [AliyunPathManager compositionRootDir];
        if (![[NSFileManager defaultManager] fileExistsAtPath:compositionRootDir]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:compositionRootDir withIntermediateDirectories:YES attributes:nil error:nil];
        }
        NSString *outputVideoPath = [[compositionRootDir stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"mp4"];
        // Option
        PHVideoRequestOptions *videoRequestOptions = [[PHVideoRequestOptions alloc] init];
        videoRequestOptions.version = PHVideoRequestOptionsVersionOriginal;
        videoRequestOptions.deliveryMode = PHVideoRequestOptionsDeliveryModeAutomatic;
        [videoRequestOptions setProgressHandler:^(double progress, NSError * _Nullable error, BOOL * _Nonnull stop, NSDictionary * _Nullable info) {
            AVDLog(@"download icloud video progress: %lf",progress);
            if (self->_hasListeners) {
                [self sendEventWithName:@"icloudVideoDownloadProgress" body:@{@"progress":@(progress)}];
            }
        }];
        videoRequestOptions.networkAccessAllowed = YES;//打开网络获取iCloud的图片的功能
        
        PHImageManager *manager = [PHImageManager defaultManager];
        [manager requestExportSessionForVideo:asset
                                      options:videoRequestOptions
                                 exportPreset:AVAssetExportPresetHighestQuality
                                resultHandler:^(AVAssetExportSession * _Nullable exportSession, NSDictionary * _Nullable info) {
            
            exportSession.outputURL = [NSURL fileURLWithPath:outputVideoPath];
            exportSession.shouldOptimizeForNetworkUse = NO;
            exportSession.outputFileType = AVFileTypeMPEG4; // mp4
            [exportSession exportAsynchronouslyWithCompletionHandler:^{
                switch ([exportSession status]) {
                    case AVAssetExportSessionStatusFailed:
                    {
                        reject(@"",@"AVAssetExportSessionStatusFailed",nil);
                    }
                        break;
                    case AVAssetExportSessionStatusCompleted:
                    {
                        resolve(outputVideoPath);
                    }
                        break;
                    default:
                        break;
                }
            }];
        }];
        
    } else if (asset.mediaType == PHAssetResourceTypePhoto) {
        CGSize maxSize = CGSizeMake(1080, 1920);
        NSString *outputPhotoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"jpg"];
        PHImageRequestOptions *imageRequestOptions = [[PHImageRequestOptions alloc] init];
        imageRequestOptions.resizeMode   = PHImageRequestOptionsResizeModeExact;
        imageRequestOptions.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
        imageRequestOptions.synchronous = NO;
        imageRequestOptions.networkAccessAllowed = YES;//打开网络获取iCloud的图片的功能
        [imageRequestOptions setProgressHandler:^(double progress, NSError * _Nullable error, BOOL * _Nonnull stop, NSDictionary * _Nullable info) {
            AVDLog(@"🖼 download icloud image progress: %lf",progress);
            if (self->_hasListeners) {
                [self sendEventWithName:@"icloudImageDownloadProgress" body:@{@"progress":@(progress)}];
            }
        }];
        
        CGFloat factor = MAX(maxSize.width,maxSize.height)/MAX(asset.pixelWidth, asset.pixelHeight);
        if (factor > 1) {
            factor = 1.0f;
        }
        // 最终分辨率必须为偶数
        CGFloat outputWidth = rint(asset.pixelWidth * factor / 2 ) * 2;
        CGFloat outputHeight = rint(asset.pixelHeight * factor / 2) * 2;
        
        [[PHImageManager defaultManager] requestImageForAsset:asset
                                                   targetSize:CGSizeMake(outputWidth, outputHeight)
                                                  contentMode:PHImageContentModeDefault
                                                      options:imageRequestOptions
                                                resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
            if (!result) {
                reject(@"",@"request image failure",[NSError errorWithDomain:@"com.aliyun.photo" code:101 userInfo:nil]);
            }else {
                if (result.imageOrientation != UIImageOrientationUp) {
                    UIGraphicsBeginImageContextWithOptions(result.size, NO, result.scale);
                    [result drawInRect:(CGRect){0, 0, result.size}];
                    UIImage *normalizedImage = UIGraphicsGetImageFromCurrentImageContext();
                    UIGraphicsEndImageContext();
                    result = normalizedImage;
                }
                NSData *imageData = UIImageJPEGRepresentation(result, 1);
                BOOL writeSuccess = [imageData writeToFile:outputPhotoPath atomically:YES];
                if (writeSuccess) {
                    resolve(outputPhotoPath);
                } else {
                    reject(@"",@"image write fail",nil);
                }
            }
        }];
    }
}

#pragma mark -抽帧
RCT_EXPORT_METHOD(generateImages:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [self generateImages:options handler:^(NSArray *path) {
        resolve(path);
    }];
}

RCT_EXPORT_METHOD(removeThumbnaiImages:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    BOOL isRemoved = [self removeImages];
    resolve(@(isRemoved));
}

static NSString * ThumnailDirectory() {
    return [NSString stringWithFormat:@"%@/Documents/thumbNail", NSHomeDirectory()];
}

- (void)generateImages:(NSDictionary *)options handler:(void(^)(NSArray *))complete
{
    NSString *videoPath = [options valueForKey:@"videoPath"];
    if (!videoPath || [videoPath isEqualToString:@""]) {
        return;
    }
    AliyunNativeParser *parser = [[AliyunNativeParser alloc] initWithPath:videoPath];
    CGFloat duration = [parser getVideoDuration];
    CGFloat startTime = [[options valueForKey:@"startTime"] floatValue];
    if (startTime >= duration) {
        return;
    }
    
    NSInteger itemPerTime = [[options valueForKey:@"itemPerTime"] integerValue]; //ms
    if (itemPerTime == 0) {
        itemPerTime = 1000;
    }
    
    //for test only
//    videoPath = [[NSUserDefaults standardUserDefaults] objectForKey:@"videoSavePath"];
//    AliyunNativeParser *parser = [[AliyunNativeParser alloc] initWithPath:videoPath];
//    CGFloat duration = [parser getVideoDuration];
//    CGFloat startTime = 0.0;
//    NSInteger itemPerTime = 1000; //ms
    [self thumbnailFromVideoPath:videoPath
                     itemPerTime:itemPerTime
                       startTime:startTime
                        duration:duration
             generatorOutputSize:CGSizeMake(200, 200)
                        complete:complete];
}

- (void)thumbnailFromVideoPath:(NSString *)videoPath
                   itemPerTime:(NSInteger)itemPerTime
                     startTime:(CGFloat)beginTime
                      duration:(CGFloat)duration
           generatorOutputSize:(CGSize)outputSize
                      complete:(void (^)(NSArray *))complete
{
    [self removeImages];
    CMTime startTime = beginTime == 0 ? kCMTimeZero : CMTimeMakeWithSeconds(beginTime, 1000);
    NSMutableArray *array = [NSMutableArray array];
    CMTime addTime = CMTimeMakeWithSeconds(itemPerTime/1000.0, 1000);
    
    CMTime endTime = CMTimeMakeWithSeconds(duration, 1000);
    
    while (CMTIME_COMPARE_INLINE(startTime, <=, endTime)) {
        [array addObject:[NSValue valueWithCMTime:startTime]];
        startTime = CMTimeAdd(startTime, addTime);
    }
    
    // 第一帧取第0.1s   规避有些视频并不是从第0s开始的
    array[0] = [NSValue valueWithCMTime:CMTimeMakeWithSeconds(0.1, 1000)];
    
    NSURL *url = [[NSURL alloc] initFileURLWithPath:videoPath];
    AVURLAsset *urlAsset = [[AVURLAsset alloc] initWithURL:url options:nil];
    AVAssetImageGenerator *imageGenerator = [[AVAssetImageGenerator alloc] initWithAsset:urlAsset];
    imageGenerator.appliesPreferredTrackTransform = YES;
    imageGenerator.requestedTimeToleranceBefore = kCMTimeZero;
    imageGenerator.requestedTimeToleranceAfter = kCMTimeZero;
    imageGenerator.maximumSize = outputSize;
    __block int index = 0;
    NSMutableArray *pathArr = [@[] mutableCopy];
    [imageGenerator generateCGImagesAsynchronouslyForTimes:array completionHandler:^(CMTime requestedTime, CGImageRef  _Nullable image, CMTime actualTime, AVAssetImageGeneratorResult result, NSError * _Nullable error) {
        
        if (result == AVAssetImageGeneratorSucceeded) {
            UIImage *img = [[UIImage alloc] initWithCGImage:image];
            dispatch_sync(dispatch_get_main_queue(), ^{
                NSString *path = [self saveImgToSandBox:img withIndex:++index];
                if (path) {
                    [pathArr addObject:path];
                }
                if (pathArr.count == array.count) {
                    complete(pathArr);
                }
            });
        }
    }];
}

- (NSString *)saveImgToSandBox:(UIImage *)image withIndex:(int)index
{
    NSString *fileDirectoryPath = ThumnailDirectory();
    NSFileManager *fm = [NSFileManager defaultManager];
    if (![fm fileExistsAtPath:fileDirectoryPath]) {
        NSString *documentsDirectory = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
        fileDirectoryPath = [documentsDirectory stringByAppendingPathComponent:@"thumbNail"];
        [fm createDirectoryAtPath:fileDirectoryPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    NSData *imgData = UIImagePNGRepresentation(image);
    NSString *imageName = [NSString stringWithFormat:@"%03d.png", index];
    NSString *imgPath = [fileDirectoryPath stringByAppendingPathComponent:imageName];
    BOOL suc = [imgData writeToFile:imgPath atomically:YES];
    if (suc) {
        return imgPath;
    }
    return nil;
}

- (BOOL)removeImages
{
    BOOL isRemoved = [[NSFileManager defaultManager] removeItemAtPath:ThumnailDirectory() error:nil];
    return isRemoved;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

@end
