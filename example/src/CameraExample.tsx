import React, { FC } from 'react';
import { View, StyleSheet } from 'react-native';
import Camera from '../../src/Camera';
import { CameraType } from '../../src/CameraScreen';

const recordConfig = {
  minDuration: 2.0,
  maxDuration: 15.0,
  gop: 30,
  fps: 30,
  bitrate: 15000000,
  videoCodec: 'h264',
  cutMode: 0,
  videoOnly: true,
  backgroundColor: '#000000',
  videoQuality: 'High',
  videoRotate: 0,
  deleteVideoClipOnExit: true,
  resolution: {
    width: 1280,
    height: 720,
  },
  useFaceDetect: true,
  faceDetectCount: 2,
  faceDectectSync: true,
  beautifyStatus: true,
  videoFlipH: false,
};

export const CameraExample: FC = () => {
  return (
    <View style={styles.cameraContainer}>
      <Camera
        ref={this.camera}
        style={{ flex: 1 }}
        recordConfig={recordConfig} // require
        cameraType={CameraType.Back} // optional
        flashMode='auto' // on/off/auto(default)
        focusMode='on' // off/on(default)
        zoomMode='on' // off/on(default)
        torchMode='off' // on/off(default)
        ratioOverlay='1:1' // optional
        ratioOverlayColor='#00000077' // optional
        saveToCameraRole={false} // iOS only
      />
    </View>
  );
};

const styles = StyleSheet.create({
  cameraContainer: {
    flex: 1,
    backgroundColor: 'black',
  },
});
