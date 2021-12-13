//
//  RNEditViewManager.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//

#import "RNEditViewManager.h"
#import "RNEditView.h"

@interface RNEditViewManager ()<RCTInvalidating>

@property (nonatomic, strong) RNEditView *editView;

@end

@implementation RNEditViewManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
    self.editView = [[RNEditView alloc] initWithBridge:self.bridge];
    return self.editView;
}

RCT_EXPORT_VIEW_PROPERTY(videoPath, NSString)         //视频路径
RCT_EXPORT_VIEW_PROPERTY(imagePath, NSString)         //图片路径
RCT_EXPORT_VIEW_PROPERTY(filterName, NSString)     //设置滤镜（）
RCT_EXPORT_VIEW_PROPERTY(startExportVideo, BOOL)    //是否准备导出视频文件
RCT_EXPORT_VIEW_PROPERTY(onExportVideo, RCTBubblingEventBlock) //导出视频的进度回调
RCT_EXPORT_VIEW_PROPERTY(saveToPhotoLibrary, BOOL)  //是否保存到相册
RCT_EXPORT_VIEW_PROPERTY(videoMute, BOOL)          //是否视频静音
/*
 音乐信息设置
 {
    "localPath":""
 }
 */
RCT_EXPORT_VIEW_PROPERTY(musicInfo, NSDictionary)

//播放进度回调
RCT_EXPORT_VIEW_PROPERTY(onPlayProgress, RCTBubblingEventBlock)

/*
 编辑视图宽高信息
 {
    "width":0.0,
    "height":0.0
 }
 */
RCT_EXPORT_VIEW_PROPERTY(editStyle, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(mediaInfo, NSDictionary)


/*
 文字信息
 
 {
   "text": "",
   fontName: string;
   fontStyle: string; // normal | italic | bold
   color: string;      //文字颜色
   textAlignment: string; //left, center, right
   backgroundColor: string; //背景颜色
   "startTime": 0.0,
   "duration": 10.0,
   "center": { "x": 0.0, "y": 0.0 },
   "rotate": 0.0,
   "scale": 1.0
 }
 */
RCT_EXPORT_VIEW_PROPERTY(captionInfo, NSDictionary)



RCT_EXPORT_METHOD(play)
{
    [self.editView play];
}

RCT_EXPORT_METHOD(resume)
{
    [self.editView resume];
}

RCT_EXPORT_METHOD(replay)
{
    [self.editView replay];
}

RCT_EXPORT_METHOD(pause)
{
    [self.editView pause];
}

RCT_EXPORT_METHOD(stop)
{
    [self.editView stop];
}

RCT_EXPORT_METHOD(seekToTime:(nonnull NSNumber *)numberTime)
{
    CGFloat time = [numberTime floatValue];
    [self.editView seekToTime:time];
}

RCT_EXPORT_METHOD(trimVideo:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    CGFloat startTime = [[options objectForKey:@"startTime"] floatValue];
    CGFloat endTime = [[options objectForKey:@"endTime"] floatValue];
    [self.editView trimVideoFromTime:startTime toTime:endTime];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (void)invalidate
{
    
}

@end
