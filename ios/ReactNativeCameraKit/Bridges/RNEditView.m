//
//  RNMovieView.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//

#import "RNEditView.h"

#import <AliyunVideoSDKPro/AliyunImporter.h>
#import <AliyunVideoSDKPro/AliyunEditor.h>
#import <AliyunVideoSDKPro/AliyunPasterManager.h>
#import <AliyunVideoSDKPro/AliyunErrorCode.h>
#import <AliyunVideoSDKPro/AliyunVodPublishManager.h>

#import "RNEditViewManager.h"
#import "AliyunMediaConfig.h"
#import "AliyunEffectPrestoreManager.h"
#import "AliyunPathManager.h"
#import "AliyunEditZoneView.h"
#import "AliyunEffectFilterInfo.h"
#import "AliyunDBHelper.h"
#import "AliyunEffectMvGroup.h"
#import "AliyunEffectResourceModel.h"
#import <Photos/Photos.h>
#import "AliAssetImageGenerator.h"
#import "AliyunTimelineMediaInfo.h"
#import "AliyunCompositionInfo.h"
#import "AliyunAlbumModel.h"
#import "AliyunPhotoLibraryManager.h"

@interface RNEditView()<
AliyunIPlayerCallback,
AliyunIRenderCallback,
AliyunEditZoneViewDelegate,
AliyunIExporterCallback
>
{
    BOOL _prePlaying;
}
@property(nonatomic, assign) CGSize inputOutputSize;
@property(nonatomic, assign) CGSize outputSize;

@property(nonatomic, strong) AliyunPasterManager *pasterManager;
@property(nonatomic, strong) AliyunEditZoneView *editZoneView;
@property(nonatomic, strong) AliyunEditor *editor;
@property(nonatomic, strong) id<AliyunIPlayer> player;
@property(nonatomic, strong) id<AliyunIClipConstructor> clipConstructor;

@property (nonatomic, weak) RNEditViewManager *manager;
@property (nonatomic, weak) RCTBridge *bridge;

@property (nonatomic, strong) UIView *preview;

@property (nonatomic, strong) AliyunVodPublishManager *publishManager;

@property (nonatomic, strong) NSString *filterName;
@property (nonatomic) BOOL startExportVideo;
@property (nonatomic) BOOL saveToPhotoLibrary;
@property (nonatomic, strong) AliAssetImageGenerator *generator;

@property (nonatomic) BOOL videoMute;
@property (nonatomic, strong) NSString *imagePath;
@property (nonatomic, strong) AliyunCompositionInfo *compositionInfo;
@end

@implementation RNEditView

- (void)dealloc
{
    [_editor stopEdit];
    [self.generator cancel];
}

- (AliyunMediaConfig *)mediaConfig
{
    if (!_mediaConfig) {//默认配置
        _mediaConfig = [AliyunMediaConfig defaultConfig];
        _mediaConfig.minDuration = 2.0f;
        _mediaConfig.maxDuration = 15.f;
        _mediaConfig.gop = 30;
        _mediaConfig.cutMode = AliyunMediaCutModeScaleAspectFill;
        _mediaConfig.videoOnly = YES;
        _mediaConfig.backgroundColor = [UIColor blackColor];
        _mediaConfig.videoQuality = AliyunMediaQualityVeryHight;
    }
    return _mediaConfig;
}

- (AliyunVodPublishManager *)publishManager{
    if (!_publishManager) {
        _publishManager =[[AliyunVodPublishManager alloc]init];
        _publishManager.exportCallback = self;
    }
    return _publishManager;
}

- (AliAssetImageGenerator *)generator {
    if (!_generator) {
        _generator = [AliAssetImageGenerator new];
    }
    return _generator;
}

- (instancetype)initWithManager:(RNEditViewManager*)manager bridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        self.manager = manager;
        self.bridge = bridge;
        self.backgroundColor = [UIColor blackColor];
//        self.imagePath = @"";
    }
    return self;
}

///设置初始值
- (void)initBaseData
{
    AliyunEffectPrestoreManager *pm = [[AliyunEffectPrestoreManager alloc] init];
    [pm insertInitialData];
    
    // 校验视频分辨率，如果首段视频是横屏录制，则outputSize的width和height互换
    self.inputOutputSize = self.mediaConfig.outputSize;
    self.outputSize = [self.mediaConfig fixedSize];
    self.mediaConfig.outputSize = _outputSize;
    
    [self _setVideoTaskPath];
    
    //防size异常奔溃处理
    if (_outputSize.height == 0 || _outputSize.width == 0) {
        _outputSize.width = 720;
        _outputSize.height = 1280;
        NSAssert(false, @"调试的时候崩溃,_outputSize分辨率异常处理");
    }
}

- (void)_setPhotoTaskPath:(NSString *)photoPath
{
    NSString *editDir = [AliyunPathManager compositionRootDir];
    NSString *taskPath = [editDir stringByAppendingPathComponent:[AliyunPathManager randomString]];
    
    AliyunImporter *importor = [[AliyunImporter alloc] initWithPath:taskPath outputSize:CGSizeMake(1080, 1920)];
    AliyunClip *clip = [[AliyunClip alloc] initWithImagePath:photoPath duration:3.0 animDuration:0];
    [importor addMediaClip:clip];
    
    // set video param
    AliyunVideoParam *param = [[AliyunVideoParam alloc] init];
    param.fps = self.mediaConfig.fps;
    param.gop = self.mediaConfig.gop;
    param.bitrate = 15*1000*1000;
    param.scaleMode = AliyunScaleModeFill;
    param.codecType = AliyunVideoCodecHardware;
    [importor setVideoParam:param];
    
    // generate config
    [importor generateProjectConfigure];
    // output path
    self.mediaConfig.outputPath = [[taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
    self.taskPath = taskPath;
}

/// 单视频接入编辑页面，生成一个新的taskPath
- (void)_setVideoTaskPath {
    if (_taskPath) {
        return;
    }
    _taskPath = [[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]];
    
    AliyunImporter *importer =[[AliyunImporter alloc] initWithPath:self.taskPath outputSize:self.outputSize];
    AliyunVideoParam *param = [[AliyunVideoParam alloc] init];
    param.fps = self.mediaConfig.fps;
    param.gop = self.mediaConfig.gop;
    if (self.mediaConfig.videoQuality == AliyunMediaQualityVeryHight) {
        param.bitrate = 10*1000*1000; // 10Mbps
    } else {
        param.videoQuality = (AliyunVideoQuality)self.mediaConfig.videoQuality;
    }
    if (self.mediaConfig.cutMode == AliyunMediaCutModeScaleAspectCut) {
        param.scaleMode = AliyunScaleModeFit;
    } else {
        param.scaleMode = AliyunScaleModeFill;
    }
    // 编码模式
    if (self.mediaConfig.encodeMode ==  AliyunEncodeModeHardH264) {
        param.codecType = AliyunVideoCodecHardware;
    }else if(self.mediaConfig.encodeMode == AliyunEncodeModeSoftFFmpeg) {
        param.codecType = AliyunVideoCodecOpenh264;
    }
    
    [importer setVideoParam:param];
    AliyunClip *clip = [[AliyunClip alloc] initWithVideoPath:_videoPath animDuration:0];
    [importer addMediaClip:clip];
    [importer generateProjectConfigure];
//    NSLog(@"----------clip.duration:%f",clip.duration);
    self.mediaConfig.outputPath = [[_taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
}

- (UIView *)preview
{
    if (!_preview) {
        CGFloat factor = _outputSize.height / _outputSize.width;
        CGRect frame;
        frame.size.width = [UIScreen mainScreen].bounds.size.width;
        frame.size.height = [UIScreen mainScreen].bounds.size.width * factor;
        _preview = [[UIView alloc] initWithFrame:frame];
        _preview.backgroundColor = [UIColor lightGrayColor];
    }
    return _preview;
}

/// 初始化sdk相关
- (void)initSDKAbout
{
    // editor
    self.editor = [[AliyunEditor alloc] initWithPath:self.taskPath preview:self.preview];
    
    self.editor.playerCallback =  (id)self;
    self.editor.renderCallback =  (id)self;
    // player
    self.player = [self.editor getPlayer];
    
    // constructor
    self.clipConstructor = [self.editor getClipConstructor];
    
    // setup pasterEditZoneView
    self.editZoneView = [[AliyunEditZoneView alloc] initWithFrame:self.preview.bounds];
    self.editZoneView.delegate = (id)self;
    [self.preview addSubview:self.editZoneView];
    
    // setup pasterManager
    self.pasterManager = [self.editor getPasterManager];
    self.pasterManager.displaySize = self.editZoneView.bounds.size;
    self.pasterManager.outputSize = _outputSize;
    self.pasterManager.previewRenderSize = [self.editor getPreviewRenderSize];
    self.pasterManager.delegate = (id)self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
}


- (void)prepareForExport
{
    [self.player stop];
    [self.editor stopEdit];
    
    int result = [self.publishManager exportWithTaskPath:self.taskPath outputPath:self.mediaConfig.outputPath];
    if (result != 0) {
        NSLog(@"合成失败");
    }
}

- (void)setImagePath:(NSString *)imagePath
{
    if (_imagePath != imagePath) {
        _imagePath = imagePath;
        if (imagePath && ![imagePath isEqualToString:@""]) {
            if ([imagePath containsString:@"file://"]) { //in case path contains scheme
                _imagePath = [NSURL URLWithString:imagePath].path;
            }
            [self _setPhotoTaskPath:_imagePath];
            [self initEditorSDK];
        } else {
            //**For test only**
            NSString * photoPath = [[NSUserDefaults standardUserDefaults] objectForKey:@"photoPath"];
            if (photoPath) {
                [self _setPhotoTaskPath:photoPath];
                _imagePath = photoPath;
            }
            [self initEditorSDK];
        }
        NSLog(@"------imagePath：%@",_imagePath);
    }
}

- (void)setVideoPath:(NSString *)videoPath
{
    if (_videoPath != videoPath) {
        _videoPath = videoPath;
        
        if (videoPath && ![videoPath isEqualToString:@""]) {
            if ([videoPath containsString:@"file://"]) { //in case path contains scheme
                _videoPath = [NSURL URLWithString:videoPath].path;
            }
            [self initEditorSDK];
        } else {
            //**For test only**
//            NSString * videoSavePath = [[NSUserDefaults standardUserDefaults] objectForKey:@"videoSavePath"];
//            _videoPath = videoSavePath;
//            [self initEditorSDK];
        }
        NSLog(@"------videoPath：%@",_videoPath);
    }
}

- (void)initEditorSDK
{
    [self initBaseData];
    [self addSubview:self.preview];
    [self initSDKAbout];
    
    [self.editor startEdit];
    [self play];
}

- (void)setOnExportVideo:(RCTBubblingEventBlock)onExportVideo
{
    if (_onExportVideo != onExportVideo) {
        _onExportVideo = onExportVideo;
    }
}

- (void)setFilterName:(NSString *)filterName
{
    if (_filterName != filterName) {
        _filterName = filterName;
        [self changeFilterByName:filterName];
    }
}

- (void)setStartExportVideo:(BOOL)startExportVideo
{
    if (_startExportVideo != startExportVideo) {
        _startExportVideo = startExportVideo;
        if (startExportVideo) {
            [self prepareForExport];
        }
    }
}

- (void)setSaveToPhotoLibrary:(BOOL)saveToPhotoLibrary
{
    if (_saveToPhotoLibrary != saveToPhotoLibrary) {
        _saveToPhotoLibrary = saveToPhotoLibrary;
    }
}

- (void)setVideoMute:(BOOL)videoMute {
    if (_videoMute != videoMute) {
        _videoMute = videoMute;
        [self.editor setMute:videoMute];
    }
}

- (void)requestAuthorization:(void(^)(void))completion
{
    PHAuthorizationStatus authStatus = [PHPhotoLibrary authorizationStatus];
    if (authStatus == AVAuthorizationStatusNotDetermined) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (status == PHAuthorizationStatusAuthorized) {
                    completion();
                }
            });
        }];
    }else if (authStatus == PHAuthorizationStatusAuthorized) {
        completion();
    }
}

- (void)saveResourceType:(PHAssetResourceType)type withPath:(NSString *)path
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self requestAuthorization:^{
            [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
                PHAssetCreationRequest *cr = [PHAssetCreationRequest creationRequestForAsset];
                [cr addResourceWithType:type fileURL:[NSURL fileURLWithPath:path] options:nil];
            } completionHandler:^(BOOL success, NSError * _Nullable error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (success) {
                        NSLog(@"保存%@成功!", type == PHAssetResourceTypeVideo ? @"视频" : @"图片");
                    } else {
                        NSLog(@"保存%@失败:%@", type == PHAssetResourceTypeVideo ? @"视频" : @"图片", error);
                    }
                });
            }];
        }];
    });
}

- (void)saveVideoWithPath:(NSString *)videoPath
{
    [self requestAuthorization:^{
        [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
            PHAssetCreationRequest *cr = [PHAssetCreationRequest creationRequestForAsset];
            [cr addResourceWithType:PHAssetResourceTypeVideo fileURL:[NSURL fileURLWithPath:videoPath] options:nil];
        } completionHandler:^(BOOL success, NSError * _Nullable error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (success) {
                    NSLog(@"保存视频成功!");
                } else {
                    NSLog(@"保存视频失败:%@", error);
                }
            });
        }];
    }];
}

#pragma mark - AliyunIPlayerCallback --播放器回调
///播放结束
- (void)playerDidEnd
{
    NSLog(@"--- %s",__PRETTY_FUNCTION__);
    [self replay];
}

- (void)playProgress:(double)playSec streamProgress:(double)streamSec
{
    
}

- (void)changeFilterByName:(NSString *)filterName
{
    [self.editor removeFilter];
    AliyunEffectFilterInfo *info = [self getFilterInfoByName:filterName];
    NSString *filePath = [info localFilterResourcePath];
    AliyunEffectFilter *effectFilter = [[AliyunEffectFilter alloc] initWithFile:filePath];
    [self.editor applyFilter:effectFilter];
}

- (AliyunEffectFilterInfo *)getFilterInfoByName:(NSString *)name
{
    AliyunEffectFilterInfo *info = [[AliyunEffectFilterInfo alloc] init];
    info.name = name;
    info.resourcePath = [NSString stringWithFormat:@"Filter/%@",name];
    info.icon = @"icon";
    return info;
}

- (void)playError:(int)errorCode
{
    NSLog(@"--- %s:  %d",__PRETTY_FUNCTION__,errorCode);
}

#pragma mark - AliyunIRenderCallback
- (int)customRender:(int)srcTexture size:(CGSize)size
{
    return srcTexture;
}

#pragma mark - AliyunIExporterCallback -合成导出回调

///导出结束
- (void)exporterDidEnd:(NSString *)outputPath
{
    __block NSString *path = outputPath;
    if (_saveToPhotoLibrary) {
        if (self.imagePath) {
            __weak typeof(self) weakSelf = self;
            [self _generateImageFromVideoPath:outputPath
                                  itemPerTime:1000
                                    startTime:0
                                     duration:3.0
                          generatorOutputSize:CGSizeMake(1080, 1920)
                                     complete:^(NSArray * imagePaths) {
                path = imagePaths.firstObject;
                [weakSelf saveResourceType:PHAssetResourceTypePhoto withPath:path];
            }];
        } else if(self.videoPath) {
            [self saveResourceType:PHAssetResourceTypeVideo withPath:path];
        }
    }
    id event = @{@"exportProgress": @(1.0), @"outputPath":path};
    _onExportVideo(event);
}

///导出取消
- (void)exporterDidCancel
{
    
}

///导出进度
- (void)exportProgress:(float)progress
{
    id event = @{@"exportProgress": @(progress)};
    _onExportVideo(event);
}

- (void)exportError:(int)errorCode
{
    
}

#pragma mark - AliyunEditZoneViewDelegate
- (void)currentTouchPoint:(CGPoint)point
{
    
}

/// 接收到移动触摸事件的代理方法
/// @param fp 起点
/// @param tp 终点
- (void)mv:(CGPoint)fp to:(CGPoint)tp
{
    
}

/// 触摸事件结束
- (void)touchEnd
{
    
}

/// 尝试播放视频
- (void)play
{
    if (self.player.isPlaying) {
        NSLog(@"短视频编辑播放器测试:当前播放器正在播放状态,不调用play");
    } else {
        int returnValue = [self.player play];
        NSLog(@"短视频编辑播放器测试:调用了play接口");
        if (returnValue == 0) {
            NSLog(@"短视频编辑播放器测试:play返回0成功");
        } else {
            switch (returnValue) {
                case ALIVC_COMMON_INVALID_STATE: //-4
                    NSLog(@"------播放失败： 状态错误");
                    break;
                default:
                    break;
            }
        }
        [self updateUIAndDataWhenPlayStatusChanged];
    }
}

/// 尝试继续播放视频
- (void)resume
{
    if (self.player.isPlaying) {
        NSLog(@"短视频编辑播放器测试:当前播放器正在播放状态,不调用resume");
    } else {
        int returnValue = [self.player resume];
        NSLog(@"短视频编辑播放器测试:调用了resume接口");
        if (returnValue == 0) {
            //            [self forceFinishLastEditPasterView];
            NSLog(@"短视频编辑播放器测试:resume返回0成功");
        } else {
            [self.player play];
            NSLog(@"短视频编辑播放器测试:！！！！继续播放错误,错误码:%d",returnValue);
        }
    }
    [self updateUIAndDataWhenPlayStatusChanged];
}

/// 重新播放
- (void)replay
{
    [self.player replay];
    [self updateUIAndDataWhenPlayStatusChanged];
}

/// 尝试暂停视频
- (void)pause
{
    if (self.player.isPlaying) {
        int returnValue = [self.player pause];
        NSLog(@"短视频编辑播放器测试:调用了pause接口");
        if (returnValue == 0) {
            NSLog(@"短视频编辑播放器测试:pause返回0成功");
        } else {
            NSLog(@"短视频编辑播放器测试:！！！！暂停错误,错误码:%d", returnValue);
        }
    } else {
        NSLog(@"短视频编辑播放器测试:当前播放器不是播放状态,不调用pause");
    }
    [self updateUIAndDataWhenPlayStatusChanged];
}

/// 更新UI 当状态改变的时候，播放的状态下是暂停按钮，其余都是播放按钮
- (void)updateUIAndDataWhenPlayStatusChanged
{
    if (self.player.isPlaying) {
        _prePlaying = YES;
    } else {
        _prePlaying = NO;
    }
}

- (int)seekToTime:(CGFloat)time
{
    return [self.player seek:time];
}

- (void)trimVideoFromTime:(CGFloat)startTime toTime:(CGFloat)endTime
{
    [self.editor stopEdit];
    if (self.clipConstructor.mediaClips.count != 1) {return;}
    AliyunClip *clip = [self.clipConstructor mediaClipAtIndex:0];
    if (clip.mediaType != AliyunClipVideo) { return; }
    if (startTime) {
        clip.startTime = startTime;
    }
    if (endTime && clip.duration > endTime) {
        CGFloat newD = endTime - startTime;
        clip.duration = newD;
    }
    // 3.23版本以下可以不用调用这句--很重要
    [[self.editor getClipConstructor] updateMediaClip:clip atIndex:0];
    [self.editor startEdit];
    [self play];
}

static NSString * ThumnailDirectory() {
    return [NSString stringWithFormat:@"%@/Documents/thumbNail", NSHomeDirectory()];
}

- (void)_generateImageFromVideoPath:(NSString *)videoPath
                        itemPerTime:(NSInteger)itemPerTime
                          startTime:(CGFloat)startTime
                           duration:(CGFloat)duration
                generatorOutputSize:(CGSize)outputSize
                           complete:(void (^)(NSArray *))complete
{
    [self.generator addVideoWithPath:videoPath
                           startTime:startTime
                            duration:duration
                        animDuration:0];
    CGFloat videoDuration = self.generator.duration;
    CGFloat singleTime = itemPerTime / 1000.0;// 一个图片的时间
    NSMutableArray *timeValues = [[NSMutableArray alloc] init];
    int idx = 0;
    while (idx * singleTime < videoDuration) {
        double time = idx * singleTime;
        [timeValues addObject:@(time)];
        idx++;
    }
    NSLog(@"-------: %d -- %lu",idx, (unsigned long)[timeValues count]);
    self.generator.imageCount = [timeValues count];
    self.generator.outputSize = outputSize;
    self.generator.timePerImage = singleTime;
    
    __weak typeof(self) weakSelf = self;
    [self removeImages];
    [self.generator generateWithCompleteHandler:^(UIImage *image) {
        if (image) {
            [weakSelf saveImgToSandBox:image];
        }
    }];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        complete([self getResourcePaths]);
    });
}

- (void)generateImages:(NSDictionary *)options handler:(void(^)(NSArray *))complete
{
    NSString *videoPath = [options valueForKey:@"videoPath"];
    videoPath = videoPath ? : [[NSUserDefaults standardUserDefaults] objectForKey:@"videoSavePath"];
    CGFloat duration = [[options valueForKey:@"duration"] floatValue] ? : [self.player getDuration];
    CGFloat startTime = [[options valueForKey:@"startTime"] floatValue] ? : 0.0;
    NSInteger itemPerTime = [[options valueForKey:@"itemPerTime"] integerValue] ? : 1000; //ms
    [self _generateImageFromVideoPath:videoPath
                          itemPerTime:itemPerTime
                            startTime:startTime
                             duration:duration
                  generatorOutputSize:CGSizeMake(200, 200)
                             complete:complete];
}

- (NSString *)saveImgToSandBox:(UIImage *)image
{
    NSString *fileDirectoryPath = ThumnailDirectory();
    NSFileManager *fm = [NSFileManager defaultManager];
    if (![fm fileExistsAtPath:fileDirectoryPath]) {
        NSString *documentsDirectory = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
        fileDirectoryPath = [documentsDirectory stringByAppendingPathComponent:@"thumbNail"];
        [fm createDirectoryAtPath:fileDirectoryPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    static int i = 0;
    NSData *imgData = UIImagePNGRepresentation(image);
    NSString *imageName = [NSString stringWithFormat:@"%03d.png", ++i];
    NSString *imgPath = [fileDirectoryPath stringByAppendingPathComponent:imageName];
    BOOL suc = [imgData writeToFile:imgPath atomically:YES];
    NSLog(@"-------writeToFile :%@", suc == 1 ? @"YES" : @"NO");
    return imgPath;
}

- (void)removeImages
{
    NSFileManager *fm = [NSFileManager defaultManager];
    NSString *folderPath = ThumnailDirectory();
    NSDirectoryEnumerator *dirEnumerator = [fm enumeratorAtPath:folderPath];
    BOOL isDir = NO, isExist = NO;
    for (NSString *fileName in dirEnumerator.allObjects) {
        NSString *filePath = [NSString stringWithFormat:@"%@/%@", folderPath, fileName];
        isExist = [fm fileExistsAtPath:filePath isDirectory:&isDir];
        if (!isDir && isExist) {
            NSLog(@"-------isExist :%@", isExist == 1 ? @"YES" : @"NO");
            [fm removeItemAtPath:filePath error:nil];
        }
    }
}

- (NSArray *)getResourcePaths
{
    NSMutableArray *arr = [NSMutableArray array];
    NSFileManager *fm = [NSFileManager defaultManager];
    NSString *folderPath = ThumnailDirectory();
    NSDirectoryEnumerator *dirEnumerator = [fm enumeratorAtPath:folderPath];
    BOOL isDir = NO, isExist = NO;
    for (NSString *fileName in dirEnumerator.allObjects) {
        NSString *filePath = [NSString stringWithFormat:@"%@/%@", folderPath, fileName];
        isExist = [fm fileExistsAtPath:filePath isDirectory:&isDir];
        if (!isDir && isExist) {
            [arr addObject:filePath];
        }
    }
    return arr;
}

@end
