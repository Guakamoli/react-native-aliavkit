package com.aliyun.svideo.editor.publish.paiya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.aliyun.svideo.common.utils.DensityUtils;

/**
 * 封面缩略图触摸管理
 */
public class CoverThumbnailOnTouchManage {

    public interface OnTouchCallback{
        void onSeekTo(  float indicatorX,long seekTime );
    }

    public CoverThumbnailOnTouchManage(Context context,ViewGroup coverThumbnailView, long videoDuration,OnTouchCallback callback) {
        coverThumbnailView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                coverThumbnailView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int layoutWidth = coverThumbnailView.getMeasuredWidth();
                int layoutLeft = coverThumbnailView.getLeft();
                int layoutRight = coverThumbnailView.getRight();
//                int paddingLeft = DensityUtils.dip2px(context, 16f);
                int paddingLeft = 0;
                int indicatorWidth = DensityUtils.dip2px(context, 3f);
                coverThumbnailView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        float eventX = event.getX();
                        if (eventX < layoutLeft) {
                            eventX = layoutLeft;
                        }
                        if (eventX > layoutRight) {
                            eventX = layoutRight;
                        }
                        float indicatorX = eventX + paddingLeft;
                        if (indicatorX > layoutRight + paddingLeft - indicatorWidth) {
                            indicatorX = indicatorX - indicatorWidth;
                        }
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_MOVE:
                            case MotionEvent.ACTION_UP:
                                if(callback!=null){
                                    long seekTime = (long) (videoDuration * eventX / layoutWidth);
                                    callback.onSeekTo(indicatorX,seekTime);
                                }
//                                viewIndicator.setX(indicatorX);
//                                if (mPlayerVideoCoverManage != null) {
//
//                                    mSeekToTime = seekTime;
//                                    mPlayerVideoCoverManage.seekTo(seekTime);
//                                }
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
            }
        });
    }

}
