import React from 'react';

import { AppState, StyleSheet, View, Text, Image, Dimensions, TouchableOpacity, Pressable } from 'react-native'

import CameraRoll from '@react-native-community/cameraroll';

import RNGetPermissions from '../permissions/RNGetPermissions';

import ScrollBottomSheet from 'react-native-scroll-bottom-sheet';

import FastImage from '@rocket.chat/react-native-fast-image';

const { width, height } = Dimensions.get('window');

const photoItemWidth = (width - 2) / 3.0;
const photoItemHeight = photoItemWidth * 16 / 9;


class PototItemView extends React.Component {
    constructor(props) {
        super(props)
    }

}

class StoryPhoto extends React.Component {

    constructor(props) {
        super(props)
        console.info("初始化:")

        this.multipleSelectNumber = props.multipleSelectNumber ? props.multipleSelectNumber : 5;

        this.state = {
            singleSelect: props.singleSelect ? props.singleSelect : true,
            openPhotos: false,
            firstPhotoUri: '',
            photoList: [],
            multipleSelectList: [],
        };
        this.appChangeEventListener = null;
        this.bottomSheetRef;
    }

    /**
     * 
     */
    componentDidMount() {
        console.info("在第一次绘制 render() 之后, componentDidMount", this.props.insets.top)
        this.appChangeEventListener = AppState.addEventListener('change', this.onAppChangeEventListener);
        this.getPhotos();
    }

    /**
     * setState 刷新时触发
     * @returns true 会继续更新； false 不会执行 render
     */
    shouldComponentUpdate(nextProps, nextState) {
        if (this.state.openPhotos !== nextState.openPhotos) {
            return true;
        }
        if (this.state.firstPhotoUri !== nextState.firstPhotoUri) {
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
        return false;
    }

    /**
     * 销毁
     */
    componentWillUnmount() {
        //当组件要被从界面上移除的时候调用 ,可以做组件相关的清理工作
        this.appChangeEventListener?.remove('change', this.onAppChangeEventListener);
    }

    onAppChangeEventListener = (nextAppState) => {
        // if (this.state.appState.match(/inactive|background/) && nextAppState === 'active') {
        if (nextAppState === 'active') {
            //进入前台
        } else {
            //进入后台
        }
    }

    getPhotos = async () => {
        if (!(await RNGetPermissions.checkStoragePermissions())) {
            if (await RNGetPermissions.getStoragePermissions(true)) {
                this.getPhotos();
            }
            return;
        }
        CameraRoll.getPhotos({
            first: 40,
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
                console.info("firstPhotoUri", photoList[0]);
                this.setState({
                    firstPhotoUri: firstPhotoUri,
                    photoList: photoList
                });
            })
            .catch((err) => {
                //Error Loading Images
            });
    }

    openBottomSheet = () => {
        this.setState({ openPhotos: true });
        this.bottomSheetRef?.snapTo(0);
        this.resetToolsBotton(false);
    }
    hideBottomSheet = () => {
        this.bottomSheetRef?.snapTo(1);
        // this.resetToolsBotton(true);
    }

    resetToolsBotton = (isShow = false) => {
        if (isShow) {
            this.props.showBottomTools()
        } else {
            this.props.hideBottomTools()
        }
    }

    PototItemView = (index, item) => {
        console.info(index, item.image.uri);
        return (
            <View style={[styles.bottomSheetItem, { marginStart: index % 3 === 0 ? 0 : 1 }]}>
                <Image style={{ width: '100%', height: '100%' }} resizeMode='contain' resizeMethod='resize' source={{ uri: item?.image?.uri }} />
                {!this.state.singleSelect && (
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
                )}
            </View>
        )
    }

    PhotoView = () => {
        return (
            <View style={[styles.photoView, { height: this.state.openPhotos ? height : 0 }]}>
                <ScrollBottomSheet
                    ref={(ref) => (this.bottomSheetRef = ref)}
                    componentType="FlatList"
                    //snapPoints 是组件距离屏幕顶部的距离
                    snapPoints={[this.props.insets.top + this.props.insets.bottom, height]}
                    //初始显示对应 snapPoints 中的下标
                    initialSnapIndex={1}
                    data={this.state.photoList}
                    keyExtractor={(item, index) => {
                        return item.image.uri
                    }}
                    friction={0.8}
                    animationType={'spring'}
                    numColumns={3}
                    initialNumToRender={12}
                    refreshing={true}
                    enableOverScroll={true}
                    contentContainerStyle={styles.contentContainerStyle}
                    onSettle={(index) => {
                        if (index === 1) {
                            this.setState({ openPhotos: false });
                            this.resetToolsBotton(true);
                        }
                    }}
                    renderHandle={() => (
                        <View style={styles.bottomSheetHead}>
                            <TouchableOpacity
                                style={styles.bottomSheetHeadClose}
                                hitSlop={{ left: 10, top: 10, right: 20, bottom: 10 }}
                                onPress={() => {
                                    this.hideBottomSheet();
                                }}>
                                <Text style={styles.bottomSheetHeadCloseText}>关闭</Text>
                            </TouchableOpacity>

                        </View>
                    )}
                    renderItem={({ index, item }) => (
                        this.PototItemView(index, item)
                    )}
                />
            </View>
        )
    }


    render() {
        return (
            <View style={styles.container}>
                <View style={[styles.btnContainer, { bottom: this.props.toolsInsetBottom + 5 }]}>
                    <TouchableOpacity
                        hitSlop={{ left: 10, top: 10, right: 20, bottom: 10 }}
                        onPress={() => {
                            this.openBottomSheet();
                        }}>
                        <Image key={!!this.state.firstPhotoUri ? 'firstPhotoUri' : 'require'} style={{ width: 25, height: 25 }} resizeMode='stretch' resizeMethod='resize'
                            source={!!this.state.firstPhotoUri ? { uri: this.state.firstPhotoUri } : require('../../images/ic_story_photo.png')} />
                    </TouchableOpacity>
                </View>
                {this.PhotoView()}
            </View >
        );
    }

}

const styles = StyleSheet.create({
    container: {
        width: '100%', height: '100%'
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
        marginBottom: 1,
        height: 219,
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


});

export default StoryPhoto