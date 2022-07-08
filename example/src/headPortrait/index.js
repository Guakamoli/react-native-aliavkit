import React, { useEffect, useState, useRef } from 'react';

import { AVKitPhotoView, PhotoModule, SortModeEnum } from 'react-native-aliavkit';

import { HeaderBackButton } from '@react-navigation/elements';

import RNGetPermissions, { PermissionsResults } from '../permissions/RNGetPermissions';


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
    SafeAreaView
} from 'react-native';

const { width, height } = Dimensions.get('window');


const HeadPortraitScreen = (props) => {

    const { navigation } = props;

    const [isStoragePermission, setStoragePermission] = useState(false);
    const [isPhotoLimited, setPhotoLimited] = useState(false);

    useEffect(() => {
        getPhotos();
        return () => {
        };
    }, []);

    const getPhotos = async () => {
        const storagePermission = await RNGetPermissions.checkStoragePermissions();
        setPhotoLimited(storagePermission?.permissionStatus === PermissionsResults.LIMITED);
        if (!storagePermission?.isGranted) {
            await new Promise((resolved) => {
                setTimeout(() => {
                    resolved()
                }, 300);
            })
            if (await RNGetPermissions.getStoragePermissions(true)) {
                setStoragePermission(true);
            }
            return;
        }
        setStoragePermission(true);
    };

    const onSelectedPhotoCallback = ({ data }) => {
        navigation.navigate("CropHeadPortrait", { imageUri: data[0].uri });
    };

    return (
        <SafeAreaView style={{ flex: 1 }}>
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

                {isPhotoLimited && (
                    <View style={styles.photoLimitedContainer}>
                        <Text style={styles.photoLimitedText}>{'点击“'}</Text>
                        <Pressable onPress={RNGetPermissions.openSettings}>
                            <Text style={[styles.photoLimitedText, { color: '#8EF902' }]}>{'去设置'}</Text>
                        </Pressable>
                        <Text style={styles.photoLimitedText}>{'”切换至允许访问所有照片。'}</Text>
                    </View>
                )}

                {isStoragePermission && (
                    <AVKitPhotoView
                        style={{ width: width, height: height - (isPhotoLimited ? 52 : 0), backgroundColor: 'black' }}
                        multiSelect={false}
                        numColumns={3}
                        pageSize={90}
                        sortMode={SortModeEnum.SORT_MODE_PHOTO}
                        defaultSelectedPosition={-1}
                        onSelectedPhotoCallback={onSelectedPhotoCallback}
                        onMaxSelectCountCallback={() => { }}
                    />
                )}
            </View>
        </SafeAreaView>
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
    photoLimitedContainer: {
        width,
        height: 52,
        backgroundColor: '#262626',
        paddingHorizontal: 16,
        flexDirection: 'row'
    },
    photoLimitedText: {
        textAlign: 'left',
        color: '#929292',
        lineHeight: 52,
        height: 52
    }
})

export default HeadPortraitScreen