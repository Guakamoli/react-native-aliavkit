
import React from 'react';
import { NativeModules, DeviceEventEmitter } from 'react-native';
const { RNEditorKitModule,RNCameraKitModule } = NativeModules;

type MusicRequestType = {
  name: string,
  songID: string,
  page: number,
  pageSize: number,
}

export default class AVService {

  static async getFilterIcons() {
    let colorFilterList = await RNEditorKitModule.getColorFilterList();
    return JSON.parse(colorFilterList)
  }

  static async getFacePasterInfos({ }) {
    var pasterInfos = await RNCameraKitModule.getPasterInfos();
    return JSON.parse(pasterInfos)
  }

  //视频、图片宽高裁剪
  static async crop({ source, cropOffsetX, cropOffsetY, cropWidth, cropHeight }) {
    console.log('11111 android crop', source, cropOffsetX, cropOffsetY, cropWidth, cropHeight);
    //裁剪进度监听
    const carpListener = DeviceEventEmitter.addListener('startVideoCrop', (progress) => {
      //0~1
      console.log("CropFile", progress);
    });
    const carpParam = {
      'source': source,
      'cropOffsetX': cropOffsetX,
      'cropOffsetY': cropOffsetY,
      'cropWidth': cropWidth,
      'cropHeight': cropHeight,
    };
    const carpFile = await RNEditorKitModule.crop(carpParam);
    carpListener.remove();
    console.log('11111 android carpFile', carpFile);
    return carpFile
  }


  // {sourcePth:"",resourceType:"photo/video"}
  static async saveResourceToPhotoLibrary({ sourcePath, sourceType }) {
    return await RNEditorKitModule.saveMediaStore(sourcePath, sourceType);
  }

  static async playMusic(songID: string) {
    return await RNEditorKitModule.playMusic(songID);
  }

  static async pauseMusic(songID: string) {
    return await RNEditorKitModule.stopMusic(songID);
  }

  static async getMusics({ name, page, songID, pageSize }: MusicRequestType) {
    console.log('123', { name, page, songID, pageSize });
    if (name && name == 'all-music') {
      name = ""
    }
    var musics = await RNEditorKitModule.getMusicList(name, "", page, pageSize);
    return JSON.parse(musics)
  }

  static async getThumbnails({ videoPath, startTime, itemPerTime }) {
  }

  static async removeThumbnaiImages() {
  }
  static enableHapticIfExist() {
    // AliAVServiceBridge.enableHapticIfExist();
  }
}
