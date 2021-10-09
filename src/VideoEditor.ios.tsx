import React from 'react';
import { requireNativeComponent } from 'react-native';
import PropTypes from 'prop-types';
const EditView = requireNativeComponent('RNEditView');

class VideoEditor extends React.Component {
  constructor(props) {
    super(props);
    this.editRef = React.createRef();
  }
  _onExportVideo = (event) => {
    if (!this.props.onExportVideo) {
      return;
    }
    this.props.onExportVideo(event.nativeEvent);
  };

  async getImages() {
    const imagePaths =  await this.editRef.current.generateImages({});
    return imagePaths;
  }

  render() {
    return (
      <EditView
        ref={this.editRef}
        style={{ minWidth: 100, minHeight: 100 }}
        {...this.props}
        onExportVideo={this._onExportVideo}
      ></EditView>
    );
  }
}

VideoEditor.propTypes = {
  onExportVideo: PropTypes.func,
};

export default VideoEditor;
