//
//  RNEditViewManager.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//

#import "RNEditViewManager.h"
#import "RNEditView.h"

@interface RNEditViewManager ()

@property (nonatomic, strong) RNEditView *editView;

@end

@implementation RNEditViewManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
    if (!self.editView) {
        self.editView = [[RNEditView alloc] initWithManager:self bridge:self.bridge];
    }
    return self.editView;
}

RCT_EXPORT_VIEW_PROPERTY(videoPath, NSString)
RCT_EXPORT_VIEW_PROPERTY(imagePath, NSString)
RCT_EXPORT_VIEW_PROPERTY(onExportVideo, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(filterName, NSString)
RCT_EXPORT_VIEW_PROPERTY(startExportVideo, BOOL)
RCT_EXPORT_VIEW_PROPERTY(saveToPhotoLibrary, BOOL)
RCT_EXPORT_VIEW_PROPERTY(videoMute, BOOL)

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

RCT_EXPORT_METHOD(seekToTime:(NSNumber *)numberTime)
{
    CGFloat time = [numberTime floatValue];
    [self.editView seekToTime:time];
}

RCT_EXPORT_METHOD(trimVideo:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    CGFloat startTime = [[options valueForKey:@"startTime"] floatValue];
    CGFloat endTime = [[options valueForKey:@"endTime"] floatValue];
    [self.editView trimVideoFromTime:startTime toTime:endTime];
}

RCT_EXPORT_METHOD(generateImages:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [self.editView generateImages:options handler:^(NSArray *path) {
        resolve(path);
    }];
}

RCT_EXPORT_METHOD(removeThumbnaiImages:(NSDictionary*)options)
{
    [self.editView removeImages];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}


RCT_EXPORT_METHOD(getFilterIcons:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSArray *names = @[@"柔柔",@"优雅",@"红润",@"阳光",@"海蓝",@"炽黄",@"浓烈",@"闪耀",@"朝阳",@"经典",@"粉桃",@"雪梨",@"鲜果",@"麦茶",@"灰白",@"波普",@"光圈",@"海盐",@"黑白",@"胶片",@"焦黄",@"蓝调",@"迷糊",@"思念",@"素描",@"鱼眼",@"马赛克",@"模糊"];
    NSMutableArray *infos = [NSMutableArray array];
    for (NSString *name in names) {
        NSString *iconPath = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:[NSString stringWithFormat:@"Filter/%@/icon.png",name]];
        [infos addObject:@{@"iconPath":iconPath,@"filterName":name}];
    }
    NSLog(@"------filterInfos： %@",infos);
    resolve(infos);
    
}

@end
