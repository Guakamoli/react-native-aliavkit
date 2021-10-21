//
//  RNStorageManager.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/21.
//

#import "RNStorageManager.h"
#import "RNMusicInfo.h"

@implementation RNStorageManager
{
    NSDictionary *_musicSearchCache;
}

static RNStorageManager *_instance = nil;
+ (instancetype)shared
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (!_instance) {
            _instance = [[RNStorageManager alloc] init];
        }
    });
    return _instance;
}

- (instancetype)init
{
    if (self = [super init]) {
        _musicSearchCache = @{};
    }
    return self;
}

- (NSArray *)getPageDataWithPage:(NSUInteger)page inArray:(NSArray *)array
{
    int pageDataCount = 10;
    NSUInteger num = array.count % page;
    if (array.count <= pageDataCount) {
        return array;
    }
    else if (num != 0) {
        if (page * pageDataCount > array.count) {
            return [array subarrayWithRange:NSMakeRange((page-1) * pageDataCount, array.count)];
        }
        
    }
    return [array subarrayWithRange:NSMakeRange(0, pageDataCount * page)];
}

- (RNMusicInfo *)findMusicByID:(NSString *)songID inArray:(NSArray<RNMusicInfo *> *)array
{
    __block RNMusicInfo *song = nil;
    [array enumerateObjectsUsingBlock:^(RNMusicInfo * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.songID isEqualToString:songID]) {
            song = obj;
            *stop = YES;
        }
    }];
//    if (song) {
//        NSString *cacheID = [NSString stringWithFormat:@"%@-%@",song.songID,song.name];
//        [cacheID setValue:song forKey:cacheID];
//    }
    return song;
}

- (NSArray<RNMusicInfo *> *)findMusicByName:(NSString *)name inArray:(NSArray<RNMusicInfo *> *)array
{
    NSMutableArray *arr = [NSMutableArray array];
    [array enumerateObjectsUsingBlock:^(RNMusicInfo * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.name containsString:name]) {
            [arr addObject:obj];
        }
    }];
    return arr;
}

- (void)saveSearchedSongInMemoryCache:(RNMusicInfo *)searchedSong
{
    if (!searchedSong) {
        return;
    }
    NSString *cacheID = [self cacheID:searchedSong];
    [_musicSearchCache setValue:searchedSong forKey:cacheID];
}

- (BOOL)shouldMusicSearchedBefore:(RNMusicInfo *)song
{
    if(_musicSearchCache.allKeys.count) {
        return [_musicSearchCache.allKeys containsObject:[self cacheID:song]];
    }
    return NO;
}

- (RNMusicInfo *)getCachedSongByID:(NSString *)cacheID
{
    return [_musicSearchCache valueForKey:cacheID];
}

- (NSString *)cacheID:(RNMusicInfo *)song
{
    return [NSString stringWithFormat:@"%@-%@",song.songID,song.name];
}

@end
