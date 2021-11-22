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
#import <AliyunVideoSDKPro/AliyunCrop.h>
#import <AliyunVideoSDKPro/AliyunNativeParser.h>

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
#import "AliyunTimelineMediaInfo.h"
#import "AliyunCompositionInfo.h"
#import "AliyunAlbumModel.h"
#import "AliyunPhotoLibraryManager.h"
#import "AliyunMusicPickModel.h"
#import "RNMusicInfo.h"
#import "ShortCut.h"
#import "RNAVDeviceHelper.h"

typedef void(^TransCode_blk_t)(CGFloat);

@interface RNEditView()<
AliyunIPlayerCallback,
AliyunIRenderCallback,
AliyunEditZoneViewDelegate,
AliyunIExporterCallback,
AliyunCropDelegate
>
{
    BOOL _prePlaying;
    AliyunCrop *_musicCrop;
    BOOL _isPresented;
    CGFloat _editWidth;
    CGFloat _editHeight;
}
@property(nonatomic, assign) CGSize inputOutputSize;
@property(nonatomic, assign) CGSize outputSize;

@property(nonatomic, strong) AliyunPasterManager *pasterManager;
@property(nonatomic, strong) AliyunEditZoneView *editZoneView;
@property(nonatomic, strong) AliyunEditor *editor;
@property(nonatomic, weak) id<AliyunIPlayer> player;
@property(nonatomic, weak) id<AliyunIClipConstructor> clipConstructor;

@property (nonatomic, strong) UIView *preview;
@property (nonatomic, weak) RCTBridge *bridge;

@property (nonatomic, strong) AliyunVodPublishManager *publishManager;

@property (nonatomic, copy) NSString *filterName;
@property (nonatomic) BOOL startExportVideo;
@property (nonatomic) BOOL saveToPhotoLibrary;

@property (nonatomic) BOOL videoMute;
@property (nonatomic, copy) NSString *imagePath;
@property (nonatomic, strong) AliyunCompositionInfo *compositionInfo;
@property (nonatomic, copy) NSDictionary *musicInfo;
@property (nonatomic, copy) TransCode_blk_t transCode_blk;

@property (nonatomic, strong) NSDictionary *editStyle;

@end

@implementation RNEditView

- (AliyunMediaConfig *)mediaConfig
{
    if (!_mediaConfig) {//默认配置
        _mediaConfig = [AliyunMediaConfig defaultConfig];
        _mediaConfig.minDuration = 0.5f;
        _mediaConfig.maxDuration = 15.f;
        _mediaConfig.gop = 30;
        _mediaConfig.cutMode = AliyunMediaCutModeScaleAspectFill;
        _mediaConfig.videoOnly = YES;
        _mediaConfig.backgroundColor = [UIColor blackColor];
        _mediaConfig.videoQuality = AliyunMediaQualityHight;
        _mediaConfig.outputSize = CGSizeMake(1080, 1920);
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

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
    if ((self = [super initWithFrame:CGRectZero])) {
        self.bridge = bridge;
        _isPresented = NO;
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
//    AVDLog(@"%@", NSStringFromCGSize(self.outputSize));
    self.mediaConfig.outputSize = self.outputSize;
    
    //防size异常奔溃处理
    if (_outputSize.height == 0 || _outputSize.width == 0) {
        _outputSize.width = 1080;
        _outputSize.height = 1920;
        NSAssert(false, @"调试的时候崩溃,_outputSize分辨率异常处理");
    }
}

- (void)setPhotoTaskPathWithPhotoPath:(NSString *)photoPath
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
    param.bitrate = 10*1000*1000;
    param.scaleMode = AliyunScaleModeFill;
    param.codecType = AliyunVideoCodecHardware;
    [importor setVideoParam:param];
    
//    AVDLog(@"PhototaskPath: %@", taskPath);
    // generate config
    [importor generateProjectConfigure];
    // output path
    self.mediaConfig.outputPath = [[taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
    self.taskPath = taskPath;
}

/// 单视频接入编辑页面，生成一个新的taskPath
- (void)setVideoTaskPathWithVideopath:(NSString *)videoPath
{
    self.taskPath = [[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]];
//    AVDLog(@"VideotaskPath: %@", self.taskPath);
    AliyunImporter *importer =[[AliyunImporter alloc] initWithPath:self.taskPath outputSize:self.outputSize];
    AliyunVideoParam *param = [[AliyunVideoParam alloc] init];
    param.fps = self.mediaConfig.fps;
    param.gop = self.mediaConfig.gop;
    if ([RNAVDeviceHelper isBelowIphone_11]) {
        param.videoQuality = AliyunVideoQualityMedium;
    } else {
        param.bitrate = 10*1000*1000; // 10Mbps
    }
    param.scaleMode = AliyunScaleModeFill;
    // 编码模式
    param.codecType = AliyunVideoCodecHardware;
    
    [importer setVideoParam:param];
//    AVDLog(@"_videoPath:  %@",videoPath);
    AliyunClip *clip = [[AliyunClip alloc] initWithVideoPath:videoPath animDuration:0];
    [importer addMediaClip:clip];
    [importer generateProjectConfigure];
//    AVDLog(@"clip.duration:%f",clip.duration);
    self.mediaConfig.outputPath = [[_taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
}

- (UIView *)preview
{
    if (!_preview) {
        CGFloat factor = _outputSize.height / _outputSize.width;
        CGRect frame = CGRectZero;
        if (_editWidth > [UIScreen mainScreen].bounds.size.width) {
            frame.size.width = [UIScreen mainScreen].bounds.size.width;
            if (_editHeight == _editWidth) {
                frame.size.height = [UIScreen mainScreen].bounds.size.width;
            } else {
                frame.size.height = [UIScreen mainScreen].bounds.size.width * factor;
            }
        }
        else {
            frame.size.width = _editWidth > 0.0 ? _editWidth : [UIScreen mainScreen].bounds.size.width;
            frame.size.height = _editHeight > 0.0 ? _editHeight : [UIScreen mainScreen].bounds.size.width * factor;
        }
        _preview = [[UIView alloc] initWithFrame:frame];
        _preview.backgroundColor = [UIColor lightGrayColor];
    }
    return _preview;
}

- (void)didMoveToSuperview
{
    [super didMoveToSuperview];
    if (_isPresented && !self.superview) {
        AVDLog(@"🪝appeared, going disappear");
        [_editor stopEdit];
        _isPresented = NO;
    }
}

- (void)didMoveToWindow
{
    [super didMoveToWindow];
    if (!_isPresented && self.window) {
        AVDLog(@"🪝ready to appear");
        if (self.videoPath) {
            [self _initVideoEditor];
            return;
        }
        if (self.imagePath) {
            [self _initImageEditor];
        }
    }
    if (_isPresented && !self.window) {
        [_editor stopEdit];
        _isPresented = NO;
    }
}
- (void)_initVideoEditor
{
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.videoPath]) {
        AVDLog(@"🔥 videoPath doesn't exist");
        return;
    }
    [self initBaseData];
    [self setVideoTaskPathWithVideopath:self.videoPath];
    [self addSubview:self.preview];
    [self initSDKAbout];
    
    int num = [self.editor startEdit];
    if (num == ALIVC_COMMON_RETURN_SUCCESS) {
        [[self.editor getPlayer] play];
    }
    else if (num == ALIVC_COMMON_INVALID_STATE) {
        AVDLog(@"INVALID_STATE");
    }
    else if (num == ALIVC_COMMON_INVALID_PARAM) {
        AVDLog(@"INVALID_PARAM");
    }
    _isPresented = YES;
}

- (void)_initImageEditor
{
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.imagePath]) {
        AVDLog(@"🔥 imagePath doesn't exist");
        return;
    }
    [self initBaseData];
    [self setPhotoTaskPathWithPhotoPath:self.imagePath];
    [self addSubview:self.preview];
    [self initSDKAbout];
    
    int num = [self.editor startEdit];
    if (num == ALIVC_COMMON_RETURN_SUCCESS) {
        [[self.editor getPlayer] play];
    }
    else if (num == ALIVC_COMMON_INVALID_STATE) {
        AVDLog(@"INVALID_STATE");
    }
    else if (num == ALIVC_COMMON_INVALID_PARAM) {
        AVDLog(@"INVALID_PARAM");
    }
    _isPresented = YES;
}

/// 初始化sdk相关
- (void)initSDKAbout
{
    if ([[NSFileManager defaultManager] fileExistsAtPath:self.taskPath]) {
        AVDLog(@"%s",__PRETTY_FUNCTION__);
    }
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

#pragma mark - Setter

- (void)setEditStyle:(NSDictionary *)editStyle
{
    if (_editStyle != editStyle && ![editStyle isEqualToDictionary:@{}]) {
        _editWidth = [[editStyle valueForKey:@"width"] floatValue];
        _editHeight = [[editStyle valueForKey:@"height"] floatValue];
    }
}

- (void)setImagePath:(NSString *)imagePath
{
    if (_imagePath != imagePath) {
        if (imagePath && ![imagePath isEqualToString:@""]) {
            _imagePath = imagePath;
            if ([imagePath containsString:@"file://"]) { //in case path contains scheme
                _imagePath = [NSURL URLWithString:imagePath].path;
            }
            AVDLog(@"imagePath：%@",_imagePath);
        }
//        else {
            //**For test only**
//            NSString * photoPath = [[NSUserDefaults standardUserDefaults] objectForKey:@"photoPath"];
//            if (photoPath) {
//                _imagePath = photoPath;
//            }
//        }
    }
}

- (void)setVideoPath:(NSString *)videoPath
{
    if (_videoPath != videoPath) {
        if (videoPath && ![videoPath isEqualToString:@""]) {
            _videoPath = videoPath;
            if ([videoPath containsString:@"file://"]) { //in case path contains scheme
                _videoPath = [NSURL URLWithString:videoPath].path;
            }
            AVDLog(@"videoPath：%@",_videoPath);
        }
    }
    else {
        //**For test only**
//        NSString * videoSavePath = [[NSUserDefaults standardUserDefaults] objectForKey:@"videoSavePath"];
//        if (videoSavePath) {
//            _videoPath = videoSavePath;
//        }
    }
}

- (void)prepareForExport
{
    [self.player stop];
    [self.editor stopEdit];
    
    int result = [self.publishManager exportWithTaskPath:self.taskPath outputPath:self.mediaConfig.outputPath];
    if (result != 0) {
        AVDLog(@"合成失败");
    }
}

- (void)setOnExportVideo:(RCTBubblingEventBlock)onExportVideo
{
    if (_onExportVideo != onExportVideo) {
        _onExportVideo = onExportVideo;
    }
}

- (void)setOnPlayProgress:(RCTBubblingEventBlock)onPlayProgress
{
    if (_onPlayProgress != onPlayProgress) {
        _onPlayProgress = onPlayProgress;
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

- (void)setMusicInfo:(NSDictionary *)musicInfo
{
    _musicInfo = musicInfo;
    
    if (musicInfo && ![musicInfo isEqualToDictionary:@{}]) {
        AliyunMusicPickModel *model = [AliyunMusicPickModel new];
        model.path = [musicInfo valueForKey:@"localPath"];
        model.startTime = 0;
        
        AliyunNativeParser *parser = [[AliyunNativeParser alloc] initWithPath:model.path];
        NSString *format = [parser getValueForKey:ALIYUN_AUDIO_CODEC];
        model.duration = [parser getAudioDuration];
        float outputVolume = [[AVAudioSession sharedInstance] outputVolume];
        model.volume = outputVolume;
        AVDLog(@"outputVolume: %lf", outputVolume);
        
        if (model.path && model.duration > 0.0) {
            if ([format isEqualToString:@"mp3"] ) {
                __weak typeof(self) weakSelf = self;
                [self transcode:model complete:^(CGFloat progress) {
                    if (progress == 1.0) {
                        [weakSelf composeAACFormatMusic:model];
                    }
                }];
            } else {
                [self composeAACFormatMusic:model];
            }
        }
    }
}

- (void)transcode:(AliyunMusicPickModel *)model complete:(TransCode_blk_t)complete
{
    //配音功能只支持aac格式，mp3格式的音乐需要转码
    //建议使用aac格式的音乐资源
    _musicCrop = [[AliyunCrop alloc] initWithDelegate:self];
    NSString *outputPath = [[AliyunPathManager createMagicRecordDir] stringByAppendingPathComponent:[model.path lastPathComponent]];
    _musicCrop.inputPath = model.path;
    _musicCrop.outputPath = outputPath;
    _musicCrop.startTime = model.startTime;
    _musicCrop.endTime = model.duration + model.startTime;
    model.path = outputPath;
    int num = [_musicCrop startCrop];
    if (num == 0) {
        AVDLog(@"startCrop: %d",num);
        self.transCode_blk = complete;
    }
}

#pragma mark - aliyun crop
- (void)cropOnError:(int)error
{
    
}

- (void)cropTaskOnProgress:(float)progress
{
    AVDLog(@"%f", progress);
    self.transCode_blk(progress);
}

- (void)cropTaskOnComplete
{
    self.transCode_blk(1.0);
}

- (void)cropTaskOnCancel
{
    
}

/// 合成应用ACC格式的音乐，非此格式需要转码
/// @param music AliyunMusicPickModel
- (void)composeAACFormatMusic:(AliyunMusicPickModel *)music
{
    [self.editor removeMusics];
    if ([[NSFileManager defaultManager] fileExistsAtPath:music.path]) {
        AVDLog(@"music.path: %@",music.path);
    }
    AliyunEffectMusic *effectMusic = [[AliyunEffectMusic alloc] initWithFile:music.path];
    effectMusic.startTime = music.startTime * 0.001;
    effectMusic.duration = music.duration;
    effectMusic.audioMixWeight = (int)roundf(music.volume*100);
    int code = [self.editor applyMusic:effectMusic];
    if (code == ALIVC_COMMON_RETURN_SUCCESS) {
        AVDLog(@"composeAACFormatMusic success");
    }
    
    [self resume];
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
                        AVDLog(@"保存%@成功!", type == PHAssetResourceTypeVideo ? @"视频" : @"图片");
                    } else {
                        AVDLog(@"保存%@失败:%@", type == PHAssetResourceTypeVideo ? @"视频" : @"图片", error);
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
                    AVDLog(@"保存视频成功!");
                } else {
                    AVDLog(@"保存视频失败:%@", error);
                }
            });
        }];
    }];
}

#pragma mark - AliyunIPlayerCallback --播放器回调
///播放结束
- (void)playerDidEnd
{
    if (_onPlayProgress) {
        _onPlayProgress(@{@"playEnd":@(YES)});
    }
    [self replay];
}

- (void)playProgress:(double)playSec streamProgress:(double)streamSec
{
    id event = @{ @"playProgress":@(playSec),
                  @"streamProgress":@(streamSec)};
    if (_onPlayProgress) {
        _onPlayProgress(event);
    }
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
    AVDLog(@"%d",errorCode);
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
            [self saveResourceType:PHAssetResourceTypeVideo withPath:path];
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

- (void)exporterDidStart
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

- (void)stop
{
    [self.player stop];
    [_editor stopEdit];
}

/// 尝试播放视频
- (void)play
{
    if (self.player.isPlaying) {
        AVDLog(@"短视频编辑播放器测试:当前播放器正在播放状态,不调用play");
    } else {
        int returnValue = [self.player play];
        AVDLog(@"短视频编辑播放器测试:调用了play接口");
        if (returnValue == 0) {
            AVDLog(@"短视频编辑播放器测试:play返回0成功");
        } else {
            switch (returnValue) {
                case ALIVC_COMMON_INVALID_STATE: //-4
                    AVDLog(@"------播放失败： 状态错误");
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
        AVDLog(@"短视频编辑播放器测试:当前播放器正在播放状态,不调用resume");
    } else {
        int returnValue = [self.player resume];
        AVDLog(@"短视频编辑播放器测试:调用了resume接口");
        if (returnValue == 0) {
            //            [self forceFinishLastEditPasterView];
            AVDLog(@"短视频编辑播放器测试:resume返回0成功");
        } else {
            [self.player play];
            AVDLog(@"短视频编辑播放器测试:！！！！继续播放错误,错误码:%d",returnValue);
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
        AVDLog(@"短视频编辑播放器测试:调用了pause接口");
        if (returnValue == 0) {
            AVDLog(@"短视频编辑播放器测试:pause返回0成功");
        } else {
            AVDLog(@"短视频编辑播放器测试:！！！！暂停错误,错误码:%d", returnValue);
        }
    } else {
        AVDLog(@"短视频编辑播放器测试:当前播放器不是播放状态,不调用pause");
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

@end
