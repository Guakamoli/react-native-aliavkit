//
//  ShortCut.h
//  Pods
//
//  Created by jimmy on 2021/10/13.
//

#ifndef ShortCut_h
#define ShortCut_h

#ifdef DEBUG
# define DLog(fmt, ...) NSLog((@"--- [method:%s]" "[line:%d]" fmt),__FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
#if TARGET_IPHONE_SIMULATOR
# define DLog(fmt, ...) NSLog((@"--- [method:%s]" "[line:%d]" fmt),__FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
# define DLog(...);
#endif
#endif


static inline BOOL isNotch_IPhone() {
  BOOL iPhoneXSeries = NO;
  if (UIDevice.currentDevice.userInterfaceIdiom != UIUserInterfaceIdiomPhone) {
    return iPhoneXSeries;
  }
  if (@available(iOS 11.0, *)) {
    UIWindow *mainWindow = [[[UIApplication sharedApplication] delegate] window];
    if (mainWindow.safeAreaInsets.bottom > 0.0) {
      iPhoneXSeries = YES;
    }
  }
  return iPhoneXSeries;
}

#define IS_IPHONEX isNotch_IPhone()
#define iPhone8 CGSizeEqualToSize(CGSizeMake(375, 667), [[UIScreen mainScreen] bounds].size)
#define iPhone8p CGSizeEqualToSize(CGSizeMake(414, 736), [[UIScreen mainScreen] bounds].size)

#define kStatusBarHeight [UIApplication sharedApplication].statusBarFrame.size.height
#define ScreenWidth  [UIScreen mainScreen].bounds.size.width
#define ScreenHeight  [UIScreen mainScreen].bounds.size.height

#define ADAPT_FOUNT_SIZE(size) (iPhone8 || IS_IPHONEX) ? size : (iPhone8p ? size + floor(size / 10) : size - floor(size / 10))

#endif /* ShortCut_h */

