//
//  AlivcShortVideoRaceManager.m
//  AliyunVideoClient_Entrance
//
//  Created by 郦立 on 2019/9/19.
//  Copyright © 2019 Alibaba. All rights reserved.
//

#import "BeautyEngineManager.h"
#import <GLKit/GLKit.h>
#import <queen/Queen.h>
#import "AliyunPathManager.h"

@interface BeautyEngineManager()

@property (nonatomic, assign) CGSize size;
@property (nonatomic, strong) NSLock *lock;
@property (nonatomic, assign) BOOL hasInit;

@property (nonatomic, strong) QueenEngine *beautyEngine;
@property (nonatomic) CVPixelBufferRef newPixelBuffer;
@property (nonatomic, assign) int cameraRotate;
@property (nonatomic, strong) NSThread *processPixelThread;
@property (nonatomic, readwrite) BOOL liteVersion;

@end

static BeautyEngineManager *_instance = nil;

@implementation BeautyEngineManager

+ (BeautyEngineManager *)shareManager
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _instance = [[BeautyEngineManager alloc] init];
        _instance.size = CGSizeZero;
        _instance.lock = [[NSLock alloc]init];
        _instance.hasInit = NO;
        BOOL isPro = [[QueenEngine getVersion] containsString:@"official-pro"];
        _instance.liteVersion = !isPro;
    });
    return _instance;
}

-(void)clearBeautyEngine
{
    self.beautyEngine = nil;
}

- (void)clear
{
    [self.lock lock];
    _instance.hasInit = NO;
    [self.processPixelThread cancel];
    [self performSelector:@selector(clearBeautyEngine) onThread:self.processPixelThread withObject:nil waitUntilDone:YES];
    _newPixelBuffer = NULL;
    [self.lock unlock];
}

- (CVPixelBufferRef)customRenderWithBuffer:(CMSampleBufferRef)sampleBuffer
                                    rotate:(int)cameraRotate
                               skinBuffing:(CGFloat)skinBuffingValue
                             skinWhitening:(CGFloat)skinWhiteningValue
                                   sharpen:(CGFloat)sharpenValue
                                    bigEye:(CGFloat)bigEyeValue
                                  longFace:(CGFloat)longFaceValue
                                   cutFace:(CGFloat)cutFaceValue
                                  thinFace:(CGFloat)thinFaceValue
                                  lowerJaw:(CGFloat)lowerJawValue
                                mouthWidth:(CGFloat)mouthWidthValue
                                  thinNose:(CGFloat)thinNoseValue
                              thinMandible:(CGFloat)thinMandibleValue
                                  cutCheek:(CGFloat)cutCheekValue
{
    [self.lock lock];
    
    if(!self.hasInit) {
        QueenEngineConfigInfo *configInfo = [QueenEngineConfigInfo new];
        // 引擎初始化
        self.beautyEngine = [[QueenEngine alloc] initWithConfigInfo:configInfo];
        self.hasInit = YES;
        
        self.processPixelThread = [[NSThread alloc] initWithTarget:self selector:@selector(initPixelThread) object:nil];
        [self.processPixelThread start];
    }
    
    _cameraRotate = cameraRotate;
    _newPixelBuffer = (CVPixelBufferRef)CMSampleBufferGetImageBuffer(sampleBuffer);
    if(!_newPixelBuffer) {
        [self.lock unlock];
        return 0;
    }
    /**
     1、脸长、下巴、唇宽三项参数设置值时数据取反，设置值时加上负号即可（数值越大，脸越长、下巴越长、唇越宽）
     2.美型参数统一调整为原来的1倍，调节明显
     */
    //基础美颜
    [self basicBeautyWithSkinBuffing:skinBuffingValue
                       skinWhitening:skinWhiteningValue
                             sharpen:sharpenValue];
    //高级美颜
    [self advancedFaceBeauty];
    //美型
    [self faceShapeWithBigEye:bigEyeValue
                     longFace:longFaceValue
                      cutFace:cutFaceValue
                     thinFace:thinFaceValue
                     lowerJaw:lowerJawValue
                   mouthWidth:mouthWidthValue
                     thinNose:thinNoseValue
                 thinMandible:thinMandibleValue
                     cutCheek:cutCheekValue];
    
    [self lutBeauty];
    //美妆
    //    [self faceMakeup];
    //美颜美型渲染
    [self performSelector:@selector(outputSampleBuffer) onThread:self.processPixelThread withObject:nil waitUntilDone:YES];
    
    [self.lock unlock];
    return _newPixelBuffer;
}

#pragma mark - 美颜参数

/// 基础美颜参数项
/// @param skinBuffingValue 设置磨皮系数
/// @param skinWhiteningValue 设置锐化系数
/// @param sharpenValue 设置美白系数
- (void)basicBeautyWithSkinBuffing:(CGFloat)skinBuffingValue
                     skinWhitening:(CGFloat)skinWhiteningValue
                           sharpen:(CGFloat)sharpenValue
{
    // 打开磨皮锐化功能开关 - 基础美颜（主要包括磨皮、锐化）
    [self.beautyEngine setQueenBeautyType:kQueenBeautyTypeSkinBuffing enable:YES];
    // 打开美白功能开关 - 基础美颜（美白）
    [self.beautyEngine setQueenBeautyType:kQueenBeautyTypeSkinWhiting enable:YES];
    // 设置磨皮系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsSkinBuffing value:skinBuffingValue];
    // 设置锐化系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsSharpen value:sharpenValue];
    // 设置美白系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsWhitening value:skinWhiteningValue];
}

/// 高级美颜功能
- (void)advancedFaceBeauty
{
    //脸部美颜（主要包括去眼袋、法令纹、白牙、口红、腮红）
    [self.beautyEngine setQueenBeautyType:kQueenBeautyTypeFaceBuffing enable:YES];
    // 设置去眼袋系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsPouch value:0.7f];
    // 设置去法令纹系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsNasolabialFolds value:0.6f];
    // 设置白牙系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsWhiteTeeth value:0.2f];
    // 设置口红系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsLipstick value:0.3f];
    // 设置腮红系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsBlush value:0.2f];
    // 设置口红色相系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsLipstickColorParam value:0];
    // 设置口红饱和度系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsLipstickGlossParam value:0.1f];
    // 设置口红明度系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsLipstickBrightnessParam value:0];
    // 设置亮眼系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsBrightenEye value:0.25f];
    // 设置红润系数
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsSkinRed value:0.5f];
}

/// 美型功能
- (void)faceShapeWithBigEye:(CGFloat)bigEyeValue
                   longFace:(CGFloat)longFaceValue
                    cutFace:(CGFloat)cutFaceValue
                   thinFace:(CGFloat)thinFaceValue
                   lowerJaw:(CGFloat)lowerJawValue
                 mouthWidth:(CGFloat)mouthWidthValue
                   thinNose:(CGFloat)thinNoseValue
               thinMandible:(CGFloat)thinMandibleValue
                   cutCheek:(CGFloat)cutCheekValue
{
    // 打开美型功能开关 - （主要包括瘦脸、瘦下巴、大眼、瘦鼻、美唇等）
    [self.beautyEngine setQueenBeautyType:kQueenBeautyTypeFaceShape enable:YES];
    // 设置大眼系数
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeBigEye value:bigEyeValue * 3];//大眼
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeLongFace value:longFaceValue * -1.5];//脸长
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeCutFace value:cutFaceValue];//削脸
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeThinFace value:thinFaceValue * 1.5];//瘦脸
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeLowerJaw value:lowerJawValue * -1];//下巴
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeMouthWidth value:mouthWidthValue * -1];//唇宽
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeThinNose value:thinNoseValue * 1];//瘦鼻
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeThinMandible value:thinMandibleValue * 1];//下颌
    [self.beautyEngine setFaceShape:kQueenBeautyFaceShapeTypeCutCheek value:cutCheekValue * 1];//颧骨
}

- (void)lutBeauty
{
    /// 打开滤镜功能开关
    [self.beautyEngine setQueenBeautyType:kQueenBeautyTypeLUT enable:YES];
    NSBundle *currentBundle = [NSBundle bundleForClass:[self class]];
    NSString *lutPath = @"ReactNativeAliAVKit.bundle/lz27.png";
    NSString *fullPath = [[currentBundle resourcePath] stringByAppendingPathComponent:lutPath];
    // 设置滤镜资源，也可以是资源的绝对路径
    [self.beautyEngine setLutImagePath:fullPath];
    // 设置滤镜强度
    [self.beautyEngine setQueenBeautyParams:kQueenBeautyParamsLUT value:0.8f];
}

/// 美妆功能
- (void)faceMakeup
{
    //美妆（主要包括整妆、高光、美瞳、口红、眼妆）
    [self.beautyEngine setQueenBeautyType:kQueenBeautyTypeMakeup enable:YES];
    NSBundle *currentBundle = [NSBundle bundleForClass:[self class]];
    NSString *makeupPath = @"ReactNativeAliAVKit.bundle/蜜桃妆.png";
    NSString *fullPath = [[currentBundle resourcePath] stringByAppendingPathComponent:makeupPath];
    // 设置整妆资源，也可以是资源的绝对路径
    [self.beautyEngine setMakeupWithType:kQueenBeautyMakeupTypeWhole paths:@[fullPath] blendType:kQueenBeautyBlendNormal];
}

-(void)initPixelThread
{
    [[NSRunLoop currentRunLoop] addPort:[[NSPort alloc] init] forMode:NSDefaultRunLoopMode];
    [[NSRunLoop currentRunLoop] run];
}

- (void)outputSampleBuffer
{
    QEPixelBufferData *bufferData = [QEPixelBufferData new];
    bufferData.bufferIn = _newPixelBuffer;
    bufferData.bufferOut = _newPixelBuffer;
    bufferData.inputAngle = _cameraRotate;
    bufferData.outputAngle = _cameraRotate;
    // 对pixelBuffer进行图像处理，输出处理后的buffer
    kQueenResultCode resultCode = [self.beautyEngine processPixelBuffer:bufferData];
    if (resultCode != kQueenResultCodeOK)
    {
        NSLog(@"queen processPixelBuffer error == %i",(int)resultCode);
    }
}

@end
