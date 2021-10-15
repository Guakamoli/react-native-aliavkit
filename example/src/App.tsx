import React from 'react';
import { StyleSheet, Text, View, TouchableOpacity, Button } from 'react-native';

import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import CameraScreenExample from './CameraScreenExample';
import CameraExample from './CameraExample';
import VideoEditorExample from './VideoEditorExample';
import Example from './trimmer'
import StoryMusic from '../../src/StoryMusic'
function HomeScreen({ navigation, route }) {
  return (
    <View style={{ flex: 1 }}>
      <View style={styles.headerContainer}>
        <Text style={{ fontSize: 60 }}>🎈</Text>
        <Text style={styles.headerText}>React Native Camera Kit</Text>
      </View>
      <View style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('Camera')}>
          <Text style={styles.buttonText}>Camera</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('CameraScreen')}>
          <Text style={styles.buttonText}>Camera Screen</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => navigation.navigate('VideoEditor')}
        >
          <Text style={styles.buttonText}>Video Play</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.button}
          onPress={() => navigation.navigate('aa')}
        >
          <Text style={styles.buttonText}>aa</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

function CameraScreen({ navigation }) {
  React.useLayoutEffect(() => {
    navigation.setOptions({
      headerTitle: 'Camera',
      headerRight: () => <Button title='play' onPress={() => navigation.navigate('VideoEditor')} />,
    });
  }, [navigation]);

  return <CameraScreenExample />;
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
function aa (){
  return <StoryMusic />
}
const Stack = createNativeStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name='Home' component={HomeScreen} options={{ headerShown: false }} />
        <Stack.Screen name='Camera' component={Camera} />
        <Stack.Screen name='CameraScreen' component={CameraScreen} />
        <Stack.Screen name='VideoEditor' component={VideoEditor} />
        <Stack.Screen name='aa' component={aa} />
      </Stack.Navigator>
    </NavigationContainer>
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
