//
//  RNMovieView.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//

#import <UIKit/UIKit.h>
#import <React/RCTView.h>
@class RNEditViewManager;
@class RCTBridge;
@class AliyunMediaConfig;

@interface RNEditView : UIView
///视频配置参数
@property (nonatomic, strong) AliyunMediaConfig *mediaConfig;
///多个资源的本地存放文件夹路径 - 从相册选择界面进入传这个值
@property (nonatomic, strong) NSString *taskPath;
///单个视频的本地路径 - 录制进入编辑传这个值
@property (nonatomic, strong) NSString *videoPath;
@property (nonatomic, copy) RCTBubblingEventBlock onExportVideo;


- (instancetype)initWithManager:(RNEditViewManager *)manager bridge:(RCTBridge *)bridge;

/// 尝试播放视频
- (void)play;
/// 尝试继续播放视频
- (void)resume;
/// 重新播放
- (void)replay;
/// 尝试暂停视频
- (void)pause;
- (void)stop;

- (int)seekToTime:(CGFloat)time;
- (void)trimVideoFromTime:(CGFloat)startTime toTime:(CGFloat)endTime;

- (void)generateImages:(NSDictionary *)options handler:(void(^)(NSArray *))complete;
- (void)removeImages;
@end


