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
RCT_EXPORT_VIEW_PROPERTY(onExportVideo, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(filterName, NSString)
RCT_EXPORT_VIEW_PROPERTY(startExportVideo, BOOL)
RCT_EXPORT_VIEW_PROPERTY(saveToPhotoLibrary, BOOL)

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

@end
