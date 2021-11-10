package com.aliyun.svideo.editor.publish.paiya;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.utils.VideoInfoUtils;
import com.aliyun.svideo.common.base.BaseAliActivity;
import com.aliyun.svideo.common.utils.DateTimeUtils;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.FileUtils;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.publish.ComposeFactory;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.editor.AliyunIComposeCallBack;
import com.aliyun.svideosdk.editor.AliyunIVodCompose;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 拍鸭：视频合成
 */
public class PaiyaPublishActivity extends BaseAliActivity {

    public static final String KEY_PARAM_CONFIG = "project_json_path";
    public static final String KEY_PARAM_THUMBNAIL = "svideo_thumbnail";
    public static final String KEY_PARAM_VIDEO_RATIO = "key_param_video_ratio";
    public static final String KEY_PARAM_VIDEO_WIDTH = "key_param_video_width";
    public static final String KEY_PARAM_VIDEO_HEIGHT = "key_param_video_height";

    private static final int REQUEST_CODE_SELECT_COVER_PATH = 0x55;

    private Context mContext;
    private PaiyaPublishActivity mActivity;
    private String mConfigPath;
    private int mVideoWidth, mVideoHeight;
    private float mVideoRatio;
    private AliyunVideoParam mVideoParam;

    private ProgressBar progressPublish;
    private TextView tvUseVideo, tvPublishProgress, tvPublishHint, tvSelectCover;
    private ImageView imgCover;

    private int mVideoRotation;

    public void setVideoRotation(int rotation) {
        this.mVideoRotation = rotation;
    }

    /**
     * 视频地址
     */
    private String mOutputPath = "";
    /**
     * 视频封面地址
     */
    private String mThumbnailPath = "";

    private boolean mComposeCompleted;

    private AliyunIVodCompose mCompose;
    private RequestOptions mOptions;

    private void findViews() {
        tvUseVideo = findViewById(R.id.tvUseVideo);
        tvUseVideo.setEnabled(false);
        tvUseVideo.getBackground().setAlpha(100);
        progressPublish = findViewById(R.id.progressPublish);
        tvPublishProgress = findViewById(R.id.tvPublishProgress);
        tvPublishHint = findViewById(R.id.tvPublishHint);
        imgCover = findViewById(R.id.imgCover);
        tvSelectCover = findViewById(R.id.tvSelectCover);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paiya_editor_activity_publish);
        mContext = getApplicationContext();
        mActivity = this;
        findViews();
        initData();


        if (mVideoRotation == 90 || mVideoRotation == 270) {
            ConstraintLayout.LayoutParams coverParams = (ConstraintLayout.LayoutParams) imgCover.getLayoutParams();
            coverParams.dimensionRatio = "h,16:9";
            imgCover.setLayoutParams(coverParams);
        }

        startUseVideo();

        CenterCropRoundCornerTransform roundedCorners = new CenterCropRoundCornerTransform(DensityUtils.dip2px(getApplicationContext(), 20));
        mOptions = RequestOptions.bitmapTransform(roundedCorners);
    }

    /**
     * 设置封面
     */
    private void setCoverView(String coverImagePath) {
        Glide.with(getApplicationContext()).load(coverImagePath).apply(mOptions)
                .skipMemoryCache(true) // 不使用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .into(imgCover);
    }

    /**
     *
     */
    private void initData() {
        mConfigPath = getIntent().getStringExtra(KEY_PARAM_CONFIG);
        mThumbnailPath = getIntent().getStringExtra(KEY_PARAM_THUMBNAIL);
        mVideoWidth = getIntent().getIntExtra(KEY_PARAM_VIDEO_WIDTH, 0);
        mVideoHeight = getIntent().getIntExtra(KEY_PARAM_VIDEO_HEIGHT, 0);
        mVideoRatio = getIntent().getFloatExtra(KEY_PARAM_VIDEO_RATIO, 0f);
        mVideoParam = (AliyunVideoParam) getIntent().getSerializableExtra("videoParam");
        mVideoRotation = getIntent().getIntExtra("mVideoRotation", 0);

        mCompose = ComposeFactory.INSTANCE.getAliyunVodCompose();
        mCompose.init(this.getApplicationContext());
    }

    /**
     * videoType：0合成中；1合成成功；2合成失败
     */
    private void setUseVideoViewStyle(int videoType, int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (videoType == 0) {
                    progressPublish.setProgress(progress);
                    tvPublishProgress.setText(progress + "%");
                } else if (videoType == 1) {
                    tvUseVideo.setEnabled(true);
                    tvUseVideo.getBackground().setAlpha(255);
                    progressPublish.setVisibility(View.GONE);
                    tvPublishProgress.setText(progress + "%");
                    tvPublishHint.setText(R.string.paiya_publish_created);
                    //请务必在非回调线程调用，避免内存泄露
                    if (mCompose != null) {
                        mCompose.release();
                        mCompose = null;
                    }
                    imgCover.setVisibility(View.VISIBLE);
                    tvSelectCover.setVisibility(View.VISIBLE);
                } else {
                    progressPublish.setVisibility(View.GONE);
                    tvPublishHint.setText(R.string.alivc_editor_publish_tip_retry);
                    tvPublishProgress.setText(R.string.alivc_editor_publish_compose_failed);
                }
            }
        });
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String getFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 开始合成
     */
    private void startUseVideo() {
        String time = DateTimeUtils.getDateTimeFromMillisecond(System.currentTimeMillis());
        mOutputPath = Constants.SDCardConstants.getDir(this) + time + Constants.SDCardConstants.COMPOSE_SUFFIX;

        int ret = mCompose.compose(mConfigPath, mOutputPath, new AliyunIComposeCallBack() {
            @Override
            public void onComposeError(int errorCode) {
                setUseVideoViewStyle(2, 0);
            }

            @Override
            public void onComposeProgress(int progress) {
                setUseVideoViewStyle(0, progress);
            }

            @Override
            public void onComposeCompleted() {
                mComposeCompleted = true;
                setUseVideoViewStyle(1, 100);
                VideoInfoUtils.printVideoInfo(mOutputPath);

                getThumbnailImage(false);
            }
        });
        if (ret != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            return;
        }
    }


    private void getThumbnailImage(boolean isAgain) {
        AliyunIThumbnailFetcher mCoverThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mCoverThumbnailFetcher.addVideoSource(mOutputPath, 0, Integer.MAX_VALUE, 0);
        if(isAgain){
            if (mVideoRotation == 90 || mVideoRotation == 270) {
                mCoverThumbnailFetcher.setParameters(1280 * 3 / 8, 720 * 3 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
            } else {
                mCoverThumbnailFetcher.setParameters(720 * 3 / 8, 1280 * 3 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
            }
        }else {
            if (mVideoRotation == 90 || mVideoRotation == 270) {
                mCoverThumbnailFetcher.setParameters(1280 * 6 / 8, 720 * 6 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
            } else {
                mCoverThumbnailFetcher.setParameters(720 * 6 / 8, 1280 * 6 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
            }
        }

        mCoverThumbnailFetcher.requestThumbnailImage(new long[]{0},
                new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                    @Override
                    public void onThumbnailReady(Bitmap bitmap, long l, int index) {
                        if (bitmap != null && !bitmap.isRecycled()) {
                            String path = FileUtils.getDiskCachePath(mContext) + File.separator + "Media" + File.separator;
                            mThumbnailPath = FileUtils.createFile(path, "thumbnail.jpg").getPath();
                            FileOutputStream fileOutputStream = null;
                            try {
                                fileOutputStream = new FileOutputStream(mThumbnailPath);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                            } catch (Exception e) {
                                ToastUtils.show(getApplicationContext(), R.string.alivc_editor_cover_fetch_cover_error);
                                return;
                            } finally {
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            setCoverView(mThumbnailPath);
                        }
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e("AAA", "getCoverBitmap error msg: " + errorCode);
                        if (!isAgain) {
                            getThumbnailImage(true);
                        }
                    }
                });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_COVER_PATH && resultCode == RESULT_OK) {
            mThumbnailPath = data.getStringExtra(CoverEditActivity2.KEY_PARAM_RESULT);
            setCoverView(mThumbnailPath);
        }
    }


    private void showBackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.setTitle(R.string.alivc_editor_publish_dialog_cancel_content_tip)
                .setNegativeButton(R.string.alivc_editor_publish_goback, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!mComposeCompleted) {
                            if (mCompose != null) {
                                mCompose.cancelCompose();
                            }
                        }
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setPositiveButton(R.string.alivc_editor_publish_continue, null).create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mComposeCompleted) {
            setResult(Activity.RESULT_OK);
            super.onBackPressed();
        } else {
            showBackDialog();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCompose != null) {
            mCompose.resumeCompose();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCompose != null) {
            mCompose.pauseCompose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompose != null) {
            mCompose.release();
            mCompose = null;
        }
    }


    private boolean isClickUseVideo;

    public void onPublishClick(View view) {
        int id = view.getId();
        if (id == R.id.imgBack) {
            onBackPressed();
//            finish();
        } else if (id == R.id.tvUseVideo) {
            useVideo();
        } else if (id == R.id.tvSelectCover) {
            //选择封面
            Intent intent = new Intent(this, CoverEditActivity2.class);
            intent.putExtra(CoverEditActivity2.KEY_PARAM_VIDEO, mOutputPath);
            intent.putExtra("mVideoRotation", mVideoRotation);
            startActivityForResult(intent, REQUEST_CODE_SELECT_COVER_PATH);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            FileUtils.deleteFileOnExit(mOutputPath);
            setResult(Activity.RESULT_OK);
        }
    }


    /**
     * 使用视频
     */
    private void useVideo() {
        AlivcCircleLoadingDialog mLoadingDialog = new AlivcCircleLoadingDialog(this, 0);
        mLoadingDialog.show();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                try {
                    saveVideo(getApplicationContext(), new File(mOutputPath));
                    emitter.onNext("");
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())// 子线程
                .observeOn(AndroidSchedulers.mainThread()) //观察者切换为主线程，刷新UI
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(@NonNull String s) {
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        if (isFinishing()) {
                            return;
                        }
                        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                            mLoadingDialog.dismiss();
                        }

                        ToastUtils.show(mContext,getString(R.string.alivc_video_save_to_photo));
                        toUpdateVideo();
                    }
                });
    }


    /**
     * 去上传视频
     */
    private void toUpdateVideo() {
        //使用视频
        Intent intent = new Intent();
        String packName = getPackageName();
        intent.setClassName(mActivity, packName + ".MainActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //视频地址
        intent.putExtra("videoPath", mOutputPath);
        //视频封面地址
        intent.putExtra("coverImageUrl", mThumbnailPath);
        isClickUseVideo = true;
        startActivity(intent);
        overridePendingTransition(R.anim.fade_no_amin, R.anim.fade_out);
    }

    /**
     * 保存视频到相册
     *
     * @param context
     */
    private void saveVideo(Context context, File srcFile) throws Exception {
        if (!PermissionUtils.checkPermissionsGroup(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            return;
        }
        String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/paiyatalent/video";
        //拷贝文件到目录
        File dirPath = FileUtils.createDir(rootPath);
        if (dirPath == null) {
            //创建拷贝目录失败，直接返回
            return;
        }
        File outFile = new File(dirPath, srcFile.getName());
        boolean isCopy = com.blankj.utilcode.util.FileUtils.copy(srcFile, outFile);
        if (!outFile.exists()) {
            return;
        }
        //通知相册刷新
        refreshPhoto(context, outFile.getPath());
        refreshPhotoBroadcast(context, outFile);
    }


    /**
     * 刷新相册
     */
    private static void refreshPhoto(Context context, String outputFilePath) {
        // 扫描本地mp4文件并添加到本地视频库
        MediaScannerConnection mMediaScanner = new MediaScannerConnection(context, null);
        mMediaScanner.connect();
        if (mMediaScanner != null && mMediaScanner.isConnected()) {
            mMediaScanner.scanFile(outputFilePath, null);
        }
    }

    /**
     * 发送广播刷新
     */
    private static void refreshPhotoBroadcast(Context context, File outputFile) {
        if (outputFile != null && outputFile.exists()) {
            Uri uri = Uri.fromFile(outputFile);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }
    }

}

