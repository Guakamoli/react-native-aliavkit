@import AVFoundation;

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTViewManager.h>
#import <React/RCTConvert.h>
#import <React/RCTUIManager.h>
#else
#import "RCTViewManager.h"
#import "RCTConvert.h"
#endif

@interface CKCameraManager : RCTViewManager

@end
