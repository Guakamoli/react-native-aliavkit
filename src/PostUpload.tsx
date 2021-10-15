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
  ScrollView,
  FlatList,
  NativeModules
} from 'react-native';
import _ from 'lodash';
import Toast, { DURATION } from 'react-native-easy-toast'
import CameraRoll from "@react-native-community/cameraroll";
import { FlatGrid } from 'react-native-super-grid';
import Video from 'react-native-video';
import Carousel from 'react-native-snap-carousel';
import Trimmer from 'react-native-trimmer'


const { width, height } = Dimensions.get('window');
const captureIcon = (width - 98) / 2
const captureIcon2 = (width - 20) / 2;
const photosItem = (width / 4);


const { RNEditViewManager } = NativeModules;
export type Props = {
  ratioOverlay?: string,
  closeImage: any,
  goback: any

  multipleBtnImage: any
  postCameraImage: any
  startMultipleBtnImage: any
  changeSizeImage: any
  addPhotoBtnPng:any
  postMutePng:any
  postNoMutePng:any
  getUploadFile: (any) => void;
}

type State = {

  CameraRollList: any,

  fileSelectType: string
  multipleData: any
  startmMltiple: boolean
  scrollViewWidth: boolean
  photoAlbum: any
  photoAlbumselect: any
  // pasterList: any
  videoFile: any
  // facePasterInfo: any
  // filterName:any
  fileEditor:Boolean
  // 滤镜
  filterName:string
  filterList:Array<any>
  aa:Boolean
  videoMute:Boolean
  selectBottomModel:string

  // 2
  playing: boolean,
  trimmerLeftHandlePosition: any
  trimmerRightHandlePosition: any,
  scrubberPosition: any
}
const maxTrimDuration = 60000;
const minimumTrimDuration = 1000;
const totalDuration = 180000

const initialLeftHandlePosition = 0;
const initialRightHandlePosition = 36000;

const scrubInterval = 50;

export default class CameraScreen extends Component<Props, State> {
  camera: any;
  myRef: any
  editor:any

  constructor(props) {
    super(props);
    this.myRef = React.createRef();

    this.state = {
      CameraRollList: [],
      fileSelectType: '',
      multipleData: [],

      startmMltiple: false,
      photoAlbum: [],

      photoAlbumselect: {},
      videoFile: '',
      scrollViewWidth: true,
      // 
      // pasterList: [],
      // facePasterInfo: {},
      // filterName:"原片"

      // 修改页面
      fileEditor:false,
      // 滤镜
      filterName: '',
      filterList:[],
      aa:true,
      videoMute:false,
      selectBottomModel:'滤镜',

      //22
      playing: false,
    trimmerLeftHandlePosition: initialLeftHandlePosition,
    trimmerRightHandlePosition: initialRightHandlePosition,
    scrubberPosition: 1000
    };
  }
  getFilters  = async() => {
    //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}
    if(this.state.filterList.length < 1){
      const infos = await RNEditViewManager.getFilterIcons({});
      // console.log('------infos',infos);
      
      this.setState({filterList:infos})
    }
  }
  componentDidMount() {
    //获取照片
    var getPhotos = CameraRoll.getPhotos({
      first: 30,
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
      // console.log('andor',data);
      
      var photos = [];
      for (var i in edges) {
        // ios文件
        photos.push(edges[i].node);
      }
      this.setState({
        CameraRollList: photos
      });
    }, function (err) {
      // alert( '获取照片失败！' );
    });
    // 滤镜
    this.getFilters()
  }
  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }
  postHead() {
    const {fileEditor,multipleData,fileSelectType,videoMute} = this.state
    return (
      <View style={{ height: 44, backgroundColor: '#000', flexDirection: "row", justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 12 }}>
        <TouchableOpacity onPress={() => {
          fileEditor ? this.setState({fileEditor:false,multipleData:[],selectBottomModel:'滤镜',startmMltiple:false}) : this.props.goback()
        }} >
          <Image
            style={styles.closeIcon}
            source={this.props.closeImage}
            resizeMode="contain"
          />
        </TouchableOpacity>
        {fileEditor ?  
        fileSelectType === 'video' && 
        <TouchableOpacity onPress={()=>{this.setState({videoMute : !videoMute})}}>
        <Image  style={{width:18,height:17}} source={ !videoMute ? this.props.postMutePng : this.props.postNoMutePng}/>
        </TouchableOpacity>

        :   <Text style={{ fontSize: 17, fontWeight: '500', color: "#fff", lineHeight: 24 }}>新作品</Text>}
       
        <TouchableOpacity onPress={() => {
          // 进入修改
          multipleData.length < 1 ? this.myRef.current.show('请至少选择一个上传文件', 2000)  :  this.setState({fileEditor:true})
        

          // 发送选择的数据
          // let uplaodFile = []
          // console.log('this.state.multipleData', this.state.multipleData);
          // if (this.state.multipleData.length > 0) {
          //   this.state.multipleData.map(async (multipleDataItem) => {
          //     const { image: { uri, width, height, filename, fileSize, playableDuration }, type } = multipleDataItem
          //     let image_type = type + '/' + filename.split('.')[1]
          //     let localUri = await CameraRoll.requestPhotoAccess(uri.slice(5));
          //     if (this.state.fileSelectType === 'image') {
          //       uplaodFile.push({
          //         image_type,
          //         image_dimensions: { width, height },
          //         image_url: localUri,
          //         image_size: fileSize,
          //         title: filename
          //       })
          //     } else {
          //       uplaodFile.push({
          //         video_type: image_type,
          //         type: "file",
          //         title_link: localUri,
          //         video_size: fileSize,
          //         title: filename
          //       })
          //     }
          //   })

          // }
          // // 选择本地文件 数据
          // this.sendUploadFile(uplaodFile)
          
        }}>
          <Text style={{ fontSize: 15, fontWeight: '400', color: "#fff", lineHeight: 21 }}>继续</Text>
        </TouchableOpacity>
      </View>
    )
  }
  postContent() {
    const { multipleData, CameraRollList, fileSelectType, videoFile, } = this.state;

    return (
      <SafeAreaView style={{ flex: 1, padding: 0, justifyContent: 'center', alignItems: 'center', backgroundColor: '#ececec', position: 'relative' }}>

        <TouchableOpacity style={{
          width: 31,
          height: 31, marginRight: 10, position: 'absolute', left: 15, bottom: 20, zIndex: 99
        }} onPress={() => {
          this.setState({ scrollViewWidth: !this.state.scrollViewWidth })
        }}>
          <Image
            style={[{
              width: 31,
              height: 31,
            }]}
            source={this.props.changeSizeImage}

          />
        </TouchableOpacity>
        <ScrollView style={{
          height: 'auto',
          margin: 'auto',
          paddingHorizontal: 0,
          backgroundColor: '#ececec',
          width: this.state.scrollViewWidth ? width : 320
        }}
          pinchGestureEnabled={true}
          onScroll = {(event)=>{{
            console.log(event.nativeEvent.contentOffset.x);//水平滚动距离
            console.log(event.nativeEvent.contentOffset.y);//垂直滚动距离 
          }}}
        >
          {
            fileSelectType === 'image' ? <Image
              style={[{
                width: width,
                height: height - 300,
              },]}
              // 安卓展示不出来 权限问题？？？？ 
              // source={{ uri: item.image.uri }}
              source={{ uri: (multipleData.length > 0 ? multipleData[multipleData.length - 1]?.image?.uri : CameraRollList[0]?.image?.uri) }}
            /> :
              <Video
                source={{ uri: videoFile }}
                style={{
                  width: width,
                  height: height - 160,
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
    const { CameraRollList, multipleData, startmMltiple, fileSelectType } = this.state;

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
    const getVideFile = async (fileType, item) => {
      if (fileType !== 'video') return ''
      let myAssetId = item?.image?.uri.slice(5);
      // 获取视频文件 url 
      console.log(myAssetId, 'myAssetId');

      let localUri = await CameraRoll.requestPhotoAccess(myAssetId);
      this.setState({ videoFile: localUri })
    }
    return (
      <>
       <Toast
          ref={this.myRef}
          position="top"
          positionValue={300}
          fadeInDuration={1050}
          fadeOutDuration={800}
          opacity={0.8}
        />
        {this.postFileUploadHead()}
        <View style={[{ height: 291, backgroundColor: '#000', }, Platform.OS === 'android' ? { paddingBottom: 10 } : { paddingBottom: 35 }]}>
          <FlatGrid
            itemDimension={photosItem}
            data={CameraRollList}
            spacing={0}
            itemContainerStyle={{ margin: 0 }}
            renderItem={({ index, item }) => {
              const { type, image, } = item;
              const { fileSelectType, startmMltiple } = this.state
              // const a =timestamp
              return (
                <TouchableOpacity onPress={() => {
                  //  第一次
                  if (multipleData.length <= 1) {
                    // 获取第一次选择类型
                    let fileType = type.split('/')[0];
                    if (fileType === 'video') {
                      getVideFile(fileType, item)
                    }
                    this.setState({
                      fileSelectType: fileType,
                      multipleData: [item]
                    })

                  }
                  if (startmMltiple) {
                    // 图片大于10 || 视频 大于 1 
                    if (fileSelectType == 'image') {
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
                  disabled={!(type.indexOf(fileSelectType) !== -1) && startmMltiple}
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

                      }, !(type.indexOf(fileSelectType) !== -1) && startmMltiple ? { opacity: 0.4 } : {}]}
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
                      image.playableDuration ? <Text style={{ color: '#fff', fontSize: 12, fontWeight: '400', lineHeight: 17, zIndex: 100, position: "absolute", right: 8, bottom: 7 }}> {formatSeconds(Math.ceil(image.playableDuration ?? 0))}</Text> : null
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

  postEditorViewData(){
    const data =  [1,3,4]
    // let aa = true
    const {multipleData}  = this.state
    // console.log('data.length',data.length);
    const onlyOne = multipleData.length === 1
    if(onlyOne){
     return (
      <View style={[{height:395,width:width,}]}>
      <Image   source ={multipleData[0].image} style={{width:'100%',height:'100%'}} / >
      </View>
     )
    }
    return(
      <View style={{height:375,paddingHorizontal:0}}>
        
      <Carousel 
      data={multipleData}
      // containerCustomStyle={{backgroundColor:'green',}}
      itemWidth={320}
      sliderWidth={width}
      enableSnap={this.state.aa}
      onBeforeSnapToItem={(slideIndex = 0) => {
        console.log('slideIndex',slideIndex,'multipleData.length',multipleData.length-1);
        
        if(slideIndex === multipleData.length -1){
          console.log('aa:false');
        this.setState({aa:false})
        }else{
          console.log('aa:true');
          this.setState({aa:true})
        }
      }}
      ListFooterComponent={()=>{
        if(onlyOne) null
        return (
          <View style={{width:83,height:319,flexDirection:'row',alignItems:'center',marginTop:40}}>
            
          <TouchableOpacity  style={{}} onPress={()=>{
            this.setState({fileEditor:false,startmMltiple:true})
          }}>
             <Image
           style={[{
             width: 83,
             height: 83,
             marginHorizontal:34
           }]}
           source={this.props.addPhotoBtnPng}
         />
          </TouchableOpacity>
          </View>
        )
      }}
      renderItem={({index,item})=>{
        // console.log(item);
        const finall = (index == multipleData.length -1 && multipleData.length > 1)
        
        return (
          <>
          {/* <View style={{flexDirection:'row',alignItems:'center'}}> */}
          <View style={[{height:319,width:319,marginTop:40},
            // ,finall && {width:214}
            
            ]}>
            <Image   source ={item.image} style={{width:'100%',height:'100%'}} / >
            </View>
           {/* {finall && 
          
           } */}
           {/* </View> */}
          </> 
        )
      }}
      />
      
    </View>
    )
  }
  // 滤镜组件
  filterEditorFilter(){
    return (
      <View style={{marginTop:101,paddingHorizontal:17}}>
         <FlatList
          data={this.state.filterList}
          initialNumToRender={4}
          horizontal={true}
          renderItem={({ index, item })=>{
            return(
              <View  style={{height:130}}>
            
              <TouchableOpacity>
              <Image style={{width:100,height:100,backgroundColor:'green',marginRight:4}}   source={{uri:item.iconPath}} />
              </TouchableOpacity>
                </View>
            )
          }}
        />
      </View>
    )
  }

  // playScrubber = () => {
  //   this.setState({ playing: true });

  //   this.scrubberInterval = setInterval(() => {
  //     this.setState({ scrubberPosition: this.state.scrubberPosition + scrubInterval })
  //   }, scrubInterval)
  // }

  // pauseScrubber = () => {
  //   clearInterval(this.scrubberInterval)

  //   this.setState({ playing: false, scrubberPosition: this.state.trimmerLeftHandlePosition });
  // }

  // onHandleChange = ({ leftPosition, rightPosition }) => {
  //   this.setState({
  //     trimmerRightHandlePosition: rightPosition,
  //     trimmerLeftHandlePosition: leftPosition
  //   })
  // }

  // onScrubbingComplete = (newValue) => {
  //   this.setState({ playing: false, scrubberPosition: newValue })
  // }
  // 裁剪
  postTrimer(){
    const {
      trimmerLeftHandlePosition,
      trimmerRightHandlePosition,
      scrubberPosition,
      playing,
    } = this.state;
  
    return (
      
    <View style={{marginTop:160,paddingHorizontal:20}}>
     {/* <Trimmer
          onHandleChange={this.onHandleChange}
          totalDuration={totalDuration}
          trimmerLeftHandlePosition={trimmerLeftHandlePosition}
          trimmerRightHandlePosition={trimmerRightHandlePosition}
          minimumTrimDuration={minimumTrimDuration}
          maxTrimDuration={maxTrimDuration}
          maximumZoomLevel={200}
          zoomMultiplier={20}
          initialZoomValue={2}
          scaleInOnInit={true}
          tintColor="#333"
          markerColor="#5a3d5c"
          trackBackgroundColor="#382039"
          trackBorderColor="#5a3d5c"
          scrubberColor="#b7e778"
          scrubberPosition={scrubberPosition}
          onScrubbingComplete={this.onScrubbingComplete}
          onLeftHandlePressIn={() => console.log('onLeftHandlePressIn')}
          onRightHandlePressIn={() => console.log('onRightHandlePressIn')}
          onScrubberPressIn={() => console.log('onScrubberPressIn')}
        /> */}
    
    </View>


    )
  }
  // 封面
  postCover(){
    return(
      <View style={{marginTop:93,paddingHorizontal:17}}>
      <FlatList
          data={[1,2,3,4,5,6,7]}
          initialNumToRender={7}
          horizontal={true}
          renderItem={({ index, item })=>{
            return(
              <View  style={{height:130}}>
              <TouchableOpacity>
                <Image source={{ uri: 'https://guakamoli1-video-message-dev.oss-cn-qingdao.aliyuncs.com/default/3494e33ecbbb5b955a1c84bd6b8a0626/116c7efd-96bc-46ca-92d7-3008f32c09c5.jpg' }} style={{width:65,height:74,backgroundColor:'green',marginRight:4}}  />
              </TouchableOpacity>
              </View>
            )
          }}
        />
        </View>
    )
  }
  // 切换底部功能
  switchProps(){
    const switchProps =['滤镜','修剪','封面'];
    const {selectBottomModel} = this.state;
    return(
      <View style={{height:60, width:width,flexDirection:"row",justifyContent:'space-evenly',alignItems:'flex-start',position:"absolute",bottom:0}}>
        {switchProps.map(item => {
              return (
                <TouchableOpacity onPress={() => {
                  this.setState({ selectBottomModel: item })
                }}
                >
                  <Text style={[styles.postSwitchProps,selectBottomModel === item && {color:"#fff"} ]}>{item}</Text>
                </TouchableOpacity>
              )
            })}

      </View>
    )
  }
  photoEditorContent(){
    const {selectBottomModel,fileSelectType} = this.state;
   
    return (
      <View style={{flex:1,backgroundColor:'#000',position:'relative'}}>
        {this.postEditorViewData()}
        {/* <View style={{marginTop:100}}> */}
        {
          selectBottomModel  === '滤镜' && this.filterEditorFilter()
        }
        {
          selectBottomModel  === '修剪' && this.postTrimer()
        }
        {
          selectBottomModel  === '封面' && this.postCover()
        }
        
        {
          fileSelectType  != 'image' && this.switchProps()
        }
{/* </View> */}
        {}
      </View>
    )
  }
  render() {
    const {fileEditor} = this.state
    return (
      <>
        {/* {Platform.OS !== 'android' ? <View style={{ height: 44, backgroundColor: "#000" }}></View> : null} */}
        {
         
              <>
                {/* post */}
                {this.postHead()}
                {fileEditor ? 
             
               this.photoEditorContent()
                :
                <>
                 { this.postContent()}
                {this.postFileUpload()}
               </>
                }
              
               {/* {this.photoEditorContent()} */}
              </>
            
        }
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
 
    captureButton: {
      width: 49,
      height: 49,
      backgroundColor: "#fff",
      borderRadius: 49,
      position: 'absolute'
    },
    captureButtonImage: {
      position: 'absolute',
      left: captureIcon,
      zIndex: -11,
      elevation: 1,
      top: - 7,
    },
    multipleBtnImage: {
      width: 31,
      height: 31
    },
    postSwitchProps:{
      fontSize:16,color:'#8E8E8E',fontWeight:"500",
    },
  });
