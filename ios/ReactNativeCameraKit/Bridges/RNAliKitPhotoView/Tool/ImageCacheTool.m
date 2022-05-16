//
//  ImageCacheTool.m
//  ReactNativeAliAVKit
//
//  Created by ac on 2022/5/12.
//

#import "ImageCacheTool.h"
#import <CommonCrypto/CommonDigest.h>

@implementation ImageCacheTool

+(NSString *)MD5ForUpper32Bate:(NSString *)str
{
    const char* input = [str UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(input, (CC_LONG)strlen(input), result);
    
    NSMutableString *digest = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for (NSInteger i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [digest appendFormat:@"%02X", result[i]];
    }
    return digest;
}

+(NSString *)getImagePathWithName:(NSString *)name
{
    NSString *path_document = NSHomeDirectory();
    NSString *md5Name = [self MD5ForUpper32Bate:name];
    return [path_document stringByAppendingFormat:@"/Documents/photoCache/%@",md5Name];
}

+(NSString *)saveImageToCache:(UIImage *)image name:(NSString *)name
{
    NSString *imagePath = [self getImagePathWithName:name];
    [UIImagePNGRepresentation(image) writeToFile:imagePath atomically:YES];
    return imagePath;
}

+(UIImage *)imageWithName:(NSString *)name
{
    NSString *imagePath = [self getImagePathWithName:name];
    return [UIImage imageWithContentsOfFile:imagePath];
}

+(BOOL)checkImageCache:(NSString *)name
{
    NSString *imagePath = [self getImagePathWithName:name];
    return [[NSFileManager defaultManager]fileExistsAtPath:imagePath];
}
@end
