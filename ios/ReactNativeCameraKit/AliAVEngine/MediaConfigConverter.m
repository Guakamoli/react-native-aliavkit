//
//  MediaConfigConverter.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/13.
//

#import "MediaConfigConverter.h"
#import "UIColor+AlivcHelper.h"
#import "AliyunMediaConfig.h"

@implementation AVEngineConfig

- (instancetype)initWithConfig:(AliyunMediaConfig *)config
{
    if (self = [super init]) {
        self.mediaConfig = config;
    }
    return self;
}

@end



@implementation MediaConfigConverter

+ (AVEngineConfig *)convertToConfig:(NSDictionary *)options
{
    AliyunMediaConfig *mConfig = [[AliyunMediaConfig alloc] init];
    mConfig.minDuration = [[options valueForKey:@"minDuration"] floatValue];
    mConfig.maxDuration = [[options valueForKey:@"maxDuration"] floatValue];
    mConfig.gop = [[options valueForKey:@"gop"] intValue];
    mConfig.fps = [[options valueForKey:@"fps"] intValue];
    mConfig.cutMode =
    [[options valueForKey:@"cutMode"] intValue] == 0 ? AliyunMediaCutModeScaleAspectFill : AliyunMediaCutModeScaleAspectCut;
    mConfig.videoOnly = [[options valueForKey:@"videoOnly"] boolValue];
    
    NSString *videoCodec = [options valueForKey:@"videoCodec"];
    mConfig.encodeMode = [videoCodec isEqualToString:@"h264"] ? AliyunEncodeModeHardH264 : AliyunEncodeModeSoftFFmpeg;
    
    NSString *hexStr = [options valueForKey:@"backgroundColor"];
    mConfig.backgroundColor = hexStr ? [UIColor colorWithHexString:hexStr] : UIColor.blackColor;
    mConfig.videoQuality = [self _getVideoQuality:[options valueForKey:@"videoQuality"]];
    mConfig.deleteVideoClipOnExit = [[options valueForKey:@"deleteVideoClipOnExit"] boolValue];
    
    NSDictionary *size = [options objectForKey:@"resolution"];
    CGFloat width = [[size valueForKey:@"width"] floatValue];
    CGFloat height = [[size valueForKey:@"height"] floatValue];
    mConfig.outputSize = CGSizeMake(width, height);
    
    
    mConfig.videoRotate = [[options objectForKey:@"videoRotate"] intValue];
    
    AVEngineConfig *config = [[AVEngineConfig alloc] initWithConfig:mConfig];
    config.bitrate = [[options valueForKey:@"bitrate"] intValue]; //record: 15M edit: 10M
    config.useFaceDetect = [[options valueForKey:@"useFaceDetect"] boolValue];
    config.faceDetectCount = [[options valueForKey:@"faceDetectCount"] intValue];
    config.faceDectectSync = [[options valueForKey:@"faceDectectSync"] boolValue];
    config.beautifyStatus = [[options valueForKey:@"beautifyStatus"] boolValue];
    config.videoFlipH = [[options valueForKey:@"videoFlipH"] boolValue];
    if (width == 1920 && height == 1080) {
        config.frontCaptureSessionPreset = AVCaptureSessionPreset1920x1080;
    } else {
        config.frontCaptureSessionPreset = AVCaptureSessionPreset1280x720;
    }
    return config;
}

+ (AliyunMediaQuality)_getVideoQuality:(NSString *)str
{
    AliyunMediaQuality videoQuality = AliyunMediaQualityHight;
    if ([str isEqualToString:@"VeryHigh"]) {
        videoQuality = AliyunMediaQualityVeryHight;
    }
    if ([str isEqualToString:@"High"]) {
        videoQuality = AliyunMediaQualityHight;
    }
    if ([str isEqualToString:@"Medium"]) {
        videoQuality = AliyunMediaQualityMedium;
    }
    if ([str isEqualToString:@"Low"]) {
        videoQuality = AliyunMediaQualityLow;
    }
    if ([str isEqualToString:@"Poor"]) {
        videoQuality = AliyunMediaQualityPoor;
    }
    if ([str isEqualToString:@"ExtraPoor"]) {
        videoQuality = AliyunMediaQualityExtraPoor;
    }
    return videoQuality;
}

@end
