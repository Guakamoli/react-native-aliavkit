import React, { Component } from 'react';
import { Dimensions, View, StyleSheet } from 'react-native';
import AVKitPhotoView from '../../src/AVKitPhotoView';

const { width } = Dimensions.get('window');

const photoItemWidth = width / 3.0;
const photoItemHeight = photoItemWidth * 16 / 9;

export default class StoryPickerExample extends Component {
  render() {
    return (
      <View style={styles.cameraContainer}>
        <AVKitPhotoView {...this.props}
          style={{ ...StyleSheet.absoluteFill, backgroundColor: 'black' }}
          // itemWidth={photoItemWidth}
          // itemHeight={photoItemHeight}
          multiSelect={false}
          numColumns={3}
          pageSize={90}
          defaultSelectedPosition={-1}
          onSelectedPhotoCallback={() => { }}
          onMaxSelectCountCallback={() => { }}
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
