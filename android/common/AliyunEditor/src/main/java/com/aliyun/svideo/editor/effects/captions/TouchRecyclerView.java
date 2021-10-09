package com.aliyun.svideo.editor.effects.captions;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.DensityUtils;

import org.jetbrains.annotations.NotNull;

public class TouchRecyclerView extends RecyclerView {

    public TouchRecyclerView(@NonNull @NotNull Context context) {
        super(context);
        init(context);
    }

    public TouchRecyclerView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchRecyclerView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private GestureDetector mGestureDetector;

    private void init(Context context) {
////        mScaledSlop = DensityUtils.dip2px(context, 3f);
//        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onSingleTapUp(MotionEvent e) {
//                if (mTouchClickListener != null) {
//                    mTouchClickListener.onSingleClick(TouchRecyclerView.this);
//                }
//                Log.e("AAA", "onSingleTapUp");
//                return super.onSingleTapUp(e);
//            }
//
//            @Override
//            public boolean onSingleTapConfirmed(MotionEvent e) {
//                Log.e("AAA", "onSingleTapConfirmed");
//                return super.onSingleTapConfirmed(e);
//            }
//        });
    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (ev.getAction() != MotionEvent.ACTION_MOVE) {
//            requestDisallowInterceptTouchEvent(true);
//            return super.onInterceptTouchEvent(ev);
//        }else {
//            requestDisallowInterceptTouchEvent(false);
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event){
//        //这个方法如果返回 true 的话 两个手指移动，启动一个按下的手指的移动不能被传播出去。
//        super.onInterceptTouchEvent(event);
//        return false;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        Log.e("AAA","onTouchEvent 111");
//        return super.onTouchEvent(event);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        mGestureDetector.onTouchEvent(event);
//        return  super.onTouchEvent(event);
//    }


    private long mDownTime;
    private boolean mHasMove;

    /**
     * 安全滑动距离
     */
    private int mScaledSlop = 9;
    private float mRawX, mRawY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownTime = SystemClock.elapsedRealtime();
                mRawX = ev.getRawX();
                mRawY = ev.getRawY();
                mHasMove = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = ev.getRawX() - mRawX;
                float distanceY = ev.getRawY() - mRawY;
                if (mHasMove == false && Math.abs(distanceX) <= mScaledSlop && Math.abs(distanceY) <= mScaledSlop) {

                } else {
                    mHasMove = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!mHasMove && SystemClock.elapsedRealtime() - mDownTime < 500) {
                    if (mTouchClickListener != null) {
                        mTouchClickListener.onSingleClick(this);
                    }
                }
                mHasMove = false;
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private OnTouchClickListener mTouchClickListener;

    public void setOnTouchClickListener(OnTouchClickListener listener) {
        mTouchClickListener = listener;
    }

    public interface OnTouchClickListener {
        void onSingleClick(RecyclerView view);
    }


}
