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
  Pressable,
  Animated,
  ScrollView,
  NativeEventEmitter,
  Platform
} from 'react-native';
import _ from 'lodash';
import Toast, { DURATION } from 'react-native-easy-toast';
import CameraRoll from '@react-native-community/cameraroll';
import { FlatGrid } from 'react-native-super-grid';
import Video from 'react-native-video';
import Carousel from 'react-native-snap-carousel';
import Trimmer from './react-native-trimmer';
import VideoEditor from './VideoEditor';
import AVService from './AVService';
import { Grayscale, Temperature, Sepia } from 'react-native-image-filter-kit';

// let a  = require('../images/postEditorNoMute.png');

const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2;
const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
const photosItem = width / 4;
const cropWidth = width - 30 * 2;
const PostHead = React.memo((props)=> {
  const {videoMute, setvideoMute} =props

  const {closePng, volumeImage, noVolumeImage, goback, continueEdit, continueRef} = props
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
          if (continueRef.current) return
         goback();
        }}
        style={{
          height: 30, 
          width: 40,
          paddingHorizontal: 12,
        
          justifyContent:"center",
        }}
      >
        <Image style={styles.closeIcon} source={require("../images/backArrow.png")} resizeMode='contain' />
      </Pressable>
      {props.fileType === 'video'?(
  <TouchableOpacity
  onPress={() => {
    setvideoMute(!videoMute);
  }}
>
  <Image style={{ width: 30, height: 21 }} source={!videoMute ? volumeImage : noVolumeImage} />
</TouchableOpacity>
      ):null}
    

      <Pressable
        onPress={continueEdit}
        style={{
          height: 30, 
          paddingHorizontal: 12,
        
          justifyContent:"center",
          alignItems:"flex-end"
        }}
      >
        <Text style={styles.continueText}>继续</Text>
      </Pressable>
    </View>
  );
})
const PostEditor = (props) => {
  // const {params:{fileType='',trimVideoData="",trimmerRight="",videoduration=''}} = props;
  const {
    
    params: { trimVideoData = '', fileType = '' },

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
  const aniRef = useRef(null);
  const toast = useRef();

  const [imgfilterName, setImgFilterName] = useState('');
  const stopRef = useRef(false);
  const startRef = useRef(false);
  const lockRef = useRef(false);
  const continueRef = useRef(false)
  const [photoFile, setPhotoFile] = useState('');
  const outputPathRef = useRef(null);

  const continueEdit = async () => {
    if (continueRef.current) return 
    continueRef.current = true
    const cropData = props.params.cropDataResult

    if (fileType === 'image') {
      try {

   
      const path = photoFile
      let uploadFile = [];
      uploadFile.push({
        Type: `image/png`,
        path: path,
        size: 0,
        Name: path,
        coverImage: path,
      });
      props.getUploadFile(uploadFile);
      props.goback()
  
    }catch (e) {
        console.info(e, '错误')
        setTimeout(() => {
          continueRef.current = false
  
        }, 1500);
      }
    } else {
      // 裁剪视频
      console.info(toast.current, 'asasasas')
      toast.current.show('正在导出, 请不要离开', 0);
    
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
  }
  let coverData = [];
  let editor = null;
  let scrubberInterval = null;
  const getFilters = async () => {
    //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}

    const infos = await AVService.getFilterIcons({});

    infos.unshift({ filterName: null, iconPath: '', title: '无效果' });
    setfilterList(infos);
  };

  useEffect(() => {
    getFilters();
    const {
       params 
    } = props;

    if (!params) return null
    setmultipleSandBoxData([params?.trimVideoData]);
    setVideoTime(params?.videoduration);
    settrimmerRightHandlePosition(params?.trimmerRight);
  }, [props.params]);
  useEffect(() => {
    const managerEmitter = new NativeEventEmitter(AliAVServiceBridge);
    const subscription = managerEmitter.addListener('cropProgress', (reminder) => {
      console.log(reminder);

      if (reminder.progress == 1 && fileType === 'video') {
        // 可以再这里做loading
        toast.current.close();

      }
  
    });
    return () => {
      console.info('销毁了', subscription);
      AVService.removeThumbnaiImages();

      //TODO
      if(Platform.OS==='ios'){
        RNEditViewManager.stop();
      }else{

      }
     
      props.params?.playVideo?.();
      managerEmitter.removeAllListeners('cropProgress')
    };
  }, []);
  const getcoverData = async () => {
    try {
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
    } catch (e) {
      console.info(e)
    }

  };
  useEffect(() => {
    if (multipleSandBoxData.length > 0) {
      getcoverData();
    }
  }, [multipleSandBoxData]);

  const onExportVideo = async(event) => {
    try{

  
    if (event.exportProgress === 1) {
      const cropData = props.params.cropDataResult
      let outputPath = event.outputPath;
      const Wscale = 1080 / props.params.cropDataRow.srcSize.width
      const Hscale = 1920 / props.params.cropDataRow.srcSize.height
  
      outputPath = await AVService.crop({
        source: `file://${outputPath}`,
        cropOffsetX: cropData.offset.x,
        cropOffsetY: cropData.offset.y * Hscale,
        cropWidth: cropData.size.width * Wscale,
        cropHeight: cropData.size.height * Wscale,
        duration: (trimmerRightHandlePosition - trimmerLeftHandlePosition) / 1000,
      });
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
      setTimeout(() => {
        props.goback()
      }, 0);
    }
  } catch (e) {
    console.info(e)
  }
  };

  const postEditorViewData = () => {
    const delta = trimmerRightHandlePosition - trimmerLeftHandlePosition;
    const top =props.params.cropDataRow.positionY

    const width1 =props.params.cropDataRow.fittedSize.width
    const height1 =props.params.cropDataRow.fittedSize.height
    return (
      <View style={{
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor:"black",
        width: width,
        height: width,
        overflow:"hidden"
      }}>
          <View style={{
          width: width1,
          height: height1,
            transform:[{
              translateY:top
            }]
          }}
          >
        <VideoEditor
          // editWidth={width1}
          // editHeight={height1}
          editStyle={{
            width:width1,
            height: height1
          }}
          ref={(edit) => (editor = edit)}
          filterName={filterName}
          //TODO
          videoPath={multipleSandBoxData[0]??props.params.trimVideoData}
          saveToPhotoLibrary={false}
          startExportVideo={exportVideo}
          videoMute={videoMute}
          onExportVideo={(event) => {
            onExportVideo(event);
          }}
          onPlayProgress={({ nativeEvent }) => {
            if (nativeEvent.playProgress * 1000 >= trimmerLeftHandlePosition && !startRef.current && !lockRef.current) {
              startRef.current = true;

              aniRef.current = Animated.timing(
                // 随时间变化而执行动画
                scrollAniRef, // 动画中的变量值
                {
                  toValue: Math.min(delta / videoTime, 1) * cropWidth, // 透明度最终变为1，即完全不透明
                  duration: delta, // 让动画持续一段时间
                  useNativeDriver: true,
                },
              );
              aniRef.current.start();
            }
            if (fileType === 'video') {
              if (
                nativeEvent.playProgress === undefined ||
                (nativeEvent.playProgress * 1000 >= trimmerRightHandlePosition && !stopRef.current && !lockRef.current)
              ) {
                stopRef.current = true;
                startRef.current = false;

                aniRef.current.stop();
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
      </View>
    );

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
                    if (continueRef.current) return
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
        lockRef.current = false;
        RNEditViewManager.play();
        stopRef.current = false;
        startRef.current = false;
      }, 500);
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
              if (continueRef.current) return

              stopRef.current = true;
              startRef.current = false;``
              lockRef.current = true;
              aniRef.current.stop();

              scrollAniRef.setValue(0);

              RNEditViewManager.pause();
            }}
            trackWidth={cropWidth}
            onLeftHandlePressIn={() => {
              if (continueRef.current) return

              aniRef.current.stop();
              lockRef.current = true;

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
    const left=  props.params.cropDataRow.positionX
    const top =props.params.cropDataRow.positionY
    const scale = props.params.cropDataRow.scale
   
    const Extractor = (imgFilter) => {
      // const width =props.params.cropDataRow.fittedSize.width
      // const height =props.params.cropDataRow.fittedSize.height
      const ImageComponent =   <Image
      style={{ width: width, height: width ,
        // transform:[
        // {
        //   scale:scale
        // },
        //    {translateX:left},
  
        //     {translateY:top},
            
        //   ]
         }}
      source={{ uri: multipleSandBoxData[0] }}
    />
      switch (imgFilter) {
        case 'Sepia': {
          return (
            <Sepia
              image={
            
                ImageComponent
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
                ImageComponent
              }
            />
          );
        }
        case 'Sepia2': {
          return (
            <Sepia
              amount={0.4}
              image={
                ImageComponent
              }
            />
          );
        }
        default: {
          return ImageComponent
          
        }
      }
    };

    return (
      <>
        <View style={{width: width, height:width, overflow:"hidden"}}>
          
        <View style={{
          // overflow: 'hidden',
          // alignItems: 'center',
          // justifyContent: 'center',
        
          }}>
        <Grayscale
          amount={0}
          onExtractImage={({ nativeEvent }) => {
            setPhotoFile(nativeEvent.uri);
          }}
          extractImageEnabled={true}
          image={Extractor(imgfilterName)}
        ></Grayscale>
        </View>
        </View>
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
    return (
      <View style={{ backgroundColor: 'black' ,position: 'relative',height:"100%"}}>
        <PostHead {...props} continueEdit={continueEdit} videoMute={videoMute} setvideoMute={setvideoMute} continueRef={continueRef}/>
  
      {result()}
    

      </View>)
  }
  return (
    <View style={{  backgroundColor: 'black', position: 'relative',height:"100%"}}>
      <Toast
        ref={toast}
        position='top'
        positionValue={300}
        fadeInDuration={1050}
        fadeOutDuration={800}
        opacity={0.8}
      />
        <PostHead {...props} continueEdit={continueEdit} continueRef={continueRef} videoMute={videoMute} setvideoMute={setvideoMute}/>
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
  closeIcon: {
    width:12,
    height: 20,
  },
  postSwitchProps: {
    fontSize: 16,
    color: '#8E8E8E',
    fontWeight: '500',
  },
  continueText: {
    fontSize: 15, 
    fontWeight: '400',
    color: '#fff', 
    lineHeight: 21 
  },
  textCenter: {
    fontSize: 17, 
    fontWeight: '500',
    color: '#fff', 
    lineHeight: 24 
  }
});

export default PostEditor;
