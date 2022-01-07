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

//点击的item在所有图片中的下标
let lastSelectedItemPosition = 0;
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
    if (this.props.selectMultiple) {
      let selectData = null;
      // 变成单选，设置最后一次选中的 item 为单选选中状态
      if (this.props?.multipleData?.length) {
        if (lastSelectedItemIndex > 0 && this.props.multipleData.length > lastSelectedItemIndex - 1) {
          selectData = this.props.multipleData[lastSelectedItemIndex - 1]
        } else {
          selectData = this.props.multipleData[this.props.multipleData.length - 1];
        }
      }
      if (!!selectData) {
        this.props.setMultipleData([selectData]);
      }
      // let endSelectData = this.props.multipleData[this.props.multipleData.length - 1];
      // this.props.setMultipleData([endSelectData]);
    }
    this.props.setSelectMultiple();
  };
  render() {
    // return null;
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

class PostContent extends Component {

  private moveScale: number;
  private showItemUri: string;
  constructor(props) {
    super(props);
    this.state = {
      cropScale: 0,
      videoPaused: false,
      isChangeScale: false,
      minScale: 0,
      positionX: 0,
      positionY: 0,
    };
    // 展示中的图片缩放值
    this.moveScale = 0;
    // 展示中的图片uri
    this.showItemUri = ""
  }

  componentDidUpdate(nextProps, nextState) {
    if (nextProps.selectMultiple !== this.props.selectMultiple) {
      // console.log("单选切换---");
        this.setState({
          positionX: 0,
          positionY: 0,
          cropScale: 0,
          minScale: 0,
        });
    }
  }

  shouldComponentUpdate(nextProps, nextState) {
    //没有选择照片时，不更新
    if (!nextProps.multipleData || nextProps.multipleData.length == 0) {
      return false;
    }
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
    } else if (nextState.cropScale !== this.moveScale) {
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
  toggleCropWidth = (imageItem) => {
    if (!!imageItem && !!this.moveScale) {
      let minScale = 1;
      if (imageItem.width > imageItem.height) {
        minScale = imageItem.height / imageItem.width
      } else {
        minScale = imageItem.width / imageItem.height
      }
      // isChangeScale = true 表示本次刷新强行更新 scale
      // 更新完 scale，回调 setChangeScale 重新设置为false,表示仅本次点击更新有效
      if (this.moveScale >= 1) {
        this.setState({
          cropScale: minScale,
          minScale: minScale,
          isChangeScale: true,
        });
      } else {
        this.setState({
          cropScale: 1,
          minScale: minScale,
          isChangeScale: true,
        });
      }
    }
  };

  /**
   * 重置 isChangeScale状态
   */
  setChangeScale = () => {
    this.setState({
      isChangeScale: false,
    });
  };

  render() {

    //设置选中的图片下标，并设置到 ImageCropper
    let imageItem = '';
    if (!!this.props.multipleData && this.props.multipleData.length > 0) {
      if (lastSelectedItemIndex > 0 && this.props.multipleData.length > lastSelectedItemIndex - 1) {
        imageItem = this.props.multipleData[lastSelectedItemIndex - 1].image
      } else {
        imageItem = this.props.multipleData[this.props.multipleData.length - 1].image;
      }
    }

    if (!this.props.multipleData[0]) return null;
    const { cropScale } = this.state;
    if (!imageItem) return null;

    let minScale = 1;
    if (imageItem.width > imageItem.height) {
      minScale = imageItem.height / imageItem.width
    } else {
      minScale = imageItem.width / imageItem.height
    }

    //多选模式，图片切换执行
    if (this.props.selectMultiple&&this.showItemUri !== imageItem?.uri) {
      const itemCropData = cropDataRow[imageItem?.uri];
      if (!!itemCropData) {
        const positionX = itemCropData?.positionX;
        const positionY = itemCropData?.positionY;
        const positionScale = itemCropData?.scale;

        if (positionX !== this.state.positionX || positionY !== this.state.positionY || positionScale !== this.state.cropScale) {
          this.setState({
            cropScale: positionScale,
            minScale: minScale,
            positionX: positionX,
            positionY: positionY,
          });
          this.showItemUri = imageItem?.uri;
          return null;
        }
      }
    }
    this.showItemUri = imageItem?.uri;
    //没有裁剪比例
    if (!cropScale) {
      this.setState({
        cropScale: minScale,
        minScale: minScale,
      });
      return null;
    }

    //宽高比不一致了，需要刷新一次
    if (this.state.minScale != minScale) {
      this.setState({
        cropScale: minScale,
        minScale: minScale,
        isChangeScale: true,
      });
      return null;
    }

    // console.log("positionX", this.state.positionX, "positionY", this.state.positionY, "cropScale", this.state.cropScale, "minScale", minScale);

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
        {(!imageItem?.videoFile || !imageItem?.playableDuration) &&
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
              this.toggleCropWidth(imageItem)
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
        }
        <View
          style={{
            backgroundColor: '#ececec',
            width: '100%',
          }}
        >
          <View style={{ backgroundColor: 'black' }}>
            <ImageCropper

              isChangeScale={this.state.isChangeScale}
              setChangeScale={this.setChangeScale}
              minScale={this.state.minScale}
              positionX={this.state.positionX}
              positionY={this.state.positionY}
              scale={cropScale}

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
              areaOverlay={<View></View>}
              setCropperParams={(cropperParams) => {
                if (imageItem?.videoFile) {
                  // cropDataRow?.imageItem?.videoFile = cropperParams
                  // cropDataRow = {...cropDataRow,imageItem.videoFile:cropperParams}
                } else {
                  // cropDataRow?.imageItem?.uri = cropperParams
                }
                let newKey = imageItem.uri;
                cropDataRow[newKey] = cropperParams;
                this.moveScale = cropperParams.scale;
                // console.log("cropperParams", cropperParams);
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
  selectMultiple: state.shootPost.selectMultiple,
});

PostContent = connect(PostContentMapStateToProps)(PostContent);
class GridItemCover extends Component {
  constructor(props) {
    super(props);
    this.state = {
      active: false,
      selectPostion: -1,
      selectIndex: 1,
      selectType: 0,
    };
    this.animteRef = new Animated.Value(0);
    if (this.props.multipleData.findIndex((i) => i.image.uri === this.props.item.image.uri) > -1) {
      this.state.active = true;
      this.animteRef = new Animated.Value(0.5);
    }
  }

  shouldComponentUpdate(nextProps, nextState) {

    //从选中一个，变成选中0个
    if (this.props.multipleData?.length > 0 && nextProps.multipleData.length == 0) {
      this.setState({
        active: false,
      });
      this.animteRef.setValue(0);
      return true;
    }

    //从选中0个，变成选中一个
    if (this.props.multipleData?.length == 0 && nextProps.multipleData.length > 0) {
      const active = nextProps.multipleData.findIndex((i) => i.image.uri === this.props.item.image.uri) > -1;
      this.setState({
        active,
      });
      const selectIndex = nextProps.multipleData.findIndex((item) => item.image.uri === this.props.item.image.uri);
      this.setState({ selectIndex: selectIndex + 1 });
      this.animteRef.setValue(active ? 0.5 : 0);
      return true;
    }

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

      //设置选中时的白色半透明背景
      if (nextProps.selectMultiple) {
        if (nextState.selectPostion === lastSelectedItemPosition) {
          if (active) {
            this.animteRef.setValue(0.5);
          } else {
            // 被取消选中
            this.animteRef.setValue(0);
          }
        } else {
          //多选
          this.animteRef.setValue(0);
        }
      } else {
        //单选
        this.animteRef.setValue(active ? 0.5 : 0);
      }
      return false;
    }
    if (nextState.active !== this.state.active) {
      return true;
    }
    if (nextState.selectIndex !== this.state.selectIndex) {
      return true;
    }
    return true;
  }


  clickItem = async (selectType: any) => {
    const { item, multipleData, selectMultiple, index } = this.props;
    const { type } = item;
    let fileType = item.playableDuration || type.split('/')[0] === 'video' ? 'video' : 'image';


    if (!!selectType && this.state.active && fileType === 'image') {
      //多选模式、已经选中的图片 被点击时，图片背景高亮
      let datalist = [...multipleData];
      this.props.setMultipleData(datalist);
      lastSelectedItemPosition = index;
      if (this.state.selectIndex != 0) {
        lastSelectedItemIndex = this.state.selectIndex;
      }
      this.setState({
        selectPostion: index,
        selectType: selectType,
      });
      return;
    } else {
      lastSelectedItemIndex = 0;
    }

    if (!selectMultiple) {
      cropDataRow = {};
    }

    if (multipleData?.length) {
      //  多选
      let fileSelectType = multipleData[multipleData.length - 1].type;
      if (fileSelectType != fileType && selectMultiple) {
        console.info('选择不一致 无效');
        return;
      }
    }

    const itemCopy = { ...item };

    if (fileType === 'video' && Math.ceil(item?.image?.playableDuration) > 300) {
      return this.props.toastRef.current.show('视频时长不能超过5分钟', 1000);
    }
    if (fileType === 'video') {
      // 这里验证一下是否可以用
      const localUri = await this.props.getVideFile(fileType, item);
      if (!localUri) {
        return;
      }
      //选择的是之前选中的视频，取消选中
      if (!!multipleData[0]) {
        if (multipleData[0].image.uri == itemCopy.image.uri) {
          this.props.setMultipleData([]);
          return
        }
      }
      itemCopy.image.videoFile = localUri;
      lastSelectedItemPosition = index;
      this.setState({
        selectPostion: index,
        selectType: selectType,
      });
      this.props.setMultipleData([itemCopy]);
    } else {
      // 单选
      if (!selectMultiple) {
        this.props.setMultipleData([itemCopy]);
        return;
      }

      let datalist = [...multipleData];

      // 遍历 判断是否已有
      let isrepetition = false;

      try {
        datalist.forEach((data, index) => {
          if (data.image.uri == itemCopy.image.uri) {
            isrepetition = true;
            //可以不选择图片或者视频
            // if (datalist.length == 1) {
            //   throw new Error('LoopTerminates');
            // }
            datalist.splice(index, 1);

          }
        });
      } catch (error) {
        console.info('至少选择一张图片');
      }
      if (datalist.length >= 10) {
        this.props.toastRef.current.show('最多选择十张图片', 1000);
        // 无效 注意
        return;
      }
      if (!isrepetition) {
        //这里保存每个选中的图片，在相册中对应的下标
        itemCopy.itemPosition = index;
        datalist.push(itemCopy);
      }

      lastSelectedItemPosition = index;

      //如果有删除，将选中的下标设置为数组的最后一个条在相册中对应的下标
      if (isrepetition) {
        if (datalist?.length) {
          lastSelectedItemPosition = datalist[datalist.length - 1].itemPosition
        }
      }

      this.setState({
        selectPostion: index,
        selectType: selectType,
      });
      this.props.setMultipleData(datalist);
    }
  };






  render() {
    const { item, multipleData, selectMultiple, index } = this.props;

    const { type } = item;

    //当前文件的类型
    let fileType = item.playableDuration || type.split('/')[0] === 'video' ? 'video' : 'image';

    //是否可以选择，默认可以
    let isSelector = true;
    if (selectMultiple) {
      if (!!multipleData && multipleData.length > 0) {
        //选中的文件类型
        let fileSelectType = multipleData[multipleData.length - 1]?.type ?? '';
        if (fileSelectType !== fileType) {
          isSelector = false;
        }
      }
    }

    return (
      <TouchableOpacity
        onPress={() => this.clickItem(1)}
        activeOpacity={1}
        disabled={!isSelector}
        style={[
          {
            backgroundColor: 'rgba(255,0,0,0)',
            width: photosItem,
            height: photosItem,
            position: 'absolute',
            zIndex: 1,
          },
          !isSelector && { backgroundColor: '#000', opacity: 0.5 },
        ]}
      >
        <View>
          <Pressable
            hitSlop={{ top: 10, bottom: 10, right: 10, left: 10 }}
            style={{ zIndex: 2, backgroundColor: '#000' }}
            onPress={() => {
              this.clickItem(0)
            }}>
            <View>
              <View
                style={[
                  {
                    borderRadius: 22,
                    borderWidth: 1,
                    width: 22,
                    height: 22,
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
                {fileType == 'video' ?
                  <Image
                    source={postFileSelectPng}
                    style={{
                      width: 20,
                      height: 20,
                      // borderRadius: 20,
                      zIndex: 99,
                      backgroundColor: '#836BFF',
                      justifyContent: 'center',
                      alignItems: 'center',
                      display: this.state.active ? 'flex' : 'none',
                    }}
                  />
                  :
                  <View
                    style={{
                      width: 20,
                      height: 20,
                      // borderRadius: 20,
                      zIndex: 99,
                      backgroundColor: '#836BFF',
                      justifyContent: 'center',
                      // alignItems: 'center',
                      display: this.state.active ? 'flex' : 'none',
                    }}
                  >
                    <Text style={[{ color: '#FFFFFF', textAlign: 'center', fontSize: 12, marginRight: 1 }]}>
                      {this.state.selectIndex}
                    </Text>
                  </View>
                }
              </View></View>
          </Pressable>
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

        </View>
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
        <Text style={[styles.continueText, multipleData[0]?.image?.playableDuration > 300 && { color: '#333' }]}>
          继续
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
    if (!!this.props.isExample) {
      this.getPhotos();
    } else {
      this.getPhotoFromCache();
    }
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
          let myAssetId = multipleData[0]?.image?.uri.slice(5);
          let localUri = await CameraRoll.requestPhotoAccess(myAssetId);
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
      return this.myRef.current.show('请至少选择一个上传文件', 1000);
    }

    const imageItem = multipleData[multipleData.length - 1].image;
    // TODO  安卓type 待文件类型
    let type = multipleData[multipleData.length - 1]?.type;

    if (type === 'video' && Math.ceil(imageItem.playableDuration) > 300) {
      return this.myRef.current.show('视频时长不能超过5分钟', 1000);
    }

    try {
      Platform.OS === 'android' ? (type = type.split('/')[0]) : '';
      let trimVideoData = null;
      let resultData = [];

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

        this.setState({
          postEditorParams: {
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
      return;
    } catch (e) {
      console.info(e, '错误');
    }
  };

  componentDidMount() { }

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
          <PostHeadWrap key={'PostHead'} {...this.props} postEditor={this.postEditor} />
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
