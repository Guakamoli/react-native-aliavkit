package com.aliyun.svideo.editor.effects.captions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.editor.R;

import java.util.List;

public class CaptionLineOverlay extends LinearLayout {


    /**
     * 文字区域是否点击了
     */
    private boolean isOverlayTextOnTouch;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //文字区域未点击，判断为点击到了空白区域
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOverlayTextOnTouch) {
            //表示点击到了空白区域
            showThumb(false);
        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            isOverlayTextOnTouch = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    private OnCaptionLineOverlayClickListener mOverlayClickListener;

    public void setOnCaptionLineOverlayClickListener(OnCaptionLineOverlayClickListener listener) {
        mOverlayClickListener = listener;
    }

    public interface OnCaptionLineOverlayClickListener {
        void onSingleClick(int position, CaptionLineOverlay overlay);

        void onDoubleClick(int position, CaptionLineOverlay overlay, float offsetX);

        default void onUpdateTime(long playTime) {

        }

        void onChangeTime(int position, long startTime, long endTime, int startType);

        void onMoveOffsetX(int offsetX);
    }

    public CaptionLineOverlay(Context context) {
        super(context);
        init(context);
    }

    public CaptionLineOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CaptionLineOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Context mContext;

    private LinearLayout overlayLayout;
    private View imgLeftThumb, imgRightThumb;
    private OverlayTextView tvOverlay;

    /**
     * View 的最大长度
     */
    protected int mBaseWidth;


    /**
     * 屏幕宽度
     */
    protected long mScreenWidth;

    /**
     * 两边滑块的宽度
     */
    public int mThumbWidth;

    /**
     * 对应字幕条的下标
     */
    private int mPosition;
    /**
     * 视频的总长度
     */
    private long mDuration;
    /**
     * 最小距离
     */
    public float mMinDistance = 0.1f;   //字幕的最小宽度，即两个滑块之间的距离 (0.2s)
    public float minIntervalDistance;   //两个字幕间的间隔

    private List<CaptionLineOverlay> mCaptionLineOverlayList;


    public void initData(int position, long duration, int distance, List<CaptionLineOverlay> list) {
        this.mPosition = position;
        this.mDuration = duration;
        this.mMinDistance = this.mMinDistance * distance;
        this.mCaptionLineOverlayList = list;
    }

    protected final long mCacheIntervalTime = 1000000;
    protected int mCoverItemWidth;

    private long distance2Duration(float distance) {
        float duration = distance * mCacheIntervalTime / mCoverItemWidth;
        return Math.round(duration);
    }

    public int getOverlayLeftForEndTime() {
        LinearLayout.LayoutParams rightThumbParam = (LayoutParams) imgRightThumb.getLayoutParams();
        return mBaseWidth - rightThumbParam.getMarginEnd() - mThumbWidth * 2;
    }

    /**
     * 字体覆盖物的左边坐标，基于横向ListView 的宽度
     *
     * @return
     */
    public int getOverlayLeft() {
        LinearLayout.LayoutParams leftThumbParam = (LayoutParams) imgLeftThumb.getLayoutParams();
        return leftThumbParam.getMarginStart();
    }

    /**
     * 字幕的右边的坐标，基于横向ListView的中的尾部坐标
     *
     * @return
     */
    public int getOverlayRight() {
        LinearLayout.LayoutParams rightThumbParam = (LayoutParams) imgRightThumb.getLayoutParams();
        return mBaseWidth - rightThumbParam.getMarginEnd();
    }


    public long getMarginEnd() {
        LinearLayout.LayoutParams rightThumbParam = (LayoutParams) imgRightThumb.getLayoutParams();
        return rightThumbParam.getMarginEnd();
    }

    public float getMinDistance() {
        return mMinDistance;
    }

    public long getStartTime() {
        long startTime = distance2Duration(getOverlayLeft());
        return startTime;
    }

    public long getEndTime() {
        long endTime = distance2Duration(getOverlayRight() - mThumbWidth * 2);
        return endTime;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context) {
        mContext = context;
        mScreenWidth = ScreenUtils.getWidth(mContext);
        mCoverItemWidth = DensityUtils.dip2px(mContext, 50f);
        mThumbWidth = DensityUtils.dip2px(mContext, 20f);
        minIntervalDistance = DensityUtils.dip2px(mContext, 1f);
        View view = LayoutInflater.from(context).inflate(R.layout.paiya_caption_line_overlay, this);
        overlayLayout = view.findViewById(R.id.overlayLayout);
        imgLeftThumb = view.findViewById(R.id.imgLeftThumb);
        imgRightThumb = view.findViewById(R.id.imgRightThumb);
        tvOverlay = view.findViewById(R.id.tvOverlay);

        tvOverlay.setOnTouchClickListener(new OverlayTextView.OnTouchClickListener() {
            @Override
            public void onTouchClick(View v) {
                isOverlayTextOnTouch = true;
                if (mOverlayClickListener != null) {
                    mOverlayClickListener.onSingleClick(mPosition, CaptionLineOverlay.this);
                }
            }

            @Override
            public void onTouchDoubleClick(View view, float rawX) {
                if (mOverlayClickListener != null) {
                    mOverlayClickListener.onDoubleClick(mPosition, CaptionLineOverlay.this,  rawX - mScreenWidth/2);
                }
            }
        });

        imgLeftThumb.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (imgLeftThumb.getVisibility() == View.VISIBLE) {
                    return setLeftThumbOnTouch(imgLeftThumb, event);
                } else {
                    return true;
                }
            }
        });
        imgRightThumb.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (imgRightThumb.getVisibility() == View.VISIBLE) {
                    return setRightThumbOnTouch(imgRightThumb, event);
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
    /**
     * 点击的当前字幕 距离右边的 margin 值
     */
    private long imgRightThumbMarginEnd;
    /**
     * 上一条字幕的尾部位置
     */
    private long mLastThumbRightWidth;

    /**
     * 下一条字幕的开头位置
     */
    private long mNextThumbLeftWidth;


    private float mMoveEndOffsetX = -1;


    private boolean setRightThumbOnTouch(View view, MotionEvent event) {
        boolean isOnTouch = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.getParent().requestDisallowInterceptTouchEvent(true);
                mMoveEndOffsetX = -1;
                isOnTouch = true;
                thumbLeftX = imgLeftThumb.getX();
                thumbRightX = imgRightThumb.getX();
                mThumbTouchX = event.getRawX();
                imgRightThumbMarginEnd = ((MarginLayoutParams) imgRightThumb.getLayoutParams()).getMarginEnd();
                if (mCaptionLineOverlayList != null && mCaptionLineOverlayList.size() > mPosition + 1) {
                    CaptionLineOverlay nextOverlay = mCaptionLineOverlayList.get(mPosition + 1);
                    mNextThumbLeftWidth = nextOverlay.getOverlayLeft() + mThumbWidth;
                } else {
                    mNextThumbLeftWidth = mBaseWidth;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                isOnTouch = true;

                float rawX = event.getRawX();
                float dx = rawX - mThumbTouchX;

                float rightMargin = imgRightThumbMarginEnd - dx;
                if (rightMargin < 0) {
                    rightMargin = 0;
                }
                if (rightMargin < mBaseWidth - mNextThumbLeftWidth - mThumbWidth + minIntervalDistance) {
                    //字幕滑动到了下一条字幕的开始位置
                    return isOnTouch;
                }
                if (mBaseWidth - thumbLeftX - rightMargin - mThumbWidth * 2 < mMinDistance) {
                    //字幕条已经达到了最小宽度
                    return isOnTouch;
                }
//                if (rawX > mScreenWidth - mThumbWidth * 2.5F) {
//                    if (mMoveEndOffsetX == -1) {
//                        mMoveEndOffsetX = rawX;
//                    }
//                    //右边滑块滑动到屏幕右边的处理
//                    if (mMoveEndOffsetX >= 0 && mMoveEndOffsetX < rawX) {
//                        int marginEnd = ((MarginLayoutParams) imgRightThumb.getLayoutParams()).getMarginEnd();
//                        if (marginEnd < mThumbWidth*2) {
//                            break;
//                        }
//                        upDataViewRightMargin(marginEnd - mThumbWidth*2);
//                        for (int i = 0; i < 4; i++) {
//                            if (mOverlayClickListener != null) {
//                                mOverlayClickListener.onMoveOffsetX((int) (mThumbWidth*(0.5f+i*0.5f)));
//                            }
//                        }
//                        mMoveEndOffsetX = rawX;
//                        break;
//                    }
//                } else if (rawX < mThumbWidth * 2.5F) {
//                    if (mMoveEndOffsetX == -1) {
//                        mMoveEndOffsetX = rawX;
//                    }
//                    //右边滑块滑动到屏幕左边的处理
//                    if (mMoveEndOffsetX >= 0 && mMoveEndOffsetX > rawX) {
//                        int marginEnd = ((MarginLayoutParams) imgRightThumb.getLayoutParams()).getMarginEnd();
//                        if (marginEnd < 0) {
//                            break;
//                        }
//                        int marginStart = ((MarginLayoutParams) imgLeftThumb.getLayoutParams()).getMarginStart();
//                        if (marginEnd + marginStart > mBaseWidth - mThumbWidth * 4) {
//                            break;
//                        }
//                        upDataViewRightMargin(marginEnd + mThumbWidth);
//                        if (mOverlayClickListener != null) {
//                            mOverlayClickListener.onMoveOffsetX((int) -(mThumbWidth));
//                        }
//                        mMoveEndOffsetX = rawX;
//                        break;
//                    }
//                }
                upDataViewRightMargin(rightMargin);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.getParent().requestDisallowInterceptTouchEvent(false);
                mMoveEndOffsetX = -1;
                isOnTouch = false;
                if (mOverlayClickListener != null) {
                    mOverlayClickListener.onChangeTime(mPosition, getStartTime(), getEndTime(), 1);
                }
                break;
            default:
                break;
        }
        return isOnTouch;
    }


    private boolean setLeftThumbOnTouch(View view, MotionEvent event) {
        boolean isOnTouch = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.getParent().requestDisallowInterceptTouchEvent(true);
                isOnTouch = true;
                thumbLeftX = imgLeftThumb.getX();
                thumbRightX = imgRightThumb.getX();
                mThumbTouchX = event.getRawX();
                if (mCaptionLineOverlayList != null) {
                    if (mPosition > 0 && mCaptionLineOverlayList.size() > mPosition - 1) {
                        CaptionLineOverlay lastOverlay = mCaptionLineOverlayList.get(mPosition - 1);
                        mLastThumbRightWidth = lastOverlay.getOverlayRight() - mThumbWidth;
                    } else {
                        mLastThumbRightWidth = 0;
                    }
                } else {
                    mLastThumbRightWidth = 0;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                isOnTouch = true;

                float rawX = event.getRawX();
                float dx = rawX - mThumbTouchX;

                float leftMargin = thumbLeftX + dx;
                if (leftMargin < 0) {
                    leftMargin = 0;
                }
                if (leftMargin + mThumbWidth - minIntervalDistance <= mLastThumbRightWidth) {
                    //字幕滑动到了上一条字幕的末尾
                    return isOnTouch;
                }
                if (thumbRightX - leftMargin - mThumbWidth < mMinDistance) {
                    //字幕条到达了最小距离
                    return isOnTouch;
                }


//                if (rawX > mScreenWidth - mThumbWidth * 2.5F) {
//                    //左边边滑块滑动到屏幕右边的处理 //继续向右滑动中
//                    if (mMoveEndOffsetX >= 0 && mMoveEndOffsetX < rawX) {
//                        int marginStart = ((MarginLayoutParams) imgLeftThumb.getLayoutParams()).getMarginStart();
//                        if (marginStart < 0) {
//                            break;
//                        }
//                        int marginEnd = ((MarginLayoutParams) imgRightThumb.getLayoutParams()).getMarginEnd();
//                        if (marginEnd + marginStart > mBaseWidth - mThumbWidth * 3) {
//                            break;
//                        }
//                        upDataViewLeftMargin(marginStart + mThumbWidth);
//                        if (mOverlayClickListener != null) {
//                            mOverlayClickListener.onMoveOffsetX((int) (mThumbWidth));
//                        }
//                        mMoveEndOffsetX = rawX;
//                        break;
//                    }
//                    mMoveEndOffsetX = rawX;
//                } else if (rawX < mThumbWidth * 2.5F) {
//                    //左边滑块滑动到屏幕左边的处理，  //向左继续移动
//                    if (mMoveEndOffsetX >= 0 && mMoveEndOffsetX > rawX) {
//                        int marginStart = ((MarginLayoutParams) imgLeftThumb.getLayoutParams()).getMarginStart();
//                        if (marginStart < mThumbWidth) {
//                            break;
//                        }
//                        upDataViewLeftMargin(marginStart - mThumbWidth);
//                        if (mOverlayClickListener != null) {
//                            mOverlayClickListener.onMoveOffsetX((int) -(mThumbWidth));
//                        }
//                        mMoveEndOffsetX = rawX;
//                        break;
//                    }
//                    mMoveEndOffsetX = rawX;
//                }
                upDataViewLeftMargin(leftMargin);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.getParent().requestDisallowInterceptTouchEvent(false);
                isOnTouch = false;
                if (mOverlayClickListener != null) {
                    mOverlayClickListener.onChangeTime(mPosition, getStartTime(), getEndTime(), 0);
                }
                break;
            default:
                break;
        }
        return isOnTouch;
    }

    private void upDataViewLeftMargin(float leftMargin) {
        if (leftMargin >= 0) {
            LinearLayout.LayoutParams leftThumbParam = (LayoutParams) imgLeftThumb.getLayoutParams();
            leftThumbParam.setMarginStart((int) leftMargin);
            imgLeftThumb.setLayoutParams(leftThumbParam);
        }

        if (mOverlayClickListener != null) {
            mOverlayClickListener.onUpdateTime(getStartTime());
        }
    }

    private void upDataViewRightMargin(float rightMargin) {
        if (rightMargin >= 0) {
            LinearLayout.LayoutParams leftThumbParam = (LayoutParams) imgRightThumb.getLayoutParams();
            leftThumbParam.setMarginEnd((int) rightMargin);
            imgRightThumb.setLayoutParams(leftThumbParam);
        }
        if (mOverlayClickListener != null) {
            mOverlayClickListener.onUpdateTime(getEndTime());
        }
    }


    private boolean isShowThumb;

    public boolean isShowThumb() {
        return isShowThumb;
    }

    public void showThumb(boolean isShow) {
        isShowThumb = isShow;
        if (isShow) {
            imgLeftThumb.setVisibility(VISIBLE);
            imgRightThumb.setVisibility(VISIBLE);
            overlayLayout.setBackgroundColor(Color.WHITE);
        } else {
            imgLeftThumb.setVisibility(INVISIBLE);
            imgRightThumb.setVisibility(INVISIBLE);
            overlayLayout.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * @param startWidth view 展示的起点位置，MarginStart 的值
     * @param distance   view 展示的总长度
     * @param blessing   文案
     * @param width      总长度，为整个列表的最大长度
     * @return
     */
    public CaptionLineOverlay getOverlayViewByDuration(int startWidth, int distance, String blessing, int width) {
        setBlessingChanger(blessing);
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
        layoutParams.width = width;
        setLayoutParams(layoutParams);
        mBaseWidth = width;

        LinearLayout.LayoutParams leftThumbParam = (LayoutParams) imgLeftThumb.getLayoutParams();
        if (startWidth < 0) {
            startWidth = 0;
        }

        leftThumbParam.setMarginStart(startWidth);
        imgLeftThumb.setLayoutParams(leftThumbParam);

        LinearLayout.LayoutParams rightThumbParam = (LayoutParams) imgRightThumb.getLayoutParams();
        int marginEnd = width - distance - startWidth;
        if (marginEnd < 0) {
            marginEnd = 0;
        }
        rightThumbParam.setMarginEnd(marginEnd);
        imgRightThumb.setLayoutParams(rightThumbParam);

        return this;
    }

    public void setBlessingChanger(String text) {
        if (tvOverlay != null) {
            text = text.replaceAll("\r|\n", "");
            tvOverlay.setText(text);
        }
    }

}
