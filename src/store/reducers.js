import { combineReducers } from 'redux';
import shootPost from '../reducers/post';
import shootStory from '../reducers/story';
import shootContainer from '../reducers/container';


export default combineReducers({
    shootPost,
    shootStory,
    shootContainer
});
export {
    shootPost,
    shootStory,
    shootContainer
}
