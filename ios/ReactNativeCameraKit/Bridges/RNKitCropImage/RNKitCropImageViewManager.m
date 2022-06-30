//
//  RNKitCropImageViewManager.m
//  AFNetworking
//
//  Created by Mac on 2022/6/30.
//

#import <Foundation/Foundation.h>

#import "RNKitCropImageViewManager.h"
#import "RNKitCropImageView.h"

@interface RNAliKitPhotoViewManager ()

@property (nonatomic, weak) RNKitCropImageView *mCropImageView;

@end

@implementation RNAliKitPhotoViewManager

RCT_EXPORT_MODULE();

- (UIView *)view
{
    //相册不属于公有组件,每次都创建新对象关联到RN中
    RNKitCropImageView *view = [RNKitCropImageView new];
    return self.mCropImageView = view;
}

//图片输入 uri
RCT_EXPORT_VIEW_PROPERTY(imageUri, NSString*)

//图片旋转角度 0  90  180  270
RCT_EXPORT_VIEW_PROPERTY(angle, NSUInteger)

//是否去裁剪图片
RCT_EXPORT_VIEW_PROPERTY(startCrop, BOOL)

//图片裁剪完成回调
RCT_EXPORT_VIEW_PROPERTY(onCropped, RCTBubblingEventBlock)


@end
