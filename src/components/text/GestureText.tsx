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
    Pressable,
    Alert
} from 'react-native';

import AVService from '../../AVService.ios';

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
    TapGestureHandler,
} from 'react-native-gesture-handler';
import { rgba } from 'react-native-image-filter-kit';

type CaptionInfo = {
    text: string;
    fontName: string;
    // fontStyle: string; // normal | italic | bold
    color: string;      //文字颜色
    textAlignment: string; //left, center, right
    backgroundColor: string; //背景颜色
    // startTime: number;
    // duration: number;
    x: number;
    y: number;
    rotate: number;
    scale: number;
};



type GestureTextProps = {
    with: number;
    height: number;
    textAlign?: any;
    textColor?: any;
    textBackgroundColor?: any;
    textFontName?: any;
    editable?: any;
    isTextEdit?: any;
    lastTextEdit?: any;
    minDist?: number;
    boxStyle?: StyleProp<ViewStyle>;
    onTextMove: (info: CaptionInfo) => void;
    onEditEnable: (isEnable: any) => void;
};

type GestureTextState = {
    text: string;
};

export default class GestureText extends Component<GestureTextProps, GestureTextState> {
    private panRef = React.createRef<PanGestureHandler>();
    private rotationRef = React.createRef<RotationGestureHandler>();
    private pinchRef = React.createRef<PinchGestureHandler>();
    private doubleTapRef = React.createRef<TapGestureHandler>();
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
    private inputRef: any;

    private lastEditable: any;
    private onPanGestureEvent: (event: PanGestureHandlerGestureEvent) => void;

    constructor(props: GestureTextProps) {
        super(props);
        this.translateX = new Animated.Value(0);
        this.translateY = new Animated.Value(0);
        this.lastOffset = { x: 0, y: 0 };
        this.inputRef = null

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
            text: 'test caption test caption test caption test caption',
        };

        this.lastEditable = true;

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
            // console.log('-----: lastOffset.x:', this.lastOffset.x, 'lastOffset.y :', this.lastOffset.y);
            this.onSetTextInfo();
        }
    };

    private onRotateHandlerStateChange = (event: RotationGestureHandlerStateChangeEvent) => {
        if (event.nativeEvent.oldState === State.ACTIVE) {
            Keyboard.dismiss();
            this.lastRotate += event.nativeEvent.rotation;
            this.rotate.setOffset(this.lastRotate);
            this.rotate.setValue(0);
            // console.log('-----: rotate', this.lastRotate);
            this.onSetTextInfo();
        }
    };
    private onPinchHandlerStateChange = (event: PinchGestureHandlerStateChangeEvent) => {
        if (event.nativeEvent.oldState === State.ACTIVE) {
            Keyboard.dismiss();
            this.lastScale *= event.nativeEvent.scale;
            this.baseScale.setValue(this.lastScale);
            this.pinchScale.setValue(1);
            this.onSetTextInfo();
        }
    };

    onSetTextInfo = () => {
        this.props.onTextMove({
            text: this.state.text,
            color: this.props.textColor || 'white',
            backgroundColor: this.props.textBackgroundColor || 'transparent',
            fontName: this.props.textFontName || '',
            textAlignment: this.props.textAlign || 'center',
            x: this.lastOffset.x,
            y: this.lastOffset.y,
            rotate: this.lastRotate,
            scale: this.lastScale,
        });
    }

    //单击
    onTextSingleTap = () => {
        // console.log("onTextSingleTap")
    }

    //双击
    onTextDoubleTap = () => {
        // console.log("onTextDoubleTap")
        this.props.onEditEnable(true);
        setTimeout(() => {
            this.inputRef.focus()
            this.translateX.setOffset(0);
            this.translateY.setOffset(0);
            this.rotate.setOffset(0);
            this.baseScale.setValue(1);
        }, 100);
    }

    onGetFontList = async () => {
        // console.log("onTextSingleTap")
        const fontList = await AVService.fetchFontList();
        console.log("fontList", fontList);

        const fontInfo = await AVService.downloadFont(fontList[3].id);

        console.log("fontInfo", fontInfo);
    }

    /**
   * 在第一次绘制 render() 之后，这里去做网络渲染等等工作
   */
    componentDidMount() {
        console.log("第一次render加载完成")
        this.onGetFontList();
    }



    /**
     * 更新组件，调用render。在 shouldComponentUpdate 返回 ture 才会执行
     * 首次加载不会调用
     */
    componentDidUpdate(nextProps, nextState) {
        if (this.props.isTextEdit && (!!this.lastEditable && !this.props.editable)) {
            //退出编辑状态，添加字幕变成可拖动
            console.log("点击了完成");
            this.translateX.setOffset(this.lastOffset.x);
            this.translateY.setOffset(this.lastOffset.y);
            this.rotate.setOffset(this.lastRotate);
            this.baseScale.setValue(this.lastScale);
            this.onSetTextInfo();
        }

        if (!this.props.lastTextEdit && !!this.props.isTextEdit) {
            console.log("进入文字编辑");
            //将文字框移动到初始位置，并且展开软键盘
            this.onTextDoubleTap();
        }
        this.lastEditable = this.props.editable
    }

    renderText() {
        return (
            <View style={[styles.textInputBox, (!this.props.isTextEdit || !this.props.editable) && { backgroundColor: 'transparent' }]} >
                <TextInput
                    // key={this.props.textAlign + this.props.textColor}
                    ref={(ref) => {
                        this.inputRef = ref
                    }}
                    style={[styles.textInput, {
                        fontSize: 25.0,
                        color: this.props.textColor || 'white',
                        backgroundColor: this.props.textBackgroundColor || 'transparent'
                    }]}
                    placeholderTextColor={this.props.textColor || 'white'}
                    selectionColor={this.props.textColor || '836BFF'}
                    value={this.state.text}
                    textAlign={this.props.textAlign}
                    editable={this.props.editable}
                    autoFocus={this.props.isTextEdit && this.props.editable}
                    multiline={true}

                    onChangeText={(text) => { this.setState({ text }) }}
                />
                {!this.props.editable && <Text style={styles.text} />}
            </View >
        );
    }


    render() {
        return (
            <View style={styles.container}>
                <PanGestureHandler
                    {...this.props}
                    ref={this.panRef}
                    enabled={this.props.isTextEdit && !this.props.editable}
                    onGestureEvent={this.onPanGestureEvent}
                    onHandlerStateChange={this.onPanHandlerStateChange}
                    minDist={this.props.minDist}>
                    <Animated.View style={styles.wrapperRotation}>
                        <RotationGestureHandler
                            ref={this.rotationRef}
                            enabled={this.props.isTextEdit && !this.props.editable}
                            simultaneousHandlers={this.pinchRef}
                            onGestureEvent={this.onRotateGestureEvent}
                            onHandlerStateChange={this.onRotateHandlerStateChange}>
                            <Animated.View style={styles.wrapperPinch}>
                                <TapGestureHandler
                                    onHandlerStateChange={({ nativeEvent }) => {
                                        if (nativeEvent.state === State.ACTIVE) {
                                            this.onTextSingleTap()
                                        }
                                    }}
                                    enabled={this.props.isTextEdit && !this.props.editable}
                                    waitFor={this.doubleTapRef}>
                                    <TapGestureHandler
                                        ref={this.doubleTapRef}
                                        onHandlerStateChange={({ nativeEvent }) => {
                                            if (nativeEvent.state === State.ACTIVE) {
                                                this.onTextDoubleTap()
                                            }
                                        }}
                                        enabled={this.props.isTextEdit && !this.props.editable}
                                        numberOfTaps={2}>
                                        <Animated.View style={styles.wrapperPinch}>
                                            <PinchGestureHandler
                                                ref={this.pinchRef}
                                                enabled={this.props.isTextEdit && !this.props.editable}
                                                simultaneousHandlers={this.rotationRef}
                                                onGestureEvent={this.onPinchGestureEvent}
                                                onHandlerStateChange={this.onPinchHandlerStateChange} >
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
                                                    ]}>

                                                    {this.renderText()}

                                                </Animated.View>
                                            </PinchGestureHandler>
                                        </Animated.View>
                                    </TapGestureHandler>
                                </TapGestureHandler>
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
        // flex: 1,
        width: "100%",
        height: "100%",
        justifyContent: 'center',
        // backgroundColor: 'plum',
    },
    //缩放布局
    box: {
        paddingLeft: 30,
        paddingRight: 30,
        paddingTop: 10,
        paddingBottom: 10,
        alignSelf: 'center',
        // backgroundColor: 'plum',
        // zIndex: 200,
    },
    textInputBox: {
        backgroundColor: 'rgba(51, 51, 51, 0.4)',
        borderRadius: 3,
        position: 'relative',
        overflow: 'hidden'
    },
    textInput: {
        fontSize: 18,
        color: '#fff',
        paddingStart: 8,
        paddingEnd: 10,
        paddingBottom: 3,
    },
    text: {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
    },
    wrapperRotation: {
        alignSelf: 'center',
    },
    wrapperPinch: {
        alignSelf: 'center',
        // justifyContent: 'center',
    },
});
