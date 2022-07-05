import React, { useEffect, useState, useRef } from 'react';

import { AVKitPhotoView, PhotoModule, SortModeEnum } from 'react-native-aliavkit';

import { HeaderBackButton } from '@react-navigation/elements';

import {
    StyleSheet,
    View,
    Text,
    Image,
    TextInput,
    FlatList,
    StatusBar,
    Pressable,
    TouchableOpacity,
    Dimensions,
    Platform,
    Keyboard,
} from 'react-native';



const { width, height } = Dimensions.get('window');


const HeadPortraitScreen = (props) => {

    const { navigation } = props;

    useEffect(() => () => {

    }, []);

    const onSelectedPhotoCallback = ({ data }) => {
        navigation.navigate("CropHeadPortrait", { imageUri: data[0].uri });
    };

    return (
        <View style={styles.cameraContainer}>
            <StatusBar backgroundColor={"#000"} barStyle={'light-content'} animated />
            <View style={styles.continueHeadView} >
                <HeaderBackButton
                    label=''
                    tintColor='#FFFFFF'
                    onPress={navigation.goBack}
                    style={{ position: 'absolute', left: Platform.OS === 'android' ? 0 : 8 }}
                />
                <Text style={styles.textCenter}>最近项目</Text>
            </View>

            <AVKitPhotoView
                style={{ width: width, height: height, backgroundColor: 'black' }}
                multiSelect={false}
                numColumns={3}
                pageSize={90}
                sortMode={SortModeEnum.SORT_MODE_PHOTO}
                defaultSelectedPosition={-1}
                onSelectedPhotoCallback={onSelectedPhotoCallback}
                onMaxSelectCountCallback={() => { }}
            />
        </View>
    )
}

const styles = StyleSheet.create({
    continueHeadView: {
        height: 50,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#000',
        position: 'relative',
    },
    textCenter: {
        fontSize: 17,
        fontWeight: '500',
        color: '#fff',
        lineHeight: 24,
    },
    cameraContainer: {
        flex: 1,
        backgroundColor: 'black',
    },
})

export default HeadPortraitScreen