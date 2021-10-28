import React, { Component, useEffect, useRef, useState } from 'react';
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
  Pressable,
  Animated,
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
import { SoftLightBlend, Emboss, Earlybird, Invert, RadialGradient } from 'react-native-image-filter-kit';
// let a  = require('../images/postEditorNoMute.png');

const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2;
const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
const photosItem = width / 4;
const cropWidth = width - 30 * 2;
const PostEditor = (props) => {
  // const {videoTime} = props;

  // const {params:{fileType='',trimVideoData="",trimmerRight="",videoduration=''}} = props;
  const {
    route: {
      params: { trimVideoData = '', fileType = '' },
    },
    navigation,
    uploadFile,
  } = props;
  const [multipleSandBoxData, setmultipleSandBoxData] = useState([]);
  const [multipleData, setmultipleData] = useState([]);

  const [filterList, setfilterList] = useState([]);
  const [filterName, setfilterName] = useState('');
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
  const stopRef = useRef(false);

  // console.log('navigationnavigation',navigation);

  // const result = await RNEditViewManager.trimVideo({
  //   videoPath: this.state.videoPath,
  //   startTime: 2.0,
  //   endTime: 8.0,
  // });

  navigation.setOptions({
    headerTitle: (props) => {
      return (
        // <Button title='play' onPress={() => {

        // } }  />

        <Image
          style={{ width: 40, height: 40 }}
          source={{
            uri: props.noVolume,
          }}
        />
      );
    },
    // headerRight: () =>
    //   headerStyle: {
    //     backgroundColor: "#000",
    // },
    headerRight: () => {
      return (
        <Pressable
          onPress={async () => {
            // uploadFile(123)

            console.log(1231, multipleSandBoxData);
            // 裁剪视频
            RNEditViewManager.trimVideo({
              videoPath: multipleSandBoxData[0],
              startTime: 1.4,
              endTime: 5.0,
            });
            // 导出视频
            if (exportVideo) {
              return;
            }
            setexportVideo(true);
            // this.setState({ startExportVideo: true });
            console.log('22222');
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
          <Text style={{ color: '#000', fontWeight: '700', fontSize: 15 }}>继续</Text>
        </Pressable>
      );
    },
  });
  // let scrubberPosition= 0
  let coverData = [];
  let editor = null;
  let scrubberInterval = null;
  // console.log('----------',props);

  // const uploadFile

  const getFilters = async () => {
    //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}
    // if(this.state.filterList.length < 1){
    const infos = await RNEditViewManager.getFilterIcons({});
    // console.log('------infos',infos);
    setfilterList(infos);
    // this.setState({filterList:infos})
    // }
  };

  useEffect(() => {
    getFilters();
    const {
      route: { params },
    } = props;
    console.log('---props', props);
    // setmultipleSandBoxData(props.route.para)
    setmultipleSandBoxData([params?.trimVideoData]);
    setVideoTime(params?.videoduration);
    settrimmerRightHandlePosition(params?.trimmerRight);
  }, [props]);
  useEffect(() => {
    return () => {
      console.log('销毁了');

      //
      AVService.removeThumbnaiImages();
      RNEditViewManager.stop();
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
    // await AVService.removeThumbnaiImages()
    const videoTimeSecond = videoTime / 1000;

    let itemPerTime = videoTime / 13;
    if (videoTimeSecond < 10) {
      itemPerTime = videoTime / 8;
    }
    coverData = await AVService.getThumbnails({
      videoPath: multipleSandBoxData[0],
      startTime: 0,
      itemPerTime: itemPerTime,
    });
    // console.log('=====coverList:',coverList);

    console.log('------', coverData);
    setcoverList(coverData);
    // this.setState({coverList:coverData})
    setcoverImage(coverData[0]);
  };
  useEffect(() => {
    console.log('获取封面', multipleSandBoxData);
    if (multipleSandBoxData.length > 0) {
      getcoverData();
    }
  }, [multipleSandBoxData]);

  const onExportVideo = (event) => {
    if (event.exportProgress === 1) {
      let outputPath = event.outputPath;
      // this.setState({ startExportVideo: false,outputPath:event.outputPath });
      // console.log('视频导出成功, path = ', event.outputPath);
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
      // this.sendUploadFile(uploadFile)
    }
  };

  const postEditorViewData = () => {
    // const {multipleData,multipleSandBoxData}  = this.state
    // console.log('1231',multipleData);
    // console.log('data.length',multipleData[0].length);
    // const onlyOne = multipleSandBoxData.length === 1

    // if(onlyOne){
    // console.log('11111111111postEditorViewData',multipleSandBoxData);
    // console.log('filterName',filterName);

    // if(!this.state.fileEditor){
    console.log('销毁postEditorViewData', multipleSandBoxData[0]);

    //   return null

    // }
    //       if(multipleSandBoxData.length < 1){
    //  return null
    //       }
    // return null

    console.log('exportVideo', exportVideo);
    const delta = trimmerRightHandlePosition - trimmerLeftHandlePosition;

    return (
      <View>
        <VideoEditor
          ref={(edit) => (editor = edit)}
          filterName={filterName}
          // videoPath={"/var/mobile/Containers/Data/Application/9DBC4AB0-799C-4BD4-ADF1-E1F8339AF173/Documents/com.guakamoli.engine/composition/4AAF423D-3D69-4020-A695-37A855D5E460.mp4"}
          // imagePath={multipleSandBoxData[0]}
          videoPath={multipleSandBoxData[0]}
          saveToPhotoLibrary={false}
          startExportVideo={exportVideo}
          videoMute={false}
          onExportVideo={(event) => {
            onExportVideo(event);
          }}
          onPlayProgress={({ nativeEvent }) => {
            if (fileType === 'video') {
              if (!stopRef.current && nativeEvent.playProgress) {
                scrollAniRef.setValue(
                  ((nativeEvent.playProgress * 1000 - trimmerLeftHandlePosition) / delta) *
                    (Math.min(delta / videoTime, 1) * cropWidth),
                );
              }

              if (
                nativeEvent.playProgress === undefined ||
                (nativeEvent.playProgress * 1000 >= trimmerRightHandlePosition && !stopRef.current)
              ) {
                stopRef.current = true;
                RNEditViewManager.pause();

                RNEditViewManager.seekToTime(trimmerLeftHandlePosition / 1000);
                scrollAniRef.setValue(0);
                setTimeout(() => {
                  stopRef.current = false;

                  RNEditViewManager.play();
                }, 500);
                return;
              }

              // setscrubberPosition(nativeEvent.playProgress * 1000 ?? 0)
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
  };
  // 滤镜组件
  const filterEditorFilter = () => {
    return (
      <View style={{ marginTop: 101, paddingHorizontal: 17 }}>
        {/* <Image style={{width:100,height:100,marginRight:4}}   source={{uri:require('../images/Aa.png')}} /> */}
        <FlatList
          data={filterList}
          initialNumToRender={4}
          horizontal={true}
          renderItem={({ index, item }) => {
            return (
              <View style={{ height: 130 }}>
                <TouchableOpacity
                  onPress={() => {
                    setfilterName(item?.filterName);
                  }}
                >
                  <Image style={{ width: 100, height: 100, marginRight: 4 }} source={{ uri: item.iconPath }} />
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
    // const {
    //   trimmerLeftHandlePosition,
    //   trimmerRightHandlePosition,
    //   scrubberPosition,
    //   videoTime
    // } = this.state;

    //     console.log();
    //     let  scrubberInterval = null
    //   // return null
    //   const  playScrubber = () => {
    //   // this.setState({ playing: true });

    //   scrubberInterval = setInterval(() => {
    //     this.setState({ scrubberPosition: this.state.scrubberPosition + 1 })
    //   }, 1000)
    // }

    // const  pauseScrubber = () => {
    //   clearInterval(scrubberInterval)

    //   this.setState({ scrubberPosition: this.state.trimmerLeftHandlePosition });
    // }
    //   const onStopPlay = async () => {
    //     audioRecorderPlayer.stopPlayer();
    //     audioRecorderPlayer.removePlayBackListener();
    //     setPlayType(0);
    // };

    //  await RNEditViewManager.pause()

    //  await RNEditViewManager.replay()
    //  await
    //  await RNEditViewManager.resume()
    const onHandleChange = async ({ leftPosition, rightPosition }) => {
      console.info('Bianhas');
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
      }, 500);
      console.info(leftPosition, rightPosition, 'leftPosition, rightPosition ');
      settrimmerLeftHandlePosition(leftPosition);
      settrimmerRightHandlePosition(rightPosition);
      setscrubberPosition(leftPosition);
    };

    // const onScrubbingComplete = (newValue) => {
    //   this.setState({  scrubberPosition: newValue })
    // }

    // 播放视频

    // const { ,trimmerLeftHandlePosition,trimmerRightHandlePosition} = this.state
    return (
      <>
        {/* <View style={{position:'absolute',bottom:200}}>
      <TouchableOpacity onPress={async()=>{
          // await RNEditViewManager.play()
      // playScrubber()
      }}>
        <Text style={{fontSize:20,color:"red"}}>播放</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={async()=>{ 
           await RNEditViewManager.pause()
          //  pauseScrubber()
           }}>
        <Text style={{fontSize:20,color:"red",marginTop:20}}>暂停</Text>
        </TouchableOpacity>
        </View> */}
        <View style={{ paddingHorizontal: 20, position: 'relative' }}>
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
              scrollAniRef.setValue(0);

              RNEditViewManager.pause();
            }}
            trackWidth={cropWidth}
            onLeftHandlePressIn={() => {
              stopRef.current = true;
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
    // const {coverList,videoTime} = this.state;
    // console.log('-----coverList',coverList);
    console.log('---this.state.videoPath', multipleSandBoxData);
    console.log('this.state.videoTime---', videoTime);

    // if( coverData && coverData.length < 1){
    //   getcoverData()
    // }
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
                    // this.setState({coverImage:item})
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
    // const {selectBottomModel} = this.state;
    return (
      <View
        style={{
          height: 60,
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
                // this.setState({ selectBottomModel: item })
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
  const result = () => {
    return (
      <Earlybird
        image={
          <SoftLightBlend
            // 画布
            resizeCanvasTo={'dstImage'}
            dstTransform={{
              scale: 'CONTAIN',
            }}
            dstImage={
              <Emboss
                image={
                  <Image
                    style={{ width: 320, height: 320 }}
                    // source={require('./parrot.png')}
                    source={{ uri: multipleSandBoxData[0] }}
                    resizeMode={'contain'}
                  />
                }
              />
            }
            srcTransform={{
              anchor: { x: 0.5, y: 1 },
              translate: { x: 0.5, y: 1 },
            }}
            srcImage={
              <Invert
                image={
                  <RadialGradient
                    colors={['rgba(0, 0, 255, 1)', '#00ff00', 'red']}
                    stops={[0.25, 0.75, 1]}
                    center={{ x: '50w', y: '100h' }}
                  />
                }
              />
            }
          />
        }
      />
    );
  };
  if (fileType == 'image') {
    return <View style={{ flex: 1, backgroundColor: '#000' }}>{result()}</View>;
  }

  return (
    <View style={{ flex: 1, backgroundColor: '#000' }}>
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