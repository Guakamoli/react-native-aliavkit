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
#import "RNStorageManager.h"

static NSString * const kOssBasePath =  @"https://static.paiyaapp.com/music";
static NSString * const kJsonURL = @"https://static.paiyaapp.com/music/songs.json";

@interface RNMusicService ()
{
    RCTPromiseResolveBlock _resolve;
}

@property (nonatomic, strong) AVPlayer *player;
@property (nonatomic, strong) NSMutableArray<RNMusicInfo *> *musics;
@property (nonatomic, strong) NSMutableArray<RNMusicInfo *> *downloadingMusics;

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
    NSUInteger pageSize = [[musicRequest valueForKey:@"pageSize"] integerValue] ? : 10;
    if ([songName isEqualToString:@"all-music"]) {
        if (self.musics.count == 0) {
            [self _requestJson:^(NSArray<RNMusicInfo *> *infos, NSError *error) {
                if (error) {
                    reject(@"",@"json request fail",error);
                } else {
                    [self.musics addObjectsFromArray:infos];
                    NSArray *array = [[RNStorageManager shared] getPageDataWithPage:page pageSize:pageSize inArray:infos];
                    NSMutableArray *tmpArray = [NSMutableArray array];
                    for (RNMusicInfo *info in array) {
                        [tmpArray addObject:[info convertModelToInfo]];
                    }
                    resolve(tmpArray);
                }
            }];
        } else {
            NSArray<RNMusicInfo *> *array = [[RNStorageManager shared] getPageDataWithPage:page pageSize:pageSize inArray:self.musics];
            NSMutableArray *tmpArray = [NSMutableArray array];
            for (RNMusicInfo *info in array) {
                [tmpArray addObject:[info convertModelToInfo]];
            }
            resolve(tmpArray);
        }
    } else {
        NSArray<RNMusicInfo *> *array= [[RNStorageManager shared] findMusicByName:songName inArray:self.musics];
        NSMutableArray *tmpArray = [NSMutableArray array];
        for (RNMusicInfo *info in array) {
            [tmpArray addObject:[info convertModelToInfo]];
        }
        resolve(tmpArray);
    }
}

RCT_EXPORT_METHOD(playMusic:(NSString *)songID
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    RNMusicInfo *music = [[RNStorageManager shared] findMusicByID:songID inArray:self.musics];
    if (!music) {
        reject(@"",@"Can't find this music",nil);
    }
    if (music.isDBContain) {
        [self _playItemAtPath:music.localPath];
        NSDictionary *info = [music convertModelToInfo];
        resolve(info);
    } else {
        NSString *urlStr = [music.url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
        [RNMusicService downloadMusicWithSongID:songID
                                urlStr:urlStr
                              complete:^(BOOL success, NSString *path) {
            music.localPath = path;
            [self _playItemAtPath:path];
            NSDictionary *info = [music convertModelToInfo];
            resolve(info);
        }];
    }
}

RCT_EXPORT_METHOD(pauseMusic:(NSString *)songID
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    RNMusicInfo *music = [[RNStorageManager shared] findMusicByID:songID inArray:self.musics];
    if (!music) {
        reject(@"",@"Can't find this music",nil);
    }

    if (self.player.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
        [self.player pause];
        BOOL isPaused = (self.player.timeControlStatus == AVPlayerTimeControlStatusPaused);
        resolve(@(isPaused));
    }
}

+ (void)downloadMusicWithSongID:(NSString *)songID
                         urlStr:(NSString *)urlStr
                       complete:(void(^)(BOOL, NSString *))complete
{
    NSURL *url = [NSURL URLWithString:urlStr];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDownloadTask *downloadTask = [session downloadTaskWithRequest:request
                                                            completionHandler:^(NSURL * _Nullable location,
                                                                                NSURLResponse * _Nullable response,
                                                                                NSError * _Nullable error) {
        NSString *fileName = [NSString stringWithFormat:@"%@-%@", songID, response.suggestedFilename];
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
    if (!path || [path isEqualToString:@""]) {
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
}

- (void)_requestJson:(void(^)(NSArray<RNMusicInfo *> *, NSError *error))complete
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
