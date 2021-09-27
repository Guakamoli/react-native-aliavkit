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

static NSString *const vp = @"var/mobile/Containers/Data/Application/AC7438D6-5415-4E63-931E-4BB876EF56EB/Documents/com.guakamoli.engine/record/52D68940-0129-4F25-85C3-40BD1970405C/BF406AA1-51ED-4E2A-B83C-42E3842E8053.mp4";

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


- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

@end
