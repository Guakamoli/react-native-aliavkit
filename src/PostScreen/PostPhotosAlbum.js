import React, { Component } from 'react';

import {
    StyleSheet,
    Dimensions,
    Platform,
    AppState,
    Alert,
} from 'react-native';

import _, { lte } from 'lodash';
import I18n from '../i18n';

import AVkitPhotoView from '../AVKitPhotoView';

import { request, requestMultiple, check, checkMultiple, openSettings, PERMISSIONS, RESULTS } from 'react-native-permissions';

const { width, height } = Dimensions.get('window');



export default class PostPhotosAlbum extends Component {

    constructor(props) {
        super(props);
        this.appState = '';
        this.state = {
            isStoragePermission: false
        };
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


    //WUYQTODO
    onSelectedPhotoCallback = (data) => {
        if (!!this.props.setMultipleData) {
            this.props.setMultipleData(data)
        }
        // console.info("multipleData",props.multipleData);
        // console.info("selectMultiple",props.selectMultiple);
        // console.info("setSelectMultiple",props.setSelectMultiple);
        // console.info("setMultipleData",props.setMultipleData);
    }

    onMaxSelectCountCallback = (data) => {
        this.props.toastRef.current.show(`${I18n.t('Select_up_to_ten_pictures')}`, 2000);
    }


    render() {
        if (!this.state.isStoragePermission) {
            return null
        }
        return (
            <AVkitPhotoView {...this.props}
                style={{ height: height - 44 - 50 - width, width: width, backgroundColor: 'black' }}
                multiSelect={this.props.selectMultiple}
                onSelectedPhotoCallback={this.onSelectedPhotoCallback}
                onMaxSelectCountCallback={this.onMaxSelectCountCallback}
            ></AVkitPhotoView>
        )
    }

}