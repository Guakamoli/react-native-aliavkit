//
//  FontService.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/12/13.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_REMAP_MODULE(RNFontService, FontService, RCTEventEmitter)

RCT_EXTERN_METHOD(fetchFontList:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(setFont:(nonnull NSNumber *)fontID
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject);
@end
