//
//  FacePasterBridge.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/29.
//

#import "RNEventEmitter.h"
#import <React/RCTBridge.h>
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>



@interface RNEventEmitter ()
{
    BOOL _hasListeners;
  
}

//@property (nonatomic, strong) AliyunCrop *cutPanel;


@end

@implementation RNEventEmitter

// 标记宏（必要）
RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(enableHapticIfExist)
{

}

+ (id)allocWithZone:(NSZone *)zone {
    static RNEventEmitter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[
        @"addFacePasterListener"
    ];
}

/**
 * 贴纸下载进度回调
 */
- (void)setFacePasterDownloadProgress:(CGFloat)progress  index:(NSNumber *)index{
    [self sendEventWithName:@"addFacePasterListener" body:@{@"progress":@(progress),@"index":index}];
}

@end
