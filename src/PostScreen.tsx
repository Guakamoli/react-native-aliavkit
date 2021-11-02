import React, { Component, useRef, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Image,
  Dimensions,
  Platform,
  SafeAreaView,
  ScrollView,
  FlatList,
  NativeModules,
  NativeEventEmitter,
  StatusBar,
  Modal,
  Pressable,
} from 'react-native';
import _, { lte } from 'lodash';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import { FlatGrid } from 'react-native-super-grid';
import AVService from './AVService.ios';
import ImageCropper from './react-native-simple-image-cropper/src';

const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2;

const photosItem = width / 4;

const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
export type Props = {
  multipleBtnImage: any;
  postCameraImage: any;
  startMultipleBtnImage: any;
  changeSizeImage: any;

  getUploadFile: (any) => void;
  navigation: any;

  captureButtonImage: any;
};

type State = {
  CameraRollList: any;

  fileSelectType: string;
  multipleData: any;
  startmMltiple: boolean;
  scrollViewWidth: boolean;
  photoAlbum: any;
  photoAlbumselect: any;

  videoFile: any;

  fileEditor: Boolean;

  // 2

  trimmerRightHandlePosition: any;
  scrubberPosition: any;

  videoTime: any;

  cropOffset: Array<any>;
  cropOffsetX: any;
  cropOffsetY: any;
  videoPaused: boolean;
};

let subscription = null;
let trimVideoData = null;

let cropData = {};
let cropDataRow = {};

const PostContent = (props) => {
  const [cropScale, setCropScale] = useState(0.9);
  const { multipleData, CameraRollList, fileSelectType, videoFile } = props;
  const imageItem = multipleData.length > 0 ? multipleData[multipleData.length - 1]?.image : CameraRollList[0]?.image;
  const toggleCropWidth = () => {
    if (!cropDataRow.scale || cropDataRow.scale < 1 || cropScale === 0.9) {
      setCropScale(1);
    } else {
      setCropScale(0.9);
    }
  };
  if (videoFile) {
    console.info(imageItem, 'imageItem');
  }
  if (!imageItem && !videoFile) return null;
  return (
    <View
      style={{
        padding: 0,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#ececec',
        position: 'relative',
        height: width,
        width: '100%',
      }}
    >
      {/* 左侧尺寸按钮 */}
      <TouchableOpacity
        style={{
          width: 31,
          height: 31,
          marginRight: 10,
          position: 'absolute',
          left: 15,
          bottom: 20,
          zIndex: 99,
        }}
        onPress={toggleCropWidth}
      >
        <Image
          style={[
            {
              width: 31,
              height: 31,
            },
          ]}
          source={props.changeSizeImage}
        />
      </TouchableOpacity>

      <View
        style={{
          backgroundColor: '#ececec',
          width: '100%',
        }}
      >
        <View style={{ backgroundColor: 'red' }}>
          <ImageCropper
            imageUri={imageItem?.uri}
            videoFile={videoFile}
            videoPaused={props.videoPaused}
            srcSize={{
              width: imageItem.width,
              height: imageItem.height,
            }}
            cropAreaWidth={width}
            cropAreaHeight={width}
            containerColor='black'
            areaColor='black'
            scale={cropScale}
            areaOverlay={<View></View>}
            setCropperParams={(cropperParams) => {
              cropDataRow = cropperParams;
            }}
          />
        </View>
      </View>
    </View>
  );
};
let firstJump = false;
export default class CameraScreen extends Component<Props, State> {
  camera: any;
  myRef: any;
  editor: any;

  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    const Navigation = this.props.navigation;
    props.navigation.setOptions({
      headerTitle: '新作品',

      // statusBarColor: 'transparent',
      headerStyle: {
        backgroundColor: '#000',
      },
      headerTintColor: 'rgba(255,255,255,1)',
      headerTitleStyle: {
        color: '#fff',
        fontSize: 17,
        fontWeight: '500',
        lineHeight: 22,
      },
      headerLeft: () => {
        return (
          <TouchableOpacity
            style={{ marginLeft: 10 }}
            onPress={() => {
              console.info('123');
              props.goback();
            }}
          >
            <Image style={{ width: 30, height: 30 }} source={props.closePng} />
          </TouchableOpacity>
        );
      },
      headerRight: () => {
        return (
          <TouchableOpacity
            onPress={() => {
              this.postEditor();
            }}
          >
            <Text style={{ fontSize: 15, fontWeight: '400', color: '#fff', lineHeight: 21 }}>继续</Text>
          </TouchableOpacity>
        );
      },
    });
    this.cropData = {};
    this.state = {
      CameraRollList: [],
      fileSelectType: '',
      multipleData: [],

      startmMltiple: false,
      photoAlbum: [],
      photoAlbumselect: {},
      videoFile: '',
      scrollViewWidth: true,

      fileEditor: false,

      trimmerRightHandlePosition: 1000,
      videoTime: 60000,
      scrubberPosition: 0,

      cropOffset: [],
      cropOffsetX: 0,
      cropOffsetY: 0,
      videoPaused: false,
      siwtchlibrary: false,
    };
  }

  postEditor = async () => {
    const {
      multipleData,
      fileSelectType,
      cropOffsetX,
      cropOffsetY,

      CameraRollList,
    } = this.state;
    if (multipleData.length < 1) {
      return this.myRef.current.show('请至少选择一个上传文件', 2000);
    }
    try {
      const imageItem =
        multipleData.length > 0 ? multipleData[multipleData.length - 1]?.image : CameraRollList[0]?.image;
      const result = await ImageCropper.crop({
        ...cropDataRow,
        imageUri: imageItem?.uri,
        cropSize: {
          width: width,
          height: width,
        },
        cropAreaSize: {
          width: width,
          height: width,
        },
      });
      cropData = result;
      console.info(multipleData, cropData);
      trimVideoData = await AVService.crop({
        source: `${multipleData[0].image.uri}`,
        cropOffsetX: cropData.offset.x,
        cropOffsetY: cropData.offset.y,
        cropWidth: cropData.size.width,
        cropHeight: cropData.size.height,
      });
      this.myRef.current.show('上传中', 3000);
      // 进入修改
      if (fileSelectType === 'image') {
        console.log('开始裁剪');

        this.sendUploadFile({ trimVideoData, fileType: fileSelectType });
        this.setState({ videoPaused: true });
        console.log('视频暂停拉');

        this.props.goPostEditor({
          trimVideoData,
          fileType: fileSelectType,
          palyVide: () => {
            this.setState({ videoPaused: false });
          },
        });
      }
    } catch (e) {
      console.info(e, '错误');
    }
  };

  componentDidMount() {
    // const { fileSelectType} =  this.state
    const managerEmitter = new NativeEventEmitter(AliAVServiceBridge);
    subscription = managerEmitter.addListener('cropProgress', (reminder) => {
      console.log(reminder);

      if (reminder.progress == 1 && this.state.fileSelectType === 'video' && !this.state.videoPaused) {
        this.setState({ videoPaused: true });

        let trimmerRightHandlePosition = this.state.trimmerRightHandlePosition;
        let videoTime = this.state.videoTime;
        firstJump = true;
        console.log('跳转了啊');

        this.props.goPostEditor({
          trimVideoData,
          videoduration: videoTime,
          trimmerRight: trimmerRightHandlePosition,
          fileType: this.state.fileSelectType,
          palyVide: () => {
            this.setState({ videoPaused: false });
          },
        });
      }
      //
    });

    //获取照片
    let getPhotos = CameraRoll.getPhotos({
      first: 100,
      assetType: 'All',
      //todo  安卓调试隐藏
      include: ['playableDuration', 'filename', 'fileSize', 'imageSize'],
      // groupTypes: 'Library'
    });
    // var getAlbums = CameraRoll.getAlbums({
    //   assetType: 'All',
    // });
    // let getAlbums = CameraRoll.getAllLibraryPhotos({
    //   include: ['playableDuration', 'filename', 'fileSize', 'imageSize']
    // });

    // getAlbums.then((data) => {
    //   console.log('获取相册封面', data);
    //   let photoAlbumData = []
    //   // 获取相册
    //   data.map(async (item) => {
    //     const cover = await CameraRoll.getPhotos({ first: 1, assetType: 'All', groupName: `${item.title}` });
    //     console.log('获取相册封面', item);
    //     photoAlbumData.push({ title: item.title, count: item.count, coverImage: cover.edges[0].node })
    //   });

    //   // 相册数据
    //   this.setState({ photoAlbum: photoAlbumData });
    // });

    getPhotos.then(
      async (data) => {
        var edges = data.edges;
        var photos = [];
        for (var i in edges) {
          // ios文件
          photos.push(edges[i].node);
        }
        console.log(photos[0]);
        let firstData = photos[0];
        this.setState({
          CameraRollList: photos,
          multipleData: [firstData],
          fileSelectType: firstData?.type,
          trimmerRightHandlePosition:
            Math.ceil(firstData.image.playableDuration) < 300
              ? Math.ceil(firstData.image.playableDuration) * 1000
              : 300 * 1000,

          videoTime: Math.ceil(firstData.image.playableDuration) * 1000,
          videoFile: '',
        });
      },
      function (err) {
        // alert( '获取照片失败！' );
      },
    );
  }
  componentWillUnmount() {
    console.log('销毁');
    // 结束编辑页面
    RNEditViewManager.stop();
    this.setState = () => false;
  }
  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }

  postFileUploadHead() {
    const { startmMltiple, multipleData } = this.state;

    return (
      <View
        style={{
          height: 58,
          backgroundColor: '#000',
          flexDirection: 'row',
          justifyContent: 'space-between',
          alignItems: 'center',
          paddingHorizontal: 12,
        }}
      >
        {/* <TouchableOpacity onPress={() => { this.setState({ siwtchlibrary: true }) }}> */}
        <TouchableOpacity onPress={() => { }}>
          <View>
            <Text style={{ fontSize: 17, fontWeight: '500', color: '#fff', lineHeight: 24 }}>最近相册</Text>
          </View>
        </TouchableOpacity>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
          {/* 多选按钮 */}
          <TouchableOpacity
            onPress={() => {
              if (startmMltiple && multipleData.length) {
                // 取最后一张
                this.setState({ multipleData: [multipleData[multipleData.length - 1]] });
              }
              this.setState({ startmMltiple: !startmMltiple });
              // 暂时单张图㲏上传
              // this.setState({ startmMltiple: false });
            }}
          >
            <Image
              style={[styles.multipleBtnImage, { marginRight: 10 }]}
              source={startmMltiple ? this.props.startMultipleBtnImage : this.props.multipleBtnImage}
              resizeMode='contain'
            />
          </TouchableOpacity>
          <Image style={styles.multipleBtnImage} source={this.props.postCameraImage} resizeMode='contain' />
        </View>
      </View>
    );
  }
  postFileUpload() {
    const { CameraRollList, multipleData, startmMltiple, fileSelectType } = this.state;

    const formatSeconds = (s) => {
      let t = '';
      if (s > -1) {
        let min = Math.floor(s / 60) % 60;
        let sec = s % 60;
        if (min < 10) {
          t += '0';
        }
        t += min + ':';
        if (sec < 10) {
          t += '0';
        }
        t += sec;
      }
      return t;
    };
    const getVideFile = async (fileType, item) => {
      if (fileType !== 'video') return '';
      let myAssetId = item?.image?.uri.slice(5);
      // 获取视频文件 url
      // console.log(myAssetId, 'myAssetId');

      let localUri = await CameraRoll.requestPhotoAccess(myAssetId);

      this.setState({ videoFile: localUri });
    };

    return (
      <>
        <Toast
          ref={this.myRef}
          position='top'
          positionValue={300}
          fadeInDuration={1050}
          fadeOutDuration={800}
          opacity={0.8}
        />
        {this.postFileUploadHead()}
        <StatusBar barStyle={'light-content'} />
        <View
          style={[
            { height: height * 0.4, backgroundColor: '#000' },
            Platform.OS === 'android' ? { paddingBottom: 10 } : { paddingBottom: 35 },
          ]}
        >
          <FlatGrid
            itemDimension={photosItem}
            data={CameraRollList}
            spacing={0}
            itemContainerStyle={{ margin: 0 }}
            renderItem={({ index, item }) => {
              const { type, image } = item;
              const { fileSelectType, startmMltiple } = this.state;
              // const a =timestamp
              return (
                <TouchableOpacity
                  onPress={async () => {
                    //  第一次
                    cropDataRow = {};

                    if (multipleData.length <= 1) {
                      // 获取第一次选择类型
                      let fileType = type.split('/')[0];
                      if (fileType === 'video') {
                        // ios 转码视频地址
                        getVideFile(fileType, item);
                        // 视频设置编辑器  总数 右边值 同步
                        this.setState({
                          trimmerRightHandlePosition:
                            Math.ceil(item.image.playableDuration) < 300
                              ? Math.ceil(item.image.playableDuration) * 1000
                              : 300 * 1000,

                          videoTime: Math.ceil(item.image.playableDuration) * 1000,
                        });
                      }

                      console.log('----playableDuration', Math.ceil(item.image.playableDuration));
                      this.setState({
                        fileSelectType: fileType,
                        multipleData: [item],
                        videoFile: '',
                      });
                    }
                    // 暂时屏蔽
                    // if (startmMltiple) {
                    //   // 图片大于10 || 视频 大于 1
                    //   if (fileSelectType == 'image') {
                    //     if (multipleData.length == 1) {
                    //       this.myRef.current.show('最多十张图片', 2000);
                    //       return;
                    //     }
                    //   }
                    //   if (fileSelectType == 'video') {
                    //     if ((multipleData.length = 1)) {
                    //       this.myRef.current.show('最多选择一个视频', 2000);
                    //       return;
                    //     }
                    //   }
                    //   let datalist = multipleData;
                    //   // 已经选择了
                    //   if (datalist.includes(item)) {
                    //     // 循环找到选中的 去掉
                    //     datalist.map((datalistitem, index) => {
                    //       if (datalistitem.image.uri == image.uri) {
                    //         datalist.splice(index, 1);
                    //       }
                    //     });
                    //   } else {
                    //     datalist.push(item);
                    //   }
                    //   this.setState({
                    //     multipleData: datalist,
                    //     videoFile: '',
                    //   });
                    // }
                  }}
                  // disabled={!(type.indexOf(fileSelectType) !== -1) && startmMltiple}
                  activeOpacity={0.9}
                >
                  <View
                    style={[
                      {
                        position: 'relative',
                      },
                    ]}
                  >
                    {startmMltiple ? (
                      <>
                        <View
                          style={{
                            borderRadius: 10,
                            borderWidth: 2,
                            width: 20,
                            height: 20,
                            borderColor: 'white',
                            overflow: 'hidden',
                            position: 'absolute',
                            zIndex: 99,
                            right: 5,
                            top: 5,
                          }}
                        >
                          {multipleData.includes(item) ? (
                            <View
                              style={{
                                width: 18,
                                height: 18,
                                borderRadius: 20,
                                zIndex: 99,
                                backgroundColor: '#836BFF',
                                justifyContent: 'center',
                                alignItems: 'center',
                              }}
                            >
                              {/* 暂时屏蔽 */}
                              {/* <Text style={{ color: '#fff', fontSize: 13, fontWeight: '400' }}>
                                {multipleData.indexOf(item) !== -1 ? multipleData.indexOf(item) + 1 : 1}
                              </Text> */}
                            </View>
                          ) : null}
                        </View>
                      </>
                    ) : null}
                    <Image
                      key={index}
                      style={[
                        {
                          width: photosItem,
                          height: photosItem,
                        },
                        // 暂时屏蔽
                        // !(type.indexOf(fileSelectType) !== -1) && startmMltiple ? { opacity: 0.4 } : {},
                      ]}
                      // 安卓展示不出来 权限问题？？？？
                      source={{ uri: item.image.uri }}
                      resizeMode='cover'
                    />
                    <View
                      style={[
                        {
                          width: photosItem,
                          height: photosItem,
                          position: 'absolute',
                          backgroundColor: '#fff',
                        },
                        multipleData[multipleData.length - 1]?.image.uri === image.uri
                          ? { opacity: 0.5 }
                          : { opacity: 0 },
                      ]}
                    ></View>
                    {/* 时长 */}
                    {image.playableDuration ? (
                      <Text
                        style={{
                          color: '#fff',
                          fontSize: 12,
                          fontWeight: '400',
                          lineHeight: 17,
                          zIndex: 100,
                          position: 'absolute',
                          right: 8,
                          bottom: 7,
                        }}
                      >
                        {' '}
                        {formatSeconds(Math.ceil(image.playableDuration ?? 0))}
                      </Text>
                    ) : null}
                  </View>
                </TouchableOpacity>
              );
            }}
          />
        </View>
        <View
          style={{
            backgroundColor: '#222222',
            borderRadius: 22,
            zIndex: 1,
            width: 120,
            height: 43,
            position: 'absolute',
            right: width * 0.3,
            bottom: 40,
            flexDirection: 'row',
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          <Text style={{ color: '#fff', fontSize: 14, marginRight: 10 }}> 作品</Text>
          <TouchableOpacity
            onPress={() => {
              this.props.goStory();
            }}
          >
            <Text style={{ color: '#8d8d8c', fontSize: 13 }}> 快拍</Text>
          </TouchableOpacity>
        </View>
      </>
    );
  }

  render() {
    return (
      <>
        {/* 相册内容切换 暂时屏蔽 */}
        {/* <Modal
            animationType="slide"
            transparent={false}
            visible={this.state.siwtchlibrary}
            style={styles.modal}
          >
            <View style={{ flex: 1, backgroundColor: '#000', paddingTop: 20 }}>
              <View style={{ width: width, height: height * 0.1, backgroundColor: '#000', flexDirection: "row", alignItems: 'flex-end', justifyContent: 'center', position: 'relative', paddingBottom: 20, }} >
                <TouchableOpacity onPress={() => { this.setState({ siwtchlibrary: false }) }} style={{ position: 'absolute', left: 0, bottom: 20, left: 10, }}>
                  <Text style={{ color: '#fff', fontSize: 16, }} >取消</Text>
                </TouchableOpacity>
                <Text style={{ color: '#fff', fontSize: 16 }} >选择相册</Text>
              </View>
              <FlatList
                data={this.state.photoAlbum}
                // data={[{ a: 1 }, { a: 2 }, { a: 3 }]}
                renderItem={({ item, index }) => {
                  console.log('12313', item);

                  return (
                    <TouchableOpacity onPress={() => { this.setState({ photoAlbumselect: item }) }}>
                      <View style={{ width: width, height: 100, marginBottom: 10, flexDirection: 'row', alignItems: 'center', padding: 10 }}>
                        <Image source={{ uri: item.coverImage.image.uri }} style={{ width: 93, height: 93, backgroundColor: 'red' }} />
                        <View style={{ marginLeft: 10, justifyContent: 'space-around' }}>
                          <Text style={{ color: '#fff', fontSize: 16, marginBottom: 10 }}>{item.title}</Text>
                          <Text style={{ color: '#fff', fontSize: 12 }}>{item.count}</Text>
                        </View>
                      </View>
                    </TouchableOpacity>
                  )
                }}
              >

              </FlatList>

            </View>
          </Modal> */}

        <PostContent
          {...this.props}
          videoPaused={this.state.videoPaused}
          multipleData={this.state.multipleData}
          CameraRollList={this.state.CameraRollList}
          fileSelectType={this.state.fileSelectType}
          videoFile={this.state.videoFile}
        />
        {this.postFileUpload()}
      </>
    );
  }
}

const styles = StyleSheet.create({
  closeIcon: {
    width: 28,
    height: 28,
  },

  captureButton: {
    width: 49,
    height: 49,
    backgroundColor: '#fff',
    borderRadius: 49,
    position: 'absolute',
  },
  captureButtonImage: {
    position: 'absolute',
    left: captureIcon,
    zIndex: -11,
    elevation: 1,
    top: -7,
  },
  multipleBtnImage: {
    width: 31,
    height: 31,
  },
  postSwitchProps: {
    fontSize: 16,
    color: '#8E8E8E',
    fontWeight: '500',
  },
  shadow: {
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,

    elevation: 5,
  },
});
