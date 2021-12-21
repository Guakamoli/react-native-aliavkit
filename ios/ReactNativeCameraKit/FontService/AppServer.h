//
//  AppServer.h
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/12/13.
//

#import <Foundation/Foundation.h>



@interface AppServer : NSObject


/// get请求
/// @param baseURL baseURL 字符串
/// @param path 请求路径
/// @param parameters 参数
/// @param completionHandler 完成回调
+ (void)getRequestBaseURL:(NSString *)baseURL
                     path:(NSString *)path
               parameters:(NSDictionary *)parameters
        completionHandler:(void (^)(NSURLResponse *response, NSDictionary *responseObject,  NSError * error))completionHandler;

/// get请求
/// @param path 请求路径
/// @param parameters 参数
/// @param completionHandler 完成回调
+ (void)getRequestPath:(NSString *)path
            parameters:(NSDictionary *)parameters
     completionHandler:(void (^)(NSURLResponse *response,
                                 NSDictionary *responseObject,
                                 NSError * error))completionHandler;

/// 下载请求
/// @param remotePath 请求路径
/// @param destination 本地保存路径
/// @param downloadProgressBlock 下载进度回调
/// @param completionHandler 完成回调
+ (void)downloadWithRemotePath:(NSString *)remotePath
             toDestinationPath:(NSString *)destination
                      progress:(void (^)(NSProgress *downloadProgress))downloadProgressBlock
             completionHandler:(void(^)(NSURL *filePath, NSError *error))completionHandler;

@end


