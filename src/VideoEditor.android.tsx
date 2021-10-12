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
  colorFilterPosition: number;
  colorFilterList: any[];
};

const NativeEditor = requireNativeComponent('CKEditorManager');


export default class Editor extends Component<Props, State> {
  constructor(props) {
    super(props)
    this.state = {
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


  onExportVideo = async () => {
    let videoPath = await RNEditorKitModule.exportVideo(findNodeHandle(this.nativeRef.current));
    console.log("exportVideo", videoPath);
  };


  onExportImage = async () => {
    let imagePath = await RNEditorKitModule.exportImage(findNodeHandle(this.nativeRef.current));
    console.log("exportImage", videoPath);
  };

  onColorFilter = async () => {
    let videoPath = await RNEditorKitModule.setColorFilter(0, findNodeHandle(this.nativeRef.current));
    console.log("onColorFilter", videoPath);
  };



  componentDidMount() {
    //播放回调
    this.startVideoPlayListener = DeviceEventEmitter.addListener('startVideoEditor', (duration) => {
      // console.log("startVideoEditor", duration);
    });

    //合成回调
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
          videoPath="/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/Media//paiya-record.mp4"
        // imagePath="/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/Media//1634025894098-photo.jpg"
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
        {/* <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.corpVideoFrame()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
          </TouchableOpacity>
        </View> */}
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
