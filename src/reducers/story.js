import { STORY } from '../actions/actionsTypes';

const initialState = {
    facePasterInfo: { eid: 0 },
    currentIndex: 0
};

export default function encryption(state = initialState, action) {
    switch (action.type) {
        case STORY.SET:
            return {
                ...state,
                enabled: action.enabled,
                banner: action.banner
            };
        case STORY.SET_BANNER:
            return {
                ...state,
                banner: action.banner
            };
        case STORY.INIT:
            return initialState;
        default:
            return state;
    }
}
