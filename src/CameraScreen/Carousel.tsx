import PropTypes from 'prop-types';
import React, { Component, useRef, useEffect, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  Dimensions,
  Platform,
  Animated,
  FlatList,
  AppState,
  Pressable,
} from 'react-native';

import FastImage from '@rocket.chat/react-native-fast-image';

import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler, LongPressGestureHandler, NativeViewGestureHandler } from 'react-native-gesture-handler';
import { setFacePasterInfo } from '../actions/story';
import Reanimated, { EasingNode, interpolateNode } from 'react-native-reanimated';

import _ from 'lodash';
import Carousel, { getInputRangeFromIndexes } from '../react-native-snap-carousel/src';
import I18n from '../i18n';

import AVService from '../AVService';
import { connect } from 'react-redux';
import { transform } from '@babel/core';
import CircleProgress from './CircleProgress';
const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const smallImageSize = 52;
const bigImageSize = 64;
const captureIcon2 = (width - 20) / 2;

const stateAttrsUpdate = ['pasterList', 'facePasterInfo'];

export type Props = {
  facePasterInfo: object;
};

type State = {
  pasterList: any[];
};

type PropsType = {
  facePasterInfo: {
    eid: any;
  };
  giveUpImage: any;
  snapToItem: Function;
  scrollPos: Animated.Value;
  scaleAnimated: Reanimated.Value;
};

class TopReset extends Component<PropsType> {
  constructor(props) {
    super(props);
  }
  render() {
    const { scrollPos, scaleAnimated } = this.props;
    return (
      <Reanimated.View
        style={[
          styles.clearBox,
          {
            transform: [
              {
                scale: interpolateNode(scaleAnimated, {
                  inputRange: [0, 0.00001, 1],
                  outputRange: [1, 0, 0],
                  extrapolate: 'clamp',
                }),
              },
            ],
          },
        ]}
      >
        <Animated.View
          style={[
            {
              transform: [
                {
                  scale: scrollPos.interpolate({
                    inputRange: [0, 1],
                    outputRange: [0, 1],
                    extrapolate: 'clamp',
                  }),
                },
              ],
            },
          ]}
        >
          {Platform.OS === 'android' ?
            <NativeViewGestureHandler
              disallowInterruption={true}
              shouldActivateOnStart={true}
              onHandlerStateChange={(event) => {
                //
                console.info("NativeViewGestureHandler", event.nativeEvent.state);
                if (event.nativeEvent.state === State.END) {
                  this.props.snapToItem?.(0);
                }
              }}
            >
              <View style={styles.clearIcon}>
                <FastImage source={this.props.giveUpImage} style={styles.clearIcon} />
              </View>
            </NativeViewGestureHandler>
            :
            <Pressable
              style={styles.clearIcon}
              onPress={() => {
                this.props.snapToItem?.(0);
              }}
            >
              <FastImage source={this.props.giveUpImage} style={styles.clearIcon} />
            </Pressable>}
        </Animated.View>
      </Reanimated.View>
    );
  }
}

class RenderBigCircle extends Component {
  constructor(props) {
    super(props);
  }
  shouldComponentUpdate(nextProps) {
    if (nextProps.pasterList && nextProps.pasterList !== this.props.pasterList) {
      return true;
    }
    return false;
  }
  render() {
    const { pasterList, scrollPos } = this.props;
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
                          outputRange: [(bigImageSize - smallImageSize) / 2, 0, -(bigImageSize - smallImageSize) / 2],

                          extrapolate: 'clamp',
                        }),
                      },
                    ],
                  },
                  i.eid == 0 && { backgroundColor: '#fff' },
                ]}
              >
                <FastImage
                  style={{ width: bigImageSize, height: bigImageSize, borderRadius: bigImageSize }}
                  source={{ uri: i.icon }}
                />
              </Animated.View>
            </View>
          );
        })}
      </Animated.View>
    );
  }
}
class RenderChildren extends Component {
  constructor(props) {
    super(props);
    this.longPressRef = React.createRef();

    this.isLongPress = false;
  }
  shouldComponentUpdate(nextProps) {
    if (nextProps.pasterList !== this.props.pasterList) {
      return true;
    }
    return false;
  }

  render() {
    const { pasterList, scrollPos, captureButtonImage } = this.props;
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
        {/* 安卓必须设置 shouldCancelWhenOutside={false}，否则松开后不会返回 State.END */}
        <LongPressGestureHandler
          ref={this.longPressRef}
          shouldCancelWhenOutside={false}
          onHandlerStateChange={({ nativeEvent }) => {
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
          <Animated.View
            style={[{ width: circleSize, height: circleSize, borderRadius: circleSize, overflow: 'hidden' }]}
          >
            <TapGestureHandler
              shouldCancelWhenOutside={true}
              onHandlerStateChange={({ nativeEvent }) => {
                if (nativeEvent.state === State.END) {
                  this.props.singlePress();
                }
              }}
            >
              <Animated.View>
                <View style={styles.bigCircleBox}></View>
                <RenderBigCircle {...this.props} />
              </Animated.View>
            </TapGestureHandler>
          </Animated.View>
        </LongPressGestureHandler>
      </Animated.View>
    );
  }
}
const RenderItem = React.memo((props) => {
  const { index, item, snapToItem } = props;
  const toItem = () => {
    snapToItem?.(index);
  };

  return (
    <Pressable delayLongPress={300} onPress={toItem}>
      <View>
        <View style={[styles.propStyle, styles.img]}>
          <FastImage style={styles.img} source={{ uri: item.icon }} />
        </View>
      </View>
    </Pressable>
  );
});
class CarouselWrapper extends Component<Props, State> {
  FlatListRef: any;
  scrollPos: Animated.Value;
  constructor(props) {
    super(props);
    this.pressLock = false;
    this.FlatListRef = React.createRef();
    this.scrollPos = new Animated.Value(0);
    this.state = {
      pasterList: [],
    };
    this.arcAngle = new Reanimated.Value(0);
    this.ani = null;
    this.startTime = null;
    this.endTime = null;
    this.scaleAnimated = new Reanimated.Value(0);
  }

  longPress = () => {
    if (this.pressLock) {
      return;
    }
    this.pressLock = true;
    this.startAnimate();
  };
  singlePress = async () => {
    this.startTime = null;
    if (this.pressLock) {
      return;
    }
    this.pressLock = true;
    await this.props.onCaptureImagePressed();
    setTimeout(() => {
      this.pressLock = false;
    }, 2500);
  };
  startAnimate = async () => {
    try {
      const success = await this.props.camera.current?.startRecording?.();
      this.props.hideBottomTools();
      if (!success) {
        this.props.myRef?.current?.show?.(`${I18n.t('Camera_failed_please_try_again')}`, 2000);
        this.pressLock = false;

        return;
      }
      this.startTime = Date.now();

      Reanimated.timing(this.scaleAnimated, {
        toValue: 1,
        easing: EasingNode.inOut(EasingNode.quad),
        duration: 200,
      }).start(({ finished }) => {
        if (finished) {
          this.ani = Reanimated.timing(this.arcAngle, {
            toValue: 360,
            easing: EasingNode.linear,
            duration: 1000 * 15,
          });
          this.ani.start(({ finished }) => {
            if (finished) {
              this.endTime = Date.now();
              this.shotCamera();
            }
          });
        }
      });
    } catch (e) {

    }
  };
  shotCamera = async () => {

    const recordingTime = this.endTime - this.startTime;

    this.props.showBottomTools();

    setTimeout(() => {
      this.reset();
      this.pressLock = false;
    }, 500);

    if (recordingTime <= 500) {
      //ios 录制时间过短stopRecording 不会返回 videoPath。这里不能 return ，必须执行 stopRecording，否则不能再次 startRecording
      this.props.myRef?.current?.show?.(`${I18n.t('record_time_small')}`, 500);
    }
    const videoPath = await this.props.camera.current?.stopRecording?.();
    if (recordingTime >= 500) {
      //安卓端目前小于500ms也会返回videoPath，这里统一处理，小于500ms 不进入编辑
      this.props.setShootData({
        fileType: 'video',
        videoPath,
        ShootSuccess: true,
      });
    }
  };
  reset = () => {
    this.ani?.stop();
    this.arcAngle?.setValue(0);
    this.startTime = null;
    this.endTime = null;
    Reanimated.timing(this.scaleAnimated, {
      toValue: 0,
      easing: EasingNode.inOut(EasingNode.quad),
      duration: 200,
    }).start();
  };
  stopAnimate = async () => {
    if (!this.startTime) {
      this.pressLock = false;
    }
    this.endTime = Date.now();
    this.shotCamera();
  };
  handleAppStateChange = (e) => {
    if (this.props.isDrawerOpen && this.props.type === 'story') {
      if (e.match(/inactive|background/)) {
        if (this.pressLock) {
          this.ani.stop();
          this.reset();
          this.pressLock = false;
        }
      } else {
        this.startAnimate;
      }
    }
  };
  componentDidMount() {
    AppState.addEventListener('change', this.handleAppStateChange);
    this.getPasterInfos();
    setTimeout(() => {
      AVService.enableHapticIfExist();
    }, 2000);
  }
  componentWillUnmount() {
    AppState.removeEventListener('change', this.handleAppStateChange);
  }
  shouldComponentUpdate(nextProps, nextState) {
    const stateUpdated = stateAttrsUpdate.some((key) => nextState[key] !== this.state[key]);
    if (stateUpdated) {
      setTimeout(() => {
        AVService.enableHapticIfExist();
      }, 3000);
      return true;
    }
    return false;
  }

  getPasterInfos = async () => {
    const pasters = await AVService.getFacePasterInfos({});
    pasters.forEach((item, index) => {
      if (index == 0) {
        return;
      }
      if (item.icon) {
        item.icon = item.icon.replace('http://', 'https://');
      }
      if (item.url) {
        item.url = item.url.replace('http://', 'https://');
      }
    });
    pasters.unshift({ eid: 0 });
    this.setState({
      pasterList: pasters,
    });
  };

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
    this.FlatListRef?.snapToItem?.(data);
  };
  selectionAsync = () => {
    if (this.props.enableCount.count < 5) {
      AVService.enableHapticIfExist();
      this.props.enableCount.count += 1;
    }
    this.props.haptics?.selectionAsync();
  };
  render() {
    const { pasterList } = this.state;

    let firstItem = pasterList.findIndex((i) => {
      return this.props.facePasterInfo.id === i.id;
    });
    if (firstItem === -1) {
      firstItem = 0;
    }
    if (!pasterList.length) return null;
    return (
      <View style={{ justifyContent: 'center' }}>
        <TopReset
          {...this.props}
          snapToItem={this.snapToItem}
          scrollPos={this.scrollPos}
          scaleAnimated={this.scaleAnimated}
        />
        <Reanimated.View
          style={{
            transform: [
              {
                scale: interpolateNode(this.scaleAnimated, {
                  inputRange: [0, 0.00001, 1],
                  outputRange: [1, 0, 0],
                  extrapolate: 'clamp',
                }),
              },
            ],
            zIndex: 200,
          }}
        >
          <Carousel
            ref={(flatList) => {
              this.FlatListRef = flatList;
            }}
            lockScrollWhileSnapping={true}
            snapToInterval={itemWidth}
            impactAsync={this.selectionAsync}
            enableMomentum={true}
            scrollInterpolator={this._scrollInterpolator}
            slideinterpolateNodedStyle={this._animatedStyles}
            enableSnap={true}
            data={pasterList}
            decelerationRate={'normal'}
            swipeThreshold={1}
            firstItem={firstItem}
            itemWidth={itemWidth}
            inactiveSlideOpacity={1}
            scrollPos={this.scrollPos}
            sliderWidth={width}
            slideStyle={{ justifyContent: 'center', alignItems: 'center' }}
            contentContainerCustomStyle={{ height: 120, justifyContent: 'center', alignItems: 'center' }}
            useScrollView={true}
            onSnapToItem={(slideIndex = 0) => {
              this.props.setFacePasterInfo(pasterList[slideIndex]);
            }}
            renderItem={(props) => <RenderItem {...props} snapToItem={this.snapToItem} />}
          >

            <RenderChildren
              {...this.props}
              pasterList={pasterList}
              scrollPos={this.scrollPos}
              longPress={this.longPress}
              singlePress={this.singlePress}
              startAnimate={this.startAnimate}
              stopAnimate={this.stopAnimate}
            />
          </Carousel>
        </Reanimated.View>

        <CircleProgress scale={this.scaleAnimated} arcAngle={this.arcAngle} />

        {/* 临时方案  安卓 拍摄不会触发 */}
      </View>
    );
  }
}
const ClMapStateToProps = (state) => ({
  facePasterInfo: state.shootStory.facePasterInfo,
});
const ClMapDispatchToProps = (dispatch) => ({
  setFacePasterInfo: (params) => dispatch(setFacePasterInfo(params)),
});
export default connect(ClMapStateToProps, ClMapDispatchToProps)(CarouselWrapper);
const styles = StyleSheet.create({
  bottomButtons: {
    flex: 1,
    overflow: 'visible',
    zIndex: 100,
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
    backgroundColor: '#000',
    opacity: 0.8,
  },
  clearIcon: {
    width: 32,
    height: 32,
  },
  clearBox: {
    alignItems: 'center',
    position: 'absolute',
    zIndex: 1,
    top: -32,
    left: (width - 32) / 2,
  },
  img: {
    width: smallImageSize,
    height: smallImageSize,
    borderRadius: smallImageSize,
  },
  bigCircleBox: {
    width: circleSize,
    height: circleSize,
    zIndex: 1,
    borderRadius: circleSize,
    borderWidth: 4,
    borderColor: 'white',
  },
});
