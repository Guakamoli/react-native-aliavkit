//
//  AliyunPathManager.m
//  AliyunVideo
//
//  Created by Worthy on 2017/3/11.
//  Copyright (C) 2010-2017 Alibaba Group Holding Limited. All rights reserved.
//

#import "AliyunPathManager.h"

@implementation AliyunPathManager

#pragma mark - Public

+ (NSString *)aliyunRootPath
{
    return [[self documentPath] stringByAppendingPathComponent:@"com.guakamoli.engine"];
}

+ (NSString *)quCachePath
{
    return [[self cachePath] stringByAppendingPathComponent:@"com.guakamoli.engine"];
}

+ (NSString *)compositionRootDir
{
    return [[self aliyunRootPath] stringByAppendingPathComponent:@"composition"];
}

+ (NSString *)quRelativeRootPath
{
    
    return @"Documents/com.guakamoli.engine";
}

+ (void)clearDir:(NSString *)dirPath
{
    [[NSFileManager defaultManager] removeItemAtPath:dirPath error:nil];
    [[NSFileManager defaultManager] createDirectoryAtPath:dirPath withIntermediateDirectories:YES attributes:nil error:nil];
}

+ (NSError *)makeDirExist:(NSString *)dirPath
{
    NSError *error = nil;
    BOOL isDir = NO;
    if ([NSFileManager.defaultManager fileExistsAtPath:dirPath isDirectory:&isDir] && isDir) {
        return error;
    }
    [NSFileManager.defaultManager createDirectoryAtPath:dirPath withIntermediateDirectories:YES attributes:nil error:&error];
    return error;
}

+ (NSString*)randomString
{
    CFUUIDRef puuid = CFUUIDCreate(nil);
    CFStringRef uuidString = CFUUIDCreateString(nil, puuid);
    NSString * result = (NSString *)CFBridgingRelease(CFStringCreateCopy( NULL, uuidString));
    CFRelease(puuid);
    CFRelease(uuidString);
    return result;
}

+ (NSString *)createRecrodDir
{
    return [[self aliyunRootPath] stringByAppendingPathComponent:@"record"];
}

+ (NSString *)createMagicRecordDir
{
    return [[self aliyunRootPath] stringByAppendingPathComponent:@"magicRecord"];
}

+ (NSString *)createResourceDir
{
    return [[self aliyunRootPath] stringByAppendingPathComponent:@"QPRes"];
}

+ (NSString *)resourceRelativeDir
{
    return [[self quRelativeRootPath] stringByAppendingPathComponent:@"QPRes"];
}

#pragma mark - Private

+ (NSString *)documentPath
{
    return NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
}

+ (NSString *)cachePath
{
    return NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
}

@end
