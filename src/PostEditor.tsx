import React, { Component, useEffect, useRef, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Image,
  Dimensions,
  FlatList,
  NativeModules,
  StatusBar,
  Pressable,
  Animated,
  ScrollView,
} from 'react-native';
import _ from 'lodash';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import { FlatGrid } from 'react-native-super-grid';
import Video from 'react-native-video';
import Carousel from 'react-native-snap-carousel';
import Trimmer from './react-native-trimmer';
import VideoEditor from './VideoEditor';
import AVService from './AVService.ios';
import { Grayscale, Temperature, Sepia } from 'react-native-image-filter-kit';

// let a  = require('../images/postEditorNoMute.png');

const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2;
const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
const photosItem = width / 4;
const cropWidth = width - 30 * 2;
const PostEditor = (props) => {
  // const {params:{fileType='',trimVideoData="",trimmerRight="",videoduration=''}} = props;
  const {
    route: {
      params: { trimVideoData = '', fileType = '' },
    },
    navigation,
    uploadFile,
    volumeImage,
    noVolumeImage,
  } = props;
  const [multipleSandBoxData, setmultipleSandBoxData] = useState([]);
  const [filterList, setfilterList] = useState([]);
  const [filterName, setfilterName] = useState(null);
  const [videoMute, setvideoMute] = useState(false);
  const [coverList, setcoverList] = useState([]);
  const [coverImage, setcoverImage] = useState('');
  const [selectBottomModel, setselectBottomModel] = useState('滤镜');
  const [trimmerLeftHandlePosition, settrimmerLeftHandlePosition] = useState(0);
  const [trimmerRightHandlePosition, settrimmerRightHandlePosition] = useState(0);
  const [videoTime, setVideoTime] = useState(0);
  const [scrubberPosition, setscrubberPosition] = useState(0);
  const [exportVideo, setexportVideo] = useState(false);
  const scrollAniRef = useRef(new Animated.Value(10)).current;
  const [imgfilterName, setImgFilterName] = useState('');
  const stopRef = useRef(false);
  const startRef = useRef(false);

  const [photoFile, setPhotoFile] = useState('');
  navigation.setOptions({
    headerTitle: (props) => {
      if (fileType == 'image') {
        return null;
      }
      return (
        <TouchableOpacity
          onPress={() => {
            setvideoMute(!videoMute);
          }}
        >
          <Image style={{ width: 30, height: 21 }} source={!videoMute ? volumeImage : noVolumeImage} />
        </TouchableOpacity>
      );
    },
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
        <Pressable
          onPress={async () => {
            if (fileType === 'image') {
              console.log('导出图片');
              let uploadFile = [];
              uploadFile.push({
                Type: `image/png`,
                path: photoFile,
                size: 0,
                Name: photoFile,
                coverImage: photoFile,
              });
              props.getUploadFile(uploadFile);
            } else {
              // 裁剪视频
              RNEditViewManager.trimVideo({
                videoPath: multipleSandBoxData[0],
                startTime: trimmerLeftHandlePosition / 1000,
                endTime: trimmerRightHandlePosition / 1000,
              });
              // 导出视频
              if (exportVideo) {
                return;
              }
              setexportVideo(true);
            }
          }}
          style={{
            width: 30,
            height: 20,
            justifyContent: 'center',
            alignItems: 'center',
            // borderRadius: 100,
            // backgroundColor: `rgba(0, 0, 0, 0.19)`,
          }}
        >
          <Text style={{ color: '#fff', fontWeight: '700', fontSize: 15 }}>继续</Text>
        </Pressable>
      );
    },
  });

  let coverData = [];
  let editor = null;
  let scrubberInterval = null;
  const getFilters = async () => {
    //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}

    const infos = await RNEditViewManager.getFilterIcons({});

    infos.unshift({ filterName: null, iconPath: '', title: '无效果' });
    setfilterList(infos);
  };

  useEffect(() => {
    getFilters();
    const {
      route: { params },
    } = props;
    setmultipleSandBoxData([params?.trimVideoData]);
    setVideoTime(params?.videoduration);
    settrimmerRightHandlePosition(params?.trimmerRight);
  }, [props]);
  useEffect(() => {
    return () => {
      console.log('销毁了');
      AVService.removeThumbnaiImages();
      RNEditViewManager.stop();
      props.route.params.palyVide();
    };
  }, []);
  const getcoverData = async () => {
    console.log('获取封面3');
    console.log();
    console.log('multipleSandBoxData', multipleSandBoxData);
    console.log(fileType);
    if (fileType == 'image') {
      return null;
    }

    const videoTimeSecond = videoTime / 1000;

    let itemPerTime = videoTime / 13;
    if (videoTimeSecond < 10) {
      itemPerTime = videoTime / 8;
    }

    coverData = await AVService.getThumbnails({
      videoPath: multipleSandBoxData[0],
      startTime: 0,
      itemPerTime: Math.floor(itemPerTime),
    });

    setcoverList(coverData);
    setcoverImage(coverData[0]);
  };
  useEffect(() => {
    console.log('获取封面', multipleSandBoxData);
    if (multipleSandBoxData.length > 0) {
      getcoverData();
      console.log('----: getcoverData');
    }
  }, [multipleSandBoxData]);

  const onExportVideo = (event) => {
    if (event.exportProgress === 1) {
      let outputPath = event.outputPath;
      let uploadFile = [];
      //
      let type = outputPath.split('.');
      uploadFile.push({
        Type: `${fileType}/${type[type.length - 1]}`,
        path: fileType == 'video' ? `file://${encodeURI(outputPath)}` : outputPath,
        size: 0,
        Name: outputPath,
        coverImage: coverImage ? `file://${encodeURI(coverImage)}` : '',
      });
      props.getUploadFile(uploadFile);
    }
  };

  const postEditorViewData = () => {
    const delta = trimmerRightHandlePosition - trimmerLeftHandlePosition;

    return (
      <View style={{}}>
        <VideoEditor
          editWidth={width}
          editHeight={height * 0.3}
          ref={(edit) => (editor = edit)}
          filterName={filterName}
          videoPath={multipleSandBoxData[0]}
          saveToPhotoLibrary={true}
          startExportVideo={exportVideo}
          videoMute={videoMute}
          onExportVideo={(event) => {
            onExportVideo(event);
          }}
          onPlayProgress={({ nativeEvent }) => {
            console.info(nativeEvent.playProgress, trimmerLeftHandlePosition, 'trimmerRightHandlePosition');
            if (nativeEvent.playProgress * 1000 >= trimmerLeftHandlePosition && !startRef.current) {
              startRef.current = true;

              Animated.timing(
                // 随时间变化而执行动画
                scrollAniRef, // 动画中的变量值
                {
                  toValue: Math.min(delta / videoTime, 1) * cropWidth, // 透明度最终变为1，即完全不透明
                  duration: delta, // 让动画持续一段时间
                  useNativeDriver: true,
                },
              ).start();
            }
            if (fileType === 'video') {
              if (
                nativeEvent.playProgress === undefined ||
                (nativeEvent.playProgress * 1000 >= trimmerRightHandlePosition && !stopRef.current)
              ) {
                stopRef.current = true;
                startRef.current = false;

                RNEditViewManager.pause();

                RNEditViewManager.seekToTime(trimmerLeftHandlePosition / 1000);
                scrollAniRef.setValue(0);
                setTimeout(() => {
                  stopRef.current = false;

                  RNEditViewManager.play();
                }, 500);
                return;
              }
            }
          }}
          onExportVideo={(event) => {
            onExportVideo(event);
          }}
        />
      </View>
    );
    // }
    // 多图 展示
    // return(
    //   <View style={{height:375,paddingHorizontal:0}}>

    //   <Carousel
    //   data={multipleSandBoxData}
    //   // containerCustomStyle={{backgroundColor:'green',}}
    //   itemWidth={1000}
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
  };
  // 滤镜组件
  const filterEditorFilter = () => {
    return (
      <View style={{ bottom: height * 0.15, position: 'absolute' }}>
        <FlatList
          data={filterList}
          initialNumToRender={4}
          horizontal={true}
          renderItem={({ index, item }) => {
            return (
              <View style={{ height: 130, flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                <TouchableOpacity
                  onPress={() => {
                    setfilterName(item?.filterName);
                  }}
                >
                  <View
                    style={[
                      { marginRight: 4 },
                      filterName == item.filterName && { borderWidth: 2, borderColor: '#fff' },
                    ]}
                  >
                    {item.filterName == null ? (
                      <View style={{ width: 100, height: 100, backgroundColor: 'rgba(69, 69, 73, 0.7);' }}>
                        <Image style={{ width: 100, height: 100 }} source={props.noResultPng} />
                      </View>
                    ) : (
                      <Image style={{ width: 100, height: 100 }} source={{ uri: item.iconPath }} />
                    )}
                  </View>
                </TouchableOpacity>
              </View>
            );
          }}
        />
      </View>
    );
  };

  // 裁剪
  const postTrimer = () => {
    const onHandleChange = async ({ leftPosition, rightPosition }) => {
      if (leftPosition < 0) {
        leftPosition = 0;
      }
      if (rightPosition < 2000) {
        rightPosition = 2000;
      }
      scrollAniRef.setValue(0);
      RNEditViewManager.seekToTime(leftPosition / 1000);
      setTimeout(() => {
        RNEditViewManager.play();
        stopRef.current = false;
        startRef.current = false;
      }, 500);
      console.info(leftPosition, rightPosition, 'leftPosition, rightPosition ');
      settrimmerLeftHandlePosition(leftPosition);
      settrimmerRightHandlePosition(rightPosition);
      setscrubberPosition(leftPosition);
    };

    return (
      <>
        <View style={{ paddingHorizontal: 5, bottom: 140, position: 'absolute' }}>
          <Trimmer
            onHandleChange={onHandleChange}
            totalDuration={videoTime}
            initialZoomValue={1}
            maxTrimDuration={trimmerRightHandlePosition}
            trimmerLeftHandlePosition={trimmerLeftHandlePosition}
            trimmerRightHandlePosition={trimmerRightHandlePosition}
            scrubberPosition={scrubberPosition}
            onScrubbingComplete={() => {
              // RNEditViewManager.replay();
            }}
            scrollAniRef={scrollAniRef}
            tintColor='white'
            markerColor='#5a3d5c'
            trackBackgroundColor='white'
            trackBorderColor='#5a3d5c'
            scrubberColor='white'
            onScrubberPressIn={() => {
              console.log('onScrubberPressIn');
            }}
            onRightHandlePressIn={() => {
              stopRef.current = true;
              startRef.current = false;
              scrollAniRef.setValue(0);

              RNEditViewManager.pause();
            }}
            trackWidth={cropWidth}
            onLeftHandlePressIn={() => {
              stopRef.current = true;
              startRef.current = false;

              scrollAniRef.setValue(0);

              RNEditViewManager.pause();
            }}
            trackHeight={50}
          >
            <View style={{ flexDirection: 'row' }}>
              {coverList.map((i) => {
                return (
                  <Image
                    source={{ uri: i }}
                    style={{ width: cropWidth / coverList.length, height: 50 }}
                    resizeMode={'cover'}
                  />
                );
              })}
            </View>
          </Trimmer>
        </View>
      </>
    );
  };
  // 封面
  const postCover = () => {
    return (
      <View style={{ marginTop: 93, paddingHorizontal: 17 }}>
        <FlatList
          data={coverList}
          initialNumToRender={7}
          horizontal={true}
          renderItem={({ index, item }) => {
            return (
              <View style={{ height: 130 }}>
                {/* 封面选择 */}
                <TouchableOpacity
                  onPress={() => {
                    setcoverImage(item);
                  }}
                >
                  <Image
                    source={{ uri: item }}
                    style={{ width: 65, height: 74, backgroundColor: 'green', marginRight: 2 }}
                  />
                </TouchableOpacity>
              </View>
            );
          }}
        />
      </View>
    );
  };
  // 切换底部功能
  const switchProps = () => {
    const switchProps = ['滤镜', '修剪'];

    return (
      <View
        style={{
          height: height * 0.08,
          width: width,
          flexDirection: 'row',
          justifyContent: 'space-evenly',
          alignItems: 'flex-start',
          position: 'absolute',
          bottom: 0,
        }}
      >
        {switchProps.map((item, index) => {
          return (
            <TouchableOpacity
              key={index}
              onPress={() => {
                setselectBottomModel(item);
              }}
            >
              <Text style={[styles.postSwitchProps, selectBottomModel === item && { color: '#fff' }]}>{item}</Text>
            </TouchableOpacity>
          );
        })}
      </View>
    );
  };
  // 图片滤镜
  const result = () => {
    const Extractor = (imgFilter) => {
      switch (imgFilter) {
        case 'Sepia': {
          return (
            <Sepia
              image={
                <Image
                  style={{ width: width, height: width }}
                  source={{ uri: multipleSandBoxData[0] }}
                  resizeMode={'contain'}
                />
              }
              amount={2}
            />
          );
        }
        case 'Temperature': {
          return (
            <Temperature
              amount={0.5}
              image={
                <Image
                  style={{ width: width, height: width }}
                  source={{ uri: multipleSandBoxData[0] }}
                  resizeMode={'contain'}
                />
              }
            />
          );
        }
        case 'Sepia2': {
          return (
            <Sepia
              amount={0.4}
              image={
                <Image
                  style={{ width: width, height: width }}
                  source={{ uri: multipleSandBoxData[0] }}
                  resizeMode={'contain'}
                />
              }
            />
          );
        }
        default: {
          return (
            <Image
              style={{ width: width, height: width }}
              source={{ uri: multipleSandBoxData[0] }}
              resizeMode={'contain'}
            />
          );
        }
      }
    };

    return (
      <>
        <Grayscale
          amount={0}
          onExtractImage={({ nativeEvent }) => {
            setPhotoFile(nativeEvent.uri);
          }}
          extractImageEnabled={true}
          image={Extractor(imgfilterName)}
        ></Grayscale>
        <ScrollView horizontal={true} contentContainerStyle={{ alignItems: 'center' }}>
          <TouchableOpacity
            onPress={() => {
              setImgFilterName('');
            }}
          >
            <View style={{ width: 100, height: 100, backgroundColor: 'rgba(69, 69, 73, 0.7);' }}>
              <Image style={{ width: 100, height: 100 }} source={props.noResultPng} />
            </View>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => {
              setImgFilterName('Sepia');
            }}
          >
            <Sepia
              image={
                <Image
                  style={{ width: 100, height: 100, marginRight: 5 }}
                  // source={require('./parrot.png')}
                  source={{ uri: multipleSandBoxData[0] }}
                  resizeMode={'contain'}
                />
              }
              amount={2}
            />
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => {
              setImgFilterName('Temperature');
            }}
          >
            <Temperature
              amount={0.5}
              image={
                <Image
                  style={{ width: 100, height: 100, marginRight: 5 }}
                  // source={require('./parrot.png')}
                  source={{ uri: multipleSandBoxData[0] }}
                  resizeMode={'contain'}
                />
              }
            />
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => {
              setImgFilterName('Sepia2');
            }}
          >
            <Sepia
              amount={0.4}
              image={
                <Image
                  style={{ width: 100, height: 100, marginRight: 5 }}
                  // source={require('./parrot.png')}
                  source={{ uri: multipleSandBoxData[0] }}
                  resizeMode={'contain'}
                />
              }
            />
          </TouchableOpacity>
        </ScrollView>
      </>
    );
  };
  if (fileType == 'image') {
    return <View style={{ flex: 1, backgroundColor: '#000' }}>{result()}</View>;
  }

  return (
    <View style={{ flex: 1, backgroundColor: '#000', position: 'relative' }}>
      <StatusBar barStyle={'light-content'} />
      {postEditorViewData()}

      {selectBottomModel === '滤镜' && filterEditorFilter()}
      {selectBottomModel === '修剪' && postTrimer()}
      {/* {
        selectBottomModel === '封面' && postCover()
      } */}
      {fileType !== 'image' && switchProps()}
    </View>
  );
};

const styles = StyleSheet.create({
  postSwitchProps: {
    fontSize: 16,
    color: '#8E8E8E',
    fontWeight: '500',
  },
});

export default PostEditor;
