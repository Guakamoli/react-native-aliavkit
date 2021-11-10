import { CONTAINER } from '../actions/actionsTypes';

const initialState = {
    type: "post",

};

export default function postReducer(state = initialState, action) {
    switch (action.type) {
        case CONTAINER.SET_TYPE:
            return {
                ...state,
                type: action.data

            };

        default:
            return state;
    }
}
