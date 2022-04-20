
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RNAvkitEventEmitter : RCTEventEmitter<RCTBridgeModule>

+ (id)allocWithZone:(NSZone *)zone;
- (void)setFacePasterDownloadProgress:(CGFloat)progress  index:(NSNumber *)index;

@end
