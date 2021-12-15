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
  AppState,
} from 'react-native';
import { useSelector } from 'react-redux';
import { setSelectMultiple, setMultipleData } from '../actions/post';
import _, { lte } from 'lodash';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import { FlatGrid } from 'react-native-super-grid';
import AVService from '../AVService';
import ImageCropper from '../react-native-simple-image-cropper/src';
import PostEditor from '../PostEditor';
import { connect } from 'react-redux';
import Animated from 'react-native-reanimated';
import { Button } from 'react-native-elements';

import ImageMap from '../../images';
const { postFileSelectPng, errorAlertIconPng } = ImageMap;
const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2;
let clickItemLock = false;

const photosItem = width / 4;
let prevClickCallBack = null;
let multipleData = [];
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
  selectMultiple: boolean;
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
let getPhotosNum = 40;
let isMax = false;
let end_cursor;
class MultipleSelectButton extends Component {
  pressMultiple = () => {
    // 点击在这里修改数值
    // 取消多选 采用最后一个
    if (this.props.selectMultiple) {
      let endSelectData = this.props.multipleData[this.props.multipleData.length - 1];
      this.props.setMultipleData([endSelectData]);
    }
    this.props.setSelectMultiple();
  };
  render() {
    return (
      <Pressable onPress={this.pressMultiple}>
        <Image
          style={[styles.multipleBtnImage, { marginRight: 10 }]}
          source={this.props.selectMultiple ? this.props.startMultipleBtnImage : this.props.multipleBtnImage}
          resizeMode='contain'
        />
      </Pressable>
    );
  }
}
const MtbMapStateToProps = (state) => ({
  selectMultiple: state.shootPost.selectMultiple,
  multipleData: state.shootPost.multipleData,
});
const MtbMapDispatchToProps = (dispatch) => ({
  setSelectMultiple: () => dispatch(setSelectMultiple()),
  setMultipleData: (params) => {
    multipleData = params;

    dispatch(setMultipleData(params));
  },
});
MultipleSelectButton = connect(MtbMapStateToProps, MtbMapDispatchToProps)(MultipleSelectButton);
const PostFileUploadHead = React.memo((props) => {
  return (
    <View
      style={{
        height: 50,
        backgroundColor: 'black',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 12,
      }}
    >
      <TouchableOpacity>
        <Text style={{ fontSize: 17, fontWeight: '500', color: '#fff', lineHeight: 24 }}>最近相册</Text>
      </TouchableOpacity>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <MultipleSelectButton {...props} key={'MultipleSelectButton'} />

        {/* <Image style={styles.multipleBtnImage} source={props.postCameraImage} resizeMode='contain' /> */}
      </View>
    </View>
  );
});

const PostHead = React.memo((props) => {
  const { postEditor, goback } = props;
  const closePng = require('../../images/postClose.png');
  return (
    <View
      style={{
        height: 44,
        backgroundColor: '#000',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingBottom: 9,
      }}
    >
      <Pressable
        onPress={async () => {
          goback();
        }}
        style={{
          height: 30,
          width: 50,
          paddingHorizontal: 12,

          justifyContent: 'center',
        }}
      >
        <Image style={styles.closeIcon} source={closePng} resizeMode='contain' />
      </Pressable>
      <Text style={styles.textCenter}>新作品</Text>

      <Pressable
        onPress={postEditor}
        style={{
          height: 30,
          paddingHorizontal: 12,

          justifyContent: 'center',
          alignItems: 'flex-end',
        }}
      >
        <Text style={styles.continueText}>继续</Text>
      </Pressable>
    </View>
  );
});
class PostContent extends Component {
  constructor(props) {
    super(props);
    this.state = {
      cropScale: 1,
      videoPaused: false,
    };
  }
  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.multipleData !== this.props.multipleData) {
      if (!!nextProps.multipleData[0] && nextProps.multipleData[0]?.type == 'image') {
        this.setState({
          videoPaused: true,
        });
      } else {
        this.setState({
          videoPaused: false,
        });
      }
      return true;
    }
    if (nextState.cropScale !== this.state.cropScale) {
      return true;
    }
    if (nextState.videoPaused !== this.state.videoPaused) {
      return true;
    }

    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      this.setState({
        videoPaused: !nextProps.isDrawerOpen,
      });
      return false;
    }
    if (nextProps.postEditorParams !== this.props.postEditorParams) {
      console.info('修改');
      setTimeout(() => {
        this.setState({
          videoPaused: !!this.props.postEditorParams,
        });
      }, 0);

      return false;
    }

    return false;
  }
  toggleCropWidth = () => {
    if (!cropDataRow.scale || cropDataRow.scale < 1 || this.state.cropScale === 1) {
      this.setState({
        cropScale: 1,
      });
    } else {
      this.setState({
        cropScale: 1,
      });
    }
  };

  render() {
    if (!this.props.multipleData[0]) return null;
    const imageItem = this.props.multipleData[this.props.multipleData.length - 1].image;
    console.info('-----', imageItem);
    const { cropScale } = this.state;
    if (!imageItem) return null;
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
        {/* <TouchableOpacity
          style={{
            width: 31,
            height: 31,
            marginRight: 10,
            position: 'absolute',
            left: 15,
            bottom: 20,
            zIndex: 99,
          }}
          onPress={this.toggleCropWidth}
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
        </TouchableOpacity> */}

        <View
          style={{
            backgroundColor: '#ececec',
            width: '100%',
          }}
        >
          <View style={{ backgroundColor: 'black' }}>
            <ImageCropper
              imageUri={imageItem?.uri}
              videoFile={imageItem?.videoFile}
              videoPaused={this.state.videoPaused}
              srcSize={{
                width: imageItem.width,
                height: imageItem.height,
              }}
              disablePin={!!imageItem?.videoFile}
              cropAreaWidth={width}
              cropAreaHeight={width}
              containerColor='black'
              areaColor='black'
              scale={cropScale}
              areaOverlay={<View></View>}
              setCropperParams={(cropperParams) => {
                if (imageItem?.videoFile) {
                  console.info('是视频');
                  console.info('00000-----cropperParams', cropDataRow);
                  // cropDataRow?.imageItem?.videoFile = cropperParams
                  // cropDataRow = {...cropDataRow,imageItem.videoFile:cropperParams}
                } else {
                  // cropDataRow?.imageItem?.uri = cropperParams
                }
                let newKey = imageItem.uri;
                cropDataRow[newKey] = cropperParams;
                // cropDataRow = cropperParams;
                console.info('00000-----cropperParams', cropDataRow);
              }}
            />
          </View>
        </View>
      </View>
    );
  }
}
const PostContentMapStateToProps = (state) => ({
  multipleData: state.shootPost.multipleData,
});

PostContent = connect(PostContentMapStateToProps)(PostContent);
class GridItemCover extends Component {
  constructor(props) {
    super(props);
    this.state = {
      active: false,
      selectIndex: 1,
    };
    this.animteRef = new Animated.Value(0);
    if (this.props.multipleData.findIndex((i) => i.image.uri === this.props.item.image.uri) > -1) {
      this.state.active = true;
      this.animteRef = new Animated.Value(0.5);
    }
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.selectMultiple !== this.props.selectMultiple) {
      return true;
    }
    if (nextProps.multipleData !== this.props.multipleData) {
      const active = nextProps.multipleData.findIndex((i) => i.image.uri === this.props.item.image.uri) > -1;
      this.setState({
        active,
      });

      const selectIndex = nextProps.multipleData.findIndex((item) => item.image.uri === this.props.item.image.uri);

      this.setState({ selectIndex: selectIndex + 1 });

      this.animteRef.setValue(active ? 0.5 : 0);
      return false;
    }
    if (nextState.active !== this.state.active) {
      return true;
    }
    if (nextState.selectIndex !== this.state.selectIndex) {
      return true;
    }

    return false;
  }
  clickItem = async () => {
    // if (clickItemLock) return
    // clickItemLock = true
    const { item, multipleData, selectMultiple } = this.props;
    const { type } = item;
    if (!selectMultiple) {
      cropDataRow = {};
    }

    let fileType = item.playableDuration || type.split('/')[0] === 'video' ? 'video' : 'image';
    let fileSelectType = multipleData[multipleData.length - 1].type;
    const itemCopy = { ...item };
    //  已选的必须 一致

    if (fileSelectType != fileType && selectMultiple) {
      console.info('选择不一致 无效');
      return;
    }

    if (fileType === 'video') {
      // 这里验证一下是否可以用
      const localUri = await this.props.getVideFile(fileType, item);
      if (!localUri) return;

      itemCopy.image.videoFile = localUri;
      this.props.setMultipleData([itemCopy]);
    } else {
      // 单选
      console.info('222', !selectMultiple);
      if (!selectMultiple) {
        this.props.setMultipleData([itemCopy]);
        return;
      }
      //

      let datalist = [...multipleData];

      // 遍历 判断是否已有
      let isrepetition = false;
      console.info('--------datalist', datalist);
      try {
        datalist.forEach((data, index) => {
          if (data.image.uri == itemCopy.image.uri) {
            isrepetition = true;
            console.info('datalist.length', datalist.length);
            if (datalist.length == 1) {
              this.props.toastRef.current.show('最少选择一张图片', 2000);
              throw new Error('LoopTerminates');
            }
            datalist.splice(index, 1);
          }
        });
      } catch (error) {
        console.info('至少悬着一个2');
      }
      if (datalist.length >= 10) {
        this.props.toastRef.current.show('最多只能选择10张照片', 2000);
        // 无效 注意
        return;
      }
      if (!isrepetition) {
        datalist.push(itemCopy);
      }
      this.props.setMultipleData(datalist);
    }
    // prevClickCallBack?.();
    // prevClickCallBack = () => {
    //   this.setState({ active: false });
    //   this.animteRef.setValue(0);
    // };

    // if (this.props.selectMultiple) {
    //   this.animteRef.setValue(this.state.active ? 0 : 0.5);
    // } else {
    //   this.animteRef.setValue(0.5);
    // }
    // this.setState({
    //   active: !this.state.active,
    // });
    // setTimeout(() => {
    //   clickItemLock = false
    // }, 60);
  };
  render() {
    const { item, multipleData, selectMultiple } = this.props;

    const { type } = item;

    let fileSelectType = multipleData[multipleData.length - 1].type;
    let fileType = item.playableDuration || type.split('/')[0] === 'video' ? 'video' : 'image';
    let filtTypeSame = fileSelectType != fileType && selectMultiple;
    return (
      <TouchableOpacity
        onPress={this.clickItem}
        activeOpacity={1}
        disabled={filtTypeSame}
        style={[
          {
            width: photosItem,
            height: photosItem,
            position: 'absolute',
            zIndex: 1,
          },
          filtTypeSame && { backgroundColor: '#000', opacity: 0.5 },
        ]}
      >
        <>
          {/* TODO */}
          <View
            style={[
              {
                borderRadius: 10,
                borderWidth: 1,
                width: 20,
                height: 20,
                borderColor: 'white',
                overflow: 'hidden',
                position: 'absolute',
                zIndex: 99,
                right: 5,
                top: 5,
                display: this.props.selectMultiple ? 'flex' : 'none',
              },
              Platform.OS === 'android' && !this.props.selectMultiple && { position: 'relative' },
            ]}
          >
            <Image
              source={postFileSelectPng}
              style={{
                width: 18,
                height: 18,
                borderRadius: 18,
                zIndex: 99,
                backgroundColor: '#836BFF',
                justifyContent: 'center',
                alignItems: 'center',
                display: this.state.active && fileSelectType == 'video' ? 'flex' : 'none',
              }}
            />

            <View
              style={{
                width: 18,
                height: 18,
                // borderRadius: 20,
                zIndex: 99,
                backgroundColor: '#836BFF',

                justifyContent: 'center',
                alignItems: 'center',

                display: this.state.active && fileSelectType == 'image' ? 'flex' : 'none',
              }}
            >
              <Text style={{ color: '#FFFFFF', fontSize: 13 }}> {this.state.selectIndex}</Text>
            </View>
          </View>
          <Animated.View
            style={[
              {
                width: photosItem,
                height: photosItem,
                position: 'absolute',
                backgroundColor: '#fff',
              },
              { opacity: this.animteRef },
            ]}
          ></Animated.View>
        </>
      </TouchableOpacity>
    );
  }
}

const GIWMapStateToProps = (state) => ({
  selectMultiple: state.shootPost.selectMultiple,
  multipleData: state.shootPost.multipleData,
});
const GIWMapDispatchToProps = (dispatch) => ({
  setSelectMultiple: () => dispatch(setSelectMultiple()),
  setMultipleData: (params) => {
    multipleData = params;
    console.info('打印', params.length);
    dispatch(setMultipleData(params));
  },
});
GridItemCover = connect(GIWMapStateToProps, GIWMapDispatchToProps)(GridItemCover);
class GridItem extends Component {
  constructor(props) {
    super(props);
  }
  render() {
    const { item } = this.props;
    const { type, image } = item;
    return (
      <View>
        <GridItemCover {...this.props} key={item.image.uri} />
        <Image
          style={[
            {
              width: photosItem,
              height: photosItem,
            },
          ]}
          source={{ uri: item.image.uri }}
          resizeMode='cover'
        />

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
            {image.playableDurationFormat}
          </Text>
        ) : null}
      </View>
    );
  }
}

class PostFileUpload extends Component {
  constructor(props) {
    super(props);
    this.appState = '';
    this.state = {
      CameraRollList: [],
    };
  }
  formatSeconds = (s) => {
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
  getPhotos = (getMore = false) => {
    //获取照片
    clickItemLock = false;
    let getPhotosProps = {
      first: getPhotosNum,
      assetType: 'All',
      include: ['playableDuration', 'filename', 'fileSize', 'imageSize'],
    };
    let getPhotos = CameraRoll.getPhotos(getPhotosProps);
    const { AsyncStorage } = this.props;
    getPhotos.then(
      async (data) => {
        if (getPhotosNum > data.edges.length) {
          isMax = true;
        }
        // end_cursor =  data?.page_info?.end_cursor
        var edges = data.edges;
        var photos = [];
        for (var i in edges) {
          // ios文件
          const node = edges[i].node;
          node.image.playableDurationFormat = this.formatSeconds(Math.ceil(node.image.playableDuration ?? 0));
          photos.push(node);
        }
        let firstData = photos[0];

        let selectedValid = false;
        if (multipleData[0]) {
          let myAssetId = multipleData[0]?.image?.uri.slice(5);
          let localUri = await CameraRoll.requestPhotoAccess(myAssetId);

          if (localUri) {
            selectedValid = true;
          }
        }
        if (!selectedValid) {
          this.props.setMultipleData([firstData]);
        }

        if (AsyncStorage) {
          await AsyncStorage.setItem('AvKitCameraRollList', JSON.stringify(photos));
        }
        this.setState({
          CameraRollList: photos,
        });
      },
      function (err) {
        // alert( '获取照片失败！' );
      },
    );
  };
  _handleAppStateChange = (nextAppState) => {
    if (this.appState.match(/inactive|background/) && nextAppState === 'active') {
      clickItemLock = false;
      this.getPhotos();
      // 在这里重新获取数据

      console.log('App has come to the foreground!');
    }

    this.appState = nextAppState;
  };
  getVideFile = async (fileType, item) => {
    if (fileType !== 'video') return '';
    //TODO
    let localUri;
    if (Platform.OS === 'ios') {
      let myAssetId = item?.image?.uri.slice(5);
      localUri = await CameraRoll.requestPhotoAccess(myAssetId);
    } else {
      localUri = item?.image?.uri;
    }
    return localUri;
  };
  componentDidMount() {
    AppState.addEventListener('change', this._handleAppStateChange);
    this.getPhotoFromCache();
  }
  shouldComponentUpdate(nextProps, nextState) {
    if (nextState.CameraRollList !== this.state.CameraRollList) {
      return true;
    }
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen && nextProps.isDrawerOpen) {
      if (this.props.type === 'post') {
        this.getPhotos();
      }
      return false;
    }
    if (nextProps.type !== this.props.type && nextProps.type === 'post') {
      if (this.props.isDrawerOpen) {
        this.getPhotos();
      }
      return false;
    }

    return false;
  }
  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
  }
  getPhotoFromCache = async () => {
    const { AsyncStorage } = this.props;
    if (AsyncStorage) {
      let photos = await AsyncStorage.getItem('AvKitCameraRollList');
      if (photos) {
        photos = JSON.parse(photos);
      }
      if (photos) {
        const firstData = photos[0];
        if (!firstData) return;
        let selectedValid = false;
        if (multipleData[0]) {
          console.info(multipleData[0]?.image?.uri, 'multipleData[0]?.image?.uri');
          let myAssetId = multipleData[0]?.image?.uri.slice(5);
          console.info('kaishi');
          let localUri = await CameraRoll.requestPhotoAccess(myAssetId);
          console.info('kaishi22');

          console.info(localUri, 'localUrilocalUrilocalUri');
          if (localUri) {
            selectedValid = true;
          }
        }
        if (!selectedValid) {
          this.props.setMultipleData([firstData]);
        }

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
      }
    }
  };
  render() {
    return (
      <>
        <View
          style={[
            { height: height * 0.4, backgroundColor: '#000' },
            Platform.OS === 'android' ? { paddingBottom: 10 } : { paddingBottom: 35 },
          ]}
        >
          <FlatGrid
            //TODO android上  spacing={0} 时，页面隐藏会闪退
            itemDimension={Platform.OS === 'android' ? photosItem - 4 : photosItem}
            data={this.state.CameraRollList}
            spacing={Platform.OS === 'android' ? 1 : 0}
            initialNumToRender={30}
            maxToRenderPerBatch={30}
            windowSize={3}
            removeClippedSubviews={true}
            itemContainerStyle={{ margin: 0 }}
            renderItem={(props) => {
              return (
                <GridItem
                  {...props}
                  getVideFile={this.getVideFile}
                  toastRef={this.props.toastRef}
                  key={props?.item?.image?.uri}
                />
              );
            }}
            onEndReachedThreshold={0.5}
            onEndReached={() => {
              if (isMax) {
                return;
              }
              getPhotosNum += 12;
              this.getPhotos();
            }}
          />
        </View>
      </>
    );
  }
}
const PFUMapStateToProps = (state) => ({
  selectMultiple: state.shootPost.selectMultiple,
});
const PFUMapDispatchToProps = (dispatch) => ({
  setMultipleData: (params) => {
    multipleData = params;

    dispatch(setMultipleData(params));
  },
});
PostFileUpload = connect(PFUMapStateToProps, PFUMapDispatchToProps)(PostFileUpload);
export default class CameraScreen extends Component<Props, State> {
  camera: any;
  myRef: any;
  editor: any;
  messageRef: any;
  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    this.messageRef = React.createRef();
    this.appState = '';
    this.cropData = {};
    this.state = {
      postEditorParams: null,
      page: 'main',
    };
  }
  playVideo = () => {
    this.setState({ videoPaused: false });
  };
  postEditor = async () => {
    if (multipleData.length < 1) {
      return this.myRef.current.show('请至少选择一个上传文件', 2000);
    }
    try {
      const imageItem = multipleData[multipleData.length - 1].image;
      // TODO  安卓type 待文件类型
      let type = multipleData[multipleData.length - 1]?.type;
      Platform.OS === 'android' ? (type = type.split('/')[0]) : '';
      let trimVideoData = null;
      let resultData = [];

      console.info('----cropDataRow', cropDataRow);
      // const result = await ImageCropper.crop({
      //   ...cropDataRow[imageItem?.uri],
      //   imageUri: imageItem.uri,
      //   cropSize: {
      //     width: width,
      //     height: width,
      //   },
      //   cropAreaSize: {
      //     width: width,
      //     height: width,
      //   },
      // });
      // 循环选中的图片 取出裁剪数据
      const result = await Promise.all(
        multipleData.map(async (item) => {
          return await ImageCropper.crop({
            ...cropDataRow[item.image.uri],
            imageUri: imageItem.uri,
            cropSize: {
              width: width,
              height: width,
            },
            cropAreaSize: {
              width: width,
              height: width,
            },
          });
        }),
      );
      console.info('resultsresultsresultsresults', result);
      if (type === 'video') {
        if (Platform.OS !== 'android') {
          trimVideoData = await AVService.saveToSandBox({
            path: imageItem.uri,
          });
          resultData.push(trimVideoData);
        } else {
          trimVideoData = imageItem.uri;
        }
      } else {
        const cropData = result;
        let results = await Promise.all(
          multipleData.map(async (item, index) => {
            // 等待异步操作完成，返回执行结果
            return await AVService.crop({
              source: item.image.uri,
              cropOffsetX: cropData[index].offset.x,
              cropOffsetY: cropData[index].offset.y,
              cropWidth: cropData[index].size.width,
              cropHeight: cropData[index].size.height,
            });
          }),
        );
        console.info('------裁剪数据回调233', results);
        resultData = results;
      }

      console.info('-xx', resultData);

      // this.setState({ videoPaused: true });
      if (resultData.length > 0) {
        let trimmerRightHandlePosition =
          Math.ceil(imageItem.playableDuration) < 300 ? Math.ceil(imageItem.playableDuration) * 1000 : 300 * 1000;
        let fileType = imageItem.playableDuration || type.split('/')[0] === 'video' ? 'video' : 'image';

        let videoTime = Math.ceil(imageItem.playableDuration) * 1000 ?? 0;
        if (type === 'video' && Math.ceil(imageItem.playableDuration) > 300) {
          this.myRef.current.show('请修剪视频,视频时长不能超过5分钟', 2000);
        }

        this.setState({
          postEditorParams: {
            trimVideoData: resultData,
            videoduration: videoTime,
            trimmerRight: trimmerRightHandlePosition,
            fileType,
            cropDataRow: cropDataRow,
            cropDataResult: result,
            cropDataKey: multipleData,
          },
          videoPaused: true,
          page: 'eidt',
        });
        this.props.setType('edit');
      }
      return;
    } catch (e) {
      console.info(e, '错误');
    }
  };

  componentDidMount() {}

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.connected !== this.props.connected && !nextProps.connected) {
      this.messageRef?.current?.show(
        <View
          style={{
            width: width * 0.9,
            height: 30,
            borderRadius: 9,
            justifyContent: 'flex-start',
            alignItems: 'center',
            flexDirection: 'row',
          }}
        >
          <Image source={errorAlertIconPng} style={{ width: 22, height: 22, marginRight: 14 }} />
          <Text style={{ color: '#fff', fontSize: 14, fontWeight: '400' }}>无网络连接</Text>
        </View>,
        2000,
      );
      return true;
    }
    if (nextState.postEditorParams !== this.state.postEditorParams) {
      return true;
    }
    if (nextState.page !== this.state.page) {
      return true;
    }
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      return true;
    }
    return false;
  }
  componentWillUnmount() {
    // 结束编辑页面
    // RNEditViewManager.stop();
  }
  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }
  render() {
    return (
      <>
        <StatusBar barStyle={'light-content'} />

        <Toast
          ref={this.myRef}
          position='top'
          positionValue={height * 0.4}
          fadeInDuration={1050}
          fadeOutDuration={800}
          opacity={0.8}
        />
        <Toast
          ref={this.messageRef}
          position='top'
          positionValue={height * 0.8}
          fadeInDuration={1050}
          fadeOutDuration={800}
          opacity={0.8}
        />
        <View style={{ display: this.state.page === 'main' ? 'flex' : 'none' }}>
          <PostHead key={'PostHead'} {...this.props} postEditor={this.postEditor} />
          <PostContent key={'PostContent'} {...this.props} postEditorParams={this.state.postEditorParams} />
          <PostFileUploadHead key={'PostFileUploadHead'} {...this.props} />

          <PostFileUpload {...this.props} toastRef={this.myRef} />
        </View>
        {this.state.postEditorParams ? (
          <PostEditor
            {...this.props}
            params={this.state.postEditorParams}
            playVideo={this.playVideo}
            goback={() => {
              this.props.setType('post');

              this.setState({ page: 'main', postEditorParams: null });
            }}
          />
        ) : null}
      </>
    );
  }
}

const styles = StyleSheet.create({
  closeIcon: {
    width: 16,
    height: 16,
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
  continueText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#836BFF',
    lineHeight: 21,
  },
  textCenter: {
    fontSize: 17,
    fontWeight: '500',
    color: '#fff',
    lineHeight: 24,
  },
});
