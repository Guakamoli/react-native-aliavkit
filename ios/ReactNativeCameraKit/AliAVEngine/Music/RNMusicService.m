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
#import <AliyunVideoSDKPro/AVAsset+AliyunSDKInfo.h>
#import "AliyunPathManager.h"
#import <React/RCTBridge.h>
#import <AFNetworking/AFNetworking.h>
#import <AliyunVideoSDKPro/AliyunNativeParser.h>
#import <AliyunVideoSDKPro/AliyunCrop.h>

NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";
NSString * const kOssBasePath =  @"https://static.paiyaapp.com";

@interface RNMusicService ()<AliyunCropDelegate>
{
    AliyunMusicPickModel *_currentMusicModel;
    RCTPromiseResolveBlock _resolve;
}
@property (nonatomic, strong) AVPlayer *player;
@property (nonatomic, strong) AliyunCrop *musicCrop;

@end

@implementation RNMusicService

RCT_EXPORT_MODULE()

- (AVPlayer *)player{
    if (!_player) {
        _player = [[AVPlayer alloc] init];
    }
    return _player;
}

RCT_EXPORT_METHOD(downloadMusic:(NSString *)musicName
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    _resolve = resolve;
    NSString *httpPath = [NSString stringWithFormat:@"%@/%@",kOssBasePath,musicName];
    NSString *urlStr = [httpPath stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    NSString *m1 = @"https://static.paiyaapp.com/music/794-%E6%9C%89%E4%BD%95%E4%B8%8D%E5%8F%AF/ChAKC11sg22ABl56AB2HjB36SoY.64.aac?OSSAccessKeyId=LTAI4G3ydMZChzG5mGHoojLx&Expires=1634730233&Signature=A1QZ2UckazCPNNmoiQtH5kXgdGE%3D";
    NSString *m2 = @"https://static.paiyaapp.com/music/Berlin%20-%20Take%20My%20Breath%20Away.mp3?OSSAccessKeyId=LTAI4G3ydMZChzG5mGHoojLx&Expires=1634646685&Signature=WNpS9%2BhnsdsGpAfm%2FQK5tcaW4B8%3D";
    [self downloadLargeFileByURLStr:m1];
}

- (void)downloadLargeFileByURLStr:(NSString *)urlStr
{
    NSURL *url = [NSURL URLWithString:urlStr];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDownloadTask *downloadTask = [session downloadTaskWithRequest:request
                                                            completionHandler:^(NSURL * _Nullable location,
                                                                                NSURLResponse * _Nullable response,
                                                                                NSError * _Nullable error) {
        NSString *fileName = response.suggestedFilename;
        NSString *musicDirPath = [[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:@"music"];
        [AliyunPathManager makeDirExist:musicDirPath];
        NSString *destinationPath = [musicDirPath stringByAppendingPathComponent:fileName];
        
        BOOL success = [[NSFileManager defaultManager] moveItemAtURL:location toURL:[NSURL fileURLWithPath:destinationPath] error:nil];
        if (success) {
            [self playItemWithPath:destinationPath];
        }
//        NSLog(@"fullPath: %@",destinationPath);
    }];
    
    [downloadTask resume];
}

- (void)playItemWithPath:(NSString *)path
{
    if (!path) {
        return;
    }
    
    CGFloat start = 0.0;
    AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:path]];
    float duration = [asset aliyunDuration];
    AVMutableComposition *mutableComposition = [AVMutableComposition composition]; // Create the video composition track.
    AVMutableCompositionTrack *mutableCompositionAudioTrack =
    [mutableComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
    
    NSArray *mediaTracks = [asset tracksWithMediaType:AVMediaTypeAudio];
    if (mediaTracks.count <= 0) {
        return;
    }
    AVAssetTrack *audioTrack = mediaTracks.firstObject;
    CMTime startTime = CMTimeMake(start, 1000);
    CMTime stopTime = CMTimeMake((start+duration*1000), 1000);
    CMTimeRange exportTimeRange = CMTimeRangeFromTimeToTime(startTime,stopTime);
    [mutableCompositionAudioTrack insertTimeRange:exportTimeRange ofTrack:audioTrack atTime:kCMTimeZero error:nil];
    
    [self.player replaceCurrentItemWithPlayerItem:[AVPlayerItem playerItemWithAsset:mutableComposition]];
    [self.player play];
    
    AliyunMusicPickModel *model = [AliyunMusicPickModel new];
    model.startTime = 0;
    model.duration = duration;
    model.path = path;
    NSDictionary *dict = @{@"path":model.path,
                           @"startTime":@(model.startTime),
                           @"duration":@(model.duration)};
    _resolve(dict);
}

#pragma mark - AliyunCropDelegate -
-(void)cropOnError:(int)error
{
    
}

-(void)cropTaskOnComplete
{
    
}
    
@end
