package com.aliyun.svideo.editor.publish.paiya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.base.BaseAliActivity;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.FileUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 拍鸭：视频封面选择
 */
public class CoverEditActivity2 extends BaseAliActivity {

    public static final String KEY_PARAM_VIDEO = "videos_path";
    public static final String KEY_PARAM_RESULT = "thumbnail";

    private FrameLayout coverBottomLayout;
    private CardView layoutCover;
    private TextureView coverTextureView;
    private View viewIndicator;
    private RecyclerView coverThumbnailRecyclerView;

    private Context mContext;
    private final int mThumbnailCount = 8;
    private ThumbnailFetcherManage mThumbnailFetcherManage;
    private PlayerVideoCoverManage mPlayerVideoCoverManage;


    private List<CoverInfo> mBitmapList = Arrays.asList(new CoverInfo[mThumbnailCount]);
    private long mSeekToTime;

    /**
     * 视频地址
     */
    private String mVideoPath = "";

    /**
     * 截取视频单个封面的宽度
     */
    private int mCoverItemWidth, mCoverItemHeight;

    private int mVideoRotation;

    public void setVideoRotation(int rotation) {
        this.mVideoRotation = rotation;
    }

    private void findViews() {
        coverBottomLayout = findViewById(R.id.coverBottomLayout);
        layoutCover = findViewById(R.id.layoutCover);
        coverTextureView = findViewById(R.id.coverTextureView);
        viewIndicator = findViewById(R.id.viewIndicator);
        coverThumbnailRecyclerView = findViewById(R.id.coverThumbnailRecyclerView);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paiya_cover_edit_activity);
        mContext = getApplicationContext();

        mCoverItemHeight =  DensityUtils.dip2px(mContext, 50f);

        mVideoPath = getIntent().getStringExtra(KEY_PARAM_VIDEO);
        mVideoRotation = getIntent().getIntExtra("mVideoRotation", 0);

        findViews();

        initData();


        initTextureView();

        setOnTouchCoverView();
    }


    private void initData() {
        //竖屏
        ConstraintLayout.LayoutParams coverParams = (ConstraintLayout.LayoutParams) layoutCover.getLayoutParams();
        ConstraintLayout.LayoutParams vParams = (ConstraintLayout.LayoutParams) coverBottomLayout.getLayoutParams();
        if (mVideoRotation == 0 || mVideoRotation == 180) {
            //竖屏拍摄
            coverParams.dimensionRatio = "9:16";
            coverParams.matchConstraintPercentWidth = 0.75f;
            layoutCover.setLayoutParams(coverParams);

            vParams.matchConstraintPercentWidth = 0.75f;
            coverBottomLayout.setLayoutParams(vParams);

            mCoverItemWidth = (int) ((ScreenUtils.getWidth(mContext) * 0.75f) / mThumbnailCount);
        }else {
            //横屏拍摄
            coverParams.dimensionRatio = "16:9";
            coverParams.matchConstraintPercentWidth = 0.8f;
            layoutCover.setLayoutParams(coverParams);

            vParams.matchConstraintPercentWidth = 0.8f;
            coverBottomLayout.setLayoutParams(vParams);

            mCoverItemWidth = (int) ((ScreenUtils.getWidth(mContext) * 0.8f) / mThumbnailCount);
        }

        mThumbnailFetcherManage = new ThumbnailFetcherManage(mVideoPath, mCoverItemWidth, mCoverItemHeight, mThumbnailCount);
        mThumbnailFetcherManage.getCoverThumbnailList(new ThumbnailFetcherManage.OnCoverThumbnailCallback() {
            @Override
            public void onNext(CoverInfo coverInfo) {
                if (coverInfo != null) {
                    mBitmapList.set(coverInfo.getPosition(), coverInfo);
                    fillAdapter(coverInfo.getPosition());
                }
            }
        });
    }


    private void initTextureView() {
        mPlayerVideoCoverManage = new PlayerVideoCoverManage(mContext, mVideoPath);
        coverTextureView.setSurfaceTextureListener(mPlayerVideoCoverManage);
    }

    private void setOnTouchCoverView() {
        long videoDuration = mThumbnailFetcherManage.getDuration();
        new CoverThumbnailOnTouchManage(mContext, coverThumbnailRecyclerView, videoDuration, new CoverThumbnailOnTouchManage.OnTouchCallback() {
            @Override
            public void onSeekTo(float indicatorX, long seekTime) {
                viewIndicator.setX(indicatorX);
                if (mPlayerVideoCoverManage != null) {
                    mSeekToTime = seekTime;
                    mPlayerVideoCoverManage.seekTo(seekTime);
                }
            }
        });
    }


    private CoverThumbnailAdapter mAdapter;

    private void fillAdapter(Integer position) {
        if (mAdapter == null) {
            mAdapter = new CoverThumbnailAdapter(mContext, mBitmapList, mCoverItemWidth, mCoverItemHeight);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false) {
                @Override
                public boolean canScrollHorizontally() {
                    return false;
                }
            };
            coverThumbnailRecyclerView.setLayoutManager(layoutManager);
            coverThumbnailRecyclerView.setItemAnimator(new DefaultItemAnimator());
            coverThumbnailRecyclerView.setAdapter(mAdapter);
        } else {
            if (position != null) {
                int index = position;
                if (index >= 0 && index < mBitmapList.size()) {
                    mAdapter.notifyItemChanged(index);
                    return;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mThumbnailFetcherManage != null) {
            mThumbnailFetcherManage.cleans();
        }
    }

    /**
     * 使用该封面
     */
    private void setCover() {
        AlivcCircleLoadingDialog mLoadingDialog = new AlivcCircleLoadingDialog(this, 0);
        mLoadingDialog.show();
        mThumbnailFetcherManage.getCoverBitmap(mVideoRotation,mSeekToTime,false, new ThumbnailFetcherManage.OnCoverThumbnailCallback() {
            @Override
            public void onNextCoveError(int errorCode) {
                ToastUtils.show(CoverEditActivity2.this, R.string.alivc_editor_cover_fetch_cover_error);
            }

            @Override
            public void onNextCover(Bitmap bitmap) {
                String path = FileUtils.getDiskCachePath(mContext) + File.separator + "Media" + File.separator ;
                path =  FileUtils.createFile(path,"thumbnail.jpg").getPath();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                } catch (Exception e) {
                    ToastUtils.show(CoverEditActivity2.this, R.string.alivc_editor_cover_fetch_cover_error);
                    mLoadingDialog.dismiss();
                    return;
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mLoadingDialog.dismiss();
                }

                Intent data = new Intent();
                data.putExtra(KEY_PARAM_RESULT, path);
                setResult(RESULT_OK, data);
                finish();

            }
        });
    }

    /**
     * 点击事件
     */
    public void onCoverEditClick(View view) {
        int id = view.getId();
        if (id == R.id.imgBack) {
            finish();
        } else if (id == R.id.imgSave) {
            //使用封面
            setCover();
        }
    }

}
