import * as React from 'react';
import { View, Text, Button, Image, TouchableOpacity, Pressable } from 'react-native';
import { NavigationContainer, useNavigation } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import CameraScreen from './CameraScreen';
import PostUpload from './PostUpload';
import PostEditor from './PostEditor'
// export { useNavigation, useIsFocused } from '@react-navigation/native';
// closeImage={}

function Post({ navigation }) {
  return (

    <PostUpload
      navigation={navigation}
      //  退出操作
      goback={() => {
        console.log(12313);
      }}
      // 拿到上传数据
      getUploadFile={(data) => { console.log('getUploadFile-----------', data); }}
      multipleBtnImage={require('../images/multipleBtn.png')}
      startMultipleBtnImage={require('../images/startMultipleBtn.png')}
      postCameraImage={require('../images/postCamera.png')}
      changeSizeImage={require('../images/changeSize.png')}
      addPhotoBtnPng={require('../images/addPhotoBtn.png')}
      postMutePng={require('../images/postEditorMute.png')}
      postNoMutePng={require('../images/postEditorNoMute.png')}
      captureButtonImage={require('../images/cameraButton.png')}
      cameraModule={true}
    />
  );
}

function PostEditorBox(props) {
  // console.log(1111,props);

  return (

    <PostEditor {...props} uploadFile={(data) => { console.log('uploadfile------s', data); }}
      volume={require('../images/volume.png')}
      noVolume={require('../images/noVolume.png')}
    />
  )
}
function Story(props) {
  return (
    <CameraScreen
      {...props}
      // navigation={navigation}
      // actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
      // onBottomButtonPressed={(event) => this.onBottomButtonPressed(event)}
      // 退出操作
      goback={() => {
        console.log(12313);
      }}
      // 拿到上传数据
      getUploadFile={(data) => { console.log('getUploadFilesss-----------', data); }}
      // 1
      cameraFlipImage={require('../images/cameraFlipIcon.png')}
      captureButtonImage={require('../images/cameraButton.png')}
      torchOnImage={require('../images/torchOn.png')}
      torchOffImage={require('../images/torchOff.png')}
      closeImage={require('../images/close.png')}
      musicImage={require('../images/music.png')}
      beautifyImage={require('../images/beautify.png')}
      beautyAdjustImag={require('../images/beautyAdjust.png')}
      AaImage={require('../images/Aa.png')}
      filterImage={require('../images/filter.png')}
      musicRevampImage={require('../images/musicRevamp.png')}
      giveUpImage={require('../images/giveUp.png')}
      noVolumeImage={require('../images/noVolume.png')}
      tailorImage={require('../images/tailor.png')}
      volumeImage={require('../images/volume.png')}

      musicDynamicGif={require('../images/musicDynamic.gif')}
      musicIconPng={require('../images/musicIcon.png')}
      showCapturedImageCount
      cameraModule={true}
    />
  )
}
const Stack = createNativeStackNavigator();

function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Home" component={Story} />
        <Stack.Screen name="PostUpload" component={Post} />
        <Stack.Screen name="PostEditorBox" component={PostEditorBox} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default App;
