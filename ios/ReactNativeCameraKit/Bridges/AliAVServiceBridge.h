//
//  FacePasterBridge.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/29.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface AliAVServiceBridge : RCTEventEmitter<RCTBridgeModule>

- (void)startMultiRecording:(CGFloat)duration;


//RCT_EXTERN_METHOD(postCropVideo:(NSString*)videoPath
//                  resolve:(RCTPromiseResolveBlock)resolve
//                  rejecter:(RCTPromiseRejectBlock)reject)

@end
