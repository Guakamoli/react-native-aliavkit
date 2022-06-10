//
//  ShortCut.h
//  Pods
//
//  Created by jimmy on 2021/11/2.
//

#ifndef ShortCut_h
#define ShortCut_h


#ifdef DEBUG
# define AVDLog(fmt, ...) NSLog((@"--- [函数名:%s]" "[行号:%d]" fmt),__FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
#if TARGET_IPHONE_SIMULATOR
# define AVDLog(fmt, ...) NSLog((@"--- [函数名:%s]" "[行号:%d]" fmt),__FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
# define AVDLog(...);
#endif
#endif

#define IS_IPHONEX (([[UIScreen mainScreen] bounds].size.height<812)?NO:YES)
#define NoStatusBarSafeTop (IS_IPHONEX ? 44 : 0)
#define ScreenWidth  [UIScreen mainScreen].bounds.size.width
#define ScreenHeight  [UIScreen mainScreen].bounds.size.height

// --
#define IS_REVOCHAT ([NSBundle.mainBundle.bundleIdentifier isEqualToString:@"co.goduck.revochat.ios"])

#endif /* ShortCut_h */
