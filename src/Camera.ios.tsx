import * as _ from 'lodash';
import React from 'react';
import { requireNativeComponent, NativeModules, processColor, NativeAppEventEmitter, UIManager } from 'react-native';
import AVService from './AVService';

const { CKCameraManager } = NativeModules;
const NativeCamera = requireNativeComponent('CKCamera');

const Camera = React.forwardRef((props, ref) => {
  React.useImperativeHandle(ref, () => ({
    capture: async () => {
      return await CKCameraManager.capture({});
    },
    startRecording: async () => {
      return await CKCameraManager.startRecording({});
    },
    stopRecording: async () => {
      return await CKCameraManager.stopRecording({});
    },
    requestDeviceCameraAuthorization: async () => {
      return await CKCameraManager.checkDeviceCameraAuthorizationStatus();
    },
    checkDeviceCameraAuthorizationStatus: async () => {
      return await CKCameraManager.checkDeviceCameraAuthorizationStatus();
    },
    //获取服务器端的贴纸
    getPasterInfos: async () => {
      return await AVService.getFacePasterInfos({});
    },
    setPasterInfo: async (data) => {
      return await CKCameraManager.setPasterInfo(data);
    },
    //停止camera feed
    cameraStopPreview: () => {
      return CKCameraManager.cameraStopPreview();
    },
  }));

  React.useEffect(() => {
    const subscription = NativeAppEventEmitter.addListener('startVideoRecord', ({ duration }) => {
      //{ target: 65, duration: 5.769999980926514 }
      // console.log('---- recordProgress: ', duration);
    });
    return () => {
      subscription.remove();
    };
  }, []);

  const transformedProps = _.cloneDeep(props);
  _.update(transformedProps, 'cameraOptions.ratioOverlayColor', (c) => processColor(c));
  // const nativeRef = React.useRef();
  return (
    <NativeCamera
      style={{ minWidth: 100, minHeight: 500 }}
      ref={ref}
      cameraStyle={props.cameraStyle}
      {...transformedProps}
      onRecordingProgress={(event) => props.onRecordingProgress(event.nativeEvent)}
    />
  );
});

Camera.defaultProps = {
  normalBeautyLevel: 30,
  saveToCameraRoll: true,
  saveToCameraRollWithPhUrl: true,
};

export default Camera;
