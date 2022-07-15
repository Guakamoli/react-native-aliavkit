import React, { useEffect, useState, useRef } from 'react';

import { AVService } from 'react-native-aliavkit';

import {
    StyleSheet,
    View,
    Text,
    FlatList,
    TextInput,
    StatusBar,
    Pressable,
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

    const getCoverImage = props.route.params?.getCoverImage;

    const firstData = fileData[0];

    //上传类型是否是视频
    const isVideoType = firstData?.type?.includes('video') ? true : false;

    //视频播放
    const [vidoePaused, setVideoPaused] = useState(false);

    //视频封面列表
    const [coverList, setCoverList] = useState([]);

    //选中的视频封面
    const [videoCover, setVideoCover] = useState('');

    //图片数组
    const [imageSelectedPosition, setImageSelectedPosition] = useState(0);


    useEffect(() => {
        if (isVideoType) {
            getVideoFrames();
        }
        return () => {
        };
    }, []);

    /**
     * 获取视频帧
     */
    const getVideoFrames = async () => {
        const framesCount = 15;

        const videoDuration = firstData.playableDuration;
        let videoPath = firstData.path;
        if (!!videoPath && videoPath.startsWith("file://")) {
            videoPath = videoPath.slice(7)
        }
        const itemPerTime = parseInt(videoDuration / framesCount);
        const startTime = parseInt(itemPerTime / 2);
        const imageSize = { width: firstData.width, height: firstData.height };
        const framesParam = { videoPath, startTime, itemPerTime, needCover: false, imageSize }
        const thumbnails = await AVService.getThumbnails(framesParam);

        console.info("thumbnails", thumbnails.length);

        setCoverList(thumbnails);
    }


    const HeadView = () => {
        return (
            <View style={styles.continueHeadView}>
                <TouchableOpacity onPress={navigation.goBack}>
                    <Text style={styles.textClean}>取消</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={() => {
                    let coverParh = '';

                    if (isVideoType) {
                        coverParh = videoCover;
                    } else {
                        coverParh = fileData[imageSelectedPosition].uri
                    }
                    if (!!getCoverImage) {
                        getCoverImage(coverParh);
                    }
                    navigation.goBack();
                }}>
                    <Text style={styles.textConfirm}>确认</Text>
                </TouchableOpacity>
            </View>
        )
    }

    const ContentView = () => {
        return (
            <View style={styles.contentView}>
                {isVideoType ?
                    // <Video
                    //     source={{ uri: Platform.OS === 'android' ? firstData.uri : firstData.path }}
                    //     muted
                    //     resizeMode='cover'
                    //     paused={Platform.OS === 'android' ? vidoePaused : true}
                    //     style={styles.fileContentView}
                    //     onLoadStart={(data) => {
                    //         if (Platform.OS === 'android') {
                    //             setVideoPaused(false);
                    //         }
                    //     }}
                    //     onLoad={(data) => {
                    //         if (Platform.OS === 'android') {
                    //             setVideoPaused(true);
                    //         }
                    //     }}
                    // />
                    <Image
                        style={styles.fileContentView}
                        source={{ uri: fileData[imageSelectedPosition].uri }}
                        resizeMode='cover'
                    />
                    :
                    <Image
                        style={styles.fileContentView}
                        source={{ uri: fileData[imageSelectedPosition].uri }}
                        resizeMode='cover'
                    />
                }
            </View>
        )
    }

    return (
        <SafeAreaView style={{ flex: 1, backgroundColor: 'black' }}>
            <View style={styles.cropContainer}>
                {HeadView()}
                {ContentView()}


                {isVideoType ?
                    <View style={styles.flatListContainerStyle}>
                        <FlatList
                            style={{ width: '100%', height: '100%', backgroundColor: 'blue' }}
                            data={coverList}
                            horizontal
                            keyExtractor={(item, index) => index}
                            renderItem={({ item, index }) => (
                                <Image
                                    style={{ width: 40, height: 60 }}
                                    source={{ uri: item.uri }}
                                />
                            )}
                        />
                    </View> :
                    <View style={[styles.flatListContainerStyle]}>
                        <FlatList
                            style={{ width: fileData.length <= 8 ? fileData.length * 40 + 5 * (fileData.length - 1) : '100%', height: '100%' }}
                            data={fileData}
                            horizontal
                            keyExtractor={(item, index) => index}
                            renderItem={({ item, index }) => {
                                return (
                                    <TouchableOpacity onPress={() => {
                                        // console.info("setImageSelectedPosition",item);
                                        setImageSelectedPosition(index);
                                    }} >
                                        <Image
                                            resizeMode={'cover'}
                                            style={{
                                                width: 40, height: 60, borderRadius: 2, marginLeft: index === 0 ? 0 : 5,
                                                borderWidth: 2,
                                                borderColor: imageSelectedPosition === index ? '#8EF902' : '#00000000',
                                            }}
                                            source={{ uri: item.uri }}
                                        />
                                    </TouchableOpacity>
                                )
                            }}
                        />
                    </View>

                }


            </View>
        </SafeAreaView >

    )
}

const styles = StyleSheet.create({
    flatListContainerStyle: {
        width: width - 30,
        height: 60,
        marginTop: 50,
        marginHorizontal: 15,
        borderRadius: 2,
        overflow: 'hidden',
        justifyContent: 'center',
        alignItems: 'center'
    },
    contentView: {
        marginTop: 14,
        marginHorizontal: 50,
        width: width - 100,
        height: (width - 100) * 16 / 9,
        borderRadius: 4,
        overflow: 'hidden',
    },
    fileContentView: {
        width: '100%',
        height: '100%',
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

