import React, { Component } from 'react';
import {
    StyleSheet,
    View,
    TouchableOpacity,
    Dimensions,
} from 'react-native';

import ImageCropper from '../react-native-simple-image-cropper/src';

import FastImage from '@rocket.chat/react-native-fast-image';

const { width, height } = Dimensions.get('window');

export default class PostContent extends Component {
    constructor(props) {
        super(props);

        this.cropParams = {};
        this.state = {
            imageItem: "",
            cropScale: 0,
            videoPaused: true,
            videoMuted: true,
            isChangeScale: false,
            minScale: 0,
            positionX: 0,
            positionY: 0,
            selectMultiple: false,//多选单选
        };
        // 展示中的图片缩放值
        this.moveScale = 0;
    }

    componentDidMount() {
        setTimeout(() => {
            this.setState({
                videoMuted: false,
                videoPaused: false,
            });
        }, 500);
    }


    shouldComponentUpdate(nextProps, nextState) {

        if (nextProps.selectMultiple !== this.props.selectMultiple) {
            this.cropParams = {};
            this.props.onCropParams(this.cropParams);
            return true;
        }

        if (nextProps.isVidoePlayer !== this.props.isVidoePlayer) {
            this.setState({
                videoPaused: !nextProps.isVidoePlayer,
            });
        }

        if (nextProps.type !== this.props.type) {
            if (nextProps.type === 'post') {
                setTimeout(() => {
                    this.setState({
                        videoMuted: false,
                        videoPaused: false,
                    });
                }, 500);
            } else {
                this.setState({
                    videoMuted: true,
                    videoPaused: true,
                });
            }
        }
        if (nextState.videoMuted !== this.state.videoMuted) {
            return true;
        }
        if (nextState.videoPaused !== this.state.videoPaused) {
            return true;
        }

        if (!!nextProps.multipleData && !!nextProps.multipleData?.data && nextProps.multipleData !== this.props.multipleData) {
            const imageItem = nextProps.multipleData?.data[nextProps.multipleData?.selectedIndex];
            if (!!imageItem) {
                this.setState({
                    imageItem: imageItem,
                });
            }
        }

        if (nextState.imageItem !== this.state.imageItem) {
            const imageItem = nextState.imageItem;
            if (!!imageItem) {
                const itemCropData = this.cropParams[imageItem.url];

                const isVideo = imageItem?.type?.includes('video');
                // const videoPaused = !isVideo;

                let minScale = 1;
                if (imageItem.width > imageItem.height) {
                    minScale = imageItem.height / imageItem.width
                } else {
                    minScale = imageItem.width / imageItem.height
                }
                if (!!itemCropData) {
                    const positionX = itemCropData?.positionX;
                    const positionY = itemCropData?.positionY;
                    const positionScale = itemCropData?.scale;
                    this.setState({
                        // videoPaused: videoPaused,
                        cropScale: positionScale,
                        minScale: minScale,
                        positionX: positionX,
                        positionY: positionY,
                    });
                } else {
                    this.setState({
                        // videoPaused: videoPaused,
                        cropScale: minScale,
                        minScale: minScale,
                        positionX: 0,
                        positionY: 0,
                    });
                }

            }
        }

        if (nextState.isChangeScale !== this.state.isChangeScale) {
            return true;
        }
        if (nextState.imageItem !== this.state.imageItem) {
            return true;
        }
        if (nextState.cropScale !== this.state.cropScale) {
            return true;
        }
        if (nextState.minScale !== this.state.minScale) {
            return true;
        }
        if (nextState.positionX !== this.state.positionX) {
            return true;
        }
        if (nextState.positionY !== this.state.positionY) {
            return true;
        }
        return false;
    }

    toggleCropWidth = (imageItem) => {
        if (!!imageItem && !!this.moveScale) {
            let minScale = 1;
            if (imageItem.width > imageItem.height) {
                minScale = imageItem.height / imageItem.width
            } else {
                minScale = imageItem.width / imageItem.height
            }
            // isChangeScale = true 表示本次刷新强行更新 scale
            // 更新完 scale，回调 setChangeScale 重新设置为false,表示仅本次点击更新有效
            if (this.moveScale >= 1) {
                this.setState({
                    cropScale: minScale,
                    minScale: minScale,
                    positionX: 0,
                    positionY: 0,
                    isChangeScale: true,
                });
            } else {
                this.setState({
                    cropScale: 1,
                    minScale: minScale,
                    isChangeScale: true,
                });
            }
        }
    };

    setChangeScale = () => {
        this.setState({
            isChangeScale: false,
        });
    };

    render() {

        const imageItem = this.state.imageItem;

        if (!imageItem) {
            return (<View style={styles.continueView} />)
        }

        const fileSize = { width: imageItem.width, height: imageItem.height }
        const fileUri = imageItem.url
        const isVideo = imageItem.type.includes('video')

        return (
            <View style={styles.continueView} >

                <ImageCropper
                    imageUri={isVideo ? "" : fileUri}
                    videoFile={isVideo ? fileUri : ""}

                    isChangeScale={this.state.isChangeScale}
                    setChangeScale={this.setChangeScale}
                    minScale={this.state.minScale}
                    positionX={this.state.positionX}
                    positionY={this.state.positionY}
                    scale={this.state.cropScale}

                    videoPaused={this.state.videoPaused}
                    videoMuted={this.state.videoMuted}

                    disablePin={isVideo}
                    srcSize={fileSize}
                    cropAreaWidth={width}
                    cropAreaHeight={width}
                    containerColor='black'
                    areaColor='black'
                    areaOverlay={<View></View>}
                    setCropperParams={(cropperParams) => {
                        let newKey = imageItem.url;
                        this.cropParams[newKey] = cropperParams;
                        this.moveScale = cropperParams.scale;

                        this.props.onCropParams(this.cropParams);

                    }}
                />

                {!isVideo && <TouchableOpacity
                    style={styles.scaleImageView}
                    onPress={() => {
                        this.toggleCropWidth(imageItem)
                    }}>
                    <FastImage
                        testID='change-image-size-button'
                        style={{ width: 31, height: 31, }}
                        source={this.props.changeSizeImage}
                    />
                </TouchableOpacity>}

            </View >
        )
    }
}


const styles = StyleSheet.create({
    continueView: {
        padding: 0,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#000',
        position: 'relative',
        height: width,
        width: width,
    },

    scaleImageView: {
        width: 31,
        height: 31,
        marginRight: 10,
        position: 'absolute',
        left: 15,
        bottom: 20,
        zIndex: 99,
    }

})