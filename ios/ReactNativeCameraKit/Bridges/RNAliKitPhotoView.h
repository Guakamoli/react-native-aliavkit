//
//  RNAliKitPhotoView.h
//  Pods
//
//  Created by Mac on 2022/5/11.
//

#ifndef RNAliKitPhotoView_h
#define RNAliKitPhotoView_h


#endif /* RNAliKitPhotoView_h */



#import <UIKit/UIKit.h>
#import <React/RCTView.h>
#import "RNAliKitPhotoViewManager.h"

@class RNAliKitPhotoViewManager;
@class RCTBridge;


@interface RNAliavkitPhotoView : UIView


@property (nonatomic, copy) RCTDirectEventBlock onSelectedPhotos;

@end
