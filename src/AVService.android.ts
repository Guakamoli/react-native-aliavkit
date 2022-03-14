
import React from 'react';
import { NativeModules, DeviceEventEmitter } from 'react-native';
const { RNEditorKitModule, RNCameraKitModule } = NativeModules;

type MusicRequestType = {
  name: string,
  songID: string,
  page: number,
  pageSize: number,
}

export default class AVService {

  //Post 视频上传压缩裁剪
  static async postCropVideo(videoPath: String, progressListener: (progress: number) => void) {
    const carpListener = DeviceEventEmitter.addListener('postVideoCrop', (progress) => {
      //0~1
      // console.log("post 视频裁剪中...", progress);
      if (progressListener) {
        progressListener(progress);
      }
    });
    let cropVideoPath = await RNEditorKitModule.postCropVideo(videoPath);
    carpListener.remove();
    // console.log('post 视频裁剪完成', cropVideoPath);
    return cropVideoPath;
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

  static async playMusic(songID: string) {
    let musicInfo = await RNEditorKitModule.playMusic(songID);
    return JSON.parse(musicInfo)
  }

  static async pauseMusic(songID: string) {
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
    let fontList = await RNEditorKitModule.getFontList();
    return JSON.parse(fontList)
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
    console.log("去下载字体", font);
    let fontInfo = await RNEditorKitModule.downloadFont(font);
    return JSON.parse(fontInfo)
  }


  static async downloadFontTest() {
    // //TODO
    let fontList = await AVService.getFontList();
    console.log("所有字体：", "+" + fontList.length);
    for (let i = 0; i < fontList.length; i++) {
      let fontInfo = await AVService.downloadFont(fontList[i]);
      console.log("下载成功的字体", fontInfo);
    }
  }
}
