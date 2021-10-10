import React, { Component } from 'react';
import { View, StyleSheet, TouchableOpacity, Text, Alert } from 'react-native';
import VideoEditor from './VideoEditor';

export default class VideoEditorExample extends Component {
  constructor(props) {
    super(props);
    this.state = {
      videoPath: '',
      filterName: '原片',
      startExportVideo: false,
    };
    this.onExportVideo = this.onExportVideo.bind(this);
  }
  
  changeFilter(value) {
    this.setState({ filterName: value });
  }

  onExportVideo(event) {
    if (event.exportProgress === 1) {
      this.setState({ startExportVideo: false });
      Alert.alert('视频导出成功, path = ', event.outputPath);
    }
  }

  startExportVideo() {
    console.log(this.state.startExportVideo);
    if (this.state.startExportVideo) {
      return;
    }
    this.setState({ startExportVideo: true });
  }

  render() {
    return (
      <View style={styles.outContainer}>
        <VideoEditor
          ref={(edit) => (this.editor = edit)}
          style={styles.editContainer}
          filterName={this.state.filterName}
          videoPath={this.state.videoPath}
          saveToPhotoLibrary={true}
          startExportVideo={this.state.startExportVideo}
          onExportVideo={this.onExportVideo}
        >
          <View style={styles.buttonContainer}>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.startExportVideo()}>
              <Text style={{ color: 'orange' }}>导出</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.changeFilter('原片')}>
              <Text>原片</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.changeFilter('灰白')}>
              <Text>灰白</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.changeFilter('柔柔')}>
              <Text>柔柔</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.changeFilter('波普')}>
              <Text>波普</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.changeFilter('胶片')}>
              <Text>胶片</Text>
            </TouchableOpacity>
          </View>
        </VideoEditor>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  outContainer: {
    flex: 1,
    backgroundColor: 'black',
  },
  editContainer: {
    flex: 1,
    minWidth: 100,
    minHeight: 100,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonContainer: {
    flex: 1,
    flexDirection: 'row',
    position: 'absolute',
    bottom: 50,
    alignItems: 'center',
  },
  buttonItem: {
    backgroundColor: '#F5FCFF',
    width: 50,
    height: 50,
    borderRadius: 25,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
