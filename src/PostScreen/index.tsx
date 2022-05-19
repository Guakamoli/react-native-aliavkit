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

import PostImageEditor from '../PostEditor/PostImageEditor'


let multipleData: any = [];

type Props = {
  getUploadFile: (any) => void;
  setType: (string) => void;
  type: string;
  connected: string;
};

type State = {
  isVidoePlayer: boolean;
  uploadData: Array<any>;
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
const PostImageEditorView = connect(getPostContentMapStateToProps, setPostContentMapStateToProps)(PostImageEditor);

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
      uploadData: [],
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

  onUploadImage = async (data: Array<any>) => {
    let uploadData: Array<any> = [];
    data.forEach((item, index) => {
      const itemCrop = this.cropParams[item.url]
      let imageWidthScale = item.width / width;
      let imageHeightScale = item.height / width;
      let translateXScale = itemCrop.positionX / width;
      let translateYScale = itemCrop.positionY / width;
      const cropParams = {
        scale: itemCrop.scale,
        widthScale: imageWidthScale,
        heightScale: imageHeightScale,
        translateXScale: translateXScale,
        translateYScale: translateYScale,
      }
      const imageInfo = {
        index: item.index,
        width: item.width,
        height: item.height,
        path: item.url,
        size: item.fileSize,
        name: item.filename,
        type: item.type,
        coverImage: '',
        localPath: item.url,
        cropParams: cropParams
      }

      uploadData.push(imageInfo);
    });
    
    this.setState({ uploadData: uploadData });
    this.props.setType('postImageEdit');

    // console.info("onUploadVideo", uploadData);
    // this.sendUploadFile(uploadData)
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
    if (nextState.uploadData !== this.state.uploadData) {
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
      <View style={{ width: '100%', height: '100%', position: 'relative', overflow: this.props.type === 'postImageEdit' ? 'hidden' : 'visible' }}>
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

        {this.props.type === 'postImageEdit' &&
          <PostImageEditorView {...this.props} toastRef={this.myRef} uploadData={this.state.uploadData} />
        }

      </View>
    );
  }
}


const CameraScreenView = connect(getPostContentMapStateToProps, setPostContentMapStateToProps)(CameraScreen);


const styles = StyleSheet.create({




});
