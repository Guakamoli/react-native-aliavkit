import React, { Component, useRef, useState, useCallback, useEffect, useDebugValue } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Image,
  Dimensions,
  FlatList,
  NativeModules,
  TextInput,
  Platform,
} from 'react-native';

import Carousel from 'react-native-snap-carousel';
import AVService from './AVService';
import { Button } from 'react-native-elements';
import ImageMap from '../images';
import { concat } from 'lodash';
import { ForceTouchGestureHandler } from 'react-native-gesture-handler';
const { useMusic } = ImageMap;
const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
const { width, height } = Dimensions.get('window');

let mMusicList = [];
let mSelectedMusicPosition = 0;

const StoryMusic = (props) => {
  const {
    musicDynamicGif,
    musicIconPng,
    getmusicInfo,
    musicSearch,
    musicIcongray,
    setMusicState,
    getMusicOn,
    connected,
  } = props;
  //打开搜索列表
  const [musicChoice, setMmusicChoice] = useState(false);

  const carouselRef = React.useRef(null);

  //音乐列表
  const [songData, setSongData] = useState(mMusicList);
  //音乐列表是否选择第一条item
  const [carouselFirstItem, setCarouselFirstItem] = useState(mSelectedMusicPosition);

  //搜索框内容
  const [musicSearchValue, setMusicSearchValue] = useState('');
  //输入名称搜索的歌曲列表
  const [searchMusicList, setSearchMusicList] = useState([]);
  //搜索列表选中的歌曲
  const [searchSelectedMusic, setSearchSelectedMusic] = useState(null);

  //选中音乐下标
  const [currentIndex, setCurrentIndex] = useState(mSelectedMusicPosition);
  //正在播放的音乐
  const [currentPlayMusic, setCurrentPlayMusic] = useState({});

  const [pages, setpage] = useState(1);

  React.useEffect(() => {
    if (!carouselRef.current) {
      return;
    }
    console.info("carouselRef", currentIndex);
    carouselRef.current?.snapToItem(currentIndex);
    setTimeout(() => {
      carouselRef.current?.snapToItem(currentIndex);
    }, 1000);
  }, [carouselRef]);

  useEffect(() => {
    //初始化获取
    console.log('初始化', currentIndex, songData.length, carouselFirstItem);
    if (!songData || songData.length == 0) {
      getSong({});
    } else {
      if (setMusicState) {
        setCurrentPlayMusic(songData[currentIndex])
        playMusic(songData[currentIndex]);
      }
    }
    return () => {
      console.log('音乐销毁');
    };
  }, []);

  const onLengthHandle = useCallback(
    (e) => {
      // 歌曲搜索
      console.log("name", e.nativeEvent.text);
      getSearchSong(e.nativeEvent.text ? e.nativeEvent.text.trim() : "");
      setMusicSearchValue(e.nativeEvent.text);
    },
    [musicSearchValue],
  );
  const playMusic = async (song) => {
    if (!song) {
      return;
    }
    const songa = await AVService.playMusic(song.songID);
    getMusicOn(songa);
    getmusicInfo(songa);
    console.info('播放音乐', song.songID, song.name);
    // getmusicInfo(song)
  };
  const pauseMusic = async (song) => {
    console.info('暂停音乐', song.songID, song.name);
    if (!song) {
      return;
    }
    await AVService.pauseMusic(song.songID);
    getmusicInfo({});
  };

  const getSearchSong = async (name) => {
    if (!name) {
      return;
    }
    const song = await AVService.getMusics({ name: name, page: 1, pageSize: 5 });
    console.info("搜索歌曲列表 ", song.length, song);
    setSearchMusicList(song);
  }

  const getSong = async ({ name = '', page = 1, pageSize = 5 }) => {
    const song = await AVService.getMusics({ name, page, pageSize });
    // console.info("获取歌曲列表 ", song.length, song);
    if (!song?.length) {
      return
    }
    songData.forEach((item, index) => {
      song.forEach((itemNew, position) => {
        if (item.songID === itemNew.songID) {
          song.splice(position, 1);
        }
      });
    });
    if (!songData?.length) {
      setCurrentPlayMusic(song[currentIndex])
      playMusic(song[currentIndex]);
      !setMusicState && props.setMusic(true);
    }
    const musicList = page <= 1 ? song : songData.concat(song);
    setSongData(musicList);
    mMusicList = musicList
  };

  const loading = () => {
    return (
      <Button
        buttonStyle={{
          backgroundColor: 'transparent',
        }}
        loadingStyle={{
          width: 35,
          height: 35,
          backgroundColor: 'transparent',
        }}
        style={{ backgroundColor: 'transparent' }}
        containerStyle={{
          backgroundColor: 'transparent',
        }}
        loading
        loadingProps={{ size: 'large' }}
      />
    );
  };
  const musicCarousel = () => {
    if (songData.length < 1) {
      return (
        <View style={styles.noNetworkBox}>
          <View style={[styles.musicCarouselBox]}>{loading()}</View>
        </View>
      );
    }

    return (
      <Carousel
        ref={(carouselRef)}
        data={songData}
        itemWidth={298}
        sliderWidth={width}
        initialNumToRender={10}
        firstItem={carouselFirstItem}
        activeAnimationType={'timing'}
        onEndReachedThreshold={0}
        onEndReached={() => {
          const page = pages + 1;
          getSong({ name: '', page: page, pageSize: 5 });
          setpage(page);
        }}
        onSnapToItem={(slideIndex = 0) => {
          playMusic(songData[slideIndex]);
          !setMusicState && props.setMusic(true);
          setCurrentPlayMusic(songData[slideIndex]);
          console.info("slideIndex", slideIndex)
          setCurrentIndex(slideIndex);
          mSelectedMusicPosition = slideIndex;
        }}
        renderItem={({ index, item }) => {
          const IsPlayMusic = (index === currentIndex) && (currentPlayMusic && currentPlayMusic.songID == item.songID);
          return (
            <TouchableOpacity
              onPress={() => {
                if (currentPlayMusic && currentPlayMusic.songID == item.songID) {
                  pauseMusic(item);
                  setCurrentPlayMusic({});
                  props.setMusic(false);
                } else {
                  playMusic(item);
                  setCurrentPlayMusic(item);
                  !setMusicState && props.setMusic(true);
                }
              }}
            >
              <View style={[styles.musicCarouselBox, IsPlayMusic && { backgroundColor: 'rgba(255,255,255,0.95)' }]}>
                <View style={styles.musicCarouselContent}>
                  <Image source={musicIconPng} style={styles.musicIcon} />
                  {/* 播放展示gif */}
                  {IsPlayMusic && <Image source={musicDynamicGif} style={styles.musicPlayGif} />}
                </View>
                <View>
                  <Text>{item?.name}</Text>
                </View>
              </View>
            </TouchableOpacity>
          );
        }}
      />
    );
  };
  const findMusic = () => {
    return (
      <View style={styles.musicFindContent}>
        <View style={styles.findMusicHead}>
          <TouchableOpacity
            onPress={() => {

              if (searchSelectedMusic) {
                pauseMusic(searchSelectedMusic);
              }
              //搜索列表取消，关闭搜索列表,继续播放原来的歌曲
              if (!!currentPlayMusic?.songID) {
                playMusic(currentPlayMusic)
              }
              setMusicSearchValue("");
              setSearchMusicList([]);
              setSearchSelectedMusic(null);
              setMmusicChoice(false);
            }}
          >
            <Text style={styles.findMusicCancel}>取消</Text>
          </TouchableOpacity>
          <Text style={styles.findMusicHeadTitle}>背景音乐</Text>
          <TouchableOpacity
            onPress={() => {
              if (searchSelectedMusic) {
                // pauseMusic(searchSelectedMusic);
                let musicList = songData;
                musicList.forEach((item, index) => {
                  if (item.songID === searchSelectedMusic.songID) {
                    musicList.splice(index, 1);
                    return
                  }
                });
                musicList.unshift(searchSelectedMusic)
                if (!!musicList[0]) {
                  playMusic(musicList[0])
                  //设置成当前播放歌曲
                  setCurrentPlayMusic(musicList[0]);
                }
                setSongData(musicList);
                mMusicList = musicList
                setCurrentIndex(0);
                mSelectedMusicPosition = 0;
                setCarouselFirstItem(0);
              }
              //清空搜索栏的内容
              setMusicSearchValue("");
              setSearchMusicList([]);
              setSearchSelectedMusic(null);
              setMmusicChoice(false);
            }}
          >
            <Text style={[styles.musicFindSuccess, { color: '#fff' }]}> 完成</Text>
          </TouchableOpacity>
        </View>
        <View style={styles.searchMusic}>
          <Image source={musicSearch} style={styles.musicFindSearchIcon} />

          <TextInput
            multiline={true}
            textAlignVertical={'top'}
            numberOfLines={1}
            onChange={onLengthHandle}
            style={[styles.musicFindSearchInput]}
            value={musicSearchValue}
            selectionColor='#895EFF'
          />
        </View>
        {songData.length < 1 && <View style={styles.noNetworkBox}>{loading()}</View>}
        <View style={styles.musicFindContentBox}>
          <FlatList
            data={searchMusicList}
            onEndReachedThreshold={0.2}
            // onEndReached={() => {
            //   // let page = pages s+ 1;
            //   // getSong({ name: '', page: pages, pageSize: 5 });
            //   // setpage(page);
            // }}
            renderItem={({ item }) => {
              const isPlayMusic = searchSelectedMusic && (searchSelectedMusic?.songID == item?.songID);
              return (
                <TouchableOpacity
                  key={item.songID}
                  onPress={async () => {
                    console.log('搜索列表点击', item);
                    if (!isPlayMusic) {
                      playMusic(item);
                    }
                    setSearchSelectedMusic(item);
                  }}
                >
                  <View style={[styles.musicFindBox, isPlayMusic && { backgroundColor: 'rgba(255,255,255,0.95)' }]}>
                    <View style={[styles.musicFindItem]}>
                      <Image source={isPlayMusic ? musicIconPng : musicIcongray} style={styles.musicFindIcon} />
                      <View style={styles.musicFindSongData}>
                        <Text style={[styles.musicFindSongTitle, isPlayMusic && { color: '#000' }]}>{item?.name}</Text>
                        {/* 播放展示gif */}
                        {isPlayMusic && <Image source={musicDynamicGif} style={styles.musicPlayGif} />}
                      </View>
                    </View>
                    {/* <Text style={{ fontWeight: '400', color: "#a6a5a2", fontSize: 15, lineHeight: 21, }}>长大以后我只能奔跑 我多害怕黑暗中跌倒</Text> */}
                  </View>
                </TouchableOpacity>
              );
            }}
          />
        </View>
      </View>
    );
  };
  return (
    <View>
      {!musicChoice && (
        <TouchableOpacity
          onPress={() => {
            //打开搜索列表，暂停之前播放的音乐
            if (!!currentPlayMusic?.songID) {
              pauseMusic(currentPlayMusic)
            }
            setMmusicChoice(true);
          }}
        >
          <View style={styles.musicChoiceBox}>
            <Image source={musicSearch} style={styles.musicFindSearchIcon} />
            <Text style={styles.musicSearchText}>搜索</Text>
          </View>
        </TouchableOpacity>
      )}

      {musicChoice ? findMusic() : musicCarousel()}
      {!musicChoice && (
        <View style={styles.musicChoiceContent}>
          <TouchableOpacity
            style={{ flexDirection: 'row', alignItems: 'center' }}
            onPress={async () => {
              //配乐
              props.setMusic(!setMusicState);
              if (!setMusicState) {
                playMusic(songData[currentIndex]);
                setCurrentPlayMusic(songData[currentIndex]);
              } else {
                pauseMusic(songData[currentIndex]);
                setCurrentPlayMusic({});
              }
            }}
          >
            {setMusicState ? (
              <Image source={useMusic} style={styles.musicIcon} />
            ) : (
              <View style={styles.musicUnSelect}></View>
            )}
            <Text style={styles.musicSelectTitle}>配乐</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  findMusicHead: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    margin: 15,
    marginBottom: 0,
  },
  findMusicHeadTitle: {
    fontSize: 16,
    fontWeight: '500',
    lineHeight: 22,
    color: '#fff',
  },
  findMusicSuccess: {
    fontSize: 16,
    fontWeight: '500',
    lineHeight: 22,
    color: 'rgba(255,255,255,0.4)',
  },
  findMusicCancel: {
    fontSize: 16,
    fontWeight: '400',
    lineHeight: 22,
    color: '#fff',
  },
  searchMusic: {
    width: width - 30,
    height: 35,
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 11,
    marginTop: 32,
    marginHorizontal: 15,
    // justifyContent:'center',
    alignItems: 'center',
    flexDirection: 'row',
    padding: 10,
  },
  musicCarouselBox: {
    width: 298,
    height: 85,
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 15,
    marginVertical: 16,
    padding: 14,
  },
  noNetworkBox: {
    justifyContent: 'center',
    alignItems: 'center',
    flex: 1,
  },
  musicCarouselContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 14,
  },
  musicPlayGif: {
    width: 30,
    height: 18,
  },
  musicIcon: {
    width: 18,
    height: 18,
  },
  musicFindContent: {
    height: height * 0.6,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
  },
  musicFindSuccess: {
    fontSize: 16,
    fontWeight: '500',
    color: 'rgba(255, 255, 255, 0.4)',
  },
  musicFindIcon: {
    width: 19,
    height: 19,
    marginRight: 5,
  },
  musicFindBox: {
    width: width - 30,
    height: 84,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    marginTop: 20,
    borderRadius: 15,
    padding: 15,
    marginHorizontal: 15,
    alignItems: 'center',
    justifyContent: 'center',
  },
  musicFindItem: {
    flexDirection: 'row',
    marginBottom: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  musicFindSearchIcon: {
    width: 12,
    height: 12,
    marginRight: 5,
  },
  musicFindSearchInput: {
    width: '100%',
    borderRadius: 14,
  },
  musicFindContentBox: {
    flexDirection: 'column',
    paddingBottom: 120,
  },
  musicFindSongTitle: {
    fontWeight: '400',
    fontSize: 16,
    color: '#fff',
    marginLeft: 15,
    lineHeight: 21,
  },
  musicFindSongData: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    flex: 1,
    alignItems: 'center',
  },
  musicChoiceBox: {
    width: 63,
    height: 31,
    flexDirection: 'row',
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 16,
    paddingHorizontal: 9,
    alignItems: 'center',
    marginLeft: (width - 298) / 2,
  },
  musicChoiceContent: {
    height: 100,
    backgroundColor: '#000',
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  musicSearchText: {
    fontSize: 13,
    lineHeight: 13,
    fontWeight: '500',
    color: '#fff',
  },
  musicUnSelect: {
    borderWidth: 1,
    width: 18,
    height: 18,
    borderRadius: 18,
    borderColor: '#fff',
    zIndex: 1,
  },
  musicSelectTitle: {
    fontSize: 16,
    lineHeight: 18,
    color: '#FFFFFF',
    marginLeft: 5,
    fontWeight: '500',
  },
});
export default StoryMusic;
