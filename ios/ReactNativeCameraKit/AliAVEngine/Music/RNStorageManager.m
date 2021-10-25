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

- (NSArray<RNMusicInfo *> *)getPageDataWithPage:(NSUInteger)page
                                       pageSize:(NSUInteger)pageSize
                                        inArray:(NSArray<RNMusicInfo *> *)array
{
    NSUInteger num = array.count % page;
    if (array.count <= pageSize) {
        return array;
    }
    else if (num != 0) {
        if (page * pageSize > array.count) {
            NSUInteger index = array.count / pageSize;
            NSUInteger length = array.count - index * pageSize;
            return [array subarrayWithRange:NSMakeRange(index * pageSize, length)];
        }
    }
    NSArray *tmpArray = [array subarrayWithRange:NSMakeRange(0, pageSize * page)];
    [tmpArray enumerateObjectsUsingBlock:^(RNMusicInfo *  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj isDBContain];
    }];
    return tmpArray;
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
    if (song) {
        [song isDBContain];
    }
    
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
