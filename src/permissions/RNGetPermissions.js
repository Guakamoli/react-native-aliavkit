
import { Platform, Alert, } from 'react-native';
import { request, requestMultiple, check, checkMultiple, openSettings, PERMISSIONS, RESULTS } from 'react-native-permissions';
import I18n from '../i18n';

export default class RNGetPermissions {

    /**
     * 检测是否有存储权限（Android）/ 相册权限（iOS）
     */
    static checkStoragePermissions = async (isToSetting = false, isCheckLimited = true) => {
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
                return true;
            } else if (statuses === RESULTS.BLOCKED) {
                if (isToSetting) {
                    this.showToSettingAlert();
                }
            } else if (statuses === RESULTS.LIMITED) {
                //受限，iOS 只可以访问指定选中的照片
                if (isCheckLimited) {
                    await new Promise((resolved) => {
                        setTimeout(() => {
                            resolved()
                        }, 300);
                    })
                }
                return isCheckLimited;
            }
        }
        return false;
    }

    /**
     *  获取存储权限
     * @param isToSetting  是否展示去设置的 Alert
     */
    static getStoragePermissions = async (isToSetting = false) => {
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
                return true;
            } else if (statuses === RESULTS.BLOCKED) {
                if (isToSetting) {
                    this.showToSettingAlert();
                }
            } else if (statuses === RESULTS.LIMITED) {
                return true;
            }
        }
        return false;
    };



    static showToSettingAlert = () =>
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

}