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
  Alert,
  AppState,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import { connect, useSelector, useDispatch } from 'react-redux';

import FastImage from '@rocket.chat/react-native-fast-image';

import { request, requestMultiple, check, checkMultiple, openSettings, PERMISSIONS, RESULTS } from 'react-native-permissions';

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

  // 视频路径
  videoPath: any;

  pasterList: any;
  facePasterInfo: any;

  fileType: String;
  musicOpen: Boolean;
};

/**
 * 前后摄像头切换
 */
class RenderswitchModule extends React.PureComponent {
  constructor(props) {
    super(props);
  }
  render() {
    let marginBottom = this.props.toolsInsetBottom + 5
    // console.info("RenderswitchModule marginBottom", marginBottom);
    return (
      <View style={styles.BottomBox}>
        <Pressable
          hitSlop={{ left: 20, top: 10, right: 20, bottom: 10 }}
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
          <FastImage style={{ width: 32, height: 26, marginBottom: marginBottom, marginRight: 20, }} source={this.props.cameraFlipImage} resizeMode='contain' />
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
  mAppState = '';
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
      loadedPermissions: false,
    };
    this.initPermissions();

  }

  initPermissions = async () => {
    if (await this.checkCameraPermissions(false, true)) {
      this.setState({ loadedPermissions: true });
    } else {
      if (await this.getStoragePermissions(true)) {
        this.setState({ loadedPermissions: true });
      }
    }
  }

  /**
  * 检测是否有权限
  */
  checkCameraPermissions = async (isToSetting: boolean = false, isCheckLimited: boolean = false) => {
    if (Platform.OS === 'android') {
      const permissions = [PERMISSIONS.ANDROID.CAMERA, PERMISSIONS.ANDROID.RECORD_AUDIO];
      const statuses = await checkMultiple(permissions);
      if (statuses[permissions[0]] === RESULTS.GRANTED && statuses[permissions[1]] === RESULTS.GRANTED) {
        return true;
      } else if (statuses[permissions[0]] === RESULTS.BLOCKED || statuses[permissions[1]] === RESULTS.BLOCKED) {
        //拒绝且不再询问
        if (isToSetting) {
          this.showToSettingAlert();
        }
      }
    } else if (Platform.OS === 'ios') {
      const permissions = [PERMISSIONS.IOS.CAMERA, PERMISSIONS.IOS.MICROPHONE];
      const statuses = await checkMultiple(permissions);
      if (statuses[permissions[0]] === RESULTS.GRANTED && statuses[permissions[1]] === RESULTS.GRANTED) {
        return true;
      } else if (statuses[permissions[0]] === RESULTS.BLOCKED || statuses[permissions[1]] === RESULTS.BLOCKED) {
        if (isToSetting) {
          this.showToSettingAlert();
        }
      } else if (statuses[permissions[0]] === RESULTS.LIMITED || statuses[permissions[1]] === RESULTS.LIMITED) {
        return isCheckLimited;
      }
    }
    return false;
  }

  /**
 *  获取权限
 * @param isToSetting  是否展示去设置的 Alert
 */
  getStoragePermissions = async (isToSetting: boolean = false) => {
    if (Platform.OS === 'android') {
      const permissions = [PERMISSIONS.ANDROID.CAMERA, PERMISSIONS.ANDROID.RECORD_AUDIO];
      const statuses = await requestMultiple(permissions);
      if (statuses[permissions[0]] === RESULTS.GRANTED && statuses[permissions[1]] === RESULTS.GRANTED) {
        return true;
      } else if (statuses[permissions[0]] === RESULTS.DENIED || statuses[permissions[1]] === RESULTS.DENIED) {
      } else if (statuses[permissions[0]] === RESULTS.BLOCKED || statuses[permissions[1]] === RESULTS.BLOCKED) {
        if (isToSetting) {
          this.showToSettingAlert();
        }
      }
    } else if (Platform.OS === 'ios') {
      const permissions = [PERMISSIONS.IOS.CAMERA, PERMISSIONS.IOS.MICROPHONE];
      const statuses = await requestMultiple(permissions);
      if (statuses[permissions[0]] === RESULTS.GRANTED && statuses[permissions[1]] === RESULTS.GRANTED) {
        return true;
      } else if (statuses[permissions[0]] === RESULTS.BLOCKED || statuses[permissions[1]] === RESULTS.BLOCKED) {
        if (isToSetting) {
          this.showToSettingAlert();
        }
      }
    }
    return false;
  };


  showToSettingAlert = () =>
    Alert.alert(
      Platform.OS === 'ios' ? "“拍鸭”需要获取您的相机和麦克风权限" : "",
      Platform.OS === 'ios' ? "" : "“拍鸭”需要获取您的相机和麦克风权限",
      [
        {
          text: "暂不设置",
          style: "default",
        },
        {
          text: "去设置",
          onPress: () => openSettings(),
          style: "default",
        },
      ],
      {
        cancelable: true,
      }
    );

  componentDidMount() {
    let ratios = [];
    if (this.props.cameraRatioOverlay) {
      ratios = this.props.cameraRatioOverlay.ratios || [];
    }
    const { cameraModule } = this.props;
    this.setState({
      ratios: ratios || [],
      ratioArrayPosition: ratios.length > 0 ? 0 : -1,
    });
    setTimeout(() => {
      AVService.enableHapticIfExist();
    }, 2000);

    AppState.addEventListener('change', this._handleAppStateChange);

  }


  _handleAppStateChange = (nextAppState) => {
    if (this.mAppState === nextAppState) {
      return
    }
    if (this.props.isExample) {
      if (this.props.type !== 'story') {
        return
      }
    } else {
      if (!this.props.isDrawerOpen || this.props.type !== 'story') {
        return
      }
    }
    if (nextAppState === 'active') {
      this.mAppState = 'active';
      this.initPermissions();
    } else {
      this.mAppState = 'background';
    }
  };

  componentWillUnmount() {
    if (Platform.OS === 'android') {
      // this.props.camera?.current?.release();
    }
    this.setState = () => false;
    AppState.removeEventListener('change', this._handleAppStateChange);

  }
  componentDidUpdate(props, state) {
    // this.myRef?.current?.show?.('点击拍照，长按拍视频', 1000);
  }
  shouldComponentUpdate(nextProps, nextState) {

    if (this.props.initStory != nextProps.initStory) {
      return true;
    }
    // console.info(nextProps.initStory, nextProps.type);
    if (this.state.loadedPermissions != nextState.loadedPermissions) {
      return true;
    }
    if (this.props.bottomToolsVisibility != nextProps.bottomToolsVisibility) {
      return true;
    }

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
        }, this.props.initStory ? 0 : 0);
      });
      return true;
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
      let imagePath = '';
      if (Platform.OS !== 'android') {
        imagePath = image?.uri;
      } else {
        imagePath = image;
      }
      if (this.props.allowCaptureRetake) {
        this.setState({ imageCaptured: imagePath, fileType: 'image' });
      } else {
        if (image) {
          // this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) });
          this.setState({
            ShootSuccess: true,
            fileType: 'image',
            imageCaptured: imagePath,
          });
          this.props.setType('storyedit');
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

    let bottomHeight = 60;

    if (this.props.bottomSpaceHeight) {
      bottomHeight = this.props.bottomSpaceHeight + 10;
    } else {
      bottomHeight = this.props.toolsInsetBottom * 2 + 36;
    }

    return (
      <View style={{ position: 'absolute', bottom: 0, width: '100%' }}>
        <RenderbeautifyBox {...this.props} />
        <View
          style={{ position: 'absolute', bottom: bottomHeight, backgroundColor: 'rgba(255,0,0,0)' }}
        >
          <Carousel
            {...this.props}
            myRef={this.myRef}
            onCaptureImagePressed={this.onCaptureImagePressed}
            camera={this.cameraBox}
            enableCount={this.enableCount}
            setShootData={this.setShootData}
          />
        </View>

        {this.props.bottomToolsVisibility && <RenderswitchModule {...this.props} camera={this.cameraBox} />}
      </View>
    );
  }
  // ？？？
  onRatioButtonPressed() {
    const newRatiosArrayPosition = (this.state.ratioArrayPosition + 1) % this.state.ratios.length;
    this.setState({ ratioArrayPosition: newRatiosArrayPosition });
  }




  CameraEditorView() {
    return (
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
          }, 0);
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
    )
  }


  CameraView() {
    return (
      <RenderCamera {...this.props} camera={this.cameraBox} enableCount={this.enableCount} myRef={this.myRef} />
    );
  }

  render() {
    if (!this.state.loadedPermissions) {
      return null;
    }
    return (
      <View style={{ backgroundColor: '#000', flex: 1, position: 'relative' }}>
        <Toast
          ref={this.myRef}
          position='center'
          positionValue={70}
          fadeInDuration={750}
          fadeOutDuration={1000}
          opacity={0.8}
        />

        <View style={{ width: "100%", height: '100%', display: !this.state.ShootSuccess ? 'flex' : 'none' }}>
          {this.CameraView()}
          {this.renderBottom()}
        </View>

        {this.state.ShootSuccess &&
          <View style={{ width: "100%", height: '100%' }}>
            {this.CameraEditorView()}
          </View>
        }
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
    alignItems: 'center',
    width: '100%',
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
