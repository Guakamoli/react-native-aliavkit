import { NativeModules, Platform } from 'react-native';

const isIOS = Platform.OS === 'ios';

import Camera from './Camera';
import VideoEditor from './VideoEditor';

import AVService from './AVService';

import AVKitPhotoView from './AVKitPhotoView';

import CropImageView from './CropImageView';

const { CameraKit } = NativeModules;

const CameraModule = isIOS ? NativeModules.CKCameraManager : NativeModules.RNCameraKitModule;

const EditorModule = isIOS ? NativeModules.RNEditViewManager : NativeModules.RNEditorKitModule;

const PhotoModule = isIOS ? NativeModules.RNAliKitPhotoViewManager : NativeModules.RNAliKitPhotoViewModule;

const CropImageViewModule = isIOS ? NativeModules.RNKitCropImageViewManager : NativeModules.RNKitCropImageViewModule;

export const Orientation = {
  PORTRAIT: 0, // ⬆️
  LANDSCAPE_LEFT: 1, // ⬅️
  PORTRAIT_UPSIDE_DOWN: 2, // ⬇️
  LANDSCAPE_RIGHT: 3, // ➡️
};

export const SortModeEnum = {
  SORT_MODE_ALL: 'all',
  SORT_MODE_PHOTO: 'photo',
  SORT_MODE_VIDEO: 'video',
}

export default CameraKit;

export {
  Camera, VideoEditor, AVKitPhotoView, CropImageView,
  AVService,
  CameraModule, EditorModule, PhotoModule, CropImageViewModule
};
