//
//  RNAliavkitEventEmitter.m
//  ReactNativeAliAVKit
//
//  Created by Mac on 2022/4/20.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RNAliavkitEventEmitter : RCTEventEmitter<RCTBridgeModule>

+ (id)allocWithZone:(NSZone *)zone;
- (void)setFacePasterDownloadProgress:(CGFloat)progress  index:(NSNumber *)index;
- (void)setExportWaterMarkVideoProgress:(CGFloat)progress;

@end
