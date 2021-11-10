package com.aliyun.svideo.editor.publish.paiya;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;

/**
 * 视频帧管理
 */
public class ThumbnailFetcherManage {

    private final AliyunIThumbnailFetcher mThumbnailFetcher;

    private AliyunIThumbnailFetcher mCoverThumbnailFetcher;

    private long mDuration;

    private int mCacheSize;


    public interface OnCoverThumbnailCallback {
        default void onStart(int cacheSize) {
        }
        default void onEndWidth(double endWidth) {
        }

        default void onNext(CoverInfo coverInfo) {
        }

        default void onNextCover(Bitmap bitmap) {
        }

        default void onNextCoveError(int errorCode) {
        }
    }

    public ThumbnailFetcherManage(String mVideoPath, int itemWidth, int itemHeight, int cacheSize) {
        this.mCacheSize = cacheSize;
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.addVideoSource(mVideoPath, 0, Integer.MAX_VALUE, 0);
        mThumbnailFetcher.setParameters(itemWidth, itemHeight, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, cacheSize);

        mCoverThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mCoverThumbnailFetcher.addVideoSource(mVideoPath, 0, Integer.MAX_VALUE, 0);


        mDuration = mThumbnailFetcher.getTotalDuration();
    }


    public ThumbnailFetcherManage(Uri mUri, int itemWidth, int itemHeight, int cacheSize) {
        this.mCacheSize = cacheSize;
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.fromConfigJson(mUri.getPath());
        mThumbnailFetcher.setParameters(itemWidth, itemHeight,
                AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, cacheSize);
        mDuration = mThumbnailFetcher.getTotalDuration();
    }

    public void getCoverThumbnailList(OnCoverThumbnailCallback callback, long startTime, long endTime, int count) {
        mDuration = endTime - startTime;
        mCacheSize = count;
        long interval = mDuration / mCacheSize;//取值间隔
        if (callback != null) {
            callback.onStart(mCacheSize);
        }
        for (int i = 0; i < mCacheSize; i++) {
            long coverTimeMs = interval * i + interval / 2 + startTime;
            if (coverTimeMs > endTime) {
                coverTimeMs = endTime;
            }
            long[] coverTimes = {coverTimeMs / 1000};
            final int[] position = {i};
            mThumbnailFetcher.requestThumbnailImage(coverTimes, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                @Override
                public void onThumbnailReady(Bitmap bitmap, long longTime, int index) {
//                    Log.e("BBB", "onThumbnailReady longTime: " + longTime);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        if (callback != null) {
                            CoverInfo coverInfo = new CoverInfo();
                            coverInfo.setBitmap(bitmap);
                            coverInfo.setPosition(position[0]);
                            callback.onNext(coverInfo);
                        }
                    }
                }

                @Override
                public void onError(int errorCode) {
                }
            });
        }
    }

    public int getCoverThumbnailList(OnCoverThumbnailCallback callback, long startTime, long endTime, long mCacheIntervalTime) {
        mDuration = endTime - startTime;
        long endIntervalTime = mDuration % mCacheIntervalTime;
        double endIntervalWidth = 0;

        mCacheSize = (int) ((int) mDuration / mCacheIntervalTime);
        if (endIntervalTime >= mCacheIntervalTime / 100) {
            mCacheSize = mCacheSize + 1;
            endIntervalWidth = (double) endIntervalTime / (double) mCacheIntervalTime;
        }
        long interval;
        if (mCacheSize == 0) {
            interval = mCacheIntervalTime / 2;
        } else {
            interval = mDuration / mCacheSize;
        }
        if (callback != null) {
            callback.onStart(mCacheSize);
            callback.onEndWidth(endIntervalWidth);
        }
        for (int i = 0; i < mCacheSize; i++) {
            long coverTimeMs = interval * i + interval / 2 + startTime;
            if (coverTimeMs > endTime) {
                coverTimeMs = endTime;
            }
            long[] coverTimes = {coverTimeMs / 1000};
            final int[] position = {i};
            mThumbnailFetcher.requestThumbnailImage(coverTimes, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                @Override
                public void onThumbnailReady(Bitmap bitmap, long longTime, int index) {
//                    Log.e("BBB", "onThumbnailReady longTime: " + longTime);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        if (callback != null) {
                            CoverInfo coverInfo = new CoverInfo();
                            coverInfo.setBitmap(bitmap);
                            coverInfo.setPosition(position[0]);
                            callback.onNext(coverInfo);
                        }
                    }
                }

                @Override
                public void onError(int errorCode) {
                }
            });
        }
        return mCacheSize;
    }

    /**
     * 获取缩略图列表
     */
    public void getCoverThumbnailList(OnCoverThumbnailCallback callback) {
        long interval = mDuration / mCacheSize;
        for (int i = 0; i < mCacheSize; i++) {
            long coverTimeMs = interval * i + interval / 2;
            if (coverTimeMs > mDuration) {
                coverTimeMs = mDuration;
            }
            long[] coverTimes = {coverTimeMs};
            final int[] position = {i};
            mThumbnailFetcher.requestThumbnailImage(coverTimes, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                @Override
                public void onThumbnailReady(Bitmap bitmap, long longTime, int index) {
//                    Log.e("BBB", "onThumbnailReady longTime: " + longTime);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        if (callback != null) {
                            CoverInfo coverInfo = new CoverInfo();
                            coverInfo.setBitmap(bitmap);
                            coverInfo.setPosition(position[0]);
                            callback.onNext(coverInfo);
                        }
                    }
                }

                @Override
                public void onError(int errorCode) {
                    Log.e("BBB", "getCoverThumbnailList error msg: " + errorCode);
                }
            });
        }
    }

    /**
     * 获取封面
     *
     * @param coverTime ms
     */
    public void getCoverBitmap(int mVideoRotation, long coverTime, boolean isAgain, OnCoverThumbnailCallback callback) {
        if (mCoverThumbnailFetcher == null) {
            if (callback != null) {
                callback.onNextCover(null);
            }
            return;
        }
        if (mVideoRotation == 90 || mVideoRotation == 270) {
            mCoverThumbnailFetcher.setParameters(1280 * 6 / 8, 720 * 6 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
        } else {
            mCoverThumbnailFetcher.setParameters(720 * 6 / 8, 1280 * 6 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
        }

        mCoverThumbnailFetcher.requestThumbnailImage(new long[]{coverTime},
                new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                    @Override
                    public void onThumbnailReady(Bitmap bitmap, long l, int index) {
                        if (bitmap != null && !bitmap.isRecycled()) {
                            if (callback != null) {
                                callback.onNextCover(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e("AAA", "getCoverBitmap error msg: " + errorCode);
                        if (!isAgain) {
                            if (mVideoRotation == 90 || mVideoRotation == 270) {
                                mCoverThumbnailFetcher.setParameters(1280 * 3 / 8, 720 * 3 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
                            } else {
                                mCoverThumbnailFetcher.setParameters(720 * 3 / 8, 1280 * 3 / 8, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
                            }
                            getCoverBitmap(mVideoRotation, coverTime, true, callback);
                        } else {
                            if (callback != null) {
                                callback.onNextCoveError(errorCode);
                            }
                        }
                    }
                });
    }


    /**
     * 获取视频时长
     *
     * @return ms
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * 清除
     */
    public void cleans() {
        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }
        if (mCoverThumbnailFetcher != null) {
            mCoverThumbnailFetcher.release();
        }
    }

}
