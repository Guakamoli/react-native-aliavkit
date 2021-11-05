import PropTypes from 'prop-types';
import React, { Component, useRef, useEffect, useState } from 'react';
import {
    StyleSheet,
    Text,
    View,
    // Pressable,
    Image,
    Dimensions,
    Platform,
    Animated,
    FlatList,
    Easing,
    Pressable,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import {
    setFacePasterInfo,

} from '../actions/story';
import _ from 'lodash';
import Carousel, { getInputRangeFromIndexes } from '../react-native-snap-carousel/src';

import AVService from '../AVService.ios';
import { connect } from 'react-redux';
import { transform } from '@babel/core';

const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const smallImageSize = 52;
const bigImageSize = 64;
const captureIcon2 = (width - 20) / 2;

// import AVService from '../AVService.ios.ts';
const stateAttrsUpdate = [
    'pasterList', 'facePasterInfo']


export type Props = {
    facePasterInfo: object
};

type State = {
    pasterList: any[];

};

type PropsType = {
    facePasterInfo: {
        eid: any
    }
    giveUpImage: any,
    snapToItem: Function,
    scrollPos: Animated.Value

}

class TopReset extends Component<PropsType>{
    constructor(props) {
        super(props)
    }
    render() {
        const { scrollPos } = this.props
        return (
            <Animated.View style={[
                styles.clearBox,
                {
                    transform: [
                        {
                            scale: scrollPos.interpolate({
                                inputRange: [0, 1],
                                outputRange: [0, 1],
                                extrapolate: "clamp"
                            })
                        }
                    ]
                }

            ]}>
                <Pressable
                    style={styles.clearIcon}
                    onPress={() => {
                        this.props.snapToItem?.(0);
                        this.setState({ facePasterInfo: { eid: 0 } });
                    }}
                >
                    <Image source={this.props.giveUpImage} style={styles.clearIcon} />
                </Pressable>
            </Animated.View>
        )
    }
}
// const TRMapStateToProps = state => ({
//     // facePasterInfo: state.shootStory.facePasterInfo,
// });
// const TRMapDispatchToProps = dispatch => ({
//     setFacePasterInfo: (params) => dispatch(setFacePasterInfo(params)),

// });
// TopReset = connect(TRMapStateToProps, TRMapDispatchToProps)(TopReset)

class RenderBigCircle extends Component {
    constructor(props) {
        super(props)
    }
    shouldComponentUpdate(nextProps) {
        if (nextProps.pasterList && nextProps.pasterList !== this.props.pasterList) {
            return true
        }
        return false
    }
    render() {

        const { pasterList, scrollPos } = this.props
        return (
            <Animated.View
                style={{
                    position: 'absolute',
                    flexDirection: 'row',
                    left: -(itemWidth - circleSize) / 2,
                    top: (circleSize - bigImageSize) / 2,
                    transform: [{ translateX: Animated.multiply(scrollPos, -1) }],
                }}
            >
                {pasterList.map((i, index) => {
                    return (
                        <View
                            key={index}
                            style={{
                                alignItems: 'center',
                                width: itemWidth,
                            }}
                        >
                            <Animated.View
                                style={[
                                    styles.propStyle,
                                    {
                                        width: bigImageSize,
                                        height: bigImageSize,
                                        opacity: 1,
                                        borderRadius: bigImageSize,
                                        transform: [
                                            {
                                                translateX: scrollPos.interpolate({
                                                    inputRange: [(index - 1) * itemWidth, index * itemWidth, (index + 1) * itemWidth],
                                                    outputRange: [
                                                        (bigImageSize - smallImageSize) / 2,
                                                        0,
                                                        -(bigImageSize - smallImageSize) / 2,
                                                    ],

                                                    extrapolate: 'clamp',
                                                }),
                                            },
                                        ],
                                    },
                                    i.eid == 0 && { backgroundColor: '#fff' },
                                ]}
                            >
                                <Image
                                    style={{ width: bigImageSize, height: bigImageSize, borderRadius: bigImageSize }}
                                    source={{ uri: i.icon }}
                                />
                            </Animated.View>
                        </View>
                    );
                })}
            </Animated.View>
        )
    }
}
class RenderChildren extends Component {
    constructor(props) {
        super(props)
        this.startTime = null
    }
    shouldComponentUpdate(nextProps) {
        if (nextProps.pasterList !== this.props.pasterList) {
            return true
        }
        return false
    }
    render() {
        console.info("渲染")
        const { pasterList, scrollPos, captureButtonImage } = this.props
        return (
            <Animated.View
                style={[
                    styles.captureButtonImage,
                    { width: circleSize, height: circleSize, borderRadius: circleSize, zIndex: 11 },
                    {
                        transform: [{ translateX: Animated.multiply(scrollPos, 1) }],
                    },
                ]}
            >
                <Pressable
                    style={[{ width: circleSize, height: circleSize, borderRadius: circleSize, overflow: 'hidden' }]}
                    delayLongPress={500}
                    // 长按
                    pressRetentionOffset={{ bottom: 1000, left: 1000, right: 1000, top: 1000 }}
                    onLongPress={async () => {
                        // 按钮动画
                        Animated.timing(
                            // 随时间变化而执行动画
                            this.state.fadeInOpacity, // 动画中的变量值
                            {
                                toValue: 122, // 透明度最终变为1，即完全不透明
                                duration: 500, // 让动画持续一段时间
                                useNativeDriver: false,
                            },
                        ).start();
                        const success = await this.props.camera.startRecording();
                        // 获取开始时间
                        this.startTime = Date.parse(new Date()).toString().substr(0, 10);
                        this.setState({ fileType: 'video', startShoot: success });
                        if (success) {
                            // 调用进度条 开始拍摄
                            this.animate();
                        } else {
                            this.myRef.current.show('摄像失败,请重试', 2000);
                        }
                    }}
                    // 长按结束

                    onPressOut={async () => {
                        this.setState({
                            flag: null,
                        });
                        // 结束时间 小于两秒重置
                        let endTime = Date.parse(new Date()).toString().substr(0, 10);
                        if (Number(endTime) - Number(this.startTime) < 2) {
                            this.myRef.current.show('时间小于2秒，请继续拍摄', 2000);
                            this.setState({
                                startShoot: false,
                                ShootSuccess: false,
                                fadeInOpacity: new Animated.Value(60),
                            });
                        }
                        if (this.state.startShoot) {
                            const videoPath = await this.camera.stopRecording();
                            this.setState({
                                fileType: 'video',
                                videoPath,
                                startShoot: false,
                                ShootSuccess: true,
                                fadeInOpacity: new Animated.Value(60),
                            });
                        }
                    }}
                    // 单击
                    onPress={() => {
                        const { startShoot, progress } = this.state;
                        if (!startShoot || progress === 0) {
                            // 拍照
                            this.onCaptureImagePressed();
                            this.setState({ fileType: 'image' });
                        }
                    }}
                >

                    <View style={styles.bigCircleBox}></View>
                    <RenderBigCircle {...this.props} />
                </Pressable>
            </Animated.View>
        )
    }
}
const RenderItem = React.memo((props) => {
    const { index, item, snapToItem } = props
    const toItem = () => {
        snapToItem?.(index);
    };
    console.info("nikanan")

    return (
        <Pressable delayLongPress={500} onPress={toItem} >
            <View >
                <View style={[styles.propStyle, styles.img]}>
                    <Image style={styles.img} source={{ uri: item.icon }} />
                </View>
            </View>
        </Pressable>
    );
})
class CarouselWrapper extends Component<Props, State> {
    FlatListRef: any;
    scrollPos: Animated.Value;
    constructor(props) {
        super(props);
        this.FlatListRef = React.createRef();
        this.scrollPos = new Animated.Value(0);
        this.state = {
            pasterList: [],
        };
    }

    componentDidMount() {
        this.getPasterInfos()
        setTimeout(() => {
            AVService.enableHapticIfExist()
        }, 2000);

    }
    shouldComponentUpdate(nextProps, nextState) {
        const stateUpdated = stateAttrsUpdate.some(key => nextState[key] !== this.state[key]);
        if (stateUpdated) {
            return true;
        }
        return false
    }

    getPasterInfos = async () => {
        const pasters = await AVService.getFacePasterInfos({});
        pasters.forEach((item, index) => {
            if (index == 0) {
                return
            }
            item.icon = item.icon.replace('http://', 'https://');
            item.url = item.url.replace('http://', 'https://');
        })
        pasters.unshift({ eid: 0 });
        this.setState({
            pasterList: pasters,
        });
    }


    _scrollInterpolator = (index, carouselProps) => {
        const range = [3, 2, 1, 0, -1, -2, -3]; // <- Remember that this has to be declared in a reverse order
        const inputRange = getInputRangeFromIndexes(range, index, carouselProps);
        const outputRange = range;
        return { inputRange, outputRange };
    };
    _animatedStyles(index, animatedValue, carouselProps) {
        return {
            opacity: animatedValue.interpolate({
                inputRange: [2, 3],
                outputRange: [1, 2],

                extrapolate: 'clamp',
            }),
            transform: [
                {
                    translateX: animatedValue.interpolate({
                        inputRange: [-3, -2, -1, 0, 1, 2, 3],
                        outputRange: [1.5 * smallImageSize, 0, 0, 0, 0, 0, -1.5 * smallImageSize],
                        extrapolate: 'clamp',
                    }),
                },
                {
                    scale: animatedValue.interpolate({
                        inputRange: [-3, -2, -1, 0, 1, 2, 3],
                        outputRange: [0, 0.5, 0.8, 1, 0.8, 0.5, 0],
                        extrapolate: 'clamp',
                    }),
                },
                {},
            ],
        };
    }
    snapToItem = (data) => {
        this.FlatListRef?.snapToItem?.(data)
    }
    render() {
        const { pasterList } = this.state;
        console.info("这里也渲染了")
        return (
            <View>
                <TopReset {...this.props} snapToItem={this.snapToItem} scrollPos={this.scrollPos} />
                <Carousel
                    ref={(flatList) => {
                        this.FlatListRef = flatList;
                    }}
                    lockScrollWhileSnapping={true}
                    snapToInterval={itemWidth}
                    impactAsync={this.props.haptics?.selectionAsync}
                    enableMomentum={true}
                    scrollInterpolator={this._scrollInterpolator}
                    slideInterpolatedStyle={this._animatedStyles}
                    enableSnap={true}
                    data={pasterList}
                    decelerationRate={'normal'}
                    swipeThreshold={1}
                    itemWidth={itemWidth}
                    inactiveSlideOpacity={1}
                    scrollPos={this.scrollPos}
                    sliderWidth={width}

                    slideStyle={{ justifyContent: 'center', alignItems: 'center' }}
                    contentContainerCustomStyle={{ height: 100, justifyContent: 'center', alignItems: 'center' }}
                    useScrollView={true}
                    onSnapToItem={(slideIndex = 0) => {
                        this.props.setFacePasterInfo(pasterList[slideIndex])

                    }}
                    renderItem={(props) => <RenderItem {...props} snapToItem={this.snapToItem} />}
                >
                    <RenderChildren {...this.props} pasterList={pasterList} scrollPos={this.scrollPos} />

                </Carousel>
                {/* 临时方案  安卓 拍摄不会触发 */}
            </View>
        );
    }

}
const ClMapStateToProps = state => ({
    // facePasterInfo: state.shootStory.facePasterInfo,
});
const ClMapDispatchToProps = dispatch => ({
    setFacePasterInfo: (params) => dispatch(setFacePasterInfo(params)),

});
export default connect(ClMapStateToProps, ClMapDispatchToProps)(CarouselWrapper)
const styles = StyleSheet.create({
    bottomButtons: {
        flex: 1,
    },
    textStyle: {
        color: 'white',
        fontSize: 20,
    },
    ratioBestText: {
        color: 'white',
        fontSize: 18,
    },
    ratioText: {
        color: '#ffc233',
        fontSize: 18,
    },
    BottomBox: {

        flex: 1,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        position: 'relative',
    },

    cameraContainer: {
        ...Platform.select({
            android: {
                // position: 'absolute',
                top: 0,
                left: 0,
                width,
                height,
            },
            default: {
                flex: 1,
                flexDirection: 'column',
            },
        }),
    },
    bottomButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        padding: 10,
    },
    bottomContainerGap: {
        flex: 1,
        flexDirection: 'row',
        justifyContent: 'flex-end',
        alignItems: 'center',
        padding: 10,
    },
    gap: {
        flex: 10,
        flexDirection: 'column',
    },

    videoTitle: {
        fontSize: 13,
        color: '#7E7E7E',
        lineHeight: 18,
        fontWeight: '500',
        position: 'absolute',
        right: 60,
    },
    snapshotTitle: {
        fontSize: 13,
        lineHeight: 18,
        color: '#FFFFFF',
    },
    snapshotMuse: {
        fontSize: 13,
        lineHeight: 18,
        color: '#FFFFFF',
        marginHorizontal: 30,
    },
    switchScreen: {},
    musicIcon: {
        width: 28,
        height: 28,
    },
    leftIconBox: {
        position: 'absolute',
        top: height * 0.35,
        left: 20,
        zIndex: 99,
    },
    beautifyIcon: {
        width: 28,
        height: 28,
        marginTop: 30,
    },
    closeBox: {
        position: 'absolute',
        top: height * 0.05,
        left: 20,
        zIndex: 99,
    },
    closeIcon: {
        width: 28,
        height: 28,
    },
    beautifyBoxHead: {
        paddingHorizontal: 20,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingTop: 15,
        paddingBottom: 26,
    },
    beautifyTitle: {
        fontSize: 15,
        fontWeight: '500',
        color: '#fff',
        lineHeight: 21,
    },
    beautyAdjustIcon: {
        width: 20,
        height: 16,
    },
    beautifyBoxContent: {
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    beautifySelect: {
        width: 48,
        height: 48,
        backgroundColor: ' rgba(69, 69, 73, 0.7)',
        borderRadius: 9,
        justifyContent: 'center',
        alignItems: 'center',
    },
    beautifySelectTitle: {
        fontSize: 20,
        fontWeight: '500',
        color: '#fff',
        lineHeight: 28,
    },
    beautifySelecin: {
        borderWidth: 2,
        borderColor: '#836BFF',
    },
    progress: {
        margin: 10,
    },

    uploadBox: {
        width: 130,
        height: 40,
        borderRadius: 22,
        backgroundColor: '#fff',
        justifyContent: 'center',
        alignItems: 'center',
    },
    uploadTitle: {
        fontWeight: '500',
        fontSize: 13,
        color: '#000',
        lineHeight: 18,
    },
    UpdateBox: {
        position: 'absolute',
        zIndex: 99,
        top: 20,
    },
    updateTopIcon: {
        width: 40,
        height: 40,
        marginRight: 10,
    },
    filterLensSelectTitle: {
        fontSize: 13,
        fontWeight: '500',
        color: '#fff',
        lineHeight: 18,
    },
    startShootAnnulus: {
        backgroundColor: 'rgba(255,255,255,0.5)',
        borderRadius: 122,
        position: 'absolute',
    },
    captureButton: {
        width: 49,
        height: 49,
        backgroundColor: '#fff',
        borderRadius: 49,
        position: 'absolute',
    },
    captureButtonContainer: {
        justifyContent: 'center',
        alignItems: 'center',
        flexDirection: 'row',
        position: 'absolute',
        bottom: 0,
    },
    captureButtonImage: {
        position: 'absolute',
        left: itemWidth * 2 + (itemWidth - circleSize) / 2,
        zIndex: -11,
        elevation: 1,
        // top: -(circleSize - smallImageSize) / 2,
    },
    slider: {
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        display: 'flex',
        zIndex: 99,
        elevation: 10,
    },
    startShootBox: {
        justifyContent: 'center',
        alignItems: 'center',
        position: 'relative',
        left: captureIcon2,
    },

    propStyle: {
        backgroundColor: '#334',
        opacity: 0.8,
    },
    clearIcon: {
        width: 32,
        height: 32,
    },
    clearBox: {
        alignItems: "center"
    },
    img:
    {
        width: smallImageSize,
        height: smallImageSize,
        borderRadius: smallImageSize
    },
    bigCircleBox: {
        width: circleSize,
        height: circleSize,
        zIndex: 1,
        borderRadius: circleSize,
        borderWidth: 4,
        borderColor: "white"
    }
});
