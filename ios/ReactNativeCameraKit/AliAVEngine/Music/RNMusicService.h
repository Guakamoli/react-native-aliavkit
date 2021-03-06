//
//  RNMusicService.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/19.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
NS_ASSUME_NONNULL_BEGIN

@interface RNMusicService : NSObject<RCTBridgeModule>


+ (void)downloadMusicWithSongID:(NSString *)songID
                         urlStr:(NSString *)urlStr
                       complete:(void(^)(BOOL, NSString *))complete;

@end

NS_ASSUME_NONNULL_END
