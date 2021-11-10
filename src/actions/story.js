import * as types from './actionsTypes';

export function setFacePasterInfo(data) {
    return {
        type: types.STORY.SET_FACE_PASEER_INFO,
        data
    };
}
export function setCameraType(data) {
    return {
        type: types.STORY.SET_CAMERA_TYPE,
        data
    };
}
export function setShowBeautify(data) {
    return {
        type: types.STORY.SET_SHOWBEAUTIFY,
        data
    };
}

export function setNormalBeautyLevel(data) {
    return {
        type: types.STORY.SET_NORMAL_BEAUTY_LEVEl,
        data
    };
}