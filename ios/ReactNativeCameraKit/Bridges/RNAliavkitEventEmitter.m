//
//  RNAliavkitEventEmitter.h
//  Pods
//
//  Created by Mac on 2022/4/20.
//

#ifndef RNAliavkitEventEmitter_h
#define RNAliavkitEventEmitter_h


#endif /* RNAliavkitEventEmitter_h */


#import "RNAliavkitEventEmitter.h"


@interface RNAliavkitEventEmitter ()
{
    BOOL _hasListeners;
}

@end

@implementation RNAliavkitEventEmitter

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"addFacePasterListener"];
}

+ (id)allocWithZone:(NSZone *)zone {
    static RNAliavkitEventEmitter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

/**
 * 贴纸下载进度回调
 */
- (void)setFacePasterDownloadProgress:(CGFloat)progress  index:(NSNumber *)index{
//    [self sendEventWithName:@"addFacePasterListener" body:@{@"progress":@(progress),@"index":index}];
}

@end

