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
import { State } from 'react-native-gesture-handler';

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
        }

        // setTimeout(() => {
        //     this.refRanimatedCarousel?.current?.goToIndex(1, true);
        // }, 3000);

    }

    componentDidMount() {
    }

    componentWillUnmount() {
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.loop !== this.state.loop) {
            return true;
        }
        if (nextState.enabled !== this.state.enabled) {
            return true;
        }
        return false;
    }

    onHandlerStateChange = (event) => {
        if (event.nativeEvent.state === State.ACTIVE) {
            //暂停/开启 自动播放
        }
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

                    autoPlay={true}
                    autoPlayInterval={1000}
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
        return (
            <View style={{ width: width, height: width * 16 / 9, overflow: 'hidden', alignItems: 'center', justifyContent: 'center' }}>
                < FastImage
                    source={{ uri: item.url }}
                    style={{ width: width, height: item.height / item.width * width, backgroundColor: 'rgba(100,100,100,0.5)' }}
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
        backgroundColor: '#000'
    },

})