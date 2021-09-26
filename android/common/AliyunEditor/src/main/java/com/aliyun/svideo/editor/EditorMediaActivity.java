/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.common.base.BaseAliActivity;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.crop.AliyunImageCropActivity;
import com.aliyun.svideo.crop.AliyunVideoCropActivity;
import com.aliyun.svideo.crop.bean.AlivcCropInputParam;
import com.aliyun.svideo.crop.bean.AlivcCropOutputParam;
import com.aliyun.svideo.editor.bean.AlivcEditInputParam;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.editor.util.FixedToastUtils;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideo.media.MediaStorage;
import com.aliyun.svideo.media.MutiMediaView;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.struct.common.CropKey;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.duanqu.transcode.NativeParser;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 编辑模块的media选择Activity
 * 拍鸭：视频选择 - 相册选择
 */
public class EditorMediaActivity extends BaseAliActivity {

    private static final int IMAGE_DURATION = 3000;//图片代表的时长

    private final static String TAG = EditorMediaActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    private Transcoder mTransCoder;
    private MediaInfo mCurrMediaInfo;
    private int mCropPosition;

    private AlivcEditInputParam mInputParam;

    /**
     * 页面恢复时保存mBundleSaveMedias对象的key
     * 保存时 {@link #onSaveInstanceState(Bundle)}
     * 恢复时 {@link #onRestoreInstanceState(Bundle)}
     */
    private static final String BUNDLE_KEY_SAVE_MEDIAS = "bundle_key_save_transcoder";

    /**
     * 页面恢复时保存选择Medias对象
     */
    private ArrayList<MediaInfo> mBundleSaveMedias;
    private MutiMediaView mMutiMediaView;
    private int mRatio;

    private String mStrTitle = "", mStrContent = "";
    private boolean isShowTrim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_no_amin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_media);
        initData();
        checkPermissions();
    }


    private void initData() {
        Intent intent = getIntent();

        mStrTitle = intent.getStringExtra("strTitle");
        mStrContent = intent.getStringExtra("strContent");
        isShowTrim = intent.getBooleanExtra("isShowTrim",true);

        int mBitrate = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_BITRATE, AlivcEditInputParam.DEFAULT_VALUE_BITRATE);

        int mFrameRate = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_FRAME, 30);
        int mGop = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_GOP, 250);
        mRatio = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_RATION_MODE, AlivcEditInputParam.RATIO_MODE_9_16);
        VideoQuality mVideoQuality = (VideoQuality) intent.getSerializableExtra(AlivcEditInputParam.INTENT_KEY_QUALITY);
        if (mVideoQuality == null) {
            mVideoQuality = VideoQuality.HD;
        }
        int mResolutionMode = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_RESOLUTION_MODE, AlivcEditInputParam.RESOLUTION_720P);
        VideoCodecs mVideoCodec = (VideoCodecs) intent.getSerializableExtra(AlivcEditInputParam.INTENT_KEY_CODEC);
        if (mVideoCodec == null) {
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
        int mCrf = intent.getIntExtra(AlivcEditInputParam.INTETN_KEY_CRF, 23);
        float mScaleRate = intent.getFloatExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_RATE, 1.0f);
        VideoDisplayMode mScaleMode = (VideoDisplayMode) intent.getSerializableExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_MODE);
        if (mScaleMode == null) {
            mScaleMode = VideoDisplayMode.FILL;
        }
        boolean mHasTailAnimation = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_TAIL_ANIMATION, false);
        boolean hasDeNoise = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_DE_NOISE, false);
        boolean canReplaceMusic = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_REPLACE_MUSIC, true);
        ArrayList<MediaInfo> mediaInfos = intent.getParcelableArrayListExtra(AlivcEditInputParam.INTENT_KEY_MEDIA_INFO);
        boolean hasWaterMark = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_WATER_MARK, false);
        mInputParam = new AlivcEditInputParam.Builder()
                .setBitrate(mBitrate)
                .setFrameRate(mFrameRate)
                .setGop(mGop)
                .setRatio(mRatio)
                .setVideoQuality(mVideoQuality)
                .setResolutionMode(mResolutionMode)
                .setVideoCodec(mVideoCodec)
                .setCrf(mCrf)
                .setScaleRate(mScaleRate)
                .setScaleMode(mScaleMode)
                .setHasTailAnimation(mHasTailAnimation)
                .setCanReplaceMusic(canReplaceMusic)
                .addMediaInfos(mediaInfos)
                .setDeNoise(hasDeNoise)
                .setHasWaterMark(hasWaterMark)
                .build();
    }

    private void init() {
        mMutiMediaView = findViewById(R.id.media_view);
        //最大时长3分钟
        mMutiMediaView.enableSelectView(5 * 60 * 1000);
        //设置仅支持视频
        mMutiMediaView.setMediaSortMode(MediaStorage.SORT_MODE_VIDEO);
        mMutiMediaView.enableSwap();
        mTransCoder = new Transcoder();
        mTransCoder.init(this);
        mTransCoder.setInputParam(mInputParam);

        mTransCoder.setTransCallback(new Transcoder.TransCallback() {
            @Override
            public void onError(Throwable e, final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        switch (errorCode) {
                            case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                                ToastUtil.showToast(getApplicationContext(), R.string.alivc_crop_video_tip_not_supported_audio);
                                break;
                            case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                                ToastUtil.showToast(getApplicationContext(), R.string.alivc_crop_video_tip_crop_failed);
                                break;
                            case AliyunErrorCode.ALIVC_COMMON_UNKNOWN_ERROR_CODE:
                            default:
                                ToastUtil.showToast(getApplicationContext(), R.string.alivc_crop_video_tip_crop_failed);
                        }
                    }
                });

            }

            @Override
            public void onProgress(int progress) {
                if (progressDialog != null) {
                    progressDialog.setProgress(progress);
                }
            }

            @Override
            public void onComplete(List<MediaInfo> resultVideos) {
                Log.d("TRANCODE", "ONCOMPLETED, dialog : " + (progressDialog == null));
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                mInputParam.setMediaInfos((ArrayList<MediaInfo>) resultVideos);

                EditorActivity.startEdit(EditorMediaActivity.this, mInputParam,mStrTitle, mStrContent,isShowTrim);
            }

            @Override
            public void onCancelComplete() {
                //取消完成
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMutiMediaView.setNextEnable(true);
                    }
                });
            }
        });

        /**
         * 下一步
         */
        mMutiMediaView.setOnActionListener(new MutiMediaView.OnActionListener() {
            @Override
            public void onNext(boolean isReachedMaxDuration) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if (isReachedMaxDuration) {
                    ToastUtil.showToast(getApplicationContext(), R.string.alivc_media_message_max_duration_import);
                    return;
                }
                //对于大于720P的视频需要走转码流程

                int videoCount = mTransCoder.getVideoCount();
                if (videoCount > 0 && (progressDialog == null || !progressDialog.isShowing())) {
                    progressDialog = ProgressDialog.show(EditorMediaActivity.this, null, getResources().getString(R.string.alivc_media_wait));
                    progressDialog.setCancelable(true);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setOnCancelListener(new OnCancelListener(EditorMediaActivity.this));
                    mTransCoder.transcode(EditorMediaActivity.this.getApplicationContext(), mInputParam.getScaleMode());
                } else {
                    ToastUtil.showToast(getApplicationContext(), R.string.alivc_media_please_select_video);
                }
            }

            @Override
            public void onBack() {
                finish();
            }
        });

        mMutiMediaView.setOnMediaClickListener(new MutiMediaView.OnMediaClickListener() {
            @Override
            public void onClick(MediaInfo info) {
                Log.i(TAG, "log_editor_video_path : " + info.filePath);
                MediaInfo infoCopy = new MediaInfo();
                infoCopy.addTime = info.addTime;
                infoCopy.mimeType = info.mimeType;
                if (info.mimeType.startsWith("image")) {
                    if (info.filePath.endsWith("gif") || info.filePath.endsWith("GIF")) {
                        NativeParser parser = new NativeParser();
                        parser.init(info.filePath);
                        int frameCount;

                        try {
                            frameCount = Integer.parseInt(parser.getValue(NativeParser.VIDEO_FRAME_COUNT));
                        } catch (Exception e) {
                            ToastUtils.show(getApplicationContext(), R.string.alivc_editor_error_tip_play_video_error);
                            parser.release();
                            parser.dispose();
                            return;
                        }
                        //当gif动图为一帧的时候当作图片处理，否则当作视频处理
                        if (frameCount > 1) {
                            int duration;
                            try {
                                duration = Integer.parseInt(parser.getValue(NativeParser.VIDEO_DURATION)) / 1000;
                            } catch (Exception e) {
                                ToastUtils.show(getApplicationContext(), R.string.alivc_editor_error_tip_play_video_error);
                                parser.release();
                                parser.dispose();
                                return;
                            }
                            infoCopy.mimeType = "video";
                            infoCopy.duration = duration;
                        } else {
                            infoCopy.duration = IMAGE_DURATION;
                        }
                        parser.release();
                        parser.dispose();

                    } else {
                        if (mRatio == AlivcEditInputParam.RATIO_MODE_ORIGINAL) {
                            //原比例下android解码器对图片大小有要求，目前支持为单边不大于3840
                            try {
                                ParcelFileDescriptor pfd = EditorMediaActivity.this.getContentResolver().openFileDescriptor(Uri.parse(info.fileUri), "r");
                                if (pfd != null) {
                                    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                                    if (bitmap != null && (bitmap.getHeight() > 3840 || bitmap.getWidth() > 3840)) {
                                        ToastUtils.show(getApplicationContext(), "原尺寸输出时，图片宽高不能超过3840");
                                        return;
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        infoCopy.duration = IMAGE_DURATION;
                    }

                } else {
                    infoCopy.duration = info.duration;
                }
                infoCopy.filePath = info.filePath;
                infoCopy.fileUri = info.fileUri;
                infoCopy.id = info.id;
                infoCopy.isSquare = info.isSquare;
                infoCopy.thumbnailPath = info.thumbnailPath;
                infoCopy.thumbnailUri = info.thumbnailUri;
                infoCopy.title = info.title;
                infoCopy.type = info.type;

                mMutiMediaView.addSelectMedia(infoCopy);
                mMutiMediaView.setNextEnable(true);
                mTransCoder.addMedia(infoCopy);
            }
        });

        mMutiMediaView.setOnSelectMediaChangeListener(new MutiMediaView.OnSelectMediaChangeListener() {
            @Override
            public void onRemove(MediaInfo info) {
                mTransCoder.removeMedia(info);
            }

            @Override
            public void onClick(MediaInfo info, int position) {

                if (FastClickUtil.isFastClickActivity(EditorMediaActivity.class.getSimpleName())) {
                    return;
                }

                mCurrMediaInfo = info;
                mCropPosition = position;

                if (info.filePath.endsWith("gif") || info.filePath.endsWith("GIF")) {
                    Toast.makeText(EditorMediaActivity.this, R.string.alivc_crop_media_gif_not_support, Toast.LENGTH_SHORT).show();
                    return;
                }

                AlivcCropInputParam cropInputParam = new AlivcCropInputParam.Builder()
                        .setBitrate(mInputParam.getBitrate())
                        .setRatioMode(mInputParam.getRatio())
                        .setResolutionMode(mInputParam.getResolutionMode())
                        .setCropMode(mInputParam.getScaleMode())
                        .setFrameRate(mInputParam.getFrameRate())
                        .setGop(mInputParam.getGop())
                        .setQuality(mInputParam.getVideoQuality())
                        .setVideoCodecs(mInputParam.getVideoCodec())
                        .setAction(CropKey.ACTION_SELECT_TIME)
                        .setMediaInfo(info)
                        .build();
                if (info.mimeType.startsWith("video")) {
                    cropInputParam.setPath(info.filePath);
                    AliyunVideoCropActivity.startVideoCropForResult(EditorMediaActivity.this, cropInputParam, AliyunVideoCropActivity.REQUEST_CODE_EDITOR_VIDEO_CROP);
                } else if (info.mimeType.startsWith("image")) {
                    cropInputParam.setPath(info.filePath);
                    AliyunImageCropActivity.startImageCropForResult(EditorMediaActivity.this, cropInputParam, AliyunImageCropActivity.REQUEST_CODE_EDITOR_IMAGE_CROP);
                }
            }

            @Override
            public void onSwap(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                               RecyclerView.ViewHolder target) {
                mTransCoder.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }
        });
        mMutiMediaView.loadMedia();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTING_PERMISSION_REQUEST_CODE){
            setting_permission_request_code = requestCode;
            setting_permission_result_code = resultCode;
        }
        if (resultCode == Activity.RESULT_OK) {
            AlivcCropOutputParam outputParam = (AlivcCropOutputParam) data.getSerializableExtra(AlivcCropOutputParam.RESULT_KEY_OUTPUT_PARAM);
            if (outputParam == null) {
                return;
            }
            String path = outputParam.getOutputPath();
            switch (requestCode) {
                case AliyunVideoCropActivity.REQUEST_CODE_EDITOR_VIDEO_CROP:

                    //TODO
                    long duration = outputParam.getDuration();
                    long startTime = outputParam.getStartTime();
                    if (!TextUtils.isEmpty(path) && duration > 0 && mCurrMediaInfo != null) {
                        mMutiMediaView.changeDurationPosition(mCropPosition, duration);
                        int index = mTransCoder.removeMedia(mCurrMediaInfo);
                        mCurrMediaInfo.filePath = path;
                        mCurrMediaInfo.startTime = startTime;
                        mCurrMediaInfo.duration = (int) duration;
                        mTransCoder.addMedia(index, mCurrMediaInfo);
                    }
                    break;
                case AliyunImageCropActivity.REQUEST_CODE_EDITOR_IMAGE_CROP:
                    if (!TextUtils.isEmpty(path) && mCurrMediaInfo != null) {
                        int index = mTransCoder.removeMedia(mCurrMediaInfo);
                        mCurrMediaInfo.filePath = path;
                        mTransCoder.addMedia(index, mCurrMediaInfo);
                    }
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMutiMediaView!=null){
            mMutiMediaView.onDestroy();
        }
        if(mTransCoder!=null){
            mTransCoder.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //恢复选择的medias
        if (mBundleSaveMedias != null) {
            for (MediaInfo mediaInfo : mBundleSaveMedias) {
                mMutiMediaView.addSelectMedia(mediaInfo);
                mTransCoder.addMedia(mediaInfo);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //每次退到后台清空save的值，避免正常时也会恢复
        mBundleSaveMedias = null;
        onActivityCloseAnim();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mTransCoder!=null){
            outState.putParcelableArrayList(BUNDLE_KEY_SAVE_MEDIAS, mTransCoder.getOriginalVideos());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<MediaInfo> data = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_SAVE_MEDIAS);
        if (data != null && data.size() != 0) {
            mBundleSaveMedias = data;
        }
    }

    /**
     * progressDialog cancel listener
     */
    private static class OnCancelListener implements DialogInterface.OnCancelListener {

        private WeakReference<EditorMediaActivity> weakReference;

        private OnCancelListener(EditorMediaActivity mediaActivity) {
            weakReference = new WeakReference<>(mediaActivity);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            EditorMediaActivity mediaActivity = weakReference.get();
            if (mediaActivity != null) {
                mediaActivity.mMutiMediaView.setNextEnable(false);//为了防止未取消成功的情况下就开始下一次转码，这里在取消转码成功前会禁用下一步按钮
                mediaActivity.mTransCoder.cancel();
            }
        }
    }

    public static void startImport(Activity context, AlivcEditInputParam param, String strTitle,String strContent,boolean isShowTrim, int requestCode) {
        if (param == null) {
            return;
        }
        Intent intent = new Intent(context, EditorMediaActivity.class);

        intent.putExtra("strTitle", strTitle);
        intent.putExtra("strContent", strContent);
        intent.putExtra("isShowTrim", isShowTrim);

        intent.putExtra(AlivcEditInputParam.INTENT_KEY_BITRATE, param.getBitrate());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_FRAME, param.getFrameRate());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_GOP, param.getGop());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_RATION_MODE, param.getRatio());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_QUALITY, param.getVideoQuality());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_RESOLUTION_MODE, param.getResolutionMode());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_CODEC, param.getVideoCodec());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_CRF, param.getCrf());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_RATE, param.getScaleRate());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_MODE, param.getScaleMode());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_TAIL_ANIMATION, param.isHasTailAnimation());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_REPLACE_MUSIC, param.isCanReplaceMusic());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_WATER_MARK, param.isHasWaterMark());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_DE_NOISE, param.getDeNoise());
        intent.putParcelableArrayListExtra(AlivcEditInputParam.INTENT_KEY_MEDIA_INFO, param.getMediaInfos());
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_no_amin);
    }

    protected void onActivityCloseAnim() {
        if (isFinishing()) {
            //finish() 无效
            this.overridePendingTransition(R.anim.fade_no_amin, R.anim.fade_out);
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.fade_no_amin, R.anim.fade_out);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if(setting_permission_request_code==SETTING_PERMISSION_REQUEST_CODE&&setting_permission_result_code==0){
            checkPermissions();
        }
        setting_permission_request_code = 0;
        setting_permission_result_code = -1;
    }


    private  String[] permission = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int PERMISSION_REQUEST_CODE = 1000;
    public static final int SETTING_PERMISSION_REQUEST_CODE = 1001;

    private int setting_permission_request_code;
    private int setting_permission_result_code=-1;

    private void checkPermissions(){
        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, permission);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了
                init();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                showPermissionDialog();
            }
        }
    }


    //系统授权设置的弹框
    AlertDialog openAppDetDialog = null;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.app_name) + getResources().getString(R.string.alivc_recorder_record_dialog_permission_remind));
        builder.setPositiveButton(getResources().getString(R.string.alivc_record_request_permission_positive_btn_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(intent,SETTING_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton(getResources().getString(R.string.alivc_recorder_record_dialog_not_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });
        if (null == openAppDetDialog) {
            openAppDetDialog = builder.create();
        }
        if (null != openAppDetDialog && !openAppDetDialog.isShowing()) {
            openAppDetDialog.show();
        }
    }
}
