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
} from 'react-native';
import _ from 'lodash';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import { FlatGrid } from 'react-native-super-grid';
import Video from 'react-native-video';
import Carousel from 'react-native-snap-carousel';
// import Trimmer from 'react-native-trimmer';
import VideoEditor from './VideoEditor';
import AVService from './AVService.ios';
// import Trimmer from './Trimer';
import ImageCropper from './react-native-simple-image-cropper/src';

const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2;

const photosItem = width / 4;

const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
export type Props = {
  ratioOverlay?: string;
  closeImage: any;
  goback: any;

  multipleBtnImage: any;
  postCameraImage: any;
  startMultipleBtnImage: any;
  changeSizeImage: any;
  addPhotoBtnPng: any;
  postMutePng: any;
  postNoMutePng: any;
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
  // pasterList: any
  videoFile: any;
  // facePasterInfo: any
  // filterName:any
  fileEditor: Boolean;
  // 滤镜
  // filterName:string
  // filterList:Array<any>
  // aa:Boolean
  // videoMute:Boolean
  // selectBottomModel:string

  // 2
  trimmerLeftHandlePosition: any;
  trimmerRightHandlePosition: any;
  scrubberPosition: any;

  videoTime: any;

  cropOffset: Array<any>;
  cropOffsetX: any;
  cropOffsetY: any;

  multipleSandBoxData: any;

  // 封面数据
  // coverList:any
  // coverImage:any
};

const scrubInterval = 50;
let subscription = null;
let trimVideoData = null;
let coverData = [];
let cropData = {};
let cropDataRow = {};
// const navigation = useNavigation();
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
export default class CameraScreen extends Component<Props, State> {
  camera: any;
  myRef: any;
  editor: any;

  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    console.log('----', props);
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
      headerRight: () => {
        return (
          <TouchableOpacity onPress={() => {
            this.postEditor()
          }} >
            <Text style={{ fontSize: 15, fontWeight: '400', color: "#fff", lineHeight: 21 }}>继续</Text>
          </TouchableOpacity>
        )
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
      //
      // pasterList: [],
      // facePasterInfo: {},
      // filterName:"原片"

      // 修改页面
      fileEditor: false,
      // 滤镜
      // filterName: '',
      // filterList:[],
      // aa:true,
      // videoMute:false,
      // selectBottomModel:'滤镜',

      //22
      trimmerLeftHandlePosition: 0,
      trimmerRightHandlePosition: 1000,
      videoTime: 60000,
      scrubberPosition: 0,

      cropOffset: [],
      cropOffsetX: 0,
      cropOffsetY: 0,
      multipleSandBoxData: '',
      coverList: [],
      coverImage: '',
    };
  }

  postEditor = async () => {
    const {
      fileEditor,
      multipleData,
      fileSelectType,
      cropOffsetX,
      cropOffsetY,
      multipleSandBoxData,
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

      console.log('trimVideoData', trimVideoData, 'fileSelectType', fileSelectType);

      this.myRef.current.show('请稍后', DURATION.FOREVER);
      // 进入修改
      if (fileSelectType === 'image') {
        console.log('开始裁剪');

        // this.myRef.current.close();
        this.sendUploadFile({ trimVideoData, fileType: fileSelectType });
        this.props.goPostEditor({ trimVideoData, fileType: fileSelectType })
        // this.props.navigation.push('PostEditorBox', );
      }
    } catch (e) {
      console.info(e, '错误');
    }
  };
  // getFilters  = async() => {
  //   //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}
  //   if(this.state.filterList.length < 1){
  //     const infos = await RNEditViewManager.getFilterIcons({});
  //     // console.log('------infos',infos);

  //     this.setState({filterList:infos})
  //   }
  // }
  componentDidMount() {
    // const { fileSelectType} =  this.state
    const managerEmitter = new NativeEventEmitter(AliAVServiceBridge);
    subscription = managerEmitter.addListener('cropProgress', (reminder) => {
      console.log(reminder);

      if (reminder.progress == 1 && this.state.fileSelectType === 'video') {

        let trimmerRightHandlePosition = this.state.trimmerRightHandlePosition;
        let videoTime = this.state.videoTime;
        this.props.goPostEditor({
          trimVideoData,
          videoduration: videoTime,
          trimmerRight: trimmerRightHandlePosition,
          fileType: this.state.fileSelectType,
        })

        // this.props.getUploadFile();
        // this.setState({fileEditor:true,multipleSandBoxData:[trimVideoData]})
      }
      //
    });

    //获取照片
    var getPhotos = CameraRoll.getPhotos({
      first: 100,
      assetType: 'All',
      //todo  安卓调试隐藏
      include: ['playableDuration', 'filename', 'fileSize', 'imageSize'],
      // groupTypes: 'Library'
    });
    var getAlbums = CameraRoll.getAlbums({
      assetType: 'All',
    });
    console.log(233);
    getAlbums.then((data) => {
      // 获取相册封面
      data.map(async (item) => {
        const cover = await CameraRoll.getPhotos({ first: 1, assetType: 'Photos', groupName: `${item.title}` });
        // 通过相册 名称获取
        data.map((item2) => {
          if (item2.title == cover.edges[0].node.group_name) {
            item2.cover = cover.edges[0].node.image.uri;
          }
        });
      });
      // 相册数据
      this.setState({ photoAlbum: data, photoAlbumselect: data[0] });
    });

    getPhotos.then(
      async (data) => {
        console.log(123);

        var edges = data.edges;
        console.log('andor', data);

        var photos = [];
        for (var i in edges) {
          // ios文件
          photos.push(edges[i].node);
        }
        // console.log('-------',photos);

        this.setState({
          CameraRollList: photos,
          videoFile: '',
        });
      },
      function (err) {
        // alert( '获取照片失败！' );
      },
    );
    // 滤镜
    // this.getFilters()
  }
  componentWillUnmount() {
    // subscription.remove();
    console.log('销毁');
    // 结束编辑页面
    RNEditViewManager.stop();
    this.setState = () => false;
  }
  sendUploadFile(data) {
    // console.info('11111', this.props.getUploadFile);
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }
  postHead() {
    const { fileEditor, multipleData, fileSelectType, cropOffsetX, cropOffsetY, multipleSandBoxData } = this.state;
    return (
      <View
        style={{
          height: 44,
          backgroundColor: '#000',
          flexDirection: 'row',
          justifyContent: 'space-between',
          alignItems: 'center',
          paddingHorizontal: 12,
        }}
      >
        <TouchableOpacity
          onPress={async () => {


            this.props.goback();
          }}
        >
          <Image style={styles.closeIcon} source={this.props.closePng} resizeMode='contain' />
        </TouchableOpacity>
        {fileEditor ? (
          fileSelectType === 'video' &&
          // <TouchableOpacity onPress={()=>{this.setState({videoMute : !videoMute})}}>
          {
            /* <Image  style={{width:18,height:17}} source={ !videoMute ? this.props.postMutePng : this.props.postNoMutePng}/> */
          }
        ) : (
          // </TouchableOpacity>

          <Text style={{ fontSize: 17, fontWeight: '500', color: '#fff', lineHeight: 24 }}>新作品</Text>
        )}

        <TouchableOpacity
          onPress={async () => {
            this.postEditor();
          }}
        >
          <Text style={{ fontSize: 15, fontWeight: '400', color: '#fff', lineHeight: 21 }}>继续</Text>
        </TouchableOpacity>
      </View>
    );
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
        <TouchableOpacity onPress={() => { }}>
          <View>
            <Text style={{ fontSize: 17, fontWeight: '500', color: '#fff', lineHeight: 24 }}>最近相册</Text>
          </View>
        </TouchableOpacity>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
          {/* 多选按钮 */}
          {/* <TouchableOpacity
            onPress={() => {
              if (startmMltiple && multipleData.length) {
                // 取最后一张
                this.setState({ multipleData: [multipleData[multipleData.length - 1]] });
              }
              this.setState({ startmMltiple: !startmMltiple, })
              // 暂时单张图㲏上传
              // this.setState({ startmMltiple: false });
            }}
          >
            <Image
              style={[styles.multipleBtnImage, { marginRight: 10 }]}
              source={startmMltiple ? this.props.startMultipleBtnImage : this.props.multipleBtnImage}
              resizeMode='contain'
            />
          </TouchableOpacity> */}
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
            { height: 291, backgroundColor: '#000' },
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
                      // let list = []
                      // datalist.map(async(item)=>{

                      //  let sandData =  await AVService.saveToSandBox({path:`${item.image.uri}`})
                      // list.push(sandData)
                      // console.log('123131',sandData);

                      // multipleSandBoxData
                      // this.setState({})
                      // } )
                      // this.setState({multipleSandBoxData:[sandData]})
                      console.log('----playableDuration', Math.ceil(item.image.playableDuration));
                      this.setState({
                        fileSelectType: fileType,
                        multipleData: [item],
                        videoFile: '',
                      });
                    }
                    if (startmMltiple) {
                      // 图片大于10 || 视频 大于 1
                      if (fileSelectType == 'image') {
                        if (multipleData.length == 10) {
                          this.myRef.current.show('最多十张图片', 2000);
                          return;
                        }
                      }
                      if (fileSelectType == 'video') {
                        if ((multipleData.length = 1)) {
                          this.myRef.current.show('最多选择一个视频', 2000);
                          return;
                        }
                      }
                      let datalist = multipleData;
                      // 已经选择了
                      if (datalist.includes(item)) {
                        // 循环找到选中的 去掉
                        datalist.map((datalistitem, index) => {
                          if (datalistitem.image.uri == image.uri) {
                            datalist.splice(index, 1);
                          }
                        });
                      } else {
                        datalist.push(item);
                      }
                      this.setState({
                        multipleData: datalist,
                        videoFile: '',
                      });
                    }
                  }}
                  disabled={!(type.indexOf(fileSelectType) !== -1) && startmMltiple}
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
                              <Text style={{ color: '#fff', fontSize: 13, fontWeight: '400' }}>
                                {multipleData.indexOf(item) !== -1 ? multipleData.indexOf(item) + 1 : 1}
                              </Text>
                            </View>
                          ) : null}
                        </View>
                        {/* < Image source={this.props.captureButtonImage} style={[{ width: 20, height: 20, position: 'absolute', right: 5, top: 5, zIndex: 98 }]} />
                          {
                            multipleData.includes(item) ? <View style={[
                              { },
                            ]}>
                             
                            </View> : null
                          } */}
                      </>
                    ) : null}
                    <Image
                      key={index}
                      style={[
                        {
                          width: photosItem,
                          height: photosItem,
                        },
                        !(type.indexOf(fileSelectType) !== -1) && startmMltiple ? { opacity: 0.4 } : {},
                      ]}
                      // 安卓展示不出来 权限问题？？？？
                      source={{ uri: item.image.uri }}
                      // source={require('../example/images/11.png')}
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
      </>
    );
  }


  render() {
    const { fileEditor } = this.state;
    const { selectBottomModel, fileSelectType } = this.state;
    return (
      <ScrollView showsVerticalScrollIndicator={false} style={{ backgroundColor: 'black' }}>
        <>
          <PostContent
            {...this.props}
            multipleData={this.state.multipleData}
            CameraRollList={this.state.CameraRollList}
            fileSelectType={this.state.fileSelectType}
            videoFile={this.state.videoFile}
          />
          {this.postFileUpload()}
        </>
      </ScrollView>
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
