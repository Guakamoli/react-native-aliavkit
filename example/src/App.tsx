import React from 'react';
import { StyleSheet, Text, View, TouchableOpacity, Button } from 'react-native';

import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import CameraScreenExample from './CameraScreenExample';
import CameraExample from './CameraExample';
import VideoEditorExample from './VideoEditorExample';
import Example from './trimmer';
import StoryMusic from '../../src/StoryMusic';

import CameraScreen from '../../src/CameraScreen';
import PostUpload from '../../src/PostScreen';
import PostEditor from '../../src/PostEditor';
import Entry from '../../src/Entry'
import store from '../../src/store';
import { Provider } from 'react-redux';

import EntryTest from '../../src/EntryTest'

import ImageMap from '../../images/index';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

function HomeScreen(props) {
  const { server, user, item, navigation, initType } = props;
  const insets = useSafeAreaInsets()
  const sendfile = async (data) => {
    try {
      console.log(data)
    } catch (e) {
      console.log(e);
    }

  }
  const goBack = () => {
    navigation.goBack()

  }
  return (

    <>
      <EntryTest {...props} goBack={goBack} {...ImageMap} sendfile={sendfile}
       insets={insets}
        getUploadFile={(data) => {
          navigation.navigate('FeedsPublishView', { 'attachments': data, type: data[0].Type.split('/')[0], })
        }}></EntryTest>

    </>
    // </View>
  );
  return (
    <View style={{ flex: 1 }}>
      <View style={styles.headerContainer}>
        <Text style={{ fontSize: 60 }}>🎈</Text>
        <Text style={styles.headerText}>React Native Camera Kit</Text>
      </View>
      <View style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('storyPost')}>
          <Text style={styles.buttonText}>storyUpload</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('postUpload')}>
          <Text style={styles.buttonText}>postUpload</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

function Camera({ navigation }) {
  return <CameraExample />;
}

function VideoEditor({ navigation }) {
  React.useLayoutEffect(() => {
    navigation.setOptions({
      headerTitle: 'Camera',
      headerRight: () => <Button title='Home' onPress={() => navigation.navigate('Home')} />,
    });
  }, [navigation]);

  return <VideoEditorExample />;
}

function Story(props) {
  return (
    <CameraScreen
      {...props}
      // 退出操作
      goback={() => {
        console.log(12313);
        props.navigation.navigate('Home');
      }}
      // 拿到上传数据
      getUploadFile={(data) => {
        console.log('getUploadFileStory-----------', data);
      }}
      // story 跳转post 路由
      goPost={() => {
        props.navigation.push('postUpload');
      }}
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
      videomusicIcon={require('../images/videomusicIcon.png')}
      giveUpImage={require('../images/giveUp.png')}
      tailorImage={require('../images/tailor.png')}
      noVolumeImage={require('../images/noVolume.png')}
      volumeImage={require('../images/volume.png')}
      musicDynamicGif={require('../images/musicDynamic.gif')}
      musicIconPng={require('../images/musicIcon.png')}
      musicIcongray={require('../images/musicIcongray.png')}
      musicSearch={require('../images/musicSearch.png')}
      selectBeautify={require('../images/selectBeautify.png')}
      noResultPng={require('../images/noResult.png')}
      zoomMode='on'
      showCapturedImageCount
      cameraModule={true}
    />
  );
}

function Post(props) {
  // console.info(props, 'ahshash');
  const { server, user, item, navigation } = props;
  return (
    <PostUpload
      // refs={postRef}
      {...props}
      //  退出操作
      goback={() => {
        // 回退操作
        console.info('1111', props.navigation);
        props.navigation.replace('Home');
      }}
      goStory={() => {
        props.navigation.replace('storyPost');
      }}
      goPostEditor={(data) => {
        console.log('postUploadpostUploadpostUpload', data);
        props.navigation.push('PostEditorBox', { ...data });
      }}
      // 拿到上传数据
      getUploadFile={(data) => {
        console.log('getUploadFile-----------', data);
      }}
      multipleBtnImage={require('../images/multipleBtn.png')}
      startMultipleBtnImage={require('../images/startMultipleBtn.png')}
      postCameraImage={require('../images/postCamera.png')}
      changeSizeImage={require('../images/changeSize.png')}
      closePng={require('../images/close.png')}
    />
  );
}

function PostEditorBox(props) {
  // console.log(1111,props);

  return (
    <>
      <PostEditor
        {...props}
        actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
        // 退出操作
        // goback={
        //   () => {

        //     navigation.goBack(-1)
        //   }

        // }
        goPostUpload={() => {
          // navigation.push('')
        }}
        // 拿到上传数据
        getUploadFile={(data) => {
          console.log('PostEditor-----getUploadFile', data);
        }}
        noVolumeImage={require('../images/noVolume.png')}
        volumeImage={require('../images/volume.png')}
        noResultPng={require('../images/noResult.png')}
        cameraModule={true}
      />
    </>
  );
}

const Stack = createNativeStackNavigator();

export default function App() {

  return (
    <Provider store={store}>
      <NavigationContainer>
        <Stack.Navigator>
          <Stack.Screen name='Home' component={HomeScreen} options={{ headerShown: false }} />
        </Stack.Navigator>
      </NavigationContainer>
    </Provider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 30,
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
    marginHorizontal: 24,
  },
  headerContainer: {
    flexDirection: 'column',
    backgroundColor: '#F5FCFF',
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: 100,
  },
  headerText: {
    color: 'black',
    fontSize: 24,
    fontWeight: 'bold',
  },
  button: {
    height: 60,
    borderRadius: 30,
    marginVertical: 12,
    width: '100%',
    backgroundColor: '#dddddd',
    justifyContent: 'center',
  },
  buttonText: {
    textAlign: 'center',
    fontSize: 20,
  },
});
