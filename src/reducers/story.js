import { ENCRYPTION } from '../actions/actionsTypes';

const initialState = {
    facePasterInfo: { eid: 0 },
    currentIndex: 0
};

export default function encryption(state = initialState, action) {
    switch (action.type) {
        case ENCRYPTION.SET:
            return {
                ...state,
                enabled: action.enabled,
                banner: action.banner
            };
        case ENCRYPTION.SET_BANNER:
            return {
                ...state,
                banner: action.banner
            };
        case ENCRYPTION.INIT:
            return initialState;
        default:
            return state;
    }
}
