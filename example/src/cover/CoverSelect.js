import React, { useEffect, useState, useRef } from 'react';

import { AVService } from 'react-native-aliavkit';

import {
    Animated,
    StyleSheet,
    View,
    Text,
    FlatList,
    TouchableOpacity,
    Dimensions,
    Platform,
    Image,
    SafeAreaView,
} from 'react-native';

import {
    State,
    PanGestureHandler
} from "react-native-gesture-handler";

import Video from 'react-native-video';

const { width, height } = Dimensions.get('window');

let videoCoverList = [];
let videoCoverPath = '';
let translationWidth = 0;
let videoSeekTime = 0

let translateXMaxRange = width - 30 - 40;

const CoverSelect = (props) => {

    const { navigation } = props;

    const refVideo = useRef();

    //上传数据，数组
    const fileData = props.route.params?.fileData;

    const firstData = fileData[0];

    //上传类型是否是视频
    const isVideoType = firstData?.type?.includes('video') ? true : false;

    const [isLandscape, setLandscape] = useState(false);

    //视频播放
    const [vidoePaused, setVideoPaused] = useState(false);

    //图片数组选中下标
    const [imageSelectedPosition, setImageSelectedPosition] = useState(0);

    const [videoCoveItemWidth, setVideoCoveItemWidth] = useState(40);

    //视频封面列表
    const [coverList, setCoverList] = useState([]);

    //选中的视频封面
    const [videoCover, setVideoCover] = useState('');

    const coverTranslateX = useRef(new Animated.Value(0));
    let coverTranslateXRange = coverTranslateX.current.interpolate({
        inputRange: [0, translateXMaxRange],
        outputRange: [0, translateXMaxRange],
        extrapolate: 'clamp'
    })

    useEffect(() => {
        videoCoverPath = [];
        videoCoverList = '';
        translationWidth = 0;
        videoSeekTime = 0;
        let isLandscape = false;
        if (firstData.rotation === 90 || firstData.rotation === 270) {
            isLandscape = firstData.width < firstData.height;
        } else {
            isLandscape = firstData.width > firstData.height;
        }
        setLandscape(isLandscape)
        if (isVideoType) {
            getVideoFrames(isLandscape);
        }
        return () => {
        };
    }, []);


    /**
     *  定位播放位置
     */
    const onSeekVideo = (moveValue) => {
        if (!isVideoType) {
            return
        }
        const videoDuration = firstData.playableDuration;
        let seekTime = moveValue / translateXMaxRange * videoDuration;
        if (seekTime >= firstData.playableDuration - 500) {
            seekTime = firstData.playableDuration - 500;
        } else if (seekTime < 0) {
            seekTime = 0;
        }
        refVideo?.current?.seek(seekTime / 1000.0);
        videoSeekTime = seekTime;
    }

    /**
     * 获取视频帧
     */
    const getVideoFrames = async (isLandscape = false) => {
        const framesCount = 9;
        const itemWidth = (width - 30) / framesCount;
        setVideoCoveItemWidth(itemWidth)
        translateXMaxRange = width - 30 - itemWidth;
        coverTranslateXRange = coverTranslateX.current.interpolate({
            inputRange: [0, translateXMaxRange],
            outputRange: [0, translateXMaxRange],
            extrapolate: 'clamp'
        })
        const videoDuration = firstData.playableDuration;
        let videoPath = firstData.path;
        if (!!videoPath && videoPath.startsWith("file://")) {
            videoPath = videoPath.slice(7)
        }
        const itemPerTime = parseInt(videoDuration / framesCount);
        const startTime = parseInt(itemPerTime / 3);
        let scale = 3.0;
        if (isLandscape) {
            scale = 5.0;
        }
        const coverWidth = itemWidth * scale;
        const coverHeight = coverWidth * firstData.height / firstData.width;
        const imageSize = { width: coverWidth, height: coverHeight };
        const framesParam = { videoPath, startTime, itemPerTime, needCover: false, imageSize }
        const thumbnails = await AVService.getThumbnails(framesParam);
        if (thumbnails?.length) {
            setVideoCover(thumbnails[0]);
            setCoverList(thumbnails);
            videoCoverPath = thumbnails[0];
            videoCoverList = thumbnails;
        }
    }

    /**
     * 获取视频封面
     */
    const getVideoCover = async () => {
        AVService.removeThumbnaiImages();
        let coverParh = '';
        if (isVideoType) {
            let videoPath = firstData.path;
            if (!!videoPath && videoPath.startsWith("file://")) {
                videoPath = videoPath.slice(7)
            }
            const startTime = videoSeekTime;
            const itemPerTime = 1000;
            // needCover 代表截取封面。
            const needCover = true;
            const imageSize = { width: firstData.width, height: firstData.height };
            const framesParam = { videoPath, startTime, itemPerTime, imageSize, needCover }

            const thumbnails = await AVService.getThumbnails(framesParam);

            if (thumbnails?.length) {
                coverParh = 'file://' + thumbnails[0];
            }
        } else {
            coverParh = fileData[imageSelectedPosition].uri
        }
        navigation.navigate({
            name: 'CoverScreen',
            params: { coverImagePath: coverParh },
            merge: true,
        });
    }


    const onGestureEvent = Animated.event(
        [{
            nativeEvent: {
                translationX: coverTranslateX.current,
            }
        }],
        {
            useNativeDriver: true, listener: (event) => {
                if (event.nativeEvent.state === State.ACTIVE) {
                    const moveWidt = translationWidth + event.nativeEvent.translationX
                    onSeekVideo(moveWidt);
                    const selectPosition = Math.floor((moveWidt / videoCoveItemWidth + 0.5));
                    if (videoCoverList[selectPosition] !== videoCoverPath) {
                        videoCoverPath = videoCoverList[selectPosition];
                        setVideoCover(videoCoverPath);
                    }
                }
            }
        }
    )

    const onHandlerStateChange = (event) => {
        if (event.nativeEvent.state === State.END) {
            translationWidth += event.nativeEvent.translationX;
            coverTranslateX?.current?.setOffset(translationWidth);
            coverTranslateX?.current?.setValue(0);
            onSeekVideo(translationWidth);
        }
    }

    const HeadView = () => {
        return (
            <View style={styles.continueHeadView}>
                <TouchableOpacity onPress={navigation.goBack}>
                    <Text style={styles.textClean}>取消</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={getVideoCover}>
                    <Text style={styles.textConfirm}>确认</Text>
                </TouchableOpacity>
            </View>
        )
    }


    const ContentView = () => {
        return (
            <View>
                {isVideoType ?
                    <View style={{ width: width, height: (width - 100) * 16 / 9 + 12, justifyContent: 'center' }}>
                        <View style={isLandscape ? styles.contentViewLandscape : styles.contentViewVertical} >
                            <Video
                                ref={refVideo}
                                style={styles.fileContentView}
                                source={{ uri: Platform.OS === 'android' ? firstData.uri : firstData.path }}
                                muted
                                resizeMode='cover'
                                paused={Platform.OS === 'android' ? vidoePaused : true}
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
                        </View>
                    </View>
                    :
                    <Image
                        style={styles.contentViewVertical}
                        source={{ uri: fileData[imageSelectedPosition].uri }}
                        resizeMode='cover'
                    />
                }
            </View>
        )
    }

    const ImageCoveView = () => {
        return (<View style={[styles.flatListContainerStyle]}>
            <FlatList
                style={{ width: fileData.length <= 8 ? fileData.length * 40 + 5 * (fileData.length - 1) : '100%', height: '100%' }}
                data={fileData}
                horizontal
                keyExtractor={(item, index) => index}
                renderItem={({ item, index }) => {
                    return (
                        <TouchableOpacity onPress={() => setImageSelectedPosition(index)} >
                            <Image source={{ uri: item.uri }} resizeMode={'cover'}
                                style={
                                    [
                                        styles.imageCoveItemStyle,
                                        {
                                            marginLeft: index === 0 ? 0 : 5,
                                            borderColor: imageSelectedPosition === index ? '#8EF902' : '#00000000',
                                        }
                                    ]
                                }
                            />
                        </TouchableOpacity>
                    )
                }}
            />
        </View>)
    }

    const VideoCoveView = () => {
        return (
            <View style={[styles.flatListContainerVideoStyle, {}]}>
                {coverList?.map((item, index) => {
                    return (<Image
                        key={item}
                        style={{ width: videoCoveItemWidth, height: 60 }}
                        source={{ uri: item }}
                    />)
                })}
                <PanGestureHandler
                    minDist={5}
                    hitSlop={{ left: 15, top: 50, right: 15, bottom: 50 }}
                    shouldActivateOnStart={true}
                    shouldCancelWhenOutside={true}
                    disallowInterruption={true}
                    onGestureEvent={onGestureEvent}
                    onHandlerStateChange={onHandlerStateChange}
                >
                    <Animated.View style={{
                        position: 'absolute',
                        top: 0,
                        bottom: 0,
                        left: 0,
                        right: 0,
                        alignItems: 'center',
                    }}>
                        <Animated.View style={{ marginTop: 48, width: width - 26, height: 64 }}>
                            <Animated.View style={
                                [
                                    styles.videoSelectedCover,
                                    { width: videoCoveItemWidth + 4 },
                                    { transform: [{ translateX: coverTranslateXRange }] }
                                ]
                            }>
                                {!!videoCover && <Image
                                    key={videoCover}
                                    style={{ width: '100%', height: '100%' }}
                                    source={{ uri: videoCover }}
                                    resizeMode='cover'
                                />}
                            </Animated.View>
                        </Animated.View>

                    </Animated.View>
                </PanGestureHandler>
            </View>
        )
    }

    return (
        <SafeAreaView style={{ flex: 1, backgroundColor: 'black' }}>
            <View style={styles.cropContainer}>
                {HeadView()}
                {ContentView()}
                {isVideoType ? VideoCoveView() : ImageCoveView()}
            </View>
        </SafeAreaView >
    )
}

const styles = StyleSheet.create({
    videoSelectedCover: {
        width: 40,
        height: '100%',
        borderRadius: 4,
        overflow: 'hidden',
        borderColor: '#8EF902',
        borderWidth: 2,
    },
    imageCoveItemStyle: {
        width: 40,
        height: 60,
        borderRadius: 2,
        borderWidth: 2,
    },
    flatListContainerStyle: {
        width: width - 30,
        height: 60,
        marginTop: 50,
        marginHorizontal: 15,
        borderRadius: 2,
        overflow: 'hidden',
        justifyContent: 'center',
        alignItems: 'center',
    },
    flatListContainerVideoStyle: {
        width: width,
        paddingTop: 50,
        borderRadius: 2,
        overflow: 'hidden',
        justifyContent: 'center',
        flexDirection: 'row',
        flex: 1,
    },
    contentViewVertical: {
        marginTop: 12,
        marginHorizontal: 50,
        width: width - 100,
        height: (width - 100) * 16 / 9,
        borderRadius: 4,
        overflow: 'hidden',
        position: 'relative'
    },
    contentViewLandscape: {
        width: width,
        height: width * 9 / 16,
        borderRadius: 4,
        overflow: 'hidden',
        position: 'relative'
    },
    fileContentView: {
        width: '100%',
        height: '100%',
    },
    continueHeadView: {
        width: width,
        height: 47,
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

