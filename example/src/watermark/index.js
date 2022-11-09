import React, { useEffect, useState, useRef } from 'react';

import { AVKitPhotoView, EditorModule, SortModeEnum, AVService } from 'react-native-aliavkit';

import { HeaderBackButton } from '@react-navigation/elements';

import RNGetPermissions, { PermissionsResults } from '../permissions/RNGetPermissions';

import { ReanimatedArcBase } from '@callstack/reanimated-arc';

import Reanimated from 'react-native-reanimated';

import {
    StyleSheet,
    View,
    Text,
    StatusBar,
    Pressable,
    TouchableOpacity,
    Dimensions,
    Platform,
    SafeAreaView,
} from 'react-native';

const { width, height } = Dimensions.get('window');


const HeadPortraitScreen = (props) => {

    const { navigation } = props;

    const [isStoragePermission, setStoragePermission] = useState(false);
    const [isPhotoLimited, setPhotoLimited] = useState(false);

    const [isExport, setExport] = useState(false);

    const [exportProgress, setExportProgress] = useState(0);

    const exportAngle = useRef(new Reanimated.Value(0));

    var videoUri = '';

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
        videoUri = data[0].path;
        if (!!videoUri && videoUri.startsWith("file://")) {
            videoUri = videoUri.slice(7)
        }
        console.info("videoUri:", videoUri);
    };


    const exportWaterMarkVideo = async () => {
        setExport(true)
        setExportProgress(0)
        exportAngle?.current?.setValue(0);

        const exportParam = {
            videoPath: videoUri,
            watermarkText: "REVOID: 111222333",
            // watermarkImagePath: path
            // watermarkImagePath:'/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/cache/media/save/logo_video_watermark.png'
            // watermarkImagePath: "/private/var/containers/Bundle/Application/73BB0879-67BD-4409-9E2D-ADCB9B4D9703/CameraKitExample.app/AliKitPhotoView/ic_water_mark_logo.png"
        }

        const waterMarkVideoPath = await AVService.exportWaterMarkVideo(exportParam, (progress) => {
            //0~1
            console.info("onExportWaterMarkVideo progress:", progress);
            setExportProgress(parseInt(progress * 100))
            exportAngle?.current?.setValue(progress * 360);
        });

        console.info("exportWaterMarkVideo path:", waterMarkVideoPath);
        navigation.navigate('PlayerVideo', { videoUri: "file://" + waterMarkVideoPath });
        setTimeout(() => {
            setExport(false)
        }, 0);
    }


    const LoadingView = () => {
        return (
            <View style={{
                position: 'absolute',
                left: 0,
                right: 0,
                top: 0,
                bottom: 0,
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: 'rgba(0,0,0,0.5)'
            }}>
                <Reanimated.View
                    style={{
                        width: 150,
                        height: 150,
                        backgroundColor: 'rgba(100,100,100,1)',
                        alignItems: 'center',
                        justifyContent: 'center',
                        borderRadius: 15,
                    }}
                >
                    <ReanimatedArcBase
                        color='rgba(216,216,216,0.4)'
                        diameter={100}
                        width={5}
                        arcSweepAngle={360}
                        lineCap='round'
                        rotation={360}
                        hideSmallAngle={false}
                        style={{
                            position: 'absolute',
                        }}
                    />
                    <ReanimatedArcBase
                        color='#FFF'
                        diameter={100}
                        width={5}
                        arcSweepAngle={exportAngle?.current}
                        lineCap='round'
                        rotation={360}
                        hideSmallAngle={false}
                        style={{
                            position: 'absolute',
                        }}
                    />
                </Reanimated.View>
                <Text style={{ fontSize: 17, color: "#fff", position: 'absolute' }}>{exportProgress}%</Text>
            </View>
        )
    }

    return (
        <SafeAreaView style={{ flex: 1, backgroundColor: 'black' }}>
            <View style={styles.cameraContainer}>
                <StatusBar backgroundColor={"#000"} barStyle={'light-content'} animated />
                <View style={styles.continueHeadView} >
                    <HeaderBackButton
                        label=''
                        tintColor='#FFFFFF'
                        onPress={navigation.goBack}
                        style={{ left: Platform.OS === 'android' ? 0 : 8 }}
                    />
                    <Text style={styles.textCenter}>最近项目</Text>
                    <TouchableOpacity onPress={() => {
                        exportWaterMarkVideo();
                    }}>
                        <Text style={styles.textConfirm}>导出</Text>
                    </TouchableOpacity>
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
                        sortMode={SortModeEnum.SORT_MODE_VIDEO}
                        defaultSelectedPosition={-1}
                        onSelectedPhotoCallback={onSelectedPhotoCallback}
                        onMaxSelectCountCallback={() => { }}
                    />
                )}
                {isExport && LoadingView()}
            </View>
        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    continueHeadView: {
        height: 50,
        flexDirection: 'row',
        justifyContent: 'space-between',
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
    textConfirm: {
        fontSize: 17,
        lineHeight: 47,
        fontWeight: '500',
        color: '#8EF902',
        paddingHorizontal: 16,
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