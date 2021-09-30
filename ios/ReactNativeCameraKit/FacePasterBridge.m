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
            NSMutableArray *arr = [NSMutableArray array];
            NSArray *pastList = responseObject[@"data"];
            NSString *filterName = [NSString stringWithFormat:@"Face_Sticker/hanfumei-800"];
            NSString *path = [[NSBundle mainBundle] pathForResource:filterName ofType:nil];
            NSString *lastComponent = [path lastPathComponent];
            NSArray *comp = [lastComponent componentsSeparatedByString:@"-"];
            NSDictionary *localPaster = @{
                @"name":comp.firstObject,
                @"id":@([comp.lastObject integerValue]),
                @"icon":[path stringByAppendingPathComponent:@"icon.png"],
                @"type":@2,
                @"bundlePath":path
            };
            [arr addObject:localPaster];
            [arr addObjectsFromArray:pastList];
            resolve(arr);
        }
    }];
}

@end
