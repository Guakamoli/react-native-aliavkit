import React, { Component } from 'react';
import { View, StyleSheet, TouchableOpacity, Text, Alert, NativeModules } from 'react-native';
import VideoEditor from '../../src/VideoEditor';
const { RNEditViewManager } = NativeModules;
import AVService from '../../src/AVService.ios';

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
      setMusic: false,
      musicInfo: {},
      musics: [],
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

  //'play: ', { nativeEvent: { target: 685, streamProgress: 4.906666, playProgress: 4.906666 } }
  render() {
    return (
      <View style={styles.outContainer}>
        <VideoEditor
          ref={(edit) => (this.editor = edit)}
          style={styles.editContainer}
          filterName={this.state.filterName}
          videoPath={this.state.videoPath}
          // imagePath={this.state.imagePath}
          videoMute={this.state.videoMute}
          saveToPhotoLibrary={true}
          startExportVideo={this.state.startExportVideo}
          onExportVideo={this.onExportVideo}
          onPlayProgress={({ nativeEvent }) => {
            // if (nativeEvent.playEnd === true) {
            //   console.log('playEnd', nativeEvent.playEnd);
            // } else {
            //   console.log('play: ', nativeEvent.playProgress);
            // }
          }}
          musicInfo={this.state.setMusic ? this.state.musicInfo : {}}
        >
          <View style={{ flex: 1, flexDirection: 'row', justifyContent: 'center' }}>
            <TouchableOpacity
              style={{
                width: 80,
                height: 80,
                backgroundColor: '#3f0',
                justifyContent: 'center',
                alignItems: 'center',
                borderRadius: 40,
              }}
              onPress={async () => {
                const musics = await AVService.getMusics({ name: 'all-music', page: 6, pageSize:5 });
                console.log('---- getMusics: ', musics);
                this.setState({ musics });
              }}
            >
              <Text style={{ fontSize: 20, color: 'white' }}>get-musics</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={{
                width: 80,
                height: 80,
                backgroundColor: '#3f0',
                justifyContent: 'center',
                alignItems: 'center',
                borderRadius: 40,
              }}
              onPress={async () => {
                const song = await AVService.playMusic(this.state.musics[2].songID);
                console.log('---- playMusic: ', song);
                this.setState({ musicInfo: song });
              }}
            >
              <Text style={{ fontSize: 20, color: 'white' }}>play</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={{
                width: 80,
                height: 80,
                backgroundColor: '#3f0',
                justifyContent: 'center',
                alignItems: 'center',
                borderRadius: 40,
              }}
              onPress={async () => {
                const playingSong = await AVService.pauseMusic(this.state.musics[2].songID);
                console.log('---- pauseMusic: ', playingSong);
                
              }}
            >
              <Text style={{ fontSize: 20, color: 'white' }}>pause</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.buttonContainer}>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.startExportVideo()}>
              <Text style={{ color: 'orange' }}>导出</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.buttonItem} onPress={() => this.changeFilter('原片')}>
              <Text>原片</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.buttonItem}
              onPress={async () => {
                const status = await AVService.pauseMusic(this.state.musicInfo.songID);
                if (status === true) {
                  console.log('---- pauseMusic: ', status);
                  this.setState({ setMusic: true });
                }
              }}
            >
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
