import React from 'react';
import { NativeModules } from 'react-native';
const { AliAVServiceBridge, RNMusicService } = NativeModules;

type MusicRequestType = {
  name: string;
  songID: string;
  page: number;
  pageSize: number;
};

export default class AVService {
  static async getFilterIcons() {
    return await AliAVServiceBridge.getFilterIcons({});
  }

  static async getFacePasterInfos({}) {
    return await AliAVServiceBridge.getFacePasterInfos({});
  }

  // {sourcePth:"",resourceType:"photo/video"}
  static async saveResourceToPhotoLibrary({ sourcePath, resourceType }) {
    return await AliAVServiceBridge.saveResourceToPhotoLibrary({ sourcePath, resourceType });
  }

  /*
  如果是视频，则需要监听cropProgress事件，
  如果是图片，则不需要监听，只需要await path即可
*/
  static async crop({ source, duration, cropOffsetX, cropOffsetY, cropWidth, cropHeight }) {
    return await AliAVServiceBridge.crop({ source, duration, cropOffsetX, cropOffsetY, cropWidth, cropHeight });
  }

  //path ph://02C321FF-5B7B-4C3F-83E3-3D66BD9EDD78/L0/001
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
    // console.info('getMusics', { name, page, songID, pageSize });
    return await RNMusicService.getMusics({ name, page, songID, pageSize });
  }

  static async getThumbnails({ videoPath, startTime, itemPerTime, needCover }) {
    return await AliAVServiceBridge.generateImages({ videoPath, startTime, itemPerTime, needCover});
  }

  static async removeThumbnaiImages() {
    return await AliAVServiceBridge.removeThumbnaiImages({});
  }
  //振动
  static enableHapticIfExist() {
    AliAVServiceBridge.enableHapticIfExist();
  }

  static async clearResources({ tmp, record, composition }) {
    return await AliAVServiceBridge.clearResources({ tmp, record, composition });
  }


  //获取所有字体
  static async getFontList() {
    // 安卓端返回： List<FileDownloaderModel>
    // [
    //   {  "id": 52,
    //      "taskId": -380016714, // taskId: 安卓端用来创建下载任务的id
    //      "name": "趣.憧憬",
    //      "banner": "https://alivc-demo-vod.aliyuncs.com/image/default/BD4E71CE644043019E89A5737A79AC51-6-2.jpg",
    //      "icon": "https://alivc-demo-vod.aliyuncs.com/image/default/C1F83D29D3B64C3C87CF7E3BDEF3B08F-6-2.jpg", 
    //      "isDbContain": 1,  //是否下载到本地 0 未下载；1 已下载。  非必要，如果没有，则用 path 字段是否为空来判断
    //      "path": "/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/downloads/fonts/52-趣.憧憬/font.ttf", 
    //      "url": "https://alivc-demo-vod.aliyuncs.com/video/material/B12F02D94A184481A9DE629ECFCD8C0D-7-4.mat",
    //      "isunzip": 1,
    //      "effectType": 1,
    //      ...
    //   },
    //   ...
    // ]
  }


  /**
   * 下载字体
   * @param font  上面 getFontList 函数返回的字体对象：FileDownloaderModel
   * @returns 
   */
  static async downloadFont(font) {
    // 返回：FileDownloaderModel
    //   {  "id": 52,
    //      "taskId": -380016714, // taskId: 安卓端用来创建下载任务的id
    //      "name": "趣.憧憬",
    //      "banner": "https://alivc-demo-vod.aliyuncs.com/image/default/BD4E71CE644043019E89A5737A79AC51-6-2.jpg",
    //      "icon": "https://alivc-demo-vod.aliyuncs.com/image/default/C1F83D29D3B64C3C87CF7E3BDEF3B08F-6-2.jpg", 
    //      "isDbContain": 1,  //是否下载到本地 0 未下载；1 已下载。  非必要，如果没有，则用 path 字段是否为空来判断
    //      "path": "/storage/emulated/0/Android/data/com.guakamoli.paiya.android.test/files/downloads/fonts/52-趣.憧憬/font.ttf", 
    //      "url": "https://alivc-demo-vod.aliyuncs.com/video/material/B12F02D94A184481A9DE629ECFCD8C0D-7-4.mat",
    //      "isunzip": 1,
    //      "effectType": 1,
    //      ...
    //   }

    
  }
}
