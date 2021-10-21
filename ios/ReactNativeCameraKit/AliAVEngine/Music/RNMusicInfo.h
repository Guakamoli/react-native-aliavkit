//
//  RNMusicInfo.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/21.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNMusicInfo : NSObject

@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *songID;
@property (nonatomic, copy) NSString *localPath;
@property (nonatomic, copy) NSString *cover;
@property (nonatomic, copy) NSString *artist;
@property (nonatomic, copy) NSString *url;

@property (nonatomic) BOOL isDBContain;

///开始时间 - 裁剪的时候的记录 单位是毫秒ms
@property (nonatomic, assign) NSInteger startTime;

/// 时长
@property (nonatomic, assign) CGFloat duration;

/// 音量大小
@property (nonatomic, assign) float volume;

- (instancetype)initWithDictionary:(NSDictionary *)dictionary;
- (NSDictionary *)convertModelToInfo;

@end

NS_ASSUME_NONNULL_END
