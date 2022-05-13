//
//  AliyunCompositionCell.h
//  AliyunVideo
//
//  Created by Worthy on 2017/3/9.
//  Copyright (C) 2010-2017 Alibaba Group Holding Limited. All rights reserved.
//

#import <UIKit/UIKit.h>


//当前蒙层显示
typedef NS_ENUM(NSInteger,AYPhotoCellStatus){
    AYPhotoCellStatusDefault = 0,       //默认可选
    AYPhotoCellStatusNoEnabled,         //不可编辑状态,灰色蒙层
    AYPhotoCellStatusSelect,            //已选中 白色蒙层
};

//右上角选择框样式
typedef NS_ENUM(NSInteger,AYPhotoSelectStatus){
    AYPhotoSelectStatusDefault = 0,     //默认不显示
    AYPhotoSelectStatusUnchecked,       //显示圆圈,未选中状态
    AYPhotoSelectStatusCheck,           //显示圆圈,勾选状态
    AYPhotoSelectStatusNumber,           //显示圆圈,数字状态
};

//cell右上角取消事件在cell中接收再抛出去
@class AliyunCompositionCell;
@protocol AYCellDelegate<NSObject>
- (void)cell:(AliyunCompositionCell *)cell didSelectItemAtIndexPath:(NSIndexPath *)indexPath;
@end

//相册单个item view
@interface AliyunCompositionCell : UICollectionViewCell
//图片显示
@property (strong, nonatomic) UIImageView *imageView;
//视频显示出时长
@property (strong, nonatomic) NSString *labelDuration;
//非视频隐藏时长label
@property (nonatomic,assign)BOOL hiddenDuration;
//cell显示状态
@property (nonatomic,assign)AYPhotoCellStatus cellStatus;
//cell选择状态
@property (nonatomic,assign)AYPhotoSelectStatus selectStatus;
//只在selectStatus=number才需要,注意下标是从1开始
@property (nonatomic,assign)NSInteger photoIndex;
//当前cell下标
@property (nonatomic,strong)NSIndexPath *indexPath;
//右上角取消事件
@property (nonatomic, weak) id <AYCellDelegate> delegate;
@end
