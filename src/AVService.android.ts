
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
  static async crop({ source, duration, cropOffsetX, cropOffsetY, cropWidth, cropHeight }) {
    console.log('视频裁剪 android crop', source, cropOffsetX, cropOffsetY, cropWidth, cropHeight);
    //裁剪进度监听
    const carpListener = DeviceEventEmitter.addListener('startVideoCrop', (progress) => {
      //0~1
      console.log("视频裁剪中...", progress);
    });
    const carpParam = {
      'source': source,
      'cropOffsetX': cropOffsetX,
      'cropOffsetY': cropOffsetY,
      'cropWidth': cropWidth,
      'cropHeight': cropHeight,
      'duration': duration,
    };
    const carpFile = await RNEditorKitModule.crop(carpParam);
    carpListener.remove();
    console.log('裁剪完成 android carpFile', carpFile);
    return carpFile
  }


  // {sourcePth:"",resourceType:"photo/video"}
  //保存图片到相册
  static async saveResourceToPhotoLibrary({ sourcePath, sourceType }) {
    return await RNEditorKitModule.saveMediaStore(sourcePath, sourceType);
  }

  static async playMusic(songID) {
    return await RNEditorKitModule.playMusic(songID);
  }

  static async pauseMusic(songID) {
    console.info('pauseMusic', songID);
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

  // static async getThumbnails({ videoPath, startTime, itemPerTime,cropOffsetX,cropOffsetY,cropWidth,cropHeight }) {
  //   // console.log("getThumbnails",videoPath,startTime,itemPerTime)
  //   var thumbnails = await RNEditorKitModule.corpVideoFrame({ videoPath, startTime, itemPerTime,cropOffsetX,cropOffsetY,cropWidth,cropHeight });
  //   return JSON.parse(thumbnails)
  // }

  static async getThumbnails(params) {
    // console.log("getThumbnails",params)
    var thumbnails = await RNEditorKitModule.corpVideoFrame(params);
    return JSON.parse(thumbnails)
  }

  //删除抽帧的图片
  static async removeThumbnaiImages() {
    return await RNEditorKitModule.removeThumbnaiImages();
  }

  //删除 录制、编辑时产生的图片、视频缓存
  static async clearResources({ tmp, record, composition }) {
    return await RNEditorKitModule.clearResources();
  }
  static enableHapticIfExist() {
    // AliAVServiceBridge.enableHapticIfExist();
  }
}
