import React, { Component } from 'react';
import { View, StyleSheet, TouchableOpacity, Text, Alert, NativeModules } from 'react-native';
import VideoEditor from '../../src/VideoEditor';
const { RNEditViewManager, RNMusicService } = NativeModules;

export default class VideoEditorExample extends Component {
  constructor(props) {
    super(props);
    this.state = {
      videoPath: '',
      imagePath: '',
      filterName: '原片',
      startExportVideo: false,
      thumbnails: [],
      videoMute: false,
    };
    this.onExportVideo = this.onExportVideo.bind(this);

    this.getFilters();
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

  async getThumbnails() {
    const imgPaths = await RNEditViewManager.generateImages({
      videoPath: this.state.videoPath,
      duration: 10,
      startTime: 0,
      itemPerTime: 1000,
    });
    console.log(imgPaths);
  }

  async trimVideo() {
    const result = await RNEditViewManager.trimVideo({
      videoPath: this.state.videoPath,
      startTime: 2.0,
      endTime: 8.0,
    });
    console.log(result);
  }

  async getFilters() {
    //{iconPath: '.../柔柔/icon.png', filterName: '柔柔'}
    const infos = await RNEditViewManager.getFilterIcons({});
    console.log('-------:', infos);
  }

  async downloadMusic() {
    const res = await RNMusicService.downloadMusic('Berlin - Take My Breath Away.mp3');
    console.log(res);
  }

  render() {
    return (
      <View style={styles.outContainer}>
        <VideoEditor
          ref={(edit) => (this.editor = edit)}
          style={styles.editContainer}
          filterName={this.state.filterName}
          // videoPath={this.state.videoPath}
          imagePath={this.state.imagePath}
          videoMute={this.state.videoMute}
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
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.downloadMusic()}>
              <Text>music</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.changeFilter('波普')}>
              <Text>波普</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.buttonItem}
              onPress={() => this.setState({ videoMute: !this.state.videoMute })}
            >
              <Text style={{ color: 'orange' }}>🔇静音</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.getThumbnails()}>
              <Text>抽帧</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.trimVideo()}>
              <Text>裁剪</Text>
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
