//
//  RNKitCropImageView.m
//  AFNetworking
//
//  Created by Mac on 2022/6/30.
//

#import <Foundation/Foundation.h>

#import "RNKitCropImageView.h"
#import "RNKitCropImageViewManager.h"
#if __has_include(<React/RCTBridge.h>)
#import <React/RCTComponent.h>
#else
#import "RCTComponent.h"
#endif


@interface RNKitCropImageView ()

//图片输入 uri
@property (nonatomic) NSString* imageUri;
//图片旋转角度 0  90  180  270
@property (nonatomic) NSUInteger angle;
//是否去裁剪图片
@property (nonatomic) BOOL startCrop;
//图片裁剪完成回调
@property (nonatomic, copy) RCTBubblingEventBlock onCropped;

@end

@implementation RNKitCropImageView


- (void)setImageUri:(NSString*)imageUri
{
    _imageUri = imageUri;
}

- (void)setAngle:(NSUInteger)angle
{
    if(_angle != angle){
        _angle = angle;

    }
}

- (void)setStartCrop:(BOOL)startCrop
{
    if(_startCrop != startCrop){
        _startCrop = _startCrop;
        if(startCrop){
            //[self onStartCrop];
        }
    }
}


@end
