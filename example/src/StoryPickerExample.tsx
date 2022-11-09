import React, { useCallback, useRef } from 'react';
import { Dimensions, View, StyleSheet } from 'react-native';
import AVKitPhotoView from '../../src/AVKitPhotoView';

const { width } = Dimensions.get('window');

const photoItemWidth = width / 3.0;
const photoItemHeight = photoItemWidth * 16 / 9;

export default function StoryPickerExample() {
  const ref = useRef(null);

  const handleSelectedPhotoCallback = useCallback((event) => {
    console.info("handleSelectedPhotoCallback", event);

    if (event.data?.length > 0) {
      setTimeout(() => {
        ref.current?.uncheckPhoto({ index: 0 });
      }, 2000);
    }
  }, [ref]);

  return (
    <View style={styles.cameraContainer}>
      <AVKitPhotoView {...this.props}
        ref={ref}
        style={{ flex: 1, backgroundColor: 'black' }}
        // itemWidth={photoItemWidth}
        // itemHeight={photoItemHeight}
        multiSelect={false}
        numColumns={3}
        pageSize={90}
        defaultSelectedPosition={0}
        onSelectedPhotoCallback={handleSelectedPhotoCallback}
        onMaxSelectCountCallback={() => { }}
      ></AVKitPhotoView>
    </View>
  );
}

const styles = StyleSheet.create(
  {
    cameraContainer: {
      flex: 1,
      backgroundColor: 'black',
    },
  },
);
