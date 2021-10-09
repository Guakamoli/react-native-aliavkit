package com.aliyun.svideo.editor.editor.thumblinebar;

import android.graphics.Point;
import android.net.Uri;

import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;

/**
 * Created by cross_ly on 2018/8/22.
 * <p>描述:缩略图配置类 自定义配置时使用
 */
public class ThumbLineConfig {

    /**
     * 缩略图获取者
     */
    private AliyunIThumbnailFetcher thumbnailFetcher;

    /**
     * 缩略图总数 默认10张
     */

    private int thumbnailCount = 10;

    /**
     * 缩略图尺寸 x = width , y = height.
     */
    private Point thumbnailPoint;

    /**
     * screen width
     */
    private int screenWidth;

    private Uri mUri;
    private long mStartTime;
    private long mEndTime;

    private ThumbLineConfig() {
    }

    public AliyunIThumbnailFetcher getThumbnailFetcher() {
        return thumbnailFetcher;
    }

    public Point getThumbnailPoint() {
        return thumbnailPoint;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getThumbnailCount() {
        return thumbnailCount;
    }

    public Uri getUri() {
        return mUri;
    }
    public long getStartTime() {
        return mStartTime;
    }
    public long getEndTime() {
        return mEndTime;
    }

    /**
     * thumbLineConfig builder
     */
    public static class Builder {
        private ThumbLineConfig mConfig = new ThumbLineConfig();

        public Builder thumbnailFetcher(AliyunIThumbnailFetcher thumbnailFetcher) {
            mConfig.thumbnailFetcher = thumbnailFetcher;
            return this;
        }

        public Builder uri(Uri uri) {
            mConfig.mUri = uri;
            return this;
        }

        public Builder startTime(long startTime) {
            mConfig.mStartTime = startTime;
            return this;
        }
        public Builder endTime(long endTime) {
            mConfig.mEndTime = endTime;
            return this;
        }
        public Builder thumbPoint(Point point) {
            mConfig.thumbnailPoint = point;
            return this;
        }

        public Builder screenWidth(int screenWidth) {
            mConfig.screenWidth = screenWidth;
            return this;
        }

        public Builder thumbnailCount(int thumbnailCount) {
            mConfig.thumbnailCount = thumbnailCount;
            return this;
        }

        public ThumbLineConfig build() {
            return mConfig;
        }

    }
}
