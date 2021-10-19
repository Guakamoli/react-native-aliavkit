
import React from 'react';
import { NativeModules,DeviceEventEmitter} from 'react-native';
const { RNEditorKitModule } = NativeModules;

export default class AVService {

    //视频、图片宽高裁剪
    static async crop({ source, cropOffsetX, cropOffsetY, cropWidth, cropHeight }) {
        console.log('11111 android crop',source, cropOffsetX, cropOffsetY, cropWidth, cropHeight);
        //裁剪进度监听
        const carpListener = DeviceEventEmitter.addListener('startVideoCrop', (progress) => {
            //0~1
            console.log("CropFile", progress);
        });
        const carpParam  = {
                             'source':source,
                             'cropOffsetX':cropOffsetX,
                             'cropOffsetY':cropOffsetY,
                             'cropWidth':cropWidth,
                             'cropHeight':cropHeight,
                           };
        const carpFile = await RNEditorKitModule.crop(carpParam);
        carpListener.remove();
        console.log('11111 android carpFile',carpFile);
        return carpFile
    }


  // {sourcePth:"",resourceType:"photo/video"}
  static async saveResourceToPhotoLibrary({ sourcePath, sourceType }) {
    return await RNEditorKitModule.saveMediaStore({ sourcePath, sourceType });
  }

}
