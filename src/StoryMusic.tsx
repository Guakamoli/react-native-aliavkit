import React, { Component, useRef, useState,useCallback } from 'react';
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


import Carousel from 'react-native-snap-carousel';

const { width, height } = Dimensions.get('window');
const StoryMusic = (props) => { 
  const {musicDynamicGif,musicIconPng}  = props
  // const [musicSelect,setMusicSelect] = useState(1);
  const [musicChoice,setMmusicChoice] = useState(false); 
  const [currentIndex,setCurrentIndex] = useState(0);
  const [musicSearchValue,setMusicSearchValue] = useState('')
 const  musicCarousel  = () => {
    return (
      <Carousel
     data={[1,3,1,5,4,5]}
     itemWidth={298}
     sliderWidth={width}
     initialNumToRender={4}
     firstItem={currentIndex}
     activeAnimationType={'timing'}
     onBeforeSnapToItem={(slideIndex = 0) => {
      setCurrentIndex(slideIndex)
     }}

      renderItem={({ index, item }) => {
       
       return (
         <View style={[{width:298,height:85,backgroundColor:"rgba(255,255,255,0.2)",borderRadius:15,marginVertical:16,padding:14},currentIndex == index &&{ backgroundColor:"rgba(255,255,255,0.95)"}]}>
           <View style={{flexDirection:"row",justifyContent:"space-between",marginBottom:14}}> 
           <Image source={musicIconPng} style={{ width: 18,height:18 }} />
           <Image source={musicDynamicGif} style={{ width: 30,height:18 }} />
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
    const onLengthHandle = useCallback(
      e => {
        // setLength(copyWordCount(e.nativeEvent.text, lang));
        setMusicSearchValue(e.nativeEvent.text)
      },
      [musicSearchValue],
    );
    return ( 
      <View style={{height:571,backgroundColor:"rgba(0, 0, 0, 0.8)"}}> 
      <View style={styles.findMusicHead}>
        <TouchableOpacity onPress={()=>{ setMmusicChoice(false)}}> 
        <Text style={styles.findMusicCancel} >取消</Text>
          </TouchableOpacity>
        <Text style={ styles.findMusicHeadTitle }>背景音乐</Text>
        <Text > 完成</Text>
      </View>
      <View style={styles.searchMusic}>
   
      <Image source={{ uri: 'https://guakamoli1-video-message-dev.oss-cn-qingdao.aliyuncs.com/default/3494e33ecbbb5b955a1c84bd6b8a0626/116c7efd-96bc-46ca-92d7-3008f32c09c5.jpg' }} style={{ width: 12,height:12,marginRight:5 }} />
    
        <TextInput  
         multiline={true}
         textAlignVertical={'top'}
         numberOfLines={3}
         onChange={onLengthHandle}
        //  style={[
        //    styles.inputStyle,
        //    isRN ? {} : { boxSizing: 'border-box', paddingHorizontal: 15, width: '100%', borderRadius: 14 },
        //  ]}
         value={musicSearchValue}
        //  placeholder={`${t('commentPlaceholder')}`}
         selectionColor='#895EFF'
        />
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
  <View >
    {!musicChoice &&  <TouchableOpacity onPress={()=>{setMmusicChoice(!musicChoice)}}>
      <View style={{width:63,height:31,backgroundColor:"rgba(255,255,255,0.2)",borderRadius:16,justifyContent:'center',alignItems:"center",marginLeft:(width - 298) /2}}>
       <Text>搜索</Text>
      </View>
    </TouchableOpacity> }
    
   {musicChoice ?  findMusic() :   musicCarousel() }
  </View>
)
}

const styles = StyleSheet.create({ 
  findMusicHead:{
    flexDirection:"row",
    justifyContent:'space-between',
    margin:15,
    marginBottom:0,
  },
  findMusicHeadTitle:{
    fontSize:16,
    fontWeight:"500",
    lineHeight:22,
    color:"#fff"
  },
  findMusicSuccess:{
    fontSize:16,
    fontWeight:'500',
    lineHeight:22,
    color:"rgba(255,255,255,0.4)"
  },
  findMusicCancel:{
    fontSize:16,
    fontWeight:'400',
    lineHeight:22,
    color:"#fff"
  },
  searchMusic:{
    width:width -30,
    height:35,
    backgroundColor:"rgba(255,255,255,0.2)",
    borderRadius:11,
    marginTop:32,
    marginHorizontal:15,
    // justifyContent:'center',
    alignItems:'center',
    flexDirection:"row",
    padding:10,
  }
  
})
export default StoryMusic;
