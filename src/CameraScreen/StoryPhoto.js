import React from 'react';

import { AppState, StyleSheet, View, Text, Image, Dimensions, TouchableOpacity, Pressable } from 'react-native'

import CameraRoll from '@react-native-community/cameraroll';

import RNGetPermissions from '../permissions/RNGetPermissions';

import ScrollBottomSheet from 'react-native-scroll-bottom-sheet';

import FastImage from '@rocket.chat/react-native-fast-image';

import { State, NativeViewGestureHandler } from 'react-native-gesture-handler';

import I18n from '../i18n';

const { width, height } = Dimensions.get('window');

const photoItemWidth = (width - 2) / 3.0;
const photoItemHeight = photoItemWidth * 16 / 9;

import AVService from '../AVService';
class PhotoItemView extends React.Component {
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

    onItemClick = async () => {

        const videoType = this.props.item?.type?.indexOf('video') !== -1;

        if (!!videoType && this.props.item.image.playableDuration && this.props.item.image.playableDuration > 60.0) {
            console.info("onItemClick", videoType, this.props.item.image.playableDuration);
            this.props.myRef?.current?.show?.(`${I18n.t('selected_video_time_60')}`, 2000);
            return;
        }
        let selectUri = this.props.item.image.uri;

        if (Platform.OS === 'ios') {
            if ("image" === this.props.item.type) {
                const imageIndex = this.props.item?.image?.filename?.lastIndexOf(".");
                //获取后缀
                const imageType = this.props.item?.image?.filename?.substr(imageIndex + 1).toLowerCase();

                console.info("imageType", imageType);

                //不是通用格式，需要先转换
                if (!!imageType && (imageType !== 'jpg' || imageType !== 'png')) {
                    selectUri = await AVService.saveToSandBox(selectUri);
                } else {
                    let myAssetId = selectUri.slice(5);
                    selectUri = await CameraRoll.requestPhotoAccess(myAssetId);
                }
            } else {
                let myAssetId = selectUri.slice(5);
                selectUri = await CameraRoll.requestPhotoAccess(myAssetId);
            }
        }
        console.info("selectUri", selectUri, "item type", this.props.item.type.includes('video'), this.props.item.type, this.props.item.image);
        this.props.selectedPhoto(selectUri, this.props.item.type.includes('video') ? 'video' : 'image');
        setTimeout(() => {
            this.props.hideBottomSheet();
        }, 250);
    }

    render() {
        let videoDuration = this.formatSeconds(Math.ceil(this.props.item.image.playableDuration ?? 0));

        const videoType = this.props.item?.type?.indexOf('video') !== -1;

        return (
            <View style={[styles.bottomSheetItem, { marginStart: this.props.index % 3 === 0 ? 0 : 1 }]}>

                {Platform.OS === 'android' ?
                    <NativeViewGestureHandler
                        disallowInterruption={false}
                        shouldActivateOnStart={false}
                        onHandlerStateChange={(event) => {
                            if (event.nativeEvent.state === State.END) {
                                this.onItemClick();
                            }
                        }}
                    >
                        <Image style={{ width: '100%', height: '100%' }} resizeMode='center' source={{ uri: this.props.item?.image?.uri }} />
                    </NativeViewGestureHandler>
                    :
                    <TouchableOpacity
                        onPress={() => {
                            this.onItemClick();
                        }}>
                        <Image style={{ width: '100%', height: '100%' }} resizeMode='center' source={{ uri: this.props.item?.image?.uri }} />
                    </TouchableOpacity>
                }

                {!!videoType && <Text style={styles.bottonSheetItemVideoTime}>{videoDuration}</Text>}

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
        this.bottomSheetInnerRef;

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
                // this.hideBottomSheet();
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
        setTimeout(() => {
            this.resetToolsBotton(false);
        }, 100);
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

    PhotoHandleView = () => {
        return (
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
                            this.hideBottomSheet();
                        }}>
                        <Text style={styles.bottomSheetHeadCloseText}>关闭</Text>
                    </TouchableOpacity>
                }
            </View>)
    }

    PhotoView = () => {
        return (
            <View style={[styles.photoView, { height: this.props.openPhotos ? height : 0 }]}>
                <ScrollBottomSheet
                    testID='action-sheet'
                    ref={(ref) => (this.bottomSheetRef = ref)}
                    innerRef={(ref) => (this.bottomSheetInnerRef = ref)}
                    componentType="FlatList"
                    //snapPoints 是组件距离屏幕顶部的距离
                    snapPoints={[0, height]}
                    //初始显示对应 snapPoints 中的下标
                    initialSnapIndex={1}
                    data={this.state.photoList}
                    keyExtractor={(item, index) => {
                        return index
                    }}
                    friction={0.8}
                    numColumns={3}
                    initialNumToRender={9}
                    refreshing={this.state.bottomSheetRefreshing}
                    enableOverScroll={true}
                    containerStyle={styles.contentContainerStyle}
                    contentContainerStyle={styles.contentContainerStyle}
                    onSettle={(index) => {
                        if (index === 1) {
                            this.hideBottomSheet();
                            this.props.onCloseView();
                        }
                    }}

                    onEndReachedThreshold={0.5}
                    onEndReached={() => {
                        //上拉加载更多
                        this.getPhotosNum += 18;
                        this.setState({ bottomSheetRefreshing: true });
                        this.getPhotos();
                    }}
                    renderHandle={() => this.PhotoHandleView()}
                    renderItem={({ index, item }) => (
                        <PhotoItemView
                            {...this.props}
                            index={index}
                            item={item}
                            hideBottomSheet={this.hideBottomSheet}
                        />

                    )}
                    ListEmptyComponent={() => {
                        return null;
                    }}
                />
            </View>
        )
    }


    render() {
        return (
            <View style={[styles.container, { height: this.props.openPhotos ? height : 0 }]}>
                {this.PhotoView()}
            </View >
        );
    }

}

const styles = StyleSheet.create({
    container: {
        width: '100%', height: '100%', position: 'absolute', zIndex: 999, //backgroundColor: 'red',
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