import { transform } from 'lodash';
import React, { Component, useEffect, useRef, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Image,
  Dimensions,
  Platform,
  SafeAreaView,
  ScrollView,
  FlatList,
  NativeModules,
  NativeEventEmitter,
  Button,
  Pressable,
  Animated,
} from 'react-native';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import eventBus from './EventBus';
import EventBus from './EventBus';

const { width, height } = Dimensions.get('window');
const MovingTimeLine = (props) => {
  // 这里有个标识符用于重新界定播放的范围
  const { leftAnimateRef } = props;
  const data = useRef({
    left: 0,
    right: 100,
  }).current;
  const leftRef = useRef(new Animated.Value(0)).current;
  const animationRef = useRef(null);
  const listRef = useRef(null);

  const startAnimation = () => {
    const animationSlider = Animated.sequence([
      Animated.timing(leftRef, {
        toValue: data.right,
        duration: 3000,
        useNativeDriver: true,
      }),
      Animated.timing(leftRef, {
        toValue: data.left,
        duration: 0,
        useNativeDriver: true,
      }),
    ]);
    animationRef.current = Animated.loop(animationSlider);
    animationRef.current.start();
  };
  const resetAnimation = () => {
    return leftRef.setValue(0);
  };
  const stopAnimationEvent = (e) => {
   
    if (e.draging) {
      resetAnimation();
      stopAnimation();
    } else {
      if (e.right !== null) {
        data.right = e.right;
      }
      startAnimation();
    }
  };
  const stopAnimation = () => {
    animationRef.current?.stop?.();
  };

  useEffect(() => {
    listRef.current = EventBus.addListener('dragingHandle', stopAnimationEvent);

    startAnimation();
    return () => {
      if (listRef.current) {
        EventBus.remove(listRef.current);
        listRef.current = null;
      }
      stopAnimation();
    };
  }, []);
  return (
    <Animated.View
      style={[
        styles.timeColumLineWrapper,
        {
          transform: [{ translateX: leftAnimateRef }],
        },
      ]}
    >
      <Animated.View
        style={[
          styles.timeColumLine,

          {
            transform: [{ translateX: leftRef }],
          },
        ]}
      ></Animated.View>
    </Animated.View>
  );
};
const BottomTimeLine = (props) => {
  const { duration } = props;
  const [dots, setDots] = useState([]);
  let span = 1;
  let acc = 3;
  if (duration > 30) {
    span = 5;
    acc = 1;
  }
  useEffect(() => {
    const data = [];

    const surplus = duration % span;
    const tail = surplus ? span : 0;
    const normalDuration = parseInt(duration / span) * span + tail;
    for (let i = 0; i <= normalDuration; i += span) {
      const mint = parseInt(i / 60);
      let second = parseInt(i % 60);
      if (second < 10) {
        second = `0${second}`;
      }
      data.push({
        time: i,
        timeFormat: `${mint}:${second}`,
      });
    }
    setDots(data);
  }, [duration]);
  return (
    <View style={{ flexDirection: 'row', zIndex: 99, justifyContent: 'space-between', marginTop: 11 }}>
      {dots.map((i) => {
        return (
          <View style={{ alignItems: 'center' }}>
            <View
              style={{
                width: 4,
                height: 4,
                backgroundColor: i.time % (15 / acc) ? '#8E8E8EFF' : '#BEBEBEFF',
                borderRadius: 4,
                marginBottom: 8,
              }}
            ></View>

            <Text
              style={{
                fontSize: 12,
                color: !(i.time % (15 / acc)) ? '#909090FF' : 'transparent',
                lineHeight: 17,
                fontWeight: '400',
                textAlign: 'center',
              }}
            >
              {i.timeFormat}
            </Text>
          </View>
        );
      })}
    </View>
  );
};
const ImagesBackList = (props) => {
  const [iamges] = useState([
    { url: 'https://video-message-001.paiyaapp.com/dhAgCqD36QCAhEqXj.jpg' },
    {
      url:
        'https://video-message-001.paiyaapp.com/default/868486a4f91a5a14556bfdca7a27ff2a/71971b58-8650-4e63-b6dd-5a9f17db5f50.jpg',
    },
    { url: 'https://video-message-001.paiyaapp.com/kQvcLGto6MA2Ewq7B.jpg' },
    {
      url:
        'https://video-message-001.paiyaapp.com/default/868486a4f91a5a14556bfdca7a27ff2a/71971b58-8650-4e63-b6dd-5a9f17db5f50.jpg',
    },
    { url: 'https://video-message-001.paiyaapp.com/dhAgCqD36QCAhEqXj.jpg' },
    {
      url:
        'https://video-message-001.paiyaapp.com/default/868486a4f91a5a14556bfdca7a27ff2a/71971b58-8650-4e63-b6dd-5a9f17db5f50.jpg',
    },
    // { url: 'https://video-message-001.paiyaapp.com/kQvcLGto6MA2Ewq7B.jpg' },
    // {
    //   url:
    //     'https://video-message-001.paiyaapp.com/default/868486a4f91a5a14556bfdca7a27ff2a/71971b58-8650-4e63-b6dd-5a9f17db5f50.jpg',
    // },
    // { url: 'https://video-message-001.paiyaapp.com/dhAgCqD36QCAhEqXj.jpg' },
    // {
    //   url:
    //     'https://video-message-001.paiyaapp.com/default/868486a4f91a5a14556bfdca7a27ff2a/71971b58-8650-4e63-b6dd-5a9f17db5f50.jpg',
    // },
    // { url: 'https://video-message-001.paiyaapp.com/kQvcLGto6MA2Ewq7B.jpg' },
    // {
    //   url:
    //     'https://video-message-001.paiyaapp.com/default/868486a4f91a5a14556bfdca7a27ff2a/71971b58-8650-4e63-b6dd-5a9f17db5f50.jpg',
    // },
    // { url: 'https://video-message-001.paiyaapp.com/dhAgCqD36QCAhEqXj.jpg' },
  ]);
  const onLayout = (e) => {
    //
  };
  const onScroll = Animated.event(
    [
      {
        nativeEvent: {
          contentOffset: {},
        },
      },
    ],
    {
      useNativeDriver: true,
      listener: (e) => {},
    },
  );
  return (
    <Animated.ScrollView
      onLayout={onLayout}
      // onScroll={onScroll}
      horizontal={true}
      // scrollEnabled={false}
      contentContainerStyle={{ marginTop: 30 }}
      style={{ paddingHorizontal: 50, backgroundColor: 'black' }}
    >
      <View>
        {props.children}
        <View style={styles.images}>
          {iamges.map((i) => {
            return <Image source={{ uri: i.url }} resizeMode={'cover'} style={styles.image}></Image>;
          })}
        </View>
        <BottomTimeLine duration={29} />
      </View>
    </Animated.ScrollView>
  );
};
const MovingHandler = (props) => {
  const offsetRef = useRef(0);
  const { animationRef, type, limits, tranXRef, tranXRRef, tranXLRef } = props;

  const onEnd = (e) => {
    animationRef.setOffset(offsetRef.current + tranXRef.current);
    tranXRef.current += offsetRef.current;
    animationRef.setValue(0);

    const data = {
      draging: false,
      right: tranXRRef.current - tranXLRef.current - 3,
    };

    eventBus.emit('dragingHandle', data);
  };
  const onStart = (e) => {
    offsetRef.current = 0;
    eventBus.emit('dragingHandle', { draging: true });
  };
  const onGestureEvent = Animated.event(
    [
      {
        nativeEvent: {
          // translationX: animationRef,
        },
      },
    ],
    {
      useNativeDriver: true,
      listener: (e) => {
        if (type === 'left') {
          // 不能少于0, 不能大于右侧的左边界
          if (e.nativeEvent.translationX + tranXLRef.current < 0) return;
          if (e.nativeEvent.translationX + tranXLRef.current > tranXRRef.current) {
            return;
          }
        } else {
          // 不能小于左侧，不能大于右侧边界以手机屏幕来计算
          if (e.nativeEvent.absoluteX > width - 15) return;
          if (e.nativeEvent.translationX + tranXRRef.current < tranXLRef.current) {
            return;
          }
        }
        offsetRef.current = e.nativeEvent.translationX;
        animationRef.setValue(e.nativeEvent.translationX);
      },
    },
  );
  // if (type !== 'left') return null;
  return (
    <PanGestureHandler onGestureEvent={onGestureEvent} onEnded={onEnd} onBegan={onStart}>
      <Animated.View
        style={[
          styles.handlerWrapper,
          {
            transform: [
              {
                translateX: animationRef,
              },
            ],
          },
        ]}
      >
        <Animated.View style={[styles.handler, props.type === 'left' ? styles.handlerLeft : styles.handlerRight]}>
          <View style={styles.handlerInner}></View>
        </Animated.View>
      </Animated.View>
    </PanGestureHandler>
  );
};
const CoverTrimer = (props) => {
  //
  const { leftAnimateRef, rightAnimateRef } = props;
  const { type } = props;
  const blockWidth = width * 4;
  return (
    <View style={styles.coverTrimer}>
      <Animated.View
        style={{
          backgroundColor: '#FFFFFF4D',
          height: 62,
          width: blockWidth,
          position: 'absolute',
          transform: [
            {
              translateX: Animated.divide(leftAnimateRef, blockWidth).interpolate({
                inputRange: [0, 1, 10],
                outputRange: [0 - blockWidth / 2, 0, 0],
                extrapolateLeft: 'clamp',
              }),
            },
            {
              scaleX: Animated.divide(leftAnimateRef, blockWidth),
            },
          ],
        }}
        key={'left'}
      ></Animated.View>
      <Animated.View
        style={{
          backgroundColor: 'transparent',
          width: blockWidth,
          height: 62,
          zIndex: 1,
          position: 'absolute',
          transform: [
            {
              translateX: leftAnimateRef,
            },
          ],
        }}
        key={'middle'}
      ></Animated.View>
      <Animated.View
        style={{
          backgroundColor: '#FFFFFF4D',
          width: blockWidth,
          height: 62,
          position: 'absolute',
          zIndex: 2,
          transform: [
            {
              translateX: rightAnimateRef,
            },
          ],
        }}
        key={'right'}
      ></Animated.View>
    </View>
  );
};

const Trimer = (props) => {
  const movingLeftRightData = {
    left: 0,
    right: 1,
  };

  let initRight = 60 * 4;
  const leftAnimateRef = useRef(new Animated.Value(0)).current;
  const tranXLRef = useRef(0);
  const tranXRRef = useRef(initRight);
  const rightAnimateRef = useRef(new Animated.Value(initRight)).current;
  const [layout, setLayout] = useState({});

  const [handlers] = useState([{ type: 'left' }, { type: 'right' }]);
  return (
    <ImagesBackList>
      <MovingTimeLine leftAnimateRef={leftAnimateRef} tranXLRef={tranXLRef} tranXRRef={tranXRRef} />

      {handlers.map((i) => {
        return (
          <>
            <MovingHandler
              type={i.type}
              key={`${i.type}-handler`}
              animationRef={i.type === 'left' ? leftAnimateRef : rightAnimateRef}
              tranXLRef={tranXLRef}
              tranXRRef={tranXRRef}
              tranXRef={i.type === 'left' ? tranXLRef : tranXRRef}
            />
          </>
        );
      })}
      <CoverTrimer leftAnimateRef={leftAnimateRef} rightAnimateRef={rightAnimateRef} />
    </ImagesBackList>
  );
};

const styles = StyleSheet.create({
  root: {},
  handlerWrapper: {
    position: 'absolute',
    zIndex: 19,
    justifyContent: 'flex-start',
    alignItems: 'flex-start',
  },
  handler: {
    width: 15,
    height: 62,
    backgroundColor: '#333333FF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  handlerLeft: {
    borderTopLeftRadius: 2,
    borderBottomLeftRadius: 2,
    left: -15,
  },
  handlerRight: {
    borderTopRightRadius: 2,
    borderBottomRightRadius: 2,
  },
  handlerInner: {
    backgroundColor: 'white',
    borderRadius: 1,
    height: 18,
    width: 2,
  },
  images: {
    flexDirection: 'row',
    height: 62,
    alignItems: 'center',
    backgroundColor: 'black',
  },
  image: {
    width: 60,
    height: 60,
  },
  timeColumLine: {
    borderRadius: 1.5,
    backgroundColor: 'white',
    width: 3,
    height: 68,
  },
  timeColumLineWrapper: {
    width: 3,
    height: 68,
    position: 'absolute',
    zIndex: 20,
    top: -(68 - 60) / 2,
  },
  coverTrimer: {
    // backgroundColor: 'red',
    width: '100%',
    flexDirection: 'row',
    position: 'absolute',
    zIndex: 3,
  },
});

export default Trimer;
