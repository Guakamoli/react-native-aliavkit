package com.aliyun.svideo.editor.effects.captions;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.aliyun.svideo.common.utils.DensityUtils;

public class OverlayTextView extends androidx.appcompat.widget.AppCompatTextView {

    public OverlayTextView(Context context) {
        super(context);
        init(context);
    }

    public OverlayTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OverlayTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Context mContext;

//    private GestureDetector mGestureDetector;
//    private int mScaledSlop = 9;
//    private float mRawX, mRawY;

    private void init(Context context) {
        this.mContext = context;
//        mScaledSlop = DensityUtils.dip2px(context, 3f);
//        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onSingleTapUp(MotionEvent e) {
//                if (mTouchClickListener != null) {
//                    mTouchClickListener.onTouchClick(OverlayTextView.this);
//                }
//                return super.onSingleTapUp(e);
//            }
//
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                if (mTouchClickListener != null) {
//                    mTouchClickListener.onTouchDoubleClick(OverlayTextView.this);
//                }
//                return super.onDoubleTap(e);
//            }
//        });
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return mGestureDetector.onTouchEvent(event);
//    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        mGestureDetector.onTouchEvent(event);
//        return super.dispatchTouchEvent(event);
//    }

//    /**
//     * onTouchEvent 返回 true
//     * 子View 加载到 RecyclerView 或者  ScrollView
//     * 需要处理 父布局的 onInterceptTouchEvent 返回 false
//     * 否则一旦滑动后，当前View onTouchEvent 将不会再触发
//     *
//     * @return
//     */
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        mGestureDetector.onTouchEvent(ev);
////        switch (ev.getAction()) {
////            case MotionEvent.ACTION_DOWN:
////                mRawX = ev.getRawX();
////                mRawY = ev.getRawY();
////                break;
////            case MotionEvent.ACTION_MOVE:
////                float distanceX = ev.getRawX() - mRawX;
////                float distanceY = ev.getRawY() - mRawY;
////                if ( Math.abs(distanceX) <= mScaledSlop && Math.abs(distanceY) <= mScaledSlop) {
////                    return true;
////                } else {
////                    return false;
////                }
//////                break;
////            case MotionEvent.ACTION_UP:
////            case MotionEvent.ACTION_CANCEL:
////                return false;
//////                break;
////            default:
////                break;
////        }
//
//        return true;
//    }

    private long downTime;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
//        mGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mTouchClickListener != null) {
                mTouchClickListener.onTouchClick(this);
                if (SystemClock.elapsedRealtime() - downTime < 500) {
//                    float x = event.getX();
                    float rawX = event.getRawX();
//                    float left = getLeft();
//                    Log.e("AAA", "rawX：" + rawX );
                    mTouchClickListener.onTouchDoubleClick(this,rawX);
                    downTime = 0;
                }
            }
            downTime = SystemClock.elapsedRealtime();
        }
        return super.dispatchTouchEvent(event);
    }

    private OnTouchClickListener mTouchClickListener;

    public void setOnTouchClickListener(OnTouchClickListener listener) {
        mTouchClickListener = listener;
    }

    public interface OnTouchClickListener {
        void onTouchClick(View view);

        void onTouchDoubleClick(View view,float rawX );
    }

}
