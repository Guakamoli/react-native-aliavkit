import React, { Component, ReactNode, RefObject } from 'react';
import { StyleSheet, View, Dimensions } from 'react-native';
import { PanGestureHandler, PinchGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import Animated, { EasingNode, lessOrEq } from 'react-native-reanimated';
import { timing } from './helpers/reanimatedTiming';
import { IImageViewerData } from './types';
import Video from 'react-native-video';
const windowWidth = Dimensions.get('window').width;
const windowHeight = windowWidth; //这个值根据实际的尺寸
interface IProps {
  image: string;
  areaWidth: number;
  areaHeight: number;
  imageWidth: number;
  imageHeight: number;
  minScale: number;
  onMove: ({ positionX, positionY, scale }: IImageViewerData) => void;
  containerColor?: string;
  videoFile?: string;
  disablePin?: boolean;
  imageBackdropColor?: string;
  overlay?: ReactNode;
}

const defaultProps = {
  containerColor: 'black',
  imageBackdropColor: 'black',
};

const {
  Value,
  event,
  block,
  set,
  cond,
  eq,
  and,
  neq,
  greaterThan,
  greaterOrEq,
  lessThan,
  add,
  sub,
  min,
  max,
  multiply,
  divide,
  call,
  Extrapolate,
} = Animated;

const styles = StyleSheet.create({
  panGestureInner: {
    // ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
  },

  imageWrapper: {
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },

  image: {},
  shadow: {
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 0,
    },
    shadowOpacity: 0.9,
    shadowRadius: 2,

    elevation: 24,
  },
});

class ImageViewer extends Component<IProps> {
  pinchRef: RefObject<PinchGestureHandler>;

  dragRef: RefObject<PanGestureHandler>;

  translateX: Animated.Value<number>;

  translateY: Animated.Value<number>;
  gridX: Animated.Value<number>;

  gridY: Animated.Value<number>;
  gridOpacity: Animated.Value<number>;
  gridWidth: Animated.Value<number>;
  gridHeight: Animated.Value<number>;
  scale: Animated.Value<number>;

  onTapGestureEvent: (...args: any[]) => void;

  onPanGestureEvent: (...args: any[]) => void;

  onPinchGestureEvent: (...args: any[]) => void;

  static defaultProps = defaultProps;

  constructor(props: IProps) {
    super(props);
    let { areaWidth, areaHeight, imageWidth, imageHeight, minScale, propsScale,propsX,propsY} = props;
    minScale = minScale;

    this.pinchRef = React.createRef();
    this.dragRef = React.createRef();
    this.translateX = new Value(propsX?propsX:0);
    this.translateY = new Value(propsY?propsY:0);
    this.scale = new Value(propsScale?propsScale:1);
    this.enableXRef = new Value(this.props.disablePin ? 0 : 1);
    const timingDefaultParams = {
      duration: 200,
      easing: EasingNode.linear,
    };

    const maxScale = 2;

    const offsetX = new Value(0);
    const offsetY = new Value(0);
    const offsetZ = new Value(minScale);

    const viewerAreaWidth = new Value(areaWidth);
    const viewerAreaHeight = new Value(areaHeight);

    const viewerImageWidth = new Value(imageWidth);
    const viewerImageHeight = new Value(imageHeight);

    const maxX = new Value(0);
    const negMaxX = new Value(0);
    this.negMaxX = negMaxX;
    this.maxX = maxX;
    const maxY = new Value(0);
    const negMaxY = new Value(0);
    this.state = {
      showOverlay: false,
    };
    const horizontalMax = divide(divide(sub(multiply(viewerImageWidth, this.scale), viewerAreaWidth), 2), this.scale);

    const verticalMax = divide(divide(sub(multiply(viewerImageHeight, this.scale), viewerAreaHeight), 2), this.scale);
    const scaledWidth = multiply(viewerImageWidth, this.scale);
    const scaledHeight = multiply(viewerImageHeight, this.scale);
    this.scaledWidth = scaledWidth;
    this.scaledHeight = scaledHeight;
    const gridHide = new Value(0);

    // 网格专用
    this.gridX = new Value(0);
    this.gridY = new Value(0);
    this.gridOpacity = new Value(0);
    this.gridWidth = new Value(imageWidth);
    this.gridHeight = new Value(imageHeight);
    this.onTapGestureEvent = event([
      {
        nativeEvent: ({ state }: { state: State }) =>
          block([
            cond(eq(state, State.END), [
              set(offsetZ, new Value(minScale)),
              set(offsetX, new Value(0)),
              set(offsetY, new Value(0)),
              set(
                this.scale,
                timing({
                  from: this.scale,
                  to: minScale,
                  ...timingDefaultParams,
                }),
              ),

              set(
                this.translateX,
                timing({
                  from: this.translateX,
                  to: 0,
                  ...timingDefaultParams,
                }),
              ),

              set(
                this.translateY,
                timing({
                  from: this.translateY,
                  to: 0,
                  ...timingDefaultParams,
                }),
              ),
            ]),
            cond(and(eq(state, State.BEGAN), neq(this.gridOpacity, new Value(100))), [
              set(this.gridOpacity, new Value(1)),
              set(gridHide, new Value(1)),
            ]),
            cond(and(eq(state, State.END), eq(gridHide, new Value(1))), [
              set(gridHide, new Value(0)),

              set(this.gridOpacity, new Value(0)),
            ]),
            cond(and(eq(state, State.FAILED), eq(gridHide, new Value(1))), [
              set(gridHide, new Value(0)),

              set(this.gridOpacity, new Value(0)),
            ]),
          ]),
      },
    ]);

    this.onPanGestureEvent = event([
      {
        nativeEvent: ({
          translationX,
          translationY,
          state,
        }: {
          translationX: number;
          translationY: number;
          state: State;
        }) =>
          block([
            cond(eq(state, State.ACTIVE), [
              set(this.translateX, this.props.disablePin ? offsetX : add(divide(translationX, this.scale), offsetX)),
              set(this.translateY, add(divide(translationY, this.scale), offsetY)),

              set(maxX, horizontalMax),
              set(negMaxX, multiply(horizontalMax, new Value(-1))),
              set(maxY, verticalMax),
              set(negMaxY, multiply(verticalMax, new Value(-1))),

              cond(eq(this.gridOpacity, new Value(0)), [set(this.gridOpacity, new Value(1))]),
            ]),

            cond(
              and(
                eq(state, State.END),
                greaterOrEq(scaledWidth, multiply(viewerAreaWidth, this.props.minScale)),
                greaterOrEq(this.scale, new Value(minScale)),
              ),

              cond(
                and(
                  greaterOrEq(this.enableXRef, 1),
                  lessThan(this.translateX, negMaxX),
                  greaterOrEq(this.scale, new Value(minScale)),
                ),
                [
                  set(
                    this.translateX,
                    timing({
                      from: this.translateX,
                      to: 0,
                      ...timingDefaultParams,
                    }),
                  ),
                ],
                cond(
                  and(
                    greaterOrEq(this.enableXRef, 1),
                    greaterThan(this.translateX, maxX),
                    greaterOrEq(this.scale, new Value(minScale)),
                  ),
                  [
                    set(
                      this.translateX,
                      timing({
                        from: this.translateX,
                        to: maxX,
                        ...timingDefaultParams,
                      }),
                    ),
                  ],
                ),
              ),
            ),

            cond(
              and(
                eq(state, State.END),
                greaterOrEq(scaledHeight, viewerAreaHeight),
                greaterOrEq(this.scale, new Value(minScale)),
              ),
              cond(
                and(lessThan(this.translateY, negMaxY), greaterOrEq(this.scale, new Value(minScale))),
                [
                  set(negMaxY, multiply(verticalMax, new Value(-1))),
                  set(
                    this.translateY,
                    timing({
                      from: this.translateY,
                      to: negMaxY,
                      ...timingDefaultParams,
                    }),
                  ),
                ],
                cond(and(greaterThan(this.translateY, maxY), greaterOrEq(this.scale, new Value(minScale))), [
                  set(maxY, verticalMax),
                  set(
                    this.translateY,
                    timing({
                      from: this.translateY,
                      to: maxY,
                      ...timingDefaultParams,
                    }),
                  ),
                ]),
              ),
            ),

            cond(and(eq(state, State.END), greaterOrEq(this.scale, new Value(minScale))), [
              set(offsetX, this.translateX),
              set(offsetY, this.translateY),
            ]),
          ]),
      },
    ]);

    this.onPinchGestureEvent = event([
      {
        nativeEvent: ({ scale, state }: { scale: number; state: State }) =>
          block([
            cond(and(eq(state, State.ACTIVE), greaterThan(scale, new Value(0.6))), [
              set(this.scale, multiply(offsetZ, scale)),
            ]),

            cond(eq(state, State.END), [
              set(offsetZ, this.scale),

              set(maxX, horizontalMax),
              set(negMaxX, multiply(horizontalMax, new Value(-1))),

              set(maxY, verticalMax),
              set(negMaxY, multiply(verticalMax, new Value(-1))),
            ]),

            cond(and(eq(state, State.END), greaterThan(this.scale, new Value(maxScale))), [
              set(offsetZ, new Value(maxScale)),

              set(
                this.scale,
                timing({
                  from: this.scale,
                  to: maxScale,
                  ...timingDefaultParams,
                }),
              ),
            ]),
            cond(and(eq(state, State.END), lessThan(this.scale, new Value(minScale))), [
              set(offsetZ, new Value(minScale)),

              set(
                this.scale,
                timing({
                  from: this.scale,
                  to: minScale,
                  ...timingDefaultParams,
                }),
              ),
            ]),
            cond(and(eq(state, State.END)), [set(gridHide, new Value(0)), set(this.gridOpacity, new Value(0))]),
          ]),
      },
    ]);
  }
  // shouldComponentUpdate(nextProps){
  //   const { disablePin} = this.props;

  //   if (nextProps.disablePin !== disablePin) {
  //     this.enableXRef = new Value(this.props.disablePin? 0: 1)
  //     return false
  //   }
  //   return true
  // }
  componentDidUpdate(prevProps: IProps) {
    const { propsScale, disablePin, isChangeScale } = this.props;

    if (isChangeScale) {
      this.props.setChangeScale();
      this.scale.setValue(propsScale);
      return;
    }

    if (propsScale && prevProps.propsScale !== propsScale) {
      this.scale.setValue(propsScale);
    }
  }
  handleMove = (args: readonly number[]): void => {
    const { onMove } = this.props;

    const positionX = args[0];
    const positionY = args[1];
    const scale = args[2];
    onMove({ positionX, positionY, scale });
  };

  render() {
    const {
      image,
      imageWidth,
      imageHeight,
      areaWidth,
      areaHeight,
      containerColor,
      imageBackdropColor,
      overlay,
      videoFile,
      minScale,
      videoPaused,
      srcSize,
    } = this.props;
    const imageSrc = {
      uri: image,
    };
    const showCover = true;
    const containerStyles = [
      styles.panGestureInner,
      {
        backgroundColor: containerColor,
      },
    ];

    const areaStyles = {
      width: areaWidth,
      height: areaHeight,
      backgroundColor: imageBackdropColor,
    };

    const overlayContainerStyle = [
      styles.image,
      {
        position: 'absolute' as 'absolute',
        opacity: this.gridOpacity,
        width: imageWidth,
        height: imageHeight,
        zIndex: 99,
        overflow: 'hidden',
        transform: [
          {
            scale: this.scale,
          },
          {
            translateX: this.translateX,
          },
          {
            translateY: this.translateY,
          },
        ],
      },
    ];

    const imageWrapperStyles = [styles.imageWrapper, areaStyles];

    const imageStyles = [
      styles.image,
      {
        width: imageWidth,
        height: imageHeight,
        transform: [
          {
            scale: this.scale,
          },
          {
            translateX: this.translateX,
          },
          {
            translateY: this.translateY,
          },
        ],
      },
    ];
    const rowData = srcSize;
    // 这里裁减策略修改为超出一定比例的时候自动裁切
    let videoBoxWidth = windowWidth;
    let videoBoxHeight = windowWidth;
    let videoWidth = windowWidth;
    let videoHeight = windowWidth;
    // 只处理小于和大于的情况
    const wHRatio = rowData.width / rowData.height;
    if (wHRatio > 2) {
      videoBoxWidth = windowWidth;
      videoBoxHeight = windowWidth / 2;
      videoHeight = windowWidth / 2;
      videoWidth = videoHeight * wHRatio;
    } else if (wHRatio < 4 / 5) {
      videoBoxWidth = windowWidth;
      videoBoxHeight = (windowWidth / 4) * 5;
      videoWidth = windowWidth;
      videoHeight = videoWidth / wHRatio;
    } else {
      // 宽小于高但是没有超出限制,以屏幕宽乘以比例为主
      videoBoxWidth = windowWidth;
      videoBoxHeight = windowWidth / wHRatio;
      videoWidth = windowWidth;
      videoHeight = windowWidth / wHRatio;
    }
    const videoStyle = {
      width: videoWidth,
      height: videoHeight,
    };
    const videoBoxStyle = {
      width: windowWidth,
      height: windowWidth,
    };
    return (
      <>
        <Animated.Code>
          {() =>
            block([call([this.translateX, this.translateY, this.scale, this.negMaxX, this.maxX], this.handleMove)])
          }
        </Animated.Code>
        <PanGestureHandler
          ref={this.dragRef}
          simultaneousHandlers={this.pinchRef}
          minPointers={1}
          maxPointers={2}
          avgTouches
          onGestureEvent={this.onPanGestureEvent}
          onHandlerStateChange={this.onPanGestureEvent}
        >
          <Animated.View style={containerStyles}>
            <TapGestureHandler numberOfTaps={2} onHandlerStateChange={this.onTapGestureEvent}>
              <Animated.View style={areaStyles}>
                <PinchGestureHandler
                  ref={this.pinchRef}
                  enabled={!this.props.disablePin}
                  onGestureEvent={this.onPinchGestureEvent}
                  onHandlerStateChange={this.onPinchGestureEvent}
                >
                  <Animated.View style={imageWrapperStyles} collapsable={false}>
                    {videoFile ? (
                      // <Animated.View style={imageStyles}>
                      //   <Video
                      //     paused={videoPaused}
                      //     repeat={true}
                      //     muted={true}
                      //     source={{ uri: videoFile }}
                      //     style={{
                      //       width: imageWidth,
                      //       height: imageHeight,
                      //     }}
                      //   />
                      // </Animated.View>
                      <Animated.View style={[videoBoxStyle, { justifyContent: 'center', alignItems: 'center' }]}>
                        <Video
                          posterResizeMode={"cover"}
                          resizeMode={"cover"}
                          paused={videoPaused}
                          repeat={true}
                          muted={false}
                          source={{ uri: videoFile }}
                          style={[videoStyle]}
                        />
                      </Animated.View>
                    ) : (
                      <Animated.Image style={imageStyles} source={imageSrc} />
                    )}
                    {showCover && !videoFile ? (
                      <Animated.View style={overlayContainerStyle}>
                        <Animated.View style={{ height: '100%', width: '100%' }}>
                          <Animated.View
                            style={[
                              {
                                left: areaWidth / 3 + Math.abs(imageWidth - areaWidth) / 2,
                                width: StyleSheet.hairlineWidth,
                                height: '100%',
                                backgroundColor: 'rgba(255,255,255,0.5)',
                                position: 'absolute',
                                transform: [
                                  {
                                    translateX: add(
                                      multiply(this.translateX, -1),
                                      divide(
                                        multiply(
                                          max(sub(this.scale, 1), 0),
                                          multiply(
                                            (areaWidth / 3) * 2 + (imageWidth - areaWidth) / 2 - imageWidth / 2,
                                            1,
                                          ),
                                        ),
                                        this.scale,
                                      ),
                                    ),
                                  },
                                  {
                                    scaleX: divide(1, this.scale),
                                  },
                                ],
                              },
                              styles.shadow,
                            ]}
                          ></Animated.View>
                          <Animated.View
                            style={[
                              {
                                left: (areaWidth / 3) * 2 + Math.abs(imageWidth - areaWidth) / 2,
                                width: StyleSheet.hairlineWidth,
                                height: '100%',
                                backgroundColor: 'rgba(255,255,255,0.5)',
                                position: 'absolute',

                                transform: [
                                  {
                                    translateX: add(
                                      multiply(this.translateX, -1),
                                      divide(
                                        multiply(
                                          max(sub(this.scale, 1), 0),
                                          multiply(
                                            (areaWidth / 3) * 2 + (imageWidth - areaWidth) / 2 - imageWidth / 2,
                                            -1,
                                          ),
                                        ),
                                        this.scale,
                                      ),
                                    ),
                                  },
                                  {
                                    scaleX: divide(1, this.scale),
                                  },
                                ],
                              },
                              styles.shadow,
                            ]}
                          ></Animated.View>
                          <Animated.View
                            style={[
                              {
                                top: areaWidth / 3 + (imageHeight - areaWidth) / 2,
                                width: '100%',
                                height: StyleSheet.hairlineWidth,
                                backgroundColor: 'rgba(255,255,255,0.5)',
                                position: 'absolute',
                                transform: [
                                  {
                                    translateY: add(
                                      multiply(this.translateY, -1),
                                      divide(
                                        multiply(
                                          max(sub(this.scale, 1), 0),
                                          multiply(
                                            (areaWidth / 3) * 2 + (imageHeight - areaWidth) / 2 - imageHeight / 2,
                                            1,
                                          ),
                                        ),
                                        this.scale,
                                      ),
                                    ),
                                  },
                                  {
                                    scaleY: divide(1, this.scale),
                                  },
                                ],
                              },
                              styles.shadow,
                            ]}
                          ></Animated.View>
                          <Animated.View
                            style={[
                              {
                                top: (areaWidth / 3) * 2 + (imageHeight - areaWidth) / 2,
                                width: '100%',
                                height: StyleSheet.hairlineWidth,
                                backgroundColor: 'rgba(255,255,255,0.5)',
                                position: 'absolute',
                                transform: [
                                  {
                                    translateY: add(
                                      multiply(this.translateY, -1),
                                      divide(
                                        multiply(
                                          max(sub(this.scale, 1), 0),
                                          multiply(
                                            (areaWidth / 3) * 2 + (imageHeight - areaWidth) / 2 - imageHeight / 2,
                                            -1,
                                          ),
                                        ),
                                        this.scale,
                                      ),
                                    ),
                                  },
                                  {
                                    scaleY: divide(1, this.scale),
                                  },
                                ],
                              },
                              styles.shadow,
                            ]}
                          ></Animated.View>
                        </Animated.View>
                      </Animated.View>
                    ) : null}
                  </Animated.View>
                </PinchGestureHandler>
              </Animated.View>
            </TapGestureHandler>
          </Animated.View>
        </PanGestureHandler>
      </>
    );
  }
}

export default ImageViewer;
