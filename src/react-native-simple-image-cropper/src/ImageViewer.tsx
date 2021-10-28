import React, { Component, ReactNode, RefObject } from 'react';
import { StyleSheet, View } from 'react-native';
import { PanGestureHandler, PinchGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import Animated, { Easing, lessOrEq } from 'react-native-reanimated';
import { timing } from './helpers/reanimatedTiming';
import { IImageViewerData } from './types';
import Video from 'react-native-video';

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

    const { areaWidth, areaHeight, imageWidth, imageHeight, minScale } = props;
    this.pinchRef = React.createRef();
    this.dragRef = React.createRef();
    console.info(areaWidth, areaHeight, imageWidth, imageHeight, minScale);
    this.translateX = new Value(0);
    this.translateY = new Value(0);
    this.scale = new Value(minScale);

    const timingDefaultParams = {
      duration: 200,
      easing: Easing.linear,
    };

    const maxScale = minScale + 3;

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
    // const horizontalMax = new Value(20);
    // const verticalMax = new Value(20);
    set(maxX, horizontalMax);
    set(negMaxX, multiply(horizontalMax, new Value(-1)));
    set(maxY, verticalMax);
    set(negMaxY, multiply(verticalMax, new Value(-1)));
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
              set(this.translateX, add(divide(translationX, this.scale), offsetX)),
              set(this.translateY, add(divide(translationY, this.scale), offsetY)),

              set(maxX, horizontalMax),
              set(negMaxX, multiply(horizontalMax, new Value(-1))),
              set(maxY, verticalMax),
              set(negMaxY, multiply(verticalMax, new Value(-1))),
              cond(eq(this.gridOpacity, new Value(0)), [set(this.gridOpacity, new Value(1))]),

              call([], () => {
                console.info('gaile');
              }),
            ]),

            cond(
              and(
                eq(state, State.END),
                greaterOrEq(scaledWidth, viewerAreaWidth),
                greaterOrEq(this.scale, new Value(minScale)),
              ),
              cond(
                and(lessThan(this.translateX, negMaxX), greaterOrEq(this.scale, new Value(minScale))),
                [
                  set(
                    this.translateX,
                    timing({
                      from: this.translateX,
                      to: negMaxX,
                      ...timingDefaultParams,
                    }),
                  ),
                  call([], () => {
                    console.info('gaile111');
                  }),
                ],
                cond(and(greaterThan(this.translateX, maxX), greaterOrEq(this.scale, new Value(minScale))), [
                  set(
                    this.translateX,
                    timing({
                      from: this.translateX,
                      to: maxX,
                      ...timingDefaultParams,
                    }),
                  ),
                  call([], () => {
                    console.info('gail222e');
                  }),
                ]),
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
            cond(and(eq(state, State.ACTIVE), greaterThan(scale, new Value(0.6)), lessOrEq(scale, new Value(2))), [
              set(this.scale, multiply(offsetZ, scale)),
              call([], () => {
                console.info(111);
              }),
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
          ]),
      },
    ]);
  }

  handleMove = (args: readonly number[]): void => {
    const { onMove } = this.props;

    const positionX = args[0];
    const positionY = args[1];
    const scale = args[2];
    console.info(positionX, 'haha', args[3], args[4]);
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
                  onGestureEvent={this.onPinchGestureEvent}
                  onHandlerStateChange={this.onPinchGestureEvent}
                >
                  <Animated.View style={imageWrapperStyles} collapsable={false}>
                    {videoFile ? (
                      <Animated.View style={imageStyles}>
                        <Video
                          repeat={true}
                          source={{ uri: videoFile }}
                          style={{
                            width: imageWidth,
                            height: imageHeight,
                          }}
                        />
                      </Animated.View>
                    ) : (
                      <Animated.Image style={imageStyles} source={imageSrc} />
                    )}
                    {showCover ? (
                      <Animated.View style={overlayContainerStyle}>
                        <Animated.View style={{ height: '100%', width: '100%' }}>
                          <Animated.View
                            style={[
                              {
                                left: areaWidth / 3,
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
                                left: (areaWidth / 3) * 2,
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
