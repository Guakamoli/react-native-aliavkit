import React, { useCallback, useRef } from 'react';
import { View, StyleSheet } from 'react-native';
import AVKitPhotoView from '../../src/AVKitPhotoView';

export default function PostPickerExample() {
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
        multiSelect={true}
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
