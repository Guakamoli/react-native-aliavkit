import PropTypes from 'prop-types';
import React, { Component, useRef, useEffect, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  Dimensions,
  Platform,
  FlatList,
  Pressable,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler, LongPressGestureHandler, NativeViewGestureHandler } from 'react-native-gesture-handler';
import { setFacePasterInfo } from '../actions/story';
import _ from 'lodash';
import Carousel, { getInputRangeFromIndexes } from '../react-native-snap-carousel/src';

import AVService from '../AVService';
import { connect } from 'react-redux';
import { transform } from '@babel/core';
import { ReanimatedArcBase } from '@callstack/reanimated-arc';
import Reanimated from 'react-native-reanimated';

const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const smallImageSize = 52;
const bigImageSize = 64;
const captureIcon2 = (width - 20) / 2;

class CircleProgress extends Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() { }
  render() {
    return (
      <View style={{
        position: 'absolute', left: 0, right: 0,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}>

        <NativeViewGestureHandler
          disallowInterruption={true}
          shouldActivateOnStart={true}
          onHandlerStateChange={(event) => {
            if (event.nativeEvent.state === State.END) {
              console.info("回撤录制");
              this.props?.deleteLastMultiRecording?.()
            }
          }}
        >
          <Text style={{ marginLeft: 26, width: 38, height: 38, alignContent: 'center', justifyContent: 'center', backgroundColor: 'blue', lineHeight: 38 }}>回撤</Text>
        </NativeViewGestureHandler>

        {/* <Pressable
          style={{ marginLeft: 26, width: 38, height: 38, alignContent: 'center', justifyContent: 'center', backgroundColor: 'blue' }}
          onPress={() => {
            console.info("回撤录制");
          }}
        >
          <Text>回撤</Text>
        </Pressable> */}

        <LongPressGestureHandler
          ref={this.longPressRef}
          shouldCancelWhenOutside={false}
          onHandlerStateChange={({ nativeEvent }) => {
            //TODOWUYQ
            // console.info("LongPressGestureHandler 1111 ", nativeEvent.state);

            if (nativeEvent.state === State.ACTIVE) {
              this.isLongPress = true;
              this.props.longPress();
            } else if (nativeEvent.state === State.END) {
              this.isLongPress = false;
              this.props.stopAnimate();
            } else {
              if (this.isLongPress) {
                this.isLongPress = false;
                this.props.stopAnimate();
              }
            }
          }}
          minDurationMs={300}
          maxDist={30}
        >
          <Reanimated.View
            style={[
              styles.container,
              {
                transform: [{ scale: this.props.scale, translateY: -0 }],
              },
            ]}
          >
            {/* <Reanimated.Code
                exec={Reanimated.call([arcAngle.current], ([value]) => {
                  setText(`${Math.round((value / 240) * 100)}%`);
                })}
              /> */}
            <View style={styles.centerBox} />
            <ReanimatedArcBase
              color='rgba(255, 255, 255, 0)'
              diameter={122}
              width={6}
              arcSweepAngle={360}
              lineCap='round'
              rotation={360}
              style={styles.absolute}
            />
            <ReanimatedArcBase
              color='rgba(234, 54, 0, 1)'
              diameter={122}
              width={6}
              arcSweepAngle={this.props.arcAngle}
              lineCap='round'
              rotation={360}
              style={styles.absolute}
            />
          </Reanimated.View>
        </LongPressGestureHandler>

        {/* <Pressable
          style={{ marginRight: 26, width: 38, height: 38, alignContent: 'center', justifyContent: 'center', backgroundColor: 'blue' }}
          onPress={() => {
            console.info("完成录制");
          }}
        >
          <Text>完成</Text>
        </Pressable> */}

        <NativeViewGestureHandler
          disallowInterruption={true}
          shouldActivateOnStart={true}
          onHandlerStateChange={(event) => {
            if (event.nativeEvent.state === State.END) {
              console.info("完成录制");
              this.props?.finishMultiRecording?.()
            }
          }}
        >
          <Text style={{ marginRight: 26, width: 38, height: 38, alignContent: 'center', justifyContent: 'center', backgroundColor: 'blue', lineHeight: 38 }}>完成</Text>
        </NativeViewGestureHandler>

      </View>
    );
  }
}
const ClMapStateToProps = (state) => ({
  // facePasterInfo: state.shootStory.facePasterInfo,
});
const ClMapDispatchToProps = (dispatch) => ({
  setFacePasterInfo: (params) => dispatch(setFacePasterInfo(params)),
});
export default connect(ClMapStateToProps, ClMapDispatchToProps)(CircleProgress);
const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
    width: 122,
    height: 122,
    // TODO
    zIndex: 120,
    backgroundColor: 'rgba(255,255,255,0.3)',
    borderRadius: 129,
    // top: 66,
    left: (width - 122) / 2,
  },
  absolute: {
    position: 'absolute',
  },
  centerBox: {
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
    width: 66,
    height: 66,
    backgroundColor: 'white',
    borderRadius: 66,
    // top: 66,
  },
});
