// import PropTypes from 'prop-types';
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
  Animated,
  FlatList,
  NativeModules,
} from 'react-native';
import _ from 'lodash';
import Camera from './Camera';
import VideoEditor from './VideoEditor';
import Carousel from 'react-native-snap-carousel';
// import * as Progress from 'react-native-progress';
import Toast, { DURATION } from 'react-native-easy-toast'


const { width, height } = Dimensions.get('window');
const CameraHeight = (height)
const { RNEditViewManager } = NativeModules;
export enum CameraType {
  Front = 'front',
  Back = 'back',
}

export type Props = {
  AaImage: any
  filterImage: any
  musicRevampImage: any
  giveUpImage: any
  noVolumeImage: any
  tailorImage: any
  volumeImage: any
  rephotograph: () => void;

  getUploadFile: (any) => void;
  
  goback: any
 // 视频路径
 filePaht: any, 
}

type State = {
  // 照片数据
  captureImages: any[],
  cameraType: CameraType,
  showBeautify: boolean,



  mute: boolean,
  showFilterLens: boolean,
  filterLensSelect: number,

 
// 滤镜名称
  filterName:any
  filterList:any
}


export default class StoryEditor extends Component<Props, State> {
  camera: any;
  myRef: any
  editor:any
  constructor(props) {
    super(props);
    this.myRef = React.createRef();
    this.state = {
      // 照片存储
      captureImages: [],
      cameraType: CameraType.Front,
      showBeautify: false,

      // 是否静音
      mute: false,

      showFilterLens: false,
      filterLensSelect: 0,
      // 视频 照片地址
      filterName:"柔柔",
      filterList:[]
    };
  }

     getFilters  = async() => {
    //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}
    if(this.state.filterList.length < 1){
      const infos = await RNEditViewManager.getFilterIcons({});
      this.setState({filterList:infos})
    }

  
  }
  componentDidMount() {
    this.getFilters()
    // console.log(123131);
      console.log('-------this.props.filePaht',this.props.filePaht);
      
  }

  // 底部 切换模块
  renderUploadStory() {
    const { captureImages } = this.state
    const {filePaht}  = this.props
    return (
      <View style={styles.BottomBox}>
        <>
          {/*  作品快拍 切换*/}
          
          {/* 发布 */}
       
            <TouchableOpacity onPress={() => {
              let uploadFile = []
              if (filePaht) {
                let type = filePaht.split('.')
                uploadFile.push({
                  video_type: `video/${type[type.length - 1]}`,
                  title_link: filePaht,
                  type: "file"
                })
              } else {
                let type = captureImages[0]?.uri.split('.')
                uploadFile.push({
                  image_url: captureImages[0]?.uri,
                  image_type: `image/${type[type.length - 1]}`,
                  image_size: captureImages[0]?.size,
                  type: "file",
                })
              }
              this.sendUploadFile(uploadFile)
            }}>
              <View style={styles.uploadBox}>
                <Text style={styles.uploadTitle}>发布快拍</Text>
              </View>
            </TouchableOpacity>
        </>
      </View>
    );
  }

  
  // 编辑头部按钮
  renderUpdateTop() {
    const imglist = [
      // 'filter': 
      { 'img': this.props.filterImage, 'onPress': () => { this.setState({ showFilterLens: !this.state.showFilterLens }) } },
      // 'volume':
      { 'img': this.state.mute ? this.props.noVolumeImage : this.props.volumeImage, 'onPress': () => { this.setState({ mute: !this.state.mute }) }, },
      // // 'tailor': 
      // { 'img': this.props.tailorImage, 'onPress': () => { } },
      // 'git':
      { 'img': this.props.musicRevampImage, 'onPress': () => { } },
      // 'Aa': 
      { 'img': this.props.AaImage, 'onPress': () => { } }
    ]
    return (
      <>
        {/* 放弃 */}
        <TouchableOpacity onPress={() => {
          this.setState({  showFilterLens: false, filterLensSelect: 0,captureImages: [] })
          this.props.rephotograph()
        }} style={[styles.UpdateBox, { left: 20 }]}>
          <Image
            style={styles.updateTopIcon}
            source={this.props.giveUpImage}
            resizeMode="contain"
          />
        </TouchableOpacity>
        {/* 编辑按钮组 */}
        <View style={[styles.UpdateBox, { right: 10, flexDirection: 'row' }]}>
          {
            imglist.map((item,index) => {
              return (
                <TouchableOpacity onPress={item.onPress} key={index}>
                  <Image
                    style={styles.updateTopIcon}
                    source={item.img}
                    resizeMode="contain"
                  />
                </TouchableOpacity>
              )
            })
          }
        </View>
      </>
    )
  }

  // 拍摄进度
  _onRecordingDuration(event) {
    // console.log('duration: ', event.duration);
  }

  // 拍摄内容渲染
  renderCamera() {
     function  onExportVideo(event) {
      if (event.exportProgress === 1) {
        // this.setState({ startExportVideo: false });
       console.log('视频导出成功, path = ', event.outputPath);
      }
    }
    const  VideoEditors =()=>{
      return (
      <View style={{height:'100%',backgroundColor:'#fff',borderRadius:20}}>
    <VideoEditor
        ref={(edit) => (this.editor = edit)}
        style={{height:CameraHeight,justifyContent:'flex-end' }}
        filterName={this.state.filterName}
        videoPath={this.props.filePaht}
        saveToPhotoLibrary={true}
        startExportVideo={false}
        videoMute={this.state.mute}
        onExportVideo={onExportVideo}
      />
   </View>
      )
    }
    return (
      <View style={[styles.cameraContainer]}>
          <TouchableOpacity style={{ flex: 1, justifyContent:'flex-end', position: "relative" }}
            onPress={() => {
              this.setState({ showFilterLens: false,})
              // !this.state.showFilterLens 
            }}
            activeOpacity={1}
            disabled={this.state.showBeautify}
          >
             { VideoEditors()}
          {this.renderUpdateTop() }
          </TouchableOpacity>
           
      </View>
    );
  }

  sendUploadFile(data) {
    if (this.props.getUploadFile) {
      this.props.getUploadFile(data);
    }
  }
  // 美颜 滤镜 box
  renderFilterBox() {
    const list = [
      { title: "原片" },
      { title: "灰白" },
      { title: "柔柔" },
      { title: "波普" },
      { title: "胶片" },

    ]
    return (
      <View style={{ height: 189, backgroundColor: "#000" }}>
        <View style={styles.beautifyBoxHead}>
          <Text style={styles.beautifyTitle}>{`滤镜`}</Text>
          
        </View>
        {this.state.showFilterLens
          ?
          <View style={{ paddingHorizontal: 20 }}>
            <FlatList
              data={this.state.filterList}
              horizontal={true}
              style={{ margin: 0, padding: 0, height: 80 }}
              renderItem={({ index, item }) => {
                return (
                  <>
                    <TouchableOpacity onPress={() => {
                      this.setState({ filterLensSelect: index ,filterName:item.filterName})
                    }}>
                      <View style={{ flexDirection: 'column', justifyContent: 'center', alignItems: 'center', marginRight: 20 }}>
                        <Image style={[styles.beautifySelect,
                        this.state.filterLensSelect === index && styles.beautifySelecin
                        ]}
                        source={{uri:item.iconPath}}
                        />
                          {/* <Image
              style={styles.beautyAdjustIcon}
              source={{uri:item.iconPath}}
              resizeMode="contain"
            />
                        </View> */}
                        <Text style={[styles.filterLensSelectTitle,
                        this.state.filterLensSelect === index && { color: '#836BFF' }
                        ]}>{item.filterName}</Text>
                      </View>
                    </TouchableOpacity>
                  </>
                )
              }}
            />
          </View>
          :
          null
        }

      </View >
    )
  }

  // 底部渲染
  renderBottom() {
    if ( this.state.showFilterLens) {
      return (
        this.renderFilterBox()
      )
    }
    return (
      <>
        <View style={{ height: 125, backgroundColor: "#000", justifyContent:'center',alignContent:'center'}}>
          {this.renderUploadStory()}
        </View>
      </>
    )
  }


  render() {
    return (
      <>
        <Toast
          ref={this.myRef}
          position="center"
          positionValue={70}
          fadeInDuration={750}
          fadeOutDuration={1000}
          opacity={0.8}
        /> 
     <>
              {/* story */}
                {Platform.OS === 'android' && this.renderCamera()}
                {Platform.OS !== 'android' && this.renderCamera()}
                {Platform.OS === 'android' && <View style={styles.gap} />}
              {this.renderBottom()}
            </>
      </>
    );
  }
}

const styles = StyleSheet.create(
  {
  
   
    
    BottomBox: {
      flex: 1,
      flexDirection: 'row',
      justifyContent: 'center',
      alignItems: "center",
      position: 'relative'
    },

    cameraContainer: {
      ...Platform.select({
        android: {
          position: 'absolute',
          top: 0,
          left: 0,
          width,
          height,
        },
        default: {
          flex: 1,
          // height:400,
          flexDirection: 'column',
        },
      }),
    },
    bottomButton: {
      flex: 1,
      flexDirection: 'row',
      alignItems: 'center',
      padding: 10,
    },
    beautifyBoxHead: {
      paddingHorizontal: 20,
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: 'center',
      paddingTop: 15,
      paddingBottom: 26,
    },
    beautifyTitle: {
      fontSize: 15,
      fontWeight: '500',
      color: "#fff",
      lineHeight: 21
    },
    beautyAdjustIcon: {
      width: 20,
      height: 16
    },
    beautifyBoxContent: {
      flexDirection: "row",
      justifyContent: "space-around",
    },
    beautifySelect: {
      width: 48,
      height: 48,
      backgroundColor: " rgba(69, 69, 73, 0.7)",
      borderRadius: 9,
      justifyContent: 'center',
      alignItems: 'center',

    },

    beautifySelecin: {
      borderWidth: 2,
      borderColor: "#836BFF"
    },
    uploadBox: {
      width: 130,
      height: 40,
      borderRadius: 22,
      backgroundColor: "#fff",
      justifyContent: 'center',
      alignItems: 'center'
    },
    uploadTitle: {
      fontWeight: '500',
      fontSize: 13,
      color: "#000",
      lineHeight: 18
    },
    UpdateBox: {
      position: 'absolute',
      zIndex: 99,
      top: 40,
    },
    updateTopIcon: {
      width: 40,
      height: 40,
      marginRight: 10
    },
    filterLensSelectTitle: {
      fontSize: 13,
      fontWeight: "500",
      color: '#fff',
      lineHeight: 18,
    },
  });

