import React, { Component } from 'react';

import {
    StyleSheet,
    Dimensions,
    Platform,
    AppState,
    Alert,
    View,
    Text,
    TouchableOpacity,
} from 'react-native';

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';

import AVkitPhotoView from '../AVKitPhotoView';

import { request, requestMultiple, check, checkMultiple, openSettings, PERMISSIONS, RESULTS } from 'react-native-permissions';

const { width, height } = Dimensions.get('window');


export default class PostPhotos extends Component {

    constructor(props) {
        super(props);
        this.appState = '';
        this.state = {
            isStoragePermission: false,
            isPhotoLimited: false,
        };

        if (!!props.selectMultiple) {
            props.setSelectMultiple()
        }
    }

    getPhotos = async (isGetPermissions = false) => {
        if (!await this.checkStoragePermissions(false, true)) {
            if (isGetPermissions) {
                if (await this.getStoragePermissions(true)) {
                    this.setState({
                        isStoragePermission: true
                    });
                }
            }
            return;
        }
        this.setState({
            isStoragePermission: true
        });
    };

    _handleAppStateChange = (nextAppState) => {
        if (this.appState.match(/inactive|background/) && nextAppState === 'active') {
            this.getPhotos();
            // 在这里重新获取数据
            this.props.setVideoPlayer(true)
        } else {
            this.props.setVideoPlayer(false)
        }

        this.appState = nextAppState;
    };

    componentDidMount() {
        AppState.addEventListener('change', this._handleAppStateChange);
        this.getPhotos(true);
    }

    componentWillUnmount() {
        AppState.removeEventListener('change', this._handleAppStateChange);
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextProps.type !== this.props.type && nextProps.type === 'post') {
            this.getPhotos(true);
            return false;
        }
        if (nextState.isStoragePermission !== this.state.isStoragePermission) {
            return true;
        }
        if (nextState.isPhotoLimited !== this.state.isPhotoLimited) {
            return true;
        }
        if (nextProps.selectMultiple !== this.props.selectMultiple) {
            return true;
        }
        return false;
    }

    /**
    * 检测是否有存储权限
    */
    checkStoragePermissions = async (isToSetting = false, isCheckLimited = false) => {
        if (Platform.OS === 'android') {
            const statuses = await checkMultiple([PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE, PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE]);
            if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === RESULTS.GRANTED && statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === RESULTS.GRANTED) {
                return true;
            } else if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === RESULTS.BLOCKED || statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === RESULTS.BLOCKED) {
                //拒绝且不再询问
                if (isToSetting) {
                    this.showToSettingAlert();
                }
            }
        } else if (Platform.OS === 'ios') {
            const statuses = await check(PERMISSIONS.IOS.PHOTO_LIBRARY);
            if (statuses === RESULTS.GRANTED) {
                this.setState({ isPhotoLimited: false });
                return true;
            } else if (statuses === RESULTS.BLOCKED) {
                if (isToSetting) {
                    this.showToSettingAlert();
                }
                this.setState({ isPhotoLimited: false });
            } else if (statuses === RESULTS.LIMITED) {
                this.setState({ isPhotoLimited: true });
                return isCheckLimited;
            }
        }
        return false;
    }


    /**
       *  获取存储权限
       * @param isToSetting  是否展示去设置的 Alert
       */
    getStoragePermissions = async (isToSetting = false) => {
        if (Platform.OS === 'android') {
            const statuses = await requestMultiple([PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE, PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE]);
            if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === 'granted' && statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === 'granted') {
                return true;
            } else if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === 'denied' || statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === 'denied') {
            } else if (statuses[PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE] === 'blocked' || statuses[PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE] === 'blocked') {
                if (isToSetting) {
                    this.showToSettingAlert();
                }
            }
        } else if (Platform.OS === 'ios') {
            const statuses = await request(PERMISSIONS.IOS.PHOTO_LIBRARY);
            if (statuses === RESULTS.GRANTED) {
                this.setState({ isPhotoLimited: false });
                return true;
            } else if (statuses === RESULTS.BLOCKED) {
                this.setState({ isPhotoLimited: false });
                if (isToSetting) {
                    this.showToSettingAlert();
                }
            } else if (statuses === RESULTS.LIMITED) {
                this.setState({ isPhotoLimited: true });
                return true;
            }
        }
        return false;
    };


    showToSettingAlert = () =>
        Alert.alert(
            Platform.OS === 'ios' ? I18n.t('Need_album_permission') : "",
            Platform.OS === 'ios' ? "" : I18n.t('Need_album_permission'),
            [
                {
                    text: `${I18n.t('Not_set_yet')}`,
                    style: "default",
                },
                {
                    text: `${I18n.t('go_to_settings')}`,
                    onPress: () => openSettings(),
                    style: "default",
                },
            ],
            {
                cancelable: true,
            }
        );


    onSelectedPhotoCallback = (data) => {
        if (!!this.props.setMultipleData) {
            this.props.setMultipleData(data)
        }
    }

    onMaxSelectCountCallback = (data) => {
        this.props.toastRef.current.show(`${I18n.t('Select_up_to_ten_pictures')}`, 2000);
    }


    _onSetSelectMultiple = () => {
        this.props.setSelectMultiple();
    }

    PostPhotosAlbumHead = () => {
        return (
            <View style={styles.continueHeadView}>
                <TouchableOpacity>
                    <Text style={{ fontSize: 17, fontWeight: '500', color: '#fff', lineHeight: 24 }}>{`${I18n.t('Recent_Albums')}`}</Text>
                </TouchableOpacity>

                <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                    <TouchableOpacity onPress={this._onSetSelectMultiple}>
                        <FastImage
                            testID={`post-multiple-button`}
                            style={[styles.multipleBtnImage, { marginRight: 10 }]}
                            source={this.props.selectMultiple ? this.props.startMultipleBtnImage : this.props.multipleBtnImage}
                            resizeMode='contain'
                        />
                    </TouchableOpacity>
                </View>
            </View>
        )
    }


    render() {
        if (!this.state.isStoragePermission) {
            return null
        }
        return (
            <View>
                {this.PostPhotosAlbumHead()}
                {this.state.isPhotoLimited && <View style={{
                    width: width, height: 54, backgroundColor: '#121212',
                    justifyContent: 'space-between', alignItems: 'center', flexDirection: 'row'
                }}>
                    <Text style={{ fontSize: 14, color: '#A8A8A8', marginStart: 11 }}>{I18n.t('camera_jurisdictions')}</Text>
                    <TouchableOpacity onPress={() => {
                        openSettings();
                    }}>
                        <Text style={{ fontSize: 14, color: '#FFFFFF', height: 54, lineHeight: 54, paddingStart: 10, paddingEnd: 12 }}>{I18n.t('canera_to_setting')}</Text>
                    </TouchableOpacity>

                </View>}
                <AVkitPhotoView {...this.props}
                    style={{ height: height - 50 - 50 - width - this.props.insets.bottom, width: width, backgroundColor: 'black' }}
                    multiSelect={this.props.selectMultiple}
                    defaultSelectedStatus={true}
                    onSelectedPhotoCallback={this.onSelectedPhotoCallback}
                    onMaxSelectCountCallback={this.onMaxSelectCountCallback}
                ></AVkitPhotoView>
            </View>
        )
    }

}

const styles = StyleSheet.create({
    continueHeadView: {
        height: 50,
        backgroundColor: 'black',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 12,
    },

    multipleBtnImage: {
        width: 31,
        height: 31,
    },
})