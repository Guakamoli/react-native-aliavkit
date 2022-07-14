

#import "RNKitWatermarkManager.h"
#import <AVFoundation/AVFoundation.h>
#import <React/RCTBridge.h>

#import "AVAsset+VideoInfo.h"

#import "AliyunPathManager.h"
#import <AliyunVideoSDKPro/AliyunImporter.h>
#import <AliyunVideoSDKPro/AliyunNativeParser.h>
#import <AliyunVideoSDKPro/AliyunEditor.h>

#import <AssetsLibrary/AssetsLibrary.h>


#import "RNAliavkitEventEmitter.h"


#define ScreenWidth  [UIScreen mainScreen].bounds.size.width
#define ScreenHeight  [UIScreen mainScreen].bounds.size.height



@interface RNKitWatermarkManager ()<AliyunIExporterCallback>
{
    RCTPromiseResolveBlock exportWaterMarkResolve;
    RNAliavkitEventEmitter *eventEmitter;
    id<AliyunIExporter> mAliyunIExporter;
}
 
@property(nonatomic, strong) AliyunEditor *mAliyunEditor;

@end

@implementation RNKitWatermarkManager

RCT_EXPORT_MODULE()


- (void)exporterDidEnd:(NSString *)outputPath {
    // 导出结束回调
    [eventEmitter setExportWaterMarkVideoProgress:1.0];

    if(exportWaterMarkResolve){
        exportWaterMarkResolve(outputPath);
        exportWaterMarkResolve = nil;
    }
}

- (void)exporterDidCancel {
    // 导出取消回调
}

- (void)exportProgress:(float)progress {
    // 导出进度回调
    [eventEmitter setExportWaterMarkVideoProgress:progress];
}

- (void)exportError:(int)errorCode {
    // 导出错误回调
}



RCT_EXPORT_METHOD(cancelExportWaterMarkVideo:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    if(mAliyunIExporter != nil){
        [mAliyunIExporter cancelExport];
        mAliyunIExporter = nil;
    }
    if(exportWaterMarkResolve != nil){
        exportWaterMarkResolve(@{});
        exportWaterMarkResolve = nil;
    }
    
    resolve(@(TRUE));
}


RCT_EXPORT_METHOD(exportWaterMarkVideo:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *videoPath = [options objectForKey:@"videoPath"];
    
    if (!videoPath || [videoPath isEqualToString:@""]) {
        reject(@"exportWaterMarkVideo", @"Video path is empty", nil);
        return;
    }
    
    exportWaterMarkResolve = resolve;
    eventEmitter = [RNAliavkitEventEmitter allocWithZone: nil];
    
    NSString *watermarkImagePath = [options objectForKey:@"watermarkImagePath"];
    
    NSString *watermarkText = [options objectForKey:@"watermarkText"];
    
//    bool isDeleteVideo = [options objectForKey:@"isDeleteVideo"];
    
       
    AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:videoPath]];
    CGSize size = [asset avAssetNaturalSize];
    CGFloat frameWidth = size.width + 1.0f;
    CGFloat frameHeight = size.height + 1.0f;
    
    AliyunNativeParser *nativeParser = [[AliyunNativeParser alloc] initWithPath:videoPath];
    NSInteger bitRate = nativeParser.getVideoBitrate;

    AliyunVideoParam *param = [self videoImporter:videoPath size:size bitRate:bitRate];
   
    
    // 获取导出控制器
    mAliyunIExporter = [self.mAliyunEditor getExporter];
    
    [mAliyunIExporter setVideoParam:param];
    
    CGFloat scale= frameWidth/1080.0;
    if(watermarkImagePath == nil || watermarkImagePath == NULL || [watermarkImagePath isKindOfClass:[NSNull class]] || watermarkImagePath.length == 0){
        //Logo 的宽高
        CGFloat watermarkLogoWidth = 45;
        CGFloat watermarkLogoHeight = 66;
        //获取文本宽度
        CGSize textSize = [watermarkText sizeWithAttributes:@{NSFontAttributeName:[UIFont systemFontOfSize:45.0f]}];
        CGFloat textWidth = textSize.width + 1.0;
        CGFloat textHeight = textSize.height+1.0;
        // logo 和 文本 的间隔
        CGFloat intervalWidth = 24.0f;
    
        NSString *watermarkPath = [self getWaterMarkImage:watermarkText
                                    scale:scale
                                    watermarkLogoWidth:watermarkLogoWidth
                                    watermarkLogoHeight:watermarkLogoHeight
                                    textWidth:textWidth
                                    textHeight:textHeight
                                    intervalWidth:intervalWidth
        ];
        
        AliyunEffectImage *watermark = [[AliyunEffectImage alloc] initWithFile:watermarkPath];
        CGFloat watermarkWidth = (watermarkLogoWidth+intervalWidth+textWidth)*scale;
        CGFloat watermarkHeight = watermarkLogoHeight*scale;
        
        watermark.frame = CGRectMake((frameWidth-watermarkWidth)/2, frameHeight-20-watermarkHeight, watermarkWidth, watermarkHeight);
        //设置输出视频水印
        [mAliyunIExporter setWaterMark:watermark];
    }else{
        CGSize imageSize = [UIImage imageNamed:watermarkImagePath].size;
        CGFloat imageWidth = imageSize.width;
        CGFloat imageHeight = imageSize.height;
        
        CGFloat watermarkLogoHeight = 66;
        
        CGFloat watermarkWidth = imageWidth*watermarkLogoHeight/imageHeight*scale;
        CGFloat watermarkHeight = watermarkLogoHeight*scale;
        
        //水印
        AliyunEffectImage *watermark = [[AliyunEffectImage alloc] initWithFile:watermarkImagePath];
        watermark.frame = CGRectMake((frameWidth-watermarkWidth)/2, frameHeight-20-watermarkHeight, watermarkWidth, watermarkHeight);
        //设置输出视频水印
        [mAliyunIExporter setWaterMark:watermark];
    }
    
    // 设置导出状态回调
    self.mAliyunEditor.exporterCallback = self;
    
     NSString *outputPath = [[[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
    //开始导出
    [mAliyunIExporter startExport:outputPath];
}


- (AliyunVideoParam *)videoImporter:(NSString *)videoPath size:(CGSize)size bitRate:(NSInteger)bitRate
{
    //0. 指定配置文件夹路径和输出视频分辨率
    NSString *taskPath = [[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]];
    //1. 创建实例
    AliyunImporter *importer = [[AliyunImporter alloc] initWithPath:taskPath outputSize:size];

    //2.1 添加视频
    AliyunClip *videoClip = [[AliyunClip alloc] initWithVideoPath:videoPath animDuration:0];
    [importer addMediaClip:videoClip];
    
    //3. 设置输出参数
    AliyunVideoParam *param = [[AliyunVideoParam alloc] init];
    param.fps = 30; // 帧率
    param.gop = 30; // 关键帧间隔
    param.bitrate = (int)bitRate; // 码率
    param.videoQuality = AliyunVideoQualityHight; // 视频质量
    param.scaleMode = AliyunScaleModeFill; // 缩放模式
    param.codecType = AliyunVideoCodecHardware; // 编码模式
    [importer setVideoParam:param];
    
    //4. 生成视频源配置文件
    [importer generateProjectConfigure];
    
    //5.创建editor
    self.mAliyunEditor = [[AliyunEditor alloc] initWithPath:taskPath preview:NULL];

    return param;
}


//图片与文字合成水印：
-(NSString *)getWaterMarkImage:(NSString *)text
  scale:(CGFloat)scale
  watermarkLogoWidth:(CGFloat)watermarkLogoWidth
  watermarkLogoHeight:(CGFloat)watermarkLogoHeight
  textWidth:(CGFloat)textWidth
  textHeight:(CGFloat)textHeight
  intervalWidth:(CGFloat)intervalWidth
{
    //获取 logo 图片
    NSString *watermarkPath = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:[NSString stringWithFormat:@"AliKitPhotoView/ic_water_mark_logo.png"]];
    UIImage *logoImage = [UIImage imageNamed:watermarkPath];
    
    //画布创建上下文
    UIGraphicsBeginImageContext(CGSizeMake(watermarkLogoWidth + intervalWidth + textWidth, watermarkLogoHeight));
    
    //先把 logo 画到上下文中
    [logoImage drawInRect:CGRectMake(0, 0, watermarkLogoWidth, watermarkLogoHeight)];
    
    //画文字到上下文中
    NSDictionary *attributes = @{ NSFontAttributeName:[UIFont systemFontOfSize:45.0], NSForegroundColorAttributeName:[UIColor whiteColor]};
    [text drawInRect:CGRectMake(watermarkLogoWidth + intervalWidth, (watermarkLogoHeight-textHeight)/2 - 3.0f, watermarkLogoWidth + intervalWidth + textWidth, watermarkLogoHeight) withAttributes:attributes];
    
    //从当前上下文中获得最终图片
    UIImage *outPutImage = UIGraphicsGetImageFromCurrentImageContext();
    //关闭上下文
    UIGraphicsEndImageContext();
    
    //保存图片到沙盒
    NSString *path = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
    NSString *filePath = [path stringByAppendingPathComponent:@"revo_watermark.png"];
    [UIImagePNGRepresentation(outPutImage) writeToFile:filePath atomically:YES];
    return  filePath;
}

@end
