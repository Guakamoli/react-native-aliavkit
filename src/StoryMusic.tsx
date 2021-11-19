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
} from 'react-native';

import Carousel from 'react-native-snap-carousel';
import AVService from './AVService';
import ImageMap from '../images';
const { useMusic } = ImageMap;
const { RNEditViewManager, AliAVServiceBridge } = NativeModules;
const { width, height } = Dimensions.get('window');
const StoryMusic = (props) => {
  const { musicDynamicGif, musicIconPng, getmusicInfo, musicSearch, musicIcongray, setMusicState, getMusicOn } = props;

  const [musicChoice, setMmusicChoice] = useState(false);
  const [checkedData, setCheckedData] = useState();
  const [musicSearchValue, setMusicSearchValue] = useState('');
  const [songData, setSongData] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [pages, setpage] = useState(2);

  useEffect(() => {
    console.log(23);

    getSong({});
    return () => {
      console.log('音乐销毁');
      console.log('音乐销毁', songData, checkedData);
    };
  }, []);

  useEffect(() => {
    if (songData.length > 0) {
      console.log('初始化', songData[0]);
      playMusic(songData[0]);
      setCheckedData(songData[0]);
      getmusicInfo(songData[0]);
      !setMusicState && props.setMusic(true);
    }
  }, [songData]);

  const onLengthHandle = useCallback(
    (e) => {
      console.log(e.nativeEvent.text);
      if (e.nativeEvent.text) {
        getSong({ name: `${e.nativeEvent.text}`, page: 1, pageSize: 5 });
      }
      // setLength(copyWordCount(e.nativeEvent.text, lang));
      setMusicSearchValue(e.nativeEvent.text);
    },

    [musicSearchValue],
  );
  const playMusic = async (song) => {
    console.log('播放音乐', song);
    // = await AVService.playMusic(song.songID);
    if (!song) {
      return;
    }

    const songa = await AVService.playMusic(song.songID);
    getMusicOn(song);
    console.log('---- 返回值: ', songa);
    // getmusicInfo(song)
  };
  const pauseMusic = async (song) => {
    console.log('暂停音乐', song);

    await AVService.pauseMusic(song.songID);
  };

  const getSong = async ({ name = 'all-music', page = 1, pageSize = 5 }) => {
    if (!name) 
      name = 'all-music';
    }
// 暂时
    if (page > 5) {
      return;
    }
    const song = await AVService.getMusics({ name, page, pageSize });
    if (song.length % pageSize != 0) {
      setSongData(songData.concat(song));
    } else {
      setSongData(song);
    }
  };

  const musicCarousel = () => {
    return (
      <Carousel
        data={songData}
        itemWidth={298}
        sliderWidth={width}
        initialNumToRender={4}
        firstItem={!musicChoice && songData.indexOf(checkedData)}
        activeAnimationType={'timing'}
        onEndReachedThreshold={0.2}
        onEndReached={() => {
          let page = pages + 1;
          getSong({ name: '', page: pages, pageSize: 5 });
          setpage(page);
        }}
        onSnapToItem={async (slideIndex = 0) => {
          playMusic(songData[slideIndex]);
          setTimeout(() => {
            !setMusicState && props.setMusic(true);
            getmusicInfo(songData[slideIndex]);
            setCheckedData(songData[slideIndex]);
            setCurrentIndex(slideIndex);
          }, 300);
        }}
        renderItem={({ index, item }) => {
          // console.log(checkedData);
          const IsPlayMusic = checkedData?.songID == item?.songID;
          return (
            <TouchableOpacity
              onPress={() => {
                if (checkedData.songID == item.songID) {
                  pauseMusic(item);
                  props.setMusic(false);
                  setCheckedData({});
                  getmusicInfo({});
                } else {
                  getmusicInfo(item);
                  setCheckedData(item);
                  playMusic(item);
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
              setMmusicChoice(false);
            }}
          >
            <Text style={styles.findMusicCancel}>取消</Text>
          </TouchableOpacity>
          <Text style={styles.findMusicHeadTitle}>背景音乐</Text>
          <TouchableOpacity
            onPress={() => {
              // pauseMusic(checkedData),
              setMmusicChoice(false);
            }}
          >
            <Text style={[styles.musicFindSuccess, checkedData && { color: '#fff' }]}> 完成</Text>
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
            //  placeholder={`${t('commentPlaceholder')}`}
            selectionColor='#895EFF'
          />
        </View>
        <View style={styles.musicFindContentBox}>
          <FlatList
            data={songData}
            onEndReachedThreshold={0.2}
            onEndReached={() => {
              // let page = pages s+ 1;
              // getSong({ name: '', page: pages, pageSize: 5 });
              // setpage(page);
            }}
            renderItem={({ item }) => {
              const isPlayMusic = checkedData?.songID == item?.songID;
              return (
                <TouchableOpacity
                  onPress={async () => {
                    console.log('点击', checkedData);
                    if (checkedData.songID == item.songID) {
                      pauseMusic(item);
                      setCheckedData({});
                    } else {
                      setCheckedData(item);
                      playMusic(item);
                    }
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
            setMmusicChoice(!musicChoice);
            pauseMusic(checkedData);
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
              props.setMusic(!setMusicState);
              if (!setMusicState) {
                playMusic(songData[currentIndex]);
                setCheckedData(songData[currentIndex]);
                getmusicInfo(songData[currentIndex]);
              } else {
                pauseMusic(songData[currentIndex]);
                setCheckedData({});
                getmusicInfo({});
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
    paddingHorizontal: 15,
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
