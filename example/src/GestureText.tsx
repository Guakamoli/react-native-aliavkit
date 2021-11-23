import React, { Component } from 'react';
import {
  Animated,
  StyleProp,
  StyleSheet,
  View,
  ViewStyle,
  KeyboardAvoidingView,
  TextInput,
  Text,
  Platform,
  TouchableWithoutFeedback,
  Button,
  Keyboard,
} from 'react-native';

import {
  State,
  PanGestureHandler,
  PanGestureHandlerGestureEvent,
  PanGestureHandlerStateChangeEvent,
  PinchGestureHandler,
  PinchGestureHandlerGestureEvent,
  PinchGestureHandlerStateChangeEvent,
  RotationGestureHandler,
  RotationGestureHandlerGestureEvent,
  RotationGestureHandlerStateChangeEvent,
} from 'react-native-gesture-handler';

type CaptionInfo = {
  text: string;
  // fontName: string;
  // fontStyle: string; // normal | italic | bold
  // color: string;      //文字颜色
  // textAlignment: string; //left, center, right
  // backgroundColor: string; //背景颜色
  // startTime: number;
  // duration: number;
  center: { x: number; y: number };
  rotate: number;
  scale: number;
};

type GestureTextProps = {
  minDist?: number;
  boxStyle?: StyleProp<ViewStyle>;
  onTextMove: (info: CaptionInfo) => void;
};

type GestureTextState = {
  text: string;
};

export default class GestureText extends Component<GestureTextProps, GestureTextState> {
  private panRef = React.createRef<PanGestureHandler>();
  private rotationRef = React.createRef<RotationGestureHandler>();
  private pinchRef = React.createRef<PinchGestureHandler>();

  private baseScale: Animated.Value;
  private pinchScale: Animated.Value;
  private scale: Animated.AnimatedMultiplication;
  private lastScale: number;
  private onPinchGestureEvent: (event: PinchGestureHandlerGestureEvent) => void;

  private rotate: Animated.Value;
  private rotateStr: Animated.AnimatedInterpolation;
  private lastRotate: number;
  private onRotateGestureEvent: (event: RotationGestureHandlerGestureEvent) => void;

  private translateX: Animated.Value;
  private translateY: Animated.Value;
  private lastOffset: { x: number; y: number };
  private onPanGestureEvent: (event: PanGestureHandlerGestureEvent) => void;

  constructor(props: GestureTextProps) {
    super(props);
    this.translateX = new Animated.Value(0);
    this.translateY = new Animated.Value(0);
    this.lastOffset = { x: 0, y: 0 };

    this.onPanGestureEvent = Animated.event(
      [
        {
          nativeEvent: {
            translationX: this.translateX,
            translationY: this.translateY,
          },
        },
      ],
      { useNativeDriver: true },
    );

    /* Pinching */
    this.baseScale = new Animated.Value(1);
    this.pinchScale = new Animated.Value(1);
    this.scale = Animated.multiply(this.baseScale, this.pinchScale);
    this.lastScale = 1;
    this.onPinchGestureEvent = Animated.event([{ nativeEvent: { scale: this.pinchScale } }], { useNativeDriver: true });

    /* Rotation */
    this.rotate = new Animated.Value(0);
    this.rotateStr = this.rotate.interpolate({
      inputRange: [-100, 100],
      outputRange: ['-100rad', '100rad'],
    });
    this.lastRotate = 0;
    this.onRotateGestureEvent = Animated.event([{ nativeEvent: { rotation: this.rotate } }], { useNativeDriver: true });

    this.state = {
      text: '',
    };
  }

  private onPanHandlerStateChange = (event: PanGestureHandlerStateChangeEvent) => {
    if (event.nativeEvent.oldState === State.ACTIVE) {
      Keyboard.dismiss();
      this.lastOffset.x += event.nativeEvent.translationX;
      this.lastOffset.y += event.nativeEvent.translationY;
      this.translateX.setOffset(this.lastOffset.x);
      this.translateX.setValue(0);
      this.translateY.setOffset(this.lastOffset.y);
      this.translateY.setValue(0);
      console.log('-----: lastOffset.x:', this.lastOffset.x, 'lastOffset.y :', this.lastOffset.y);
      this.props.onTextMove({
        text: this.state.text,
        center: { x: this.lastOffset.x, y: this.lastOffset.y },
        rotate: this.lastRotate,
        scale: this.lastScale,
      });
    }
  };

  private onRotateHandlerStateChange = (event: RotationGestureHandlerStateChangeEvent) => {
    if (event.nativeEvent.oldState === State.ACTIVE) {
      Keyboard.dismiss();
      this.lastRotate += event.nativeEvent.rotation;
      this.rotate.setOffset(this.lastRotate);
      this.rotate.setValue(0);
      console.log('-----: rotate', this.lastRotate);
      this.props.onTextMove({
        text: this.state.text,
        center: { x: this.lastOffset.x, y: this.lastOffset.y },
        rotate: this.lastRotate,
        scale: this.lastScale,
      });
    }
  };
  private onPinchHandlerStateChange = (event: PinchGestureHandlerStateChangeEvent) => {
    if (event.nativeEvent.oldState === State.ACTIVE) {
      Keyboard.dismiss();
      this.lastScale *= event.nativeEvent.scale;
      this.baseScale.setValue(this.lastScale);
      this.pinchScale.setValue(1);
      console.log('-----: pinch scale: ', this.lastScale);
      this.props.onTextMove({
        text: this.state.text,
        center: { x: this.lastOffset.x, y: this.lastOffset.y },
        rotate: this.lastRotate,
        scale: this.lastScale,
      });
    }
  };

  render() {
    return (
      <View style={styles.container}>
        <PanGestureHandler
          {...this.props}
          ref={this.panRef}
          onGestureEvent={this.onPanGestureEvent}
          onHandlerStateChange={this.onPanHandlerStateChange}
          minDist={this.props.minDist}
        >
          <Animated.View style={styles.wrapper}>
            <RotationGestureHandler
              ref={this.rotationRef}
              simultaneousHandlers={this.pinchRef}
              onGestureEvent={this.onRotateGestureEvent}
              onHandlerStateChange={this.onRotateHandlerStateChange}
            >
              <Animated.View style={styles.wrapper}>
                <PinchGestureHandler
                  ref={this.pinchRef}
                  simultaneousHandlers={this.rotationRef}
                  onGestureEvent={this.onPinchGestureEvent}
                  onHandlerStateChange={this.onPinchHandlerStateChange}
                >
                  <Animated.View
                    style={[
                      styles.box,
                      {
                        transform: [
                          { translateX: this.translateX },
                          { translateY: this.translateY },
                          { scale: this.scale },
                          { rotate: this.rotateStr },
                        ],
                      },
                      this.props.boxStyle,
                    ]}
                  >
                    <TextInput
                      placeholder='Username'
                      style={[styles.textInput, { fontSize: 18.0 * this.lastScale }]}
                      multiline={true}
                      value={this.state.text}
                      onChangeText={(text) => this.setState({ text })}
                    />
                  </Animated.View>
                </PinchGestureHandler>
              </Animated.View>
            </RotationGestureHandler>
          </Animated.View>
        </PanGestureHandler>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  box: {
    alignSelf: 'center',
    // backgroundColor: 'plum',
    zIndex: 200,
  },
  textInput: {
    width: 100,
    height: 40,
    fontSize: 18,
    margin: 35,
    backgroundColor: '#f00',
    textAlign: 'center',
  },
  wrapper: {
    flex: 1,
  },
});
