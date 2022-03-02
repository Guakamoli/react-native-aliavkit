import PropTypes from 'prop-types';
import React, { Component, useRef, useEffect, useState } from 'react';
import {
  StyleSheet,
  Text,
  InteractionManager,
  View,
  // Pressable,
  Image,
  Dimensions,
  Platform,
  Animated,
  FlatList,
  Easing,
  Pressable,
  StatusBar,
  AppState,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import { connect, useSelector, useDispatch } from 'react-redux';
import AVService from '../AVService';
import CameraRoll from '@react-native-community/cameraroll';

import _ from 'lodash';
import Camera from '../Camera';

import { BoxBlur } from 'react-native-image-filter-kit';
import { setCameraType, setShowBeautify } from '../actions/story';
const FLASH_MODE_AUTO = 'auto';
const FLASH_MODE_ON = 'on';
const FLASH_MODE_OFF = 'off';

const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const smallImageSize = 52;
const bigImageSize = 64;
const captureIcon2 = (width - 20) / 2;
const CameraHeight = height - 120;
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

const BeautyButton = React.memo((props) => {
  const dispatch = useDispatch();
  const showBeautify = useSelector((state) => {
    return state.shootStory.showBeautify;
  });
  return (
    <Pressable
      onPress={() => {
        AVService.enableHapticIfExist();

        props.haptics?.impactAsync(props.haptics.ImpactFeedbackStyle.Medium);

        dispatch(setShowBeautify());
        // this.setState({ showBeautify: !this.state.showBeautify });
      }}
    >
      <Image
        style={styles.closeIcon}
        source={require('../../images/ic_beauty_select.png')}
        // source={showBeautify ? props.selectBeautify : props.beautifyImage}
        resizeMode='contain'
      />
    </Pressable>
  );
});
const RenderLeftButtons = React.memo((props) => {
  return (
    <View style={{
      position: 'absolute', backgroundColor: 'rgba(0,0,0,0)', width: '100%', height: 40,
      flexDirection: 'row',
      justifyContent: 'space-between',
      paddingLeft: 20,
      paddingRight: 20,
      marginTop: 20,
    }}>
      {/* 取消 */}
      <Pressable onPress={() => {
        props.goback();
      }}>
        {/* <Image style={styles.closeIcon} source={props.closeImage} resizeMode='contain' /> */}
        <Image style={styles.closeIcon} source={require('../../images/ic_story_close.png')} resizeMode='contain' />
      </Pressable>
      <BeautyButton {...props} />
    </View>
  );
});
// 拍摄内容渲染
class PreviewBack extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      previewImage: null,
    };
  }
  shotPreview = async () => {
    try {
      const image = await this.props.camera.current.capture();
      const prevImage = this.state.previewImage;
      this.props.camera.current = null;
      setTimeout(() => {
        this.setState(
          {
            previewImage: image,
          },
          () => {
            if (prevImage) {
              CameraRoll.deletePhotos([prevImage.uri]);
            }
          },
        );
      }, 0);
    } catch (e) {
      console.info(e, '拍摄错误');
    }
  };
  shouldComponentUpdate(nextProps, nextState) {
    if (nextState.previewImage !== this.state.previewImage) {
      return true;
    }
    if (nextProps.type !== this.props.type) {
      this.props.enableCount.count = 0;
      if (nextProps.type === 'post') {
        this.shotPreview();
      }
      return false;
    }
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      this.props.enableCount.count = 0;

      if (!nextProps.isDrawerOpen) {
        this.shotPreview();
      }
      return false;
    }
    return false;
  }
  render() {
    return (
      <View style={{ position: 'absolute', zIndex: 0, width: width, top: 0 }}>
        <BoxBlur
          image={
            <Image
              source={{ uri: this.state.previewImage?.uri }}
              style={{ width: width, height: this.props.CameraFixHeight }}
              resizeMode={'cover'}
            />
          }
          radius={70}
        />
      </View>
    );
  }
}
class RenderCamera extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showCamera: this.props.type === 'story' && this.props.isDrawerOpen,
      showToast: false,
    };
    // this.fadeAnim = new Animated.Value(1);
  }
  handleAppStateChange = (e) => {
    if (this.props.isDrawerOpen && this.props.type === 'story') {
      if (e.match(/inactive|background/)) {
        this.setState({
          showCamera: false,
        });
        setTimeout(() => {
          AVService.enableHapticIfExist();
        }, 2000);
      } else {
        this.setState({
          showCamera: true,
        });
        setTimeout(() => {
          AVService.enableHapticIfExist();
        }, 2000);
      }
    }
  };
  componentDidMount() {
    console.log("Story 录制初始化 componentDidMount");
    if (Platform.OS === 'ios') {
      AppState.addEventListener('change', this.handleAppStateChange);
    }
  }
  componentWillUnmount() {
    console.log("Story 录制销毁 componentWillUnmount");
    this.props.camera?.current?.release();
    if (Platform.OS === 'ios') {
      AppState.removeEventListener('change', this.handleAppStateChange);
    }
  }
  shouldComponentUpdate(nextProps, nextState) {

    if (nextProps.showBeautify != this.props.showBeautify) {
      if (nextProps.showBeautify) {
        this.props.hideBottomTools();
      } else {
        this.props.showBottomTools();
      }
      return false;
    }

    const propsUpdated = stateAttrsUpdate.some((key) => nextProps[key] !== this.props[key]);
    if (propsUpdated) {
      return true;
    }
    const stateUpdated = stateAttrsUpdate.some((key) => nextState[key] !== this.state[key]);
    if (stateUpdated) {
      return true;
    }

    if (nextProps.type !== this.props.type) {
      const showCamera = nextProps.type === 'story' && nextProps.isDrawerOpen ? true : false;
      if (!showCamera) {
        this.props.camera.current?.cameraStopPreview?.();
      }
      this.setState({
        showCamera,
      });

      setTimeout(() => {
        AVService.enableHapticIfExist();
      }, 2000);

      return false;
    }
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      const showCamera = nextProps.isDrawerOpen && nextProps.type === 'story' ? true : false;
      if (!showCamera) {
        this.props.camera.current?.cameraStopPreview?.();
      }
      this.setState(
        {
          showCamera,
        },
        () => {
          setTimeout(() => {
            AVService.enableHapticIfExist();
          }, 2000);
        },
      );

      return false;
    }
    return false;
  }
  renderCamera = () => {
    //TODO
    // const topheight = Platform.OS === 'ios' ? this.props.insets.top : 0;
    // const CameraFixHeight = height - (this.props.insets.bottom + topheight + 30 + 28);
    let CameraFixHeight = width * 16 / 9;
    const fixHeight = height - this.props.insets.top - this.props.insets.bottom
    if (CameraFixHeight > fixHeight) {
      CameraFixHeight = fixHeight;
    }
    // console.info("Camera CameraFixHeight", CameraFixHeight, height, this.props.insets.top, this.props.insets.bottom);
    //TODO
    return (
      <View style={{ width: '100%', height: CameraFixHeight, overflow: 'hidden', borderRadius: 20 }}>
        <PreviewBack {...this.props} camera={this.props.camera} CameraFixHeight={CameraFixHeight} />
        <View
          style={{ position: 'absolute', zIndex: 1, width: '100%' }}
          // style={{ width: '100%', height: CameraFixHeight }}
          onLayout={() => {
            setTimeout(() => {
              AVService.enableHapticIfExist();
            }, 0);
          }}
        >
          {this.state.showCamera || this.props.isExample ? (
            <View style={{ height: CameraFixHeight, width, position: 'relative' }}>
              <Camera
                ref={(cam) => (this.props.camera.current = cam)}
                cameraStyle={{ height: CameraFixHeight, width }}
                flashMode={FLASH_MODE_AUTO}
                cameraType={this.props.cameraType}
                saveToCameraRoll={false}
                focusMode={'on'}
                normalBeautyLevel={this.props.normalBeautyLevel * 10}
                facePasterInfo={this.props.facePasterInfo}
                torchMode={'off'}
                onReadCode={() => { }}
                onRecordingProgress={() => { }}
              />
              {/* {this.state.showToast && (
                <Animated.View
                  style={[
                    styles.toastBox,
                    {
                      opacity: this.fadeAnim,
                    },
                  ]}
                >
                  <Text style={{ textAlign: 'center', fontSize: 14, color: '#000', lineHeight: 40, fontWeight: '500' }}>
                    点击拍照,长按拍视频
                  </Text>
                  <View style={styles.toast}></View>
                </Animated.View>
              )} */}
            </View>
          ) : null}
        </View>
      </View>
    );
  };
  render() {
    return (
      <View>
        <Pressable
          onPress={() => {
            this.props.setShowBeautify();
          }}
        >
          {this.renderCamera()}
          <RenderLeftButtons {...this.props} key={'RenderLeftButtons'} />
        </Pressable>
      </View>
    );
  }
}
const RenderCameraMapStateToProps = (state) => ({
  cameraType: state.shootStory.cameraType,
  normalBeautyLevel: state.shootStory.normalBeautyLevel,
  facePasterInfo: state.shootStory.facePasterInfo,
  showBeautify: state.shootStory.showBeautify,
});
const RenderCameraMapDispatchToProps = (dispatch) => ({
  setShowBeautify: () => dispatch(setShowBeautify(false)),
});
export default connect(RenderCameraMapStateToProps, RenderCameraMapDispatchToProps)(RenderCamera);
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
    paddingHorizontal: 20,
    paddingVertical: 15,
    alignItems: 'center',
    // position: 'absolute',
    backgroundColor: 'black',
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
  leftIconBox: {
    position: 'absolute',
    top: CameraHeight * 0.35,
    left: 20,
    zIndex: 99,
  },
  beautifyIcon: {
    width: 40,
    height: 40,
  },
  closeBox: {
    position: 'absolute',
    top: CameraHeight * 0.05,
    left: 20,
    zIndex: 99,
  },
  closeIcon: {
    width: 40,
    height: 40,
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
  toastBox: {
    position: 'absolute',
    backgroundColor: 'rgba(255,255,255,0.85)',
    borderRadius: 20,
    width: 200,
    height: 40,
    zIndex: 18,
    left: (width - 200) / 2,
    bottom: 120,
  },
  toast: {
    width: 0,
    height: 0,
    borderWidth: 8,
    borderTopColor: 'rgba(255,255,255,0.85)',
    borderRightColor: 'transparent',
    borderBottomColor: 'transparent',
    borderLeftColor: 'transparent',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'absolute',
    top: 40,
    left: 100 - 4,
  },
});
