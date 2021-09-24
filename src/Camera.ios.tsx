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

  return <NativeCamera style={{ minWidth: 100, minHeight: 100 }} ref={nativeRef} {...transformedProps} />;
});

Camera.defaultProps = {
  // resetFocusTimeout: 0,
  // resetFocusWhenMotionDetected: true,
  saveToCameraRoll: true,
};

export default Camera;
