import * as types from './actionsTypes';

export function setType(data) {
    return {
        type: types.CONTAINER.SET_TYPE,
        data
    };
}


