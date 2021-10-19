
import React from 'react';
import { NativeModules } from 'react-native';
const { AliAVServiceBridge, RNMusicService } = NativeModules;

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

  static async downloadMusic({ musicName }) {
    return await RNMusicService.downloadMusic({ musicName });
  }
}