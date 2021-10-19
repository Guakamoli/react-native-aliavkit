
import React from 'react';
import { NativeModules } from 'react-native';
const { AliAVServiceBridge } = NativeModules;

export default class AVService {
  static async getFacePasterInfos({}) {
    return await AliAVServiceBridge.getFacePasterInfos({});
  }
  // {sourcePth:"",resourceType:"photo/video"}
  static async saveResourceToPhotoLibrary({ sourcePath, resourceType }) {
    return await AliAVServiceBridge.saveResourceToPhotoLibrary({ resourcePath, resourceType });
  }

  /*
  如果是视频，则需要监听cropProgress事件，
  如果是图片，则不需要监听，只需要await path即可
*/
  static async crop({ source, cropOffsetX, cropOffsetY, cropWidth, cropHeight }) {
    console.log('11111',source, cropOffsetX, cropOffsetY, cropWidth, cropHeight, quality);
    
    return await AliAVServiceBridge.crop({ source, cropOffsetX, cropOffsetY, cropWidth, cropHeight });
  }
  //path ph://02C321FF-5B7B-4C3F-83E3-3D66BD9EDD78/L0/001
  static async saveToSandBox({ path }) {
    return await AliAVServiceBridge.saveToSandBox({ path });
  }
}
