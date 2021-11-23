//
//  AVCaptionModel.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/11/23.
//

#import "AVCaptionModel.h"
#import "UIColor+AlivcHelper.h"

@implementation AVCaptionModel

- (instancetype)initWithCaptionInfo:(NSDictionary *)captionInfo
{
    if (self = [super init]) {
        self.text = [captionInfo valueForKey:@"text"];
        
        self.startTime = [[captionInfo valueForKey:@"startTime"] floatValue];
        self.duration = [[captionInfo valueForKey:@"duration"] floatValue];
        
        NSDictionary *center = [captionInfo valueForKey:@"center"];
        CGFloat centerX = [[center valueForKey:@"x"] floatValue];
        CGFloat centerY = [[center valueForKey:@"y"] floatValue];
        self.center = CGPointMake(centerX, centerY);
        
        self.radians = [[captionInfo valueForKey:@"rotate"] floatValue];
        self.scale = [[captionInfo valueForKey:@"scale"] floatValue];
        
        NSString *fontName = [captionInfo valueForKey:@"fontName"];
        self.fontName = fontName ? : @"PingFangSC-Regular"; //默认系统字体
        
        NSString *fontStyle = [captionInfo valueForKey:@"fontStyle"];
        if ([fontStyle isEqualToString:@"italic"]) {
            self.fontStyle = AliyunCaptionStickerFaceTypeItatic;
        }
        else if ([fontStyle isEqualToString:@"bold"]) {
            self.fontStyle = AliyunCaptionStickerFaceTypeBold;
        }
        else {
            self.fontStyle = AliyunCaptionStickerFaceTypeNormal;
        }
        
        NSString *hexColor = [captionInfo valueForKey:@"color"];
        UIColor *forgroundColor = [UIColor blackColor];
        if (hexColor) {
            forgroundColor = [UIColor colorWithHexString:hexColor];
        }
        self.textColor = forgroundColor;
        
        NSString *hexbgColor = [captionInfo valueForKey:@"backgroundColor"];
        UIColor *backgroundColor = [UIColor clearColor];
        if (hexbgColor) {
            backgroundColor = [UIColor colorWithHexString:hexbgColor];
        }
        self.backgroundColor = backgroundColor;
        
        NSString *textAlignment = [captionInfo valueForKey:@"textAlignment"];
        if ([textAlignment isEqualToString:@"right"]) {
            self.textAlignment = AliyunCaptionStickerTextAlignmentRight;
        }
        else if ([textAlignment isEqualToString:@"center"]) {
            self.textAlignment = AliyunCaptionStickerTextAlignmentCenter;
        }
        else {
            self.textAlignment = AliyunCaptionStickerTextAlignmentLeft;
        }
    }
    return self;
}

@end
