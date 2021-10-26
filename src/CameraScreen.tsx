import PropTypes from 'prop-types';
import React, { Component, useRef, useEffect, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Image,
  Dimensions,
  Platform,
  SafeAreaView,
  Animated,
  FlatList,
  Easing,
} from 'react-native';
import { useInterval } from 'ahooks';

import _ from 'lodash';
import Camera from './Camera';
import VideoEditor from './VideoEditor';
import Carousel, { getInputRangeFromIndexes } from './react-native-snap-carousel/src';
import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import PostUpload from './PostUpload';
import StoryEditor from './StoryEditor';
import EventBus from './EventBus';
import StoryMusic from './StoryMusic';
import AVService from './AVService.ios';

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

// import AVService from './AVService.ios.ts';

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
  scanBarcode: any;
  laserColor: any;
  frameColor: any;
  torchOnImage: any;
  torchOffImage: any;
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
  onBottomButtonPressed: (any) => void;
  getUploadFile: (any) => void;
  goback: any;
  cameraModule: boolean;

  multipleBtnImage: any;
  postCameraImage: any;
  startMultipleBtnImage: any;
  changeSizeImage: any;
  addPhotoBtnPng: any;
  postMutePng: any;
  postNoMutePng: any;

  musicDynamicGif: any;
  musicIconPng: any;
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
  mute: boolean;
  showFilterLens: boolean;
  filterLensSelect: number;
  timer: any;
  fadeInOpacity: any;

  // 视频路径
  videoPath: any;
  // 区分story/post
  storyShow: boolean;

  photoAlbum: any;
  photoAlbumselect: any;

  pasterList: any;
  facePasterInfo: any;
  filterName: any;
  fileType: String;
  musicOpen: Boolean;
};

const TestComponent = () => {
  return (
    <>
      <TouchableOpacity
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
      </TouchableOpacity>
    </>
  );
};
const ProgressCircleWrapper = (props) => {
  const { flag, recordeSuccess } = props;
  let [progress, setProgress] = useState(0);
  let [timer, setTimer] = useState(null);
  useInterval(() => {
    const newprogress = (progress += 1 / 140);
    if (newprogress >= 1) {
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
  myRef: any;
  FlatListRef: any;
  scrollPos: Animated.Value;
  editor: any;

  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    this.FlatListRef = React.createRef();
    this.scrollPos = new Animated.Value(0);

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
      // 是否静音
      mute: false,

      showFilterLens: false,
      filterLensSelect: 0,

      timer: null,

      fadeInOpacity: new Animated.Value(60),

      // 视频 照片地址
      videoPath: null,
      fileType: 'video',
      //
      storyShow: false,
      photoAlbum: [],
      photoAlbumselect: {},

      //
      pasterList: [],
      facePasterInfo: {},
      filterName: '原片',

      // 音乐打开
      musicOpen: false,
    };
  }

  //  aa =async ()=>{
  //    console.log(1231);

  //   const pasters = await this.camera.getPasterInfos();
  //   console.log('--------pasters',pasters)
  //   this.setState({
  //     pasterList: pasters,
  //     facePasterInfo: pasters[0]
  //   });
  // }
  componentDidMount() {
    // console.log('newDate',new Date().getMinutes());

    var getAlbums = CameraRoll.getAlbums({
      assetType: 'All',
    });
    getAlbums.then((data) => {
      // 相册数据
      this.setState({ photoAlbum: [], photoAlbumselect: {} });
    });
    // this.aa()

    let ratios = [];
    if (this.props.cameraRatioOverlay) {
      ratios = this.props.cameraRatioOverlay.ratios || [];
    }
    const { cameraModule } = this.props;
    this.setState({
      ratios: ratios || [],
      ratioArrayPosition: ratios.length > 0 ? 0 : -1,
      storyShow: cameraModule,
    });
  }

  componentWillUnmount() {
    if (Platform.OS === 'android') {
      console.log(Platform.OS === 'android');
      this.camera.release();
    }
    console.log('销毁');
    this.setState = () => false;
  }
  componentWillUpdate(props, state) {
    // 离开story
    // if(!state.storyShow && Platform.OS === 'android'){
    //   console.log(Platform.OS === 'android');
    //    this.camera.release();
    // }
  }
  // ？？？？
  isCaptureRetakeMode() {
    return !!(this.props.allowCaptureRetake && !_.isUndefined(this.state.imageCaptured));
  }
  // 闪光灯拍摄    ？？？？
  // renderFlashButton() {
  //   return (
  //     !this.isCaptureRetakeMode() && (
  //       <TouchableOpacity style={{ paddingHorizontal: 15 }} onPress={() => this.onSetFlash()}>
  //         <Image
  //           style={{ flex: 1, justifyContent: 'center' }}
  //           source={this.state.flashData.image}
  //           resizeMode='contain'
  //         />
  //       </TouchableOpacity>
  //     )
  //   );
  // }
  // 闪光灯
  // renderTorchButton() {
  //   return (
  //     !this.isCaptureRetakeMode() && (
  //       <TouchableOpacity style={{ paddingHorizontal: 15 }} onPress={() => this.onSetTorch()}>
  //         <Image
  //           style={{ flex: 1, justifyContent: 'center' }}
  //           source={this.state.torchMode ? this.props.torchOnImage : this.props.torchOffImage}
  //           resizeMode='contain'
  //         />
  //       </TouchableOpacity>
  //     )
  //   );
  // }

  // 底部 切换模块
  renderswitchModule() {
    const { captureImages, videoPath, musicOpen } = this.state;
    if (musicOpen) {
      return (
        <SafeAreaView style={styles.BottomBox}>
          <View style={{ flexDirection: 'row', justifyContent: 'center' }}>
            <TouchableOpacity onPress={() => this.setState({ storyShow: true })}>
              <Text style={styles.snapshotMuse}>配乐</Text>
            </TouchableOpacity>
          </View>
        </SafeAreaView>
      );
    }
    return (
      <SafeAreaView style={styles.BottomBox}>
        <>
          {/*  作品快拍 切换*/}
          {this.state.startShoot || this.state.ShootSuccess ? null : (
            <View style={{ flexDirection: 'row', justifyContent: 'center' }}>
              <TouchableOpacity
                onPress={() => {
                  // this.props.navigation.navigate('PostEditorBox')
                  console.log(this.props.navigation.push('PostUpload'));
                }}
              >
                <Text style={styles.videoTitle}>作品</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={() => this.setState({ storyShow: true })}>
                <Text style={styles.snapshotTitle}>快拍</Text>
              </TouchableOpacity>
            </View>
          )}
          {/* 发布 */}
          {/* {this.state.ShootSuccess ?
            <TouchableOpacity onPress={() => {
              let uploadFile = []
              if (videoPath) {
                let type = videoPath.split('.')
                uploadFile.push({
                  video_type: `video/${type[type.length - 1]}`,
                  title_link: videoPath,
                  type: "file"
                })
              } else {
                let type = captureImages[0].uri.split('.')
                uploadFile.push({
                  image_url: captureImages[0].uri,
                  image_type: `image/${type[type.length - 1]}`,
                  image_size: captureImages[0].size,
                  type: "file",
                })
              }
              this.sendUploadFile(uploadFile)
            }}>
              <View style={styles.uploadBox}>
                <Text style={styles.uploadTitle}>发布快拍</Text>
              </View>
            </TouchableOpacity>
            : null} */}
          {/* 相机翻转 */}
          {this.state.ShootSuccess ? null : (
            <TouchableOpacity style={styles.switchScreen} onPress={() => this.onSwitchCameraPressed()}>
              <Image style={{ width: 26, height: 23 }} source={this.props.cameraFlipImage} resizeMode='contain' />
            </TouchableOpacity>
          )}
        </>
      </SafeAreaView>
    );
  }

  // 左侧 三个icon
  renderLeftButtons() {
    return (
      <>
        {/* 取消 */}
        <TouchableOpacity
          onPress={() => {
            this.props.goback();
          }}
          style={styles.closeBox}
        >
          <Image style={styles.closeIcon} source={this.props.closeImage} resizeMode='contain' />
        </TouchableOpacity>
        <View style={styles.leftIconBox}>
          {/* 音乐 */}
          <TouchableOpacity
            onPress={() => {
              this.setState({ musicOpen: !this.state.musicOpen });
            }}
          >
            <Image style={styles.musicIcon} source={this.props.musicImage} resizeMode='contain' />
          </TouchableOpacity>
          {/* 美颜 */}
          <TouchableOpacity
            onPress={() => {
              this.setState({ showBeautify: !this.state.showBeautify });
            }}
          >
            <Image style={styles.beautifyIcon} source={this.props.beautifyImage} resizeMode='contain' />
          </TouchableOpacity>
        </View>
      </>
    );
  }
  // 编辑头部按钮
  // renderUpdateTop() {
  //   const imglist = [
  //     // 'filter':
  //     { 'img': this.props.filterImage, 'onPress': () => { this.setState({ showFilterLens: !this.state.showFilterLens }) } },
  //     // 'volume':
  //     { 'img': this.state.mute ? this.props.noVolumeImage : this.props.volumeImage, 'onPress': () => { this.setState({ mute: !this.state.mute }) }, },
  //     // 'tailor':
  //     { 'img': this.props.tailorImage, 'onPress': () => { } },
  //     // 'git':
  //     { 'img': this.props.musicRevampImage, 'onPress': () => { } },
  //     // 'Aa':
  //     { 'img': this.props.AaImage, 'onPress': () => { } }
  //   ]
  //   return (
  //     <>
  //       {/* 放弃 */}
  //       <TouchableOpacity onPress={() => {
  //         this.setState({ ShootSuccess: false, showFilterLens: false, filterLensSelect: 0, progress: 0, captureImages: [], uploadFile: null })
  //       }} style={[styles.UpdateBox, { left: 20 }]}>
  //         <Image
  //           style={styles.updateTopIcon}
  //           source={this.props.giveUpImage}
  //           resizeMode="contain"
  //         />
  //       </TouchableOpacity>
  //       {/* 编辑按钮组 */}
  //       <View style={[styles.UpdateBox, { right: 10, flexDirection: 'row' }]}>
  //         {
  //           imglist.map(item => {
  //             return (
  //               <TouchableOpacity onPress={item.onPress} >
  //                 <Image
  //                   style={styles.updateTopIcon}
  //                   source={item.img}
  //                   resizeMode="contain"
  //                 />
  //               </TouchableOpacity>
  //             )
  //           })
  //         }
  //       </View>
  //     </>
  //   )
  // }

  _onRecordingDuration = (event) => {
    // EventBus.emit('record_duration', parseFloat(event.duration).toFixed(2));
  };

  // 拍摄内容渲染
  renderCamera() {
    //  function  onExportVideo(event) {
    //   if (event.exportProgress === 1) {
    //     // this.setState({ startExportVideo: false });
    //    console.log('视频导出成功, path = ', event.outputPath);
    //   }
    // }
    // const  VideoEditors =()=>{
    //   console.log('12313',this.state.videoPath);
    //   return (

    //     <VideoEditor
    //     ref={(edit) => (this.editor = edit)}
    //     style={{ flex: 1, justifyContent: 'flex-end'}}
    //     filterName={this.state.filterName}
    //     // filterName={'胶片'}
    //     // videoPath={ this.state.captureImages.length > 0 ? this.state.captureImages[0].uri  :this.state.videoPath}
    //     videoPath={this.state.videoPath}
    //     saveToPhotoLibrary={true}
    //     startExportVideo={false}
    //     onExportVideo={onExportVideo}
    //   />
    //   )
    // }
    const shoot = () => {
      return (
        <Camera
          ref={(cam) => (this.camera = cam)}
          style={{ height: CameraHeight }}
          cameraType={this.state.cameraType}
          flashMode={this.state.flashData.mode}
          torchMode={this.state.torchMode ? 'on' : 'off'}
          ratioOverlay={this.state.ratios[this.state.ratioArrayPosition]}
          saveToCameraRoll={!this.props.allowCaptureRetake}
          showFrame={this.props.showFrame}
          scanBarcode={this.props.scanBarcode}
          laserColor={this.props.laserColor}
          frameColor={this.props.frameColor}
          onReadCode={this.props.onReadCode}
          normalBeautyLevel={this.state.normalBeautyLevel * 10}
          onRecordingProgress={this._onRecordingDuration}
          facePasterInfo={this.state.facePasterInfo}
        />
      );
    };

    return (
      <View style={[styles.cameraContainer]}>
        {this.isCaptureRetakeMode() ? (
          <Image style={{ flex: 1, justifyContent: 'flex-end' }} source={{ uri: this.state.imageCaptured }} />
        ) : (
          <TouchableOpacity
            style={[
              Platform.OS != 'android' && { flex: 1, justifyContent: 'flex-end' },
              { position: 'relative', borderRadius: 20 },
            ]}
            onPress={() => {
              this.setState({ showFilterLens: false, showBeautify: false });
            }}
            activeOpacity={1}
            disabled={!this.state.showBeautify}
          >
            {/* {this.state.ShootSuccess ? this.renderUpdateTop() : this.renderLeftButtons()} */}
            {/* {this.state.ShootSuccess ? VideoEditors(): shoot()} */}
            {this.renderLeftButtons()}
            {shoot()}
          </TouchableOpacity>
        )}
      </View>
    );
  }

  // 照片数量
  // numberOfImagesTaken() {
  //   const numberTook = this.state.captureImages.length;
  //   if (numberTook >= 2) {
  //     return numberTook;
  //   } else if (this.state.captured) {
  //     return '1';
  //   } else {
  //     return '';
  //   }
  // }

  // 进度条
  animate() {
    this.setState({
      flag: Math.random(),
    });
    return;
    let progress = 0;
    this.setState({ progress: 0 });
    const stopRecording = async () => {
      const videoPath = await this.camera.stopRecording();
      console.log('video saved to ', videoPath);
      this.setState({ videoPath });
    };
    this.setState({
      timer: setInterval(() => {
        progress += 1 / 140;
        console.log('进度条');

        if (progress > 1) {
          progress = 1;
          this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) });
          stopRecording();
          clearInterval(this.state.timer);
        }
        this.setState({ progress });
      }, 100),
    });
  }
  //  拍摄按钮
  renderCaptureButton() {
    const { fadeInOpacity, ShootSuccess, pasterList, musicOpen } = this.state;
    const getPasterData = async () => {
      const pasters = await this.camera.getPasterInfos();
      // console.log('--------pasters',pasters)
      this.setState({
        pasterList: pasters,
        facePasterInfo: pasters[0],
      });
    };
    if (pasterList.length < 1) {
      getPasterData();
      return null;
    }
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
                  recordeSuccess={async (data) => {
                    this.setState({
                      flag: null,
                      ShootSuccess: true,
                      startShoot: false,
                    });
                    const videoPath = await this.camera.stopRecording();
                    this.setState({
                      videoPath,
                    });
                  }}
                />
              </View>
              {/* 普通的切换按钮 */}
              <View style={!this.state.startShoot ? {} : { opacity: 0 }}>
                {/* <TouchableOpacity onPress={()=>{
                  console.log(23333);
                  
                  this.FlatListRef.scrollToIndex({ index: 0, animated: false  })   
                  // listRef.current.scrollToIndex({ index: 0, animated: false })
                  }} >
              <Image style={[{width:20,height:20},{position:"absolute",bottom:90,left:captureIcon3,}]} source={this.props.closeImage } />
                </TouchableOpacity> */}
                {/* {this.switchProp()} */}
                {this.state.musicOpen ? (
                  <StoryMusic musicDynamicGif={this.props.musicDynamicGif} musicIconPng={this.props.musicIconPng} />
                ) : (
                  this.switchProp()
                )}
                {/* this.state.musicOpen */}
              </View>
            </>
          }
        </View>
      )
      // )
    );
  }

  _scrollInterpolator = (index, carouselProps) => {
    const range = [3, 2, 1, 0, -1, -2, -3]; // <- Remember that this has to be declared in a reverse order
    const inputRange = getInputRangeFromIndexes(range, index, carouselProps);
    const outputRange = range;
    return { inputRange, outputRange };
  };
  _animatedStyles(index, animatedValue, carouselProps) {
    return {
      opacity: animatedValue.interpolate({
        inputRange: [2, 3],
        outputRange: [1, 2],

        extrapolate: 'clamp',
      }),
      transform: [
        {
          translateX: animatedValue.interpolate({
            inputRange: [-3, -2, -1, 0, 1, 2, 3],
            outputRange: [1.5 * smallImageSize, 0, 0, 0, 0, 0, -1.5 * smallImageSize],
            extrapolate: 'clamp',
          }),
        },
        {
          scale: animatedValue.interpolate({
            inputRange: [-3, -2, -1, 0, 1, 2, 3],
            outputRange: [0, 0.5, 0.8, 1, 0.8, 0.5, 0],
            extrapolate: 'clamp',
          }),
        },
        // {
        //   rotateY: animatedValue.interpolate({
        //     inputRange: [-3, -2, -1, 0, 1, 2, 3],
        //     outputRange: ['-90deg', '-40deg', '-10deg', '0deg', '10deg', '40deg', '90deg'],
        //     extrapolate: 'clamp',
        //   }),
        // },

        {},
      ],
    };
  }
  switchProp() {
    const { pasterList } = this.state;

    return (
      <View>
        <Carousel
          ref={(flatList) => {
            this.FlatListRef = flatList;
          }}
          snapToInterval={itemWidth}
          //  ref={this.FlatListRef}
          // scrollToIndex={()=>{animated: true, viewPosition: 0, index: 0} }
          // this._flatList.scrollToOffset({animated: true, viewPosition: 0, index: 0}); //跳转到顶部
          enableMomentum={true}
          scrollInterpolator={this._scrollInterpolator}
          slideInterpolatedStyle={this._animatedStyles}
          // onScroll={this._onScroll}
          enableSnap={true}
          data={pasterList}
          decelerationRate={'normal'}
          swipeThreshold={20}
          itemWidth={itemWidth}
          inactiveSlideOpacity={1}
          scrollPos={this.scrollPos}
          sliderWidth={width}
          slideStyle={{ justifyContent: 'center', alignItems: 'center' }}
          // initialNumToRender={4}
          contentContainerCustomStyle={{ height: 100, justifyContent: 'center', alignItems: 'center' }}
          // justifyContent='center'
          useScrollView={true}
          firstItem={this.state.currentIndex}
          onBeforeSnapToItem={(slideIndex = 0) => {
            console.log('asdasdasd', slideIndex);
            this.setState({
              currentIndex: slideIndex,
              facePasterInfo: pasterList[slideIndex],
            });
          }}
          renderItem={({ index, item }) => {
            const img = { width: smallImageSize, height: smallImageSize, borderRadius: smallImageSize };
            const toItem = () => {
              this.FlatListRef?.snapToItem?.(index);
            };
            return (
              <TouchableOpacity delayLongPress={1000} onPress={toItem}>
                <View style={{ position: 'relative' }}>
                  <View style={[styles.propStyle, img]}>
                    <Image style={img} source={{ uri: item.icon }} />
                  </View>
                </View>
              </TouchableOpacity>
            );
          }}
        >
          <Animated.View
            style={[
              styles.captureButtonImage,
              { width: circleSize, height: circleSize, borderRadius: circleSize, zIndex: 11 },
              {
                transform: [{ translateX: Animated.multiply(this.scrollPos, 1) }],
              },
            ]}
          >
            <TouchableOpacity
              style={[{ width: circleSize, height: circleSize, borderRadius: circleSize, overflow: 'hidden' }]}
              delayLongPress={500}
              // 长按
              pressRetentionOffset={{ bottom: 1000, left: 1000, right: 1000, top: 1000 }}
              onLongPress={async () => {
                // 按钮动画
                Animated.timing(
                  // 随时间变化而执行动画
                  this.state.fadeInOpacity, // 动画中的变量值
                  {
                    toValue: 122, // 透明度最终变为1，即完全不透明
                    duration: 500, // 让动画持续一段时间
                    useNativeDriver: false,
                  },
                ).start();
                const success = await this.camera.startRecording();
                this.setState({ fileType: 'video', startShoot: success });
                console.log('success', success);
                if (success) {
                  // 调用进度条 开始拍摄
                  this.animate();
                } else {
                  this.myRef.current.show('摄像失败,请重试', 2000);
                }
              }}
              // 长按结束

              onPressOut={async () => {
                console.log('onPressOut');

                if (this.state.startShoot) {
                  const videoPath = `file://${encodeURI(await this.camera.stopRecording())}`;
                  // console.log('video saved to an ', videoPath);
                  this.setState({ fileType: 'video', videoPath });
                  this.setState({
                    startShoot: false,
                    ShootSuccess: true,
                    fadeInOpacity: new Animated.Value(60),
                    flag: null,
                  });
                }
              }}
              // 单击
              onPress={() => {
                const { startShoot, progress } = this.state;
                if (!startShoot || progress === 0) {
                  // 拍照
                  this.onCaptureImagePressed();
                  this.setState({ fileType: 'image' });
                }
              }}
            >
              {/* <View style={styles.captureButtonImage}> */}
              <Image
                source={this.props.captureButtonImage}
                style={{ width: circleSize, height: circleSize, zIndex: 1 }}
              />
              <Animated.View
                style={{
                  position: 'absolute',
                  flexDirection: 'row',
                  left: -(itemWidth - circleSize) / 2,
                  top: (circleSize - bigImageSize) / 2,
                  transform: [{ translateX: Animated.multiply(this.scrollPos, -1) }],
                }}
              >
                {pasterList.map((i, index) => {
                  return (
                    <View
                      style={{
                        alignItems: 'center',
                        width: itemWidth,
                      }}
                    >
                      <Animated.View
                        style={[
                          styles.propStyle,
                          {
                            width: bigImageSize,
                            height: bigImageSize,
                            opacity: 1,
                            borderRadius: bigImageSize,
                            transform: [
                              {
                                translateX: this.scrollPos.interpolate({
                                  inputRange: [(index - 1) * itemWidth, index * itemWidth, (index + 1) * itemWidth],
                                  outputRange: [
                                    (bigImageSize - smallImageSize) / 2,
                                    0,
                                    -(bigImageSize - smallImageSize) / 2,
                                  ],

                                  extrapolate: 'clamp',
                                }),
                              },
                            ],
                          },
                        ]}
                      >
                        <Image
                          style={{ width: bigImageSize, height: bigImageSize, borderRadius: bigImageSize }}
                          source={{ uri: i.icon }}
                        />
                      </Animated.View>
                    </View>
                  );
                })}
              </Animated.View>
              {/* </View> */}
            </TouchableOpacity>
          </Animated.View>
        </Carousel>

        {/* 临时方案  安卓 拍摄不会触发 */}
      </View>
    );
  }

  // ？？？
  // renderRatioStrip() {
  //   if (this.state.ratios.length === 0 || this.props.hideControls) {
  //     return null;
  //   }
  //   return (
  //     <View style={{ flex: 1, flexDirection: 'column', justifyContent: 'flex-end' }}>
  //       <View style={{ flexDirection: 'row', alignItems: 'center', paddingRight: 10, paddingLeft: 20 }}>
  //         <Text style={styles.ratioBestText}>Your images look best at a {this.state.ratios[0] || ''} ratio</Text>
  //         <TouchableOpacity
  //           style={{ flex: 1, flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', padding: 8 }}
  //           onPress={() => this.onRatioButtonPressed()}
  //         >
  //           <Text style={styles.ratioText}>{this.state.ratioOverlay}</Text>
  //         </TouchableOpacity>
  //       </View>
  //     </View>
  //   );
  // }
  //  弹出照片数据 ???
  sendBottomButtonPressedAction(type, captureRetakeMode, image) {
    if (this.props.onBottomButtonPressed) {
      this.props.onBottomButtonPressed({ type, captureImages: this.state.captureImages, captureRetakeMode, image });
    }
  }
  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }
  // cancel 按钮点击 ???
  // onButtonPressed(type) {
  //   const captureRetakeMode = this.isCaptureRetakeMode();
  //   if (captureRetakeMode) {
  //     if (type === 'left') {
  //       this.setState({ imageCaptured: undefined });
  //     }
  //   } else {
  //     this.sendBottomButtonPressedAction(type, captureRetakeMode, null);
  //   }
  // }
  // ????
  // renderBottomButton(type) {
  //   const showButton = true;
  //   if (showButton) {
  //     const buttonNameSuffix = this.isCaptureRetakeMode() ? 'CaptureRetakeButtonText' : 'ButtonText';
  //     const buttonText = _(this.props).get(`actions.${type}${buttonNameSuffix}`);
  //     return (
  //       <TouchableOpacity
  //         style={[styles.bottomButton, { justifyContent: type === 'left' ? 'flex-start' : 'flex-end' }]}
  //       // onPress={() => this.onButtonPressed(type)}
  //       >
  //         <Text style={styles.textStyle}>{buttonText}</Text>
  //       </TouchableOpacity>
  //     );
  //   } else {
  //     return <View style={styles.bottomContainerGap} />;
  //   }
  // }

  // 相机切换
  onSwitchCameraPressed() {
    const direction = this.state.cameraType === CameraType.Back ? CameraType.Front : CameraType.Back;
    this.setState({ cameraType: direction });
  }
  //闪光灯拍摄 事件
  // onSetFlash() {
  //   this.currentFlashArrayPosition = (this.currentFlashArrayPosition + 1) % 3;
  //   const newFlashData = this.flashArray[this.currentFlashArrayPosition];
  //   this.setState({ flashData: newFlashData });
  // }
  // 闪光 事件
  // onSetTorch() {
  //   this.setState({ torchMode: !this.state.torchMode });
  // }
  // 拍照功能  改变文件类型
  async onCaptureImagePressed() {
    const image = await this.camera.capture();
    console.log('capture image path ', image);
    //  ios
    let sandData = '';
    //
    if (Platform.OS !== 'android') {
      sandData = await AVService.saveToSandBox({ path: image?.uri });
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
        this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) });
      }
      // this.sendBottomButtonPressedAction('capture', false, image);
    }
  }
  // 美颜 滤镜 box
  renderbeautifyBox() {
    // const list = [
    //   { title: "原片" },
    //   { title: "灰白" },
    //   { title: "柔柔" },
    //   { title: "波普" },
    //   { title: "胶片" },

    // ]
    return (
      <View style={{ height: 189, backgroundColor: '#000', width: width, zIndex: 99, position: 'absolute', bottom: 0 }}>
        <View style={styles.beautifyBoxHead}>
          {/* <Text style={styles.beautifyTitle}>{this.state.showFilterLens ? `滤镜` : `美颜`}</Text> */}
          <Text style={styles.beautifyTitle}>{`美颜`}</Text>

          <Image style={styles.beautyAdjustIcon} source={this.props.beautyAdjustImag} resizeMode='contain' />
        </View>
        {/* {this.state.showFilterLens
          ?
          <View style={{ paddingHorizontal: 20 }}>
            <FlatList
              data={list}
              horizontal={true}
              style={{ margin: 0, padding: 0, height: 80 }}
              renderItem={({ index, item }) => {
                return (
                  <>
                    <TouchableOpacity onPress={() => {
                      this.setState({ filterLensSelect: index ,filterName:item.title})
                    }}>
                      <View style={{ flexDirection: 'column', justifyContent: 'center', alignItems: 'center', marginRight: 20 }}>
                        <View style={[styles.beautifySelect,
                        this.state.filterLensSelect === index && styles.beautifySelecin
                        ]}>
                        </View>
                        <Text style={[styles.filterLensSelectTitle,
                        this.state.filterLensSelect === index && { color: '#836BFF' }
                        ]}>{item.title}</Text>
                      </View>
                    </TouchableOpacity>
                  </>
                )
              }}
            />
          </View>
          : */}
        <View style={styles.beautifyBoxContent}>
          {[0, 1, 2, 3, 4, 5].map((item, index) => {
            return (
              <TouchableOpacity
                onPress={() => {
                  this.setState({ normalBeautyLevel: item });
                }}
                key={index}
              >
                <View style={[styles.beautifySelect, this.state.normalBeautyLevel === item && styles.beautifySelecin]}>
                  <Text style={styles.beautifySelectTitle}>{item}</Text>
                </View>
              </TouchableOpacity>
            );
          })}
        </View>
        {/* } */}
      </View>
    );
  }

  // 底部渲染
  renderBottom() {
    if (this.state.showBeautify || this.state.showFilterLens) {
      return <View style={{ position: 'relative' }}>{this.renderbeautifyBox()}</View>;
    }
    return (
      <>
        <View style={{ position: 'relative' }}>
          {/* 拍摄按钮 */}
          {
            !this.props.hideControls &&
              // <View style={[styles.bottomButtons]}>
              this.renderCaptureButton()
            // </View>
          }
        </View>
        <View style={{ height: 100, backgroundColor: '#000' }}>{this.renderswitchModule()}</View>
      </>
    );
  }
  // ？？？
  onRatioButtonPressed() {
    const newRatiosArrayPosition = (this.state.ratioArrayPosition + 1) % this.state.ratios.length;
    this.setState({ ratioArrayPosition: newRatiosArrayPosition });
  }

  render() {
    return (
      <>
        <Toast
          ref={this.myRef}
          position='center'
          positionValue={70}
          fadeInDuration={750}
          fadeOutDuration={1000}
          opacity={0.8}
        />
        {/* {Platform.OS !== 'android' ? <View style={{ height: 44, backgroundColor: "#000" }}></View> : null} */}
        {this.state.storyShow ? (
          <>
            {/* story */}
            {this.state.ShootSuccess ? (
              <StoryEditor
                rephotograph={() => {
                  this.setState({ ShootSuccess: false, videoPath: '', imageCaptured: '' });
                }}
                getUploadFile={(data) => {
                  this.sendUploadFile(data);
                }}
                AaImage={this.props.AaImage}
                filterImage={this.props.filterImage}
                musicRevampImage={this.props.musicRevampImage}
                giveUpImage={this.props.giveUpImage}
                noVolumeImage={this.props.noVolumeImage}
                tailorImage={this.props.tailorImage}
                volumeImage={this.props.volumeImage}
                videoPath={this.state.videoPath}
                fileType={this.state.fileType}
                imagePath={this.state.imageCaptured}
                // imagePath ={'/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/Media/1634557132176-photo.jpg'}
              />
            ) : (
              <>
                {Platform.OS === 'android' && this.renderCamera()}
                {Platform.OS !== 'android' && this.renderCamera()}
                {Platform.OS === 'android' && <View style={styles.gap} />}
                {this.renderBottom()}
              </>
            )}
          </>
        ) : (
          <>{/* post */}</>
        )}
      </>
    );
  }
}

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
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
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
        flex: 1,
        flexDirection: 'column',
      },
    }),
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
  },
  snapshotTitle: {
    fontSize: 13,
    lineHeight: 18,
    color: '#FFFFFF',
    marginHorizontal: 30,
    marginRight: 80,
  },
  snapshotMuse: {
    fontSize: 13,
    lineHeight: 18,
    color: '#FFFFFF',
    marginHorizontal: 30,
  },
  switchScreen: {
    position: 'absolute',
    right: 20,
    top: 25,
  },
  musicIcon: {
    width: 28,
    height: 28,
  },
  leftIconBox: {
    position: 'absolute',
    top: 300,
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
    top: 50,
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
