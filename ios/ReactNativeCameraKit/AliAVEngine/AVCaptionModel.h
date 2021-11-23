//
//  AVCaptionModel.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/11/23.
//

#import <Foundation/Foundation.h>
@import AliyunVideoSDKPro;

NS_ASSUME_NONNULL_BEGIN

@interface AVCaptionModel : NSObject

@property (nonatomic, copy) NSString *text;
@property (nonatomic) CGFloat startTime;
@property (nonatomic) CGFloat duration;
@property (nonatomic) CGPoint center;
@property (nonatomic) CGFloat radians;
@property (nonatomic) CGFloat scale;
@property (nonatomic, copy) NSString *fontName;
@property (nonatomic) AliyunCaptionStickerFaceType fontStyle;
@property (nonatomic, strong) UIColor *textColor;
@property (nonatomic, strong) UIColor *backgroundColor;
@property (nonatomic) AliyunCaptionStickerTextAlignment textAlignment;

- (instancetype)initWithCaptionInfo:(NSDictionary *)captionInfo;

@end

NS_ASSUME_NONNULL_END
