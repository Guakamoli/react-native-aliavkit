import React from 'react';

import { AppState, StyleSheet, View, Text, Image, Dimensions, TouchableOpacity, Pressable } from 'react-native'

import CameraRoll from '@react-native-community/cameraroll';

import RNGetPermissions from '../permissions/RNGetPermissions';

import ScrollBottomSheet from 'react-native-scroll-bottom-sheet';

import FastImage from '@rocket.chat/react-native-fast-image';

import { State, NativeViewGestureHandler } from 'react-native-gesture-handler';

import I18n from '../i18n';

import Toast, { DURATION } from 'react-native-easy-toast';

const { width, height } = Dimensions.get('window');

const photoItemWidth = (width - 2) / 3.0;
const photoItemHeight = photoItemWidth * 16 / 9;


class PototItemView extends React.Component {
    constructor(props) {
        super(props)
    }

    shouldComponentUpdate(nextProps, nextState) {
        return false;
    }

    formatSeconds = (s) => {
        let t = '';
        if (s > -1) {
            let min = Math.floor(s / 60) % 60;
            let sec = s % 60;
            if (min < 10) {
                t += '0';
            }
            t += min + ':';
            if (sec < 10) {
                t += '0';
            }
            t += sec;
        }
        return t;
    };
    render() {
        let videoDuration = this.formatSeconds(Math.ceil(this.props.item.image.playableDuration ?? 0));
        return (
            <View style={[styles.bottomSheetItem, { marginStart: this.props.index % 3 === 0 ? 0 : 1 }]}>
                <Pressable
                    onPress={async () => {
                        if (this.props.item.type === 'video' && this.props.item.image.playableDuration && this.props.item.image.playableDuration > 60.0) {
                            this.toastRef?.show?.(`${I18n.t('selected_video_time_60')}`, 2000);
                            return;
                        }
                        let selectUri = this.props.item.image.uri;
                        let myAssetId = selectUri.slice(5);
                        selectUri = await CameraRoll.requestPhotoAccess(myAssetId);
                        this.props.selectedPhoto(selectUri, this.props.item.type);
                        setTimeout(() => {
                            this.props.hideBottomSheet();
                        }, 250);
                    }}>
                    <Image style={{ width: '100%', height: '100%' }} resizeMode='center' source={{ uri: this.props.item?.image?.uri }} />
                </Pressable>

                {this.props.item.type === 'video' && <Text style={styles.bottonSheetItemVideoTime}>{videoDuration}</Text>}

                {/* {!this.state.singleSelect && (
                    <Pressable
                        style={[styles.bottomSheetItemCheckbox, {}]}
                        hitSlop={{ left: 10, top: 10, right: 10, bottom: 10 }}
                        onPress={() => {
                            item.isSelected = !item.isSelected;
                            this.forceUpdate()
                        }}>
                        <View style={[styles.bottomSheetItemCheckImage, {}]}>
                            {item?.isSelected &&
                                < FastImage style={styles.bottomSheetItemCheckImage} source={require('../../images/postFileSelect.png')} />
                            }
                        </View>
                    </Pressable>
                )} */}
            </View>
        )
    }

}

class StoryPhoto extends React.Component {

    constructor(props) {
        super(props)
        this.multipleSelectNumber = props.multipleSelectNumber ? props.multipleSelectNumber : 5;

        this.state = {
            singleSelect: props.singleSelect ? props.singleSelect : true,
            photoList: [],
            multipleSelectList: [],
            bottomSheetRefreshing: false,
        };
        this.bottomSheetRef;
        this.toastRef;
        this.getPhotosNum = 36;
    }

    /**
     * 在第一次绘制 render() 之后执行
     */
    componentDidMount() {
        this.getPhotos();
    }

    /**
     * setState 刷新时触发
     * @returns true 会继续更新； false 不会执行 render
     */
    shouldComponentUpdate(nextProps, nextState) {
        if (this.props.openPhotos !== nextProps.openPhotos) {
            if (nextProps.openPhotos) {
                this.openBottomSheet();
            } else {
                this.hideBottomSheet();
            }
            return true;
        }
        if (this.state.photoList !== nextState.photoList) {
            return true;
        }
        if (this.state.singleSelect !== nextState.singleSelect) {
            return true;
        }
        if (this.state.multipleSelectList !== nextState.multipleSelectList) {
            return true;
        }
        if (this.state.bottomSheetRefreshing !== nextState.bottomSheetRefreshing) {
            return true;
        }
        return false;
    }

    /**
     * 销毁
     */
    componentWillUnmount() {
        //当组件要被从界面上移除的时候调用 ,可以做组件相关的清理工作
    }


    getPhotos = async () => {
        if (!(await RNGetPermissions.checkStoragePermissions())) {
            if (await RNGetPermissions.getStoragePermissions(true)) {
                this.getPhotos();
            }
            return;
        }
        CameraRoll.getPhotos({
            first: this.getPhotosNum,
            assetType: 'All',
            include: ['playableDuration', 'filename', 'fileSize', 'imageSize'],
        })
            .then(data => {
                if (!data?.edges?.length) {
                    return;
                }
                const photoList = [];
                for (let i = 0; i < data.edges.length; i++) {
                    const itemInfo = data.edges[i].node
                    photoList.push(data.edges[i].node);
                }
                let firstPhotoUri = photoList[0]?.image?.uri
                this.props.setFirstPhotoUri(firstPhotoUri);
                this.setState({
                    photoList: photoList,
                    bottomSheetRefreshing: false
                });
            })
            .catch((err) => {
                //Error Loading Images
            });
    }

    openBottomSheet = () => {
        this.bottomSheetRef?.snapTo(0);
        this.resetToolsBotton(false);
    }

    hideBottomSheet = () => {
        this.bottomSheetRef?.snapTo(1);
        this.resetToolsBotton(true);
    }

    resetToolsBotton = (isShow = false) => {
        if (isShow) {
            this.props.showBottomTools()
        } else {
            this.props.hideBottomTools()
        }
    }

    PhotoView = () => {
        return (
            <View style={[styles.photoView, { height: height ,backgroundColor:'rgba(255,0,0,0.2)' }]}>
                <ScrollBottomSheet
                    ref={(ref) => (this.bottomSheetRef = ref)}
                    componentType="FlatList"
                    //snapPoints 是组件距离屏幕顶部的距离
                    snapPoints={[this.props.insets.top + this.props.insets.bottom, height]}
                    //初始显示对应 snapPoints 中的下标
                    initialSnapIndex={1}
                    data={this.state.photoList}
                    keyExtractor={(item, index) => {
                        return index
                    }}
                    friction={0.8}
                    animationType={'spring'}
                    numColumns={3}
                    initialNumToRender={9}
                    refreshing={this.state.bottomSheetRefreshing}
                    enableOverScroll={true}
                    contentContainerStyle={styles.contentContainerStyle}
                    onSettle={(index) => {
                        if (index === 1) {
                            this.resetToolsBotton(true);
                            this.props.onCloseView();
                        }
                        console.info("onSettle",index);
                    }}

                    onEndReachedThreshold={0.5}
                    onEndReached={() => {
                        //上拉加载更多
                        this.getPhotosNum += 18;
                        this.setState({ bottomSheetRefreshing: true });
                        this.getPhotos();
                    }}
                    renderHandle={() => (
                        <View style={styles.bottomSheetHead}>
                            {Platform.OS === 'android' ?
                                <NativeViewGestureHandler
                                    disallowInterruption={true}
                                    shouldActivateOnStart={true}
                                    onHandlerStateChange={(event) => {
                                        if (event.nativeEvent.state === State.END) {
                                            this.hideBottomSheet();
                                        }
                                    }}
                                >
                                    <View style={styles.bottomSheetHeadClose}>
                                        <Text style={styles.bottomSheetHeadCloseText}>关闭</Text>
                                    </View>
                                </NativeViewGestureHandler>
                                :
                                <TouchableOpacity
                                    style={styles.bottomSheetHeadClose}
                                    hitSlop={{ left: 10, top: 10, right: 20, bottom: 10 }}
                                    onPress={() => {
                                        console.info("renderHandle");
                                        this.hideBottomSheet();
                                    }}>
                                    <Text style={styles.bottomSheetHeadCloseText}>关闭</Text>
                                </TouchableOpacity>
                            }

                        </View>
                    )}
                    renderItem={({ index, item }) => (
                        <PototItemView
                            {...this.props}
                            index={index}
                            item={item}
                            hideBottomSheet={this.hideBottomSheet}
                        />

                    )}
                />
            </View>
        )
    }


    render() {
        return (
            <View style={[styles.container, { height:  height  }]}>
                <Toast
                    ref={(ref) => (this.toastRef = ref)}
                    position='center'
                    opacity={0.8}
                />
                {this.PhotoView()}
            </View >
        );
    }

}

const styles = StyleSheet.create({
    container: {
        width: '100%', height: '100%', position: 'relative',
    },
    btnContainer: {
        position: 'absolute', left: 20, bottom: 0, width: 25, height: 25, borderRadius: 4, overflow: 'hidden'
    },

    photoView: {
        flex: 1,
        width: width,
        zIndex: 1,
    },
    contentContainerStyle: {
        backgroundColor: '#000',
        justifyContent: 'space-between',
    },
    bottomSheetHead: {
        width: "100%",
        height: 55,
        backgroundColor: 'black',
        alignItems: 'flex-end',
        justifyContent: 'center',
    },

    bottomSheetHeadClose: {
        width: 52,
        lineHeight: 52,
        height: 28,
        marginRight: 15,
        borderRadius: 14,
        backgroundColor: 'white',
        justifyContent: 'center',
        alignItems: 'center',
    },

    bottomSheetHeadCloseText: {
        color: 'rgba(0,0,0,0.8)',
        fontSize: 14,
    },

    bottomSheetItem: {
        width: photoItemWidth,
        height: photoItemHeight,
        marginBottom: 1,
        position: 'relative',
    },

    bottomSheetItemCheckbox: {
        borderRadius: 22,
        borderWidth: 1,
        width: 22,
        height: 22,
        borderColor: 'white',
        backgroundColor: 'rgba(255,255,255,0.3)',
        overflow: 'hidden',
        position: 'absolute',
        right: 6,
        top: 6,
        justifyContent: 'center',
        alignItems: 'center',
    },

    bottomSheetItemCheckImage: {
        width: 22,
        height: 22,
    },

    bottonSheetItemVideoTime: {
        position: 'absolute',
        bottom: 6,
        right: 6,
        lineHeight: 17,
        fontSize: 12,
        color: 'rgba(255,255,255,1)',
        textShadowColor: 'rgba(0,0,0,0.5)',

    }


});

export default StoryPhoto