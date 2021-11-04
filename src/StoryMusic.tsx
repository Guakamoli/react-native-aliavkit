import React, { Component, useRef, useState, useCallback, useEffect, useDebugValue } from 'react';
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
import AVService from './AVService.ios'
import ImageMap from '../images';
const { useMusic } = ImageMap;
const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
const { width, height } = Dimensions.get('window');
const StoryMusic = (props) => {
  const { musicDynamicGif, musicIconPng, getmusicInfo, musicSearch, musicIcongray, setMusicState } = props;


  // const [musicSelect,setMusicSelect] = useState(1);
  const [musicChoice, setMmusicChoice] = useState(false);
  // const [currentIndex, setCurrentIndex] = useState(0);
  const [checkedData, setCheckedData] = useState()
  const [musicSearchValue, setMusicSearchValue] = useState('');
  const [songData, setSongData] = useState([]);


  useEffect(() => {
    console.log(23);

    getSong({})
    return () => {
      console.log('音乐销毁',);
      console.log('音乐销毁', songData, checkedData);

    }

  }, [])


  useEffect(() => {
    if (songData.length > 0) {
      console.log('初始化', songData[0]);
      palyMusic(songData[0])
      setCheckedData(songData[0])

    }
  }, [songData])

  const onLengthHandle = useCallback(

    e => {
      console.log(e.nativeEvent.text);
      if (e.nativeEvent.text) {

        getSong({ name: `${e.nativeEvent.text}`, page: 1, pageSize: 5 })
      }
      // setLength(copyWordCount(e.nativeEvent.text, lang));
      setMusicSearchValue(e.nativeEvent.text)
    },

    [musicSearchValue],
  );
  const palyMusic = async (song) => {
    console.log('播放音乐', song);
    // = await AVService.playMusic(song.songID);
    if (!song) {
      return;
    }

    const songa = await AVService.playMusic(song.songID)
    console.log('---- 返回值: ', songa);
    // getmusicInfo(song)
  }
  const pauseMusic = async (song) => {
    console.log('暂停音乐', song);

    await AVService.pauseMusic(song.songID)
  }

  const getSong = async ({ name = 'all-music', page = 1, pageSize = 5 }) => {
    console.log('-----', `${name}`, page, pageSize);
    if (!name) {
      name = 'all-music'
    }
    const song = await AVService.getMusics({ name, page, pageSize })
    console.log('success', song);
    setSongData(song)

  }
  const musicCarousel = () => {
    return (
      <Carousel
        data={songData}
        itemWidth={298}
        sliderWidth={width}
        initialNumToRender={4}
        // firstItem={!musicChoice && songData.indexOf(checkedData)}
        activeAnimationType={'timing'}
        onBeforeSnapToItem={async (slideIndex = 0) => {
          // 当前选中的
          props.setMusic(false);
          getmusicInfo({});
          setTimeout(() => {

            setCheckedData(songData[slideIndex]);
            palyMusic(songData[slideIndex])
          }, 300);

        }}

        renderItem={({ index, item }) => {
          // console.log(checkedData);

          return (
            <TouchableOpacity onPress={() => {
              if (checkedData.songID == item.songID) {
                pauseMusic(item)
                setCheckedData({});
              } else {
                setCheckedData(item);
                palyMusic(item)
              }
            }} >
              <View style={[{ width: 298, height: 85, backgroundColor: "rgba(255,255,255,0.2)", borderRadius: 15, marginVertical: 16, padding: 14 }, checkedData?.songID == item?.songID && { backgroundColor: "rgba(255,255,255,0.95)" }]}>
                <View style={{ flexDirection: "row", justifyContent: "space-between", marginBottom: 14 }}>
                  <Image source={musicIconPng} style={{ width: 18, height: 18 }} />
                  {/* 播放展示gif */}
                  {checkedData?.songID == item?.songID && <Image source={musicDynamicGif} style={{ width: 30, height: 18 }} />}
                </View>
                <View>
                  <Text>
                    {item?.name}
                  </Text>
                </View>
              </View>
            </TouchableOpacity>
          )
        }}
      />
    )
  }
  const findMusic = () => {

    return (
      <View style={{ height: height * 0.6, backgroundColor: "rgba(0, 0, 0, 0.8)" }}>
        <View style={styles.findMusicHead}>
          <TouchableOpacity onPress={() => { setMmusicChoice(false); }}>
            <Text style={styles.findMusicCancel} >取消</Text>
          </TouchableOpacity>
          <Text style={styles.findMusicHeadTitle}>背景音乐</Text>
          <TouchableOpacity onPress={() => {
            // pauseMusic(checkedData), 
            setMmusicChoice(false)
          }}>
            <Text style={[{ fontSize: 16, fontWeight: '500', color: 'rgba(255, 255, 255, 0.4)' }, checkedData && { color: "#fff" }]} > 完成</Text>
          </TouchableOpacity>
        </View>
        <View style={styles.searchMusic}>

          <Image source={musicSearch} style={{ width: 12, height: 12, marginRight: 5 }} />

          <TextInput
            multiline={true}
            textAlignVertical={'top'}
            numberOfLines={1}
            onChange={onLengthHandle}
            style={[
              { paddingHorizontal: 15, width: '100%', borderRadius: 14 },
            ]}
            value={musicSearchValue}
            //  placeholder={`${t('commentPlaceholder')}`}
            selectionColor='#895EFF'
          />
        </View>
        <View style={{ flexDirection: 'column', paddingBottom: 120 }}>
          <FlatList
            data={songData}
            renderItem={({ item }) => {
              return (
                <TouchableOpacity onPress={async () => {
                  console.log('点击', checkedData);
                  if (checkedData.songID == item.songID) {
                    pauseMusic(item)
                    setCheckedData({});
                  } else {
                    setCheckedData(item);
                    palyMusic(item)
                  }


                }}>
                  <View style={[{ width: width - 30, height: 84, backgroundColor: 'rgba(0, 0, 0, 0.8)', marginTop: 20, borderRadius: 15, padding: 15, marginHorizontal: 15, alignItems: 'center', justifyContent: 'center' }, checkedData?.songID == item?.songID && { backgroundColor: "rgba(255,255,255,0.95)" }]}>
                    <View style={[{ flexDirection: 'row', marginBottom: 10, alignItems: 'center', justifyContent: 'center' }]}>
                      <Image source={checkedData?.songID == item?.songID ? musicIconPng : musicIcongray} style={{ width: 19, height: 19, marginRight: 5 }} />
                      <View style={{ flexDirection: "row", justifyContent: "space-between", flex: 1, alignItems: "center" }}>
                        <Text style={[{ fontWeight: '400', fontSize: 16, color: '#fff', marginLeft: 15, lineHeight: 21, }, checkedData?.songID == item?.songID && { color: "#000" }]}>{item?.name}</Text>
                        {/* 播放展示gif */}
                        {checkedData?.songID == item?.songID && <Image source={musicDynamicGif} style={{ width: 30, height: 18 }} />}
                      </View>
                    </View>
                    {/* <Text style={{ fontWeight: '400', color: "#a6a5a2", fontSize: 15, lineHeight: 21, }}>长大以后我只能奔跑 我多害怕黑暗中跌倒</Text> */}
                  </View>
                </TouchableOpacity>
              )
            }
            }
          />
        </View>
      </View >
    )
  }
  return (
    <View >
      {!musicChoice &&
        <TouchableOpacity onPress={() => { setMmusicChoice(!musicChoice); pauseMusic(checkedData) }}>
          <View style={{ width: 63, height: 31, flexDirection: 'row', backgroundColor: "rgba(255,255,255,0.2)", borderRadius: 16, paddingHorizontal: 9, alignItems: "center", marginLeft: (width - 298) / 2 }}>
            <Image source={musicSearch} style={{ width: 12, height: 12, marginRight: 5 }} />
            <Text style={{ fontSize: 13, lineHeight: 13, fontWeight: '500', color: '#fff' }}>搜索</Text>
          </View>
        </TouchableOpacity>}

      {musicChoice ? findMusic() : musicCarousel()}
      {

        !musicChoice &&

        <View style={{
          height: 100, backgroundColor: "#000",
          flexDirection: 'row',
          justifyContent: 'center',
          alignItems: "center",
          position: 'relative'
        }}>

          <TouchableOpacity style={{ flexDirection: 'row', alignItems: 'center' }} onPress={() => {
            props.setMusic(!setMusicState);
            getmusicInfo(checkedData);
          }}>
            {setMusicState ?
              <Image source={useMusic} style={{ width: 18, height: 18, }} />
              :
              <View
                style={{
                  borderWidth: 1, width: 18, height: 18, borderRadius: 18, borderColor: '#fff', zIndex: 1
                }}
              >
              </View>
            }
            <Text style={{
              fontSize: 16,
              lineHeight: 18,
              color: '#FFFFFF',
              marginLeft: 5,
              fontWeight: '500'
            }}>配乐</Text>
          </TouchableOpacity>
        </View >

      }
    </View >
  )
}

const styles = StyleSheet.create({
  findMusicHead: {
    flexDirection: "row",
    justifyContent: 'space-between',
    margin: 15,
    marginBottom: 0,
  },
  findMusicHeadTitle: {
    fontSize: 16,
    fontWeight: "500",
    lineHeight: 22,
    color: "#fff"
  },
  findMusicSuccess: {
    fontSize: 16,
    fontWeight: '500',
    lineHeight: 22,
    color: "rgba(255,255,255,0.4)"
  },
  findMusicCancel: {
    fontSize: 16,
    fontWeight: '400',
    lineHeight: 22,
    color: "#fff"
  },
  searchMusic: {
    width: width - 30,
    height: 35,
    backgroundColor: "rgba(255,255,255,0.2)",
    borderRadius: 11,
    marginTop: 32,
    marginHorizontal: 15,
    // justifyContent:'center',
    alignItems: 'center',
    flexDirection: "row",
    padding: 10,
  }

})
export default StoryMusic;
