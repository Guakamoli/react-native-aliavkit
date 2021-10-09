package com.aliyun.svideo.editor.effects.captions.adapter;

import java.io.Serializable;

public class CaptionOverlayInfo implements Serializable {

    private long startTime;
    private long endTime;
    private long minDuration = 200000;   //最小时长，到达最小时长内再无法缩减, 默认0.2s
    private boolean isShowThumb; //是否显示滑块

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(long minDuration) {
        this.minDuration = minDuration;
    }

    public boolean isShowThumb() {
        return isShowThumb;
    }

    public void setShowThumb(boolean showThumb) {
        isShowThumb = showThumb;
    }
}
