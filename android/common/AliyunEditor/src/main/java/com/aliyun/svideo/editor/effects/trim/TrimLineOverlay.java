package com.aliyun.svideo.editor.effects.trim;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.captions.CaptionLineOverlay;
import com.aliyun.svideo.editor.effects.captions.OverlayTextView;

public class TrimLineOverlay extends LinearLayout {

    private float mTimeWidth;
    private float mBaseWidth, mMinTimeWidth;
    private long mDuration;
    /**
     * 两边滑块的宽度
     */
    private float mThumbWidth;

    /**
     * 尺寸转为时间
     */
    private long distance2Duration(float distance) {
        float duration = distance * mDuration / mTimeWidth;
        return Math.round(duration);
    }

    /**
     * 时间转为尺寸
     */
    private float duration2Distance(long duration) {
        float distance = duration * mTimeWidth / mDuration;
        return Math.round(distance);
    }

    public void setData(int itemWidth, int minTimeWidth, long duration, long startTime, long endTime) {
        this.mBaseWidth = itemWidth;
        this.mMinTimeWidth = minTimeWidth;
        this.mDuration = duration;
        this.mTimeWidth = mBaseWidth - mThumbWidth * 2;
        if (startTime > 0 && leftOverlayLayout != null) {
            float leftWidth = duration2Distance(startTime);
            ViewGroup.LayoutParams leftThumbParam = leftOverlayLayout.getLayoutParams();
            leftThumbParam.width = (int) leftWidth;
            leftOverlayLayout.setLayoutParams(leftThumbParam);
        }
        if (endTime < mDuration && rightOverlayLayout != null) {
            float leftWidth = duration2Distance(mDuration - endTime);
            ViewGroup.LayoutParams leftThumbParam = rightOverlayLayout.getLayoutParams();
            leftThumbParam.width = (int) leftWidth;
            rightOverlayLayout.setLayoutParams(leftThumbParam);
        }
    }

    public TrimLineOverlay(Context context) {
        super(context);
        init(context);
    }

    public TrimLineOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrimLineOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TrimLineOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private Context mContext;

    private FrameLayout overlayLayout;
    private View leftOverlayLayout, rightOverlayLayout, imgLeftThumb, imgRightThumb;


    private void init(Context context) {
        mContext = context;
        mThumbWidth = DensityUtils.dip2px(mContext, 20f);
        View view = LayoutInflater.from(context).inflate(R.layout.paiya_trim_line_overlay, this);
        overlayLayout = view.findViewById(R.id.overlayLayout);
        imgLeftThumb = view.findViewById(R.id.imgLeftThumb);
        imgRightThumb = view.findViewById(R.id.imgRightThumb);
        leftOverlayLayout = view.findViewById(R.id.leftOverlayLayout);
        rightOverlayLayout = view.findViewById(R.id.rightOverlayLayout);
        imgLeftThumb.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (imgLeftThumb.getVisibility() == View.VISIBLE) {
                    return setThumbOnTouch(event, 0, imgLeftThumb);
                } else {
                    return true;
                }
            }
        });

        imgRightThumb.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (imgRightThumb.getVisibility() == View.VISIBLE) {
                    return setThumbOnTouch(event, 1, imgRightThumb);
                } else {
                    return true;
                }
            }
        });
    }


    /**
     * 记录下点击时字幕的左右两边滑块位置，以及点击的位置
     */
    private float thumbLeftX, thumbRightX, mThumbTouchX;
    private float mLeftOverlayWidth, mRightOverlayWidth;

    private boolean setThumbOnTouch(MotionEvent event, int startType, View view) {
        boolean isOnTouch = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.getParent().requestDisallowInterceptTouchEvent(true);
                isOnTouch = true;
                mLeftOverlayWidth = thumbLeftX = leftOverlayLayout.getMeasuredWidth();
                mRightOverlayWidth = thumbRightX = rightOverlayLayout.getMeasuredWidth();
                mThumbTouchX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                isOnTouch = true;
                float rawX = event.getRawX();
                float dx = rawX - mThumbTouchX;
                if (startType == 0) {
                    mLeftOverlayWidth = thumbLeftX + dx;
                    if (mLeftOverlayWidth < 0) {
                        mLeftOverlayWidth = 0;
                    }
                    long playtime = distance2Duration(mLeftOverlayWidth);
                    if (mSelectorPlayTimeListener != null) {
                        mSelectorPlayTimeListener.onUpdateTime(playtime);
                    }
                    upDataViewWidth(leftOverlayLayout, mLeftOverlayWidth, playtime);
                } else {
                    mRightOverlayWidth = thumbRightX - dx;
                    if (mRightOverlayWidth < 0) {
                        mRightOverlayWidth = 0;
                    }
                    long playtime = distance2Duration(mTimeWidth - mRightOverlayWidth);
                    if (mSelectorPlayTimeListener != null) {
                        mSelectorPlayTimeListener.onUpdateTime(playtime);
                    }
                    upDataViewWidth(rightOverlayLayout, mRightOverlayWidth, playtime);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.getParent().requestDisallowInterceptTouchEvent(false);
                isOnTouch = false;

                long playStartTime = distance2Duration(mLeftOverlayWidth);
                long playEndTime = distance2Duration(mTimeWidth - mRightOverlayWidth);
                if (mSelectorPlayTimeListener != null) {
                    mSelectorPlayTimeListener.onSetPlayTime(startType, playStartTime, playEndTime);
                }
                break;
            default:
                break;
        }
        return isOnTouch;
    }

    private void upDataViewWidth(View view, float width, long playtime) {
        if (mBaseWidth - mLeftOverlayWidth - mRightOverlayWidth - mThumbWidth * 2 < mMinTimeWidth) {
            return;
        }
//        Log.e("AAA", "playtime:" + playtime);
        if (width >= 0) {
            ViewGroup.LayoutParams leftThumbParam = (LayoutParams) view.getLayoutParams();
            leftThumbParam.width = (int) width;
            view.setLayoutParams(leftThumbParam);
        }
    }

    private OnSelectorPlayTimeListener mSelectorPlayTimeListener;

    public void setOnSelectorPlayTimeListener(OnSelectorPlayTimeListener listener) {
        mSelectorPlayTimeListener = listener;
    }

    public interface OnSelectorPlayTimeListener {
        default void onUpdateTime(long playTime) {
        }

        default void onSetPlayTime(int playType, long startTime, long endTime) {
        }
    }

}
