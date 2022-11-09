//
//  RNAliKitPhotoView.h
//  Pods
//
//  Created by Mac on 2022/5/11.
//


#import <UIKit/UIKit.h>

//与RN交互参数不需要写在接口文件
//交互属性不需要声明,只在manager写对应的宏
@interface RNAliKitPhotoView : UIView

- (void)uncheckPhoto:(NSInteger)index;

@end
