import React from 'react';

import {
    Platform,
    View,
    Image,
} from 'react-native';

const isAndroid = Platform.OS === 'android';

import { requireNativeComponent, NativeModules } from 'react-native';


const NativeCropImageView = isAndroid ? requireNativeComponent<NativeProps>('RNKitCropImageViewManager') : requireNativeComponent<NativeProps>('RNKitCropImageView');

// const CropImageViewModule = isAndroid ? NativeModules.RNKitCropImageViewModule : NativeModules.RNKitCropImageViewManager;


export type ImageAngle = 0 | 90 | 180 | 270;

export interface BaseProps {
    imageUri: string;
    angle?: ImageAngle;
    startCrop: boolean;
    reset: boolean;
}
interface NativeProps extends BaseProps {
    onCropped?(data: any): void;
    onCropError?(data: any): void;
}

export interface onCroppedData {
    path: string;
    uri: string;
    imageWidth: number;
    imageHeight: number;
    offsetX: number;
    offsetY: number;
}
export interface Props extends BaseProps {
    onCropped?(data: onCroppedData): void;
    onCropError?(errorMsg: string): void;
}

type State = {
};

export default class CropImageView extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props)
    };

    componentDidMount() {

    }

    componentWillUnmount() {

    }

    shouldComponentUpdate(nextProps: Props, nextState: State) {
        if (nextProps.angle !== this.props.angle) {
            return true;
        }
        if (nextProps.imageUri !== this.props.imageUri) {
            return true;
        }
        if (nextProps.startCrop !== this.props.startCrop) {
            return true;
        }
        if (nextProps.reset !== this.props.reset) {
            return true;
        }
        return false;
    }


    onCropError = ({ nativeEvent }) => {
        // console.info("onCropError", nativeEvent);
        if (this.props.onCropError) {
            this.props.onCropError(nativeEvent);
        }
    }
    onCropped = ({ nativeEvent }) => {
        // console.info("onCropped", nativeEvent);
        if (this.props.onCropped) {
            this.props.onCropped(nativeEvent);
        }
    }

    render(): React.ReactNode {
        return (
            <NativeCropImageView
                {...this.props}
                onCropped={this.onCropped}
                onCropError={this.onCropError}
            />
        );
    }


}
