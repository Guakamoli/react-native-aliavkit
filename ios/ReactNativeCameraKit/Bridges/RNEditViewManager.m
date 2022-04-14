//
//  RNEditViewManager.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//

#import "RNEditViewManager.h"
#import "RNEditView.h"
#import <AliyunVideoSDKPro/AliyunVodPublishManager.h>

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

RCT_EXPORT_VIEW_PROPERTY(videoPath, NSString)
RCT_EXPORT_VIEW_PROPERTY(imagePath, NSString)
RCT_EXPORT_VIEW_PROPERTY(onExportVideo, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(filterName, NSString)
RCT_EXPORT_VIEW_PROPERTY(startExportVideo, BOOL)
RCT_EXPORT_VIEW_PROPERTY(saveToPhotoLibrary, BOOL)
RCT_EXPORT_VIEW_PROPERTY(videoMute, BOOL)
RCT_EXPORT_VIEW_PROPERTY(musicInfo, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(onPlayProgress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(editStyle, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(mediaInfo, NSDictionary)


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

RCT_EXPORT_METHOD(getTaskPath:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *taskPath = [self.editView getTaskPath];
    resolve(taskPath);
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (void)invalidate
{
    
}

@end
