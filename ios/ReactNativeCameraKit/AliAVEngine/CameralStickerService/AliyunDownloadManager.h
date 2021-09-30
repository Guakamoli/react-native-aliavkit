//
//  AliyunDownloadManager.h
//  AliyunVideo
//
//  Created by Vienta on 2017/1/16.
//  Copyright (C) 2010-2017 Alibaba Group Holding Limited. All rights reserved.
//  下载管理类

#import <Foundation/Foundation.h>

@class AliyunPasterInfo;

typedef void (^TaskProgressBlk_t)(NSProgress *progress);
typedef void (^TaskDownloadCompleteBlk_t)(NSString *filePath, NSError *error);


@interface AliyunDownloadTask : NSObject

/// 正在下载的block回调
@property(nonatomic, copy) TaskProgressBlk_t progressBlock;

/// 下载完成的block回调
@property(nonatomic, copy) TaskDownloadCompleteBlk_t completionHandler;

/// 人脸动图模型
@property(nonatomic, readonly) AliyunPasterInfo *pasterInfo;

/// 初始化方法
/// @param pasterInfo 人脸动图模型
- (instancetype)initWithInfo:(AliyunPasterInfo *)pasterInfo;

@end


@interface AliyunDownloadManager : NSObject

/// 下载管理器中添加任务
/// @param task 任务
- (void)addTask:(AliyunDownloadTask *)task;

/// 下载管理器中添加任务
/// @param task 任务
/// @param progressBlock 正在下载的block回调
/// @param completionHandler 下载完成的block回调
- (void)addTask:(AliyunDownloadTask *)task
       progress:(TaskProgressBlk_t)progressBlock
completionHandler:(TaskDownloadCompleteBlk_t)completionHandler;


@end

