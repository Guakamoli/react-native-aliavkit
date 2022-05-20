import React from 'react';

import {
    StyleSheet,
    View,
    Text,
    Image,
    TouchableOpacity,
    Dimensions,
} from 'react-native';

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';

import RanimatedCarousel from 'react-native-reanimated-carousel';

import PhotoProgress from './PhotoProgress'


const { width, height } = Dimensions.get('window');

export default class ImageCarousel extends React.Component {
    constructor(props) {
        super(props);

        this.refRanimatedCarousel = React.createRef();

        this.data = this.props.uploadData
        const isMulti = !!this.data?.length && this.data.length > 1
        this.state = {
            loop: isMulti,
            enabled: isMulti,
            currentTime: 0,
        }
        this.intervalTime = 100; //ms
        this.itemDuration = 3000; //ms

        if (this.data?.length) {
            this.maxTime = this.data.length * 3000; //ms
        } else {
            this.maxTime = 0;
        }
    }

    componentDidMount() {
        this.startInterva();
    }

    componentWillUnmount() {
        this.stopInterva();
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.loop !== this.state.loop) {
            return true;
        }
        if (nextState.enabled !== this.state.enabled) {
            return true;
        }
        if (nextState.currentTime !== this.state.currentTime) {
            return true;
        }
        return false;
    }

    startInterva = () => {
        if (!this.data?.length || this.data?.length <= 1) {
            return;
        }
        setTimeout(() => {
            this.timerInterval = setInterval(() => {
                let currentTime = this.state.currentTime + this.intervalTime;
                if (currentTime > this.maxTime) {
                    currentTime = 0;
                }
                if (currentTime < this.maxTime && currentTime % this.itemDuration == 0) {
                    const imageSelectPosition = parseInt((currentTime / this.itemDuration));
                    this.refRanimatedCarousel?.current.goToIndex(imageSelectPosition, imageSelectPosition !== 0);
                }
                this.setState({ currentTime: currentTime });
            }, this.intervalTime);
        }, this.intervalTime);
    }

    stopInterva = () => {
        clearInterval(this.timerInterval)
    }

    IndicatorView = () => {

        if (!this.data?.length || this.data?.length <= 1) {
            return null;
        }
        // console.info("IndicatorView currentTime", this.state.currentTime);
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
                currentDuration={this.state.currentTime}
                playAnimaton={true}
            />

        </View>)
    }

    render() {

        return (
            <View style={styles.continueView}>
                <RanimatedCarousel
                    ref={this.refRanimatedCarousel}
                    width={width}
                    height={height}
                    data={this.data}
                    keyExtractor={(i, index) => i + index}

                    loop={this.state.loop}
                    enabled={this.state.enabled}

                    autoPlay={false}
                    autoPlayInterval={this.itemDuration}
                    horizontal={true}

                    onScrollBegin={() => {
                        //开始滑动
                        // console.info("onScrollBegin")
                    }}
                    onSnapToItem={(index) => {
                        //滑动完成
                        // console.info("onSnapToItem index", index)
                    }}

                    onProgressChange={(offsetProgress, absoluteProgress) => {

                    }}

                    renderItem={({ index, item }) => (
                        <ImageItem
                            {...this.props}
                            index={index}
                            item={item}
                        />
                    )}
                />

                {this.IndicatorView()}

            </View>)
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