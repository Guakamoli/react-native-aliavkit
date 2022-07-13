import React, { useEffect, useState, useRef } from 'react';

import { AVService } from 'react-native-aliavkit';

import { HeaderBackButton } from '@react-navigation/elements';


import { ReanimatedArcBase } from '@callstack/reanimated-arc';

import Reanimated from 'react-native-reanimated';

import RNFetchBlob from 'rn-fetch-blob'

import {
    StyleSheet,
    View,
    Text,
    TextInput,
    StatusBar,
    TouchableOpacity,
    Dimensions,
    Platform,
    SafeAreaView,
} from 'react-native';

const { width, height } = Dimensions.get('window');



const DownloadWatermarkVideo = (props) => {

    const { navigation } = props;

    const [isExport, setExport] = useState(false);

    const [exportProgress, setExportProgress] = useState(0);

    const videoUrl = 'https://video-message-001.paiyaapp.com/QJ2TEznSz97mGi8ip.mp4';
    const [downloadUrl, setDownloadUrl] = useState(videoUrl);


    const [watermarkText, setWatermarkText] = useState('REVOID:123456');

    const exportAngle = useRef(new Reanimated.Value(0));

    //下载进度比例
    const downloadProgressProportion = 0.2;


    const downloadVideo = async () => {
        setExport(true)
        setExportProgress(0)
        exportAngle?.current?.setValue(0);
        RNFetchBlob.config({
            fileCache: true,
            appendExt: 'mp4'
        })
            .fetch('GET', downloadUrl, {
                //some headers ..
            })
            .progress((received, total) => {
                const progress = received / total * 100 * downloadProgressProportion;
                console.log('downloadVideo progress', progress)
                setExportProgress(parseInt(progress))
            })
            .then((res) => {
                console.info('downloadVideo path: ', res.path())
                exportWaterMarkVideo(res.path())
            })
    }


    const exportWaterMarkVideo = async (videoPath) => {
        const path = await AVService.saveToSandBox('ph://0B35D98F-5C8B-4643-9356-06DF17951E7C/L0/001');
        const exportParam = {
            videoPath: videoPath,
            watermarkText: watermarkText,
            // watermarkImagePath: path
            // watermarkImagePath:'/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/cache/media/save/logo_video_watermark.png'
            // watermarkImagePath: "/private/var/containers/Bundle/Application/73BB0879-67BD-4409-9E2D-ADCB9B4D9703/CameraKitExample.app/AliKitPhotoView/ic_water_mark_logo.png"
        }

        const waterMarkVideoPath = await AVService.exportWaterMarkVideo(exportParam, (progress) => {
            progress = downloadProgressProportion + progress * (1 - downloadProgressProportion)
            //0~1
            console.info("onExportWaterMarkVideo progress:", progress * 100);
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
        <SafeAreaView style={{ flex: 1 }}>
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
                        downloadVideo();
                    }}>
                        <Text style={styles.textConfirm}>导出</Text>
                    </TouchableOpacity>
                </View>

                <View style={{
                    marginTop: 20,
                    marginStart: 20,
                    marginEnd: 20,
                    justifyContent: 'center',

                }}>
                    <Text style={{ color: '#000', fontSize: 15 }}>水印文字：</Text>
                    <TextInput
                        style={{
                            minHeight: 30,
                            marginTop: 10,
                            padding: 10,
                            backgroundColor: 'rgba(0,0,0,0.1)',
                            overflow: 'hidden',
                            borderRadius: 8,
                        }}
                        multiline={true}
                        textAlignVertical={'center'}
                        placeholder={'请输入文字水印'}
                        value={watermarkText}
                        onChange={(e) => {
                            if (e?.nativeEvent) {
                                const inputValue = e?.nativeEvent?.text?.trim()
                                setWatermarkText(inputValue)
                            }
                        }}
                    />
                    <Text style={{ color: '#000', fontSize: 15, marginTop: 40 }}>下载地址：</Text>
                    <TextInput
                        style={{
                            minHeight: 50,
                            marginTop: 10,
                            padding: 10,
                            backgroundColor: 'rgba(0,0,0,0.1)',
                            overflow: 'hidden',
                            borderRadius: 8,
                        }}
                        multiline={true}
                        textAlignVertical={'center'}
                        placeholder={'请输入视频下载地址'}
                        value={downloadUrl}
                        onChange={(e) => {
                            if (e?.nativeEvent) {
                                const inputValue = e?.nativeEvent?.text?.trim()
                                setDownloadUrl(inputValue)
                            }
                        }}
                    />

                </View>

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
        backgroundColor: '#fff',
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


export default DownloadWatermarkVideo
