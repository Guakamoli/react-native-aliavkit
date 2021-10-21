//
//  RNMusicService.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/19.
//

#import "RNMusicService.h"
#import <AVFoundation/AVFoundation.h>
#import "RNMusicInfo.h"
#import "AliyunEffectResourceModel.h"
#import <AliyunVideoSDKPro/AliyunHttpClient.h>
#import <AliyunVideoSDKPro/AVAsset+AliyunSDKInfo.h>
#import "AliyunPathManager.h"
#import <React/RCTBridge.h>
#import <AFNetworking/AFNetworking.h>
#import "RNStorageManager.h"

static NSString * const kOssBasePath =  @"https://static.paiyaapp.com/music";
static NSString * const kJsonURL = @"https://static.paiyaapp.com/music/songs.json";

@interface RNMusicService ()
{
    RCTPromiseResolveBlock _resolve;
}

@property (nonatomic, strong) AVPlayer *player;
@property (nonatomic, strong) NSMutableArray<RNMusicInfo *> *musics;

@end

@implementation RNMusicService

RCT_EXPORT_MODULE()

- (AVPlayer *)player{
    if (!_player) {
        _player = [[AVPlayer alloc] init];
    }
    return _player;
}

- (NSMutableArray<RNMusicInfo *> *)musics
{
    if (!_musics) {
        _musics = [NSMutableArray array];
    }
    return _musics;
}

/*
 name:"", //all-music,
 page:1,
 */

RCT_EXPORT_METHOD(getMusics:(NSDictionary *)musicRequest
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *songName = [musicRequest valueForKey:@"name"];
    NSUInteger page = [[musicRequest valueForKey:@"page"] integerValue];
    if ([songName isEqualToString:@"all-music"]) {
        if (self.musics.count == 0) {
            [self requestJson:^(NSArray<RNMusicInfo *> *infos, NSError *error) {
                if (error) {
                    reject(@"",@"json request fail",error);
                } else {
                    [self.musics addObjectsFromArray:infos];
                    NSArray *array = [[RNStorageManager shared] getPageDataWithPage:page inArray:infos];
                    resolve(array);
                }
            }];
        } else {
            NSArray *array = [[RNStorageManager shared] getPageDataWithPage:page inArray:self.musics];
            resolve(array);
        }
    } else {
        NSArray *array= [[RNStorageManager shared] findMusicByName:songName inArray:self.musics];
        resolve(array);
    }
}

RCT_EXPORT_METHOD(playMusic:(NSString *)musicID
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    RNMusicInfo *music = [[RNStorageManager shared] findMusicByID:musicID inArray:self.musics];
    if (music.isDBContain) {
        [self _playItemAtPath:music.localPath];
    } else {
        NSString *urlStr = [music.url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
        [self _downloadMusicByURLStr:urlStr complete:^(BOOL success, NSString *path) {
            music.localPath = path;
            [self _playItemAtPath:path];
        }];
    }
    resolve([music convertModelToInfo]);
}

RCT_EXPORT_METHOD(pauseMusic:(NSString *)musicID
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    if (self.player.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
        [self.player pause];
        BOOL isPaused = (self.player.timeControlStatus == AVPlayerTimeControlStatusPaused);
        resolve(@(isPaused));
    }
}

- (void)_downloadMusicByURLStr:(NSString *)urlStr complete:(void(^)(BOOL, NSString *))complete
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
        if (complete) {
            complete(success, destinationPath);
        }
    }];
    
    [downloadTask resume];
}

- (void)_playItemAtPath:(NSString *)path
{
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
}

- (void)requestJson:(void(^)(NSArray<RNMusicInfo *> *, NSError *error))complete
{
    NSURL *url = [NSURL URLWithString:kJsonURL];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    NSURLSession *session = [NSURLSession sharedSession];
    
    NSMutableArray<RNMusicInfo *> *musics = [NSMutableArray array];
    NSURLSessionDataTask *task = [session dataTaskWithRequest:request
                                            completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        NSArray *songs = dict[@"songs"];
        for (NSDictionary *song in songs) {
            RNMusicInfo *info = [[RNMusicInfo alloc] initWithDictionary:song];
            [musics addObject:info];
        }
        if (complete) {
            complete(musics, error);
        }
    }];
    [task resume];
}


@end
