import React, { useEffect, useState, useRef } from 'react';

import { AVService } from 'react-native-aliavkit';

import {
    StyleSheet,
    View,
    Text,
    TextInput,
    StatusBar,
    TouchableOpacity,
    Dimensions,
    Platform,
    Image,
    SafeAreaView,
} from 'react-native';

import Video from 'react-native-video';

const { width, height } = Dimensions.get('window');

const CoverSelect = (props) => {


    const { navigation } = props;

    //上传数据，数组
    const fileData = props.route.params?.fileData;

    const firstData = fileData[0];

    //上传类型是否是视频
    const isVideoType = firstData?.type?.includes('video') ? true : false;

    console.info("isVideoType", isVideoType)


    const [vidoePaused, setVideoPaused] = useState(false);

    const HeadView = () => {
        return (
            <View style={styles.continueHeadView}>
                <TouchableOpacity onPress={navigation.goBack}>
                    <Text style={styles.textClean}>取消</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={() => {
                    setStartCrop(true);
                    cropViewRef?.current?.saveImage(true, 90);
                }}>
                    <Text style={styles.textConfirm}>确认</Text>
                </TouchableOpacity>
            </View>
        )
    }

    return (
        <SafeAreaView style={{ flex: 1, backgroundColor: 'black' }}>
            <View style={styles.cropContainer}>
                {HeadView()}
                <View style={styles.contentView}>
                    {isVideoType ?
                        <Video
                            source={{ uri: Platform.OS === 'android' ? firstData.uri : firstData.path }}
                            muted
                            resizeMode='cover'
                            paused={Platform.OS === 'android' ? vidoePaused : true}
                            style={styles.fileContentView}
                            onLoadStart={(data) => {
                                if (Platform.OS === 'android') {
                                    setVideoPaused(false);
                                }
                            }}
                            onLoad={(data) => {
                                if (Platform.OS === 'android') {
                                    setVideoPaused(true);
                                }
                            }}
                        />
                        :
                        <Image style={styles.fileContentView} source={{ uri: firstData.uri }} resizeMode='cover' />
                    }





                </View>




            </View>
        </SafeAreaView >

    )
}

const styles = StyleSheet.create({
    contentView: {
        marginTop: 14,
        marginHorizontal: 50,
        width: width - 100,
        height: (width - 100) * 16 / 9,
        borderRadius: 4,
        overflow: 'hidden',
        backgroundColor: 'red'
    },
    fileContentView: {
        width: '100%',
        height: '100%'
    },
    continueHeadView: {
        width: width,
        height: 47,
        backgroundColor: '#000',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    textClean: {
        fontSize: 17,
        lineHeight: 47,
        color: '#fff',
        paddingHorizontal: 16,

    },
    textConfirm: {
        fontSize: 17,
        lineHeight: 47,
        fontWeight: '500',
        color: '#8EF902',
        paddingHorizontal: 16,
    },
    cropContainer: {
        width: width,
        height: height,
        backgroundColor: 'black'
    }
})

export default CoverSelect

