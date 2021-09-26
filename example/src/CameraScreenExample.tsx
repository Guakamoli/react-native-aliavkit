import React, { Component } from 'react';
import { Alert } from 'react-native';
import CameraScreen from '../../src/CameraScreen';

export default class CameraScreenExample extends Component {
  onBottomButtonPressed(event) {
    const captureImages = JSON.stringify(event.captureImages);
    Alert.alert(
      `"${event.type}" Button Pressed`,
      `${captureImages}`,
      [{ text: 'OK', onPress: () => console.log('OK Pressed') }],
      { cancelable: false },
    );
  }

  render() {
    return (
      <CameraScreen
        actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
        onBottomButtonPressed={(event) => this.onBottomButtonPressed(event)}
        flashImages={{
          on: require('../images/flashOn.png'),
          off: require('../images/flashOff.png'),
          auto: require('../images/flashAuto.png'),
        }}
        // 1
        cameraFlipImage={require('../images/cameraFlipIcon.png')}
        // 1
        captureButtonImage={require('../images/cameraButton.png')}
        torchOnImage={require('../images/torchOn.png')}
        torchOffImage={require('../images/torchOff.png')}
        closeImage={require('../images/close.png')}
        musicImage={require('../images/music.png')}
        beautifyImage={require('../images/beautify.png')}
        beautyAdjustImag={require('../images/beautyAdjust.png')}
        AaImage={require('../images/Aa.png')}
        filterImage={require('../images/filter.png')}
        GifImage={require('../images/gif.png')}
        giveUpImage={require('../images/giveUp.png')}
        noVolumeImage={require('../images/noVolume.png')}
        tailorImage={require('../images/tailor.png')}
        volumeImage={require('../images/volume.png')}
        showCapturedImageCount

      />
    );
  }
}

