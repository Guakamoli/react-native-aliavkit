import PropTypes from 'prop-types';
import React, { Component, useRef, useEffect, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  // Pressable,
  Image,
  Dimensions,
  Platform,
  Animated,
  FlatList,
  Easing,
  InteractionManager,
  Pressable,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import { connect, useSelector, useDispatch } from 'react-redux';

import _ from 'lodash';
import Camera from '../Camera';
import Carousel from './Carousel';
import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import StoryEditor from '../StoryEditor';
import StoryMusic from '../StoryMusic';
import AVService from '../AVService';
import { BoxBlur } from 'react-native-image-filter-kit';
import RenderbeautifyBox from './RenderbeautifyBox';
import RenderCamera from './Camera';
import { setCameraType, setShowBeautify, setFacePasterInfo } from '../actions/story';
const FLASH_MODE_AUTO = 'auto';
const FLASH_MODE_ON = 'on';
const FLASH_MODE_OFF = 'off';

const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const captureIcon2 = (width - 20) / 2;

const stateAttrsUpdate = [
  'pasterList',
  'facePasterInfo',
  'showBeautify',
  'normalBeautyLevel',
  'cameraType',
  'ShootSuccess',
  'startShoot',
  'flag',
  'showCamera',
  'relaloadFlag',
];

export enum CameraType {
  Front = 'front',
  Back = 'back',
}
export type Props = {
  ratioOverlay?: string;

  allowCaptureRetake: boolean;
  cameraRatioOverlay: any;
  showCapturedImageCount?: boolean;
  captureButtonImage: any;
  cameraFlipImage: any;

  showFrame: any;
  scanBarcode: any;
  laserColor: any;
  frameColor: any;

  closeImage: any;
  musicImage: any;
  beautifyImage: any;
  beautyAdjustImag: any;
  AaImage: any;
  filterImage: any;
  musicRevampImage: any;
  giveUpImage: any;
  noVolumeImage: any;
  tailorImage: any;
  volumeImage: any;
  onReadCode: (any) => void;

  getUploadFile: (any) => void;
  goback: any;
  cameraModule: boolean;

  musicDynamicGif: any;
  musicIconPng: any;
  // goPostUpload  !!!
};

type State = {
  // 照片数据
  captureImages: any[];
  flashData: any;
  torchMode: boolean;
  flag: any;
  ratios: any[];
  ratioArrayPosition: number;
  imageCaptured: any;
  captured: boolean;
  cameraType: CameraType;
  // ---
  videoRecording: boolean;

  currentIndex: number;
  showBeautify: boolean;
  normalBeautyLevel: number;

  progress: number;
  startShoot: boolean;
  ShootSuccess: boolean;

  fadeInOpacity: any;

  // 视频路径
  videoPath: any;

  pasterList: any;
  facePasterInfo: any;

  fileType: String;
  musicOpen: Boolean;
};

const TestComponent = () => {
  return (
    <>
      <Pressable
        style={{
          width: 80,
          height: 80,
          backgroundColor: '#3f0',
          justifyContent: 'center',
          alignItems: 'center',
          borderRadius: 40,
        }}
        onPress={async () => {
          const music = await AVService.downloadMusic('Berlin - Take My Breath Away.mp3');
          console.log('---- downloadMusic: ', music);
        }}
      >
        <Text style={{ fontSize: 25, color: 'white' }}>音乐</Text>
      </Pressable>
    </>
  );
};
const ProgressCircleWrapper = (props) => {
  const { flag, recordeSuccess, setFlag } = props;
  let [progress, setProgress] = useState(0);
  let [timer, setTimer] = useState(null);
  useInterval(() => {
    const newprogress = (progress += 1 / 140);
    if (newprogress >= 1) {
      setTimer(null);
      setFlag(null);
      recordeSuccess();
      setProgress(0);
    } else {
      setProgress(newprogress);
    }
  }, timer);
  useEffect(() => {
    if (flag) {
      setTimer(60);
    } else {
      setTimer(null);
    }
  }, [flag]);
  return (
    <Progress.Circle
      style={[styles.progress, { position: 'absolute' }]}
      progress={progress}
      indeterminate={false}
      size={122}
      color={'#EA3600'}
      borderWidth={0}
      thickness={6}
    />
  );
};
class RenderswitchModule extends React.PureComponent {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <View style={styles.BottomBox}>
        <Pressable
          onPress={() => {
            this.props.setCameraType();
            setTimeout(() => {
              try {
                this.props.camera.current?.setPasterInfo?.(this.props.facePasterInfo);
              } catch (e) {
                console.info('eeee', e);
              }
            }, 100);

            AVService.enableHapticIfExist();

            this.props.haptics?.impactAsync(this.props.haptics.ImpactFeedbackStyle.Medium);
          }}
        >
          <View
            style={{
              height: 28 + 30,
              width: 31 + 15 * 2,
              justifyContent: 'center',
              alignItems: 'center',
            }}
          >
            <Image style={{ width: 31, height: 28 }} source={this.props.cameraFlipImage} resizeMode='contain' />
          </View>
        </Pressable>
      </View>
    );
  }
}
const RDSMMapStateToProps = (state) => ({
  facePasterInfo: state.shootStory.facePasterInfo,
});
const RDSMMapDispatchToProps = (dispatch) => ({
  setCameraType: (data) => dispatch(setCameraType(data)),
  setFacePasterInfo: (data) => dispatch(setFacePasterInfo(data)),
});
RenderswitchModule = connect(RDSMMapStateToProps, RDSMMapDispatchToProps)(RenderswitchModule);

const BeautyButton = (props) => {
  const dispatch = useDispatch();
  const showBeautify = useSelector((state) => {
    return state.shootStory.showBeautify;
  });
  return (
    <Pressable
      onPress={() => {
        dispatch(setShowBeautify());
        // this.setState({ showBeautify: !this.state.showBeautify });
      }}
    >
      <Image
        style={styles.beautifyIcon}
        source={showBeautify ? props.selectBeautify : props.beautifyImage}
        resizeMode='contain'
      />
    </Pressable>
  );
};
const RenderLeftButtons = React.memo((props) => {
  return (
    <>
      {/* 取消 */}
      <Pressable
        onPress={() => {
          props.goback();
        }}
        style={styles.closeBox}
      >
        <Image style={styles.closeIcon} source={props.closeImage} resizeMode='contain' />
      </Pressable>
      <View style={styles.leftIconBox}>
        {/* 音乐 */}
        <Pressable>
          <Image style={styles.musicIcon} source={props.musicImage} resizeMode='contain' />
        </Pressable>
        {/* 美颜 */}
        <BeautyButton {...props} />
      </View>
    </>
  );
});

class CameraScreen extends Component<Props, State> {
  static propTypes = {
    allowCaptureRetake: PropTypes.bool,
  };

  static defaultProps = {
    allowCaptureRetake: false,
  };

  currentFlashArrayPosition: number;
  flashArray: any[];
  camera: any;
  myRef: any;
  FlatListRef: any;
  scrollPos: Animated.Value;
  editor: any;

  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    this.FlatListRef = React.createRef();
    this.scrollPos = new Animated.Value(0);
    this.enableCount = { count: 0 };

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

    this.rt = null;
    this.cameraBox = { current: null };
    this.state = {
      // 照片存储
      captureImages: [],
      flashData: this.flashArray[this.currentFlashArrayPosition],
      torchMode: false,
      flag: null,
      videoRecording: false,
      ratios: [],
      ratioArrayPosition: -1,
      imageCaptured: null,
      captured: false,
      cameraType: CameraType.Front,
      currentIndex: 0,

      showBeautify: false,
      normalBeautyLevel: 3,

      progress: 0,

      startShoot: false,
      ShootSuccess: false,

      fadeInOpacity: new Animated.Value(60),

      // 视频 照片地址
      videoPath: null,
      fileType: 'video',

      //
      pasterList: [],
      //  插最前面
      facePasterInfo: { eid: 0 },

      // 音乐打开
      musicOpen: false,
      previewImage: {},
      relaloadFlag: null,
    };
  }

  componentDidMount() {
    let ratios = [];
    if (this.props.cameraRatioOverlay) {
      ratios = this.props.cameraRatioOverlay.ratios || [];
    }
    this.getPasterInfos();
    const { cameraModule } = this.props;
    this.setState({
      ratios: ratios || [],
      ratioArrayPosition: ratios.length > 0 ? 0 : -1,
    });
    setTimeout(() => {
      AVService.enableHapticIfExist();
    }, 2000);
  }

  componentWillUnmount() {
    if (Platform.OS === 'android') {
    }
    this.setState = () => false;
  }
  componentDidUpdate(props, state) {
    // this.myRef?.current?.show?.('点击拍照，长按拍视频', 1000);
  }
  shotPreview = async () => {
    try {
      const image = await this.cameraBox.current?.capture?.();
      this.setState({
        previewImage: image,
      });
      setTimeout(() => {
        this.setState({
          relaloadFlag: Math.random(),
        });
      }, 0);
    } catch (e) {}
  };
  shouldComponentUpdate(nextProps, nextState) {
    const stateUpdated = stateAttrsUpdate.some((key) => nextState[key] !== this.state[key]);
    if (stateUpdated) {
      return true;
    }
    if (nextProps.type !== this.props.type) {
      // this.cameraBox = { current: null }
      InteractionManager.runAfterInteractions(() => {
        if (this.rt) {
          clearTimeout(this.rt);
        }
        this.rt = setTimeout(() => {
          // this.props.setFacePasterInfo({eid: 0})

          this.setState({
            relaloadFlag: Math.random(),
          });
        }, 1000);
      });
      return false;
    }
    if (nextProps.connected !== this.props.connected && !nextProps.connected) {
      this.myRef?.current?.show?.('无法在你的设备使用此贴纸！', 2000);
      return true;
    }
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      // this.cameraBox = { current: null }
      InteractionManager.runAfterInteractions(() => {
        if (this.rt) {
          clearTimeout(this.rt);
        }

        this.rt = setTimeout(() => {
          // this.props.setFacePasterInfo({eid: 0})

          this.setState({
            relaloadFlag: Math.random(),
          });
        }, 1000);
      });
      return false;
    }
    return false;
  }
  isCaptureRetakeMode() {
    return !!(this.props.allowCaptureRetake && !_.isUndefined(this.state.imageCaptured));
  }

  _onRecordingDuration = (event) => {
    // EventBus.emit('record_duration', parseFloat(event.duration).toFixed(2));
  };

  // 进度条
  animate() {
    this.setState({
      flag: Math.random(),
    });
    return;
  }
  //  拍摄按钮
  getPasterInfos = async () => {
    const pasters = await AVService.getFacePasterInfos({});
    pasters.forEach((item, index) => {
      if (index == 0) {
        return;
      }
      //TODO
      if (item.icon) {
        item.icon = item.icon.replace('http://', 'https://');
      }
      if (item.url) {
        item.url = item.url.replace('http://', 'https://');
      }
    });
    pasters.unshift({ eid: 0 });
    this.setState({
      pasterList: pasters,
      facePasterInfo: pasters[0],
    });
  };
  renderCaptureButton() {
    const { fadeInOpacity, ShootSuccess, pasterList, musicOpen } = this.state;
    return (
      this.props.captureButtonImage && (
        // !this.isCaptureRetakeMode() && (
        <View style={[styles.captureButtonContainer, !musicOpen && { bottom: 30 }]}>
          {
            <>
              {/* 长按按钮 */}
              <View style={[styles.startShootBox, this.state.startShoot ? {} : { opacity: 0 }]}>
                <Animated.View
                  style={[styles.startShootAnnulus, { width: fadeInOpacity, height: fadeInOpacity }]}
                ></Animated.View>
                <View style={styles.captureButton}></View>

                <ProgressCircleWrapper
                  flag={this.state.flag}
                  setFlag={(flag) => {
                    this.setState({
                      flag,
                    });
                  }}
                  recordeSuccess={async (data) => {
                    const videoPath = await this.cameraBox.current?.stopRecording?.();
                    this.setState({
                      videoPath,
                      flag: null,
                      ShootSuccess: true,
                      startShoot: false,
                    });
                    // this.setState({
                    //   videoPath,
                    // });
                  }}
                />
              </View>
              {/* 普通的切换按钮 */}
              <View style={!this.state.startShoot ? {} : { opacity: 0 }}>
                {this.state.musicOpen ? (
                  <StoryMusic musicDynamicGif={this.props.musicDynamicGif} musicIconPng={this.props.musicIconPng} />
                ) : null}
                {/* this.state.musicOpen */}
              </View>
            </>
          }
        </View>
      )
      // )
    );
  }

  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }

  // 相机切换
  onSwitchCameraPressed() {
    const direction = this.state.cameraType === CameraType.Back ? CameraType.Front : CameraType.Back;
    this.setState({ cameraType: direction });
  }
  // 拍照功能  改变文件类型
  onCaptureImagePressed = async () => {
    try {
      const image = await this.cameraBox.current?.capture?.();
      //  ios
      let sandData = '';
      //
      if (Platform.OS !== 'android') {
        // sandData = await AVService.saveToSandBox({ path: image?.uri });
        sandData = image?.uri;
      } else {
        sandData = image;
      }
      if (this.props.allowCaptureRetake) {
        this.setState({ imageCaptured: sandData, fileType: 'image' });
      } else {
        if (image) {
          this.setState({
            captured: true,
            imageCaptured: sandData,
            fileType: 'image',
            // captureImages: _.concat(this.state.captureImages, image?.uri),?
            captureImages: _.concat(this.state.captureImages, sandData),
          });
          this.props.setType('storyedit');
          this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) });
        }
      }
    } catch (e) {
      console.info(e);
    }
  };

  setShootData = (data) => {
    try {
      this.setState(data);
      this.props.setType('storyedit');
    } catch (e) {
      console.info(e, '拍摄出错');
    }
  };
  // 底部渲染
  renderBottom() {
    return (
      <View style={{ position: 'absolute', bottom: 0, width: '100%' }}>
        <RenderbeautifyBox {...this.props} />
        <Carousel
          {...this.props}
          myRef={this.myRef}
          onCaptureImagePressed={this.onCaptureImagePressed}
          camera={this.cameraBox}
          enableCount={this.enableCount}
          setShootData={this.setShootData}
        />
        <RenderswitchModule {...this.props} camera={this.cameraBox} />
      </View>
    );
  }
  // ？？？
  onRatioButtonPressed() {
    const newRatiosArrayPosition = (this.state.ratioArrayPosition + 1) % this.state.ratios.length;
    this.setState({ ratioArrayPosition: newRatiosArrayPosition });
  }

  render() {
    return (
      // TODO
      <View style={{ backgroundColor: '#000', flex: 1 }}>
        <Toast
          ref={this.myRef}
          position='center'
          positionValue={70}
          fadeInDuration={750}
          fadeOutDuration={1000}
          opacity={0.8}
        />

        {this.state.ShootSuccess ? (
          <StoryEditor
            {...this.props}
            myRef={this.myRef}
            rephotograph={() => {
              this.props.setType('story');
              this.setState({ ShootSuccess: false, videoPath: '', imageCaptured: '' });
            }}
            getUploadFile={async (data) => {
              await this.sendUploadFile(data);

              setTimeout(() => {
                this.setState({ ShootSuccess: false, videoPath: '', imageCaptured: '' });
                this.props.setType('story');
              }, 1000);
            }}
            insets={this.props.insets}
            setType={this.props.setType}
            AaImage={this.props.AaImage}
            filterImage={this.props.filterImage}
            musicRevampImage={this.props.musicRevampImage}
            giveUpImage={this.props.giveUpImage}
            noVolumeImage={this.props.noVolumeImage}
            tailorImage={this.props.tailorImage}
            volumeImage={this.props.volumeImage}
            videoPath={this.state.videoPath}
            fileType={this.state.fileType}
            videomusicIcon={this.props.videomusicIcon}
            musicDynamicGif={this.props.musicDynamicGif}
            musicIconPng={this.props.musicIconPng}
            musicIcongray={this.props.musicIcongray}
            musicSearch={this.props.musicSearch}
            imagePath={this.state.imageCaptured}
            noResultPng={this.props.noResultPng}
            // imagePath ={'/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/Media/1634557132176-photo.jpg'}
          />
        ) : (
          <>
            <RenderCamera {...this.props} camera={this.cameraBox} enableCount={this.enableCount} myRef={this.myRef} />
            {this.renderBottom()}
          </>
        )}
      </View>
    );
  }
}
export default connect(null, RDSMMapDispatchToProps)(CameraScreen);

const styles = StyleSheet.create({
  bottomButtons: {
    flex: 1,
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
  BottomBox: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    // backgroundColor:"green",
    alignItems: 'center',
    // position: 'absolute',
    // backgroundColor: "black",
    width: '100%',
    bottom: 0,
  },

  cameraContainer: {},
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

  videoTitle: {
    fontSize: 13,
    color: '#7E7E7E',
    lineHeight: 18,
    fontWeight: '500',
    position: 'absolute',
    right: 60,
  },
  snapshotTitle: {
    fontSize: 13,
    lineHeight: 18,
    color: '#FFFFFF',
  },
  snapshotMuse: {
    fontSize: 13,
    lineHeight: 18,
    color: '#FFFFFF',
    marginHorizontal: 30,
  },
  switchScreen: {},
  musicIcon: {
    width: 28,
    height: 28,
    left: -3,
  },

  beautifyIcon: {
    width: 28,
    height: 28,
    marginTop: 30,
  },

  closeIcon: {
    width: 28,
    height: 28,
  },
  beautifyBoxHead: {
    paddingHorizontal: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 15,
    paddingBottom: 26,
  },
  beautifyTitle: {
    fontSize: 15,
    fontWeight: '500',
    color: '#fff',
    lineHeight: 21,
  },
  beautyAdjustIcon: {
    width: 20,
    height: 16,
  },
  beautifyBoxContent: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  beautifySelect: {
    width: 48,
    height: 48,
    backgroundColor: ' rgba(69, 69, 73, 0.7)',
    borderRadius: 9,
    justifyContent: 'center',
    alignItems: 'center',
  },
  beautifySelectTitle: {
    fontSize: 20,
    fontWeight: '500',
    color: '#fff',
    lineHeight: 28,
  },
  beautifySelecin: {
    borderWidth: 2,
    borderColor: '#836BFF',
  },
  progress: {
    margin: 10,
  },

  uploadBox: {
    width: 130,
    height: 40,
    borderRadius: 22,
    backgroundColor: '#fff',
    justifyContent: 'center',
    alignItems: 'center',
  },
  uploadTitle: {
    fontWeight: '500',
    fontSize: 13,
    color: '#000',
    lineHeight: 18,
  },
  UpdateBox: {
    position: 'absolute',
    zIndex: 99,
    top: 20,
  },
  updateTopIcon: {
    width: 40,
    height: 40,
    marginRight: 10,
  },
  filterLensSelectTitle: {
    fontSize: 13,
    fontWeight: '500',
    color: '#fff',
    lineHeight: 18,
  },
  startShootAnnulus: {
    backgroundColor: 'rgba(255,255,255,0.5)',
    borderRadius: 122,
    position: 'absolute',
  },
  captureButton: {
    width: 49,
    height: 49,
    backgroundColor: '#fff',
    borderRadius: 49,
    position: 'absolute',
  },
  captureButtonContainer: {
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row',
    position: 'absolute',
    bottom: 0,
  },
  captureButtonImage: {
    position: 'absolute',
    left: itemWidth * 2 + (itemWidth - circleSize) / 2,
    zIndex: -11,
    elevation: 1,
    // top: -(circleSize - smallImageSize) / 2,
  },
  slider: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    display: 'flex',
    zIndex: 99,
    elevation: 10,
  },
  startShootBox: {
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
    left: captureIcon2,
  },

  propStyle: {
    backgroundColor: '#334',
    opacity: 0.8,
  },
});
