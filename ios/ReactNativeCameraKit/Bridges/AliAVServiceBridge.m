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

static NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";

@interface AliAVServiceBridge ()<AliyunCropDelegate>
{
    BOOL _hasListeners;
}

@property (nonatomic, strong) AliyunCrop *cutPanel;

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
    return @[@"cropProgress"];
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
    
    
    if (cropWidth == 0.0 ) {
        reject(@"",@"Invalid cropWidth",nil);
    }
    
    if ( cropHeight == 0.0 ) {
        reject(@"",@"Invalid cropHeight",nil);
    }

    
    NSString *_assetId = [source stringByReplacingOccurrencesOfString:@"ph://" withString:@""];
    PHAsset *phAsset = [PHAsset fetchAssetsWithLocalIdentifiers:@[_assetId] options:nil].firstObject;
    
    CGRect cropRect = CGRectMake(cropOffsetX, cropOffsetY, cropWidth, cropHeight);
    
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
//            weakSelf.cutPanel.fps = 30;
//            weakSelf.cutPanel.gop = 30;
            // cut mode
            weakSelf.cutPanel.cropMode = 1;
            weakSelf.cutPanel.rect = cropRect;
            weakSelf.cutPanel.videoQuality = AliyunVideoQualityMedium;
//            weakSelf.cutPanel.bitrate = 10*1000*1000;   // 15Mbps
            weakSelf.cutPanel.encodeMode = 1;           // Force hardware encoding
            weakSelf.cutPanel.shouldOptimize = NO;
            
            int res =[weakSelf.cutPanel startCrop];
            if (res == ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO){
                reject(@"",@"NOT_SUPPORTED_VIDEO",nil);
            }
            else if (res == ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO){
                reject(@"",@"NOT_SUPPORTED_VIDEO",nil);
            }
            else if (res <0 && res != -314){
                reject(@"",@"",nil);
            }
            else if (res == 0) {
                resolve(outputPath);
            }
        }];
    } else if (phAsset.mediaType == PHAssetResourceTypePhoto) {
        CGSize outputSize = CGSizeMake(1080, 1920);
        NSString *rootDirPath = [AliyunPathManager compositionRootDir];
        if (![[NSFileManager defaultManager] fileExistsAtPath:rootDirPath]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:rootDirPath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        
        NSString *tmpPhotoPath = [[rootDirPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"jpg"];
        
        
        [[AliyunPhotoLibraryManager sharedManager] savePhotoWithAsset:phAsset
                                                              maxSize:outputSize
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


#pragma mark - AliyunCropDelegate
- (void)cropOnError:(int)error
{
    NSLog(@"--- %s",__PRETTY_FUNCTION__);
    [self.cutPanel cancel];
}

- (void)cropTaskOnProgress:(float)progress
{
//    NSLog(@"---🚀 %s :%f",__PRETTY_FUNCTION__, progress);
    if (_hasListeners) {
        [self sendEventWithName:@"cropProgress" body:@{@"progress":@(progress)}];
    }
}

- (void)cropTaskOnComplete
{
    NSLog(@"--- ✅ %s ✅",__PRETTY_FUNCTION__);
    if (_hasListeners) {
        [self sendEventWithName:@"cropProgress" body:@{@"progress":@(1.0)}];
    }
    [self.cutPanel cancel];
}

- (void)cropTaskOnCancel
{
    NSLog(@"--- %s",__PRETTY_FUNCTION__);
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
    [self _saveImageToSandBox:path resolve:resolve reject:reject];
}

- (void)_saveImageToSandBox:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    NSString *_assetId = [path stringByReplacingOccurrencesOfString:@"ph://" withString:@""];
    PHAsset *asset = [PHAsset fetchAssetsWithLocalIdentifiers:@[_assetId] options:nil].firstObject;
    if (asset.mediaType == PHAssetMediaTypeVideo) {
        [[AliyunPhotoLibraryManager sharedManager] getVideoWithAsset:asset completion:^(AVAsset *avAsset, NSDictionary *info) {

            NSString *tmpVideoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@".mp4"];
            AVAssetExportSession *exportSession = [[AVAssetExportSession alloc]initWithAsset:avAsset presetName:AVAssetExportPresetHighestQuality];
            exportSession.outputURL = [NSURL URLWithString:tmpVideoPath];
            exportSession.outputFileType = AVFileTypeMPEG4; // mp4
            [exportSession exportAsynchronouslyWithCompletionHandler:^{
                switch (exportSession.status) {
                    case AVAssetExportSessionStatusFailed: // export failed
                        reject(@"",@"AVAssetExportSessionStatusFailed",nil);
                        break;
                    case AVAssetExportSessionStatusCompleted: // finish
                    {
                        resolve(tmpVideoPath);
                    }
                        break;
                    default:
                        break;
                }
            }];
            
        }];
    } else if (asset.mediaType == PHAssetResourceTypePhoto) {
        NSString *tmpPhotoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"jpg"];
        [[AliyunPhotoLibraryManager sharedManager] savePhotoWithAsset:asset
                                                              maxSize:CGSizeMake(1080, 1920)
                                                           outputPath:tmpPhotoPath
                                                           completion:^(NSError *error, UIImage * _Nullable result) {
            if (error) {
                reject(@"",error.localizedDescription,error);
            }else {
                resolve(tmpPhotoPath);
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
