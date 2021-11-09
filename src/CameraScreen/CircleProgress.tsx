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
    FlatList,
    Pressable,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import {
    setFacePasterInfo,

} from '../actions/story';
import _ from 'lodash';
import Carousel, { getInputRangeFromIndexes } from '../react-native-snap-carousel/src';

import AVService from '../AVService';
import { connect } from 'react-redux';
import { transform } from '@babel/core';
import {ReanimatedArcBase} from "@callstack/reanimated-arc"
import Reanimated, {Easing} from 'react-native-reanimated';

const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const smallImageSize = 52;
const bigImageSize = 64;
const captureIcon2 = (width - 20) / 2;


class CircleProgress extends Component{
    constructor(props) {
        super(props);
   
    }

    
    componentDidMount() {
    }
    render() {
        return(
            <Reanimated.View style={[styles.container, {
                transform:[
                    {scale: this.props.scale}
                ]
            }]}>
              {/* <Reanimated.Code
                exec={Reanimated.call([arcAngle.current], ([value]) => {
                  setText(`${Math.round((value / 240) * 100)}%`);
                })}
              /> */}
              <View style={styles.centerBox}></View>
              <ReanimatedArcBase
                color="rgba(255, 255, 255, 0)"
                diameter={122}
                width={6}
                arcSweepAngle={360}
                lineCap="round"
                rotation={360}
                style={styles.absolute}
              />
              <ReanimatedArcBase
                color="rgba(234, 54, 0, 1)"
                diameter={122}
                width={6}
                arcSweepAngle={this.props.arcAngle}
                lineCap="round"
                rotation={360}
                style={styles.absolute}
              />
            </Reanimated.View>
        )
    }

}
const ClMapStateToProps = state => ({
    // facePasterInfo: state.shootStory.facePasterInfo,
});
const ClMapDispatchToProps = dispatch => ({
    setFacePasterInfo: (params) => dispatch(setFacePasterInfo(params)),

});
export default connect(ClMapStateToProps, ClMapDispatchToProps)(CircleProgress)
const styles = StyleSheet.create({
    container: {
        position: 'absolute',
        justifyContent: 'center',
        alignItems: 'center',
        width: 122,
        height: 122,
        zIndex: 999,
        backgroundColor:"rgba(255,255,255,0.3)",
        borderRadius: 129,
        // top: 66,
        left: (width - 122) /2
      },
      absolute: {
        position: 'absolute',
      },
      centerBox:{
        position: 'absolute',
        justifyContent: 'center',
        alignItems: 'center',
        width: 66,
        height: 66,
        backgroundColor:"white",
        borderRadius: 66,
        // top: 66,
      },
});
