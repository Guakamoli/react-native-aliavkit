//
//  MediaConfigConverter.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/13.
//

#import <Foundation/Foundation.h>
@class AliyunMediaConfig;

NS_ASSUME_NONNULL_BEGIN

@interface AVEngineConfig : NSObject

@property (nonatomic, strong) AliyunMediaConfig *mediaConfig;

/// 输出视频码率 bps  最小值200000bps 最大值20000000bps
@property (nonatomic, assign) int bitrate;

/// 是否开启人脸识别
///使用自带人脸识别，开启该功能，系统会在检测到有人脸动图加入时自动进行追踪显示
@property(nonatomic, assign) BOOL useFaceDetect;

///最大是3个 最小是1个  如果不需要检测人脸 使用:useFaceDetect = NO
/// 设置识别人脸的个数 当设置值小于1时，默认为1；当设置值大于3时，默认为3
@property(nonatomic, assign) int faceDetectCount;

/**
 是否同步贴合人脸

 同步贴合人脸动图会在同步线程执行，优点是贴合性强，缺点是性能差的设备会有卡顿现象
 非同步贴合人脸动图，有点是画面流畅但贴图贴合性不强
 默认是YES，6及以下机型建议异步，6以上建议同步
 */
@property(nonatomic, assign) BOOL faceDectectSync;

/// 美颜状态是否开启
@property(nonatomic, assign) BOOL beautifyStatus;

/// 录制的视频是否左右翻转
@property(nonatomic, assign) BOOL videoFlipH;

/**
 设置前置摄像头是否支持调整变焦，后置摄像头默认支持变焦，前置摄像头默认不支持变焦
 当supportVideoZoomFactorForFrontCamera = YES时，前置摄像头支持变焦
 */
@property(nonatomic, assign) BOOL frontCameraSupportVideoZoomFactor;

/**
 前置摄像头采集分辨率

 默认:AVCaptureSessionPreset640x480  更多参数参见：AVCaptureSession.h
 */
@property(nonatomic, copy) NSString *frontCaptureSessionPreset;

@end


@interface MediaConfigConverter : NSObject

+ (AVEngineConfig *)convertToConfig:(NSDictionary *)options;

@end

NS_ASSUME_NONNULL_END
