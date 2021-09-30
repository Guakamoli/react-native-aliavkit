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
  ScrollView,
} from 'react-native';
import _, { size } from 'lodash';
import Camera from './Camera';
import Carousel from 'react-native-snap-carousel';
import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast'
import CameraRoll from "@react-native-community/cameraroll";
import { FlatGrid } from 'react-native-super-grid';
import Video from 'react-native-video';

const FLASH_MODE_AUTO = 'auto';
const FLASH_MODE_ON = 'on';
const FLASH_MODE_OFF = 'off';

const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2
const captureIcon2 = (width - 20) / 2;
const photosItem = (width / 4);

export enum CameraType {
  Front = 'front',
  Back = 'back'
}

export type Props = {


  multipleBtnImage: any
  postCameraImage: any
  startMultipleBtnImage: any
  videoFile: any

}

type State = {
  CameraRollList: any,

  photoSelectType: string
  multipleData: any
  startmMltiple: boolean

  photoAlbum: any
  photoAlbumselect: any
}


export default class PostUpload extends Component<Props, State> {
  camera: any;
  myRef: any

  constructor(props) {
    super(props);
    this.myRef = React.createRef();



    this.state = {
      CameraRollList: [],
      photoSelectType: '',
      multipleData: [],
      startmMltiple: false,
      photoAlbum: [],
      photoAlbumselect: {},
      videoFile: ''
    };
  }

  componentDidMount() {


    var _that = this;
    //获取照片
    var getPhotos = CameraRoll.getPhotos({
      first: 100,
      assetType: 'All',
      //todo  安卓调试隐藏
      include: ["playableDuration", 'filename', 'fileSize', 'imageSize',],
      // groupTypes: 'Library'
    })
    var getAlbums = CameraRoll.getAlbums({
      assetType: 'All',

    })
    getAlbums.then((data) => {

      // 获取相册封面
      data.map(async (item) => {
        const cover = await CameraRoll.getPhotos({ first: 1, assetType: 'Photos', groupName: `${item.title}` })
        // 通过相册 名称获取
        data.map(item2 => {
          if (item2.title == cover.edges[0].node.group_name) {
            item2.cover = cover.edges[0].node.image.uri
          }
        })
      })
      // 相册数据
      this.setState({ photoAlbum: data, photoAlbumselect: data[0] })
    })

    getPhotos.then(async (data) => {
      var edges = data.edges;
      var photos = [];
      for (var i in edges) {
        // ios文件
        photos.push(edges[i].node);
      }
      _that.setState({
        CameraRollList: photos
      });
    }, function (err) {
      // alert( '获取照片失败！' );
    });
  }

  postHead() {
    return (
      <View style={{ height: 44, backgroundColor: '#000', flexDirection: "row", justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 12 }}>
        <Image
          style={styles.closeIcon}
          source={this.props.closeImage}
          resizeMode="contain"
        />
        <Text style={{ fontSize: 17, fontWeight: '500', color: "#fff", lineHeight: 24 }}>新作品</Text>
        <TouchableOpacity onPress={() => {


          let uplaodFile = []
          console.log('this.state.multipleData', this.state.multipleData);
          if (this.state.multipleData.length > 0) {
            this.state.multipleData.map(async (multipleDataItem) => {
              const { image: { uri, width, height, filename, fileSize, playableDuration }, type } = multipleDataItem


              let image_type = type + '/' + filename.split('.')[1]
              if (this.state.photoSelectType === 'image') {
                uplaodFile.push({
                  image_type,
                  image_dimensions: { width, height },
                  image_url: uri,
                  image_size: fileSize,
                  title: filename,
                  type: "file",
                })
              } else {
                uplaodFile.push({
                  video_type,
                  type: "file",
                  title_link: uri,
                  video_size: fileSize,
                  title: filename,
                })
              }


            })

          }
          console.log(uplaodFile);
        }}>
          <Text style={{ fontSize: 15, fontWeight: '400', color: "#fff", lineHeight: 21 }}>继续</Text>
        </TouchableOpacity>
      </View>
    )
  }
  postContent() {
    const { multipleData, CameraRollList, photoSelectType, videoFile } = this.state;
    return (
      <SafeAreaView style={{ flex: 1, padding: 0 }}>
        <ScrollView style={{
          height: height,
          margin: 0,
        }}
        >

          {
            photoSelectType === 'image' ? <Image
              style={[{
                width: width,
                height: height,
              },]}
              // 安卓展示不出来 权限问题？？？？ 
              // source={{ uri: item.image.uri }}
              source={{ uri: (multipleData.length > 0 ? multipleData[multipleData.length - 1]?.image?.uri : CameraRollList[0]?.image?.uri) }}
            /> :
              <Video
                source={{ uri: videoFile }}
                style={{
                  width: width,
                  height: height,
                }} />
          }
        </ScrollView>
      </SafeAreaView>
    )
  }
  postFileUploadHead() {
    const { startmMltiple, multipleData } = this.state;

    return (
      <View style={{ height: 58, backgroundColor: '#000', flexDirection: "row", justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 12 }}>
        <TouchableOpacity onPress={() => { }}>
          <View>
            <Text style={{ fontSize: 17, fontWeight: '500', color: "#fff", lineHeight: 24 }}>最近相册</Text>
          </View>
        </TouchableOpacity>
        <View style={{ flexDirection: "row", justifyContent: 'space-between', alignItems: 'center' }}>
          <TouchableOpacity onPress={() => {
            if (startmMltiple && multipleData.length) {
              this.setState({ multipleData: [multipleData[multipleData.length - 1]] })
            }
            this.setState({ startmMltiple: !startmMltiple, })
          }} >
            <Image
              style={[styles.multipleBtnImage, { marginRight: 10 }]}
              source={startmMltiple ? this.props.startMultipleBtnImage : this.props.multipleBtnImage}
              resizeMode="contain"
            />
          </TouchableOpacity>
          <Image
            style={styles.multipleBtnImage}
            source={this.props.postCameraImage}
            resizeMode="contain"
          />
        </View>
      </View>
    )
  }
  postFileUpload() {
    const { CameraRollList, multipleData, startmMltiple, photoSelectType } = this.state;

    const formatSeconds = (s) => {
      let t = '';
      if (s > -1) {
        let min = Math.floor(s / 60) % 60;
        let sec = s % 60;
        if (min < 10) { t += "0"; }
        t += min + ":";
        if (sec < 10) { t += "0"; }
        t += sec;
      }
      return t;
    }
    const getVideFile = async (item) => {
      if (photoSelectType !== 'video') return ''
      let myAssetId = item?.image?.uri.slice(5);
      // 获取视频文件 url 
      let returnedAssetInfo = await CameraRoll.getPhotoInfo(myAssetId, {});
      this.setState({ videoFile: returnedAssetInfo.localUri })
    }
    return (
      <>
        {this.postFileUploadHead()}
        <View style={[{ height: 291, backgroundColor: '#000', }, Platform.OS === 'android' ? { paddingBottom: 10 } : { paddingBottom: 35 }]}>
          <FlatGrid
            itemDimension={photosItem}
            data={CameraRollList}
            spacing={0}
            itemContainerStyle={{ margin: 0 }}
            renderItem={({ index, item }) => {
              const { type, image, } = item;
              const { photoSelectType, startmMltiple } = this.state
              // const a =timestamp
              return (
                <TouchableOpacity onPress={() => {
                  //  第一次

                  if (multipleData.length <= 1) {
                    // 获取第一次选择类型
                    let fileType = type.split('/')[0];
                    this.setState({
                      photoSelectType: fileType,
                      multipleData: [item]
                    })
                    if (photoSelectType === 'video') {
                      getVideFile(item)
                    }
                  }
                  if (startmMltiple) {
                    // 图片大于10 || 视频 大于 1 
                    if (photoSelectType == 'image') {
                      if (multipleData.length == 10) {
                        this.myRef.current.show('最多十张图片', 2000);
                        return;
                      }
                    } else {
                      if (multipleData.length = 1) {
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
                      })
                    } else {
                      datalist.push(item)
                    }
                    this.setState({
                      multipleData: datalist
                    })


                  }
                }}
                  disabled={!(type.indexOf(photoSelectType) !== -1) && startmMltiple}
                  activeOpacity={0.9}
                >
                  <View style={[{

                    position: 'relative',

                  },]}>

                    {
                      startmMltiple ? (
                        <>
                          < Image source={this.props.captureButtonImage} style={[{ width: 20, height: 20, position: 'absolute', right: 5, top: 5, zIndex: 98 }]} />
                          {
                            multipleData.includes(item) ? <View style={[
                              { width: 18, height: 18, borderRadius: 20, position: 'absolute', right: 6, top: 6, zIndex: 99, backgroundColor: '#836BFF', justifyContent: 'center', alignItems: 'center' },
                            ]}>
                              <Text style={{ color: '#fff', fontSize: 13, right: 5, position: 'absolute', top: 0, fontWeight: '400' }}>
                                {multipleData.indexOf(item) !== -1 ? multipleData.indexOf(item) + 1 : 1}
                              </Text>
                            </View> : null
                          }

                        </>
                      ) : null
                    }
                    <Image
                      key={index}
                      style={[{
                        width: photosItem,
                        height: photosItem,

                      }, !(type.indexOf(photoSelectType) !== -1) && startmMltiple ? { opacity: 0.4 } : {}]}
                      // 安卓展示不出来 权限问题？？？？ 
                      source={{ uri: item.image.uri }}
                      // source={require('../example/images/11.png')}
                      resizeMode="cover"
                    />
                    <View style={[{
                      width: photosItem,
                      height: photosItem,
                      position: 'absolute',
                      backgroundColor: '#fff',

                    }, multipleData[multipleData.length - 1]?.image.uri === image.uri ? { opacity: 0.5 } : { opacity: 0 }]}>
                    </View>
                    {
                      image.playableDuration ? <Text style={{ color: '#fff', fontSize: 12, fontWeight: '400', lineHeight: 17, zIndex: 100, position: "absolute", right: 6, bottom: 7 }}> {formatSeconds(Math.ceil(image.playableDuration ?? 0))}</Text> : null
                    }

                  </View>
                </TouchableOpacity>
              )
            }
            }
          />
        </View>
      </>
    )
  }
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

        {Platform.OS !== 'android' ? <View style={{ height: 44, backgroundColor: "red" }}></View> : null}


        <>
          {/* post */}
          {this.postHead()}
          {this.postContent()}
          {this.postFileUpload()}
        </>

      </>
    );
  }
}

const styles = StyleSheet.create(
  {

    closeIcon: {
      width: 28,
      height: 28,
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
    multipleBtnImage: {
      width: 31,
      height: 31
    }
  });

