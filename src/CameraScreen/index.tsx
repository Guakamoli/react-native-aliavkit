import PropTypes from 'prop-types';
import React, { Component, useRef, useEffect, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  Dimensions,
  Platform,
  Animated,
  FlatList,
  Easing,
  InteractionManager,
  Pressable,
  TouchableOpacity,
  Alert,
  AppState,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import { connect, useSelector, useDispatch } from 'react-redux';

import FastImage from '@rocket.chat/react-native-fast-image';

import { request, requestMultiple, check, checkMultiple, openSettings, PERMISSIONS, RESULTS } from 'react-native-permissions';
import I18n from '../i18n';

import _ from 'lodash';
import Camera from '../Camera';
import CarouselWrapper from './Carousel';
import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import StoryEditor from '../StoryEditor';
import StoryMusic from '../StoryMusic';
import AVService from '../AVService';
import { BoxBlur } from 'react-native-image-filter-kit';
import RenderbeautifyBox from './RenderbeautifyBox';
import RenderCamera from './Camera';
import StoryPhoto from './StoryPhoto';
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
  showRenderBottom: Boolean;
  stopMulti: Boolean;
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
    //
    return (
      <View style={[styles.BottomBox, {}]}>
        <TouchableOpacity
          hitSlop={{ left: 10, top: 10, right: 10, bottom: 10 }}
          onPress={() => {
            this.props.setCameraType();
            setTimeout(() => {
              try {
                this.props.camera.current?.setPasterInfo?.(this.props.facePasterInfo);
              } catch (e) {

              }
            }, 100);
            AVService.enableHapticIfExist();
            this.props.haptics?.impactAsync(this.props.haptics.ImpactFeedbackStyle.Medium);
          }}
        >
          <FastImage style={{ width: 30, height: 25, marginBottom: marginBottom, marginRight: 20, }} source={this.props.cameraFlipImage} resizeMode='contain' />
        </TouchableOpacity>
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
  multiType: number;
  FlatListRef: any;
  scrollPos: Animated.Value;
  editor: any;
  mAppState = '';
  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    this.multiType = 0;
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
      showRenderBottom: true,

      //相册是否展开
      openPhotos: false,
      firstPhotoUri: '',
      stopMulti: false,
    };
    this.initPermissions();

  }

  setMultiType = (multiType) => {
    this.multiType = multiType;
    this.setState({ stopMulti: false });
  }

  setShowRenderBottom = (isShow) => {
    if (isShow) {
      this.setState({ showRenderBottom: true });
    } else {
      this.setState({ showRenderBottom: false });
    }

  }

  initPermissions = async (isActive = false) => {
    if (await this.checkCameraPermissions(false, true)) {
      this.setState({ loadedPermissions: true });
    } else {
      if (!isActive) {//RECORD
        if (await this.getRecordPermissions(true)) {
          this.setState({ loadedPermissions: true });
        }
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
  getRecordPermissions = async (isToSetting: boolean = false) => {
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
      Platform.OS === 'ios' ? I18n.t('Need_camera_permission') : "",
      Platform.OS === 'ios' ? "" : I18n.t('Need_camera_permission'),
      [
        {
          text: `${I18n.t('Not_set_yet')}`,
          style: "default",
        },
        {
          text: `${I18n.t('go_to_settings')}`,
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
      this.initPermissions(true);
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
    // this.myRef?.current?.show?.(`${I18n.t('Tap_to_take_a_photo_long_press_to_take_a_video')}`, 1000);
  }
  shouldComponentUpdate(nextProps, nextState) {
    if (this.state.stopMulti != nextState.stopMulti) {
      return true;
    }
    if (this.state.openPhotos != nextState.openPhotos) {
      return true;
    }
    if (this.state.firstPhotoUri != nextState.firstPhotoUri) {
      return true;
    }
    if (this.props.showRenderBottom != nextProps.showRenderBottom) {
      return true;
    }
    if (this.props.initStory != nextProps.initStory) {
      return true;
    }
    //
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
      if (nextProps.type === 'story') {
        this.initPermissions();
      }
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
      this.myRef?.current?.show?.(`${I18n.t('Cannot_use_this_sticker_on_your_device')}`, 2000);
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

    }
  };

  selectedPhoto = (uri, type) => {
    if (type === 'video') {
      this.setState({
        fileType: 'video',
        videoPath: uri,
        ShootSuccess: true,
      });
    } else {
      this.setState({
        fileType: 'image',
        imageCaptured: uri,
        ShootSuccess: true,
      });
    }
    this.props.setType('storyedit');
  }

  setShootData = (data) => {
    try {
      this.setState(data);
      this.props.setType('storyedit');
    } catch (e) {

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
      <View style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: this.props.bottomSpaceHeight + 120, width: '100%', zIndex: 99 }}>
        <RenderbeautifyBox {...this.props} />
        <View style={{ position: 'absolute', bottom: bottomHeight, backgroundColor: 'rgba(255,0,0,0)', height: this.state.showRenderBottom ? 'auto' : 0 }}>
          <CarouselWrapper
            {...this.props}
            stopMulti={this.state.stopMulti}
            setMultiType={this.setMultiType}
            myRef={this.myRef}
            onCaptureImagePressed={this.onCaptureImagePressed}
            camera={this.cameraBox}
            enableCount={this.enableCount}
            setShootData={this.setShootData}
          />
        </View>

        {/* TODOWUYQ */}
        {this.props.bottomToolsVisibility &&
          <View style={{ position: 'absolute', left: 20, width: 25, height: 25, borderRadius: 4, overflow: 'hidden', bottom: this.props.toolsInsetBottom + 5 }} >
            <TouchableOpacity
              hitSlop={{ left: 10, top: 10, right: 20, bottom: 10 }}
              onPress={() => {
                this.setState({ openPhotos: true });
              }}>
              {(!!this.state?.firstPhotoUri) ?
                <Image key={"firstPhotoUri"} style={{ width: 25, height: 25 }} resizeMode='cover' source={{ uri: this.state.firstPhotoUri }} />
                :
                <Image key={"requireUri"} style={{ width: 25, height: 25 }} resizeMode='cover' source={require('../../images/ic_story_photo.png')} />
              }

            </TouchableOpacity>
          </View>}


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

  bassGoBadck = () => {
    if (this.multiType === 0) {
      this.props.goback();
    } else {
      //清空多选录制
      this.setState({ stopMulti: true });
    }
  }


  CameraView() {
    return (
      <RenderCamera
        {...this.props}
        setShowRenderBottom={this.setShowRenderBottom}
        bassGoBadck={this.bassGoBadck}
        camera={this.cameraBox}
        enableCount={this.enableCount}
        myRef={this.myRef}

        setShootData={this.setShootData} />
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



        <View style={{ position: 'relative', width: "100%", height: '100%', display: !this.state.ShootSuccess ? 'flex' : 'none' }}>
          {this.CameraView()}
          <View style={{ display: this.props.type === 'story' ? 'flex' : 'none' }}>
            {this.renderBottom()}
          </View>
          {/* TODOWUYQ */}
          {<StoryPhoto
            myRef={this.myRef}
            {...this.props} selectedPhoto={this.selectedPhoto} openPhotos={this.state.openPhotos}
            setFirstPhotoUri={(uri: string) => {
              if (uri) {
                this.setState({
                  firstPhotoUri: uri,
                });
              }
            }}
            //关闭时执行
            onCloseView={() => {
              this.setState({ openPhotos: false });
            }}
          />}
        </View>
        {
          this.state.ShootSuccess &&
          <View style={{ width: "100%", height: '100%' }}>
            {this.CameraEditorView()}
          </View>
        }
      </View >
    );
  }
}
export default connect(null, RDSMMapDispatchToProps)(CameraScreen);

const styles = StyleSheet.create({
  BottomBox: {
    // flexDirection: 'row',
    // justifyContent: 'flex-end',
    // alignItems: 'flex-end',
    // height:'auto'
    // width: '100%',
    // backgroundColor:'red'
    position: 'absolute',
    right: 0,
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
