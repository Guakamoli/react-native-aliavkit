import PropTypes from 'prop-types';
import React, { Component, useRef } from 'react';
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
  NativeAppEventEmitter
} from 'react-native';
import _ from 'lodash';
import Camera from './Camera';
import Carousel from 'react-native-snap-carousel';
import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast'


const FLASH_MODE_AUTO = 'auto';
const FLASH_MODE_ON = 'on';
const FLASH_MODE_OFF = 'off';

const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2
const captureIcon2 = (width - 20) / 2
export enum CameraType {
  Front = 'front',
  Back = 'back'
}

export type Props = {
  ratioOverlay?: string,
  ratioOverlayColor?: string,
  allowCaptureRetake: boolean,
  cameraRatioOverlay: any,
  showCapturedImageCount?: boolean,
  captureButtonImage: any,
  cameraFlipImage: any,
  hideControls: any,
  showFrame: any,
  scanBarcode: any,
  laserColor: any,
  frameColor: any,
  torchOnImage: any,
  torchOffImage: any,
  closeImage: any,
  musicImage: any,
  beautifyImage: any,
  beautyAdjustImag: any,
  AaImage: any
  filterImage: any
  musicRevampImage: any
  giveUpImage: any
  noVolumeImage: any
  tailorImage: any
  volumeImage: any
  onReadCode: (any) => void;
  onBottomButtonPressed: (any) => void;
  isstory: boolean
  ispost: boolean
}

type State = {
  // 照片数据
  captureImages: any[],
  flashData: any,
  torchMode: boolean,
  ratios: any[],
  ratioArrayPosition: number,
  imageCaptured: any,
  captured: boolean,
  cameraType: CameraType,
  // ---
  videoRecording: boolean;

  currentIndex: number,
  showBeautify: boolean,
  normalBeautyLevel: number,

  progress: number,
  startShoot: boolean,
  ShootSuccess: boolean,
  mute: boolean,
  showFilterLens: boolean,
  filterLensSelect: number,
  timer: any,
  fadeInOpacity: any,


  //
  progressListen: any,
  // 视频路径
  videoPath: any,
  // 区分story/post 
  storyShow: boolean,
  postShow: boolean,
}


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
  myRef: any

  constructor(props) {
    super(props);
    this.myRef = React.createRef();
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
      focusMode: true,
      videoRecording: false,
      ratios: [],
      ratioArrayPosition: -1,
      imageCaptured: false,
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

      //进度条监听
      progressListen: null,
      // 视频 照片地址
      videoPath: null,
      //
      storyShow: false,
      postShow: false,
    };
  }

  componentDidMount() {
    let ratios = [];
    if (this.props.cameraRatioOverlay) {
      ratios = this.props.cameraRatioOverlay.ratios || [];
    }

    const { isstory, ispost } = this.props
    this.setState({
      ratios: ratios || [],
      ratioArrayPosition: ratios.length > 0 ? 0 : -1,
      storyShow: isstory,
      postShow: ispost,
    });

  }
  // ？？？？ 
  isCaptureRetakeMode() {
    return !!(this.props.allowCaptureRetake && !_.isUndefined(this.state.imageCaptured));
  }
  // 闪光灯拍摄    ？？？？
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
  // 闪光灯
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

  // 底部 切换模块
  renderswitchModule() {
    return (
      <SafeAreaView style={styles.BottomBox}>
        <>
          {/*  作品快拍 切换*/}
          {this.state.startShoot || this.state.ShootSuccess ? null :
            <View style={{ flexDirection: 'row', justifyContent: "center", }}>
              <TouchableOpacity onPress={() => this.setState({ storyShow: false, postShow: true })}>
                <Text style={styles.videoTitle}>作品</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={() => this.setState({ storyShow: true, postShow: false })}>
                <Text style={styles.snapshotTitle}>快拍</Text>
              </TouchableOpacity>
            </View>}
          {/* 发布 */}
          {this.state.ShootSuccess ?
            <TouchableOpacity onPress={() => { }}>
              <View style={styles.uploadBox}>
                <Text style={styles.uploadTitle}>发布快拍</Text>
              </View>
            </TouchableOpacity>
            : null}
          {/* 相机翻转 */}
          {this.state.ShootSuccess ? null :
            <TouchableOpacity style={styles.switchScreen} onPress={() => this.onSwitchCameraPressed()}>
              <Image
                style={{ width: 26, height: 23 }}
                source={this.props.cameraFlipImage}
                resizeMode="contain"
              />
            </TouchableOpacity>
          }
        </>
      </SafeAreaView>
    );
  }

  // 左侧 三个icon
  renderLeftButtons() {
    return (
      <>
        {/* 取消 */}
        <TouchableOpacity onPress={() => {

          this.myRef.current.show('1231312', 2000);
        }} style={styles.closeBox}>
          <Image
            style={styles.closeIcon}
            source={this.props.closeImage}
            resizeMode="contain"
          />
        </TouchableOpacity>
        <View style={styles.leftIconBox}>
          {/* 音乐 */}
          <TouchableOpacity onPress={() => { }} >
            <Image
              style={styles.musicIcon}
              source={this.props.musicImage}
              resizeMode="contain"
            />
          </TouchableOpacity>
          {/* 美颜 */}
          <TouchableOpacity onPress={() => { this.setState({ showBeautify: !this.state.showBeautify }) }} >
            <Image
              style={styles.beautifyIcon}
              source={this.props.beautifyImage}
              resizeMode="contain"
            />
          </TouchableOpacity>
        </View>
      </>
    )
  }
  // 编辑头部按钮
  renderUpdateTop() {
    const imglist = [
      // 'filter': 
      { 'img': this.props.filterImage, 'onPress': () => { this.setState({ showFilterLens: !this.state.showFilterLens }) } },
      // 'volume':
      { 'img': this.state.mute ? this.props.noVolumeImage : this.props.volumeImage, 'onPress': () => { this.setState({ mute: !this.state.mute }) }, },
      // 'tailor': 
      { 'img': this.props.tailorImage, 'onPress': () => { } },
      // 'git':
      { 'img': this.props.musicRevampImage, 'onPress': () => { } },
      // 'Aa': 
      { 'img': this.props.AaImage, 'onPress': () => { } }
    ]
    return (
      <>
        {/* 放弃 */}
        <TouchableOpacity onPress={() => {
          this.setState({ ShootSuccess: false, showFilterLens: false, filterLensSelect: 0, progress: 0 })

        }} style={[styles.UpdateBox, { left: 20 }]}>
          <Image
            style={styles.updateTopIcon}
            source={this.props.giveUpImage}
            resizeMode="contain"
          />
        </TouchableOpacity>
        {/* 编辑按钮组 */}
        <View style={[styles.UpdateBox, { right: 10, flexDirection: 'row' }]}>
          {
            imglist.map(item => {
              return (
                <TouchableOpacity onPress={item.onPress} >
                  <Image
                    style={styles.updateTopIcon}
                    source={item.img}
                    resizeMode="contain"
                  />
                </TouchableOpacity>
              )
            })
          }
        </View>
      </>
    )
  }

  // 拍摄内容渲染
  renderCamera() {
    return (
      <View style={[styles.cameraContainer]}>
        {this.isCaptureRetakeMode() ? (
          <Image style={{ flex: 1, justifyContent: 'flex-end' }} source={{ uri: this.state.imageCaptured.uri }} />
        ) : (
          <TouchableOpacity style={{ flex: 1, justifyContent: 'flex-end', position: "relative", borderRadius: 20 }}
            onPress={() => {
              this.setState({ showFilterLens: false, showBeautify: false })
            }}
            activeOpacity={1}
            disabled={!this.state.showBeautify}
          >
            {this.state.ShootSuccess ? this.renderUpdateTop() : this.renderLeftButtons()}
            <Camera
              ref={(cam) => (this.camera = cam)}
              style={{ flex: 1, borderRadius: 20, }}
              cameraType={this.state.cameraType}
              flashMode={this.state.flashData.mode}
              torchMode={this.state.torchMode ? 'on' : 'off'}
              focusMode={this.props.focusMode}
              zoomMode={this.props.zoomMode}
              ratioOverlay={this.state.ratios[this.state.ratioArrayPosition]}
              saveToCameraRoll={!this.props.allowCaptureRetake}
              showFrame={this.props.showFrame}
              scanBarcode={this.props.scanBarcode}
              laserColor={this.props.laserColor}
              frameColor={this.props.frameColor}
              onReadCode={this.props.onReadCode}
              normalBeautyLevel={this.state.normalBeautyLevel * 10}
            />

          </TouchableOpacity>
        )
        }
      </View>
    );
  }
  // 照片数量
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

  // 进度条
  animate() {
    let progress = 0;
    this.setState({ progress: 0 });
    const stopRecording = async () => {
      const videoPath = await this.camera.stopRecording();
      console.log('video saved to ', videoPath);
      this.setState({ videoPath })
    }
    this.setState({
      timer: setInterval(() => {
        progress += 1 / 14;
        console.log('进度条');
        if (progress > 1) {
          progress = 1;
          this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) })
          stopRecording()
          clearInterval(this.state.timer)
        }
        this.setState({ progress, });
      }, 1000)
    })

  }
  //  拍摄按钮
  renderCaptureButton() {
    const { fadeInOpacity, ShootSuccess } = this.state
    if (ShootSuccess) {
      return null;
    }
    return (
      this.props.captureButtonImage &&
      !this.isCaptureRetakeMode() && (
        <View style={[styles.captureButtonContainer]}>
          {
            <>
              {/* 长按按钮 */}
              <View style={[styles.startShootBox, this.state.startShoot ? {} : { opacity: 0 }]} >
                <Animated.View style={[styles.startShootAnnulus, { width: fadeInOpacity, height: fadeInOpacity }]}>
                </Animated.View>
                <View style={styles.captureButton}></View>
                <Progress.Circle
                  style={[styles.progress, { position: 'absolute' }]}
                  progress={this.state.progress}
                  indeterminate={false}
                  size={122}
                  color={"#EA3600"}
                  borderWidth={0}
                  thickness={6}
                />
              </View>
              {/* 普通的切换按钮 */}
              <View style={!this.state.startShoot ? {} : { opacity: 0 }}>
                {this.switchProp()}
              </View>
            </>
          }
        </View >
      )
    );
  }

  // 切换道具
  switchProp() {
    let img = { width: 64, height: 64, borderRadius: 64 }
    return (
      <View style={{ position: "relative" }} >
        <Carousel
          data={[1, 2, 3, 4, 5, 6, 7]}
          itemWidth={83}
          sliderWidth={width}
          firstItem={this.state.currentIndex}
          onBeforeSnapToItem={slideIndex => {
            this.setState({
              currentIndex: slideIndex
            })
          }}

          renderItem={({ index, item }) => {
            if (this.state.currentIndex === index) {
              img = { width: 64, height: 64, borderRadius: 64 }
            } else {
              img = { width: 52, height: 52, borderRadius: 52 }
            }
            return (

              <TouchableOpacity
                style={{ width: 64, height: 64, borderRadius: 64, }}
                delayLongPress={500}
                disabled={!(this.state.currentIndex === index)}
                // 长按
                onLongPress={async () => {
                  console.log('onLongPress');
                  clearInterval(this.state.timer)
                  // 按钮动画
                  Animated.timing(                        // 随时间变化而执行动画
                    this.state.fadeInOpacity,             // 动画中的变量值
                    {
                      toValue: 122,                       // 透明度最终变为1，即完全不透明
                      duration: 500,                       // 让动画持续一段时间
                    }
                  ).start();
                  const success = await this.camera.startRecording();
                  this.setState({ startShoot: success })
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
                    this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) })
                    const videoPath = await this.camera.stopRecording();
                    console.log('video saved to ', videoPath);
                    this.setState({ videoPath })
                    setTimeout(() => {
                      if (this.state.timer != null) {
                        clearInterval(this.state.timer);
                      }
                    }, 500);
                  }

                }}
                // 单击
                onPress={() => {
                  console.log('onPress');

                  const { startShoot, progress } = this.state
                  // console.log('1313');
                  if (!startShoot || progress === 0) {
                    // 拍照
                    this.onCaptureImagePressed()
                    this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) })
                  }
                }}
              >
                <View style={{ position: 'relative' }}>
                  <View style={[{ backgroundColor: "green", },
                    img
                  ]}></View>
                </View>
              </TouchableOpacity>
            )
          }}
        />


        {/* 临时方案  安卓 拍摄不会触发 */}
        <TouchableOpacity
          style={styles.captureButtonImage}
          delayLongPress={500}
          disabled={Platform.OS !== 'android'}
          // 长按
          onLongPress={async () => {
            console.log('onLongPress');
            clearInterval(this.state.timer)
            // 按钮动画
            Animated.timing(                        // 随时间变化而执行动画
              this.state.fadeInOpacity,             // 动画中的变量值
              {
                toValue: 122,                       // 透明度最终变为1，即完全不透明
                duration: 500,                       // 让动画持续一段时间
              }
            ).start();
            console.log('--------this.camera', this.camera);
            const success = await this.camera.startRecording();
            this.setState({ startShoot: success })
            // let success = true
            console.log('success', success);
            console.log(13132);

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
              this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) })
              const videoPath = await this.camera.stopRecording();
              console.log('video saved to ', videoPath);
              this.setState({ videoPath })
              setTimeout(() => {
                if (this.state.timer != null) {
                  clearInterval(this.state.timer);
                }
              }, 500);
            }

          }}
          // 单击
          onPress={() => {
            console.log('onPress');
            const { startShoot, progress } = this.state
            if (!startShoot || progress === 0) {
              // 拍照
              this.onCaptureImagePressed()
              this.setState({ startShoot: false, ShootSuccess: true, fadeInOpacity: new Animated.Value(60) })
            }
          }}
        >
          < Image source={this.props.captureButtonImage} />
        </TouchableOpacity>
      </View >
    )
  }


  // ？？？
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
  //  弹出照片数据 ???
  sendBottomButtonPressedAction(type, captureRetakeMode, image) {
    if (this.props.onBottomButtonPressed) {
      this.props.onBottomButtonPressed({ type, captureImages: this.state.captureImages, captureRetakeMode, image });
    }
  }
  // cancel 按钮点击 ???
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
  // ???? 
  renderBottomButton(type) {
    const showButton = true;
    if (showButton) {
      const buttonNameSuffix = this.isCaptureRetakeMode() ? 'CaptureRetakeButtonText' : 'ButtonText';
      const buttonText = _(this.props).get(`actions.${type}${buttonNameSuffix}`);
      return (
        <TouchableOpacity
          style={[styles.bottomButton, { justifyContent: type === 'left' ? 'flex-start' : 'flex-end' }]}
          onPress={() => this.onButtonPressed(type)}
        >
          <Text style={styles.textStyle}>{buttonText}</Text>
        </TouchableOpacity>
      );
    } else {
      return <View style={styles.bottomContainerGap} />;
    }
  }


  // 相机切换
  onSwitchCameraPressed() {
    console.log('13131');

    const direction = this.state.cameraType === CameraType.Back ? CameraType.Front : CameraType.Back;
    this.setState({ cameraType: direction });
  }
  //闪光灯拍摄 事件
  onSetFlash() {
    this.currentFlashArrayPosition = (this.currentFlashArrayPosition + 1) % 3;
    const newFlashData = this.flashArray[this.currentFlashArrayPosition];
    this.setState({ flashData: newFlashData });
  }
  // 闪光 事件
  onSetTorch() {
    this.setState({ torchMode: !this.state.torchMode });
  }
  // 拍照功能
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
  // 美颜 滤镜 box
  renderbeautifyBox() {
    const list = [
      { title: "123" },
      { title: "123" },
      { title: "123" },
      { title: "123" },
      { title: "123" },
      { title: "123" },
      { title: "123" },
      { title: "123" },
      { title: "123" },
    ]
    return (
      <View style={{ height: 189, backgroundColor: "#000" }}>
        <View style={styles.beautifyBoxHead}>
          <Text style={styles.beautifyTitle}>{this.state.showFilterLens ? `滤镜` : `美颜`}</Text>
          {!this.state.showFilterLens &&
            <Image
              style={styles.beautyAdjustIcon}
              source={this.props.beautyAdjustImag}
              resizeMode="contain"
            />
          }
        </View>
        {this.state.showFilterLens
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
                      this.setState({ filterLensSelect: index })
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
          :
          <View style={styles.beautifyBoxContent}>
            {[0, 1, 2, 3, 4, 5].map(item => {
              return (
                <TouchableOpacity onPress={() => {
                  this.setState({ normalBeautyLevel: item })
                }}
                >
                  <View style={[
                    styles.beautifySelect,
                    this.state.normalBeautyLevel === item && styles.beautifySelecin
                  ]}>
                    <Text style={styles.beautifySelectTitle}>{item}</Text>
                  </View>
                </TouchableOpacity>
              )
            })}
          </View>
        }

      </View >
    )
  }
  // 拍摄
  async onRecordVideoPressed() {
    console.log('12313123');
    console.log(this.state.videoRecording);

    if (this.state.videoRecording) {
      console.log('stopRecording');
      this.setState({ videoRecording: false });
      const videoPath = await this.camera.stopRecording();
      console.log('video saved to ', videoPath);
    } else {
      console.log('startRecording');

      const success = await this.camera.startRecording();
      console.log('---- success: ', success);
      this.setState({ videoRecording: true });
    }
  }
  // 底部切换 和捕获
  renderSwitchCapture() {
    return (
      <>

      </>
    )
  }
  // 底部渲染
  renderBottom() {
    if (this.state.showBeautify || this.state.showFilterLens) {
      return (
        this.renderbeautifyBox()
      )
    }
    return (
      <>
        <View style={{ position: 'relative', }}>
          {/* 拍摄按钮 */}
          {!this.props.hideControls && (
            // <View style={[styles.bottomButtons]}>
            this.renderCaptureButton()
            // </View>
          )}
        </View>
        <View style={{ height: 100, backgroundColor: "#000", }}>
          {this.renderswitchModule()}
        </View>
      </>
    )
  }
  // ？？？
  onRatioButtonPressed() {
    const newRatiosArrayPosition = (this.state.ratioArrayPosition + 1) % this.state.ratios.length;
    this.setState({ ratioArrayPosition: newRatiosArrayPosition });
  }
  // postHead() {
  //   return(
  //     <View>

  //       </View>
  //   )
  // }
  render() {
    return (
      <>
        <Toast
          ref={this.myRef}
          position="center"
          positionValue={70}
          fadeInDuration={750}
          fadeOutDuration={1000}
          opacity={0.8}
        />
        {/* story */}
        <View style={{ height: 44, backgroundColor: "red" }}></View>
        {
          this.state.storyShow && (
            <>
              {Platform.OS === 'android' && this.renderCamera()}
              {Platform.OS !== 'android' && this.renderCamera()}
              {Platform.OS === 'android' && <View style={styles.gap} />}
              {this.renderBottom()}
            </>
          )
        }
        {/* post */}
        {
          this.state.postShow && (
            // <>
            //   {this.postHead()}
            // </>

            null
          )
        }

      </>
    );
  }
}

const styles = StyleSheet.create(
  {
    bottomButtons: {
      flex: 1
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
      alignItems: "center",
      position: 'relative'
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
      marginRight: 80
    },
    switchScreen: {
      position: "absolute",
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
      zIndex: 99
    },
    beautifyIcon: {
      width: 28,
      height: 28,
      marginTop: 30
    },
    closeBox: {
      position: 'absolute',
      top: 17,
      left: 15,
      zIndex: 99
    },
    closeIcon: {
      width: 28,
      height: 28,
    },
    beautifyBoxHead: {
      paddingHorizontal: 20,
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: 'center',
      paddingTop: 15,
      paddingBottom: 26,
    },
    beautifyTitle: {
      fontSize: 15,
      fontWeight: '500',
      color: "#fff",
      lineHeight: 21
    },
    beautyAdjustIcon: {
      width: 20,
      height: 16
    },
    beautifyBoxContent: {
      flexDirection: "row",
      justifyContent: "space-around",
    },
    beautifySelect: {
      width: 48,
      height: 48,
      backgroundColor: " rgba(69, 69, 73, 0.7)",
      borderRadius: 9,
      justifyContent: 'center',
      alignItems: 'center',

    },
    beautifySelectTitle: {
      fontSize: 20,
      fontWeight: "500",
      color: "#fff",
      lineHeight: 28
    },
    beautifySelecin: {
      borderWidth: 2,
      borderColor: "#836BFF"
    },
    progress: {
      margin: 10,
    },

    uploadBox: {
      width: 130,
      height: 40,
      borderRadius: 22,
      backgroundColor: "#fff",
      justifyContent: 'center',
      alignItems: 'center'
    },
    uploadTitle: {
      fontWeight: '500',
      fontSize: 13,
      color: "#000",
      lineHeight: 18
    },
    UpdateBox: {
      position: 'absolute',
      zIndex: 99,
      top: 20,
    },
    updateTopIcon: {
      width: 40,
      height: 40,
      marginRight: 10
    },
    filterLensSelectTitle: {
      fontSize: 13,
      fontWeight: "500",
      color: '#fff',
      lineHeight: 18,
    },
    startShootAnnulus: {
      backgroundColor: "rgba(255,255,255,0.5)",
      borderRadius: 122,
      position: 'absolute'
    },
    captureButton: {
      width: 49,
      height: 49,
      backgroundColor: "#fff",
      borderRadius: 49,
      position: 'absolute'
    },
    captureButtonContainer: {
      justifyContent: 'center',
      alignItems: 'center',
      flexDirection: "row",
      position: 'absolute',
      bottom: 30,

    },
    captureButtonImage: {
      position: 'absolute',
      left: captureIcon,
      zIndex: -11,
      elevation: 1,
      top: - 7,
    },
    slider: {
      flexDirection: 'row',
      justifyContent: 'center',
      alignItems: 'center',
      display: "flex",
      zIndex: 99,
      elevation: 10,
    },
    startShootBox: {
      justifyContent: 'center',
      alignItems: 'center',
      position: 'relative',
      left: captureIcon2,
    },
  });

