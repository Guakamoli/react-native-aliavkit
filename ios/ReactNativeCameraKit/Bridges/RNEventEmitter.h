//
//  RNEditViewManager.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//
#import <React/RCTViewManager.h>
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RNEventEmitter : RCTEventEmitter<RCTBridgeModule>

+ (id)allocWithZone:(NSZone *)zone;
- (void)setFacePasterDownloadProgress:(CGFloat)progress  index:(NSNumber *)index;

@end


