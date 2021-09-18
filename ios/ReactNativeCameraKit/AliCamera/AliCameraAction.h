//
//  AliCameralController.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/18.
//

#import <UIKit/UIKit.h>
@class AliyunMediaConfig;

@interface AliCameraAction : NSObject

@property (nonatomic, strong) AliyunMediaConfig *mediaConfig;

@property (nonatomic, strong, readonly) UIView *cameraPreview;

+ (AliCameraAction *)action;

/// take still image
- (void)takePhotos:(void (^)(NSData *imageData))handler;
- (void)startFrontPreview;
- (void)startPreview;
- (void)stopPreview;

@end



