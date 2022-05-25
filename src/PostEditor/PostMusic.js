import React, { createRef, useRef } from 'react';

import {
    StyleSheet, View, Text, Image, Dimensions, TouchableOpacity,
    Pressable, TextInput, FlatList, KeyboardAvoidingView, Platform, Keyboard
} from 'react-native'

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';

import ScrollBottomSheet from 'react-native-scroll-bottom-sheet';

import Animated, {
    Extrapolate,
    interpolateNode,
    EasingNode,
    Value
} from 'react-native-reanimated';

import { State, TapGestureHandler } from 'react-native-gesture-handler';

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
            bottomSheetRefreshing: false,
        };
        this.defaultSelectPostion = 2;
        this.page = 1;
        this.pageSize = 20;
        this.isMore = true;
        this.initMusic = true;
        this.playingMusic = null;
        this.lasePlayingMusic = null;
        this.cacheMusicList = [];


        this.animatedPositionCurate = React.createRef();
        this.animatedPositionCurate.current = new Animated.Value(0.98)

        this.transY = interpolateNode(this.animatedPositionCurate?.current, {
            inputRange: [0, 1],
            outputRange: [64, 0],
            extrapolate: Extrapolate.CLAMP
        });

    }

    componentDidMount() {
        this.getMusic();

    }

    componentWillUnmount() {
        this.stopMusic();
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.musicSearchValue !== this.state.musicSearchValue) {
            this.getSearchMusic(nextState.musicSearchValue);
            return true;
        }
        if (nextState.musicList !== this.state.musicList) {
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
            return true;
        }
        if (nextProps.openMusicView !== this.props.openMusicView) {
            if (nextProps.openMusicView) {
                this.openBottomSheet();
            }
            return true;
        }

        if (nextProps.isPlay !== this.props.isPlay) {
            if (nextProps.isPlay) {
                if (!!this.playingMusic) {
                    AVService.resumeMusic(this.playingMusic?.songID);
                }
            } else {
                if (!!this.playingMusic) {
                    AVService.pauseMusic(this.playingMusic?.songID);
                }
            }
            return true;
        }
        return false;
    }

    pauseMusic = async () => {
        if (!!this.playingMusic) {
            AVService.stopMusic(this.playingMusic?.songID);
        }
    }

    playMusic = async (musicInfo) => {
        if (!!musicInfo?.songID && musicInfo?.songID !== this.playingMusic?.songID) {
            const songa = await AVService.playMusic(musicInfo?.songID);
            this.playingMusic = songa;
            this.lasePlayingMusic = songa;
        }
    }

    stopMusic = async () => {
        if (!!this.playingMusic) {
            AVService.stopMusic(this.playingMusic?.songID);
            this.playingMusic = null;
        }
    }

    setCurrentMusic = (musicInfo) => {
        this.props.setCurrentMusic(musicInfo)
    }


    getSearchMusic = async (name = '') => {
        if (name) {
            const musics = await AVService.getMusics({ name: name, page: this.page, pageSize: this.pageSize });
            this.setState({
                musicList: musics,
                bottomSheetRefreshing: false
            });
        } else {
            //搜索输入为空，加载分页缓存的 list
            this.setState({
                musicList: this.cacheMusicList,
                bottomSheetRefreshing: false
            });
        }

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
                if (!!musicList?.length && musicList?.length > 0) {
                    this.setCurrentMusic(musicList[this.defaultSelectPostion]);
                }
            }
            //这里把分页加载的数据缓存一份
            this.cacheMusicList = musicList.slice();
        }
    }

    openBottomSheet = () => {
        this.refScrollBottomSheet?.current?.snapTo(0);
    }


    hideBottomSheet = () => {
        Keyboard.dismiss();
        this.refScrollBottomSheet?.current?.snapTo(1);
        setTimeout(() => {
            this.props.onCloseView();
        }, 500);
    }



    MusicHeadView = () => {
        return (
            <Animated.View style={styles.headContinue}>
                <Animated.View style={{ backgroundColor: '#D8D8D8', width: 32, height: 4, borderRadius: 2, marginTop: 10 }} />
                <Animated.View style={styles.searchMusicContinue}>
                    <Animated.Image source={require('../../images/ic_post_music_ search.png')} style={styles.searchMusicImage} />
                    <TextInput
                        style={[styles.musicFindSearchInput]}
                        multiline={false}
                        maxLength={30}
                        autoFocus={false}
                        textAlignVertical={'center'}
                        onChange={(e) => {
                            if (e?.nativeEvent) {
                                const searchValue = e?.nativeEvent?.text?.trim()
                                this.setState({ musicSearchValue: searchValue });
                            }
                        }}
                        value={this.state.musicSearchValue}
                        selectionColor='#836BFF'
                        placeholder={I18n.t('search_music_placeholder')}
                        placeholderTextColor="#83848A"
                        color="#000000"
                    />

                    {!!this.state.musicSearchValue && (
                        <TapGestureHandler
                            shouldCancelWhenOutside={true}
                            onHandlerStateChange={(event) => {
                                if (event.nativeEvent.state === State.END) {
                                    this.setState({ musicSearchValue: '' });
                                }
                            }}
                        >
                            <Animated.View style={{ position: 'absolute', right: 0, top: 0, bottom: 0, width: 38, justifyContent: 'center', alignItems: 'center' }}>
                                <Image source={require('../../images/ic_post_music_close.png')} style={{ width: 18, height: 18 }} />
                            </Animated.View>
                        </TapGestureHandler>
                    )}
                </Animated.View>
            </Animated.View>
        )
    }


    MusicFootView = () => {
        return (
            <Animated.View
                style={
                    [
                        styles.bottomMusicCheckContainer,
                        {
                            transform: [
                                {
                                    translateY: this.transY
                                }
                            ]
                        }
                        , { height: this.props.openMusicView ? 64 : 0 }
                    ]
                }
            >
                <TapGestureHandler
                    shouldCancelWhenOutside={true}
                    onHandlerStateChange={(event) => {
                        if (event.nativeEvent.state === State.END) {
                            if (this.props.currentMusic) {
                                this.setCurrentMusic(null)
                            } else {
                                this.setCurrentMusic(this.lasePlayingMusic);
                            }
                        }
                    }}
                >
                    <Animated.View style={{ width: '100%', height: '100%', justifyContent: 'flex-start', alignItems: 'center', flexDirection: 'row', }}>
                        <Image source={this.props.currentMusic ? require('../../images/ic_post_music_checked.png') : require('../../images/ic_post_music_unchecked.png')}
                            style={{ width: 18, height: 18, display: this.props.openMusicView ? 'flex' : 'none' }} />
                        <Text style={{ color: '#000000', fontSize: 16, fontWeight: '500', marginStart: 10 }}>{I18n.t('Soundtrack')}</Text>
                    </Animated.View>
                </TapGestureHandler>
            </Animated.View >
        )
    }


    render() {

        return (
            <Animated.View style={[styles.continue, { height: this.props.openMusicView ? '100%' : 0, }]}>
                <TapGestureHandler
                    shouldCancelWhenOutside={true}
                    onHandlerStateChange={(event) => {
                        if (event.nativeEvent.state === State.END) {
                            this.hideBottomSheet();

                        }
                    }}
                >
                    <Animated.View style={[styles.continue, { height: this.props.openMusicView ? '100%' : 0, }]}>
                        <TapGestureHandler onHandlerStateChange={true} >
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

                                animatedPositionCurate={this.animatedPositionCurate?.current}

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

                                renderHandle={() => this.MusicHeadView()}
                                renderItem={({ index, item }) => {
                                    return <MusicItem
                                        {...this.props}
                                        index={index}
                                        item={item}
                                        onItemClick={(position, isSelected) => {
                                            if (!isSelected) {
                                                this.stopMusic()
                                                this.setCurrentMusic(item)
                                            } else {
                                                this.setCurrentMusic(null)
                                            }
                                        }}
                                    />
                                }

                                }
                            />
                        </TapGestureHandler>
                        {this.MusicFootView()}
                    </Animated.View>
                </TapGestureHandler>
            </Animated.View >
        )
    }

}

class MusicItem extends React.Component {
    constructor(props) {
        super(props);
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextProps.item !== this.props.item) {
            return true;
        }
        if (nextProps.currentMusic !== this.props.currentMusic) {
            return true;
        }
        return false;
    }

    render() {
        const { item, index, currentMusic } = this.props;
        const isSelected = item?.songID === currentMusic?.songID
        return (
            <TapGestureHandler
                shouldCancelWhenOutside={true}
                onHandlerStateChange={(event) => {
                    if (event.nativeEvent.state === State.END) {
                        this.props.onItemClick(index, isSelected);
                    }
                }}
            >
                <Animated.View style={styles.itemContainer}>
                    <Animated.Image source={isSelected ? require('../../images/ic_post_item_music.png') : require('../../images/ic_post_item_music_unselect.png')} style={{ width: 14, height: 16 }} />
                    <Animated.View style={styles.itemMusicName}>
                        <Animated.Text style={[styles.itemMusicNameText, { color: isSelected ? '#7166F9' : '#000000' }]}>{item.name}</Animated.Text>
                    </Animated.View>
                </Animated.View>
            </TapGestureHandler>
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
        width: '100%', height: '100%', position: 'absolute', zIndex: 100
    },

    contentContainerStyle: {
        overflow: 'hidden',
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12,
        backgroundColor: '#fff',
        justifyContent: 'space-between',
        paddingBottom: 70,
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
    },
    bottomMusicCheckContainer: {
        paddingLeft: '4.5%',
        justifyContent: 'flex-start',
        alignItems: 'center',
        backgroundColor: 'white',
        position: 'absolute',
        zIndex: 3,
        bottom: 0,
        height: 64,
        width: '100%',
        flexDirection: 'row',
    }


})