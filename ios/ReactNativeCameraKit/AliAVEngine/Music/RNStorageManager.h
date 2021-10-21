//
//  RNStorageManager.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/21.
//

#import <Foundation/Foundation.h>
@class RNMusicInfo;

NS_ASSUME_NONNULL_BEGIN

@interface RNStorageManager : NSObject

+ (instancetype)shared;

- (NSArray *)getPageDataWithPage:(NSUInteger)page inArray:(NSArray *)array;

- (RNMusicInfo *)findMusicByID:(NSString *)songID inArray:(NSArray<RNMusicInfo *> *)array;

- (NSArray<RNMusicInfo *> *)findMusicByName:(NSString *)name inArray:(NSArray<RNMusicInfo *> *)array;

- (void)saveSearchedSongInMemoryCache:(RNMusicInfo *)searchedSong;

- (BOOL)shouldMusicSearchedBefore:(RNMusicInfo *)song;

- (RNMusicInfo *)getCachedSongByID:(NSString *)cacheID;

- (NSString *)cacheID:(RNMusicInfo *)song;

@end

NS_ASSUME_NONNULL_END
