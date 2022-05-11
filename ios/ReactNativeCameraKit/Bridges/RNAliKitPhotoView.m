//
//  RNAliavkitPhotoView.m
//  AFNetworking
//
//  Created by Mac on 2022/5/11.
//

#import <Foundation/Foundation.h>

#import "RNAliKitPhotoView.h"
#import "RNAliKitPhotoViewManager.h"

@interface RNAliKitPhotoView ()
{
   
}

@property (nonatomic) NSUInteger pageSize;
@property (nonatomic) NSUInteger numColumns;
@property (nonatomic) BOOL multiSelect;
@property (nonatomic) NSUInteger itemWidth;
@property (nonatomic) NSUInteger itemHeight;

@end


@implementation RNAliavkitPhotoView


- (void)setPageSize:(NSUInteger)pageSize
{

}

- (void)setNumColumns:(NSUInteger)numColumns
{

}

- (void)setMultiSelect:(BOOL)videoMute {

}

- (void)setItemWidth:(NSUInteger)itemWidth
{

}

- (void)setItemHeight:(NSUInteger)itemHeight
{

}

/**
 *
 */
- (void)onCurrentPositionUpdate:(NSDictionary*)data selectedIndex:(NSUInteger)selectedIndex {
    //选择触发的回调参数结构示例：
             // data = [{
              //     index:下标：选择的图片/视频数组的顺序,
              //     width:该图片/视频的宽, 视频可能需要根据角度宽高对换
              //     height:该图片/视频的高,
              //     url:文件本地地址
              //     fileSize:文件大小（字节大小）,
              //     filename:文件名称,
              //     type: 文件类型： 格式为 "video/mp4" 或者  "image/jpeg",
              //     playableDuration: 视频时长,图片为0,视频为 ms
              //     rotation: 视频角度，通常手机拍摄的适配，宽高相反，需要根据角度重新设置宽高，（android 有这个问题）
              // }];
    if (self.onSelectedPhotos) {
//      self.onSelectedPhotos(@{ @"selectedIndex":@(0), @"data":[]});
  }
}

@end

