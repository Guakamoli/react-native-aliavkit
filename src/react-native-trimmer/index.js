import { transform } from 'lodash';
import React from 'react';
import {
  StyleSheet,
  ScrollView,
  Text,
  View,
  Dimensions,
  PanResponder,
  Animated,
  TouchableHighlight,
} from 'react-native';

import * as Arrow from './Arrow';

const { width: screenWidth, height: screenHeight } = Dimensions.get('window');

const MINIMUM_TRIM_DURATION = 2 * 1000;
const MAXIMUM_TRIM_DURATION = 5 * 60 * 1000;
const MAXIMUM_SCALE_VALUE = 1;
const ZOOM_MULTIPLIER = 1;
const INITIAL_ZOOM = 1;
const SCALE_ON_INIT_TYPE = 'trim-duration';
const SHOW_SCROLL_INDICATOR = false;
const CENTER_ON_LAYOUT = false;

//左右两端缩进
const TRACK_PADDING_OFFSET = 0;
//左右两边滑块宽度,可触摸滑动区域
const HANDLE_WIDTHS = 35;
//左右两边滑块宽度，展示出来能看到的宽度
const HANDLE_WIDTHS_SHOW = 20;
//进度指针区域的宽度
const SCRUBBER_WIDTHS = 8;
//进度指正展示出来的宽度
const SCRUBBER_WIDTHS_SHOW = 5;

const MARKER_INCREMENT = 1000;
const SPECIAL_MARKER_INCREMEMNT = 5;

const TRACK_BACKGROUND_COLOR = '#f2f6f5';
const TRACK_BORDER_COLOR = '#c8dad3';
const MARKER_COLOR = '#c8dad3';
const TINT_COLOR = '#93b5b3';
const SCRUBBER_COLOR = '#63707e';

export default class Trimmer extends React.Component {
  constructor(props) {
    super(props);

    let trackScale = props.initialZoomValue || INITIAL_ZOOM;
    if (props.scaleInOnInit) {
      const {
        maxTrimDuration = MAXIMUM_TRIM_DURATION,
        scaleInOnInitType = SCALE_ON_INIT_TYPE,
        trimmerRightHandlePosition,
        trimmerLeftHandlePosition,
      } = this.props;
      const isMaxDuration = scaleInOnInitType === 'max-duration';
      const trimDuration = isMaxDuration ? maxTrimDuration : trimmerRightHandlePosition - trimmerLeftHandlePosition;
      const smartScaleDivider = isMaxDuration ? 3 : 5; // Based on testing, 3 works better when the goal is to have the entire trimmer fit in the visible area
      const percentTrimmed = trimDuration / props.totalDuration;
      const smartScaleValue = 2 / percentTrimmed / smartScaleDivider;
      trackScale = this.clamp({ value: smartScaleValue, min: 1, max: props.maximumZoomLevel || MAXIMUM_SCALE_VALUE });
    }

    this.initiateAnimator();
    this.state = {
      scrubberTocuhing:false,
      scrubbing: false, // this value means scrubbing is currently happening
      trimming: false, // this value means the handles are being moved
      trackScale, // the scale factor for the track
      trimmingLeftHandleValue: 0,
      trimmingRightHandleValue: 0,
      internalScrubbingPosition: 0,
    };
  }

  clamp = ({ value, min, max }) => Math.min(Math.max(value, min), max);

  initiateAnimator = () => {
    this.scaleTrackValue = new Animated.Value(0);
    this.lastDy = 0;
    this.trackPanResponder = this.createTrackPanResponder();
    this.leftHandlePanResponder = this.createLeftHandlePanResponder();
    this.rightHandlePanResponder = this.createRightHandlePanResponder();
    this.scrubHandlePanResponder = this.createScrubHandlePanResponder();
  };

  createScrubHandlePanResponder = () =>
    PanResponder.create({
      onStartShouldSetPanResponder: (evt, gestureState) => true,
      onStartShouldSetPanResponderCapture: (evt, gestureState) => true,
      onMoveShouldSetPanResponder: (evt, gestureState) => true,
      onMoveShouldSetPanResponderCapture: (evt, gestureState) => true,
      onPanResponderGrant: (evt, gestureState) => {
        const { totalDuration, trackWidth,trimmerLeftHandlePosition, trimmerRightHandlePosition,} = this.props;
        const newBoundedScrubberPosition = (gestureState.x0 - TRACK_PADDING_OFFSET - HANDLE_WIDTHS + SCRUBBER_WIDTHS) / trackWidth * totalDuration
        this.setState({
          scrubbing: true,
          // internalScrubbingPosition: this.props.scrubberPosition,
          internalScrubbingPosition: newBoundedScrubberPosition,
        });
        this.handleScrubberPressIn();
      },
      onPanResponderMove: (evt, gestureState) => {
        const { totalDuration, trackWidth,trimmerLeftHandlePosition,trimmerRightHandlePosition } = this.props;
        let moveX = gestureState.moveX - TRACK_PADDING_OFFSET - HANDLE_WIDTHS + SCRUBBER_WIDTHS;
        if (moveX < 0) {
          moveX = 0;
        }
        if (moveX > trackWidth) {
          moveX = trackWidth;
        }
        const position = moveX / trackWidth * totalDuration

        const lowerBound = Math.max(0, trimmerLeftHandlePosition);
        const upperBound = trimmerRightHandlePosition;

        const newBoundedScrubberPosition = this.clamp({
          value: position,
          min: lowerBound,
          max: upperBound,
        });

        // const { trackScale } = this.state;
        // const { scrubberPosition, trimmerLeftHandlePosition, trimmerRightHandlePosition, totalDuration } = this.props;

        // const trackWidth = screenWidth * trackScale;
        // const calculatedScrubberPosition = (scrubberPosition / totalDuration) * trackWidth;

        // const newScrubberPosition = ((calculatedScrubberPosition + gestureState.dx) / trackWidth) * totalDuration;

        // const lowerBound = Math.max(0, trimmerLeftHandlePosition);
        // const upperBound = trimmerRightHandlePosition;

        // const newBoundedScrubberPosition = this.clamp({
        //   value: newScrubberPosition,
        //   min: lowerBound,
        //   max: upperBound,
        // });

        this.setState({ internalScrubbingPosition: newBoundedScrubberPosition });
      },
      //松开手指
      onPanResponderRelease: (evt, gestureState) => {
        this.handleScrubbingValueChange(this.state.internalScrubbingPosition);
        this.setState({ scrubbing: false });
      },
      onPanResponderTerminationRequest: (evt, gestureState) => true,
      onShouldBlockNativeResponder: (evt, gestureState) => true,
    });

  createRightHandlePanResponder = () =>
    PanResponder.create({
      onStartShouldSetPanResponder: (evt, gestureState) => true,
      onStartShouldSetPanResponderCapture: (evt, gestureState) => true,
      onMoveShouldSetPanResponder: (evt, gestureState) => true,
      onMoveShouldSetPanResponderCapture: (evt, gestureState) => true,
      onPanResponderGrant: (evt, gestureState) => {
        this.setState({
          trimming: true,
          trimmingRightHandleValue: this.props.trimmerRightHandlePosition,
          trimmingLeftHandleValue: this.props.trimmerLeftHandlePosition,
        });
        this.handleRightHandlePressIn();
      },
      onPanResponderMove: (evt, gestureState) => {
        const { trackScale } = this.state;
        const {
          trimmerRightHandlePosition,
          totalDuration,
          minimumTrimDuration = MINIMUM_TRIM_DURATION,
          maxTrimDuration = MAXIMUM_TRIM_DURATION,
        } = this.props;

        const trackWidth = screenWidth * trackScale;
        const calculatedTrimmerRightHandlePosition = (trimmerRightHandlePosition / totalDuration) * trackWidth;

        const newTrimmerRightHandlePosition =
          ((calculatedTrimmerRightHandlePosition + gestureState.dx) / trackWidth) * totalDuration;

        const lowerBound = minimumTrimDuration;
        const upperBound = totalDuration;

        const newBoundedTrimmerRightHandlePosition = this.clamp({
          value: newTrimmerRightHandlePosition,
          min: lowerBound,
          max: upperBound,
        });

        if (newBoundedTrimmerRightHandlePosition - this.state.trimmingLeftHandleValue >= maxTrimDuration) {
          this.setState({
            trimmingRightHandleValue: newBoundedTrimmerRightHandlePosition,
            trimmingLeftHandleValue: newBoundedTrimmerRightHandlePosition - maxTrimDuration,
          });
        } else if (newBoundedTrimmerRightHandlePosition - this.state.trimmingLeftHandleValue <= minimumTrimDuration) {
          // return;
          this.setState({
            trimmingRightHandleValue: newBoundedTrimmerRightHandlePosition,
            trimmingLeftHandleValue: newBoundedTrimmerRightHandlePosition - minimumTrimDuration,
          });
        } else {
          this.setState({ trimmingRightHandleValue: newBoundedTrimmerRightHandlePosition });
        }
      },
      onPanResponderRelease: (evt, gestureState) => {
        this.handleHandleSizeChange();
        this.setState({ trimming: false });
      },
      onPanResponderTerminationRequest: (evt, gestureState) => true,
      onShouldBlockNativeResponder: (evt, gestureState) => true,
    });

  createLeftHandlePanResponder = () =>
    PanResponder.create({
      onStartShouldSetPanResponder: (evt, gestureState) => true,
      onStartShouldSetPanResponderCapture: (evt, gestureState) => true,
      onMoveShouldSetPanResponder: (evt, gestureState) => true,
      onMoveShouldSetPanResponderCapture: (evt, gestureState) => true,
      onPanResponderGrant: (evt, gestureState) => {
        this.setState({
          trimming: true,
          trimmingRightHandleValue: this.props.trimmerRightHandlePosition,
          trimmingLeftHandleValue: this.props.trimmerLeftHandlePosition,
        });
        this.handleLeftHandlePressIn();
      },
      onPanResponderMove: (evt, gestureState) => {
        const { trackScale } = this.state;
        const {
          trimmerLeftHandlePosition,
          trimmerRightHandlePosition,
          totalDuration,
          minimumTrimDuration = MINIMUM_TRIM_DURATION,
          maxTrimDuration = MAXIMUM_TRIM_DURATION,
        } = this.props;

        const trackWidth = screenWidth * trackScale;
        const calculatedTrimmerLeftHandlePosition = (trimmerLeftHandlePosition / totalDuration) * trackWidth;

        const newTrimmerLeftHandlePosition =
          ((calculatedTrimmerLeftHandlePosition + gestureState.dx) / trackWidth) * totalDuration;

        const lowerBound = 0;
        const upperBound = totalDuration - minimumTrimDuration;

        const newBoundedTrimmerLeftHandlePosition = this.clamp({
          value: newTrimmerLeftHandlePosition,
          min: lowerBound,
          max: upperBound,
        });

        if (this.state.trimmingRightHandleValue - newBoundedTrimmerLeftHandlePosition >= maxTrimDuration) {
          this.setState({
            trimmingRightHandleValue: newBoundedTrimmerLeftHandlePosition + maxTrimDuration,
            trimmingLeftHandleValue: newBoundedTrimmerLeftHandlePosition,
          });
        } else if (this.state.trimmingRightHandleValue - newBoundedTrimmerLeftHandlePosition <= minimumTrimDuration) {
          this.setState({
            trimmingRightHandleValue: newBoundedTrimmerLeftHandlePosition + minimumTrimDuration,
            trimmingLeftHandleValue: newBoundedTrimmerLeftHandlePosition,
          });
        } else {
          this.setState({ trimmingLeftHandleValue: newBoundedTrimmerLeftHandlePosition });
        }
      },
      onPanResponderRelease: (evt, gestureState) => {
        this.handleHandleSizeChange();
        this.setState({ trimming: false });
      },
      onPanResponderTerminationRequest: (evt, gestureState) => true,
      onShouldBlockNativeResponder: (evt, gestureState) => true,
    });

  calculatePinchDistance = (x1, y1, x2, y2) => {
    let dx = Math.abs(x1 - x2);
    let dy = Math.abs(y1 - y2);
    const distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    return distance;
  };

  createTrackPanResponder = () =>
    PanResponder.create({
      // Ask to be the responder:
      onStartShouldSetPanResponder: (evt, gestureState) => !this.state.scrubbing && !this.state.trimming,
      onStartShouldSetPanResponderCapture: (evt, gestureState) => !this.state.scrubbing && !this.state.trimming,
      onMoveShouldSetPanResponder: (evt, gestureState) => !this.state.scrubbing && !this.state.trimming,
      onMoveShouldSetPanResponderCapture: (evt, gestureState) => !this.state.scrubbing && !this.state.trimming,
      onPanResponderGrant: (evt, gestureState) => {
        this.lastScaleDy = 0;
        const touches = evt.nativeEvent.touches || {};

        if (touches.length == 2) {
          const pinchDistance = this.calculatePinchDistance(
            touches[0].pageX,
            touches[0].pageY,
            touches[1].pageX,
            touches[1].pageY,
          );

          this.lastScalePinchDist = pinchDistance;
        }
      },
      onPanResponderMove: (evt, gestureState) => {
        const touches = evt.nativeEvent.touches;
        const { maximumZoomLevel = MAXIMUM_SCALE_VALUE, zoomMultiplier = ZOOM_MULTIPLIER } = this.props;

        if (touches.length == 2) {
          const pinchDistance = this.calculatePinchDistance(
            touches[0].pageX,
            touches[0].pageY,
            touches[1].pageX,
            touches[1].pageY,
          );

          if (this.lastScalePinchDist === undefined) {
            this.lastScalePinchDist = pinchDistance;
          }

          const stepValue = pinchDistance - this.lastScalePinchDist;
          this.lastScalePinchDist = pinchDistance;

          const scaleStep = (stepValue * zoomMultiplier) / screenHeight;
          const { trackScale } = this.state;

          const newTrackScaleValue = trackScale + scaleStep;
          const newBoundedTrackScaleValue = Math.max(Math.min(newTrackScaleValue, maximumZoomLevel), 1);

          this.setState({ trackScale: newBoundedTrackScaleValue });
        } else {
          const stepValue = gestureState.dy - this.lastScaleDy;
          this.lastScaleDy = gestureState.dy;

          const scaleStep = (stepValue * zoomMultiplier) / screenHeight;
          const { trackScale } = this.state;

          const newTrackScaleValue = trackScale + scaleStep;
          const newBoundedTrackScaleValue = Math.max(Math.min(newTrackScaleValue, maximumZoomLevel), 1);

          this.setState({ trackScale: newBoundedTrackScaleValue });
        }
      },
      onPanResponderTerminationRequest: (evt, gestureState) => true,
      onShouldBlockNativeResponder: (evt, gestureState) => true,
    });

  handleScrubbingValueChange = (newScrubPosition) => {
    this.state.scrubberTocuhing = true;
    const { onScrubbingComplete } = this.props;
    onScrubbingComplete && onScrubbingComplete(newScrubPosition | 0);
  };

  handleHandleSizeChange = () => {
    const { onHandleChange } = this.props;
    const { trimmingLeftHandleValue, trimmingRightHandleValue } = this.state;
    onHandleChange &&
      onHandleChange({
        leftPosition: trimmingLeftHandleValue | 0,
        rightPosition: trimmingRightHandleValue | 0,
      });
  };

  handleLeftHandlePressIn = () => {
    const { onLeftHandlePressIn } = this.props;
    onLeftHandlePressIn && onLeftHandlePressIn();
  };

  handleRightHandlePressIn = () => {
    const { onRightHandlePressIn } = this.props;
    onRightHandlePressIn && onRightHandlePressIn();
  };

  handleScrubberPressIn = () => {
    const { onScrubberPressIn } = this.props;
    onScrubberPressIn && onScrubberPressIn();
  };

  render() {
    const {
      maxTrimDuration,
      minimumTrimDuration,
      totalDuration,
      trimmerLeftHandlePosition,
      trimmerRightHandlePosition,
      scrubberPosition,
      trackBackgroundColor = TRACK_BACKGROUND_COLOR,
      trackBorderColor = TRACK_BORDER_COLOR,
      markerColor = MARKER_COLOR,
      tintColor = TINT_COLOR,
      scrubberColor = SCRUBBER_COLOR,
      centerOnLayout = CENTER_ON_LAYOUT,
      showScrollIndicator = SHOW_SCROLL_INDICATOR,
      trackHeight = 40,
      trackWidth = screenWidth * trackScale * 0.75,
    } = this.props;

    // if(maxTrimDuration < trimmerRightHandlePosition - trimmerLeftHandlePosition) {
    //   console.error('maxTrimDuration is less than trimRightHandlePosition minus trimmerLeftHandlePosition', {
    //     minimumTrimDuration, trimmerRightHandlePosition, trimmerLeftHandlePosition
    //   })
    //   return null
    // }

    if (minimumTrimDuration > trimmerRightHandlePosition - trimmerLeftHandlePosition) {
      console.error('minimumTrimDuration is less than trimRightHandlePosition minus trimmerLeftHandlePosition', {
        minimumTrimDuration,
        trimmerRightHandlePosition,
        trimmerLeftHandlePosition,
      });
      return null;
    }

    const {
      trimming,
      scrubbing,
      internalScrubbingPosition,
      trackScale,
      trimmingLeftHandleValue,
      trimmingRightHandleValue,
    } = this.state;

    // let trackWidth = screenWidth * trackScale
    // trackWidth = trackWidth * 0.75;
    if (isNaN(trackWidth)) {
      console.log(
        'ERROR render() trackWidth !== number. screenWidth',
        screenWidth,
        ', trackScale',
        trackScale,
        ', ',
        trackWidth,
      );
    }
    const trackBackgroundStyles = [
      styles.trackBackground,
      {
        width: trackWidth,
        backgroundColor: trackBackgroundColor,
        borderColor: trackBorderColor,
        height: trackHeight,
      },
    ];

    const leftPosition = trimming ? trimmingLeftHandleValue : trimmerLeftHandlePosition;
    const rightPosition = trimming ? trimmingRightHandleValue : trimmerRightHandlePosition;
    const scrubPosition = scrubbing ? internalScrubbingPosition : scrubberPosition;

    const boundedLeftPosition = Math.max(leftPosition, 0);

    let boundedScrubPosition = this.clamp({ value: scrubPosition, min: boundedLeftPosition, max: rightPosition });
    if(this.state.scrubberTocuhing){
      boundedScrubPosition = scrubPosition;
      this.state.scrubberTocuhing = false;
    }

    const boundedTrimTime = Math.max(rightPosition - boundedLeftPosition, 0);

    const actualTrimmerWidth = (boundedTrimTime / totalDuration) * trackWidth;
    const actualTrimmerOffset =
      (boundedLeftPosition / totalDuration) * trackWidth + TRACK_PADDING_OFFSET + HANDLE_WIDTHS;

    const actualScrubPosition = (boundedScrubPosition / totalDuration) * trackWidth +
      TRACK_PADDING_OFFSET + HANDLE_WIDTHS - SCRUBBER_WIDTHS / 2;
    //  TRACK_PADDING_OFFSET + HANDLE_WIDTHS / 2 + SCRUBBER_WIDTHS / 2 - SCRUBBER_WIDTHS_SHOW / 2;

    const onLayoutHandler = centerOnLayout
      ? {
        onLayout: () => {
          const centerOffset = actualTrimmerOffset + actualTrimmerWidth / 2 - screenWidth / 2;
          this.scrollView.scrollTo({ x: centerOffset, y: 0, animated: false });
        },
      }
      : null;

    if (isNaN(actualTrimmerWidth)) {
      console.log(
        'ERROR render() actualTrimmerWidth !== number. boundedTrimTime',
        boundedTrimTime,
        ', totalDuration',
        totalDuration,
        ', trackWidth',
        trackWidth,
      );
    }

    // const markers = new Array((totalDuration / MARKER_INCREMENT) | 0).fill(0) || [];
    const markers = new Array(11 | 0).fill(0) || [];
    return (
      <View style={styles.root}>
        <View
          style={[styles.horizontalScrollView, { transform: [{ scaleX: 1.0 }] }]}
        >
          <View style={trackBackgroundStyles}>
            <View style={styles.markersContainer}>
              {this.props.children}
            </View>
          </View>


          {
            typeof scrubberPosition === 'number'
              ? (
                <Animated.View style={[
                  styles.scrubberContainer,
                  { left: actualScrubPosition },
                  {
                    transform: [{ translateX: this.props.scrollAniRef }],
                  },
                ]}
                  hitSlop={{ top: 8, bottom: 8, right: 5, left: 5 }}
                  {...this.scrubHandlePanResponder.panHandlers}

                >
                  {/* <View style={[styles.scrubberTail]} /> */}
                  <View style={[styles.scrubberTail, { backgroundColor: scrubberColor }]} />
                </Animated.View>
              )
              : null
          }


          <View
            {...this.leftHandlePanResponder.panHandlers}
            style={[
              styles.handle,
              { left: actualTrimmerOffset - HANDLE_WIDTHS },
            ]}
            hitSlop={{ top: 8, bottom: 8, right: 8, left: 8 }}
          >
            <View style={[styles.leftHandleContainer, { backgroundColor: tintColor, }]}>
              <Arrow.Left />
            </View>
          </View>

          <View
            style={[
              styles.trimmer,
              { width: actualTrimmerWidth, left: actualTrimmerOffset },
              { borderColor: tintColor },
            ]}
          >
            <View style={[styles.selection, { backgroundColor: tintColor }]} />
          </View>

          <View
            {...this.rightHandlePanResponder.panHandlers}
            style={[
              styles.handle,
              { left: actualTrimmerOffset + actualTrimmerWidth },
            ]}
            hitSlop={{ top: 8, bottom: 8, right: 8, left: 8 }}
          >
            <View style={[styles.rightHandleContainer, { backgroundColor: tintColor, }]}>
              <Arrow.Right />
            </View>
          </View>

        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  root: {
    //  backgroundColor: 'red' 
    },
  horizontalScrollView: {
    overflow: 'hidden',
    position: 'relative',
  },
  trackBackground: {
    overflow: 'hidden',
    marginVertical: 10,
    backgroundColor: TRACK_BACKGROUND_COLOR,
    borderRadius: 5,
    borderWidth: 0,
    borderColor: TRACK_BORDER_COLOR,
    height: 50,
    marginHorizontal: HANDLE_WIDTHS + TRACK_PADDING_OFFSET,
  },
  trimmer: {
    position: 'absolute',
    left: TRACK_PADDING_OFFSET,
    top: 8,
    borderColor: TINT_COLOR,
    // borderWidth: 3,
    // backgroundColor: TRACK_BACKGROUND_COLOR,
    borderTopWidth: 3,
    borderBottomWidth: 3,
    // borderStartColor:TRACK_BACKGROUND_COLOR,
    height: 54,
  },
  handle: {
    zIndex: 1,
    position: 'absolute',
    width: HANDLE_WIDTHS,
    height: 54,
    // backgroundColor: TINT_COLOR,
    top: 8,
  },
  leftHandleContainer: {
    position: 'absolute',
    width: HANDLE_WIDTHS_SHOW,
    height: 54,
    marginStart: HANDLE_WIDTHS - HANDLE_WIDTHS_SHOW,
    borderTopLeftRadius: 10,
    borderBottomLeftRadius: 10,
  },
  rightHandleContainer: {
    position: 'absolute',
    width: HANDLE_WIDTHS_SHOW,
    height: 54,
    marginEnd: HANDLE_WIDTHS - HANDLE_WIDTHS_SHOW,
    borderTopRightRadius: 10,
    borderBottomRightRadius: 10,
  },
  selection: {
    opacity: 0.2,
    backgroundColor: TINT_COLOR,
    width: '100%',
    height: '100%',
  },
  markersContainer: {
    flexDirection: 'row',
    width: '100%',
    height: '100%',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  marker: {
    backgroundColor: MARKER_COLOR, // marker color,
    width: 2,
    height: 8,
    borderRadius: 2,
  },
  specialMarker: {
    height: 22,
  },
  hiddenMarker: {
    opacity: 0,
  },
  scrubberContainer: {
    zIndex: 2,
    // backgroundColor: 'rgba(0,0,255,0.2)',
    position: 'absolute',
    width: SCRUBBER_WIDTHS,
    height: 72,
    justifyContent: 'center',
    alignItems: 'center',
  },
  scrubberHead: {
    position: 'absolute',
    backgroundColor: SCRUBBER_COLOR,
    width: 6,
    marginTop: 4,
    height: 6,
    borderRadius: 6,
  },
  scrubberTail: {
    height: 62,
    width: SCRUBBER_WIDTHS_SHOW,
    // backgroundColor: 'rgba(0,255,0,0.6)',
    // backgroundColor: SCRUBBER_COLOR,
    borderRadius: SCRUBBER_WIDTHS_SHOW,
  },
});
