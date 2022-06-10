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
          numColumns={3}
          itemWidth={photoItemWidth}
          itemHeight={photoItemHeight}
          multiSelect={false}
          defaultSelectedStatus={true}
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
