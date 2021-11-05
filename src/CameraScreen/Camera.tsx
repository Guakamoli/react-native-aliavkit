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
  Pressable,
} from 'react-native';
import { useInterval, useThrottleFn } from 'ahooks';
import { PanGestureHandler, State, TapGestureHandler } from 'react-native-gesture-handler';
import { connect, useSelector, useDispatch } from 'react-redux';
import AVService from '../AVService.ios';

import _ from 'lodash';
import Camera from '../Camera';

import { BoxBlur } from 'react-native-image-filter-kit';
import {
  setCameraType,
  setShowBeautify,

} from '../actions/story';
const FLASH_MODE_AUTO = 'auto';
const FLASH_MODE_ON = 'on';
const FLASH_MODE_OFF = 'off';

const { width, height } = Dimensions.get('window');
const itemWidth = Math.ceil(width / 5);
const circleSize = 78;
const smallImageSize = 52;
const bigImageSize = 64;
const captureIcon2 = (width - 20) / 2;
const CameraHeight = height - 100;
const stateAttrsUpdate = [
  'pasterList', 'facePasterInfo', 'showBeautify',
  'normalBeautyLevel', 'cameraType', 'ShootSuccess',
  'startShoot', 'flag', 'showCamera', 'relaloadFlag']

class RenderswitchModule extends React.PureComponent {
  constructor(props) {
    super(props)

  }
  shouldComponentUpdate() {
    return false
  }
  render() {
    return (
      <View style={styles.BottomBox}>
        <Pressable

          onPress={() => this.props.setCameraType()}
        >
          <Image style={{ width: 31, height: 28 }} source={this.props.cameraFlipImage} resizeMode='contain' />
        </Pressable>
      </View>
    );
  }
}
const RDSMMapStateToProps = state => ({
});
const RDSMMapDispatchToProps = dispatch => ({
  setCameraType: () => dispatch(setCameraType()),

});
RenderswitchModule = connect(RDSMMapStateToProps, RDSMMapDispatchToProps)(RenderswitchModule)

const BeautyButton = React.memo((props) => {
  const dispatch = useDispatch()
  const showBeautify = useSelector((state) => {
    return state.shootStory.showBeautify
  })
  return (
    <Pressable
      onPress={() => {
        dispatch(setShowBeautify())
        // this.setState({ showBeautify: !this.state.showBeautify });
      }}
    >
      <Image
        style={styles.beautifyIcon}
        source={showBeautify ? props.selectBeautify : props.beautifyImage}
        resizeMode='contain'
      />
    </Pressable>
  )
})
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
        <Pressable

        >
          <Image style={styles.musicIcon} source={props.musicImage} resizeMode='contain' />
        </Pressable>
        {/* 美颜 */}
        <BeautyButton {...props} />
      </View>
    </>
  );
})
// 拍摄内容渲染
class PreviewBack extends React.PureComponent{
  constructor (props){
    super(props)
    this.state= {
      previewImage: null
    }
  }
  shotPreview = async () => {
    try {
      const image = await this.props.camera.current.capture();
      setTimeout(() => {
        this.setState({
          previewImage: image
        })
      }, 0);
    } catch (e) {
      console.info(e,"拍摄错误")
    }

  }
  shouldComponentUpdate (nextProps, nextState){
    if (nextState.previewImage !== this.state.previewImage) {
      return true
    }
    if (nextProps.type !== this.props.type) {
      if (nextProps.type === 'post') {
        this.shotPreview()
      }
      return false
    }
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      if (!nextProps.isDrawerOpen) {
        this.shotPreview()
      }
      return false
    }
    return false
  }
  render (){
    return (
      <View style={{ position: "absolute", zIndex: 0, width: width, top: 0 }}>
      <BoxBlur
        image={
  
          <Image source={{ uri: this.state.previewImage?.uri }} style={{ width: width, height: CameraHeight - 123 }} resizeMode={'cover'} />
        }
        radius={70}
      />
    </View>
    )
  }

}
class RenderCamera extends Component {
  constructor(props) {
    super(props)
    this.state = {
      showCamera: true,
    }
  }
  shouldComponentUpdate(nextProps, nextState) {
    const propsUpdated = stateAttrsUpdate.some(key => nextProps[key] !== this.props[key]);
    if (propsUpdated) {
      return true;
    }
    const stateUpdated = stateAttrsUpdate.some(key => nextState[key] !== this.state[key]);
    if (stateUpdated) {
      return true;
    }
    if (nextProps.type !== this.props.type) {

      setTimeout(() => {
        this.setState({
          showCamera: nextProps.type === 'story' ? true : false
        })
      }, 0)
      setTimeout(() => {
        AVService.enableHapticIfExist()

      }, 2000);
      return false
    }
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
    
      setTimeout(() => {
        this.setState({
          showCamera: nextProps.isDrawerOpen && this.props.type === 'story' ? true : false
        }, () => {
          setTimeout(() => {
            AVService.enableHapticIfExist()

          }, 2000);
        })
      }, 0)

      return false
    }
    return false
  }
  renderCamera = () => {
    return (
      <View style={{ width: "100%", height: CameraHeight, overflow: "hidden" }}>
        <PreviewBack {...this.props} camera={this.props.camera}/>
        <View style={{ position: "absolute", zIndex: 1 }}>
          {this.state.showCamera ? (
            <Camera
              ref={(cam) => (this.props.camera.current = cam)}
              style={{ height: CameraHeight }}
              cameraType={this.props.cameraType}
              saveToCameraRoll={false}
              normalBeautyLevel={this.props.normalBeautyLevel * 10}
              facePasterInfo={this.props.facePasterInfo}
            />
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
            this.props.setShowBeautify()
          }}

        >
          <RenderLeftButtons {...this.props} key={'RenderLeftButtons'} />
          {this.renderCamera()}
        </Pressable>
      </View>
    );
  }

}
const RenderCameraMapStateToProps = state => ({
  cameraType: state.shootStory.cameraType,
  normalBeautyLevel:state.shootStory.normalBeautyLevel,
  facePasterInfo:state.shootStory.facePasterInfo,
});
const RenderCameraMapDispatchToProps = dispatch => ({
  setShowBeautify: () => dispatch(setShowBeautify(false)),

});
export default connect(RenderCameraMapStateToProps, RenderCameraMapDispatchToProps)(RenderCamera)
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
    backgroundColor: "black",
    width: "100%",
    bottom: 0,
  },

  cameraContainer: {

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
    left: -3
  },
  leftIconBox: {
    position: 'absolute',
    top: CameraHeight * 0.35,
    left: 20,
    zIndex: 99,
  },
  beautifyIcon: {
    width: 28,
    height: 28,
    marginTop: 30,
  },
  closeBox: {
    position: 'absolute',
    top: CameraHeight * 0.05,
    left: 20,
    zIndex: 99,
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
