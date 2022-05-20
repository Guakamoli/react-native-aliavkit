import React, { Component } from 'react';
import { Alert } from 'react-native';
import CameraScreen from '../../src/CameraScreen';
import PostUpload from '../../src/PostUpload'
import App from "../../src/App";
export default class CameraScreenExample extends Component {
  onBottomButtonPressed(event) {
    const captureImages = JSON.stringify(event.captureImages);
    Alert.alert(
      `"${event.type}" Button Pressed`,
      `${captureImages}`,
      [{ text: 'OK', onPress: () => {} }],
      { cancelable: false },
    );
  }

  render() {
    return (

      <App />

      // <CameraScreen
      //     actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
      //     onBottomButtonPressed={(event) => this.onBottomButtonPressed(event)}
      //     // 退出操作
      //     goback={() => {
      //      
      //     }}
      //     // 拿到上传数据
      //     getUploadFile={(data) => {}}
      //     // 1
      //     cameraFlipImage={require('../images/cameraFlipIcon.png')}
      //     captureButtonImage={require('../images/cameraButton.png')}
      //     torchOnImage={require('../images/torchOn.png')}
      //     torchOffImage={require('../images/torchOff.png')}
      //     closeImage={require('../images/close.png')}
      //     musicImage={require('../images/music.png')}
      //     beautifyImage={require('../images/beautify.png')}
      //     beautyAdjustImag={require('../images/beautyAdjust.png')}
      //     AaImage={require('../images/Aa.png')}
      //     filterImage={require('../images/filter.png')}
      //     musicRevampImage={require('../images/musicRevamp.png')}
      //     giveUpImage={require('../images/giveUp.png')}
      //     noVolumeImage={require('../images/noVolume.png')}
      //     tailorImage={require('../images/tailor.png')}
      //     volumeImage={require('../images/volume.png')}
      //     multipleBtnImage={require('../images/multipleBtn.png')}
      //     startMultipleBtnImage={require('../images/startMultipleBtn.png')}
      //     postCameraImage={require('../images/postCamera.png')}
      //     changeSizeImage={require('../images/changeSize.png')}
      //     addPhotoBtnPng ={require('../images/addPhotoBtn.png')}
      //     postMutePng={require('../images/postEditorMute.png')}
      //     postNoMutePng={require('../images/postEditorNoMute.png')}
      //     musicDynamicGif = {require('../images/musicDynamic.gif')}
      //     musicIconPng = {require('../images/musicIcon.png')}
      //     showCapturedImageCount
      //     cameraModule={true}
      //   />
      // <PostUpload
      // // 退出操作
      //     actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
      //   onBottomButtonPressed={(event) => this.onBottomButtonPressed(event)}
      //   // 退出操作
      //   goback={() => {
      //    
      //   }}
      //   // 拿到上传数据
      //   getUploadFile={(data) => {}}
      //   // 1
      //   cameraFlipImage={require('../images/cameraFlipIcon.png')}
      //   captureButtonImage={require('../images/cameraButton.png')}
      //   torchOnImage={require('../images/torchOn.png')}
      //   torchOffImage={require('../images/torchOff.png')}
      //   closeImage={require('../images/close.png')}
      //   musicImage={require('../images/music.png')}
      //   beautifyImage={require('../images/beautify.png')}
      //   beautyAdjustImag={require('../images/beautyAdjust.png')}
      //   AaImage={require('../images/Aa.png')}
      //   filterImage={require('../images/filter.png')}
      //   musicRevampImage={require('../images/musicRevamp.png')}
      //   giveUpImage={require('../images/giveUp.png')}
      //   noVolumeImage={require('../images/noVolume.png')}
      //   tailorImage={require('../images/tailor.png')}
      //   volumeImage={require('../images/volume.png')}
      //   multipleBtnImage={require('../images/multipleBtn.png')}
      //   startMultipleBtnImage={require('../images/startMultipleBtn.png')}
      //   postCameraImage={require('../images/postCamera.png')}
      //   changeSizeImage={require('../images/changeSize.png')}
      //   showCapturedImageCount
      //   cameraModule={true}
      // />
    );
  }
}

