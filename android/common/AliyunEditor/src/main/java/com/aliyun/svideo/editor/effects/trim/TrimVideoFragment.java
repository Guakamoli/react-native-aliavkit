package com.aliyun.svideo.editor.effects.trim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.captions.adapter.CaptionThumbnailAdapter;
import com.aliyun.svideo.editor.effects.captions.adapter.CaptionTimelineAdapter;
import com.aliyun.svideo.editor.effects.captions.TouchRecyclerView;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.publish.paiya.CoverInfo;
import com.aliyun.svideo.editor.publish.paiya.ThumbnailFetcherManage;
import com.aliyun.svideo.editor.view.AlivcEditView;
import com.aliyun.svideo.editor.view.EditorVideHelper;
import com.aliyun.svideosdk.editor.AliyunIEditor;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrimVideoFragment extends BaseChooser {

    protected Context mContext;
    protected int mScreenWidth;
    protected int mCoverItemWidth, mCoverItemHeight;
    protected int mTimelineLeft;
    protected int mOverlayThumbWidth;

    private long mCountStartTime, mCountEndTime;
    private long mStartTime, mEndTime;

    public TrimVideoFragment(@NonNull @NotNull Context context) {
        super(context);
        mTimelineLeft = DensityUtils.dip2px(context, 15f);
        mOverlayThumbWidth = DensityUtils.dip2px(context, 20f);
        mCoverItemWidth = DensityUtils.dip2px(context, 50f);
        mCoverItemHeight = DensityUtils.dip2px(context, 50f);
        mScreenWidth = ScreenUtils.getWidth(context);
        if (mHandler == null) {
            mHandler = new TrimVideoFragment.StaticHandler(this);
        }
        initView(context);
    }

    protected AlivcEditView mAlivcEditView;
    protected AliyunIEditor mAliyunIEditor;

    public void setBaseData(AlivcEditView rootView, AliyunIEditor iEditor) {
        mAlivcEditView = rootView;
        mAliyunIEditor = iEditor;
    }

    /**
     * 当前View是否展示中
     */
    protected boolean isShowPage = true;

    private final List<CoverInfo> mBitmapList = new ArrayList<>();

    private TouchRecyclerView mTimelineRecyclerView;
    private TouchRecyclerView mThumbnailRecyclerView;
    private TouchRecyclerView mOverlayRecyclerView;

    private ThumbnailFetcherManage mThumbnailFetcherManage;

    private long mDuration;

    /**
     * 缩略图数量
     */
    private int mCacheSize;
    private double mEndIntervalWidth;

    /**
     * 缩略图间隔时间 1S（us）
     */
    private final static long mCacheIntervalTime = 1000000;

    /**
     * 判断列表是否被触摸
     */
    protected boolean isRvTouching;
    /**
     * 判断列表是否停止滚动，停止滚动后，才去重置触摸状态
     */
    protected int isRvNewState;

    private void initView(Context context) {
        this.mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.paiya_base_trim_linebar, this);
        initTitleView(view);
        initTimelineRecyclerView(view);
        initThumbnailRecyclerView(view);
        initOverlayRecyclerView(view);
        mThumbnailRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isRvNewState = newState;
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (isRvTouching) {
                        isRvTouching = false;
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTimelineRecyclerView.scrollBy(dx, dy);
                mOverlayRecyclerView.scrollBy(dx, dy);
                if (isRvTouching) {
                    setPlaySeek(dx);
                }
            }
        });
    }

    public void initData(Uri uri) {
        if (mPlayerListener != null) {
            mDuration = mPlayerListener.getDuration();
            if (mEndTime == 0) {
                mCountEndTime = mEndTime = mDuration;
            }
        }

        mThumbnailFetcherManage = new ThumbnailFetcherManage(uri, mCoverItemWidth / 2, mCoverItemHeight / 2, 10);
        mThumbnailFetcherManage.getCoverThumbnailList(new ThumbnailFetcherManage.OnCoverThumbnailCallback() {
            @Override
            public void onStart(int cacheSize) {
                mCacheSize = cacheSize;
                mBitmapList.clear();
                mBitmapList.addAll(Arrays.asList(new CoverInfo[cacheSize]));
            }

            @Override
            public void onEndWidth(double endWidth) {
                mEndIntervalWidth = endWidth;
            }

            @Override
            public void onNext(CoverInfo coverInfo) {
                if (coverInfo != null) {
                    mBitmapList.set(coverInfo.getPosition(), coverInfo);
                    fillThumbnailAdapter(coverInfo.getPosition());
                }
            }
        }, 0, mDuration, mCacheIntervalTime);

        fillTimelineAdapter();
        fillThumbnailAdapter(0);
        setPlayScroll();
        fillOverlayAdapter();
    }

    public void showPage() {
        isShowPage = true;
        long playTime = 0;
        if (mPlayerListener != null) {
            playTime = mAliyunIEditor.getCurrentStreamPosition();
        }
        EditorVideHelper.resetVideoTimes(mAliyunIEditor, 0, mDuration);
        mAlivcEditView.playingResume();
        mAlivcEditView.playingPause();
        //初始化播放位置
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setRvScroll(mStartTime, true);
                setPlaySeek(mStartTime);
            }
        }, 100);

        mAlivcEditView.setPlayCallback(new AlivcEditView.OnPlayingCallback() {
            @Override
            public void onPlaying(long currentPlayTime, long currentStreamPlayTime) {
                if (!mAliyunIEditor.isPlaying()) {
                    return;
                }
                if ((mEndTime > 0 && currentPlayTime >= mEndTime) || (mStartTime > 0 && currentPlayTime < mStartTime)) {
                    mHandler.sendEmptyMessage(CODE_WHAT_RE_START);
                }
            }
        });
    }

    /**
     * 时间轴
     */
    private void initTimelineRecyclerView(View view) {
        mTimelineRecyclerView = view.findViewById(R.id.timelineRecyclerView);
        mTimelineRecyclerView.setPadding(mScreenWidth / 2 - mTimelineLeft, 0, mScreenWidth / 2 - (mCoverItemWidth - mTimelineLeft), 0);
        mTimelineRecyclerView.setClipToPadding(false);
    }

    private CaptionTimelineAdapter mCaptionTimelineAdapter;

    private void fillTimelineAdapter() {
        if (mCaptionTimelineAdapter == null) {
            mCaptionTimelineAdapter = new CaptionTimelineAdapter(mContext, mBitmapList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            mTimelineRecyclerView.setLayoutManager(layoutManager);
            mTimelineRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mTimelineRecyclerView.setAdapter(mCaptionTimelineAdapter);
        } else {
            mCaptionTimelineAdapter.notifyDataSetChanged();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initThumbnailRecyclerView(View view) {
        mThumbnailRecyclerView = view.findViewById(R.id.thumbRecyclerView);
        mThumbnailRecyclerView.setPadding(mScreenWidth / 2, mThumbnailRecyclerView.getPaddingTop(), mScreenWidth / 2, 0);
        mThumbnailRecyclerView.setClipToPadding(false);
        mThumbnailRecyclerView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                setRecyclerViewOnTouchEvent(event);
                return false;
            }
        });
    }

    private CaptionThumbnailAdapter mThumbnailAdapter;

    private void fillThumbnailAdapter(Integer position) {
        if (mThumbnailAdapter == null) {
            mThumbnailAdapter = new CaptionThumbnailAdapter(mContext, mBitmapList, mCoverItemWidth, mCoverItemHeight, mScreenWidth,mEndIntervalWidth);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            mThumbnailRecyclerView.setLayoutManager(layoutManager);
            mThumbnailRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mThumbnailRecyclerView.setAdapter(mThumbnailAdapter);
        } else {
            if (position != null) {
                int index = position;
                if (index >= 0 && index < mBitmapList.size()) {
                    mThumbnailAdapter.notifyItemChanged(index);
                    return;
                }
            }
            mThumbnailAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOverlayRecyclerView(View view) {
        mOverlayRecyclerView = view.findViewById(R.id.overlayRecyclerView);
        mOverlayRecyclerView.setPadding(mScreenWidth / 2 - mOverlayThumbWidth, mOverlayRecyclerView.getPaddingTop(), mScreenWidth / 2 - mOverlayThumbWidth, 0);
        mOverlayRecyclerView.setClipToPadding(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mOverlayRecyclerView.setLayoutManager(layoutManager);
        mOverlayRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mOverlayRecyclerView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mThumbnailRecyclerView != null) {
                    mThumbnailRecyclerView.onTouchEvent(event);
                }
                setRecyclerViewOnTouchEvent(event);
                return true;
            }
        });
    }

    private TrimOverlayAdapter mOverlayAdapter;

    private void fillOverlayAdapter() {
        int width = (int) (getTimelineBarViewWidth() + mOverlayThumbWidth * 2);
        int minTimeWidth = mCoverItemWidth * 2;
        if (mOverlayAdapter == null) {
            mOverlayAdapter = new TrimOverlayAdapter(mContext, width, minTimeWidth, mDuration);
            mOverlayAdapter.setOnSelectorPlayTimeListener(new TrimLineOverlay.OnSelectorPlayTimeListener() {
                @Override
                public void onUpdateTime(long playTime) {
                    //滑动时触发
                    setPlaySeek(playTime);
                }

                @Override
                public void onSetPlayTime(int playType, long startTime, long endTime) {
                    //手指松开触发
                    mStartTime = startTime;
                    mEndTime = endTime;
                    if (mAliyunIEditor != null) {
                        mAliyunIEditor.seek(playType == 0 ? startTime : endTime);
                    }
                    setRvScroll(playType == 0 ? startTime : endTime, true);
                }
            });
            mOverlayRecyclerView.setAdapter(mOverlayAdapter);
        } else {
            mOverlayAdapter.notifyDataSetChanged();
        }
    }


    private void initTitleView(View view) {
        ImageView ivEffect = view.findViewById(R.id.iv_effect_icon);
        TextView tvTitle = view.findViewById(R.id.tv_effect_title);
        ivEffect.setImageResource(R.mipmap.ic_video_editor_trim);
        tvTitle.setText(R.string.alivc_editor_effect_trim);
        view.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dealCancel();
            }
        });
        view.findViewById(R.id.iv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowPage = false;
                mCountStartTime = mStartTime;
                mCountEndTime = mEndTime;
                mHandler.removeCallbacksAndMessages(null);
                EditorVideHelper.resetVideoTimes(mAliyunIEditor, mStartTime, mEndTime);
                mAlivcEditView.setTrimVideoTimes(mStartTime, mEndTime);
                if (mOnEffectActionLister != null) {
                    mOnEffectActionLister.onComplete();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dealCancel();
    }

    /**
     * 取消处理
     */
    private void dealCancel() {
        isShowPage = false;
        mStartTime = mCountStartTime;
        mEndTime = mCountEndTime;
        if (mOverlayAdapter != null) {
            mOverlayAdapter.setTrimTime(mStartTime, mEndTime);
        }
        EditorVideHelper.resetVideoTimes(mAliyunIEditor, mStartTime, mEndTime);

        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    private void setRecyclerViewOnTouchEvent(MotionEvent event) {
        int actionMasked = event.getAction();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                isRvTouching = true;
                if (mPlayerListener != null) {
                    mPlayerListener.onPlayPause();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isRvNewState == RecyclerView.SCROLL_STATE_IDLE) {
                    isRvTouching = false;
                }
                break;
            default:
                break;
        }
    }

    private AlivcEditView.OnPlayingCallback onPlayingCallback;


    public void setPlayScroll() {
        if (onPlayingCallback == null) {
            onPlayingCallback = new AlivcEditView.OnPlayingCallback() {
                @Override
                public void onPlaying(long currentPlayTime, long currentStreamPlayTime) {
                    if (isRvTouching) {
                        //如果正在触摸，不做任何处理
                        return;
                    }
                    setRvScroll(currentPlayTime, false);
                }
            };
            mAlivcEditView.addPlayCallback(onPlayingCallback);
        }
    }


    /**
     * 获取缩略图的总宽度
     *
     * @return int
     */
    protected float getTimelineBarViewWidth() {
        return (float) (mCacheSize * mCoverItemWidth - (1-mEndIntervalWidth) * mCoverItemWidth);
    }


    protected int mCurrScroll;

    /**
     * 根据 RecyclerView 滑动 ScrollX 更新播放进度
     */
    private synchronized void setPlaySeek(int dx) {
        mCurrScroll += dx;
        float rate = mCurrScroll / getTimelineBarViewWidth();
        long currentPlayTime = (long) (rate * mDuration);
        android.os.Message handlerMsg = new android.os.Message();
        handlerMsg.what = CODE_WHAT_UPDATE_PLAY_SEEK;
        handlerMsg.obj = currentPlayTime;
        mHandler.sendMessage(handlerMsg);
    }

    private void setPlaySeek(long playTime) {
        android.os.Message handlerMsg = new android.os.Message();
        handlerMsg.what = CODE_WHAT_TOUCH_PLAY_SEEK;
        handlerMsg.obj = playTime;
        mHandler.sendMessage(handlerMsg);
    }


    /**
     * 根据当前播放进度，更新 RecyclerView 滑动 ScrollX
     */
    protected synchronized void setRvScroll(long currentPlayTime, boolean isAnim) {
        if (!isShowPage) {
            return;
        }
        float rate = ((float) currentPlayTime) / (float) mDuration;
        float scrollBy = rate * getTimelineBarViewWidth() - mCurrScroll;
        android.os.Message handlerMsg = new android.os.Message();
        Bundle data = new Bundle();
        if (currentPlayTime == 0) {
            data.putInt("position", 0);
            mCurrScroll = 0;
            scrollBy = 0;
        }
        data.putBoolean("isAnim", isAnim);
        data.putInt("offsetX", (int) scrollBy);
        handlerMsg.setData(data);
        handlerMsg.what = CODE_WHAT_UPDATE_SCROLL;
        mHandler.sendMessage(handlerMsg);
        mCurrScroll += scrollBy;
    }


    private StaticHandler mHandler;

    private final static int CODE_WHAT_UPDATE_SCROLL = 0x1;
    private final static int CODE_WHAT_UPDATE_PLAY_SEEK = 0x2;
    private final static int CODE_WHAT_TOUCH_PLAY_SEEK = 0x3;
    private final static int CODE_WHAT_RE_START = 0x4;

    private static class StaticHandler extends Handler {
        private WeakReference<TrimVideoFragment> mContext;

        StaticHandler(TrimVideoFragment context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            final TrimVideoFragment context = mContext.get();
            if (context == null) {
                return;
            }
            if (msg.what == CODE_WHAT_UPDATE_PLAY_SEEK) {
                long currentPlayTime = (long) msg.obj;
                if (context.mPlayerListener != null) {
                    context.mPlayerListener.onPlayPause();
                    context.mPlayerListener.updateDuration(currentPlayTime);
                }
                if (context.mAliyunIEditor != null) {
                    context.mAliyunIEditor.seek(currentPlayTime);
                }
            } else if (msg.what == CODE_WHAT_UPDATE_SCROLL) {
                float position = msg.getData().getInt("position", -1);
                int offsetX = msg.getData().getInt("offsetX");
                if (position == 0) {
                    context.mTimelineRecyclerView.scrollToPosition(0);
                    context.mThumbnailRecyclerView.scrollToPosition(0);
                    context.mOverlayRecyclerView.scrollToPosition(0);
                } else {
                    boolean isAnim = msg.getData().getBoolean("isAnim", false);
                    if (isAnim) {
                        context.mThumbnailRecyclerView.smoothScrollBy(offsetX, 0);
                    } else {
                        context.mThumbnailRecyclerView.scrollBy(offsetX, 0);
                    }
                }
            } else if (msg.what == CODE_WHAT_TOUCH_PLAY_SEEK) {
                long currentPlayTime = (long) msg.obj;
                if (context.mAliyunIEditor != null) {
                    context.mAliyunIEditor.draw(currentPlayTime);
                }
                if (context.mPlayerListener != null) {
                    context.mPlayerListener.updateDuration(currentPlayTime);
                }
            } else if (msg.what == CODE_WHAT_RE_START) {
                if (context.mAliyunIEditor != null) {
                    context.mAliyunIEditor.seek(context.mStartTime);
                    context.mAliyunIEditor.play();
                }
            }
        }

    }

    @Override
    protected void init() {
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }

    @Override
    protected void onRemove() {
        super.onRemove();
        if (mAlivcEditView != null) {
            mAlivcEditView.setPlayCallback(null);
        }
    }

    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mThumbnailRecyclerView != null) {
            mThumbnailRecyclerView.clearOnScrollListeners();
        }
        //页面关闭时
        if (mAlivcEditView != null) {
            mAlivcEditView.removePlayCallback(onPlayingCallback);
            onPlayingCallback = null;
        }
        if (mThumbnailFetcherManage != null) {
            mThumbnailFetcherManage.cleans();
        }
    }
}
