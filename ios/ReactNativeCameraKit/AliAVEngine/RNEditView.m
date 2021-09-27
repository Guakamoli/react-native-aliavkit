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

#import "RNEditViewManager.h"
#import "AliyunMediaConfig.h"
#import "AliyunEffectPrestoreManager.h"
#import "AliyunPathManager.h"
#import "AliyunEditZoneView.h"

@interface RNEditView()<
AliyunIPlayerCallback,
AliyunIRenderCallback,
AliyunEditZoneViewDelegate
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
@end

@implementation RNEditView

- (void)dealloc
{
    [_editor stopEdit];
}

- (AliyunMediaConfig *)mediaConfig
{
    //{ mediaType: 'video', cameraType: 'front', allowsEditing: true, videoQuality: 'high' },
    if (!_mediaConfig) {//默认配置
        _mediaConfig = [AliyunMediaConfig defaultConfig];
        //录制时长，最小2s,最多3min
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

- (instancetype)initWithManager:(RNEditViewManager*)manager bridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        self.manager = manager;
        self.bridge = bridge;
        self.backgroundColor = [UIColor orangeColor];
        NSString * videoSavePath = [[NSUserDefaults standardUserDefaults] objectForKey:@"videoSavePath"];
        self.videoPath = videoSavePath;
        [self initBaseData];
        [self addSubview:self.preview];
        [self initSDKAbout];
        
        [self.editor startEdit];
        [self play];
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

/*
 
 /var/mobile/Containers/Data/Application/F6191174-AB01-4C14-BFBB-4D8B055FA4A8/Documents/com.guakamoli.engine/record/CB762E23-D7A7-4F16-B211-00454C824D41/853E9ABF-525D-4F4A-8C8B-E59ADD17C672.mp4
 */

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
    NSLog(@"---------->clip.duration:%f",clip.duration);
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
//    [self addSubview:self.preview];
    

}

- (void)setVideoPath:(NSString *)videoPath
{
    if (_videoPath != videoPath) {
        _videoPath = videoPath;
        
    }
}

#pragma mark - AliyunIPlayerCallback --播放器回调

///播放结束
- (void)playerDidEnd
{
    
}

/**
 播放进度
 
 @param playSec 播放时间
 @param streamSec 播放流时间
 */
- (void)playProgress:(double)playSec streamProgress:(double)streamSec
{
    NSLog(@"--- %s: %lf",__PRETTY_FUNCTION__, playSec);
}

/**
 播放异常

 @param errorCode 错误码
 状态错误 ALIVC_FRAMEWORK_MEDIA_POOL_WRONG_STATE
 DEMUXER重复创建 ALIVC_FRAMEWORK_DEMUXER_INIT_MULTI_TIMES
 DEMUXER打开失败 ALIVC_FRAMEWORK_DEMUXER_OPEN_FILE_FAILED
 DEMUXER获取流信息失败 ALIVC_FRAMEWORK_DEMUXER_FIND_STREAM_INFO_FAILED
 解码器创建失败 ALIVC_FRAMEWORK_AUDIO_DECODER_CREATE_DECODER_FAILED
 解码器状态错误 ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_STATE
 解码器输入错误 ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_INPUT
 解码器参数SPSPPS为空 ALIVC_FRAMEWORK_VIDEO_DECODER_SPS_PPS_NULL,
 解码H264参数创建失败 ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_H264_PARAM_SET_FAILED
 解码HEVC参数创建失败 ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_HEVC_PARAM_SET_FAILED
 缓存数据已满 ALIVC_FRAMEWORK_MEDIA_POOL_CACHE_DATA_SIZE_OVERFLOW
 解码器内部返回错误码
 */
- (void)playError:(int)errorCode
{
    NSLog(@"--- %s:  %d",__PRETTY_FUNCTION__,errorCode);
}

#pragma mark - AliyunIRenderCallback
- (int)customRender:(int)srcTexture size:(CGSize)size {
    // 自定义滤镜渲染
    return srcTexture;
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


/*
 渲染编排构建异常
 ALIVC_FRAMEWORK_RENDER_ERROR_LAYOUT_NOT_INIT =                  -10007009,
 */
/// 尝试播放视频
- (void)play {
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
- (void)resume {
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
-(void)replay {
    [self.player replay];
    [self updateUIAndDataWhenPlayStatusChanged];
}

/// 尝试暂停视频
- (void)pause {
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
- (void)updateUIAndDataWhenPlayStatusChanged {
    if (self.player.isPlaying) {
        
        _prePlaying = YES;
    } else {
        
        _prePlaying = NO;
    }
}
@end
