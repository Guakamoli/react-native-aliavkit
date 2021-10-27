// import PropTypes from 'prop-types';
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
  NativeModules,
} from 'react-native';
import _ from 'lodash';
import Camera from './Camera';
import VideoEditor from './VideoEditor';
import Carousel from 'react-native-snap-carousel';
// import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast'
import StoryMusic from './StoryMusic';

const { width, height } = Dimensions.get('window');
const CameraHeight = (height)
const { RNEditViewManager } = NativeModules;
export enum CameraType {
  Front = 'front',
  Back = 'back',
}

export type Props = {
  AaImage: any
  filterImage: any
  musicRevampImage: any
  giveUpImage: any
  noVolumeImage: any
  tailorImage: any
  volumeImage: any
  rephotograph: () => void;

  getUploadFile: (any) => void;

  goback: any
  // 视频路径
  videoPath: any,
  imagePath: any,
  fileType: any
  musicDynamicGif: any
  musicIconPng: any
}

type State = {
  // 照片数据
  captureImages: any[],
  cameraType: CameraType,
  showBeautify: boolean,



  mute: boolean,
  showFilterLens: boolean,



  // 滤镜名称
  filterName: any
  filterList: Array<any>
  // 导出
  startExportVideo: Boolean

  musicOpen: Boolean
  musicInfo: any
}


export default class StoryEditor extends Component<Props, State> {
  camera: any;
  myRef: any
  editor: any
  constructor(props) {
    console.info('story 编辑页面props', props);

    super(props);
    this.myRef = React.createRef();
    this.state = {
      // 照片存储
      captureImages: [],
      cameraType: CameraType.Front,
      showBeautify: false,

      // 是否静音
      mute: false,

      showFilterLens: false,
      // 视频 照片地址
      filterName: null,
      filterList: [],

      startExportVideo: false,
      musicOpen: false,
      musicInfo: {}
    };
    this.onExportVideo = this.onExportVideo.bind(this);
  }
  startExportVideo() {
    console.log(this.state.startExportVideo);
    if (this.state.startExportVideo) {
      return;
    }
    this.setState({ startExportVideo: true });
  }

  //  发布快拍   导出视频  丢出数据
  onExportVideo(event) {
    console.log('1231', event);
    const { fileType } = this.props;
    if (event.exportProgress === 1) {
      let outputPath = event.outputPath;
      this.setState({ startExportVideo: false, outputPath: event.outputPath });
      // console.log('视频导出成功, path = ', event.outputPath);
      let uploadFile = [];
      // 
      let type = outputPath.split('.')
      uploadFile.push({
        Type: `video/${type[type.length - 1]}`,
        path: fileType == 'video' ? `file://${encodeURI(outputPath)}` : outputPath,
        size: 0,
        Name: outputPath
      })

      this.sendUploadFile(uploadFile)

    }
  }
  getFilters = async () => {
    //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}
    if (this.state.filterList.length < 1) {
      if (Platform.OS === 'android') {
        const filterList = await this.editor.getColorFilterList();
        // console.log('filterList111', filterList);
        this.setState({ filterList: filterList })
      } else {
        const infos = await RNEditViewManager.getFilterIcons({});
        console.log('getFilters', infos);
        infos.unshift({ filterName: null, iconPath: '', title: "无效果" })
        console.log('getFilters', infos);
        this.setState({ filterList: infos })
      }
    }
  }
  componentDidMount() {
    this.getFilters()

  }
  componentWillUnmount() {
    if (Platform.OS === 'android') {
      // console.log(Platform.OS === 'android');
      //  this.camera.release();
    } else {
      RNEditViewManager.stop()
    }
    // 结束编辑页面
    console.log('拍摄编辑销毁');
    this.setState = () => false;
  }

  // 底部 切换模块
  renderUploadStory() {
    const { captureImages } = this.state

    return (
      <View style={styles.BottomBox}>
        <>
          {/*  作品快拍 切换*/}

          {/* 发布 */}

          <TouchableOpacity onPress={() => {
            this.startExportVideo()
          }}>
            <View style={styles.uploadBox}>
              <Text style={styles.uploadTitle}>发布快拍</Text>
            </View>
          </TouchableOpacity>
        </>
      </View>
    );
  }


  // 编辑头部按钮
  renderUpdateTop() {
    // console.log(this.props.fileType, 'this.props.fileType');

    const imglist = [
      // 'filter': 
      { 'img': this.props.filterImage, 'onPress': () => { this.setState({ showFilterLens: !this.state.showFilterLens }) } },
      // 'volume':
      { 'img': this.state.mute ? this.props.noVolumeImage : this.props.volumeImage, 'onPress': () => { this.setState({ mute: !this.state.mute }) }, },
      // 'music':
      {
        'img': this.props.fileType == 'video' ? this.props.videomusicIcon : this.props.musicRevampImage, 'onPress': () => {
          if (this.props.fileType == 'video') {

            this.setState({ musicOpen: !this.state.musicOpen })
          }
        },
      },
      // 'Aa': 
      { 'img': this.props.AaImage, 'onPress': () => { } }
    ]
    return (
      <>
        {/* 放弃 */}
        <TouchableOpacity onPress={() => {
          this.setState({ showFilterLens: false, filterName: null, captureImages: [] })
          this.props.rephotograph()
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
            imglist.map((item, index) => {
              return (
                <TouchableOpacity onPress={item.onPress} key={index}>
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

  // 拍摄进度
  _onRecordingDuration(event) {
    // console.log('duration: ', event.duration);
  }

  // 拍摄内容渲染
  renderCamera() {
    console.info('拍摄内容渲染', this.props.videoPath, 'imagePath', this.props.imagePath);

    const VideoEditors = () => {
      return (
        <View style={{ height: '100%', backgroundColor: '#fff', borderRadius: 20 }}>
          <VideoEditor
            ref={(edit) => (this.editor = edit)}
            style={{ height: CameraHeight, justifyContent: 'flex-end' }}
            filterName={this.state.filterName}
            videoPath={this.props.videoPath}
            imagePath={this.props.imagePath}

            saveToPhotoLibrary={true}
            startExportVideo={this.state.startExportVideo}
            onExportVideo={this.onExportVideo}
            videoMute={this.state.mute}
            musicInfo={this.state.musicInfo ? this.state.musicInfo : {}}
          />
        </View>
      )
    }
    return (
      <View style={[styles.cameraContainer]}>
        <TouchableOpacity style={[Platform.OS != 'android' && { flex: 1, justifyContent: 'flex-end', }, {}]}
          onPress={() => {
            this.setState({ showFilterLens: false, })
            // !this.state.showFilterLens 
          }}
          activeOpacity={1}
          disabled={this.state.showBeautify}
        >
          {VideoEditors()}
          {this.renderUpdateTop()}
        </TouchableOpacity>

      </View>
    );
  }

  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }
  // 美颜 滤镜 box
  renderFilterBox() {
    return (
      <View style={{ height: 189, backgroundColor: "#000" }}>
        <View style={styles.beautifyBoxHead}>
          <Text style={styles.beautifyTitle}>{`滤镜`}</Text>

        </View>
        {this.state.showFilterLens
          &&
          <View style={{ paddingHorizontal: 20 }}>
            <FlatList
              data={this.state.filterList}
              horizontal={true}
              style={{ margin: 0, padding: 0, height: 80 }}
              renderItem={({ index, item }) => {
                return (
                  <>
                    <TouchableOpacity onPress={() => {
                      this.setState({ filterName: item.filterName })
                    }}>
                      <View style={{ flexDirection: 'column', justifyContent: 'center', alignItems: 'center', marginRight: 20 }}>
                        <Image style={[styles.beautifySelect,
                        this.state.filterName == item.filterName && styles.beautifySelecin
                        ]}
                          source={{ uri: item.iconPath }}
                        />
                        <Text style={[styles.filterLensSelectTitle,
                        this.state.filterName == item.filterName && { color: '#836BFF' }
                        ]}>{item.filterName ? item.filterName : item.title}</Text>
                      </View>
                    </TouchableOpacity>
                  </>
                )
              }}
            />
          </View>
        }


      </View >
    )
  }

  // 底部渲染
  renderBottom() {
    if (this.state.showFilterLens) {
      return (
        this.renderFilterBox()
      )
    }
    return (
      <>
        <View style={{ height: 125, backgroundColor: "#000", justifyContent: 'center', alignContent: 'center' }}>
          {this.renderUploadStory()}
        </View>
      </>
    )
  }


  render() {
    // console.log('getmusicInfo-----', this.state.musicInfo);
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
        {Platform.OS === 'android' && this.renderCamera()}
        {Platform.OS !== 'android' && this.renderCamera()}
        {Platform.OS === 'android' && <View style={styles.gap} />}

        <View style={{ position: 'absolute', bottom: 0, width: width }}>

          {this.state.musicOpen
            ?
            <StoryMusic
              musicDynamicGif={this.props.musicDynamicGif}
              musicIconPng={this.props.musicIconPng}
              musicSearch={this.props.musicSearch}
              musicIcongray={this.props.musicIcongray}
              getmusicInfo={(data) => {


                this.setState({ musicInfo: data })
              }} />
            :
            this.renderBottom()}



        </View>

      </>
    );
  }
}

const styles = StyleSheet.create(
  {
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
          // height:400,
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

    beautifySelecin: {
      borderWidth: 2,
      borderColor: "#FFF",
      backgroundColor: "rgba(69, 69, 73, 0.7)",
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
      top: 40,
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
  });

