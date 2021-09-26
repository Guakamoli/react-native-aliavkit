package com.aliyun.svideo.common.widget;

import android.content.Context;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * 提词器布局Layout
 */
import androidx.annotation.Nullable;

public class AutocueLayoutView extends FrameLayout {

    public AutocueLayoutView(Context context) {
        super(context);
    }

    public AutocueLayoutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutocueLayoutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AutocueLayoutView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
//        mPaddingStart = getPaddingStart();
//        mPaddingTop = getPaddingTop();
//        mPaddingEnd = getPaddingEnd();
//        mPaddingBottom = getPaddingBottom();
    }


//    private int mPaddingStart, mPaddingTop, mPaddingEnd, mPaddingBottom;
//
//    public void setPaddingStartEnd(int padding) {
//        setPadding(mPaddingStart + padding, mPaddingTop, mPaddingEnd + padding, mPaddingBottom);
//    }

    public  int layoutWidth;

    public int getLayoutWidth() {
        return layoutWidth;
    }

    public void setLayoutWidth(int layoutWidth) {
        this.layoutWidth = layoutWidth;
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        setLayoutParams(params);
    }

    public void setLayoutHeight(int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = height;
        setLayoutParams(params);
    }

    private float[] mCurrentPosition = new float[2];

    public void setBezierValue(int bezierValue ,PathMeasure mPathMeasure){
        if(mPathMeasure!=null){
            //mCurrentPosition此时就是中间距离点的坐标值
            mPathMeasure.getPosTan(bezierValue, mCurrentPosition, null);
            setTranslationX(mCurrentPosition[0]);
            setTranslationY(mCurrentPosition[1]);
        }
    }

}
