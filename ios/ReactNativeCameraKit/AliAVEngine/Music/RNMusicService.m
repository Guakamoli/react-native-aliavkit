//
//  RNMusicService.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/19.
//

#import "RNMusicService.h"
#import "AlivcAppServer.h"
#import <AVFoundation/AVFoundation.h>
#import "AliyunMusicPickModel.h"

#import "AliyunEffectResourceModel.h"
#import <AliyunVideoSDKPro/AliyunHttpClient.h>
#import "AliyunPathManager.h"
#import <React/RCTBridge.h>

NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";
NSString * const kOssBasePath =  @"https://static.paiyaapp.com";

@interface RNMusicService ()

@property (nonatomic, strong) AVPlayer *player;


@end

@implementation RNMusicService

RCT_EXPORT_MODULE()

- (AVPlayer *)player{
    if (!_player) {
        _player = [[AVPlayer alloc] init];
    }
    return _player;
}

+ (void)fetchMusicPlayUrl:(NSString *)musicId
                  success:(void(^)(NSString *playPath,NSString *expireTime))success
                  failure:(void(^)(NSString *errorStr))failure
{
    NSMutableDictionary *parameters = @{}.mutableCopy;
    NSString *url = [NSString stringWithFormat:@"%@/music/getPlayPath",kAlivcQuUrlString];
    if (musicId) {
        [parameters setObject:musicId forKey:@"musicId"];
    }
    
    [AlivcAppServer getWithUrlString:url
                          parameters:parameters
                   completionHandler:^(NSString * _Nullable errString, NSDictionary * _Nullable resultDic) {
        if (errString) {
            failure(errString);
        }else{
            BOOL result = [resultDic[@"result"] boolValue];
            if (result) {
                NSString *playPath = resultDic[@"data"][@"playPath"];
                NSString *expireTime = resultDic[@"data"][@"expireTime"];
                success(playPath,expireTime);
            }else{
                failure(resultDic[@"message"]);
            }
        }
    }];
}

// https://static.paiyaapp.com/music/Berlin - Take My Breath Away.mp3
RCT_EXPORT_METHOD(downloadMusic:(NSString *)musicName
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *urlStr = [NSString stringWithFormat:@"%@/%@",kOssBasePath,musicName];

    // 下载资源
    NSString *basePath= [[self storageFullPath] stringByAppendingPathComponent:[AliyunPathManager randomString]];
    NSString *destination = [basePath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@", musicName]];
    
    AliyunHttpClient *httpClient = [[AliyunHttpClient alloc] initWithBaseUrl:nil];
    [httpClient download:urlStr destination:destination progress:^(NSProgress *downloadProgress) {
        NSLog(@"--- progress: %ll",downloadProgress.completedUnitCount/downloadProgress.totalUnitCount);
    } completionHandler:^(NSURL *filePath, NSError *error) {
        resolve(filePath.path);
    }];
}

- (NSString *)storageFullPath
{
    NSString *fullPath = [NSHomeDirectory() stringByAppendingPathComponent:@"musicRes"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:fullPath]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:fullPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    return fullPath;
}


- (void)playItem:(AliyunMusicPickModel *)model
{
//    AliyunMusicPickModel *model = a;
//    model.startTime = _startTime;
//    model.duration = _duration;
    AVMutableComposition *composition = [self generateMusicWithPath:model.path start:model.startTime duration:model.duration];
    [self.player replaceCurrentItemWithPlayerItem:[AVPlayerItem playerItemWithAsset:composition]];
    [self.player play];
}

- (AVMutableComposition *)generateMusicWithPath:(NSString *)path
                                          start:(float)start
                                       duration:(float)duration
{
    if (!path) {
        return nil;
    }
    NSLog(@"开始时间======%f",start);
    AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:path]];
    AVMutableComposition *mutableComposition = [AVMutableComposition composition]; // Create the video composition track.
    AVMutableCompositionTrack *mutableCompositionAudioTrack =[mutableComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
    NSArray *array = [asset tracksWithMediaType:AVMediaTypeAudio];
    if (array.count > 0) {
        AVAssetTrack *audioTrack = array[0];
        CMTime startTime = CMTimeMake(start, 1000);
        CMTime stopTime = CMTimeMake((start+duration*1000), 1000);
        //    CMTimeRange range = CMTimeRangeMake(kCMTimeZero, CMTimeSubtract(stopTime, startTime));
        CMTimeRange exportTimeRange = CMTimeRangeFromTimeToTime(startTime,stopTime);
        [mutableCompositionAudioTrack insertTimeRange:exportTimeRange ofTrack:audioTrack atTime:kCMTimeZero error:nil];
    }
    
    return mutableComposition;
}

@end
