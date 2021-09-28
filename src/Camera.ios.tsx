import * as _ from 'lodash';
import React from 'react';
import { requireNativeComponent, NativeModules, processColor, NativeAppEventEmitter } from 'react-native';

const { CKCameraManager } = NativeModules;
const NativeCamera = requireNativeComponent('CKCamera');

const Camera = React.forwardRef((props, ref) => {
  const nativeRef = React.useRef();

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
  }));

  const transformedProps = _.cloneDeep(props);
  _.update(transformedProps, 'cameraOptions.ratioOverlayColor', (c) => processColor(c));

  return (
    <NativeCamera
      style={{ minWidth: 100, minHeight: 100 }}
      ref={nativeRef}
      {...transformedProps}
      onRecordingProgress={(event) => props.onRecordingProgress(event.nativeEvent)}
    />
  );
});

Camera.defaultProps = {
  normalBeautyLevel: 30, 
  saveToCameraRoll: true,
};

export default Camera;
