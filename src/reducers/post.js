import { POST } from '../actions/actionsTypes';

const initialState = {
    selectMultiple: false,
    multipleData: [],
};

export default function postReducer(state = initialState, action) {
    switch (action.type) {
        case POST.SET_SELECT_MULTIPLE:
            return {
                ...state,
                selectMultiple: !state.selectMultiple

            };
        case POST.SET_MULTIPLEDATA:
            return {
                ...state,
                multipleData: action.data
            };

        default:
            return state;
    }
}
