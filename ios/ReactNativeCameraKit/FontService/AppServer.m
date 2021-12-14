//
//  AppServer.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/12/13.
//

#import "AppServer.h"
#import <AliyunVideoSDKPro/AliyunVideoSDKPro.h>

static NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";

@implementation AppServer


+ (void)getRequestPath:(NSString *)path parameters:(NSDictionary *)parameters
completionHandler:(void (^)(NSURLResponse *response, NSDictionary *responseObject,  NSError * error))completionHandler
{
    AliyunHttpClient *httpClient = [[AliyunHttpClient alloc] initWithBaseUrl:kAlivcQuUrlString];
    [httpClient GET:path parameters:parameters completionHandler:completionHandler];
}

+ (void)downloadWithRemotePath:(NSString *)remotePath
             toDestinationPath:(NSString *)destination
                      progress:(void (^)(NSProgress *downloadProgress))downloadProgressBlock
             completionHandler:(void(^)(NSURL *filePath, NSError *error))completionHandler
{
    AliyunHttpClient *httpClient = [[AliyunHttpClient alloc] initWithBaseUrl:kAlivcQuUrlString];
    [httpClient download:remotePath destination:destination progress:downloadProgressBlock completionHandler:completionHandler];
}

@end
