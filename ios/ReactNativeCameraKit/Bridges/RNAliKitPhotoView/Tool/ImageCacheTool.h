//
//  ImageCacheTool.h
//  ReactNativeAliAVKit
//
//  Created by ac on 2022/5/12.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

//图片缓存处理工具
@interface ImageCacheTool : NSObject

//image保存到本地缓存中
+(NSString *)saveImageToCache:(UIImage *)image name:(NSString *)name;

//根据图片名称获取缓存文件
+(UIImage *)imageWithName:(NSString *)name;

//图片名称是否有对应的缓存文件
+(BOOL)checkImageCache:(NSString *)name;
@end

NS_ASSUME_NONNULL_END
