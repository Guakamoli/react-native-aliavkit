import React from 'react';
import _ from 'lodash';
import { requireNativeComponent, findNodeHandle, NativeModules, processColor, DeviceEventEmitter } from 'react-native';

const { RNCameraKitModule } = NativeModules;
const NativeCamera = requireNativeComponent('CKCameraManager');

const Camera = React.forwardRef((props, ref) => {
  const nativeRef = React.useRef();

  React.useImperativeHandle(ref, () => ({
    capture: async (options = {}) => {
      // Because RN doesn't support return types for ViewManager methods
      // we must use the general module and tell it what View it's supposed to be using
      return await RNCameraKitModule.capture(options, findNodeHandle(nativeRef.current));
    },
    startRecording: async (options = {}) => {
      return await RNCameraKitModule.startRecording(findNodeHandle(nativeRef.current));
    },
    stopRecording: async (options = {}) => {
      console.log('android api stopRecording');
      return await RNCameraKitModule.stopRecording(findNodeHandle(nativeRef.current));
    },
    requestDeviceCameraAuthorization: async () => {
      return await RNCameraKitModule.requestDeviceCameraAuthorization();
    },

    //获取服务器端的贴纸
    getPasterInfos: async () => {
      var pasterInfos = await RNCameraKitModule.getPasterInfos();
      return JSON.parse(pasterInfos)
    },

    /**
     * 获取音乐列表
     * @param name 
     * @param page 
     * @param pageSize 
     * @returns 
     */
    getMusicList: async (name, page, pageSize) => {
      var musicList = await RNCameraKitModule.getMusicList(name, "", page, pageSize);
      return JSON.parse(musicList)
    },

    //获取背景音乐地址
    getMusicPath: async (songID) => {
      var musicPath = await RNCameraKitModule.getMusicPath(songID);
      return musicPath
    },

    //播放本地音乐
    playMusic: async (musicPath) => {
      var playMusic = await RNCameraKitModule.playMusic(musicPath);
      return playMusic
    },

    //停止播放
    stopMusic: async () => {
      var playMusic = await RNCameraKitModule.stopMusic();
      return playMusic
    },

    //释放资源，退出页面时调用
    release: async () => {
      // return await RNCameraKitModule.release(findNodeHandle(nativeRef.current));
    },
  }));

  React.useEffect(() => {
    //视频录制进度
    const subscription = DeviceEventEmitter.addListener('startVideoRecord', (duration) => {
      console.log("duration", duration);
    });
    //贴纸下载进度
    const downloadPaster = DeviceEventEmitter.addListener('downloadPaster', (progress) => {
      console.log("downloadPaster", progress);
    });
    //音乐下载进度
    const downloadMusic = DeviceEventEmitter.addListener('downloadMusic', (progress) => {
      console.log("downloadMusic", progress);
    });
    return () => {
      // RNCameraKitModule.release(findNodeHandle(nativeRef.current));
      subscription.remove();
      downloadPaster.remove();
      downloadMusic.remove();
    };
  }, []);

  const transformedProps = _.cloneDeep(props);
  _.update(transformedProps, 'cameraOptions.ratioOverlayColor', (c) => processColor(c));
  _.update(transformedProps, 'frameColor', (c) => processColor(c));
  _.update(transformedProps, 'laserColor', (c) => processColor(c));
  _.update(transformedProps, 'surfaceColor', (c) => processColor(c));

  return (
    <NativeCamera
      style={{ minWidth: 100, minHeight: 100 }}
      flashMode={props.flashMode}
      ref={nativeRef}
      // backgroundMusic={"/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/music/download/10020_许嵩-庐州月.aac"}
      {...transformedProps}
    />);
});

export default Camera;
