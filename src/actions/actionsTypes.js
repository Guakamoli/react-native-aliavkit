
function createRequestTypes(base, types = defaultTypes) {
    const res = {};
    types.forEach(type => (res[type] = `${base}_${type}`));
    return res;
}
export const POST = createRequestTypes('SHOOT_POST', [
    'SET_SELECT_MULTIPLE',
    'SET_MULTIPLEDATA',
]);
export const STORY = createRequestTypes('SHOOT_STORY', [
    'SET_FACE_PASEER_INFO',
    'SET_CAMERA_TYPE',
    'SET_SHOWBEAUTIFY',
    'SET_NORMAL_BEAUTY_LEVEl'
]);
