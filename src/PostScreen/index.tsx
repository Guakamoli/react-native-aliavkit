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
  Alert,
  ActivityIndicator,
} from 'react-native';

import FastImage from '@rocket.chat/react-native-fast-image';

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
import I18n from '../i18n';

import PostContent from './PostContent';
import PostPhotosAlbum from './PostPhotosAlbum'



import ImageMap from '../../images';
const { postFileSelectPng, errorAlertIconPng } = ImageMap;
const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2;

let multipleData = [];

//点击的item在选中集合中的下标
let lastSelectedItemIndex = 0;


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
  videoMuted: boolean;
  isVidoePlayer: boolean;
};


let cropDataRow = {};
let getPhotosNum = 40;
let isMax = false;
class MultipleSelectButton extends Component {
  pressMultiple = () => {
    this.props.setSelectMultiple();
  };

  /**
   * setState 刷新时触发
   * @returns true 会继续更新； false 不会执行 render
   */
  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      if (nextProps.isDrawerOpen) {
        //这里刷新，重置ppost状态
        if (this.props.selectMultiple) {
          this.props.setSelectMultiple();
        }
      }
    }
    return true
  }

  render() {
    // return null;
    return (
      <Pressable onPress={this.pressMultiple}>
        <FastImage
          testID={`post-multiple-button`}
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
        <Text style={{ fontSize: 17, fontWeight: '500', color: '#fff', lineHeight: 24 }}>{`${I18n.t('Recent_Albums')}`}</Text>
      </TouchableOpacity>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <MultipleSelectButton {...props} key={'MultipleSelectButton'} />

        {/* <Image style={styles.multipleBtnImage} source={props.postCameraImage} resizeMode='contain' /> */}
      </View>
    </View>
  );
});


const PostContentMapStateToProps = (state) => ({
  multipleData: state.shootPost.multipleData,
  selectMultiple: state.shootPost.selectMultiple,
});
const PostContentView = connect(PostContentMapStateToProps)(PostContent);

const GIWMapStateToProps = (state) => ({
  selectMultiple: state.shootPost.selectMultiple,
  multipleData: state.shootPost.multipleData,
});
const GIWMapDispatchToProps = (dispatch) => ({
  setSelectMultiple: () => dispatch(setSelectMultiple()),
  setMultipleData: (params) => {
    multipleData = params;
    dispatch(setMultipleData(params));
  },
});

const PostHead = React.memo((props) => {
  const { postEditor, goback, multipleData, setSelectMultiple } = props;
  const closePng = require('../../images/postClose.png');
  const pressMultiple = () => {
    // 点击在这里修改数值
    // 取消多选 采用最后一个
    if (props.selectMultiple) {
      let endSelectData = props.multipleData[props.multipleData.length - 1];
      props.setMultipleData([endSelectData]);
    }
    props.setSelectMultiple();
  };
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
          // pressMultiple();
        }}
        style={{
          height: 44,
          width: 50,
          paddingHorizontal: 12,
          justifyContent: 'center',
        }}
      >
        <FastImage testID={"back-home"} style={styles.closeIcon} source={closePng} resizeMode='contain' />
      </Pressable>
      <Text style={styles.textCenter}>{`${I18n.t('New_product')}`}</Text>

      <Pressable
        onPress={postEditor}
        style={{
          height: 44,
          paddingHorizontal: 12,
          justifyContent: 'center',
          alignItems: 'flex-end',
        }}
      >
        <Text testID={'confirm-upload'} style={[styles.continueText, multipleData[0]?.image?.playableDuration > 300 && { color: '#333', }]}>
          {I18n.t('continue')}
        </Text>
      </Pressable>
    </View>
  );
});
const PostHeadMapStateToProps = (state) => ({
  selectMultiple: state.shootPost.selectMultiple,
  multipleData: state.shootPost.multipleData,
});

const PostHeadWrap = connect(PostHeadMapStateToProps, GIWMapDispatchToProps)(PostHead);


const PFUMapStateToProps = (state) => ({
  selectMultiple: state.shootPost.selectMultiple,
});
const PFUMapDispatchToProps = (dispatch) => ({
  setMultipleData: (params) => {
    multipleData = params;

    dispatch(setMultipleData(params));
  },
});
const PostPhotosAlbumView = connect(PFUMapStateToProps, PFUMapDispatchToProps)(PostPhotosAlbum);

export default class CameraScreen extends Component<Props, State> {
  camera: any;
  myRef: any;
  editor: any;
  messageRef: any;
  private mClickLock: boolean;
  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    this.messageRef = React.createRef();
    this.appState = '';
    this.state = {
      postEditorParams: null,
      page: 'main',
      isVidoePlayer: true,
      isShowLoading: false,
    };

    this.mClickLock = false;
  }

  setVideoPlayer = (isVidoePlayer) => {
    this.setState({ isVidoePlayer: isVidoePlayer })
  }

  playVideo = () => {
    this.setState({ videoPaused: false });
  };
  postEditor = async () => {

    if (this.mClickLock) {
      return;
    }

    if (multipleData.length < 1) {
      return this.myRef.current.show(`${I18n.t('Please_select_at_least_one_upload_file')}`, 1000);
    }

    const imageItem = multipleData[multipleData.length - 1].image;
    //  安卓type 待文件类型
    let type = multipleData[multipleData.length - 1]?.type;

    if (type === 'video' && Math.ceil(imageItem.playableDuration) > 300) {
      return this.myRef.current.show(`${I18n.t('The_length_of_the_video_cannot_exceed_5_minutes')}`, 1000);
    }

    this.mClickLock = true;

    try {
      Platform.OS === 'android' ? (type = type.split('/')[0]) : '';
      let trimVideoData = null;
      let resultData = [];
      let editImageData = new Array;

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
      if (type === 'video') {

        this.setState({
          isShowLoading: true,
        })
        trimVideoData = imageItem.uri;

        //TODOWUYQ
        // if (Platform.OS === 'ios') {
        //   var videoIndex = imageItem.videoFile.lastIndexOf(".");
        //   //获取后缀
        //   var videoType = imageItem.videoFile.substr(videoIndex + 1).toLowerCase();
        //   if (videoType !== 'mp4') {
        //     //保存到沙盒
        //     trimVideoData = await AVService.saveToSandBox(imageItem.uri);
        //   } else {
        //     //url 授权, ios url  需要特殊处理
        //     let myAssetId = imageItem.uri.slice(5);
        //     trimVideoData = await CameraRoll.requestPhotoAccess(myAssetId);
        //     //裁剪 file://
        //     if (!!trimVideoData && trimVideoData.startsWith("file://")) {
        //       trimVideoData = trimVideoData.slice(7)
        //     }
        //   }
        // }


        // TODOWUYQ  视频压缩
        if (Platform.OS === 'ios') {
          let myAssetId = trimVideoData.slice(5);
          trimVideoData = await CameraRoll.requestPhotoAccess(myAssetId);
          if (!!trimVideoData && trimVideoData.startsWith("file://")) {
            trimVideoData = trimVideoData.slice(7)
          }
        }

        // // 测试代码
        // AVService.postCancelCrop();
        // // cropParam: {"isCrop": number, "path": String}   isCroped:是否裁剪，isCroped = 0 时不需要删除 path
        // const cropParam = await AVService.postCropVideo(trimVideoData, (progress: number) => {
        //   console.info("postCropVideo progress", progress);
        //   if (progress > 0.5) {
        //     AVService.postCancelCrop();
        //   }
        // });
        // trimVideoData = cropParam.path
        // console.info("trimVideoData save", cropParam);
        // CameraRoll.save(trimVideoData, { type: 'video' })
        // // 测试代码

        resultData.push(trimVideoData);

        this.setState({
          isShowLoading: false,
        })
      } else {
        const cropData = result;
        //图片编辑的参数，包含裁剪展示参数

        editImageData = [];
        let results = await Promise.all(
          multipleData.map(async (item, index) => {

            let imageScale = cropDataRow[item?.image?.uri].scale;
            let imageWidth = cropDataRow[item?.image?.uri].fittedSize?.width;
            let imageHeight = cropDataRow[item?.image?.uri].fittedSize?.height;

            //使用的地方，设置图片宽高需要乘以这个 imageWidthScale。imageHeightScale值
            let imageWidthScale = imageWidth / width;
            let imageHeightScale = imageHeight / width;

            let translateX = cropDataRow[item?.image?.uri].positionX;
            let translateY = cropDataRow[item?.image?.uri].positionY;

            let translateXScale = translateX / width;
            let translateYScale = translateY / width;


            let imageUri = item.image.uri;

            let imageName = item.image.filename;

            //TODOWUYQ
            if (Platform.OS === 'ios') {
              const videoIndex = imageName.lastIndexOf(".");
              //获取后缀
              const imageType = imageName.substr(videoIndex + 1).toLowerCase();

              if (!!imageType && (imageType !== 'jpg' || imageType !== 'png')) {
                //保存到沙盒
                imageUri = await AVService.saveToSandBox(imageUri);
                imageName = imageUri.substring(imageUri.lastIndexOf('/') + 1); //文件名

              } else {
                imageUri = await CameraRoll.requestPhotoAccess(imageUri.slice(5));
              }
            }

            // 新增图片选中数据接口，包含裁剪参数、下标、uri 等等
            editImageData[index] = {
              index: index,
              type: item.type,
              name: imageName,
              size: item?.image?.fileSize,
              uri: imageUri,

              //图片原始宽高
              srcWidth: item.image.width,
              srcHeight: item.image.height,

              scale: imageScale,
              widthScale: imageWidthScale,
              heightScale: imageHeightScale,
              translateXScale: translateXScale,
              translateYScale: translateYScale,
            };

            return item.image.uri;
          }),
        );
        //
        resultData = results;
      }
      //

      if (this.props.selectMultiple) {
        let selectData = null;
        // 变成单选，设置最后一次选中的 item 为单选选中状态
        if (multipleData?.length) {
          if (lastSelectedItemIndex > 0 && multipleData.length > lastSelectedItemIndex - 1) {
            selectData = multipleData[lastSelectedItemIndex - 1]
          } else {
            selectData = multipleData[multipleData.length - 1];
          }
        }
        if (!!selectData) {
          setTimeout(() => {
            this.props.setMultipleData([selectData]);
            this.props.setSelectMultiple();
          }, 1000);
        }
      }



      this.setVideoPlayer(false);

      //
      //选择图片视频直接上传，不进入编辑页面
      if (type === 'video') {
        //
        this.onUploadVideo(multipleData, resultData);
      } else {
        //
        this.onUploadPhoto(editImageData)
      }
      this.mClickLock = false;
      return;

      // this.setState({ videoPaused: true });
      if (resultData.length > 0) {
        let trimmerRightHandlePosition =
          Math.ceil(imageItem.playableDuration) < 300 ? Math.ceil(imageItem.playableDuration) * 1000 : 300 * 1000;
        let fileType = imageItem.playableDuration || type.split('/')[0] === 'video' ? 'video' : 'image';

        let videoTime = Math.ceil(imageItem.playableDuration) * 1000 ?? 0;

        this.setState({
          postEditorParams: {
            editImageData: editImageData,
            // 数据
            trimVideoData: resultData,
            videoduration: videoTime,
            trimmerRight: trimmerRightHandlePosition,
            fileType,
            cropDataRow: cropDataRow,
            cropDataResult: result,
            // 原始数据
            originalData: multipleData,
          },
          videoPaused: true,
          page: 'eidt',
        });
        this.props.setType('edit');
      }
      this.mClickLock = false;
      return;
    } catch (e) {

      this.mClickLock = false;
    }
  };

  onUploadVideo = async (multipleData, resultData) => {
    let uploadData = [];
    for (let i = 0; i < multipleData.length; i++) {
      const item = multipleData[i];
      if (!item?.image) {
        return;
      }
      if (!resultData[i]) {
        return;
      }

      let type;
      let path;
      let localPath = item.image.uri;

      if (Platform.OS === 'ios') {
        path = resultData[i];
        type = `video/mp4`;
      } else {
        type = item.type;
        path = resultData[i];
      }

      uploadData[i] = {
        index: i,
        type: type,
        path: path,
        size: item.image.fileSize,
        name: resultData[i],
        coverImage: '',
        width: item.image.width,
        height: item.image.height,
        localPath,
      }
    }
    //
    this.props.getUploadFile(uploadData);
  }

  onUploadPhoto = async (editImageData) => {

    let uploadData = [];
    for (let i = 0; i < editImageData.length; i++) {
      const item = editImageData[i];
      if (!item?.uri) {
        return;
      }

      let localUri = item.uri;
      let type;
      if (Platform.OS === 'ios') {
        type = item.name.split('.');
        type = `${item.type}/${type[type.length - 1].toLowerCase()}`;

      } else {
        type = item.type;
        localUri = item.uri;
      }
      if (!localUri) {
        return;
      }
      uploadData[i] = {
        index: item.index,
        type: type,
        path: localUri,
        size: item.size,
        name: item.name,
        coverImage: localUri,
        width: item.srcWidth,
        height: item.srcHeight,

        cropParams: {
          scale: item.scale,
          widthScale: item.widthScale,
          heightScale: item.heightScale,
          translateXScale: item.translateXScale,
          translateYScale: item.translateYScale,
        }
      }
    }
    //
    this.props.getUploadFile(uploadData);
  }

  componentDidMount() { }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.type !== this.props.type) {
      return true;
    }
    if (nextState.isShowLoading !== this.state.isShowLoading) {
      return true;
    }
    if (nextState.isVidoePlayer !== this.state.isVidoePlayer) {
      return true;
    }
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
          <Text style={{ color: '#fff', fontSize: 14, fontWeight: '400' }}>{`${I18n.t('No_internet_connection')}`}</Text>
        </View>,
        1000,
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
      if (!nextProps.isDrawerOpen) {
        //这里刷新，重置ppost状态
        this.appState = '';
        this.setState({
          postEditorParams: null,
          page: 'main',
          isVidoePlayer: true,
          isShowLoading: false,
        });
        this.mClickLock = false;
      }
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


  loadingView = (text = `${I18n.t('Loading')}`, isShow = true) => {
    return isShow && (<View style={{
      position: 'absolute',
      left: 0,
      right: 0,
      top: 0,
      bottom: 0,
      alignItems: 'center',
      justifyContent: 'center',
      // backgroundColor: 'rgba(255,0,0,1)',
    }}>
      <View style={{
        width: 120,
        height: 120,
        backgroundColor: 'rgba(80,80,80,1)',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: 10,
        paddingTop: 8
      }}>
        <ActivityIndicator
          size={'large'}
          color="#fff"
          animating={true}
          hidesWhenStopped={true}
        />
      </View>

    </View >)
  }



  render() {

    return (
      <View style={{ width: '100%', height: '100%', position: 'relative' }}>
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

          <PostHeadWrap key={'PostHead'} {...this.props} postEditor={this.postEditor} />
          <PostContentView key={'PostContent'} {...this.props} postEditorParams={this.state.postEditorParams} isVidoePlayer={this.state.isVidoePlayer} />
          <PostFileUploadHead key={'PostFileUploadHead'} {...this.props} />

          <PostPhotosAlbumView {...this.props} toastRef={this.myRef} setVideoPlayer={this.setVideoPlayer} />
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
        {this.loadingView(`${I18n.t('Video_processing')}`, this.state.isShowLoading)}
      </View>
    );
  }
}

CameraScreen = connect(PostHeadMapStateToProps, GIWMapDispatchToProps)(CameraScreen);

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
    lineHeight: 44,
  },
  textCenter: {
    fontSize: 17,
    fontWeight: '500',
    color: '#fff',
    lineHeight: 24,
  },
});
