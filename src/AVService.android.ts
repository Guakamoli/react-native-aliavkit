import React from 'react';
import { NativeModules, DeviceEventEmitter } from 'react-native';
const { RNEditorKitModule, RNCameraKitModule } = NativeModules;

type MusicRequestType = {
  name: string;
  songID: string;
  page: number;
  pageSize: number;
};

export default class AVService {

  static async setFacePasterInfo(facePasterInfo) {
    RNCameraKitModule.setFacePasterInfo(facePasterInfo);
  }

  static async removeFacePasterListener() {
    DeviceEventEmitter.removeAllListeners("addFacePasterListener");
  }

  static async addFacePasterListener(progressListener: (progress: any) => void) {
    this.removeFacePasterListener();
    DeviceEventEmitter.addListener('addFacePasterListener', (progress) => {
      //0~1
      if (progressListener) {
        progressListener(JSON.parse(progress));
      }
    });
  }

  static async stopEdit() {
    const isStop = await RNEditorKitModule.stopEdit();
    return isStop;
  }

  static async getVideoEditorJsonPath() {
    const jsonPath = await RNEditorKitModule.getVideoEditorJsonPath();
    return jsonPath;
  }


  /**
   * 
   * @returns story 取消导出
   */
  static async storyCancelCompose() {
    RNEditorKitModule.storyCancelCompose();
    DeviceEventEmitter.removeAllListeners("storyComposeVideo");
  }

  static async storyComposeVideo(jsonPath: String, progressListener: (progress: number) => void) {
    const storyComposeListener = DeviceEventEmitter.addListener('storyComposeVideo', (reminder) => {
      //0~1
      if (progressListener) {
        progressListener(reminder.progress);
      }
    });
    const videoParam = await RNEditorKitModule.storyComposeVideo(jsonPath);
    DeviceEventEmitter.removeAllListeners("storyComposeVideo");
    return JSON.parse(videoParam);
  }


  /**
   * 
   * @returns post 取消裁剪
   */
  static async postCancelCrop() {
    RNEditorKitModule.postCancelCrop();
    DeviceEventEmitter.removeAllListeners("postVideoCrop");
  }

  //Post 视频上传压缩裁剪
  static async postCropVideo(videoPath: String, progressListener: (progress: number) => void) {
    const postCaopListener = DeviceEventEmitter.addListener('postVideoCrop', (progress) => {
      //0~1
      if (progressListener) {
        progressListener(progress);
      }
    });
    const cropParam = await RNEditorKitModule.postCropVideo(videoPath);
    DeviceEventEmitter.removeAllListeners("postVideoCrop");
    return JSON.parse(cropParam);
  }


  /**
   *
   * @returns 获取录制的滤镜列表
   */
  static async getRecordColorFilter() {
    let colorFilterList = await RNCameraKitModule.getRecordColorFilter();
    return JSON.parse(colorFilterList)
  }

  static async getFilterIcons() {
    const colorFilterList = await RNEditorKitModule.getColorFilterList();
    return JSON.parse(colorFilterList);
  }

  static async getFacePasterInfos({ }) {
    var pasterInfos = await RNCameraKitModule.getPasterInfos();
    return JSON.parse(pasterInfos);
  }

  //视频、图片宽高裁剪
  static async crop({ source, duration, cropOffsetX, cropOffsetY, cropWidth, cropHeight }) {
    //裁剪进度监听
    const carpListener = DeviceEventEmitter.addListener('startVideoCrop', (progress) => {
      //0~1
    });
    const carpParam = {
      source: source,
      cropOffsetX: cropOffsetX,
      cropOffsetY: cropOffsetY,
      cropWidth: cropWidth,
      cropHeight: cropHeight,
      duration: duration,
    };
    const carpFile = await RNEditorKitModule.crop(carpParam);
    carpListener.remove();

    return carpFile;
  }

  // {sourcePth:"",resourceType:"photo/video"}
  //保存图片到相册
  static async saveResourceToPhotoLibrary({ sourcePath, sourceType }) {
    return await RNEditorKitModule.saveMediaStore(sourcePath, sourceType);
  }

  static async playMusic(songID: string) {
    const musicInfo = await RNEditorKitModule.playMusic(songID);
    return JSON.parse(musicInfo);
  }

  static async pauseMusic(songID: string) {
    return await RNEditorKitModule.stopMusic(songID);
  }

  static async getMusics({ name, page, songID, pageSize }: MusicRequestType) {
    if (name && name == 'all-music') {
      name = '';
    }
    var musics = await RNEditorKitModule.getMusicList(name, '', page, pageSize);
    return JSON.parse(musics);
  }

  // static async getThumbnails({ videoPath, startTime, itemPerTime,cropOffsetX,cropOffsetY,cropWidth,cropHeight }) {
  //   //
  //   var thumbnails = await RNEditorKitModule.corpVideoFrame({ videoPath, startTime, itemPerTime,cropOffsetX,cropOffsetY,cropWidth,cropHeight });
  //   return JSON.parse(thumbnails)
  // }

  static async getThumbnails(params) {
    //
    var thumbnails = await RNEditorKitModule.corpVideoFrame(params);
    return JSON.parse(thumbnails);
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
    //      ...  其他非必要字段
    //   },
    //   ...
    // ]
    const fontList = await RNEditorKitModule.getFontList();
    return JSON.parse(fontList);
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
    //      ...  其他非必要字段
    //   }

    const fontInfo = await RNEditorKitModule.downloadFont(font);
    return JSON.parse(fontInfo);
  }

  static async downloadFontTest() {
    const fontList = await AVService.getFontList();
    for (let i = 0; i < fontList.length; i++) {
      const fontInfo = await AVService.downloadFont(fontList[i]);
    }
  }
}
