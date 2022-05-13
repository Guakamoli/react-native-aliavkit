//
//  AliyunCompositionCell.m
//  AliyunVideo
//
//  Created by Worthy on 2017/3/9.
//  Copyright (C) 2010-2017 Alibaba Group Holding Limited. All rights reserved.
//

#import "AliyunCompositionCell.h"


//蓝湖上375的pt需要转换为实际可用的尺寸
#define pt375(pt) (pt * [UIScreen mainScreen].bounds.size.width/375)

@interface AliyunCompositionCell()
//主显示照片
@property (strong, nonatomic) UIImageView *photoView;
//阴影层
@property (nonatomic,strong)UIView *shadowView;
//视频时长label
@property (strong, nonatomic)UILabel *timeLabel;
//多选状态下显示选择框
@property (nonatomic,strong)UIView *selectView;
//多选模式下,视频选中勾选image
@property (nonatomic,strong)UIImageView *checkImageView;
//多选模式下,照片选中显示下标
@property (nonatomic,strong)UILabel *numberLabel;
@end

@implementation AliyunCompositionCell

-(instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setup];
    }
    return self;
}

-(instancetype)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)setup
{
    //基础图像层
    self.photoView = [[UIImageView alloc] initWithFrame:self.bounds];
    self.photoView.contentMode = UIViewContentModeScaleAspectFill;
    self.photoView.clipsToBounds = YES;
    [self addSubview:self.photoView];
    
    
    self.shadowView = [[UIView alloc]initWithFrame:self.bounds];
    [self addSubview:self.shadowView];
    
    self.timeLabel = [[UILabel alloc] init];
    self.timeLabel.textAlignment = NSTextAlignmentRight;
    [self addSubview:self.timeLabel];
    
    
    
    UIButton *selectView = [UIButton new];
    selectView.layer.cornerRadius = pt375(20/2);
    selectView.clipsToBounds = YES;
    selectView.layer.borderWidth = pt375((1.1));
    selectView.layer.borderColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1.0].CGColor;
    selectView.backgroundColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:0.3];
    [selectView addTarget:self action:@selector(selectPhotoEvent:) forControlEvents:(UIControlEventTouchUpInside)];
    [self addSubview:self.selectView = selectView];

    {
        //使用本地pod关联的图片资源不能直接拿取
        NSString *lutName = [NSString stringWithFormat:@"AliKitPhotoView/ic_record_complete.png"];
        NSString *fullPath = [[NSBundle mainBundle] pathForResource:lutName ofType:nil];
        UIImageView *checkImageView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:fullPath]];
        checkImageView.userInteractionEnabled = NO;
        [selectView addSubview:self.checkImageView = checkImageView];

        UILabel *numberLabel = [UILabel new];
        numberLabel.numberOfLines = 0;
        numberLabel.textColor = [UIColor whiteColor];
        numberLabel.font = [UIFont systemFontOfSize:pt375(13)];
        numberLabel.textAlignment = NSTextAlignmentCenter;
        numberLabel.backgroundColor=[UIColor colorWithRed:131/255.0 green:107/255.0 blue:255/255.0 alpha:1.0];
        numberLabel.userInteractionEnabled = NO;
        [selectView addSubview:self.numberLabel = numberLabel];
    }
}

-(void)layoutSubviews {
    [super layoutSubviews];
    self.photoView.frame = self.bounds;
    CGFloat laeblGap = pt375(7);
    CGFloat labelHeight = pt375(17);
    self.timeLabel.frame = CGRectMake(laeblGap, CGRectGetHeight(self.frame) - labelHeight - laeblGap, CGRectGetWidth(self.frame)-laeblGap*2, labelHeight);
    CGFloat selectViewGap = pt375(5);
    CGFloat selectViewWidth = pt375(20);
    self.selectView.frame = CGRectMake(CGRectGetWidth(self.frame) - selectViewWidth - selectViewGap, selectViewGap, selectViewWidth, selectViewWidth);
    self.checkImageView.frame = CGRectInset(self.selectView.bounds, pt375(-1.1), pt375(-1.1));
    self.numberLabel.frame = self.checkImageView.frame;
}

-(void)setCellStatus:(AYPhotoCellStatus)cellStatus
{
    self.userInteractionEnabled = YES;
    self.selectView.alpha = 1;
    switch (cellStatus) {
        //默认可选
        case AYPhotoCellStatusDefault:
            self.shadowView.backgroundColor = [UIColor clearColor];
            break;
        //不可编辑状态,灰色蒙层
        case AYPhotoCellStatusNoEnabled:
            self.shadowView.backgroundColor = [[UIColor blackColor]colorWithAlphaComponent:.7];
            self.userInteractionEnabled = NO;
            self.selectView.alpha = 0;
            break;
        //已选中 白色蒙层
        case AYPhotoCellStatusSelect:
            self.shadowView.backgroundColor = [[UIColor whiteColor]colorWithAlphaComponent:.5];
            break;
        default:
            break;
    }
}
-(void)setSelectStatus:(AYPhotoSelectStatus)selectStatus{
    self.selectView.hidden = NO;
    self.checkImageView.hidden = YES;
    self.numberLabel.hidden = YES;
    switch (selectStatus) {
        case AYPhotoSelectStatusDefault://默认不显示
            self.selectView.hidden = YES;
            break;
        case AYPhotoSelectStatusUnchecked://显示圆圈,未选中状态
            break;
        case AYPhotoSelectStatusCheck://显示圆圈,勾选状态
            self.checkImageView.hidden = NO;
            break;
        case AYPhotoSelectStatusNumber://显示圆圈,数字状态
            self.numberLabel.hidden = NO;
            break;
        default:
            break;
    }
}
-(void)setPhotoIndex:(NSInteger)photoIndex
{
    self.numberLabel.text = [NSString stringWithFormat:@"%@",@(photoIndex)];
}
-(void)setHiddenDuration:(BOOL)hiddenDuration
{
    self.timeLabel.hidden = hiddenDuration;
}
-(void)setLabelDuration:(NSString *)labelDuration
{
    if(labelDuration.length == 0)
    {
        self.timeLabel.text = @"";
        return;
    }
    NSShadow *shadow = [[NSShadow alloc] init];
    shadow.shadowBlurRadius = 4;
    shadow.shadowColor = [UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:0.5];
    shadow.shadowOffset =CGSizeMake(0,2);
    UIFont *labelFont = [UIFont fontWithName:@"PingFangSC" size: pt375(12)];
    labelFont = labelFont ?: [UIFont systemFontOfSize:pt375(12)];
    NSMutableAttributedString *string = [[NSMutableAttributedString alloc] initWithString:labelDuration attributes: @{NSFontAttributeName: labelFont,NSForegroundColorAttributeName: [UIColor whiteColor], NSShadowAttributeName: shadow}];
    self.timeLabel.attributedText = string;
}

//右上角点击效果,只为多选模式下照片可以取消
-(void)selectPhotoEvent:(UIButton *)button
{
    if(self.delegate && [self.delegate respondsToSelector:@selector(cell:didSelectItemAtIndexPath:)]){
        [self.delegate performSelector:@selector(cell:didSelectItemAtIndexPath:) withObject:self withObject:self.indexPath];
    }
}
-(void)setPhotoImage:(UIImage *)photoImage
{
    self.photoView.image = photoImage;
}
@end
