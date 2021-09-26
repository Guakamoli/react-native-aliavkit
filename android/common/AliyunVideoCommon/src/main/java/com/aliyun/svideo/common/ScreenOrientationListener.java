package com.aliyun.svideo.common;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.common.widget.AutocueLayoutView;
import com.blankj.utilcode.util.ToastUtils;

import java.util.List;

public class ScreenOrientationListener extends OrientationEventListener {

    private Context mContext;
    private int mOrientation;

    private int mScreenWidth;
    /**
     * 小米手机获取屏幕高度，就算隐藏了虚拟机，高度还是减去了顶部状态栏和底部的虚拟导航栏，获取的高度少了底部虚拟导航栏的高度。
     * <p>
     * 如果是小米手机，并且隐藏了NavigationBar，就在获取到的高度基础上加上NavigationBar的高度
     */
    private int mScreenHeight;


    private int mTextMaxHeightLand, mTextMaxHeightPort;

    private int mViewHeight;

    /**
     * 顶部状态栏
     */
    private int mStatusBarHeight;


    public ScreenOrientationListener(Context context, int textLeftPadding, FrameLayout aliyunRecordBtn) {
        super(context);
        mContext = context;

        mScreenWidth = ScreenUtils.getWidth(mContext);
        mScreenHeight = ScreenUtils.getHeight(mContext);

//        mTextMaxHeightLand = DensityUtils.dip2px(mContext, 500);
//        mTextMaxHeightPort = DensityUtils.dip2px(mContext, 250);
        mTextMaxHeightLand = mScreenHeight * 9 / 20;
        mTextMaxHeightPort = mScreenWidth * 9 / 20;

//        aliyunRecordBtn.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                aliyunRecordBtn.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                int playBtnWidth = aliyunRecordBtn.getMeasuredWidth();
//                mTextMaxHeightPort = (mScreenWidth - playBtnWidth) / 2 - DensityUtils.dip2px(mContext, 10);
//            }
//        });


        if (RomUtils.isXiaomi()) {
            boolean isHideNavigationBar = Settings.Global.getInt(context.getContentResolver(), "force_fsg_nav_bar", 0) != 0;
            if (isHideNavigationBar) {
                //底部导航栏
                int mNavigationHeight = ScreenUtils.getNavigationHeight(mContext);
                mScreenHeight = mScreenHeight + mNavigationHeight;
            }
        }
        mStatusBarHeight = ScreenUtils.getStatusBarHeight(mContext);

    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (!isTitleViewAdded || !isContentViewAdded || isPause) {
            return;
        }
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            //手机平放时，检测不到有效的角度
            orientation = 0;
            return;
        } else {
            //只检测是否有四个角度的改变
            if (orientation > 335 || orientation < 25) { //0度
                orientation = 0;
            } else if (orientation > 80 && orientation < 100) { //90度
                orientation = 90;
            } else if (orientation > 170 && orientation < 190) { //180度
//                orientation = 180;
                orientation = 0;
            } else if (orientation > 260 && orientation < 280) { //270度
                orientation = 270;
            } else {
                return;
            }
        }
        if (this.mOrientation == orientation) {
            return;
        }

        if (orientation == 90 || orientation == 270) {
//            ToastUtils.showShort("竖屏拍摄观感更佳");
            ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER, 0, 0).show(mContext.getResources().getString(R.string.alivc_recorder_landscape_hint));
            ToastUtils.getDefaultMaker().setGravity(-1, -1, -1);
        }

        setBackViewRotateAnim(orientation);
        startContentView(orientation);
        valueRotateAnim(orientation);
        this.mOrientation = orientation;

    }


    private boolean isPause;

    public void onResume() {
        isPause = false;
    }

    public void onPause() {
        onOrientationChanged(0);
        isPause = true;
    }


    /**
     * 设置View 的旋转方向
     */
    private void startRotateAnim(View view, float formAngle, float toAngle) {
        ObjectAnimator animRotate = ObjectAnimator.ofFloat(view, "rotation", formAngle, toAngle);
        animRotate.setDuration(250);
        animRotate.start();
    }

    private List<View> mViewList;

    private View mBackView;

    private AutocueLayoutView mContentView;
    private TextView mTvTitle, mTvContent;

    public void setAngleViews(List<View> views) {
        this.mViewList = views;
    }

    public void setBackView(View views) {
        this.mBackView = views;
    }

    private int mPaddingStartCount, mPaddingTopCount, mPaddingEndCount, mPaddingBottomCount;
    private int mPaddingStart, mPaddingTop, mPaddingEnd, mPaddingBottom;


    private int mPaddingStartCountTitle, mPaddingTopCountTitle, mPaddingEndCountTitle, mPaddingBottomCountTitle;
    private int mPaddingStartTitle, mPaddingTopTitle, mPaddingEndTitle, mPaddingBottomTitle;

    private String mStrContent;


    private float mTitleTextWidth, mTitleTextWidthCount;

    public float getTextWidth(Context context, String text, int textSize) {
        TextPaint paint = new TextPaint();
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        paint.setTextSize(scaledDensity * textSize);
        return paint.measureText(text + " 　");
    }


    private boolean isTitleViewAdded, isContentViewAdded;

    public void setContentView(AutocueLayoutView views, TextView tvTitle, TextView textView) {
        this.mContentView = views;
        this.mTvTitle = tvTitle;
        this.mTvContent = textView;
        mTitleTextSize = 24F;//sp
        mStrContent = mTvContent.getText().toString();

        mTvTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTvTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                isTitleViewAdded = true;
                mPaddingStartCountTitle = mPaddingStartTitle = mTvTitle.getPaddingStart();
                mPaddingTopCountTitle = mPaddingTopTitle = mTvTitle.getPaddingTop();
                mPaddingEndCountTitle = mPaddingEndTitle = mTvTitle.getPaddingEnd();
                mPaddingBottomCountTitle = mPaddingBottomTitle = mTvTitle.getPaddingBottom();
                mTitleTextWidthCount = getTextWidth(mContext, mTvTitle.getText().toString(), 21);
            }
        });
        mTvContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTvContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                isContentViewAdded = true;
                mPaddingStartCount = mPaddingStart = mTvContent.getPaddingStart();
                mPaddingTopCount = mPaddingTop = mTvContent.getPaddingTop();
                mPaddingEndCount = mPaddingEnd = mTvContent.getPaddingEnd();
                mPaddingBottomCount = mPaddingBottom = mTvContent.getPaddingBottom();
            }
        });
    }

    /**
     * 跟随手机4个方向的旋转
     */
    private void valueRotateAnim(int angle) {
        int formAngle = 360 - mOrientation;
        int toAngle = 360 - angle;
        //特别处理从0-270度的旋转
        if (mOrientation == 0 && angle == 270) {
            formAngle = 0;
            toAngle = 90;
        }
        //特别处理从270-0度的旋转
        if (mOrientation == 270 && angle == 0) {
            formAngle = 90;
            toAngle = 0;
        }
        if (mViewList != null) {
            for (int i = 0; i < mViewList.size(); i++) {
                startRotateAnim(mViewList.get(i), formAngle, toAngle);
            }
        }
    }


    /**
     * 返回键只有两个方向，仅有横竖屏两种状态
     */
    public void setBackViewRotateAnim(int orientation) {
        if (mBackView != null) {
            int toAngle = orientation >= 180 ? orientation - 180 : orientation;
            int formAngle = mOrientation >= 180 ? mOrientation - 180 : mOrientation;
            startRotateAnim(mBackView, formAngle, toAngle);
        }
    }


    private int mViewWidth;

    private float mViewX, mViewY, mViewRotation;

    private float mTitleTextSize;


    /**
     * 置顶的提词器内容
     * 动画包括View的起始结束的： X、Y 坐标；旋转角度；View的宽度
     */
    @SuppressLint("ObjectAnimatorBinding")
    public void startContentView(int orientation) {
        mViewRotation = 360 - mOrientation;
        int endRotation = 360 - orientation;
        //特别处理从0-270度的旋转
        if (mOrientation == 0 && orientation == 270) {
            mViewRotation = 0;
            endRotation = 90;
        }
        //特别处理从270-0度的旋转
        if (mOrientation == 270 && orientation == 0) {
            mViewRotation = 90;
            endRotation = 0;
        }
        int endX = 0;
        int endY = 0;
        int endViewWidth = 0;
        int paddingStart = 0, paddingTop = 0, paddingBottom = 0, paddingEnd = 0;
        int paddingStartTitle = 0, paddingTopTitle = 0, paddingBottomTitle = 0, paddingEndTitle = 0;
        float titleTextSize = 0;
        float contentTextMarginStart = 0;
        float quadCoe = 0.2f;
        if (orientation == 0) {
            mViewHeight = mTextMaxHeightLand;
            endX = 0;
            endY = 0;
            endViewWidth = mScreenWidth;
            quadCoe = 1 - quadCoe;

            contentTextMarginStart = 0;
            paddingStart = mPaddingStartCount;
            paddingTop = mPaddingTopCount;
            paddingEnd = mPaddingEndCount;
            paddingBottom = mPaddingBottomCount;

            titleTextSize = 24F;
            paddingStartTitle = mPaddingStartCountTitle;
            paddingTopTitle = mPaddingTopCountTitle;
            paddingEndTitle = mPaddingEndCountTitle;
            paddingBottomTitle = mPaddingBottomCountTitle;

        } else if (orientation == 90) {
            mViewHeight = mTextMaxHeightPort;
            endX = -(mScreenHeight - mViewHeight) / 2;
            endY = (mScreenHeight - mViewHeight) / 2;
            endViewWidth = mScreenHeight;

            contentTextMarginStart = mTitleTextWidthCount;
            titleTextSize = 21F;
            paddingStart = DensityUtils.dip2px(mContext, 90);
            paddingTop = DensityUtils.dip2px(mContext, 16);
            paddingEnd = DensityUtils.dip2px(mContext, 66);
            paddingBottom = DensityUtils.dip2px(mContext, 16);

            paddingStartTitle = paddingStart;
            paddingTopTitle = paddingTop;
            paddingEndTitle = paddingEnd;
            paddingBottomTitle = paddingBottom;

        } else if (orientation == 180) {
            mViewHeight = mTextMaxHeightLand;
            endX = 0;
            endY = 0;
            endViewWidth = mScreenWidth;
            quadCoe = 1 - quadCoe;

            contentTextMarginStart = 0;
            paddingStart = mPaddingStartCount;
            paddingTop = mPaddingTopCount;
            paddingEnd = mPaddingEndCount;
            paddingBottom = mPaddingBottomCount;

            titleTextSize = 24F;
            paddingStartTitle = mPaddingStartCountTitle;
            paddingTopTitle = mPaddingTopCountTitle;
            paddingEndTitle = mPaddingEndCountTitle;
            paddingBottomTitle = mPaddingBottomCountTitle;

        } else if (orientation == 270) {
            mViewHeight = mTextMaxHeightPort;
            endViewWidth = mScreenHeight;
            endX = -(mScreenHeight - mViewHeight) / 2 + (mScreenWidth - mViewHeight);
            endY = (mScreenHeight - mViewHeight) / 2;

            contentTextMarginStart = mTitleTextWidthCount;
            paddingStart = DensityUtils.dip2px(mContext, 66);
            paddingTop = DensityUtils.dip2px(mContext, 16);
            paddingEnd = DensityUtils.dip2px(mContext, 90);
            paddingBottom = DensityUtils.dip2px(mContext, 16);

            titleTextSize = 21F;
            paddingStartTitle = paddingStart;
            paddingTopTitle = paddingTop;
            paddingEndTitle = paddingEnd;
            paddingBottomTitle = paddingBottom;
        }

        mContentView.setLayoutHeight(mViewHeight);

        Path path = new Path();
        path.moveTo(mViewX, mViewY);
        //二阶贝塞尔
        path.quadTo(mViewX + (endX - mViewX) * (1 - quadCoe), mViewY + (endY - mViewY) * quadCoe, endX, endY);
        PathMeasure mPathMeasure = new PathMeasure(path, false);
        //贝塞尔曲线移动xy
        PropertyValuesHolder bezierHolder = PropertyValuesHolder.ofFloat("bezierValue", 0f, mPathMeasure.getLength());
        // 透明度
        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 1f);
        // 旋转
        PropertyValuesHolder rotationHolder = PropertyValuesHolder.ofFloat("rotation", mViewRotation, endRotation);
        //改变控件的宽高
        PropertyValuesHolder layoutWidthHolder = PropertyValuesHolder.ofInt("layoutWidth", mViewWidth, endViewWidth);

        //内容文字首行缩进
        PropertyValuesHolder contentTextMarginStartHolder = PropertyValuesHolder.ofFloat("contentTextMarginStart", mTitleTextWidth, contentTextMarginStart);
        //内容文字缩进
        PropertyValuesHolder paddingStartHolder = PropertyValuesHolder.ofInt("paddingStart", mPaddingStart, paddingStart);
        PropertyValuesHolder paddingTopHolder = PropertyValuesHolder.ofInt("paddingTop", mPaddingTop, paddingTop);
        PropertyValuesHolder paddingEndHolder = PropertyValuesHolder.ofInt("paddingEnd", mPaddingEnd, paddingEnd);
        PropertyValuesHolder paddingBottomHolder = PropertyValuesHolder.ofInt("paddingBottom", mPaddingBottom, paddingBottom);

        //标题文字大小改变
        PropertyValuesHolder titleTextSizeHolder = PropertyValuesHolder.ofFloat("titleTextSize", mTitleTextSize, titleTextSize);
        //标题文字缩进
        PropertyValuesHolder paddingStartHolderTitle = PropertyValuesHolder.ofInt("paddingStartTitle", mPaddingStartTitle, paddingStartTitle);
        PropertyValuesHolder paddingTopHolderTitle = PropertyValuesHolder.ofInt("paddingTopTitle", mPaddingTopTitle, paddingTopTitle);
        PropertyValuesHolder paddingEndHolderTitle = PropertyValuesHolder.ofInt("paddingEndTitle", mPaddingEndTitle, paddingEndTitle);
        PropertyValuesHolder paddingBottomHolderTitle = PropertyValuesHolder.ofInt("paddingBottomTitle", mPaddingBottomTitle, paddingBottomTitle);

        int animatedDuration = 250;
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mContentView, alphaHolder, rotationHolder, bezierHolder, layoutWidthHolder,
                paddingStartHolder, paddingTopHolder, paddingEndHolder, paddingBottomHolder, titleTextSizeHolder, contentTextMarginStartHolder,
                paddingStartHolderTitle, paddingTopHolderTitle, paddingEndHolderTitle, paddingBottomHolderTitle
        );
        animator.setDuration(animatedDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                int layoutWidthValue = (int) animation.getAnimatedValue("layoutWidth");
//                mContentView.setLayoutWidth(layoutWidthValue);

                int paddingStartValue = (int) animation.getAnimatedValue("paddingStart");
                int paddingTopValue = (int) animation.getAnimatedValue("paddingTop");
                int paddingEndValue = (int) animation.getAnimatedValue("paddingEnd");
                int paddingBottomValue = (int) animation.getAnimatedValue("paddingBottom");
                mTvContent.setPadding(paddingStartValue, paddingTopValue, paddingEndValue, paddingBottomValue);

                int paddingStartValueTitle = (int) animation.getAnimatedValue("paddingStartTitle");
                int paddingTopValueTitle = (int) animation.getAnimatedValue("paddingTopTitle");
                int paddingEndValueTitle = (int) animation.getAnimatedValue("paddingEndTitle");
                int paddingBottomValueTitle = (int) animation.getAnimatedValue("paddingBottomTitle");
                mTvTitle.setPadding(paddingStartValueTitle, paddingTopValueTitle, paddingEndValueTitle, paddingBottomValueTitle);
                float titleTextSize = (float) animation.getAnimatedValue("titleTextSize");
                mTvTitle.setTextSize(titleTextSize);

                float bezierValue = (float) animation.getAnimatedValue("bezierValue");
                mContentView.setBezierValue((int) bezierValue, mPathMeasure);


                float contentTextMarginStart = (float) animation.getAnimatedValue("contentTextMarginStart");
                SpannableString mSpannableString = new SpannableString(mStrContent);
                // 设置文本缩进的样式，参数arg0，首行缩进的像素，arg1，剩余行缩进的像素
                LeadingMarginSpan.Standard standard = new LeadingMarginSpan.Standard((int) contentTextMarginStart, 0);
                //其中参数what是具体样式的实现对象,start 该样式开始的位置，end 样式结束的位置，参数flags，定义在Spannable中的常量
                mSpannableString.setSpan(standard, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                mTvContent.setText(mSpannableString);

            }
        });
        animator.start();

        mViewRotation = endRotation;
        mViewX = endX;
        mViewY = endY;
        mViewWidth = endViewWidth;
        mPaddingStart = paddingStart;
        mPaddingTop = paddingTop;
        mPaddingEnd = paddingEnd;
        mPaddingBottom = paddingBottom;
        mPaddingStartTitle = paddingStartTitle;
        mPaddingTopTitle = paddingTopTitle;
        mPaddingEndTitle = paddingEndTitle;
        mPaddingBottomTitle = paddingBottomTitle;

        mTitleTextSize = titleTextSize;
        mTitleTextWidth = contentTextMarginStart;
    }


}
