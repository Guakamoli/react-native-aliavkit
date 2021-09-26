package com.aliyun.svideo.recorder.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

public class RotateTextView extends AppCompatTextView {
    /**
     * 文本旋转的角度
     */
    private int mDegrees = 270;

    public RotateTextView(Context context) {
        this(context, null);
    }

    public RotateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        this.setGravity(Gravity.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        canvas.rotate(mDegrees, this.getWidth() / 2f, this.getHeight() / 2f);
        super.onDraw(canvas);
        canvas.restore();
    }

    /**
     * 改变文本选择的角度
     *
     * @param degrees 文本旋转的角度
     */
    public void setDegrees(int degrees) {
        mDegrees = degrees;
        invalidate();
    }

}
