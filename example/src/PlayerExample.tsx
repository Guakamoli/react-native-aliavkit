import React, { Component } from 'react';
import { View, StyleSheet } from 'react-native';
import Player from '../../src/Player';


export default class CameraExample extends Component {
  render() {
    return (
      <View style={styles.cameraContainer}>
        <Player
          style={{ flex: 1, minWidth:100, minHeight:100, alignItems: 'center', justifyContent: 'center' }}
          
        />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  cameraContainer: {
    flex: 1,
    backgroundColor: 'black',
  },
});
