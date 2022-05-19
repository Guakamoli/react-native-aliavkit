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
            musicSearchValue: ''
        };
        this.page = 1;
    }

    componentDidMount() {
        this.getMusic();
    }

    componentWillUnmount() {
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.musicSearchValue !== this.state.musicSearchValue) {
            console.info("musicSearchValue", nextState.musicSearchValue);
            return true;
        }
        if (nextState.musicList !== this.state.musicList) {
            // console.info("nextState.musicList", nextState.musicList.length, nextState.musicList);
            return true;
        }
        return false;
    }

    getMusic = async (name = '') => {
        const musics = await AVService.getMusics({ name: name, page: this.page, pageSize: 10 });
        const musicList = this.state.musicList.concat(musics);
        this.setState({ musicList });
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
            <View style={styles.continue}>
                <ScrollBottomSheet
                    testID='action-sheet'
                    ref={this.refScrollBottomSheet}
                    innerRef={(ref) => (this.innerRefScrollBottomSheet = ref)}
                    componentType='FlatList'
                    snapPoints={[height / 3, height]}
                    initialSnapIndex={0}
                    data={this.state.musicList}
                    keyExtractor={(item, index) => {
                        return index
                    }}
                    enableOverScroll={true}
                    containerStyle={styles.contentContainerStyle}

                    onSettle={(index) => {
                        if (index === 1) {

                        }
                    }}

                    onEndReached={() => {
                        //上拉加载更多
                    }}

                    renderHandle={() => this.MusicHandleView()}
                    // ListFooterComponent={() => this.MusicHandleView()}
                    renderItem={({ index, item }) => (
                        <MusicItem
                            {...this.props}
                            index={index}
                            item={item}
                        />
                    )}
                >

                </ScrollBottomSheet>
            </View>
        )
    }

}

class MusicItem extends React.Component {
    constructor(props) {
        super(props);
    }

    shouldComponentUpdate(nextProps, nextState) {
        return false;
    }

    render() {
        const { musicIconPng, musicIcongray, item, index } = this.props;
        return (
            <View style={styles.itemContainer}>
                <Image source={musicIconPng} style={{ width: 40, height: 40 }}></Image>
                <TouchableOpacity style={styles.itemMusicName}>
                    <Text style={styles.itemMusicNameText}>{item.name}</Text>
                </TouchableOpacity>
            </View>
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
        position: "absolute",
        left: 0,
        top: 0,
        right: 0,
        bottom: 0,
    },

    contentContainerStyle: {
        overflow: 'hidden',
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12,
        backgroundColor: '#fff',
        justifyContent: 'space-between',
        paddingBottom: 60,
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