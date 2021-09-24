//
//  RNEditViewManager.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//

#import "RNEditViewManager.h"
#import "RNMovieView.h"

@interface RNEditViewManager ()

@property (nonatomic, strong) RNMovieView *movieView;

@end

@implementation RNEditViewManager
RCT_EXPORT_MODULE()

- (UIView *)view
{
    if (!self.movieView) {
        self.movieView = [[RNMovieView alloc] initWithManager:self bridge:self.bridge];
    }
    return self.movieView;
}

@end
