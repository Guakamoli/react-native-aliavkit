import { STORY } from '../actions/actionsTypes';

const initialState = {
    facePasterInfo: { eid: 0 },
    currentIndex: 0,
    cameraType: 'front',
    normalBeautyLevel: 3,
    showBeautify: false,

};
const cameraTypeMap = {
    front: "back",
    back: "front"
}

export default function encryption(state = initialState, action) {
    switch (action.type) {
        case STORY.SET_FACE_PASEER_INFO:
            return {
                ...state,
                facePasterInfo: action.data
            };
        case STORY.SET_CAMERA_TYPE:
            return {
                ...state,
                cameraType: action.data || cameraTypeMap[state.cameraType]
            };
        case STORY.SET_SHOWBEAUTIFY:
            return {
                ...state,
                showBeautify: action.data === undefined ? !state.showBeautify : action.data
            };
        case STORY.SET_NORMAL_BEAUTY_LEVEl:
            return {
                ...state,
                normalBeautyLevel: action.data
            };

        default:
            return state;
    }
}
