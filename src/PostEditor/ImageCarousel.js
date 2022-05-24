import React from 'react';

import {
    StyleSheet,
    View,
    Text,
    Image,
    TouchableOpacity,
    Dimensions,
    AppState,
    Animated,
} from 'react-native';

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';

import RanimatedCarousel from 'react-native-reanimated-carousel';

import PhotoProgress from './PhotoProgress'

import { TapGestureHandler, State } from 'react-native-gesture-handler';

const { width, height } = Dimensions.get('window');

export default class ImageCarousel extends React.Component {
    constructor(props) {
        super(props);

        this.refRanimatedCarousel = React.createRef();

        this.data = this.props.uploadData
        this.isMulti = !!this.data?.length && this.data.length > 1
        this.state = {
            enabled: true,
            loop: false,
            currentDuration: 0,
            isPlay: true,    //是否自动播放，单击改变
            isScroll: false,
        }
        this.intervalTime = 100; //ms  刷新间隔
        this.itemDuration = 3000; //ms 一张图/一个进度条对应的时长

        if (this.data?.length) {
            // 最大时长
            this.maxTime = this.data.length * 3000; //ms
        } else {
            this.maxTime = 0;
        }

        this.appState = '';

        this.carouselTouchType = State.UNDETERMINED;
    }

    _handleAppStateChange = (nextAppState) => {
        if (this.appState.match(/inactive|background/) && nextAppState === 'active') {
            this.startInterva();
        } else {
            this.stopInterva();
        }
        this.appState = nextAppState;
    };

    componentDidMount() {
        AppState.addEventListener('change', this._handleAppStateChange);

        setTimeout(() => {
            this.startInterva();
        }, this.intervalTime);
    }

    componentWillUnmount() {
        this.stopInterva();
        clearTimeout(this.snapToItemTimeout)
        AppState.removeEventListener('change', this._handleAppStateChange);
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.enabled !== this.state.enabled) {
            return true;
        }
        if (nextState.loop !== this.state.loop) {
            return true;
        }
        if (nextState.currentDuration !== this.state.currentDuration) {
            return true;
        }
        if (nextState.isScroll !== this.state.isScroll) {
            return true;
        }
        if (nextState.isPlay !== this.state.isPlay) {
            if (nextState.isPlay) {
                this.startInterva();
            } else {
                this.stopInterva();
            }
            return true;
        }
        return false;
    }

    startInterva = () => {
        if (!this.data?.length || this.data?.length <= 1) {
            return;
        }
        if (!!this.timerInterval) {
            return;
        }
        this.timerInterval = setInterval(() => {
            let duration = this.state.currentDuration + this.intervalTime;
            duration = duration % this.maxTime;
            if (duration < this.maxTime && duration % this.itemDuration == 0) {
                const imageSelectPosition = parseInt((duration / this.itemDuration));
                console.info("imageSelectPosition", imageSelectPosition);
                this.refRanimatedCarousel?.current.goToIndex(imageSelectPosition, imageSelectPosition !== 0);
            }
            this.setState({ currentDuration: duration });
        }, this.intervalTime);

    }

    stopInterva = () => {
        clearInterval(this.timerInterval)
        this.timerInterval = null;
    }

    IndicatorView = () => {
        if (!this.data?.length || this.data?.length <= 1) {
            return null;
        }
        const playAnimaton = !this.state.isScroll && this.state.isPlay;
        //TODO
        console.info("currentDuration", this.state.currentDuration, playAnimaton);
        return (<View style={
            {
                position: 'absolute',
                left: 0,
                right: 0,
                bottom: 82,
                height: 10,
                width: '100%',
            }
        }>
            <PhotoProgress
                itemCount={this.data?.length}
                itemDuration={this.itemDuration}
                currentDuration={this.state.currentDuration}
                playAnimaton={playAnimaton}
            />
        </View>)
    }

    render() {

        return (
            <Animated.View style={styles.continueView}>
                <TapGestureHandler
                    shouldCancelWhenOutside={true}
                    enabled={true}
                    onHandlerStateChange={({ nativeEvent }) => {
                        if (nativeEvent.state === State.END) {
                            //单击
                            const isPlay = !this.state.isPlay;
                            this.setState({ isPlay: isPlay });
                            this.props.setPlay(isPlay);
                        }
                    }}
                >
                    <Animated.View style={{ width: width, height: height, position: 'relative' }}>
                        <RanimatedCarousel
                            ref={this.refRanimatedCarousel}
                            width={width}
                            height={height}
                            data={this.data}
                            keyExtractor={(i, index) => i + index}

                            enabled={this.state.enabled}
                            loop={this.state.loop}

                            autoPlay={false}
                            autoPlayInterval={0}
                            horizontal={true}

                            panGestureHandlerProps={{
                                activeOffsetX: [-10, 10],
                                onHandlerStateChange: ({ nativeEvent }) => {
                                    //滑动
                                    if (nativeEvent.state === State.ACTIVE) {
                                        console.info("滑动开始");
                                        this.carouselTouchType = State.ACTIVE;
                                        this.setState({ isScroll: true });
                                        this.stopInterva();
                                    } else if (nativeEvent.state === State.END) {
                                        this.carouselTouchType = State.END;
                                    }
                                }
                            }}

                            onScrollBegin={() => {
                                //开始滑动
                            }}
                            onSnapToItem={(index) => {
                                //滑动完成
                                if (this.carouselTouchType === State.END) {
                                    this.carouselTouchType = State.UNDETERMINED;

                                    console.info("滑动完成 imageSelectPosition", index);

                                    // 将  duration 设置在下一页的初始位置
                                    const duration = (index + 1) * this.itemDuration;
                                    this.setState({
                                        currentDuration: duration,
                                    });
                                    if (!this.state.isPlay) {
                                        return
                                    }
                                    //延迟 3秒（this.itemDuration）后继续开始自动滚动
                                    this.snapToItemTimeout = setTimeout(() => {
                                        if (!this.state.isPlay) {
                                            return
                                        }
                                        //设置翻页到下一页
                                        const imageSelectPosition = (index + 1) % this.data?.length;
                                        this.refRanimatedCarousel?.current.goToIndex(imageSelectPosition, imageSelectPosition !== 0);

                                        console.info("滑动完成 setTimeout imageSelectPosition", imageSelectPosition);

                                        this.setState({ isScroll: false });
                                        this.startInterva();
                                    }, this.itemDuration);

                                }
                            }}

                            renderItem={({ index, item }) => (
                                <ImageItem
                                    {...this.props}
                                    index={index}
                                    item={item}
                                />
                            )}
                        />

                        {!this.state.isPlay && <Image style={
                            {
                                position: 'absolute', width: 60, height: 60,
                                left: (width - 50) / 2,
                                bottom: height / 100 * 55,
                            }
                        }
                            resizeMode={'contain'}
                            source={require('../../images/ic_post_image_play.png')}
                        />}
                    </Animated.View>
                </TapGestureHandler>

                {this.IndicatorView()}

            </Animated.View >)
    }
}


class ImageItem extends React.Component {
    constructor(props) {
        super(props);
    }
    shouldComponentUpdate(nextProps, nextState) {
        if (nextProps.item !== this.props.item) {
            return true;
        }
        return false;
    }
    render() {
        const item = this.props.item;

        const itemHeight = item.height / item.width * width;

        return (
            <View style={{ width: width, height: width * 16 / 9, overflow: 'hidden', alignItems: 'center', justifyContent: 'center' }}>
                < FastImage
                    source={{ uri: item.path }}
                    style={{ width: width, height: itemHeight, backgroundColor: 'rgba(100,100,100,0.5)' }}
                    resizeMode='cover'
                    placeholderStyle={{ backgroundColor: 'transparent' }}
                />
            </View>
        )
    }
}

const styles = StyleSheet.create({
    continueView: {
        width: '100%',
        height: '100%',
        backgroundColor: '#000',
        position: 'relative'
    },

})