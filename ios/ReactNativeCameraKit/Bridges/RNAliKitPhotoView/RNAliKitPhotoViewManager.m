//
//  RNAliKitPhotoViewManager.m
//  AFNetworking
//
//  Created by Mac on 2022/5/11.
//

#import <Foundation/Foundation.h>

#import "RNAliKitPhotoViewManager.h"
#import "RNAliKitPhotoView.h"

@interface RNAliKitPhotoViewManager ()

@property (nonatomic, weak) RNAliKitPhotoView *photoView;

@end

@implementation RNAliKitPhotoViewManager

RCT_EXPORT_MODULE();


- (UIView *)view
{
    //相册不属于公有组件,每次都创建新对象关联到RN中
    RNAliKitPhotoView *view = [RNAliKitPhotoView new];
    return self.photoView = view;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

//RCT_EXPORT_VIEW_PROPERTY用于给RNAliKitPhotoView设置参数

// pageSize: 分页加载数量 默认40张
RCT_EXPORT_VIEW_PROPERTY(pageSize, NSInteger)
// numColumns: 列数:默认4
RCT_EXPORT_VIEW_PROPERTY(numColumns, NSInteger)
// multiSelect: 是否多选  默认：false
RCT_EXPORT_VIEW_PROPERTY(multiSelect, BOOL)
// keepSelected: 单选时是否保持选择结果，不自动清除 默认：false
RCT_EXPORT_VIEW_PROPERTY(keepSelected, BOOL)
//照片最多可选择数量
RCT_EXPORT_VIEW_PROPERTY(maxSelectCount, NSInteger)
//默认选中下标
RCT_EXPORT_VIEW_PROPERTY(defaultSelectedPosition, NSInteger)
//默认选中数据
RCT_EXPORT_VIEW_PROPERTY(defaultSelectedStatus, BOOL)
// itemWidth: 相册一张图片的宽度（为0则不设置，默认是：屏幕宽度 / 列数）。post用不到，后续 story 相册，UI宽高不一样会用到
RCT_EXPORT_VIEW_PROPERTY(itemWidth, CGFloat)
// itemHeight: 同itemWidth
RCT_EXPORT_VIEW_PROPERTY(itemHeight, CGFloat)

//相册显示类型 sortMode: "all" "video" "photo"
RCT_EXPORT_VIEW_PROPERTY(sortMode, NSString*)

//onRecordingProgress:()=>(selectedIndex,selectedData)
// selectedIndex： 当前选中的 图片/视频 数组下标，单选模式固定返回0
// selectedData：  选择的图片、视频的数组，单选模式其中只有一条数据，多选模式中视频也应该只有一条数据
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
RCT_EXPORT_VIEW_PROPERTY(onSelectedPhotoCallback, RCTBubblingEventBlock)
//触发最大照片数量后的回调
RCT_EXPORT_VIEW_PROPERTY(onMaxSelectCountCallback, RCTBubblingEventBlock)
//error回调
RCT_EXPORT_VIEW_PROPERTY(onErrorCallback, RCTBubblingEventBlock)
//返回第一个相册数据
RCT_EXPORT_VIEW_PROPERTY(onGetFirstPhotoCallback, RCTBubblingEventBlock)


/**
 * 取消照片选中
 * 
 */
RCT_EXPORT_METHOD(uncheckPhoto:(NSDictionary*)options
                       resolve:(RCTPromiseResolveBlock)resolve
                        reject:(RCTPromiseRejectBlock)reject)
{
    //  (NSDictionary*)options
    // {
    //     index:下标：选择的图片/视频数组的顺序,
    //     width:该图片/视频的宽, 视频可能需要根据角度宽高对换
    //     height:该图片/视频的高,
    //     url:文件本地地址
    //     fileSize:文件大小（字节大小）,
    //     filename:文件名称,
    //     type: 文件类型： 格式为 "video/mp4" 或者  "image/jpeg",
    //     playableDuration: 视频时长,图片为0,视频为 ms
    //     rotation: 视频角度
    // }
    RNAliKitPhotoView *view = self.photoView;
    if (!view) {
        reject(@"", @"no photoview found", nil);
        return;
    }
    
    NSNumber *index = options[@"index"];
    @try {
        [view uncheckPhoto:[index integerValue]];
        resolve(nil);
    }
    @catch (NSException *exception) {
        reject(exception.name, exception.reason, nil);
    }
}

@end
