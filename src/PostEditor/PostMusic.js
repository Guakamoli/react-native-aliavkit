import React from 'react';

import { StyleSheet, View, Text, Image, Dimensions, TouchableOpacity, Pressable, TextInput, FlatList } from 'react-native'

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';

import ScrollBottomSheet from 'react-native-scroll-bottom-sheet';

const { width, height } = Dimensions.get('window');


import AVService from '../AVService';


export default class PostMusic extends React.Component {


    constructor(props) {
        super(props)
        this.refScrollBottomSheet = React.createRef();
        this.innerRefScrollBottomSheet = React.createRef();
        this.state = {
            musicList: [],
            musicSearchValue: '',
            selectPosition: 2,
            bottomSheetRefreshing: false,
        };
        this.page = 1;
        this.pageSize = 20;
        this.isMore = true;
        this.initMusic = true;
        this.playingMusic = null;
    }

    componentDidMount() {
        this.getMusic();
    }

    componentWillUnmount() {
        this.stopMusic();
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.musicSearchValue !== this.state.musicSearchValue) {
            console.info("musicSearchValue", nextState.musicSearchValue);
            return true;
        }
        if (nextState.musicList !== this.state.musicList) {
            return true;
        }
        if (nextState.selectPosition !== this.state.selectPosition) {
            this.setSelectMusic(this.state.musicList[nextState.selectPosition])
            return true;
        }
        if (nextState.bottomSheetRefreshing !== this.state.bottomSheetRefreshing) {
            return true;
        }
        if (nextProps.currentMusic !== this.props.currentMusic) {
            if (nextProps.currentMusic) {
                this.playMusic(nextProps.currentMusic);
            } else {
                this.stopMusic();
            }
            return false;
        }
        if (nextProps.openMusicView !== this.props.openMusicView) {
            if (nextProps.openMusicView) {
                this.openBottomSheet();
            }
            return true;
        }
        return false;
    }

    playMusic = async (musicInfo) => {
        if (!!musicInfo?.songID && musicInfo?.songID !== this.playingMusic?.songID) {
            const songa = await AVService.playMusic(musicInfo?.songID);
            this.playingMusic = songa;
        }
    }

    stopMusic = async () => {
        if (!!this.playingMusic) {
            AVService.pauseMusic(this.playingMusic?.songID);
            this.playingMusic = null;

            this.setState({ selectPosition: -1 })
        }
    }

    setSelectMusic = (musicInfo) => {
        this.props.setCurrentMusic(musicInfo)
    }

    getMusic = async (name = '') => {
        const musics = await AVService.getMusics({ name: name, page: this.page, pageSize: this.pageSize });
        if (!name) {
            if (!musics?.length || musics?.length < this.pageSize) {
                this.isMore = false;
            }
            const musicList = this.state.musicList.concat(musics);
            this.setState({
                musicList: musicList,
                bottomSheetRefreshing: false
            });
            if (this.initMusic) {
                this.initMusic = false;
                if (!!musicList?.length && musicList?.length > 0 && this.state.selectPosition >= 0) {
                    this.setSelectMusic(musicList[this.state.selectPosition]);
                }
            }
        }
    }

    openBottomSheet = () => {
        this.refScrollBottomSheet?.current?.snapTo(0);
        setTimeout(() => {
            // this.setSelectMusic(this.state.musicList[nextState.selectPosition ])
        }, 100);
    }


    hideBottomSheet = () => {
        this.refScrollBottomSheet?.current?.snapTo(1);
        setTimeout(() => {
            this.props.onCloseView();
        }, 500);
    }



    MusicHandleView = () => {
        return (
            <View style={styles.headContinue}>
                <View style={{ backgroundColor: '#D8D8D8', width: 32, height: 4, borderRadius: 2, marginTop: 10 }}></View>
                <View style={styles.searchMusicContinue}>
                    <Image source={require('../../images/ic_post_music_ search.png')} style={styles.searchMusicImage} />
                    <TextInput
                        style={[styles.musicFindSearchInput]}
                        multiline={false}
                        maxLength={30}
                        autoFocus={false}
                        textAlignVertical={'center'}
                        onChange={(e) => {
                            if (e?.nativeEvent) {
                                this.setState({ musicSearchValue: e?.nativeEvent?.text });
                            }
                        }}
                        value={this.state.musicSearchValue}
                        selectionColor='#836BFF'
                        placeholder={"搜索歌曲名称"}
                        placeholderTextColor="#83848A"
                        color="#000000"
                    />

                    {!!this.state.musicSearchValue && (
                        <TouchableOpacity
                            style={{ position: 'absolute', right: 0, top: 0, bottom: 0, width: 38, justifyContent: 'center', alignItems: 'center' }}
                            onPress={async () => {
                                this.setState({ musicSearchValue: '' });
                            }}  >
                            <Image source={require('../../images/ic_post_music_close.png')} style={{ width: 18, height: 18 }} />
                        </TouchableOpacity>
                    )}


                </View>
            </View>
        )
    }


    render() {
        return (
            <View style={[styles.continue, { height: this.props.openMusicView ? '100%' : 0, display: this.props.openMusicView ? 'flex' : 'none' }]}>
                <Pressable style={[styles.continue, { height: this.props.openMusicView ? '100%' : 0 }]}
                    onPress={async () => {
                        this.hideBottomSheet();
                    }}
                >
                    <ScrollBottomSheet
                        testID='action-sheet'
                        ref={this.refScrollBottomSheet}
                        innerRef={(ref) => (this.innerRefScrollBottomSheet = ref)}
                        componentType='FlatList'
                        snapPoints={[height / 4, height]}
                        initialSnapIndex={1}
                        data={this.state.musicList}
                        keyExtractor={(item, index) => {
                            return index
                        }}
                        enableOverScroll={true}
                        containerStyle={styles.contentContainerStyle}

                        onSettle={(index) => {
                            if (index === 1) {
                                this.props.onCloseView();
                            }
                        }}

                        refreshing={this.state.bottomSheetRefreshing}
                        onEndReached={() => {
                            if (this.isMore) {
                                //上拉加载更多
                                this.page++
                                this.setState({ bottomSheetRefreshing: true });
                                this.getMusic();
                            }
                        }}

                        renderHandle={() => this.MusicHandleView()}
                        // ListFooterComponent={() => this.MusicHandleView()}
                        renderItem={({ index, item }) => (
                            <MusicItem
                                {...this.props}
                                index={index}
                                selectPosition={this.state.selectPosition}
                                item={item}
                                onItemClick={(position) => {
                                    this.setState({ selectPosition: position })
                                    this.playMusic(item)
                                }}
                            />
                        )}
                    >
                    </ScrollBottomSheet>
                </Pressable>
            </View>
        )
    }

}

class MusicItem extends React.Component {
    constructor(props) {
        super(props);
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextProps.selectPosition !== this.props.selectPosition) {
            return true;
        }
        return false;
    }

    render() {
        const { musicIconPng, musicIcongray, item, index } = this.props;
        return (
            <TouchableOpacity
                onPress={() => {
                    this.props.onItemClick(index);
                }}>
                <View style={styles.itemContainer}>
                    <FastImage source={index === this.props.selectPosition ? require('../../images/ic_post_item_music.png') : require('../../images/ic_post_item_music_unselect.png')} style={{ width: 14, height: 16 }} />
                    <View style={styles.itemMusicName}>
                        <Text style={[styles.itemMusicNameText, { color: index === this.props.selectPosition ? '#7166F9' : '#000000' }]}>{item.name}</Text>
                    </View>
                </View>
            </TouchableOpacity>
        )
    }

}


const styles = StyleSheet.create({

    headContinue: {
        height: 70,
        alignItems: 'center',
    },

    searchMusicContinue: {
        marginTop: 15,
        width: "91%",
        marginLeft: 'auto',
        marginRight: 'auto',
        height: 36,
        backgroundColor: '#F1F1F1',
        borderRadius: 6,
        alignItems: 'center',
        flexDirection: 'row',
        overflow: 'hidden',
        position: 'relative'
    },

    searchMusicImage: {
        width: 14,
        height: 14,
        marginStart: 10
    },

    musicFindSearchInput: {
        flex: 1,
        height: '100%',
        borderRadius: 12,
        marginLeft: 8,
        color: '#fff',
        marginRight: 32,

    },
    continue: {
        width: '100%', height: '100%', position: 'absolute'
    },

    contentContainerStyle: {
        overflow: 'hidden',
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12,
        backgroundColor: '#fff',
        justifyContent: 'space-between',
        paddingBottom: 20,
    },

    itemContainer: {
        width: '91%',
        marginStart: 'auto',
        marginEnd: 'auto',
        height: 63,
        flexDirection: 'row',
        flex: 1,
        justifyContent: 'center', alignItems: 'center'
    },

    itemMusicName: {
        flex: 1,
        height: 63,
        marginStart: 15,
        borderBottomWidth: 1,
        borderColor: '#E1E1E1',
    },
    itemMusicNameText: {
        fontSize: 15,
        color: '#000',
        fontWeight: '500',
        lineHeight: 63,
    }


})