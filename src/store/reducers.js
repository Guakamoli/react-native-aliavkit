import { combineReducers } from 'redux';
import shootPost from '../reducers/post';
import shootStory from '../reducers/story';


export default combineReducers({
    shootPost,
    shootStory
});
export {
    shootPost,
    shootStory
}
