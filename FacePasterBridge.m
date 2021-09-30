//
//  FacePasterBridge.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/29.
//

#import "FacePasterBridge.h"
#import <React/RCTBridge.h>
#import <AliyunVideoSDKPro/AliyunHttpClient.h>

static NSString * const kAlivcQuUrlString =  @"https://alivc-demo.aliyuncs.com";

@interface FacePasterBridge ()

@end

@implementation FacePasterBridge

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(getPasterInfos:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    AliyunHttpClient *httpClient = [[AliyunHttpClient alloc] initWithBaseUrl:kAlivcQuUrlString];
    NSDictionary *param = @{@"type":@(1)};
    [httpClient GET:@"resource/getFrontPasterList"
         parameters:param
  completionHandler:^(NSURLResponse *response, id responseObject, NSError *error) {
        if (error) {
            reject(@"fetch remote paster fail", error.localizedDescription, nil);
        } else {
            NSArray *pastList = responseObject[@"data"];
//            NSMutableArray *arr = [NSMutableArray array];
//            NSString *filterName = [NSString stringWithFormat:@"Face_Sticker/hanfumei-800"];
//            NSString *path = [[NSBundle mainBundle] pathForResource:filterName ofType:nil];
//            AliyunPasterInfo *localPaster = [[AliyunPasterInfo alloc] initWithBundleFile:path];
//            [arr addObject:localPaster];
//            [arr addObjectsFromArray:pastList];
//            onSuccess(arr);
            resolve(pastList);
        }
    }];
}

@end
