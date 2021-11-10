import * as types from './actionsTypes';

export function setSelectMultiple(data) {
    return {
        type: types.POST.SET_SELECT_MULTIPLE,
        data
    };
}
export function setMultipleData(data) {
    return {
        type: types.POST.SET_MULTIPLEDATA,
        data
    };
}

