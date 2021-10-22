import React from 'react';
import { NativeModules } from 'react-native';
const { AliAVServiceBridge, RNMusicService } = NativeModules;

type MusicRequestType = {
  name: string,
  songID: string,
  page: number,
  pageSize: number,
}

export default class AVService {
  static async getFacePasterInfos({}) {
    return await AliAVServiceBridge.getFacePasterInfos({});
  }
  // {sourcePth:"",resourceType:"photo/video"}
  static async saveResourceToPhotoLibrary({ sourcePath, resourceType }) {
    return await AliAVServiceBridge.saveResourceToPhotoLibrary({ sourcePath, resourceType });
  }

  static async crop({ source, cropOffsetX, cropOffsetY, cropWidth, cropHeight }) {
    return await AliAVServiceBridge.crop({ source, cropOffsetX, cropOffsetY, cropWidth, cropHeight });
  }

  static async saveToSandBox({ path }) {
    return await AliAVServiceBridge.saveToSandBox({ path });
  }

  static async playMusic(songID: string) {
    return await RNMusicService.playMusic(songID);
  }

  static async pauseMusic(songID: string) {
    return await RNMusicService.pauseMusic(songID);
  }
  // name:'all-music' 分页传all-music'，其他传歌曲名 
  static async getMusics({ name, page, songID, pageSize }: MusicRequestType) {
    return await RNMusicService.getMusics({ name, page, songID, pageSize });
  }
}

