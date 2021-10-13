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
    console.log("getColorFilterList", colorFilterList);
    this.setState({
      colorFilterList: JSON.parse(colorFilterList),
    });
  };

  //设置滤镜
  setColorFilter = () => {
    let position = this.state.colorFilterPosition;
    let list = this.state.colorFilterList;
    let colorFilterName = list[position].name
    console.log("setColorFilter", colorFilterName);
    RNEditorKitModule.setColorFilter(colorFilterName, findNodeHandle(this.nativeRef.current));
    this.setState({
      colorFilterPosition: (position + 1) >= list.length ? 0 : (position + 1),
    });
  };


  //设置静音
  setAudioSilence = async () => {
    let audioSilence = !this.state.audioSilence;
    console.log("静音", audioSilence);
    this.setState({
      audioSilence: audioSilence,
    });
  };


  //完成导出视频，返回视频地址
  onExportVideo = async () => {
    let videoPath = await RNEditorKitModule.exportVideo(findNodeHandle(this.nativeRef.current));
    console.log("exportVideo", videoPath);
  };


  //完成导出图片，返回导出的图片地址
  onExportImage = async () => {
    let imagePath = await RNEditorKitModule.exportImage(findNodeHandle(this.nativeRef.current));
    console.log("exportImage", imagePath);
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
  }

  //停止播放
  onStop = async () => {
    let stop = await RNEditorKitModule.stop(findNodeHandle(this.nativeRef.current));
    console.log("onStop", stop);
  };

  //定位播放
  onSeek = async () => {
    // * seek到某个时间点   @param time 时间，单位：毫秒
    let seek = await RNEditorKitModule.seek(2000,findNodeHandle(this.nativeRef.current));
    console.log("onSeek", seek);
  };

  //获取视频封面
  onVideoCover = async () => {
    let videoCover = await RNEditorKitModule.videoCover(2000,findNodeHandle(this.nativeRef.current));
    console.log("onVideoCover", videoCover);
  }


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

    //导出视频 合成回调
    this.startVideoComposeListener = DeviceEventEmitter.addListener('startVideoCompose', (progress) => {
      console.log("startVideoCompose", progress);
    });
  }

  componentWillUnmount() {
    RNEditorKitModule.release(findNodeHandle(this.nativeRef.current));
    if (this.startVideoPlayListener != null) {
      this.startVideoPlayListener.remove();
    }
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
          audioSilence={this.state.audioSilence}
          videoPath="/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/Media/paiya-record.mp4"
        // imagePath="/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/Media/1634097852533-photo.jpg"
        />
        <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.getColorFilterList()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
          </TouchableOpacity>
        </View>
        <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.setColorFilter()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
          </TouchableOpacity>
        </View>

        <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.setAudioSilence()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
          </TouchableOpacity>
        </View>

        <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.onExportVideo()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
          </TouchableOpacity>
        </View>


        <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.onVideoCover()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
          </TouchableOpacity>
        </View>

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
