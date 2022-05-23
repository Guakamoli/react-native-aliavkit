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
#import <React/RCTConvert.h>
#import <AliyunVideoSDKPro/AliyunVodPublishManager.h>
#import "AliyunPasterInfo.h"
#import "ImageCacheTool.h"

static NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";

@interface AliAVServiceBridge ()<AliyunIExporterCallback,AliyunCropDelegate>
{
    BOOL _hasListeners;
    RCTPromiseResolveBlock _videoCropResolve;
    RCTPromiseRejectBlock _videoCropReject;
    RCTPromiseResolveBlock _videoComposeResolve;
    RCTPromiseRejectBlock _videoComposeReject;
    NSString *_videoCropOutputPath;
    NSString *_postCropLocalPath;
    // 裁剪模式， 1：原裁剪 2：post 压缩裁剪
    NSInteger _videoCropType;
}

@property (nonatomic, strong) AliyunCrop *cutPanel;
@property (nonatomic, strong) AliyunVodPublishManager *publishManager;

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


- (BOOL)isBlankObject:(__kindof id)object {
   if (!object) {
       return YES;
   }

   if (object == NULL) {
       return YES;
   }
   
   if ([object isEqual:[NSNull null]]) {
       return YES;
   }
   
   if ([object respondsToSelector:@selector(length)]) {
       NSUInteger count = (NSUInteger)[object performSelector:@selector(length)];
       return count == 0;
   }

    ///集合类型
   if ([object respondsToSelector:@selector(count)]) {
       NSUInteger count = (NSUInteger)[object performSelector:@selector(count)];
       return count == 0;
   }
   return NO;
}

- (NSString *)fileMIMETypeURLSessionWithPath:(NSString*)path {
    //1.确定请求路径
    NSURL *url = [NSURL fileURLWithPath:path];
    //2.创建可变的请求对象
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    __block NSString *mimeType = nil;
    NSURLSessionTask *task = [[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        mimeType = response.MIMEType;
        dispatch_semaphore_signal(semaphore);
    }];
    [task resume];
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    return mimeType;
}
#pragma mark - AliyunIExporterCallback
- (void)exportProgress:(float)progress
{
    [self sendEventWithName:@"storyComposeVideo" body:@{@"progress":@(progress)}];
}
- (void)exporterDidEnd:(NSString *)outputPath
{
    [self sendEventWithName:@"storyComposeVideo" body:@{@"progress":@(1.0)}];
    
    __block NSString *path = outputPath;

    if(_videoComposeResolve != nil){
//        //TODO
//        AliyunNativeParser *nativeParser = [[AliyunNativeParser alloc] initWithPath:path];
//        NSInteger bitRate = nativeParser.getVideoBitrate;
        
        AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:path]];
        CGSize size = [asset avAssetNaturalSize];
        CGFloat frameWidth = size.width;
        CGFloat frameHeight = size.height;
        NSInteger fileSize = [[NSFileManager defaultManager] attributesOfItemAtPath:path error:nil].fileSize;
        NSString *fileType =[self fileMIMETypeURLSessionWithPath:path];
        NSString *fileName = [path lastPathComponent];
        id videoParams = @{@"width":@(frameWidth), @"height":@(frameHeight),@"path":path,@"size":@(fileSize),@"type":fileType,@"name":fileName};
        _videoComposeResolve(videoParams);
    }
    
}
- (void)exporterDidCancel
{
    
}
- (void)exportError:(int)errorCode
{
    
}
#pragma mark - AliyunIExporterCallback


/**
 *  取消视频导出
 */
RCT_EXPORT_METHOD(storyCancelCompose:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if(![self isBlankObject:self.publishManager]){
        [self.publishManager cancelExport];
    }
    if(_videoComposeResolve != nil){
        id composeParam = @{};
        _videoComposeResolve(composeParam);
    }
    resolve(@(TRUE));
}

/**
 * story 视频导出接口
 */
RCT_EXPORT_METHOD(storyComposeVideo:(NSString *)taskPath
                  resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    _videoComposeResolve = resolve;
    _videoComposeReject = reject;
    
    _publishManager = [[AliyunVodPublishManager alloc]init];
    _publishManager.exportCallback = self;
    
    NSString *outputPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];

    
    int result = [self.publishManager exportWithTaskPath:taskPath outputPath:outputPath];
    if (result != 0) {
        AVDLog(@"合成失败");
    }
//    if (result != 0) {
//        [self showAlertWithTitle:[@"合成失败" localString] message:[@"合成失败,请返回重试" localString]];
//    }
}


RCT_EXPORT_METHOD(postCancelCrop:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if(![self isBlankObject:self.cutPanel]){
        [self.cutPanel cancel];
    }
    if(_videoCropResolve != nil){
//        id cropParam = @{@"path":@"", @"isCroped":@(NO)};
        id cropParam = @{};
        _videoCropResolve(cropParam);
        _videoCropResolve = nil;
    }
    resolve(@(TRUE));
}

RCT_EXPORT_METHOD(postCropVideo:(NSString *)videoPath
                  resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    
    if (!videoPath) {
        reject(@"",@"no path param",nil);
        return;
    }
//    if (![videoPath containsString:@"ph://"]) {
//        reject(@"",@"no ph:// scheme",nil);
//        return;
//    }
    _postCropLocalPath = videoPath;
    _videoCropResolve = resolve;
    _videoCropReject = reject;
    
    NSInteger mVideoWidth = 1080;
    NSInteger mVideoHeight = 1920;
    CGFloat mDuration = 0;
    NSInteger mFPS = 60;
    NSInteger mBitrate = 10*1000*1000;
    
    CGRect mCropRect = CGRectMake(0, 0, mVideoWidth, mVideoHeight);
            

         
    @try {
        AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:videoPath]];
        CGSize size = [asset avAssetNaturalSize];
        
        CGFloat frameWidth = size.width;
        CGFloat frameHeight = size.height;
        
        AliyunNativeParser *nativeParser = [[AliyunNativeParser alloc] initWithPath:videoPath];
        
        NSInteger frameRate = nativeParser.getVideoFrameRate;
        
        if(mFPS > frameRate){
            mFPS = frameRate;
        }
        
        NSInteger bitRate = nativeParser.getVideoBitrate;
        
        mDuration = nativeParser.getVideoDuration;
        
//        NSInteger frameWidth = nativeParser.getVideoWidth;
//        NSInteger frameHeight = nativeParser.getVideoHeight;
        
        //宽高过大需要裁剪
        if (frameWidth*frameHeight > mVideoWidth*mVideoHeight) {
            if (frameWidth > frameHeight) {
                mVideoWidth = mVideoHeight;
                mVideoHeight = mVideoWidth*frameHeight/frameWidth;
            }else{
//                mVideoHeight = mVideoWidth;
                mVideoWidth = mVideoHeight*frameWidth/frameHeight;
            }
            if (bitRate < mBitrate) {
                mBitrate = bitRate;
            }
        } else {
            if (bitRate < mBitrate) {
                mBitrate = bitRate;
            }
//            //宽高比特率都比设定值小时，不需要裁剪，直接返回原视频路径
//            if (bitRate < mBitrate) {
//                //TODO
//                id cropParam = @{@"path":videoPath, @"isCroped":@(FALSE)};
//                resolve(cropParam);
//                return;
//            }
            mVideoWidth = frameWidth;
            mVideoHeight = frameHeight;
        }
        //保证宽高为偶数
        if (mVideoWidth%2 == 1) {
            mVideoWidth += 1;
        }
        if (mVideoHeight%2 == 1) {
            mVideoHeight += 1;
        }
        mCropRect = CGRectMake(0, 0, frameWidth, frameHeight);
    } @catch (NSException *exception) {
        reject(@"",@"AliyunNativeParser catch",nil);
        return;
    }
 
    //调用初始化方法创建裁剪对象   self.cutPanel
    self.cutPanel = [[AliyunCrop alloc] initWithDelegate:(id<AliyunCropDelegate>)self];
    
    NSString *outputPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
   
    self.cutPanel.inputPath = videoPath;
    self.cutPanel.outputPath = outputPath;
 
    self.cutPanel.bitrate = mBitrate;
    self.cutPanel.fps = mFPS;
    self.cutPanel.gop = 5;
    
    //视频质量
    self.cutPanel.videoQuality = AliyunVideoQualityMedium;
    //硬编
    self.cutPanel.encodeMode = 1;
    self.cutPanel.cropMode = AliyunCropModeScaleAspectCut;
    
    self.cutPanel.outputSize = CGSizeMake(mVideoWidth, mVideoHeight);
    //裁剪区域
    self.cutPanel.rect = mCropRect;
    
    self.cutPanel.startTime = 0.0;
    self.cutPanel.endTime = mDuration;
     
    int res =[self.cutPanel startCrop];//20003004
    
    if(res == ALIVC_COMMON_RETURN_SUCCESS){
        _videoCropOutputPath = outputPath;
        _videoCropResolve = resolve;
        _videoCropReject = reject;
        _videoCropType = 2;
    }else if (res == ALIVC_SVIDEO_ERROR_PARAM_VIDEO_PATH_NULL){
        // 输入视频路径为空
        NSString *_text = [NSString stringWithFormat:@"%@%d",@"输入视频路径为空  code:",res];
        reject(@"",_text,nil);
    }else{
        NSString *_text = [NSString stringWithFormat:@"%@%d",@"裁剪视频错误， code:",res];
        reject(@"",_text,nil);
    }
}

RCT_EXPORT_METHOD(getRecordColorFilter:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSArray *names = @[@"柔柔",@"优雅",@"红润",@"阳光",@"海蓝",@"炽黄",@"浓烈",@"闪耀",@"朝阳",@"经典",@"粉桃",@"雪梨",@"鲜果",@"麦茶",@"灰白",@"波普",@"光圈",@"海盐",@"黑白",@"胶片",@"焦黄",@"蓝调",@"迷糊",@"思念",@"素描",@"鱼眼",@"马赛克",@"模糊"];
    NSMutableArray *infos = [NSMutableArray array];
    for (NSString *name in names) {
        NSString *iconPath = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:[NSString stringWithFormat:@"Filter/%@/icon.png",name]];
        NSString *path = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:[NSString stringWithFormat:@"Filter/%@",name]];
//        [infos addObject:@{@"iconPath":iconPath,@"filterName":name}];
        [infos addObject:@{@"filterName":name,@"iconPath":iconPath,@"path":path}];
    }
    if (infos.count) {
        resolve(infos);
    } else {
        reject(@"",@"filter resource doesn't exist",nil);
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
    NSString *sourcePath = [options objectForKey:@"sourcePath"];
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
    NSString *typeStr = [options objectForKey:@"resourceType"];
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
            
            for(int i=0; i < [arr count]; i++){
                NSDictionary * mOptions =  arr[i];
                NSMutableDictionary *pasterOptions = [mOptions mutableCopy];
                
                AliyunPasterInfo *info = [[AliyunPasterInfo alloc] initWithDict:pasterOptions];
                
                NSString *path = [info filePath];
                
                BOOL isLocalRes = [info fileExist];//本地文件是否存在
                if(!isLocalRes){
                    path = @"";
                }
                NSNumber *index = [NSNumber numberWithInt:(i+1)];
                [pasterOptions setValue:index forKey:@"index"];
                [pasterOptions setValue:path forKey:@"path"];
                [pasterOptions setValue:@(isLocalRes) forKey:@"isLocalRes"];
                
                [arr replaceObjectAtIndex:i withObject:pasterOptions];
            }
          
//            [arr insertObject:[self _localFacePaster] atIndex:0];
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
        @"index": @1.0,
        @"name": comp.firstObject,
        @"id": @([comp.lastObject integerValue]),
        @"icon": [path stringByAppendingPathComponent:@"icon.png"],
        @"type": @2,
        @"path": path,
        @"isLocalRes": @YES,
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
        @"storyComposeVideo",
        @"startMultiRecording",
        @"postVideoCrop",
        @"cropProgress",
        @"icloudImageDownloadProgress",
        @"icloudVideoDownloadProgress"
    ];
}

- (void)startMultiRecording:(CGFloat)duration
{
    [self sendEventWithName:@"startMultiRecording" body:@{@"duration":@(duration)}];
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
    NSString *source = [options objectForKey:@"source"];
    if (!source || [source isEqualToString:@""]) {
        reject(@"",@"source must contain a path value",nil);
        return;
    }
    CGFloat cropOffsetX = [[options objectForKey:@"cropOffsetX"] floatValue];
    CGFloat cropOffsetY = [[options objectForKey:@"cropOffsetY"] floatValue];
    CGFloat cropWidth = [[options objectForKey:@"cropWidth"] floatValue];
    CGFloat cropHeight = [[options objectForKey:@"cropHeight"] floatValue];
    
    
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
            CGFloat duration = [[options objectForKey:@"duration"] floatValue];
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
        
        NSString *croppedPhotoPath = [[rootDirPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"jpg"];
        
        PHImageRequestOptions *imgRequestOptions = [[PHImageRequestOptions alloc] init];
        imgRequestOptions.resizeMode   = PHImageRequestOptionsResizeModeExact;
        imgRequestOptions.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
        imgRequestOptions.synchronous = NO;
        imgRequestOptions.networkAccessAllowed = YES;//打开网络获取iCloud的图片的功能
        
        CGSize assetSize = CGSizeMake(phAsset.pixelWidth, phAsset.pixelHeight);
        
        [[PHImageManager defaultManager] requestImageForAsset:phAsset
                                                   targetSize:assetSize
                                                  contentMode:PHImageContentModeDefault
                                                      options:imgRequestOptions
                                                resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
            if (result == nil) {
                reject(@"",@"fetch image from photo library failed",nil);
                return;
            }
            AliyunImageCrop *imageCrop = [[AliyunImageCrop alloc] init];
            imageCrop.originImage = result;
            imageCrop.cropMode = AliyunImageCropModeAspectCut;
            imageCrop.cropRect = cropRect;
            imageCrop.outputSize = cropRect.size;
            UIImage *generatedImage = [imageCrop generateImage];
            if (!generatedImage) {
                reject(@"",@"image crop failed", nil);
                return;
            }
            NSData *data = UIImageJPEGRepresentation(generatedImage, 0.65);
            BOOL writeSuccess = [data writeToFile:croppedPhotoPath atomically:YES];
            if (writeSuccess) {
                resolve(croppedPhotoPath);
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
    if (!generatedImage) {
        reject(@"",@"image crop failed", nil);
        return;
    }
    NSData *data = UIImageJPEGRepresentation(generatedImage, 0.65);
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
   
    if(![self isBlankObject:self.cutPanel]){
        [self.cutPanel cancel];
    }
    
    if(_videoCropType == 2 && _videoCropReject != nil){
        NSString *_text = [NSString stringWithFormat:@"%@%d",@"postCropVideo Error code:",error];
        _videoCropReject(@"",_text,nil);
    }
    
    _videoCropOutputPath = nil;
    _videoCropResolve = nil;
    _videoCropReject = nil;
    _videoCropType = 0;
}

- (void)cropTaskOnProgress:(float)progress
{
//    AVDLog(@"---🚀 %s :%f",__PRETTY_FUNCTION__, progress);
    if (_hasListeners) {
        if(_videoCropType == 2){
            [self sendEventWithName:@"postVideoCrop" body:@{@"progress":@(progress)}];
        }else{
            [self sendEventWithName:@"cropProgress" body:@{@"progress":@(progress)}];
        }
    }
}

- (void)cropTaskOnComplete
{
    if(![self isBlankObject:self.cutPanel]){
        [self.cutPanel cancel];
    }
    
    AVDLog(@"--- ✅ %s ✅",__PRETTY_FUNCTION__);
    if (_hasListeners) {
//        if (_videoCropOutputPath && _videoCropOutputPath) {
//            _videoCropResolve(_videoCropOutputPath);
//        }
        if(_videoCropType == 2){
            [self sendEventWithName:@"postVideoCrop" body:@{@"progress":@(1.0)}];
        }else{
            [self sendEventWithName:@"cropProgress" body:@{@"progress":@(1.0)}];
        }
    }
    
    //TODO
//    AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:_videoCropOutputPath]];
//    CGSize size = [asset avAssetNaturalSize];
//    CGFloat frameWidth = size.width;
//    CGFloat frameHeight = size.height;
//
//    AliyunNativeParser *nativeParser = [[AliyunNativeParser alloc] initWithPath:_videoCropOutputPath];
//    NSInteger bitRate = nativeParser.getVideoBitrate;
    //                   [{"coverImage": "",
    //                        "height": 1920,
    //                        "index": 0,
    //                        "localPath": "file:///storage/emulated/0/DCIM/Camera/VID_20220422_114127.mp4",
    //                        "name": "/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/cache/media/editor/Crop_1650603488136_VID_20220422_114127.mp4",
    //                        "path": "/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/cache/media/editor/Crop_1650603488136_VID_20220422_114127.mp4",
    //                        "size": 23039811,
    //                        "type": "video/mp4",
    //                        "width": 1080}
    //                    ]
    if(_videoCropType == 2 && _videoCropResolve != nil && _videoCropOutputPath){
        
        AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:_videoCropOutputPath]];
        CGSize size = [asset avAssetNaturalSize];
        CGFloat frameWidth = size.width;
        CGFloat frameHeight = size.height;
        NSInteger fileSize = [[NSFileManager defaultManager] attributesOfItemAtPath:_videoCropOutputPath error:nil].fileSize;
        NSString *fileType =[self fileMIMETypeURLSessionWithPath:_videoCropOutputPath];
        NSString *fileName = [_videoCropOutputPath lastPathComponent];
        id cropParam = @{
            @"index":@0,
            @"localPath":_postCropLocalPath,
            @"name":fileName,
            @"path":_videoCropOutputPath,
            @"coverImage":@"",
            
            @"size":@(fileSize),
            @"width":@(frameWidth),
            @"height":@(frameHeight),
            
            @"type":@"video/mp4",
            
            @"isCroped":@(YES)
        };
        _videoCropResolve(cropParam);
    }
    
    _videoCropOutputPath = nil;
    _videoCropResolve = nil;
    _videoCropReject = nil;
    _videoCropType = 0;
}

/**
 * 主端取消或者退出到后台
 */
- (void)cropTaskOnCancel
{
    AVDLog(@"--- %s",__PRETTY_FUNCTION__);
    if(![self isBlankObject:self.cutPanel]){
        [self.cutPanel cancel];
    }
    _videoCropOutputPath = nil;
//    _videoCropResolve = nil;
    _videoCropReject = nil;
    _videoCropType = 0;
}

RCT_EXPORT_METHOD(saveToSandBox:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *path = [options objectForKey:@"path"];
    if (!path) {
        reject(@"",@"no path param",nil);
        return;
    }
    //增加对file路径
    if ([path.lowercaseString hasPrefix:@"file://"])
    {
        [self _saveFileToSandBox:path resolve:resolve reject:reject];
        return;
    }
    if (![path containsString:@"ph://"]) {
        reject(@"",@"no ph:// scheme",nil);
        return;
    }
    [self _saveImageToSandBox:path resolve:resolve reject:reject];
}

//保持
- (void)_saveFileToSandBox:(NSString *)sourcePath resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    //使用UIImage载入相册图片时,不需要file://的前缀
    NSString *localPath = [sourcePath substringFromIndex:@"file://".length];
    UIImage *result = [UIImage imageWithContentsOfFile:localPath];
    if(!result)
    {
        resolve(sourcePath);
        return;
    }
    NSString *aliyunPath = [AliyunPathManager compositionRootDir];
    NSString *outputName = [ImageCacheTool MD5ForUpper32Bate:localPath];
    NSString *outputPhotoPath = [[aliyunPath stringByAppendingPathComponent:outputName ] stringByAppendingPathExtension:@"jpg"];
    if([UIImage imageWithContentsOfFile:outputPhotoPath])
    {
        resolve(outputPhotoPath);
        return;;
    }
    if (result.imageOrientation != UIImageOrientationUp) {
        UIGraphicsBeginImageContextWithOptions(result.size, NO, result.scale);
        [result drawInRect:(CGRect){0, 0, result.size}];
        UIImage *normalizedImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        result = normalizedImage;
    }
    NSData *imageData = UIImageJPEGRepresentation(result,0.99);
    BOOL writeSuccess = [imageData writeToFile:outputPhotoPath atomically:YES];
    if (writeSuccess) {
        resolve(outputPhotoPath);
    } else {
        reject(@"",@"image write fail",nil);
    }
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
            exportSession.shouldOptimizeForNetworkUse = YES;
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
        NSString *compositionRootDir = [AliyunPathManager compositionRootDir];
        if (![[NSFileManager defaultManager] fileExistsAtPath:compositionRootDir]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:compositionRootDir withIntermediateDirectories:YES attributes:nil error:nil];
        }
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
    NSString *videoPath = [options objectForKey:@"videoPath"];
    if (!videoPath || [videoPath isEqualToString:@""]) {
        return;
    }
    AliyunNativeParser *parser = [[AliyunNativeParser alloc] initWithPath:videoPath];
    CGFloat duration = [parser getVideoDuration];
    CGFloat startTime = [[options objectForKey:@"startTime"] floatValue];
    if (startTime >= duration) {
        return;
    }
    
    NSInteger itemPerTime = [[options objectForKey:@"itemPerTime"] integerValue]; //ms
    if (itemPerTime == 0) {
        itemPerTime = 1000;
    }
    
    CGSize imageSize = [RCTConvert CGSize:options[@"imageSize"]];
    if (!imageSize.width || !imageSize.height) {
        imageSize = CGSizeMake(200, 200);
    }
    
    //for test only
//    videoPath = [[NSUserDefaults standardUserDefaults] objectForKey:@"videoSavePath"];
//    AliyunNativeParser *parser = [[AliyunNativeParser alloc] initWithPath:videoPath];
//    CGFloat duration = [parser getVideoDuration];
//    CGFloat startTime = 0.0;
//    NSInteger itemPerTime = 1000; //ms
    BOOL needCover = [RCTConvert BOOL:options[@"needCover"]];
    
    if (needCover) {
        CGFloat time = startTime == 0 ? 0.1 : startTime;
        NSArray *arr = @[[NSValue valueWithCMTime:CMTimeMakeWithSeconds(time, 1000)]];
        [self generateImagesForTimes:arr
                           videoPath:videoPath
                         maximumSize:imageSize
                             success:^(NSArray * paths) {
            complete(paths);
        }];
    } else {
        [self thumbnailFromVideoPath:videoPath
                         itemPerTime:itemPerTime
                           startTime:startTime
                            duration:duration
                 generatorOutputSize:imageSize
                            complete:complete];
    }
}

- (void)thumbnailFromVideoPath:(NSString *)videoPath
                   itemPerTime:(NSInteger)itemPerTime
                     startTime:(CGFloat)beginTime
                      duration:(CGFloat)duration
           generatorOutputSize:(CGSize)outputSize
                      complete:(void (^)(NSArray *))complete
{
//    [self removeImages];
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
            dispatch_async(dispatch_get_main_queue(), ^{
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

- (void)generateImagesForTimes:(NSArray *)array
                     videoPath:(NSString *)videoPath
                   maximumSize:(CGSize)outputSize
                       success:(void(^)(NSArray * paths))success
{
    NSURL *url = [[NSURL alloc] initFileURLWithPath:videoPath];
    AVURLAsset *urlAsset = [[AVURLAsset alloc] initWithURL:url options:nil];
    AVAssetImageGenerator *imageGenerator = [[AVAssetImageGenerator alloc] initWithAsset:urlAsset];
    imageGenerator.appliesPreferredTrackTransform = YES;
    imageGenerator.requestedTimeToleranceBefore = kCMTimeZero;
    imageGenerator.requestedTimeToleranceAfter = kCMTimeZero;
    imageGenerator.maximumSize = outputSize;
    [imageGenerator generateCGImagesAsynchronouslyForTimes:array
                                         completionHandler:^(CMTime requestedTime, CGImageRef  _Nullable image, CMTime actualTime, AVAssetImageGeneratorResult result, NSError * _Nullable error) {
        
        if (result == AVAssetImageGeneratorSucceeded) {
            UIImage *img = [[UIImage alloc] initWithCGImage:image];
            NSString *uiud = [[[NSUUID UUID] UUIDString] stringByAppendingPathExtension:@".png"];
            NSString *path = [self saveImgToSandBox:img withName:uiud];
            dispatch_async(dispatch_get_main_queue(), ^{
                success(@[path]);
            });
        }
    }];
}

- (NSString *)saveImgToSandBox:(UIImage *)image withIndex:(int)index
{
    NSString *imageName = [NSString stringWithFormat:@"%03d.png", index];
    return [self saveImgToSandBox:image withName:imageName];
}

- (NSString *)saveImgToSandBox:(UIImage *)image withName:(NSString *)name
{
    NSString *fileDirectoryPath = ThumnailDirectory();
    NSFileManager *fm = [NSFileManager defaultManager];
    if (![fm fileExistsAtPath:fileDirectoryPath]) {
        NSString *documentsDirectory = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
        fileDirectoryPath = [documentsDirectory stringByAppendingPathComponent:@"thumbNail"];
        [fm createDirectoryAtPath:fileDirectoryPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    NSData *imgData = UIImagePNGRepresentation(image);
    NSString *imgPath = [fileDirectoryPath stringByAppendingPathComponent:name];
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
