/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor.thumblinebar;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.publish.paiya.CoverInfo;
import com.aliyun.svideo.editor.publish.paiya.ThumbnailFetcherManage;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThumbRecyclerAdapter extends RecyclerView.Adapter<ThumbRecyclerAdapter.ThumbnailViewHolder> {

    private ThumbnailFetcherManage mThumbnailFetcherManage;

    private final List<CoverInfo> mBitmapList = new ArrayList<>();

    private static final String TAG = "ThumbRecyclerAdapter";
    //    private AliyunIThumbnailFetcher mFetcher;
    private int mCount;
    private long mInterval = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_FOOTER = 2;
    private static final int VIEW_TYPE_THUMBNAIL = 3;
    private final int mScreenWidth;

    private Uri mUri;

    public ThumbRecyclerAdapter(int count, int duration, AliyunIThumbnailFetcher fetcher, int screenWidth, int thumbnailWidth, int thumbnailHeight, ThumbLineConfig thumbLineConfig) {
        mInterval = duration / count;
//        this.mFetcher = fetcher;
        this.mCount = count;
        this.mScreenWidth = screenWidth;
        this.mUri = thumbLineConfig.getUri();
//        mFetcher.setParameters(thumbnailWidth, thumbnailHeight, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);

        mThumbnailFetcherManage = new ThumbnailFetcherManage(mUri, thumbnailWidth, thumbnailHeight, mCount);
        mThumbnailFetcherManage.getCoverThumbnailList(new ThumbnailFetcherManage.OnCoverThumbnailCallback() {
            @Override
            public void onNext(CoverInfo coverInfo) {
                if (coverInfo != null) {
                    mBitmapList.set(coverInfo.getPosition(), coverInfo);
                    notifyItemChanged(coverInfo.getPosition()+1);
                }
            }
        }, thumbLineConfig.getStartTime(), thumbLineConfig.getEndTime(), mCount);
        mBitmapList.clear();
        mBitmapList.addAll(Arrays.asList(new CoverInfo[mCount]));

    }

    public void setData(int count, int duration, AliyunIThumbnailFetcher fetcher, int screenWidth, int thumbnailWidth, int thumbnailHeight, ThumbLineConfig thumbLineConfig) {
        this.mUri = thumbLineConfig.getUri();
        this.mCount = count;
        mThumbnailFetcherManage = new ThumbnailFetcherManage(mUri, thumbnailWidth, thumbnailHeight, mCount);
        mThumbnailFetcherManage.getCoverThumbnailList(new ThumbnailFetcherManage.OnCoverThumbnailCallback() {
            @Override
            public void onNext(CoverInfo coverInfo) {
                if (coverInfo != null) {
                    mBitmapList.set(coverInfo.getPosition(), coverInfo);
                    notifyDataSetChanged();
                }
            }
        }, thumbLineConfig.getStartTime(), thumbLineConfig.getEndTime(), mCount);
        mBitmapList.clear();
        mBitmapList.addAll(Arrays.asList(new CoverInfo[mCount]));
        notifyDataSetChanged();
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThumbnailViewHolder holder;
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
            case VIEW_TYPE_FOOTER:
                itemView = new View(parent.getContext());
                itemView.setLayoutParams(new ViewGroup.LayoutParams(mScreenWidth / 2, ViewGroup.LayoutParams.MATCH_PARENT));
                itemView.setBackgroundColor(Color.TRANSPARENT);
                holder = new ThumbnailViewHolder(itemView);
                return holder;
            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_item_timeline_thumbnail, parent, false);
                holder = new ThumbnailViewHolder(itemView);
                holder.mIvThumbnail = itemView.findViewById(R.id.iv_thumbnail);
                return holder;
        }
    }

    @Override
    public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
        if (position > 0 && position < mCount + 1) {
            if (position-1 < mBitmapList.size() && mBitmapList.get(position-1) != null) {
                Bitmap bitmap = mBitmapList.get(position-1).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    holder.mIvThumbnail.setImageBitmap(bitmap);
                }
            }
        }
    }


    @Override
    public int getItemCount() {
        return mCount == 0 ? 0 : mCount + 2;//这里加上前后部分的view
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else if (position == mCount + 1) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_THUMBNAIL;
        }
    }

    @Override
    public void onViewRecycled(ThumbnailViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.mIvThumbnail != null) {
            holder.mIvThumbnail.setImageBitmap(null);
        }
    }


    public class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView mIvThumbnail;
        ThumbnailViewHolder(View itemView) {
            super(itemView);
        }
    }
}
