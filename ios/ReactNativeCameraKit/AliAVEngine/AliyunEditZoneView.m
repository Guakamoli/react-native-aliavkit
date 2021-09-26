//
//  AliyunEditZoneView.m
//  AliyunVideo
//
//  Created by Vienta on 2017/3/8.
//  Copyright (C) 2010-2017 Alibaba Group Holding Limited. All rights reserved.
//

#import "AliyunEditZoneView.h"

@implementation AliyunEditZoneView

- (instancetype)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor clearColor];
        self.clipsToBounds = YES;
        self.editStatus = YES;
    }
    return self;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [super touchesBegan:touches withEvent:event];
    if (!_editStatus) {
        return;
    }
    CGPoint previousPoint = [[touches anyObject] previousLocationInView:self];
    if (self.delegate && [self.delegate respondsToSelector:@selector(currentTouchPoint:)]) {
        [self.delegate currentTouchPoint:previousPoint];
    }
}

- (void)touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [super touchesMoved:touches withEvent:event];
    if (!_editStatus) {
        return;
    }
    UITouch *touch = (UITouch *)[touches anyObject];
    CGPoint previousPoint = [touch previousLocationInView:self];
    CGPoint touchPoint = [touch locationInView:self];
    if (self.delegate && [self.delegate respondsToSelector:@selector(mv:to:)]) {
        [self.delegate mv:previousPoint to:touchPoint];
    }
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [super touchesEnded:touches withEvent:event];
    if (!_editStatus) {
        return;
    }
    if (self.delegate && [self.delegate respondsToSelector:@selector(touchEnd)]) {
        [self.delegate touchEnd];
    }
}

@end
