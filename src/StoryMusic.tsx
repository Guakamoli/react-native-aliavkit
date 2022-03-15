import React, { Component, useRef, useState, useCallback, useEffect, useDebugValue } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Pressable,
  Image,
  Dimensions,
  FlatList,
  NativeModules,
  TextInput,
  Platform,
} from 'react-native';

import FastImage from '@rocket.chat/react-native-fast-image';

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

//记录音乐卡片选中的位置，下次恢复时，用于设置缓存数量
let initialNum = 5;

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
  const [songData, setSongData] = useState(setMusicState ? mMusicList : []);
  //音乐列表是否选择第一条item
  const [carouselFirstItem, setCarouselFirstItem] = useState(setMusicState ? mSelectedMusicPosition : 0);

  //搜索框内容
  const [musicSearchValue, setMusicSearchValue] = useState('');
  //输入名称搜索的歌曲列表
  const [searchMusicList, setSearchMusicList] = useState([]);
  //搜索列表选中的歌曲
  const [searchSelectedMusic, setSearchSelectedMusic] = useState(null);

  //选中音乐下标
  const [currentIndex, setCurrentIndex] = useState(setMusicState ? mSelectedMusicPosition : 0);
  //正在播放的音乐
  const [currentPlayMusic, setCurrentPlayMusic] = useState({});

  const [pages, setpage] = useState(1);

  React.useEffect(() => {
    if (!carouselRef.current) {
      return;
    }
    carouselRef.current?.snapToItem(currentIndex);
  }, [carouselRef]);

  useEffect(() => {
    //初始化获取
    //
    if (!songData || songData.length == 0 || !setMusicState) {
      getSong({});
    } else {
      if (setMusicState) {
        setCurrentPlayMusic(songData[currentIndex])
        playMusic(songData[currentIndex]);
      }
    }
    return () => {
      initialNum = mSelectedMusicPosition
     
    };
  }, []);

  const onLengthHandle = useCallback(
    (e) => {
      // 歌曲搜索
      //
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
   
    // getmusicInfo(song)
  };
  const pauseMusic = async (song) => {
   
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
    setSearchMusicList(song);
  }

  const getSong = async ({ name = '', page = 1, pageSize = 5 }) => {
    const song = await AVService.getMusics({ name, page, pageSize });
    if (!song?.length) {
      return
    }
    if (!!song?.length && !!songData?.length) {
      songData.forEach((item, index) => {
        song.forEach((itemNew, position) => {
          if (item.songID === itemNew.songID) {
            song.splice(position, 1);
          }
        });
      });
    }
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


        enableMomentum={false}
        decelerationRate={'fast'}

        ref={(carouselRef)}
        data={songData}
        itemWidth={300}
        snapToInterval={300}
        lockScrollWhileSnapping={true}
        sliderWidth={width}
        // initialNumToRender={initialNum < 5 ? 5 : initialNum + 1}
        initialNumToRender={5}
        firstItem={carouselFirstItem}
        // activeAnimationType={'timing'}
        onEndReachedThreshold={0}
        onEndReached={() => {
          const page = pages + 1;
          getSong({ name: '', page: page, pageSize: 5 });
          setpage(page);
        }}
        onBeforeSnapToItem={(slideIndex = 0) => {
         
        }}
        onSnapToItem={(slideIndex = 0) => {
         
          playMusic(songData[slideIndex]);
          !setMusicState && props.setMusic(true);
          setCurrentPlayMusic(songData[slideIndex]);
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
              <View style={[styles.musicCarouselBox, IsPlayMusic && { backgroundColor: 'rgba(255,255,255,0.98)' }]}>
                <View style={styles.musicCarouselContent}>
                  <FastImage source={musicIconPng} style={styles.musicIcon} />
                  {/* 播放展示gif */}
                  {IsPlayMusic && <FastImage source={musicDynamicGif} style={styles.musicPlayGif} />}
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
  const closeMusicSearch = () => {
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
  }

  const findMusic = () => {

    const contentHeight = height - props.insets.top - props.insets.bottom;
    const musicSearchHeight = contentHeight * 0.66;
    return (
      //Pressable 屏蔽：搜索音乐时，点击屏幕空白退出音乐View
      <Pressable onPress={closeMusicSearch} style={{ width: "100%", height: height, position: 'relative' }}>
        <View style={[styles.musicFindContent, { height: musicSearchHeight }]}>
          <View style={styles.findMusicHead}>
            <TouchableOpacity
              hitSlop={{ left: 10, top: 5, right: 10, bottom: 5 }}
              onPress={closeMusicSearch}
            >
              <Text style={styles.findMusicCancel}>取消</Text>
            </TouchableOpacity>
            <Text style={styles.findMusicHeadTitle}>背景音乐</Text>
            <TouchableOpacity
              hitSlop={{ left: 10, top: 5, right: 10, bottom: 5 }}
              onPress={() => {
                if (searchSelectedMusic) {
                  let musicList = songData;
                  if (!!musicList?.length) {
                    musicList.forEach((item, index) => {
                      if (item.songID === searchSelectedMusic.songID) {
                        musicList.splice(index, 1);
                        return
                      }
                    });
                  }
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
                  carouselRef.current?.snapToItem(0);

                } else {
                  //未选择音乐点击完成，继续播放之前的音乐
                  if (!!currentPlayMusic?.songID) {
                    playMusic(currentPlayMusic)
                  }
                }
                //清空搜索栏的内容
                setMusicSearchValue("");
                setSearchMusicList([]);
                setSearchSelectedMusic(null);
                setMmusicChoice(false);
              }}
            >
              <Text style={[styles.musicFindSuccess, { color: '#fff' }]}>完成</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.searchMusic}>
            <FastImage source={musicSearch} style={styles.musicFindSearchIcon} />
            <TextInput
              multiline={false}
              autoFocus={true}
              textAlignVertical={'center'}
              onChange={onLengthHandle}
              style={[styles.musicFindSearchInput]}
              value={musicSearchValue}
              selectionColor='#fff'
            />
          </View>
          {songData.length < 1 && <View style={styles.noNetworkBox}>{loading()}</View>}
          <View style={[styles.musicFindContentBox, { height: musicSearchHeight - (56 + 42 + 10) }]}>
            <FlatList
              data={searchMusicList}
              keyExtractor={item => item.songID}
              renderItem={({ item, index }) => {
                const isPlayMusic = searchSelectedMusic && (searchSelectedMusic?.songID == item?.songID);
                return (
                  <TouchableOpacity
                    key={item.songID}
                    onPress={async () => {
                     
                      if (!isPlayMusic) {
                        playMusic(item);
                      }
                      setSearchSelectedMusic(item);
                    }}
                  >
                    <View style={[styles.musicFindBox, { marginBottom: (searchMusicList.length - 1 === index) ? 40 : 5 }, isPlayMusic && { backgroundColor: 'rgba(255,255,255,0.9)' }]}>
                      <View style={[styles.musicFindItem]}>
                        <FastImage source={isPlayMusic ? musicIconPng : musicIcongray} style={styles.musicFindIcon} />
                        <View style={styles.musicFindSongData}>
                          <Text style={[styles.musicFindSongTitle, isPlayMusic && { color: '#000' }]}>{item?.name}</Text>
                          {/* 播放展示gif */}
                          {isPlayMusic && <FastImage source={musicDynamicGif} style={styles.musicPlayGif} />}
                        </View>
                      </View>
                    </View>
                  </TouchableOpacity>
                );
              }}
            />
          </View>
        </View>
      </Pressable >
    );
  };
  const musicBottonToolsHeight = props.bottomSpaceHeight ? props.bottomSpaceHeight : 60;
  return (
    <View style={{ marginBottom: 0 }}>
      {!musicChoice && (
        <TouchableOpacity
          onPress={() => {
            //打开搜索列表，暂停之前播放的音乐
            if (!!currentPlayMusic?.songID) {
              pauseMusic(currentPlayMusic)
            }
            setMmusicChoice(true);
          }}
          hitSlop={{ left: 10, top: 5, right: 10, bottom: 5 }}
        >
          <View style={styles.musicChoiceBox}>
            <FastImage source={musicSearch} style={{
              width: 12,
              height: 12,
              marginRight: 5,
            }} />
            <Text style={styles.musicSearchText}>搜索</Text>
          </View>
        </TouchableOpacity>
      )}

      {musicCarousel()}
      {musicChoice && findMusic()}

      {!musicChoice && (
        <View style={[styles.musicChoiceContent, { height: musicBottonToolsHeight }]}>
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
              <FastImage source={useMusic} style={styles.musicIcon} />
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
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 0,
    height: 56,
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
    marginLeft: 15,
    fontWeight: '400',
    lineHeight: 22,
    color: '#fff',
  },
  searchMusic: {
    width: "92%",
    marginLeft: "auto",
    marginRight: 'auto',
    height: 42,
    backgroundColor: 'rgba(255,255,255,0.3)',
    borderRadius: 12,
    marginTop: 5,
    marginBottom: 5,
    // justifyContent:'center',
    alignItems: 'center',
    flexDirection: 'row',
    overflow: 'hidden',
  },
  musicCarouselBox: {
    width: 298,
    height: 85,
    backgroundColor: 'rgba(255,255,255,0.3)',
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
    position: 'absolute',
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 1)',
  },
  musicFindSuccess: {
    marginRight: 15,
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
    width: "92%",
    marginLeft: "auto",
    marginRight: 'auto',
    height: 82,
    // backgroundColor: 'rgba(0, 0, 0, 0.9)',
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    marginTop: 13,
    marginBottom: 5,
    borderRadius: 15,
    padding: 15,
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
    width: 14,
    height: 14,
    marginLeft: 10,
  },
  musicFindSearchInput: {
    width: '100%',
    height: '100%',
    borderRadius: 12,
    marginLeft: 10,
    color: '#fff'
  },
  musicFindContentBox: {
    flexDirection: 'column',
    // paddingBottom: 120,
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
    width: 64,
    height: 32,
    flexDirection: 'row',
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginLeft: (width - 298) / 2,
  },
  musicChoiceContent: {
    // height: 60,
    // backgroundColor: '#000',
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
