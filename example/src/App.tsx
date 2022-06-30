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

import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';

const Stack = createStackNavigator();

import PostPickerExample from './PostPickerExample';
import StoryPickerExample from './StoryPickerExample';
import VideoEditorExample from './VideoEditorExample';
import HeadPortraitScreenExample from './headPortrait';

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
            name='HeadPortraitScreenExample'
            component={HeadPortraitScreenExample}
          />
        </Stack.Navigator>
      </NavigationContainer>


    );
  }
}

const HomeExample = (props) => {

  const { navigation } = props;

  useEffect(() => () => {

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
        <TouchableOpacity style={styles.button} onPress={() => onNavigation("HeadPortraitScreenExample")}>
          <Text style={styles.buttonText}>
            Head Portrait
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
