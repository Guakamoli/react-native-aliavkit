import React, { Component } from 'react';
import { View, StyleSheet } from 'react-native';
import AVKitPhotoView from '../../src/AVKitPhotoView';

export default class PostPickerExample extends Component {
  render() {
    return (
      <View style={styles.cameraContainer}>
        <AVKitPhotoView {...this.props}
          style={{ ...StyleSheet.absoluteFill, backgroundColor: 'black' }}
          multiSelect={true}
          numColumns={3}
          pageSize={90}
          defaultSelectedPosition={-1}
          onSelectedPhotoCallback={() => {}}
          onMaxSelectCountCallback={() => {}}
        ></AVKitPhotoView>
      </View>
    );
  }
}

const styles = StyleSheet.create(
  {
    cameraContainer: {
      flex: 1,
      backgroundColor: 'black',
    },
  },
);
