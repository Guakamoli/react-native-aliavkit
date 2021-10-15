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
  TextInput
} from 'react-native';

import _ from 'lodash';
import Toast, { DURATION } from 'react-native-easy-toast'
import CameraRoll from "@react-native-community/cameraroll";
import { FlatGrid } from 'react-native-super-grid';
import Video from 'react-native-video';
import Carousel from 'react-native-snap-carousel';
import Trimmer from 'react-native-trimmer'
const { width, height } = Dimensions.get('window');
const StoryMusic = (props) => { 
  const [musicSelect,setMusicSelect] = useState(1)
  const [musicChoice,setMmusicChoice] = useState(false)
 const  musicCarousel  = () => {
    return (
      <Carousel
     data={[1,3,1,5,4,5]}
     itemWidth={298}
     sliderWidth={width}
     initialNumToRender={4}
     // firstItem={this.state.currentIndex}
    //  onBeforeSnapToItem={(slideIndex = 0) => {
    //    // this.setState({
    //    //   currentIndex: slideIndex,
    //    //   facePasterInfo: pasterList[slideIndex]
    //    // })
    //  }}

      renderItem={({ index, item }) => {
       
       return (
         <View style={[{width:298,height:85,backgroundColor:"rgba(255,255,255,0.2)",borderRadius:15,marginVertical:16,padding:14},musicSelect == item &&{ backgroundColor:"rgba(255,255,255,0.95)"}]}>
           <View style={{flexDirection:"row",justifyContent:"space-between",marginBottom:14}}> 
           <Image source={{ uri: 'https://guakamoli1-video-message-dev.oss-cn-qingdao.aliyuncs.com/default/3494e33ecbbb5b955a1c84bd6b8a0626/116c7efd-96bc-46ca-92d7-3008f32c09c5.jpg' }} style={{ width: 18,height:18 }} />
           <Image source={{ uri: 'https://guakamoli1-video-message-dev.oss-cn-qingdao.aliyuncs.com/default/3494e33ecbbb5b955a1c84bd6b8a0626/116c7efd-96bc-46ca-92d7-3008f32c09c5.jpg' }} style={{ width: 30,height:18 }} />
           </View>
           <View>
             <Text>
               I got a pony tail I
             </Text>
           </View>
         </View>
       )
      }}     
    />
    )
  }
  const  findMusic = ()=>{
    return ( 
      <View style={{height:571,backgroundColor:"pink"}}> 
      <View style={styles.findMusicHead}>
        <Text>取消</Text>
        <Text>背景音乐</Text>
        <Text> 完成</Text>
      </View>
      <View style={styles.searchMusic}>
      </View>
      <FlatList 
      data={[1,2,3,4,5,6,7,8,9]}
      renderItem={()=>{
        return (
          <View style={{width:width-30,height:84,backgroundColor:'rgba(0, 0, 0, 0.8)',marginTop:20,borderRadius:15,padding:15,marginHorizontal:15}}> 
          <View style={{flexDirection:'row',marginBottom:10,alignItems:'center'}}>
          <Image source={{ uri: 'https://guakamoli1-video-message-dev.oss-cn-qingdao.aliyuncs.com/default/3494e33ecbbb5b955a1c84bd6b8a0626/116c7efd-96bc-46ca-92d7-3008f32c09c5.jpg' }} style={{ width: 19,height:19}} />
          <Text style={{fontWeight:'400',fontSize:16 ,color:'#fff',marginLeft:15,lineHeight:21,}}>明天，你好-牛奶咖啡</Text>

          </View>
          <Text style={{fontWeight:'400',color:"#a6a5a2",fontSize:15,lineHeight:21,}}>长大以后我只能奔跑 我多害怕黑暗中跌倒</Text>
            </View>
        )
      }}
      />
      </View>
    )
  }
return (
  <View style={{backgroundColor:"#000"}}>
    <TouchableOpacity onPress={()=>{console.log(musicChoice);
     setMmusicChoice(!musicChoice)}}>
      <View style={{width:63,height:31,backgroundColor:"rgba(255,255,255,0.2)",borderRadius:16,justifyContent:'center',alignItems:"center",marginLeft:(width - 298) /2}}>
        <Text>搜索</Text>
      </View>
    </TouchableOpacity>
   {musicChoice ?  findMusic() :   musicCarousel()
  }
  </View>
)
}

const styles = StyleSheet.create({ 
  findMusicHead:{
    flexDirection:"row",
    justifyContent:'space-between',
    marginHorizontal:15
  },
  searchMusic:{
    width:width -30,
    height:35,
    backgroundColor:"rgba(255,255,255,0.2)",
    borderRadius:11,
    marginTop:32,
    marginHorizontal:15
  }
  
})
export default StoryMusic;
