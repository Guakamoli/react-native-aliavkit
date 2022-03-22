import React, { Component } from 'react';
import { requireNativeComponent, findNodeHandle, NativeModules, processColor, DeviceEventEmitter } from 'react-native';

const { RNCameraKitModule } = NativeModules;
const NativeCamera = requireNativeComponent('CKCameraManager');


export type Props = {
}
type State = {
};
export default class Camera extends Component<Props, State> {

  private startMultiRecordingListener?: any

  constructor(props) {
    super(props)
    this.state = {
    }
  };

  resumeCamera = async () => {
    return await RNCameraKitModule.resumeCamera();
  }

  pauseCamera = async () => {
    return await RNCameraKitModule.pauseCamera();
  }

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

  //开启多段录制（录制一个片段）
  startMultiRecording = async (recordingListener: (duration: number) => void) => {
    this.startMultiRecordingListener = DeviceEventEmitter.addListener('startMultiRecording', (duration) => {
      //0~1
      if (recordingListener) {
        recordingListener(duration);
      }
    });
    return await RNCameraKitModule.startMultiRecording();
  }
  //停止多段录制（停止一个片段）
  stopMultiRecording = async () => {
    this.startMultiRecordingListener?.remove();
    return await RNCameraKitModule.stopMultiRecording();
  }

  //合成：结束录制多段视频合成一个视频
  finishMultiRecording = async () => {
    return await RNCameraKitModule.finishMultiRecording();
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
