//
//  RNAVDeviceHelper.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/11/2.
//

#import "RNAVDeviceHelper.h"
#import <sys/utsname.h>

@implementation RNAVDeviceHelper

+ (CGSize)maxVideoSize
{
    CGSize size = CGSizeZero;
    if ([self isBelowIphone_5]){
        size = CGSizeMake(720, 960);
    } else if ([self isBelowIphone_6]){
        size = CGSizeMake(1080, 1080);
    } else {
        size = CGSizeMake(1080, 1920); // Default for other devices
    }
    return size;
}

+ (int)deviceCode
{
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString *phoneType = [NSString stringWithCString: systemInfo.machine encoding:NSASCIIStringEncoding];
    //模拟器兼容
    if(phoneType.length <= 6){
        return 404;
    }
    NSRange range = [phoneType rangeOfString:@","];
    NSRange range1 = NSMakeRange(6, range.location - 6);
    int code;
    @try {
        NSString *subStr = [phoneType substringWithRange:range1];
        code = [subStr intValue];
    } @catch (NSException *exception) {
        code = 0;
    }
    return code;
}

+ (BOOL)isBelowIphone_5
{
    int code = [self deviceCode];
    return code && code < 5;
}

+ (BOOL)isBelowIphone_6
{
    int code = [self deviceCode];
    return code && code < 7;
}

+ (BOOL)isBelowIphone_11
{
    int code = [self deviceCode];
    return code && code < 12;
}


@end
