import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Image,
  Dimensions,
  Platform,
  SafeAreaView,
  VirtualizedList,
} from 'react-native';
import _ from 'lodash';
import Camera from './Camera';
import EventBus from './EventBus';
const FLASH_MODE_AUTO = 'auto';
const FLASH_MODE_ON = 'on';
const FLASH_MODE_OFF = 'off';

const { width, height } = Dimensions.get('window');

export enum CameraType {
  Front = 'front',
  Back = 'back',
}

export type Props = {
  ratioOverlay?: string;
  ratioOverlayColor?: string;
  allowCaptureRetake: boolean;
  cameraRatioOverlay: any;
  showCapturedImageCount?: boolean;
  captureButtonImage: any;
  cameraFlipImage: any;
  hideControls: any;
  showFrame: any;
  frameColor: any;
  torchOnImage: any;
  torchOffImage: any;
  onBottomButtonPressed: (any) => void;
};

type State = {
  captureImages: any[];
  flashData: any;
  torchMode: boolean;
  focusMode: boolean;
  ratios: any[];
  ratioArrayPosition: number;
  imageCaptured: any;
  captured: boolean;
  cameraType: CameraType;
  videoRecording: boolean;
  pasterList: any[];
  facePasterInfo: any;
};

type PasterItemProps = {
  item: any;
  index: number;
  applyPaster(item: any): void;
};

class PasterItem extends React.Component<PasterItemProps> {
  static propTypes = {
    item: PropTypes.object.isRequired,
    applyPaster: PropTypes.func.isRequired,
  };

  applyPaster = () => {
    this.props.applyPaster(this.props.item);
  };

  render() {
    const { item, index } = this.props;
    return (
      <View style={styles.item}>
        <TouchableOpacity onPress={this.applyPaster}>
          <Image style={{ width: 80, height: 80 }} source={{ uri: item.icon }} />
        </TouchableOpacity>
      </View>
    );
  }
}

const CameraScreeCount = () => {
  const [duration, setDuration] = React.useState(0.00);

  React.useEffect(() => {
    const callback = (e) => {
      setDuration(e);
    };
    EventBus.addListener('record_duration', callback);
    return () => {
      EventBus.remove('record_duration', callback);
    };
  }, []);

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Text style={{ color: 'red', fontSize: 30 }}>{duration}</Text>
    </View>
  );
};

export default class CameraScreen extends Component<Props, State> {
  static propTypes = {
    allowCaptureRetake: PropTypes.bool,
  };

  static defaultProps = {
    allowCaptureRetake: false,
  };

  currentFlashArrayPosition: number;
  flashArray: any[];
  camera: any;

  constructor(props) {
    super(props);
    this.currentFlashArrayPosition = 0;
    this.flashArray = [
      {
        mode: FLASH_MODE_AUTO,
        image: _.get(this.props, 'flashImages.auto'),
      },
      {
        mode: FLASH_MODE_ON,
        image: _.get(this.props, 'flashImages.on'),
      },
      {
        mode: FLASH_MODE_OFF,
        image: _.get(this.props, 'flashImages.off'),
      },
    ];

    this.state = {
      captureImages: [],
      flashData: this.flashArray[this.currentFlashArrayPosition],
      torchMode: false,
      focusMode: true,
      ratios: [],
      ratioArrayPosition: -1,
      imageCaptured: false,
      captured: false,
      cameraType: CameraType.Front,
      pasterList: [],
      facePasterInfo: {},
      videoRecording: false,
    };
  }
  componentDidMount() {
    let ratios = [];
    if (this.props.cameraRatioOverlay) {
      ratios = this.props.cameraRatioOverlay.ratios || [];
    }

    this.setState({
      ratios: ratios || [],
      ratioArrayPosition: ratios.length > 0 ? 0 : -1,
    });
    this.getPasterData();
  }

  async getPasterData() {
    const pasters = await this.camera.getPasterInfos();
    // console.log('------ :', pasters[0]);
    this.setState({
      pasterList: pasters,
    });
  }

  isCaptureRetakeMode() {
    return !!(this.props.allowCaptureRetake && !_.isUndefined(this.state.imageCaptured));
  }

  renderFlashButton() {
    return (
      !this.isCaptureRetakeMode() && (
        <TouchableOpacity style={{ paddingHorizontal: 15 }} onPress={() => this.onSetFlash()}>
          <Image
            style={{ flex: 1, justifyContent: 'center' }}
            source={this.state.flashData.image}
            resizeMode='contain'
          />
        </TouchableOpacity>
      )
    );
  }

  renderTorchButton() {
    return (
      !this.isCaptureRetakeMode() && (
        <TouchableOpacity style={{ paddingHorizontal: 15 }} onPress={() => this.onSetTorch()}>
          <Image
            style={{ flex: 1, justifyContent: 'center' }}
            source={this.state.torchMode ? this.props.torchOnImage : this.props.torchOffImage}
            resizeMode='contain'
          />
        </TouchableOpacity>
      )
    );
  }

  renderSwitchCameraButton() {
    return (
      this.props.cameraFlipImage &&
      !this.isCaptureRetakeMode() && (
        <TouchableOpacity style={{ paddingHorizontal: 15 }} onPress={() => this.onSwitchCameraPressed()}>
          <Image
            style={{ flex: 1, justifyContent: 'center' }}
            source={this.props.cameraFlipImage}
            resizeMode='contain'
          />
        </TouchableOpacity>
      )
    );
  }

  renderTopButtons() {
    return (
      !this.props.hideControls && (
        <SafeAreaView style={styles.topButtons}>
          {this.renderFlashButton()}
          {this.renderSwitchCameraButton()}
          {this.renderTorchButton()}
        </SafeAreaView>
      )
    );
  }

  _onRecordingDuration = (event) => {
    EventBus.emit('record_duration', parseFloat(event.duration).toFixed(2));
  };

  renderCamera() {
    return (
      <View style={styles.cameraContainer}>
        {this.isCaptureRetakeMode() ? (
          <Image style={{ flex: 1, justifyContent: 'flex-end' }} source={{ uri: this.state.imageCaptured.uri }} />
        ) : (
          <Camera
            ref={(cam) => (this.camera = cam)}
            style={{ flex: 1, justifyContent: 'flex-end' }}
            cameraType={this.state.cameraType}
            flashMode={this.state.flashData.mode}
            torchMode={this.state.torchMode ? 'on' : 'off'}
            focusMode={'on'}
            zoomMode={'on'}
            ratioOverlay={this.state.ratios[this.state.ratioArrayPosition]}
            saveToCameraRoll={!this.props.allowCaptureRetake}
            showFrame={this.props.showFrame}
            frameColor={this.props.frameColor}
            facePasterInfo={this.state.facePasterInfo}
            onRecordingProgress={this._onRecordingDuration}
          />
        )}
      </View>
    );
  }

  numberOfImagesTaken() {
    const numberTook = this.state.captureImages.length;
    if (numberTook >= 2) {
      return numberTook;
    } else if (this.state.captured) {
      return '1';
    } else {
      return '';
    }
  }

  renderCaptureButton() {
    return (
      this.props.captureButtonImage &&
      !this.isCaptureRetakeMode() && (
        <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.onCaptureImagePressed()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
            {this.props.showCapturedImageCount && (
              <View style={styles.textNumberContainer}>
                <Text>
                  拍照
                  <Text>{this.numberOfImagesTaken()}</Text>
                </Text>
              </View>
            )}
          </TouchableOpacity>
        </View>
      )
    );
  }

  renderRecordButton() {
    return (
      this.props.captureButtonImage &&
      !this.isCaptureRetakeMode() && (
        <View style={styles.captureButtonContainer}>
          <TouchableOpacity onPress={() => this.onRecordVideoPressed()}>
            <Image source={this.props.captureButtonImage} resizeMode='contain' />
            {this.props.showCapturedImageCount && (
              <View style={styles.textNumberContainer}>
                <Text>录制</Text>
              </View>
            )}
          </TouchableOpacity>
        </View>
      )
    );
  }

  renderRatioStrip() {
    if (this.state.ratios.length === 0 || this.props.hideControls) {
      return null;
    }
    return (
      <View style={{ flex: 1, flexDirection: 'column', justifyContent: 'flex-end' }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', paddingRight: 10, paddingLeft: 20 }}>
          <Text style={styles.ratioBestText}>Your images look best at a {this.state.ratios[0] || ''} ratio</Text>
          <TouchableOpacity
            style={{ flex: 1, flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', padding: 8 }}
            onPress={() => this.onRatioButtonPressed()}
          >
            <Text style={styles.ratioText}>{this.state.ratioOverlay}</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  sendBottomButtonPressedAction(type, captureRetakeMode, image) {
    if (this.props.onBottomButtonPressed) {
      this.props.onBottomButtonPressed({ type, captureImages: this.state.captureImages, captureRetakeMode, image });
    }
  }

  onButtonPressed(type) {
    const captureRetakeMode = this.isCaptureRetakeMode();
    if (captureRetakeMode) {
      if (type === 'left') {
        this.setState({ imageCaptured: undefined });
      }
    } else {
      this.sendBottomButtonPressedAction(type, captureRetakeMode, null);
    }
  }

  renderBottomButtons() {
    return (
      !this.props.hideControls && (
        <SafeAreaView style={[styles.bottomButtons, { backgroundColor: '#ffffff00' }]}>
          {this.renderCaptureButton()}
          {this.renderRecordButton()}
        </SafeAreaView>
      )
    );
  }

  getItemCount = () => {
    const count = this.state.pasterList.length;
    // console.log('getItemCount: ', count);
    return count;
  };

  cameraApplyPaster = (paster) => {
    // console.log('cameraApplyPaster: ', paster.url);
    this.setState({ facePasterInfo: paster });
  };
  renderItem = (item, index) => {
    return (
      <PasterItem
        item={item}
        index={index}
        applyPaster={(paster) => {
          this.cameraApplyPaster(paster);
        }}
      />
    );
  };

  renderPasterButtons() {
    return (
      <View style={{ backgroundColor: '#ffffff00', flex: 2 }}>
        <VirtualizedList
          data={this.state.pasterList}
          initialNumToRender={4}
          renderItem={({ item, index }) => this.renderItem(item, index)}
          keyExtractor={(item) => `${item.id}`}
          getItemCount={this.getItemCount}
          getItem={(data, index) => {
            return this.state.pasterList[index];
          }}
          horizontal={true}
        />
      </View>
    );
  }

  onSwitchCameraPressed() {
    const direction = this.state.cameraType === CameraType.Back ? CameraType.Front : CameraType.Back;
    this.setState({ cameraType: direction });
  }

  onSetFlash() {
    this.currentFlashArrayPosition = (this.currentFlashArrayPosition + 1) % 3;
    const newFlashData = this.flashArray[this.currentFlashArrayPosition];
    this.setState({ flashData: newFlashData });
  }

  onSetTorch() {
    this.setState({ torchMode: !this.state.torchMode });
  }

  async onCaptureImagePressed() {
    const image = await this.camera.capture();

    if (this.props.allowCaptureRetake) {
      this.setState({ imageCaptured: image });
    } else {
      if (image) {
        this.setState({
          captured: true,
          imageCaptured: image,
          captureImages: _.concat(this.state.captureImages, image),
        });
      }
      this.sendBottomButtonPressedAction('capture', false, image);
    }
  }

  async onRecordVideoPressed() {
    console.log('---- onRecordVideoPressed: ', this.state.videoRecording);
    if (this.state.videoRecording) {
      const path = await this.camera.stopRecording();
      console.log('---- stopRecording video saved to ', path);
      this.setState({ videoRecording: false });
    } else {
      const success = await this.camera.startRecording();
      console.log('---- startRecording success: ', success);
      this.setState({ videoRecording: success });
    }
  }

  onRatioButtonPressed() {
    const newRatiosArrayPosition = (this.state.ratioArrayPosition + 1) % this.state.ratios.length;
    this.setState({ ratioArrayPosition: newRatiosArrayPosition });
  }

  render() {
    return (
      <View style={{ flex: 1, backgroundColor: 'black' }} {...this.props}>
        {Platform.OS === 'android' && this.renderCamera()}
        {this.renderTopButtons()}
        {Platform.OS !== 'android' && this.renderCamera()}
        {this.renderRatioStrip()}
        {Platform.OS === 'android' && <View style={styles.gap} />}
        <CameraScreeCount />
        {this.renderPasterButtons()}
        {this.renderBottomButtons()}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  bottomButtons: {
    flex: 2,
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 14,
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
  topButtons: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingTop: 8,
    paddingBottom: 0,
  },
  cameraContainer: {
    ...Platform.select({
      android: {
        position: 'absolute',
        top: 0,
        left: 0,
        width,
        height,
      },
      default: {
        flex: 10,
        flexDirection: 'column',
      },
    }),
  },
  captureButtonContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  textNumberContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
    justifyContent: 'center',
    alignItems: 'center',
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
  item: {
    backgroundColor: '#334',
    opacity: 0.8,
    height: 80,
    width: 80,
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 0,
    marginHorizontal: 5,
    padding: 10,
  },
  title: {
    fontSize: 20,
  },
});
