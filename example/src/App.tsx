import React, { useEffect, useState, useRef } from 'react';

import {
  StyleSheet,
  View,
  Text,
  Image,
  TextInput,
  FlatList,
  Pressable,
  TouchableOpacity,
  Dimensions,
  Platform,
  Keyboard,
  Easing,
  Animated
} from 'react-native';

import { AVService } from 'react-native-aliavkit';

import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';

const Stack = createStackNavigator();

import PostPickerExample from './PostPickerExample';
import StoryPickerExample from './StoryPickerExample';
import VideoEditorExample from './VideoEditorExample';
import HeadPortraitScreen from './headPortrait';
import CropHeadPortrait from './headPortrait/CropHeadPortrait';
import CropImagePreview from './headPortrait/CropImagePreview';

import VideoWatermarkScreen from './watermark';
import DownloadWatermarkVideo from './watermark/DownloadWatermarkVideo';


import PlayerVideo from './watermark/PlayerVideo';


import CoverScreen from './cover';
import CoverSelect from './cover/CoverSelect';


export const isIOS = Platform.OS === 'ios';
export const isAndroid = !isIOS;
export default class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
    };
  }
  render() {
    const defaultHeader = {
      headerBackTitleVisible: false,
      cardOverlayEnabled: true,
      cardStyle: { backgroundColor: 'transparent' }
    };

    return (
      <NavigationContainer>
        <Stack.Navigator
          screenOptions={{ ...defaultHeader }}
        >
          <Stack.Screen
            name='HomeExample'
            component={HomeExample}
          />
          <Stack.Screen
            name='PostPickerExample'
            component={PostPickerExample}
          />
          <Stack.Screen
            name='StoryPickerExample'
            component={StoryPickerExample}
          />
          <Stack.Screen
            name='VideoEditorExample'
            component={VideoEditorExample}
          />
          <Stack.Screen
            name='HeadPortraitScreen'
            component={HeadPortraitScreen}
            options={{
              headerShown: false
            }}
          />
          <Stack.Screen
            name='CropHeadPortrait'
            component={CropHeadPortrait}
            options={{
              headerShown: false
            }}
          />
          <Stack.Screen
            name='CropImagePreview'
            component={CropImagePreview}
            options={{
              headerShown: false
            }}
          />
          <Stack.Screen
            name='VideoWatermarkScreen'
            component={VideoWatermarkScreen}
            options={{
              headerShown: false
            }}
          />
          <Stack.Screen
            name='DownloadWatermarkVideo'
            component={DownloadWatermarkVideo}
            options={{
              headerShown: false
            }}
          />
          <Stack.Screen
            name='PlayerVideo'
            component={PlayerVideo}
          />
          <Stack.Screen
            name='CoverScreen'
            component={CoverScreen}
            options={{
              headerShown: false
            }}
          />
          <Stack.Screen
            name='CoverSelect'
            component={CoverSelect}
            options={{
              headerShown: false
            }}
          />
        </Stack.Navigator>
      </NavigationContainer>


    );
  }
}

const HomeExample = (props) => {

  const { navigation } = props;

  useEffect(() => {
    return () => {
    };
  }, []);


  const onNavigation = async (screenName: string, data: Object = {}) => {
    navigation.navigate(screenName, data);
  };

  return (
    <View style={{ flex: 1 }}>
      <View style={styles.headerContainer}>
        <Text style={{ fontSize: 60 }}>🎈</Text>
        <Text style={styles.headerText}>
          React Native Camera Kit
        </Text>
      </View>
      <View style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={async () => {
          const isStorage = await AVService.checkStorage();
          console.info('checkStorage', isStorage);
          if(isStorage === 'denied'){
            const statuse = await AVService.getStorage();
            console.info('getStorage', statuse);
          }
        
        }}>
          <Text style={styles.buttonText}>
            Android 11 Permissions
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("PostPickerExample")}>
          <Text style={styles.buttonText}>
            Post Picker
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("StoryPickerExample")}>
          <Text style={styles.buttonText}>
            Story Picker
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("VideoEditorExample")}>
          <Text style={styles.buttonText}>
            Video Editor
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("HeadPortraitScreen")}>
          <Text style={styles.buttonText}>
            Head Portrait
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("VideoWatermarkScreen")}>
          <Text style={styles.buttonText}>
            Video Watermark
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("DownloadWatermarkVideo")}>
          <Text style={styles.buttonText}>
            Download Video Watermark
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("CoverScreen")}>
          <Text style={styles.buttonText}>
            Cover Screen
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  )
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
