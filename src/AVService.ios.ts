import React from 'react';
import { NativeModules, NativeEventEmitter, } from 'react-native';
const { AliAVServiceBridge, RNMusicService, RNEditViewManager, CKCameraManager } = NativeModules;

const managerEmitter = new NativeEventEmitter(AliAVServiceBridge);

const { RNAliavkitEventEmitter } = NativeModules;
const aliavkitEventEmitter = new NativeEventEmitter(RNAliavkitEventEmitter);

type MusicRequestType = {
  name: string;
  songID: string;
  page: number;
  pageSize: number;
};

export default class AVService {


  static async setFacePasterInfo(facePasterInfo) {
    CKCameraManager.setFacePasterInfo(facePasterInfo, facePasterInfo.index);
  }

  static async removeFacePasterListener() {
    aliavkitEventEmitter?.removeAllListeners('addFacePasterListener');
  }

  static async addFacePasterListener(progressListener: (progress: any) => void) {
    this.removeFacePasterListener();
    aliavkitEventEmitter.addListener('addFacePasterListener', (progress) => {
      //0~1
      if (progressListener) {
        progressListener(progress);
      }
    });
  }


  static async stopEdit() {
    const isStop = await RNEditViewManager.stopEdit({});
    return isStop;
  }

  static async getVideoEditorJsonPath() {
    const jsonPath = await RNEditViewManager.getTaskPath({});
    return jsonPath;
  }

  /**
   * 
   * @returns story 取消导出
   */
  static async storyCancelCompose() {
    // console.info("storyCancelCompose");
    AliAVServiceBridge.storyCancelCompose({});
    managerEmitter?.removeAllListeners('storyComposeVideo');
  }

  static async storyComposeVideo(jsonPath: String, progressListener: (progress: number) => void) {
    const storyComposeListener = managerEmitter.addListener('storyComposeVideo', (reminder) => {
      //0~1
      if (progressListener) {
        progressListener(reminder?.progress);
      }
    });
    const videoPath = await AliAVServiceBridge.storyComposeVideo(jsonPath);
    managerEmitter?.removeAllListeners('storyComposeVideo');
    return videoPath;
  }

  /**
   * 
   * @returns post 取消裁剪
   */
  static async postCancelCrop() {
    // console.info("postCancelCrop");
    AliAVServiceBridge.postCancelCrop({});
    managerEmitter?.removeAllListeners("postVideoCrop");
  }

  //Post 视频上传压缩裁剪
  static async postCropVideo(videoPath: String, progressListener: (progress: number) => void) {
    //裁剪 file://
    if (!!videoPath && videoPath.startsWith("file://")) {
      videoPath = videoPath.slice(7)
    }
    const postCropListener = managerEmitter?.addListener('postVideoCrop', (reminder) => {
      //
      if (progressListener) {
        progressListener(reminder?.progress);
      }
    });
    let cropParam = await AliAVServiceBridge.postCropVideo(videoPath);
    managerEmitter?.removeAllListeners("postVideoCrop");
    return cropParam;
  }

  /**
   * @returns 获取录制的滤镜列表
   */
  static async getRecordColorFilter() {
    let colorFilterList = await AliAVServiceBridge.getRecordColorFilter({});
    return colorFilterList
  }


  static async getFilterIcons() {
    let colorFilterList = await AliAVServiceBridge.getFilterIcons({});
    return colorFilterList
  }

  static async getFacePasterInfos({ }) {
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
  static async saveToSandBox(uri: string) {
    return await AliAVServiceBridge.saveToSandBox({ path: uri });
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

  static async getThumbnails({ videoPath, startTime, itemPerTime, needCover }) {
    return await AliAVServiceBridge.generateImages({ videoPath, startTime, itemPerTime, needCover });
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
