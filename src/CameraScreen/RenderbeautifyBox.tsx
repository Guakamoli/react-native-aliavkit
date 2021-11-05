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
import { connect } from 'react-redux';

import _ from 'lodash';
import Camera from '../Camera';
import Carousel from './Carousel';
import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import StoryEditor from '../StoryEditor';
import StoryMusic from '../StoryMusic';
import AVService from '../AVService.ios';
import { BoxBlur } from 'react-native-image-filter-kit';
import {
    setCameraType,
    setNormalBeautyLevel,
} from '../actions/story';


const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const captureIcon2 = (width - 20) / 2;


// 美颜 滤镜 box
class RenderbeautifyBox extends React.PureComponent {
    constructor(props) {
        super(props)
    }
    render() {
        if (!this.props.showBeautify) return null
        return (
            <View style={{ height: 300, backgroundColor: '#000', width: width, zIndex: 99, position: "absolute", }}>
                <View style={styles.beautifyBoxHead}>
                    <Text style={styles.beautifyTitle}>{`美颜`}</Text>

                    <Image style={styles.beautyAdjustIcon} source={this.props.beautyAdjustImag} resizeMode='contain' />
                </View>
                <View style={styles.beautifyBoxContent}>
                    {[0, 1, 2, 3, 4, 5].map((item, index) => {
                        return (
                            <Pressable
                                onPress={() => {
                                    this.props.setNormalBeautyLevel(item);
                                }}
                                key={index}
                            >
                                <View style={[styles.beautifySelect, this.props.normalBeautyLevel === item && styles.beautifySelecin]}>
                                    <Text style={styles.beautifySelectTitle}>{item}</Text>
                                </View>
                            </Pressable>
                        );
                    })}
                </View>
            </View>
        );
    }
}

const RDSMMapStateToProps = state => ({
    normalBeautyLevel: state.shootStory.normalBeautyLevel,
    showBeautify: state.shootStory.showBeautify,
});
const RDSMMapDispatchToProps = dispatch => ({
    setNormalBeautyLevel: (props) => dispatch(setNormalBeautyLevel(props)),

});

export default connect(RDSMMapStateToProps, RDSMMapDispatchToProps)(RenderbeautifyBox)


const styles = StyleSheet.create({

 
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
});
