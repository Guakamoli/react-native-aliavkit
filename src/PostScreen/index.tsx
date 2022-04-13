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
import I18n from '../i18n';

import { request, requestMultiple, check, checkMultiple, openSettings, PERMISSIONS, RESULTS } from 'react-native-permissions';

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
  videoMuted: boolean;
  isVidoePlayer: boolean;
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

class PostContent extends Component {

  private moveScale: number;
  constructor(props) {
    super(props);
    this.state = {
      imageItem: "",
      cropScale: 0,
      videoPaused: false,
      videoMuted: true,
      isChangeScale: false,
      minScale: 0,
      positionX: 0,
      positionY: 0,
      selectMultiple: false,
    };
    // 展示中的图片缩放值
    this.moveScale = 0;
    // 展示中的图片uri
  }

  componentDidUpdate(nextProps, nextState) {
    if (nextProps.selectMultiple !== this.props.selectMultiple) {
      this.setState({
        positionX: 0,
        positionY: 0,
        cropScale: 0,
        minScale: 0,
      });
    }

    if (nextProps.multipleData?.length < this.props.multipleData?.length) {
      this.setState({
        positionX: 0,
        positionY: 0,
        cropScale: 0,
        minScale: 0,
      });
    } else if (nextProps.multipleData?.length > this.props.multipleData?.length) {
      //
    }
  }


  shouldComponentUpdate(nextProps, nextState) {

    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen || nextProps.type !== this.props.type) {
      if ((!!nextProps.isDrawerOpen || !!this.props.isExample) && nextProps.type === 'post') {
        //  console.info("不静音");
        //延迟，相册第一个为视频时，延迟一会，否则会出现页面还没打开，声音先出现的问题
        setTimeout(() => {
          this.setState({
            videoMuted: false,
            videoPaused: false,
          });
        }, 500);
      } else {
        // console.info("静音");
        this.setState({
          videoMuted: true,
          videoPaused: true,
        });
      }
    }
    if (nextProps.isVidoePlayer !== this.props.isVidoePlayer) {
      if (nextProps.isVidoePlayer) {
        this.setState({
          videoPaused: false,
        });
      } else {
        this.setState({
          videoPaused: true,
        });
      }
      return false;
    }

    const isRender = this.isRender(nextProps, nextState)
    if (isRender == 0) {
      return false;
    } else if (isRender == 1) {
      return true;
    }

    if (nextState.isChangeScale) {
      return true;
    }

    if (nextState.cropScale !== this.state.cropScale) {
      return true;
    } else if (nextState.cropScale !== this.moveScale) {
      return true;
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
          positionX: 0,
          positionY: 0,
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

  emptyView = () => (
    <View style={{
      padding: 0,
      backgroundColor: '#000',
      position: 'relative',
      height: width,
      width: width,
    }}>
    </View>
  );

  //
  isRender = (nextProps, nextState) => {

    //没有选择照片时，不更新
    if (!nextProps.multipleData || nextProps.multipleData.length <= 0) {

      return 0;
    }

    let imageItem = null;
    if (lastSelectedItemIndex > 0 && nextProps.multipleData.length > lastSelectedItemIndex - 1) {
      imageItem = nextProps.multipleData[lastSelectedItemIndex - 1].image
    } else {
      imageItem = nextProps.multipleData[nextProps.multipleData.length - 1].image;
    }

    if (!imageItem) {
      return 0;
    }

    let videoPaused = true;
    if (!!nextProps.multipleData[0] && nextProps.multipleData[0]?.type == 'image') {
      videoPaused = true;
    } else {
      videoPaused = false;
    }

    let minScale = 1;
    if (imageItem.width > imageItem.height) {
      minScale = imageItem.height / imageItem.width
    } else {
      minScale = imageItem.width / imageItem.height
    }

    if (nextProps.selectMultiple != this.state.selectMultiple) {
      //切换单选多选
      this.setState({
        videoPaused: videoPaused,
        imageItem: imageItem,
        cropScale: minScale,
        minScale: minScale,
        positionX: 0,
        positionY: 0,
        selectMultiple: nextProps.selectMultiple,
      });
      return 0;
    }


    if (!nextProps.selectMultiple) {
      //单选 uri 改变 ，刷新
      if (imageItem?.uri !== this.state.imageItem?.uri) {
        // setTimeout(() => {
        // }, 100);
        this.setState({
          videoPaused: videoPaused,
          imageItem: imageItem,
          cropScale: minScale,
          minScale: minScale,
          positionX: 0,
          positionY: 0,
          selectMultiple: nextProps.selectMultiple,
        });
        return 0;
      } else {
        return 1;
      }
    } else {
      //多选
      if (!this.props.multipleData || nextProps.multipleData.length > this.props.multipleData.length) {
        //新增加了照片，按初始化值刷新
        this.setState({
          videoPaused: videoPaused,
          imageItem: imageItem,
          cropScale: minScale,
          minScale: minScale,
          positionX: 0,
          positionY: 0,
          selectMultiple: nextProps.selectMultiple,
        });
        return 0;
      } else {
        if (this.state.imageItem?.uri !== imageItem?.uri) {
          const itemCropData = cropDataRow[imageItem?.uri];
          if (!itemCropData) {
            this.setState({
              videoPaused: videoPaused,
              imageItem: imageItem,
              cropScale: minScale,
              minScale: minScale,
              positionX: 0,
              positionY: 0,
              selectMultiple: nextProps.selectMultiple,
            });
          } else {
            const positionX = itemCropData?.positionX;
            const positionY = itemCropData?.positionY;
            const positionScale = itemCropData?.scale;
            this.setState({
              videoPaused: videoPaused,
              imageItem: imageItem,
              cropScale: positionScale,
              minScale: minScale,
              positionX: positionX,
              positionY: positionY,
              selectMultiple: nextProps.selectMultiple,
            });
          }
          return 0;
        } else {
          return 1;
        }
      }

    }


    return -1;
  }

  render() {
    //
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
        {(!this.state.imageItem?.videoFile || !this.state.imageItem?.playableDuration) &&
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
              this.toggleCropWidth(this.state.imageItem)
            }}
          >
            <FastImage
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
              scale={this.state.cropScale}

              imageUri={this.state.imageItem?.uri ? this.state.imageItem?.uri : ""}
              videoFile={this.state.imageItem?.videoFile ? this.state.imageItem?.videoFile : ""}
              videoPaused={this.state.videoPaused}
              videoMuted={this.state.videoMuted}

              srcSize={{
                width: this.state.imageItem.width,
                height: this.state.imageItem.height,
              }}
              disablePin={!!this.state.imageItem?.videoFile}

              cropAreaWidth={width}
              cropAreaHeight={width}
              containerColor='black'
              areaColor='black'
              areaOverlay={<View></View>}
              setCropperParams={(cropperParams) => {
                if (this.state.imageItem?.videoFile) {
                  // cropDataRow?.imageItem?.videoFile = cropperParams
                  // cropDataRow = {...cropDataRow,imageItem.videoFile:cropperParams}
                } else {
                  // cropDataRow?.imageItem?.uri = cropperParams
                }
                let newKey = this.state.imageItem.uri;
                cropDataRow[newKey] = cropperParams;
                this.moveScale = cropperParams.scale;
                //
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
    //
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
      // if (fileSelectType != fileType && selectMultiple) {
      //  
      //   return;
      // }

      // android 返回的 fileSelectType 是   video/mp4 |  image/jpeg
      if (fileSelectType.indexOf(fileType) === -1 && selectMultiple) {

        return;
      }
    }

    const itemCopy = { ...item };

    if (fileType === 'video' && Math.ceil(item?.image?.playableDuration) > 300) {
      return this.props.toastRef.current.show(`${I18n.t('The_length_of_the_video_cannot_exceed_5_minutes')}`, 1000);
    }
    if (fileType === 'video') {
      // 这里验证一下是否可以用
      const localUri = await this.props.getVideFile(fileType, item);
      if (!localUri) {
        return;
      }
      //选择的是之前选中的视频，取消选中  仅多选能取消选中
      if (!!multipleData[0] && selectMultiple) {
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

            //把对应裁剪参数置空
            cropDataRow[itemCopy.image.uri] = null;
          }
        });
      } catch (error) {

      }
      if (datalist.length >= 10) {
        this.props.toastRef.current.show(`${I18n.t('Select_up_to_ten_pictures')}`, 1000);
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
        // android 的 fileSelectType 是   video/mp4 |  image/jpeg
        if (fileSelectType.indexOf(fileType) === -1) {
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
          !isSelector && { backgroundColor: '#000', opacity: 0.4 },
        ]}
      >
        <View>
          <Pressable
            style={{ zIndex: 2, width: 28, height: 28, position: 'absolute', top: 0, right: 0, overflow: 'hidden' }}
            onPress={() => {
              this.clickItem(0)
            }}>
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
                  right: 5,
                  top: 5,
                  display: this.props.selectMultiple ? 'flex' : 'none',
                },
                !this.props.selectMultiple && { position: 'relative' },
              ]}
            >
              {fileType == 'video' ?
                <FastImage
                  source={postFileSelectPng}
                  style={{
                    width: 20,
                    height: 20,
                    // borderRadius: 20,
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
            </View>
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
          height: 44,
          width: 50,
          paddingHorizontal: 12,
          justifyContent: 'center',
        }}
      >
        <FastImage style={styles.closeIcon} source={closePng} resizeMode='contain' />
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
        <Text style={[styles.continueText, multipleData[0]?.image?.playableDuration > 300 && { color: '#333', }]}>
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
class PostFileUpload extends Component {

  private isFirstLoad: boolean;
  constructor(props) {
    super(props);
    this.appState = '';
    this.isFirstLoad = true;
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


  getPhotos = async (isGetPermissions = false) => {
    // console.info("getPhotos isGetPermissions", isGetPermissions);
    if (!await this.checkStoragePermissions(false, true)) {
      if (isGetPermissions) {
        // setTimeout(async () => {
        if (await this.getStoragePermissions(true)) {
          this.getPhotos();
        }
        // }, 500);
      }
      return;
    }

    // if (this.isFirstLoad) {
    //   await new Promise((resolved) => {
    //     setTimeout(() => {
    //       resolved()
    //     }, 300);
    //   })
    //   this.isFirstLoad = false;
    // }

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

        setTimeout(async () => {
          let firstData = photos[0];
          let selectedValid = false;
          if (multipleData[0]) {
            let localUri;
            if (Platform.OS === 'ios') {
              let myAssetId = firstData?.image?.uri.slice(5);
              localUri = await CameraRoll.requestPhotoAccess(myAssetId);
            } else {
              localUri = firstData?.image?.uri;
            }
            if (localUri) {
              selectedValid = true;
            }
          }
          if (!selectedValid) {
            if (firstData.type.indexOf("video") !== -1) {
              let localUri;
              if (Platform.OS === 'ios') {
                let myAssetId = firstData?.image?.uri.slice(5);
                localUri = await CameraRoll.requestPhotoAccess(myAssetId);
              } else {
                localUri = firstData?.image?.uri;
              }
              firstData.image.videoFile = localUri;
            }
            this.props.setMultipleData([firstData]);
          }

          if (AsyncStorage) {
            await AsyncStorage.setItem('AvKitCameraRollList', JSON.stringify(photos));
          }
        }, 0);
        //
        this.setState({
          CameraRollList: photos,
        });
      },
      function (err) {
      },
    );
  };
  _handleAppStateChange = (nextAppState) => {
    if (!this.props.isDrawerOpen || this.props.type !== 'post') return

    if (this.appState.match(/inactive|background/) && nextAppState === 'active') {
      clickItemLock = false;
      this.getPhotos();
      // 在这里重新获取数据
      this.props.setVideoPlayer(true)
    } else {
      this.props.setVideoPlayer(false)
    }

    this.appState = nextAppState;
  };
  getVideFile = async (fileType, item) => {
    if (fileType !== 'video') return '';
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
      this.getPhotos(true);
    } else {
      this.getPhotoFromCache();
    }

  }

  /**
  * 检测是否有存储权限
  */
  checkStoragePermissions = async (isToSetting: boolean = false, isCheckLimited: boolean = false) => {
    if (Platform.OS === 'android') {
      const statuses = await checkMultiple([PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE, PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE]);
      if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === RESULTS.GRANTED && statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === RESULTS.GRANTED) {
        return true;
      } else if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === RESULTS.BLOCKED || statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === RESULTS.BLOCKED) {
        //拒绝且不再询问
        if (isToSetting) {
          this.showToSettingAlert();
        }
      }
    } else if (Platform.OS === 'ios') {
      const statuses = await check(PERMISSIONS.IOS.PHOTO_LIBRARY);
      if (statuses === RESULTS.GRANTED) {
        return true;
      } else if (statuses === RESULTS.BLOCKED) {
        if (isToSetting) {
          this.showToSettingAlert();
        }
      } else if (statuses === RESULTS.LIMITED) {
        // if (isCheckLimited) {
        //   await new Promise((resolved) => {
        //     setTimeout(() => {
        //       resolved()
        //     }, 500);
        //   })
        // }
        return isCheckLimited;
      }
    }
    return false;
  }

  /**
   *  获取存储权限
   * @param isToSetting  是否展示去设置的 Alert
   */
  getStoragePermissions = async (isToSetting: boolean = false) => {
    if (Platform.OS === 'android') {
      const statuses = await requestMultiple([PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE, PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE]);
      if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === 'granted' && statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === 'granted') {
        return true;
      } else if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === 'denied' || statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === 'denied') {
      } else if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === 'blocked' || statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === 'blocked') {
        if (isToSetting) {
          this.showToSettingAlert();
        }
      }
    } else if (Platform.OS === 'ios') {
      const statuses = await request(PERMISSIONS.IOS.PHOTO_LIBRARY);
      if (statuses === RESULTS.GRANTED) {
        return true;
      } else if (statuses === RESULTS.BLOCKED) {
        if (isToSetting) {
          this.showToSettingAlert();
        }
      } else if (statuses === RESULTS.LIMITED) {
        return true;
      }
    }
    return false;
  };

  showToSettingAlert = () =>
    Alert.alert(
      Platform.OS === 'ios' ? I18n.t('Need_album_permission') : "",
      Platform.OS === 'ios' ? "" : I18n.t('Need_album_permission'),
      [
        {
          text: `${I18n.t('Not_set_yet')}`,
          style: "default",
        },
        {
          text: `${I18n.t('go_to_settings')}`,
          onPress: () => openSettings(),
          style: "default",
        },
      ],
      {
        cancelable: true,
      }
    );



  shouldComponentUpdate(nextProps, nextState) {
    if (nextState.CameraRollList !== this.state.CameraRollList) {
      return true;
    }

    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen) {
      if (nextProps.isDrawerOpen) {
        this.props.setVideoPlayer(true);
        // if (this.state.CameraRollList?.length > 0) {
        //   this.props.setMultipleData([this.state.CameraRollList[0]]);
        // }
      } else {
        this.props.setVideoPlayer(false);
        //TODOWUYT
        try {
          if (this.props?.selectMultiple && multipleData?.length) {
            // console.info("拍摄器多选关闭",  multipleData.length);
            const selectData = multipleData[multipleData.length - 1];
            this.props.setMultipleData([selectData]);
            multipleData = [selectData];
          }
        } catch (error) {

        }
      }
    }

    if (nextProps.isDrawerOpen !== this.props.isDrawerOpen && nextProps.isDrawerOpen) {
      if (this.props.type === 'post') {
        setTimeout(async () => {
          this.getPhotos(true);
        }, 500);
      }
      return false;
    }
    if (nextProps.type !== this.props.type && nextProps.type === 'post') {
      if (this.props.isDrawerOpen) {
        this.getPhotos(true);
      }
      return false;
    }

    return false;
  }
  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
  }
  getPhotoFromCache = async () => {
    if (!await this.checkStoragePermissions()) {
      return;
    }

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
          let localUri;
          if (Platform.OS === 'ios') {
            let myAssetId = firstData?.image?.uri.slice(5);
            localUri = await CameraRoll.requestPhotoAccess(myAssetId);
          } else {
            localUri = firstData?.image?.uri;
          }
          if (localUri) {
            selectedValid = true;
          }
        }
        if (!selectedValid) {
          if (firstData.type.indexOf("video") !== -1) {
            let localUri;
            if (Platform.OS === 'ios') {
              let myAssetId = firstData?.image?.uri.slice(5);
              localUri = await CameraRoll.requestPhotoAccess(myAssetId);
            } else {
              localUri = firstData?.image?.uri;
            }
            firstData.image.videoFile = localUri;
          }
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
            // android上  spacing={0} 时，页面隐藏会闪退
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
    // TODO  安卓type 待文件类型
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
        // trimVideoData = await AVService.postCropVideo(trimVideoData, (progress: number) => {
        //   console.info("postCropVideo progress", progress);
        // });

        // console.info("trimVideoData save", trimVideoData);
        // CameraRoll.save(trimVideoData, { type: 'video' })

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

            //TODO 新增图片选中数据接口，包含裁剪参数、下标、uri 等等
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

      //TODO
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
      //TODO

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
      if (Platform.OS === 'ios') {
        path = `file://${encodeURI(resultData[i])}`
        type = resultData[i].split('.');
        type = `${item.type}/${type[type.length - 1].toLowerCase()}`;
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
        {/* <Text style={{ color: '#fff', fontSize: 15, paddingTop: 8, }}>{text}</Text> */}
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
          <PostContent key={'PostContent'} {...this.props} postEditorParams={this.state.postEditorParams} isVidoePlayer={this.state.isVidoePlayer} />
          <PostFileUploadHead key={'PostFileUploadHead'} {...this.props} />

          <PostFileUpload {...this.props} toastRef={this.myRef} setVideoPlayer={this.setVideoPlayer} />
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
