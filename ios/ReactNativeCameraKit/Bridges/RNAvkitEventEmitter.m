
#import "RNAvkitEventEmitter.h"


@interface RNAvkitEventEmitter ()
{
    BOOL _hasListeners;
}

@end

@implementation RNAvkitEventEmitter

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"addFacePasterListener"];
}

+ (id)allocWithZone:(NSZone *)zone {
    static RNAvkitEventEmitter *sharedInstance = nil;
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
    [self sendEventWithName:@"addFacePasterListener" body:@{@"progress":@(progress),@"index":index}];
}

@end
