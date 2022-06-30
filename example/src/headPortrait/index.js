import React, { useEffect, useState, useRef } from 'react';

import { AVKitPhotoView, PhotoModule, SortModeEnum } from 'react-native-aliavkit';

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
} from 'react-native';



const { width, height } = Dimensions.get('window');


const HeadPortraitScreenExample = (props) => {


    useEffect(() => () => {

    }, []);


    const onSelectedPhotoCallback = (data) => {
        console.info("onSelectedPhotoCallback", data?.data)
    };

    return (
        <View style={styles.cameraContainer}>
            <AVKitPhotoView
                style={{ flex: 1, backgroundColor: 'black' }}
                multiSelect={false}
                numColumns={3}
                pageSize={90}
                sortMode={SortModeEnum.SORT_MODE_PHOTO}
                defaultSelectedPosition={-1}
                onSelectedPhotoCallback={onSelectedPhotoCallback}
                onMaxSelectCountCallback={() => { }}
            ></AVKitPhotoView>
        </View>
    )
}

const styles = StyleSheet.create({
    cameraContainer: {
        flex: 1,
        backgroundColor: 'black',
    },

})

export default HeadPortraitScreenExample