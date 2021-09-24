//
//  RNMovieView.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/9/24.
//

#import "RNMovieView.h"

#import <AliyunVideoSDKPro/AliyunImporter.h>
#import <AliyunVideoSDKPro/AliyunEditor.h>
#import <AliyunVideoSDKPro/AliyunPasterManager.h>

#import "RNEditViewManager.h"
#import "AliyunMediaConfig.h"
#import "AliyunEffectPrestoreManager.h"
#import "AliyunPathManager.h"
#import "AliyunEditZoneView.h"

@interface RNMovieView()<
AliyunIPlayerCallback
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

@end

@implementation RNMovieView

- (void)dealloc
{
    [_editor stopEdit];
}

- (instancetype)initWithManager:(RNEditViewManager*)manager bridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        self.manager = manager;
        self.bridge = bridge;
        [self initBaseData];
        [self initSDKAbout];
    }
    return self;
}

///设置初始值
- (void)initBaseData
{
    AliyunEffectPrestoreManager *pm = [AliyunEffectPrestoreManager new];
    [pm insertInitialData];
    
    // 校验视频分辨率，如果首段视频是横屏录制，则outputSize的width和height互换
    _inputOutputSize = _config.outputSize;
    _outputSize = [_config fixedSize];
    _config.outputSize = _outputSize;
    
    [self _setVideoTaskPath];
    
    //防size异常奔溃处理
    if (_outputSize.height == 0 || _outputSize.width == 0) {
        _outputSize.width = 720;
        _outputSize.height = 1280;
        NSAssert(false, @"调试的时候崩溃,_outputSize分辨率异常处理");
    }
}

/// 单视频接入编辑页面，生成一个新的taskPath
- (void)_setVideoTaskPath {
    if (_taskPath) {
        return;
    }
    _taskPath = [[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:[AliyunPathManager randomString]];
    AliyunImporter *importer =[[AliyunImporter alloc] initWithPath:_taskPath outputSize:_outputSize];
    AliyunVideoParam *param = [[AliyunVideoParam alloc] init];
    param.fps = _config.fps;
    param.gop = _config.gop;
    if (_config.videoQuality == AliyunVideoQualityVeryHight) {
        param.bitrate = 10*1000*1000; // 10Mbps
    } else {
        param.videoQuality = (AliyunVideoQuality)_config.videoQuality;
    }
    if (_config.cutMode == AliyunMediaCutModeScaleAspectCut) {
        param.scaleMode = AliyunScaleModeFit;
    } else {
        param.scaleMode = AliyunScaleModeFill;
    }
    // 编码模式
    if (_config.encodeMode ==  AliyunEncodeModeHardH264) {
        param.codecType = AliyunVideoCodecHardware;
    }else if(_config.encodeMode == AliyunEncodeModeSoftFFmpeg) {
        param.codecType = AliyunVideoCodecOpenh264;
    }
    
    [importer setVideoParam:param];
    AliyunClip *clip = [[AliyunClip alloc] initWithVideoPath:_videoPath animDuration:0];
    [importer addMediaClip:clip];
    [importer generateProjectConfigure];
    _config.outputPath = [[_taskPath stringByAppendingPathComponent:[AliyunPathManager randomString]] stringByAppendingPathExtension:@"mp4"];
}

/// 初始化sdk相关
- (void)initSDKAbout
{
    // editor
    self.editor = [[AliyunEditor alloc] initWithPath:_taskPath preview:self];

    self.editor.playerCallback =  (id)self;
    self.editor.renderCallback =  (id)self;
    // player
    self.player = [self.editor getPlayer];
    
    // constructor
    self.clipConstructor = [self.editor getClipConstructor];
    
    // setup pasterEditZoneView
    self.editZoneView = [[AliyunEditZoneView alloc] initWithFrame:self.bounds];
    self.editZoneView.delegate = (id)self;
    [self addSubview:self.editZoneView];
    
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
    //播放视图
    if (_outputSize.width && _outputSize.height) {
        CGFloat factor = _outputSize.height / _outputSize.width;
        CGRect frame = self.frame;
        frame.size.width = [UIScreen mainScreen].bounds.size.width;
        frame.size.height = [UIScreen mainScreen].bounds.size.width * factor;
        self.frame = frame;
    }
}

#pragma mark - AliyunIPlayerCallback

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
    
}

/// 播放异常
- (void)playError:(int)errorCode
{
    
}


/// 尝试播放视频
- (void)play {
    if (self.player.isPlaying) {
        NSLog(@"短视频编辑播放器测试:当前播放器正在播放状态,不调用play");
    } else {
        int returnValue = [self.player play];
        NSLog(@"短视频编辑播放器测试:调用了play接口");
        if (returnValue == 0) {
            NSLog(@"短视频编辑播放器测试:play返回0成功");
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
