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



  //获取滤镜列表
  getColorFilterList = async () => {
    let colorFilterList = await RNEditorKitModule.getColorFilterList(findNodeHandle(this.nativeRef.current));
    return JSON.parse(colorFilterList)
  };


  //播放、继续播放
  onPlay = async () => {
    let play = await RNEditorKitModule.play(findNodeHandle(this.nativeRef.current));
    console.log("replay", play);
  };


  //暂停播放
  onPause = async () => {
    let pause = await RNEditorKitModule.pause(findNodeHandle(this.nativeRef.current));
    console.log("onPause", pause);
  };

  //停止播放
  onStop = async () => {
    let stop = await RNEditorKitModule.stop(findNodeHandle(this.nativeRef.current));
    console.log("onStop", stop);
  };

  //定位播放
  onSeek = async (time) => {
    // * seek到某个时间点   @param time 时间，单位：毫秒
    let seek = await RNEditorKitModule.seek(time,findNodeHandle(this.nativeRef.current));
    console.log("onSeek", seek);
  };

  //获取视频封面    @param time 时间，单位：毫秒
  onVideoCover = async (time) => {
    let videoCover = await RNEditorKitModule.videoCover(time,findNodeHandle(this.nativeRef.current));
    console.log("onVideoCover", videoCover);
  };


  //视频裁剪，时间裁剪，传入开始结束时间,成功后会播放裁剪后的视频
  videoTrim = async () => {
    let trimParam = {
      'startTime': 3000,
      'endTime': 8000,
    }
    let videoTrim = await RNEditorKitModule.videoTrim(trimParam, findNodeHandle(this.nativeRef.current));
    console.log("videoTrim", videoTrim);
  };



  componentDidMount() {
    //播放回调
    this.startVideoPlayListener = DeviceEventEmitter.addListener('startVideoEditor', (duration) => {
      // console.log("startVideoEditor", duration);
    });

    // //视频裁剪进度
    // this.startVideoCropListener = DeviceEventEmitter.addListener('startVideoCrop', (progress) => {
    //   console.log("startVideoCrop", progress);
    // });

    //导出视频 合成回调
    this.startVideoComposeListener = DeviceEventEmitter.addListener('startVideoCompose', (param) => {
      // param = {{"exportProgress": 1, "outputPath": "....jpg"}}
      // console.log("startVideoCompose", param);
      this.props.onExportVideo(param);
    });
  }

  componentWillUnmount() {
    RNEditorKitModule.release(findNodeHandle(this.nativeRef.current));
    if (this.startVideoPlayListener != null) {
      this.startVideoPlayListener.remove();
    }
    // if (this.startVideoCropListener != null) {
    //   this.startVideoCropListener.remove();
    // }
    if (this.startVideoComposeListener != null) {
      this.startVideoComposeListener.remove();
    }
  }

  render() {
    return (
      <View style={styles.cameraContainer}>
        <NativeEditor
          ref={this.nativeRef}
          style={{ minWidth: 100, minHeight: 100 }}
          {...this.props}
          // startExportVideo = {this.props.startExportVideo}
        />
      </View>
    );
  }

};

const styles = StyleSheet.create({
  cameraContainer: {
    flex: 1,
    backgroundColor: 'red',
  },

  composeVideo: {
    flex: 1,
    backgroundColor: 'red',
  },
  captureButtonContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
