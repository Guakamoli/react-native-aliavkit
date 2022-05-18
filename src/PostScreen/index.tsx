import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  Dimensions,
} from 'react-native';

import { setSelectMultiple, setMultipleData } from '../actions/post';
import Toast, { DURATION } from 'react-native-easy-toast';
import { connect } from 'react-redux';
import I18n from '../i18n';

import ImageMap from '../../images';
const { postFileSelectPng, errorAlertIconPng } = ImageMap;
const { width, height } = Dimensions.get('window');

import PostHead from './PostHead';
import PostContent from './PostContent';
import PostPhotos from './PostPhotos'

let multipleData: any = [];

export type Props = {
  getUploadFile: (any) => void;
};

type State = {
  multipleData: any;
  selectMultiple: boolean;
  isVidoePlayer: boolean;
};


const getPostContentMapStateToProps = (state: any) => ({
  multipleData: state.shootPost.multipleData,
  selectMultiple: state.shootPost.selectMultiple,
});

const setPostContentMapStateToProps = (dispatch: any) => ({
  setSelectMultiple: () => dispatch(setSelectMultiple()),
  setMultipleData: (params: any) => {
    multipleData = params;
    dispatch(setMultipleData(params));
  },
});

const PostHeadView = connect(getPostContentMapStateToProps, setPostContentMapStateToProps)(PostHead);
const PostContentView = connect(getPostContentMapStateToProps, setPostContentMapStateToProps)(PostContent);
const PostPhotosView = connect(getPostContentMapStateToProps, setPostContentMapStateToProps)(PostPhotos);

export default class CameraScreen extends Component<Props, State> {
  camera: any;
  myRef: any;
  editor: any;
  messageRef: any;
  cropParams: Array<any>;
  private mClickLock: boolean;
  constructor(props: any) {
    super(props);
    this.myRef = React.createRef();
    this.messageRef = React.createRef();
    this.cropParams = [];
    this.state = {
      isVidoePlayer: true,
    };

    this.mClickLock = false;
  }

  setVideoPlayer = (isVidoePlayer) => {
    this.setState({ isVidoePlayer: isVidoePlayer })
  }

  /**
   * 裁剪参数
   */
  _onCropParams = (data) => {
    this.cropParams = data;
  };

  /**
   * Post上传
   */
  onPostUploadFiles = async () => {
    if (this.mClickLock) {
      return;
    }
    const uploadData = multipleData?.data

    if (!uploadData || uploadData.length < 1) {
      return this.myRef.current.show(`${I18n.t('Please_select_at_least_one_upload_file')}`, 1000);
    }

    //设置成选中状态，防止重复点击
    this.mClickLock = true;

    const fileType = uploadData[0].type;
    if (fileType.includes('video')) {
      this.onUploadVideo(uploadData);
    } else {
      this.onUploadImage(uploadData);
    }

    //状态重置
    this.mClickLock = false;
  }


  onUploadVideo = async (uploadData: Array<any>) => {
    uploadData.map(async (item, index) => {
      item.path = item.url;
      item.size = item.fileSize;
      item.name = item.filename;
      item.coverImage = '';
      item.localPath = item.url;
      return item
    })
    console.info("onUploadVideo", uploadData);
    this.sendUploadFile(uploadData)
  }

  onUploadImage = async (uploadData: Array<any>) => {
    uploadData.map(async (item, index) => {
      item.path = item.url;
      item.size = item.fileSize;
      item.name = item.filename;
      item.coverImage = '';
      item.localPath = item.url;

      const cropParams = this.cropParams[item.url]
      let imageWidthScale = item.width / width;
      let imageHeightScale = item.height / width;
      let translateXScale = cropParams.positionX / width;
      let translateYScale = cropParams.positionY / width;
      item.cropParams = {
        scale: cropParams.scale,
        widthScale: imageWidthScale,
        heightScale: imageHeightScale,
        translateXScale: translateXScale,
        translateYScale: translateYScale,
      }
      return item
    })
    console.info("onUploadImage", uploadData);
    this.sendUploadFile(uploadData)
  }

  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }

  componentDidMount() {

  }

  componentWillUnmount() {

  }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.type !== this.props.type) {
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

    return false;
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

        <PostHeadView {...this.props} onPostUploadFiles={this.onPostUploadFiles} />

        <PostContentView {...this.props} onCropParams={this._onCropParams} isVidoePlayer={this.state.isVidoePlayer} />

        <PostPhotosView {...this.props} toastRef={this.myRef} setVideoPlayer={this.setVideoPlayer} />

      </View>
    );
  }
}


const CameraScreenView = connect(getPostContentMapStateToProps, setPostContentMapStateToProps)(CameraScreen);


const styles = StyleSheet.create({




});
