import React, { PureComponent, ReactNode } from 'react';
import { Image, Dimensions } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import ImageEditor from '@react-native-community/image-editor';
import ImageViewer from './ImageViewer';
import { getPercentFromNumber, getPercentDiffNumberFromNumber } from './helpers/percentCalculator';
import { ICropperParams, ICropParams, IImageViewerData, ISizeData } from './types';

interface IProps {
  imageUri: string;
  cropAreaWidth?: number;
  cropAreaHeight?: number;
  containerColor?: string;
  areaColor?: string;
  videoFile?: string;

  areaOverlay?: ReactNode;
  scale?: number;
  srcSize: ISizeData;
  disablePin?: boolean,

  setCropperParams: (params: ICropperParams) => void;
}

export interface IState {
  positionX: number;
  positionY: number;
  scale: number;
  minScale: number;
  srcSize: ISizeData;
  fittedSize: ISizeData;
  width: number;
  height: number;
  loading: boolean;
  prevImageUri: string;

}

const window = Dimensions.get('window');
const w = window.width;

const defaultProps = {
  cropAreaWidth: w,
  cropAreaHeight: w,
  containerColor: 'black',
  areaColor: 'black',
};

class ImageCropper extends PureComponent<IProps, IState> {
  static crop = (params: ICropParams): any => {
    const { positionX, positionY, scale, srcSize, fittedSize, cropSize, cropAreaSize, imageUri } = params;

    const offset = {
      x: 0,
      y: 0,
    };

    const cropAreaW = cropAreaSize ? cropAreaSize.width : w;
    const cropAreaH = cropAreaSize ? cropAreaSize.height : w;

    const wScale = cropAreaW / scale;
    const hScale = cropAreaH / scale;

    const percentCropperAreaW = getPercentDiffNumberFromNumber(wScale, fittedSize.width);
    const percentRestW = 100 - percentCropperAreaW;
    const hiddenAreaW = getPercentFromNumber(percentRestW, fittedSize.width);

    const percentCropperAreaH = getPercentDiffNumberFromNumber(hScale, fittedSize.height);
    const percentRestH = 100 - percentCropperAreaH;
    const hiddenAreaH = getPercentFromNumber(percentRestH, fittedSize.height);

    const x = hiddenAreaW / 2 - positionX;
    const y = hiddenAreaH / 2 - positionY;

    offset.x = x <= 0 ? 0 : x;
    offset.y = y <= 0 ? 0 : y;

    const srcPercentCropperAreaW = getPercentDiffNumberFromNumber(offset.x, fittedSize.width);
    const srcPercentCropperAreaH = getPercentDiffNumberFromNumber(offset.y, fittedSize.height);

    const offsetW = getPercentFromNumber(srcPercentCropperAreaW, srcSize.width);
    const offsetH = getPercentFromNumber(srcPercentCropperAreaH, srcSize.height);

    const sizeW = getPercentFromNumber(percentCropperAreaW, srcSize.width);
    const sizeH = getPercentFromNumber(percentCropperAreaH, srcSize.height);

    offset.x = Math.floor(offsetW);
    offset.y = Math.floor(offsetH);

    const cropData = {
      offset,
      size: {
        width: Math.round(sizeW),
        height: Math.round(sizeH),
      },
      displaySize: {
        width: Math.round(cropSize.width),
        height: Math.round(cropSize.height),
      },
    };
    return cropData;
    // return new Promise((resolve, reject) => ImageEditor.cropImage(imageUri, cropData).then(resolve).catch(reject));
  };

  static defaultProps = defaultProps;

  static getDerivedStateFromProps(props: IProps, state: IState) {
    if (props.imageUri !== state.prevImageUri) {
      return {
        prevImageUri: props.imageUri,
        loading: true,
      };
    }

    return null;
  }

  state = {
    positionX: 0,
    positionY: 0,
    width: 0,
    height: 0,
    scale: 1,
    minScale: 1,
    loading: true,
    srcSize: {
      width: 0,
      height: 0,
    },
    fittedSize: {
      width: 0,
      height: 0,
    },
    prevImageUri: '',
  };

  componentDidMount() {
    this.init();
  }

  componentDidUpdate(prevProps: IProps) {
    const { imageUri, scale, videoFile } = this.props;
    if (imageUri && prevProps.imageUri !== imageUri) {
      this.init();
    }
    if (videoFile && prevProps.videoFile !== videoFile) {
      this.init();
    }
    if (scale && prevProps.scale !== scale) {
      this.init();
    }
  }

  init = () => {
    const { imageUri } = this.props;
    const callback = (width, height) => {
      const { setCropperParams, cropAreaWidth, cropAreaHeight, scale: scaleProps } = this.props;

      const areaWidth = cropAreaWidth!;
      const areaHeight = cropAreaHeight!;

      const srcSize = { width, height };
      const fittedSize = { width: 0, height: 0 };
      let scale = 1;
      let wInit = w * 1;
      if (width > height) {
        const ratio = wInit / height;
        fittedSize.width = width * ratio;
        fittedSize.height = wInit;
      } else if (width < height) {
        const ratio = wInit / width;
        fittedSize.width = wInit;
        fittedSize.height = height * ratio;
      } else if (width === height) {
        fittedSize.width = wInit;
        fittedSize.height = wInit;
      }

      if (areaWidth < areaHeight || areaWidth === areaHeight) {
        if (width < height) {
          if (fittedSize.height < areaHeight) {
            scale = Math.ceil((areaHeight / fittedSize.height) * 10) / 10;
          } else {
            // 视觉窗口宽除以 图片宽
            scale = Math.ceil((wInit / fittedSize.width) * 10) / 10;
          }
        } else {
          scale = Math.ceil((areaHeight / fittedSize.height) * 10) / 10;
        }
      }

      scale = scale < scaleProps ? scaleProps : scale;
      this.setState(
        (prevState) => ({
          ...prevState,
          srcSize,
          fittedSize,
          minScale: scale,
          loading: false,
        }),
        () => {
          const { positionX, positionY } = this.state;

          setCropperParams({
            positionX,
            positionY,
            scale,
            srcSize,
            fittedSize,
          });
        },
      );
    };
    if (!this.props.srcSize.height) {
      Image.getSize(imageUri, callback, () => { });
    } else {
      callback(this.props.srcSize.width, this.props.srcSize.height);
    }
  };

  handleMove = ({ positionX, positionY, scale }: IImageViewerData) => {
    const { setCropperParams } = this.props;

    this.setState(
      (prevState) => ({
        ...prevState,
        positionX,
        positionY,
        scale,
      }),
      () => {
        const { srcSize, fittedSize } = this.state;
        setCropperParams({
          positionX,
          positionY,
          scale,
          srcSize,
          fittedSize,
        });
      },
    );
  };

  render() {
    const { loading, fittedSize, minScale } = this.state;
    const {
      imageUri,
      cropAreaWidth,
      cropAreaHeight,
      containerColor,
      areaColor,
      areaOverlay,
      videoFile,
      scale,
      disablePin,
      videoPaused,
      srcSize,
      positionX,
      positionY,
    } = this.props;

    const areaWidth = cropAreaWidth!;
    const areaHeight = cropAreaHeight!;

    const imageWidth = fittedSize.width;
    const imageHeight = fittedSize.height;

    // //设置缩放最小比例
    // let initMinScale = scale
    // if (imageWidth > imageHeight) {
    //   initMinScale = imageHeight / imageWidth
    // } else {
    //   initMinScale = imageWidth / imageHeight
    // }

    return (
      <GestureHandlerRootView>
        {!loading ? (
          <ImageViewer
            isChangeScale={this.props.isChangeScale}
            setChangeScale={this.props.setChangeScale}
            propsX={!!positionX ? positionX : 0}
            propsY={!!positionY ? positionY : 0}
            propsScale={!!scale ? scale : 1}

            image={imageUri}
            areaWidth={areaWidth}
            areaHeight={areaHeight}
            imageWidth={imageWidth}
            videoFile={videoFile}
            imageHeight={imageHeight}
            minScale={this.props.minScale}
            srcSize={srcSize}
            onMove={this.handleMove}
            containerColor={containerColor}
            imageBackdropColor={areaColor}
            overlay={areaOverlay}
            disablePin={disablePin}
            videoPaused={videoPaused}
          />
        ) : null}
      </GestureHandlerRootView>
    );
  }
}

export default ImageCropper;
