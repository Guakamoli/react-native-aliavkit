import React, { Component } from 'react';
import { requireNativeComponent, findNodeHandle, NativeModules, processColor, DeviceEventEmitter } from 'react-native';

const { RNCameraKitModule } = NativeModules;
const NativeCamera = requireNativeComponent('CKCameraManager');


export type Props = {
}
type State = {
};
export default class Camera extends Component<Props, State> {
  constructor(props) {
    super(props)
    this.state = {

    }
  };

  startRecording = async () => {
    return await RNCameraKitModule.startRecording();
  }

  stopRecording = async () => {
    return await RNCameraKitModule.stopRecording();
  }

  //拍照
  capture = async () => {
    return await RNCameraKitModule.capture();
  }

  //释放资源，退出页面时调用
  release = async () => {
    RNCameraKitModule.release();
  }

  render() {
    return (
      <NativeCamera
        style={{ minWidth: 100, minHeight: 100 }}
        {...this.props}
      />
    );
  }
}
