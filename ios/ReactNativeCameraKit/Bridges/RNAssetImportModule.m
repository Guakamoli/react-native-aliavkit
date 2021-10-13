//
//  RNAssetImportModule.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/13.
//

#import "RNAssetImportModule.h"
#import <React/RCTBridge.h>
#import "AliyunMediaConfig.h"
#import "AliyunCompressManager.h"
#import "AliyunCompositionInfo.h"
#import "AliyunPathManager.h"
#import <AliyunVideoSDKPro/AliyunErrorCode.h>
#import <AliyunVideoSDKPro/AliyunImporter.h>
#import <AliyunVideoSDKPro/AliyunNativeParser.h>
#import "AlivcShortVideoTempSave.h"
#import "AVAsset+VideoInfo.h"

@interface RNAssetImportModule ()

@property (nonatomic, strong) AliyunMediaConfig *mediaConfig;
@property (nonatomic, strong) AliyunCompressManager *manager;

@end

@implementation RNAssetImportModule
RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(cutAsset:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    
}

- (void)pickViewDidFinishWithAssets:(NSArray<AliyunCompositionInfo *> *)assets duration:(CGFloat)duration
{
    
    AliyunCompositionInfo *info = [assets firstObject];
    [self handleOriginalSizeWithInfo:info];
    
    __weak typeof(self) weakSelf = self;
    [self compressVideoIfNeeded:assets completion:^(BOOL failed , int errorResult){
        if (failed) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (errorResult != ALIVC_SVIDEO_ERROR_TRANSCODE_BACKGROUND) {
                    NSString *msg = NSLocalizedString(@"视频格式不支持", nil);
                    if (errorResult == -1) {
                        msg = NSLocalizedString(@"图片过宽或过高", nil);
                    }
                }
            });
            return;
        }
        
        NSString *editDir = [AliyunPathManager compositionRootDir];
        NSString *taskPath = [editDir stringByAppendingPathComponent:[AliyunPathManager randomString]];
        AliyunImporter *importer = [[AliyunImporter alloc] initWithPath:taskPath outputSize:weakSelf.mediaConfig.outputSize];
        // add paths
        NSMutableArray *saveAVURLAssetArray = [[NSMutableArray alloc] init];
        for (int i = 0; i < assets.count; i++) {
            AliyunCompositionInfo *info = assets[i];
            if (info.type == AliyunCompositionInfoTypePhoto) {
                AliyunClip *clip = [[AliyunClip alloc] initWithImagePath:info.sourcePath duration:info.duration animDuration:i == 0 ? 0 : 1];
                [importer addMediaClip:clip];
            } else if (info.type == AliyunCompositionInfoTypeGif) {
                AliyunClip *clip = [[AliyunClip alloc] initWithGifPath:info.sourcePath startTime:info.startTime duration:info.duration];
                [importer addMediaClip:clip];
            } else {
                AliyunClip *clip = [[AliyunClip alloc] initWithVideoPath:info.sourcePath startTime:info.startTime duration:info.duration animDuration:i == 0 ? 0 : 1];
                [importer addMediaClip:clip];
            }
            if (info.asset) {
                [saveAVURLAssetArray addObject:info.asset];
            }
        }
        
        // set video param
        AliyunVideoParam *param = [[AliyunVideoParam alloc] init];
        param.fps = weakSelf.mediaConfig.fps;
        param.gop = weakSelf.mediaConfig.gop;
        param.videoQuality = (AliyunVideoQuality)weakSelf.mediaConfig.videoQuality;
        if (weakSelf.mediaConfig.videoQuality == AliyunVideoQualityVeryHight) {
            param.bitrate = 10*1000*1000; // 10Mbps
        }
        if (weakSelf.mediaConfig.cutMode == AliyunMediaCutModeScaleAspectCut) {
            param.scaleMode = AliyunScaleModeFit;
        }else{
            param.scaleMode = AliyunScaleModeFill;
        }
        // 编码模式
        if (weakSelf.mediaConfig.encodeMode ==  AliyunEncodeModeHardH264) {
            param.codecType = AliyunVideoCodecHardware;
        }else if(weakSelf.mediaConfig.encodeMode == AliyunEncodeModeSoftFFmpeg) {
            param.codecType = AliyunVideoCodecOpenh264;
        }
        
        [importer setVideoParam:param];
        // generate config
        [importer generateProjectConfigure];
        // output path
        weakSelf.mediaConfig.outputPath = [[taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
        
        // edit view controller
        //        [[AlivcShortVideoRoute shared] registerEditVideoPath:nil];
        //        [[AlivcShortVideoRoute shared] registerEditMediasPath:taskPath];
        //        [[AlivcShortVideoRoute shared] registerHasRecordMusic:NO];
        //
        //        [[AlivcShortVideoRoute shared] registerMediaConfig:weakSelf.mediaConfig];
        //存储点击的相册资源，防止之后合成没有相关资源导致失败
        [[AlivcShortVideoTempSave shared] saveResources:(NSArray *)saveAVURLAssetArray];
        
        //controller 跳转
        //        if (self.controllerType == AlivcCompositionViewControllerTypeVideoEdit) {
        //            dispatch_async(dispatch_get_main_queue(), ^{
        //                UIViewController *editVC = [[AlivcShortVideoRoute shared] alivcViewControllerWithType:AlivcViewControlEdit];
        //                [editVC setValue:self.teleprompterModel forKey:@"teleprompterModel"];
        //                [editVC setValue:@NO forKey:@"isTakeFromPaiya"];
        //                [hud hideAnimated:YES];
        //                [self.navigationController pushViewController:editVC animated:YES];
        //            });
        //        }else{
        //            //配置
        //            AliyunCompositionInfo *info = assets.firstObject;
        //            AliyunMediaConfig *tempConfig = [weakSelf.mediaConfig copy];
        //            tempConfig.maxDuration = info.duration;
        //            tempConfig.sourcePath = assets.firstObject.sourcePath;
        //            [[AlivcShortVideoRoute shared]registerMediaConfig:tempConfig];
        //            UIViewController *record = [[AlivcShortVideoRoute shared] alivcViewControllerWithType:AlivcViewControlRecordMix];
        //            dispatch_async(dispatch_get_main_queue(), ^{
        //                [hud hideAnimated:YES];
        //                [self.navigationController pushViewController:record animated:YES];
        //            });
        //        }
    }];
}

/**
 原比例下对size的处理
 
 @param info 媒体资源
 */
- (void)handleOriginalSizeWithInfo:(AliyunCompositionInfo *)info
{
    CGFloat ratio;
    if (info.type == AliyunCompositionInfoTypePhoto) {
        //获取原比例的图片尺寸
        CGSize size = [self originalImageSizeWithInfo:info];
        ratio = size.width / size.height;
    } else {
        //获取原比例的视频尺寸
        CGSize size = CGSizeZero;
        if (info.asset) {
            size = [info.asset avAssetNaturalSize];
        } else if (info.phAsset){//单帧GIF应该取phAsset
            size = CGSizeMake(info.phAsset.pixelWidth, info.phAsset.pixelHeight);
        } else {
            NSLog(@"#Wrong:视频信息为nil");
        }
        ratio = size.width / size.height;
    }
    
    if (ratio > 1) {
        CGFloat width = _mediaConfig.outputSize.width * ratio;
        _mediaConfig.outputSize = CGSizeMake(width, _mediaConfig.outputSize.width);
    } else if (ratio > 0) {
        CGFloat height = _mediaConfig.outputSize.width / ratio;
        _mediaConfig.outputSize = CGSizeMake(_mediaConfig.outputSize.width, height);
    }
    _mediaConfig.outputSize = [_mediaConfig fixedSize];
}

//如果视频分辨率过大或fps,gop过大或存在b帧，压缩视频
- (void)compressVideoIfNeeded:(NSArray<AliyunCompositionInfo *> *)assets
                             completion:(void(^)(BOOL failed, int errorCode))completion
{
    __weak typeof(self)weakSelf =self;
    __block BOOL failed = NO;
    __block int errorResult =0;
    NSString *root = [AliyunPathManager compositionRootDir];
    dispatch_queue_t compressQueue = dispatch_queue_create("com.guakamoli.avengine", DISPATCH_QUEUE_SERIAL);
    dispatch_semaphore_t _semaphore = dispatch_semaphore_create(0);
    dispatch_async(compressQueue, ^{
        for (int i = 0; i < assets.count; i++) {
            if (failed) break;
            __weak AliyunCompositionInfo *info = assets[i];
            if (!info.phAsset) {
                AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:info.sourcePath]];
                CGFloat resolution = [asset avAssetNaturalSize].width * [asset avAssetNaturalSize].height;
                CGFloat max = [weakSelf maxVideoSize].width * [weakSelf maxVideoSize].height;
                
                AliyunNativeParser *avParser = [[AliyunNativeParser alloc] initWithPath:info.sourcePath];
                NSLog(@"--------->frameRate:%f  GopSize:%zd",asset.frameRate,avParser.getGopSize);
                //分辨率过大
                
                //合拍算一半
                //                if (self.controllerType == AlivcCompositionViewControllerTypeVideoMix) {
                //                    max = max / 2;
                //                }
                
                if (resolution > max) {
                    NSString *outputPath = [[root stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
                    CGFloat factor = MAX(weakSelf.mediaConfig.outputSize.width, weakSelf.mediaConfig.outputSize.height) / MAX([asset avAssetNaturalSize].width, [asset avAssetNaturalSize].height);
                    if (factor > 1) {
                        factor = 1.0f;
                    }
                    CGSize size = [asset avAssetNaturalSize];
                    // 最终分辨率必须为偶数
                    CGFloat outputWidth = rint(size.width * factor / 2) * 2;
                    CGFloat outputHeight = rint(size.height * factor / 2) * 2;
                    CGSize outputSize = CGSizeMake(outputWidth, outputHeight);
                    [weakSelf.manager compressWithSourcePath:info.sourcePath
                                                  outputPath:outputPath
                                                  outputSize:outputSize
                                                     success:^{
                        info.sourcePath = outputPath;
                        dispatch_semaphore_signal(_semaphore);
                    } failure:^(int errorCode) {
                        failed = YES;
                        errorResult = errorCode;
                        dispatch_semaphore_signal(_semaphore);
                    }];
                    dispatch_semaphore_wait(_semaphore, DISPATCH_TIME_FOREVER);
                }
            } else {
                //4K video is not support
                if (self.mediaConfig.outputSize.width > 3840 || self.mediaConfig.outputSize.height > 3840) {
                    failed = YES;
                    errorResult = -1;
                }
            }
        }
        completion(failed, errorResult);
    });
}

/**
 获取原比例的情况下图片的尺寸
 
 @param imageInfo 图片的图片信息
 @return 原比例下图片的分辨率
 */
- (CGSize )originalImageSizeWithInfo:(AliyunCompositionInfo *)imageInfo
{
    UIImage *image = [UIImage imageWithContentsOfFile:imageInfo.sourcePath];
    return image.size;
}

- (AliyunCompressManager *)manager {
    if (!_manager) {
        _manager = [[AliyunCompressManager alloc] initWithMediaConfig:self.mediaConfig];
    }
    return _manager;
}

- (AliyunMediaConfig *)mediaConfig
{
    if (!_mediaConfig) {//默认配置
        _mediaConfig = [AliyunMediaConfig defaultConfig];
        _mediaConfig.minDuration = 2.0f;
        _mediaConfig.maxDuration = 15.f;
        _mediaConfig.gop = 30;
        _mediaConfig.cutMode = AliyunMediaCutModeScaleAspectFill;
        _mediaConfig.videoOnly = YES;
        _mediaConfig.backgroundColor = [UIColor blackColor];
        _mediaConfig.videoQuality = AliyunMediaQualityVeryHight;
    }
    return _mediaConfig;
}

- (CGSize)maxVideoSize {
    CGSize size = CGSizeMake(1080, 1920);
    //    if ([self isBelowIphone_5]){
    //        size = CGSizeMake(720, 960);
    //    } else if ([self isBelowIphone_6]){
    //        size = CGSizeMake(1080, 1080);
    //    } else {
    //        size = CGSizeMake(1080, 1920); // Default for other devices
    //    }
    return size;
}

@end
