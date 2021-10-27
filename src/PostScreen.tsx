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
  ScrollView,
  FlatList,
  NativeModules,
  NativeEventEmitter,
  Button,
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
// const navigation = useNavigation();
export default class PostUpload extends Component<Props, State> {
  camera: any;
  myRef: any;
  editor: any;

  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    console.log('----', props);
    const Navigation = this.props.navigation;
    // 1231
    // props.refs.current = {
    //   empty: () => {
    //     this.postEditor()
    //   }
    // }
    // props.navigation.setOptions({
    //   headerTitle: '新作品',
    //   // headerRight: () => <Button title='play' onPress={() =>  }  />,

    //   headerRight: () => <Button title='play' onPress={() => this.postEditor()} />,
    //   //

    //   //   headerTitle: 'Camera',
    //   //   headerRight: () => {
    //   //     return(
    //   //       <TouchableOpacity onPress={() => navigation.navigate('PostEditorBox')} > <Text style={{ fontSize: 15, fontWeight: '400', color: "#fff", lineHeight: 21 }}>继续</Text> </TouchableOpacity>
    //   //     )
    //   //   },
    //   //   headerLeft: () =>  <Image
    //   //   style={{
    //   //     width: 28,
    //   //     height: 28,
    //   //   }}
    //   //   source={{uri:require('../images/close.png')}}
    //   //   resizeMode="contain"
    //   // />,
    // });
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
    console.log(2342);
    const { fileEditor, multipleData, fileSelectType, cropOffsetX, cropOffsetY, multipleSandBoxData } = this.state;

    if (multipleData.length < 1) {
      return this.myRef.current.show('请至少选择一个上传文件', 2000);
    }
    // console.log(fileEditor);
    // console.log('this.state.multipleSandBoxData[0],',this.state.multipleSandBoxData[0],);
    // console.log('multipleData[0].image.uri',multipleData[0].image.uri);

    // 编辑完成  导出数据  剪辑
    // if(fileEditor){
    //   console.log('----编辑完成  导出数据  剪辑');

    //     const result = await RNEditViewManager.trimVideo({
    //     videoPath: this.state.multipleSandBoxData[0],
    //     startTime: 2.0,
    //     endTime: 3.0,
    //   });
    //   console.log('-----result',result);

    //   //  发送选择的数据
    // let uplaodFile = []
    // console.log('this.state.multipleData', this.state.multipleData);
    // let uploadFile = [result];
    // //
    //   // let type = outputPath.split('.')
    //   // uploadFile.push({
    //   //   Type : `${fileType}/${type[type.length - 1]}`,
    //   //   path :   fileType == 'video' ?  `file://${encodeURI(outputPath)}` : outputPath,
    //   //   size : 0,
    //   //   Name:outputPath
    //   // })

    // }

    // 裁剪
    // trimVideoData = await AVService.crop({ source:`${multipleData[0].image.uri}` , cropOffsetX:100, cropOffsetY:100, cropWidth:800, cropHeight:800 });
    console.log('裁剪数据', multipleData);

    trimVideoData = await AVService.crop({
      source: `${multipleData[0].image.uri}`,
      cropOffsetX,
      cropOffsetY,
      cropWidth: multipleData[0].image.width,
      cropHeight: multipleData[0].image.width,
    });
    // await AVService.crop({});
    this.myRef.current.show('请稍后', DURATION.FOREVER);

    console.log('trimVideoData', trimVideoData, 'fileSelectType', fileSelectType);

    // 进入修改
    if (fileSelectType === 'image') {
      console.log('开始裁剪');
      // trimVideoData = await AVService.crop({ source: `${multipleData[0].image.uri}`, cropOffsetX, cropOffsetY, cropWidth: multipleData[0].image.width, cropHeight: multipleData[0].image.width, });
      // let a = multipleData.map(async (item, index) => {
      //   console.log('开始裁剪1');
      //   console.log('12313', item);

      //   return
      //   console.log(index, '------', trimVideoData);

      // })

      // console.log(a);

      // this.setState({fileEditor:true,multipleSandBoxData:[trimVideoData]})
      // this.sendUploadFile(trimVideoData)
      this.myRef.current.close();
      this.sendUploadFile({ trimVideoData, fileType: fileSelectType })
      this.props.navigation.push('PostEditorBox', { trimVideoData, fileType: fileSelectType });
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
      // console.log( this.state.fileSelectType);

      if (reminder.progress == 1 && this.state.fileSelectType === 'video') {
        // console.log('---data',trimVideoData);
        // this.state.trimmerRightHandlePosition
        this.myRef.current.close();

        let trimmerRightHandlePosition = this.state.trimmerRightHandlePosition;
        let videoTime = this.state.videoTime;
        this.sendUploadFile({
          trimVideoData,
          videoduration: videoTime,
          trimmerRight: trimmerRightHandlePosition,
          fileType: this.state.fileSelectType,
        })
        this.props.navigation.push('PostEditorBox', {
          trimVideoData,
          videoduration: videoTime,
          trimmerRight: trimmerRightHandlePosition,
          fileType: this.state.fileSelectType,
        });

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
        // edges.map( async(item)=>{

        //   let localUri = await CameraRoll.requestPhotoAccess(item.node.image.uri.slice(5));
        //   item.node.image.uri = localUri ;
        //   // console.log(localUri);

        //   photos.push(item.node);
        //   console.log('ios',photos);
        // })
        for (var i in edges) {
          // ios文件
          photos.push(edges[i].node);
        }
        // console.log('-------',photos);

        this.setState({
          CameraRollList: photos,
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
    console.info('11111', this.props.getUploadFile)
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
            // await RNEditViewManager.stop()
            // await AVService.removeThumbnaiImages()
            // console.log('stopandf removeThumbnaiImages ');
            // coverData = []
            // 取消按钮

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
  postContent() {
    const { multipleData, CameraRollList, fileSelectType, videoFile } = this.state;
    // 计算移动距离  通过宽
    return (
      <View
        style={{
          flex: 1,
          padding: 0,
          justifyContent: 'center',
          alignItems: 'center',
          backgroundColor: '#ececec',
          position: 'relative',
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
          onPress={() => {
            this.setState({ scrollViewWidth: !this.state.scrollViewWidth });
          }}
        >
          <Image
            style={[
              {
                width: 31,
                height: 31,
              },
            ]}
            source={this.props.changeSizeImage}
          />
        </TouchableOpacity>

        <ScrollView
          style={{
            height: 'auto',
            margin: 'auto',
            paddingHorizontal: 0,
            backgroundColor: '#ececec',
            width: this.state.scrollViewWidth ? width : width - 90,
          }}
          pinchGestureEnabled={true}
          onScroll={(event) => {
            {
              // console.log('multipleData',multipleData[0]?.image);

              console.log(event.nativeEvent.contentOffset.x); //水平滚动距离
              console.log(event.nativeEvent.contentOffset.y); //垂直滚动距离

              // 414    0   325
              // 1080        1920
              // 计算有问题  ------------
              this.setState({
                cropOffsetX: event.nativeEvent.contentOffset.x * (multipleData[0]?.image.width / width),
                cropOffsetY: event.nativeEvent.contentOffset.y * (multipleData[0]?.image.width / width),
              });
            }
          }}
        >
          {fileSelectType === 'image' ? (
            <Image
              style={[
                {
                  width: this.state.scrollViewWidth ? width : width - 90,
                  height: height,
                  // width: this.state.scrollViewWidth ? width : 320
                },
              ]}
              // 安卓展示不出来 权限问题？？？？
              // source={{ uri: item.image.uri }}
              source={{
                uri:
                  multipleData.length > 0
                    ? multipleData[multipleData.length - 1]?.image?.uri
                    : CameraRollList[0]?.image?.uri,
              }}
              resizeMode={'cover'}
            />
          ) : (
            <Video
              source={{ uri: videoFile }}
              style={{
                width: width,
                height: height - 160,
              }}
            />
          )}
        </ScrollView>
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
              // this.setState({ startmMltiple: !startmMltiple, })
              // 暂时单张图㲏上传
              this.setState({ startmMltiple: false });
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

  // postEditorViewData(){
  //   const {multipleData,multipleSandBoxData}  = this.state
  //   // console.log('1231',multipleData);
  //   // console.log('data.length',multipleData[0].length);
  //   // const onlyOne = multipleSandBoxData.length === 1

  //   // if(onlyOne){
  //     console.log('11111111111postEditorViewData',multipleSandBoxData);
  //     if(!this.state.fileEditor){
  //       console.log('销毁postEditorViewData');

  //       return null

  //     }
  //    return (
  //     <View style={[{height:width,width:width, }]}>
  //       <VideoEditor
  //       ref={(edit) => (this.editor = edit)}
  //       style={{height:111,width:411,}}
  //       filterName={this.state.filterName}
  //       // videoPath={"/var/mobile/Containers/Data/Application/9DBC4AB0-799C-4BD4-ADF1-E1F8339AF173/Documents/com.guakamoli.engine/composition/4AAF423D-3D69-4020-A695-37A855D5E460.mp4"}
  //       // imagePath={multipleSandBoxData[0]}
  //       videoPath={multipleSandBoxData[0]}
  //       saveToPhotoLibrary={false}
  //       startExportVideo={false}
  //       // videoMute={this.state.videoMute}
  //       // onExportVideo={this.onExportVideo}
  //     />

  //     </View>
  //    )
  // }
  // 多图 展示
  // return(
  //   <View style={{height:375,paddingHorizontal:0}}>

  //   <Carousel
  //   data={multipleSandBoxData}
  //   // containerCustomStyle={{backgroundColor:'green',}}
  //   itemWidth={320}
  //   sliderWidth={width}
  //   enableSnap={this.state.aa}
  //   onBeforeSnapToItem={(slideIndex = 0) => {
  //     console.log('slideIndex',slideIndex,'multipleSandBoxData.length',multipleSandBoxData.length-1);

  //     if(slideIndex === multipleSandBoxData.length -1){
  //       console.log('aa:false');
  //     this.setState({aa:false})
  //     }else{
  //       console.log('aa:true');
  //       this.setState({aa:true})
  //     }
  //   }}
  //   ListFooterComponent={()=>{
  //     if(onlyOne) null
  //     return (
  //       <View style={{width:83,height:319,flexDirection:'row',alignItems:'center',marginTop:40}}>

  //       <TouchableOpacity  style={{}} onPress={()=>{
  //         this.setState({fileEditor:false,startmMltiple:true})
  //       }}>
  //          <Image
  //        style={[{
  //          width: 83,
  //          height: 83,
  //          marginHorizontal:34
  //        }]}
  //        source={this.props.addPhotoBtnPng}
  //      />
  //       </TouchableOpacity>
  //       </View>
  //     )
  //   }}
  //   renderItem={({index,item})=>{
  //     console.log('123',item);
  //     // const finall = (index == multipleSandBoxData.length -1 && multipleSandBoxData.length > 1)

  //     return (
  //       <>
  //       {/* <View style={{flexDirection:'row',alignItems:'center'}}> */}
  //       <View style={[{height:319,width:319,marginTop:40},
  //         // ,finall && {width:214}

  //         ]}>
  //         {/* <Image   source ={{uri:item}} style={{width:'100%',height:'100%'}} / > */}

  //         <VideoEditor
  //     ref={(edit) => (this.editor = edit)}
  //     style={{width:'100%',height:'100%' }}
  //     filterName={this.state.filterName}
  //     // videoPath={item.image.uri}
  //     imagePath={item}
  //     saveToPhotoLibrary={false}
  //     // startExportVideo={this.state.startExportVideo}
  //     // videoMute={this.state.mute}
  //     // onExportVideo={this.onExportVideo}
  //   />
  //         </View>
  //        {/* {finall &&

  //        } */}
  //        {/* </View> */}
  //       </>
  //     )
  //   }}
  //   />

  // </View>
  // )
  // }
  // 滤镜组件
  // filterEditorFilter(){
  //   return (
  //     <View style={{marginTop:101,paddingHorizontal:17}}>
  //        <FlatList
  //         data={this.state.filterList}
  //         initialNumToRender={4}
  //         horizontal={true}
  //         renderItem={({ index, item })=>{
  //           return(
  //             <View  style={{height:130}}>

  //           <TouchableOpacity onPress={() => {
  //                     this.setState({ filterName:item.filterName})
  //                   }}>
  //             <Image style={{width:100,height:100,backgroundColor:'green',marginRight:4}}   source={{uri:item.iconPath}} />
  //             </TouchableOpacity>
  //               </View>
  //           )
  //         }}
  //       />
  //     </View>
  //   )
  // }

  // 裁剪
  //   postTrimer(){
  //     const {
  //       trimmerLeftHandlePosition,
  //       trimmerRightHandlePosition,
  //       scrubberPosition,
  //       videoTime
  //     } = this.state;

  //       console.log();
  //       let  scrubberInterval = null
  //     // return null
  //     const  playScrubber = () => {
  //     // this.setState({ playing: true });

  //     scrubberInterval = setInterval(() => {
  //       this.setState({ scrubberPosition: this.state.scrubberPosition + 1 })
  //     }, 1000)
  //   }

  //   const  pauseScrubber = () => {
  //     clearInterval(scrubberInterval)

  //     this.setState({ scrubberPosition: this.state.trimmerLeftHandlePosition });
  //   }
  // //   const onStopPlay = async () => {
  // //     audioRecorderPlayer.stopPlayer();
  // //     audioRecorderPlayer.removePlayBackListener();
  // //     setPlayType(0);
  // // };

  //   //  await RNEditViewManager.pause()

  //     //  await RNEditViewManager.replay()
  //     //  await
  //     //  await RNEditViewManager.resume()
  //   const  onHandleChange = async  ( { leftPosition, rightPosition }) => {
  //     console.log('12222',leftPosition, rightPosition );

  //     if (leftPosition < 0) {
  //       leftPosition = 0;
  //   }
  //   if (rightPosition < 2000) {
  //       rightPosition = 2000;
  //   }

  //     // console.log('-----222',result);
  //     // RNEditViewManager.seekToTime(1.23)
  //     if(leftPosition <= 100 ){

  //       RNEditViewManager.seekToTime(leftPosition / 1000)
  //     }
  //    setTimeout(() => {
  //     RNEditViewManager.play()
  //    }, 1000);
  //     this.setState({
  //       trimmerRightHandlePosition: rightPosition,
  //       trimmerLeftHandlePosition: leftPosition
  //     })
  //   }

  //   const onScrubbingComplete = (newValue) => {
  //     this.setState({  scrubberPosition: newValue })
  //   }
  //   // 播放视频

  //   // const { ,trimmerLeftHandlePosition,trimmerRightHandlePosition} = this.state
  //     return (
  //       <>
  //       <View style={{position:'absolute',bottom:200}}>
  //       <TouchableOpacity onPress={async()=>{
  //           // await RNEditViewManager.play()
  //       // playScrubber()
  //       }}>
  //         <Text style={{fontSize:20,color:"red"}}>播放</Text>
  //         </TouchableOpacity>
  //         <TouchableOpacity onPress={async()=>{
  //            await RNEditViewManager.pause()
  //           //  pauseScrubber()
  //            }}>
  //         <Text style={{fontSize:20,color:"red",marginTop:20}}>暂停</Text>
  //         </TouchableOpacity>
  //         </View>
  //     <View style={{marginTop:160,paddingHorizontal:20}}>

  //     <Trimmer
  //             // style={styles.trimViewContainer}
  //             // 修改回调
  //             onHandleChange={onHandleChange}
  //             // 总时间
  //             totalDuration={videoTime}
  //             // totalDuration={50000}
  //             // 渲染等级  以60000 为1 级别
  //             initialZoomValue={1}
  //             // 裁剪最大
  //             maxTrimDuration={trimmerRightHandlePosition}

  //             trimmerLeftHandlePosition={trimmerLeftHandlePosition}
  //             trimmerRightHandlePosition={trimmerRightHandlePosition}

  //             scrubberPosition={trimmerLeftHandlePosition}
  //           //   minimumTrimDuration={minimumTrimDuration}
  //               // maxTrimDuration={60000}
  //             // 滑块覆盖层颜色
  //             // tintColor={'#651FFF'}
  //             // 刻度颜色
  //             // markerColor={'#651FFF'}
  //             // 底部内容条颜色
  //             // trackBackgroundColor={'#fff'}
  //             // 底部内容条边框颜色
  //             // trackBorderColor={'#fff'}
  //             // scrubberColor={'#fff'}
  //             // maxTrimDuration={1000}
  //             // 播放完成
  //             onScrubbingComplete={()=>{console.log(123)} }
  //             tintColor="#333"
  //           markerColor="#5a3d5c"
  //           trackBackgroundColor="#382039"
  //           trackBorderColor="#5a3d5c"
  //           scrubberColor="#b7e778"
  //           onScrubberPressIn={()=>{console.log('onScrubberPressIn')}}
  //           onRightHandlePressIn={()=>{    RNEditViewManager.pause()}}
  //           onLeftHandlePressIn={()=>{    RNEditViewManager.pause()}}
  //         />
  //     </View>
  //     </>
  //     )
  //   }
  // 封面
  // postCover(){
  //   const {coverList,videoTime} = this.state;
  //   // console.log('-----coverList',coverList);
  //   console.log('---this.state.videoPath',this.state.multipleSandBoxData);
  //   console.log('this.state.videoTime---',this.state.videoTime);

  //   const  getcoverData = async() =>{
  //     coverData = await RNEditViewManager.generateImages({
  //         videoPath:this.state.multipleSandBoxData[0],
  //         duration: videoTime > 7 ? 7 : videoTime,
  //         startTime: 0,
  //         itemPerTime: 1000,
  //       });
  //       // console.log('=====coverList:',coverList);

  //       console.log('------',coverData);
  //       this.setState({coverList:coverData})
  //   }
  //   if( coverData && coverData.length < 1){
  //     getcoverData()
  //   }
  //   return(
  //     <View style={{marginTop:93,paddingHorizontal:17}}>
  //     <FlatList
  //         data={coverList}
  //         initialNumToRender={7}
  //         horizontal={true}
  //         renderItem={({ index, item })=>{
  //           return(
  //             <View  style={{height:130}}>
  //               {/* 封面选择 */}
  //             <TouchableOpacity  onPress={()=>{
  //               this.setState({coverImage:item})
  //             }}>
  //               <Image source={{ uri: item }} style={{width:65,height:74,backgroundColor:'green',marginRight:2}}  />
  //             </TouchableOpacity>
  //             </View>
  //           )
  //         }}
  //       />
  //       </View>
  //   )
  // }
  // // 切换底部功能
  // switchProps(){
  //   const switchProps =['滤镜','修剪','封面'];
  //   const {selectBottomModel} = this.state;
  //   return(
  //     <View style={{height:60, width:width,flexDirection:"row",justifyContent:'space-evenly',alignItems:'flex-start',position:"absolute",bottom:0}}>
  //       {switchProps.map((item,index) => {
  //             return (
  //               <TouchableOpacity key={index} onPress={() => {
  //                 this.setState({ selectBottomModel: item })
  //               }}
  //               >
  //                 <Text style={[styles.postSwitchProps,selectBottomModel === item && {color:"#fff"} ]}>{item}</Text>
  //               </TouchableOpacity>
  //             )
  //           })}

  //     </View>
  //   )
  // }

  // photoEditorContent(){

  //   return (

  //   )
  // }
  render() {
    const { fileEditor } = this.state;
    const { selectBottomModel, fileSelectType } = this.state;
    return (
      <>
        {/* <Trimmer /> */}
        {/* post */}
        {this.postHead()}
        {/* {  this.postEditorViewData()  }
                {fileEditor ? 
                <>
                <View style={{flex:1,backgroundColor:'#000',position:'relative'}}>
              {
                 selectBottomModel  === '滤镜' && this.filterEditorFilter()
               }
               {
                 selectBottomModel  === '修剪' && this.postTrimer()
               }
               {
                 selectBottomModel  === '封面' && this.postCover()
               }
               
               {
                 fileSelectType  != 'image' && this.switchProps()
               }
           
   
             </View> */}
        {/* </> */}
        {/* : */}
        <>
          {this.postContent()}
          {this.postFileUpload()}
        </>
        {/* } */}
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
});
