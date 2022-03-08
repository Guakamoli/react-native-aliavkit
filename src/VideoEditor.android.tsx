import React, { Component } from 'react';
import {
  DeviceEventEmitter, findNodeHandle, NativeModules, requireNativeComponent, Text,
  View, StyleSheet, Platform, TouchableOpacity, Image,
} from 'react-native';

const { RNEditorKitModule } = NativeModules;

export type Props = {
  captureButtonImage: any,
}
type State = {
  audioSilence: boolean;
  colorFilterPosition: number;
  colorFilterList: any[];
};

const NativeEditor = requireNativeComponent('CKEditorManager');


export default class Editor extends Component<Props, State> {
  constructor(props) {
    super(props)
    this.state = {
      audioSilence: false,
      colorFilterPosition: 0,
      colorFilterList: [],
    }
    this.nativeRef = React.createRef();
  };

  /**
   * 获取音乐列表
   */
  getMusicList = async (name, page, pageSize) => {
    var musicList = await RNEditorKitModule.getMusicList(name, "", page, pageSize);
    return JSON.parse(musicList)
  };

  //获取背景音乐地址
  getMusicPath = async (songID) => {
    var musicPath = await RNCameraKitModule.getMusicPath(songID);
    return musicPath
  };

  //播放本地音乐
  playMusic = async (musicPath) => {
    var playMusic = await RNEditorKitModule.playMusic(musicPath);
    return playMusic
  };

  //停止播放
  stopMusic = async () => {
    var playMusic = await RNEditorKitModule.stopMusic();
    return playMusic
  };


  //获取滤镜列表
  getColorFilterList = async () => {
    let colorFilterList = await RNEditorKitModule.getColorFilterList();
    return JSON.parse(colorFilterList)
  };


  //获取视频封面    @param time 时间，单位：毫秒
  onVideoCover = async (time) => {
    let videoCover = await RNEditorKitModule.videoCover(time);
    return videoCover
  };


  //视频裁剪，时间裁剪，传入开始结束时间,成功后会播放裁剪后的视频
  trimVideo = async (trimParams) => {
    // let trimParam = {
    //   'startTime': startTime * 1000,
    //   'endTime': endTime * 1000,
    // }
    let videoTrim = await RNEditorKitModule.trimVideo(trimParams);
    return videoTrim
    // console.log("videoTrim", videoTrim, trimParams);
  };


  onPause = async () => {
    RNEditorKitModule.pause();
  }

  onSeek = async (seekTime) => {
    RNEditorKitModule.seek(seekTime);
  }

  release = async () => {
    // console.log("Video release");
    RNEditorKitModule.release();
  };


  async componentDidMount() {

    let list = await this.getMusicList("", 2, 10);
    // console.log("getMusicList", list);
    //播放回调
    this.startVideoPlayListener = DeviceEventEmitter.addListener('startVideoEditor', (params) => {
      // console.log("startVideoEditor", params);
      this.props.onPlayProgress({ nativeEvent: params });
    });

    // //视频裁剪进度
    // this.startVideoCropListener = DeviceEventEmitter.addListener('startVideoCrop', (progress) => {
    //   console.log("startVideoCrop", progress);
    // });

    //导出视频 合成回调
    this.startVideoComposeListener = DeviceEventEmitter.addListener('startVideoCompose', (param) => {
      // param = {{"exportProgress": 1, "outputPath": "....jpg"}}
      // console.log("视频合成中...", param);
      this.props.onExportVideo(param);
    });

    //音乐下载进度
    this.downloadMusicListener = DeviceEventEmitter.addListener('downloadMusic', (progress) => {
      console.log("downloadMusic", progress);
    });
  }

  componentWillUnmount() {
    if (this.startVideoPlayListener != null) {
      this.startVideoPlayListener.remove();
    }
    // if (this.startVideoCropListener != null) {
    //   this.startVideoCropListener.remove();
    // }
    if (this.startVideoComposeListener != null) {
      this.startVideoComposeListener.remove();
    }

    if (this.downloadMusicListener != null) {
      this.downloadMusicListener.remove();
    }
    // TODO post 销毁
    // if(this.props.source != 'story'){
    //   console.log('post 销毁');
      
     RNEditorKitModule.release();
    // }

  }

  render() {
    return (
      <NativeEditor
        style={{ minWidth: 100, minHeight: 100 }}
        {...this.props}
        // editLayout={{width:props.editWidth,height:props.CameraFixHeight}}
        ref={this.nativeRef}
      // startExportVideo = {this.props.startExportVideo}
      />
    );
  }

};

const styles = StyleSheet.create({
  cameraContainer: {

  },

  composeVideo: {
    flex: 1,
  },
  captureButtonContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
