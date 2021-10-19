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
    CGSize outputSize = CGSizeMake(1080, 1920);
    
    CGRect cropRect = CGRectMake(cropOffsetX, 1920-cropOffsetY, cropWidth, cropHeight);
    
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
            weakSelf.cutPanel.fps = 30;
            weakSelf.cutPanel.gop = 30;
            // cut mode
            weakSelf.cutPanel.cropMode = 1;
            weakSelf.cutPanel.rect = cropRect;
            weakSelf.cutPanel.bitrate = 15*1000*1000; // 15Mbps
            weakSelf.cutPanel.encodeMode = 1; // Force hardware encoding
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
        NSString *tmpPhotoPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString] ] stringByAppendingPathExtension:@"jpg"];
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
    NSLog(@"---🚀 %s :%f",__PRETTY_FUNCTION__, progress);
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

@end
