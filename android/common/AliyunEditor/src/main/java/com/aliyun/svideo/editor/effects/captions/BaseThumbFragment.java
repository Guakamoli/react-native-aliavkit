package com.aliyun.svideo.editor.effects.captions;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.captions.adapter.CaptionOverlayAdapter;
import com.aliyun.svideo.editor.effects.captions.adapter.CaptionThumbnailAdapter;
import com.aliyun.svideo.editor.effects.captions.adapter.CaptionTimelineAdapter;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.trim.TrimLineOverlay;
import com.aliyun.svideo.editor.effects.trim.TrimOverlayAdapter;
import com.aliyun.svideo.editor.publish.paiya.CoverInfo;
import com.aliyun.svideo.editor.publish.paiya.ThumbnailFetcherManage;
import com.aliyun.svideo.editor.view.AlivcEditView;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.blankj.utilcode.util.SPUtils;
import com.manwei.libs.dialog.DialogUtils;
import com.manwei.libs.dialog.OnDialogListener;
import com.manwei.libs.utils.GsonManage;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 缩略图的联动
 */
public class BaseThumbFragment extends BaseChooser {

    protected AlivcEditView mAlivcEditView;
    protected AliyunIEditor mAliyunIEditor;

    public void setBaseData(AlivcEditView rootView, AliyunIEditor iEditor) {
        mAlivcEditView = rootView;
        mAliyunIEditor = iEditor;
    }

    protected Context mContext;
    protected int mCoverItemWidth, mCoverItemHeight, mScreenWidth, mTimelineLeft, mOverlayThumbWidth;
    protected int mMinIntervalDistance;
    protected boolean isShowPage = true;
    /**
     * 判断列表是否被触摸
     */
    protected boolean isRvTouching;
    /**
     * 判断列表是否停止滚动，停止滚动后，才去重置触摸状态
     */
    protected int isRvNewState;


    /**
     * 缩略图间隔时间（2us）
     */
    protected final long mCacheIntervalTime = 1000000;
    protected long mStartTime = 0L, mEndTime = 0L, mDuration = 0L;//(us)
    private ThumbnailFetcherManage mThumbnailFetcherManage;

    private final List<CoverInfo> mBitmapList = new ArrayList<>();

    private int mCacheSize;
    private double mEndIntervalWidth;

    private AppCompatActivity mActivity;

    private TouchRecyclerView mTimelineRecyclerView;
    protected TouchRecyclerView mThumbnailRecyclerView;
    protected TouchRecyclerView mOverlayRecyclerView;

    protected CaptionView mToolsLayout;
    protected TextView tvCaptionAddedHint;

    public void setActivity(AppCompatActivity activity) {
        this.mActivity = activity;
    }

    protected void initView(Context context) {
        this.mContext = context;
        mMinIntervalDistance = DensityUtils.dip2px(mContext, 1f);
        mOverlayThumbWidth = DensityUtils.dip2px(mContext, 20f);
        mTimelineLeft = DensityUtils.dip2px(mContext, 15f);
        mCoverItemWidth = DensityUtils.dip2px(mContext, 50f);
        mCoverItemHeight = DensityUtils.dip2px(mContext, 50f);
        mScreenWidth = ScreenUtils.getWidth(mContext);

        if (mHandler == null) {
            mHandler = new StaticHandler(this);
        }

        View view = LayoutInflater.from(context).inflate(R.layout.paiya_base_thumb_linebar, this);
        mToolsLayout = view.findViewById(R.id.toolsLayout);
        tvCaptionAddedHint = view.findViewById(R.id.tvCaptionAddedHint);
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


    protected void showCaptionHint() {
        if (tvCaptionAddedHint != null && tvCaptionAddedHint.getVisibility() != VISIBLE) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(tvCaptionAddedHint, "alpha", 0, 255);
            animator.setDuration(250);
            animator.setStartDelay(250);
            animator.start();
            tvCaptionAddedHint.setVisibility(VISIBLE);
            tvCaptionAddedHint.setAlpha(0);
        }
    }

    protected void hideCaptionHint() {
        if (tvCaptionAddedHint != null && tvCaptionAddedHint.getVisibility() != GONE) {
            tvCaptionAddedHint.setVisibility(GONE);
        }
    }

    public void initData(Uri uri, long startTime, long endTime, boolean isShowSelectedView) {
        isShowPage = true;
        setShowSelectedView(isShowSelectedView);
        if (startTime != mStartTime || endTime != mEndTime) {
            if (mPlayerListener != null) {
                mDuration = mPlayerListener.getDuration() - startTime;
            } else {
                mDuration = endTime - startTime;
            }
            mThumbnailFetcherManage = new ThumbnailFetcherManage(uri, mCoverItemWidth, mCoverItemHeight, 10);
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
            }, startTime, endTime, mCacheIntervalTime);

            mStartTime = startTime;
            mEndTime = endTime;
            fillThumbnailAdapter(0);
            fillTimelineAdapter();
        }

        if (!isShowSelectedView) {
            mAliyunIEditor.seek(0);
            setRvScroll(0);
            if (mPlayerListener != null) {
                mPlayerListener.updateDuration(0);
            }
        } else {
            if (mPlayerListener != null) {
                //初始化播放位置
                setRvScroll(mPlayerListener.getCurrDuration());
            }
        }

        setPlayScroll();
    }


    /**
     * 获取缩略图的总宽度
     *
     * @return int
     */
    protected float getTimelineBarViewWidth() {
        return (float) (mCacheSize * mCoverItemWidth - (1 - mEndIntervalWidth) * mCoverItemWidth);
    }


    protected int mCurrScroll;

    /**
     * 根据当前播放进度，更新 RecyclerView 滑动 ScrollX
     */
    protected synchronized void setRvScroll(long currentPlayTime) {
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
        data.putInt("offsetX", (int) scrollBy);
        handlerMsg.setData(data);
        handlerMsg.what = CODE_WHAT_UPDATE_SCROLL;
        mHandler.sendMessage(handlerMsg);
        mCurrScroll += scrollBy;
    }

    protected synchronized void setRvScrollTo(int dx) {
        android.os.Message handlerMsg = new android.os.Message();
        handlerMsg.what = CODE_WHAT_UPDATE_SCROLL_TO;
        handlerMsg.obj = dx;
        mHandler.sendMessage(handlerMsg);
    }

    protected void setPlaySeekDraw(long playTime) {
        android.os.Message handlerMsg = new android.os.Message();
        handlerMsg.what = CODE_WHAT_TOUCH_PLAY_SEEK;
        handlerMsg.obj = playTime;
        mHandler.sendMessage(handlerMsg);
    }

    /**
     * 根据 RecyclerView 滑动 ScrollX 更新播放进度
     */
    protected synchronized void setPlaySeek(int dx) {
        mCurrScroll += dx;
        float rate = mCurrScroll / getTimelineBarViewWidth();
        long currentPlayTime = (long) (rate * mDuration) + mStartTime;
        android.os.Message handlerMsg = new android.os.Message();
        handlerMsg.what = CODE_WHAT_UPDATE_PLAY_SEEK;
        handlerMsg.obj = currentPlayTime;
        mHandler.sendMessage(handlerMsg);
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

    private CaptionThumbnailAdapter mThumbnailAdapter;

    private void fillThumbnailAdapter(Integer position) {
        if (mThumbnailAdapter == null) {
            mThumbnailAdapter = new CaptionThumbnailAdapter(mContext, mBitmapList, mCoverItemWidth, mCoverItemHeight, mScreenWidth, mEndIntervalWidth);
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


    private CaptionOverlayAdapter mOverlayAdapter;

    protected FrameLayout getOverlayFrameLayout() {
        if (mOverlayAdapter != null) {
            return mOverlayAdapter.getCaptionLineOverlays();
        }
        return null;
    }

    protected void fillOverlayAdapter() {
        if (mOverlayAdapter == null) {
            int baseWidth = (int) (getTimelineBarViewWidth() + mOverlayThumbWidth * 2);
            mOverlayAdapter = new CaptionOverlayAdapter(mContext, baseWidth);
            mOverlayRecyclerView.setAdapter(mOverlayAdapter);
        } else {
            mOverlayAdapter.notifyDataSetChanged();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initThumbnailRecyclerView(View view) {
        mThumbnailRecyclerView = view.findViewById(R.id.thumbRecyclerView);
        mThumbnailRecyclerView.setPadding(mScreenWidth / 2, mThumbnailRecyclerView.getPaddingTop(), mScreenWidth / 2, mThumbnailRecyclerView.getPaddingBottom());
        mThumbnailRecyclerView.setClipToPadding(false);
        mThumbnailRecyclerView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                setRecyclerViewOnTouchEvent(event);
                return false;
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

    private void initTitleView(View view) {
        ImageView ivEffect = view.findViewById(R.id.iv_effect_icon);
        TextView tvTitle = view.findViewById(R.id.tv_effect_title);
        ivEffect.setImageResource(R.mipmap.aliyun_svideo_icon_caption);
        tvTitle.setText(R.string.alivc_editor_effect_caption);
        view.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mAlivcEditView != null && (mAlivcEditView.getCaptionsUiList() == null || mAlivcEditView.getCaptionsUiList().isEmpty())) || !(mContext instanceof AppCompatActivity)) {
                    isShowPage = false;
                    //没添加过字幕直接关闭
                    removeCaptionString();
                    setShowSelectedView(true);
                    dealCancel();
                    return;
                }

                dealCancelDialog();
            }
        });
        view.findViewById(R.id.iv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowPage = false;
                setShowSelectedView(true);
                saveCaptionString();
                mAlivcEditView.isShowEditCaptions = false;
                if (mOnEffectActionLister != null) {
                    mOnEffectActionLister.onComplete();
                }
            }
        });
    }


    private AlivcEditView.OnPlayingCallback onPlayingCallback;

    /**
     * 播放和滑动关联
     */
    public void setPlayScroll() {
        if (onPlayingCallback == null) {
            onPlayingCallback = new AlivcEditView.OnPlayingCallback() {
                @Override
                public void onPlaying(long currentPlayTime, long currentStreamPlayTime) {
                    if (isRvTouching) {
                        //如果正在触摸，不做任何处理
                        return;
                    }
                    setRvScroll(currentPlayTime);
                }
            };
            mAlivcEditView.addPlayCallback(onPlayingCallback);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if ((mAlivcEditView != null && (mAlivcEditView.getCaptionsUiList() == null || mAlivcEditView.getCaptionsUiList().isEmpty())) || !(mContext instanceof AppCompatActivity)) {
            //没添加过字幕直接关闭
            removeCaptionString();
            setShowSelectedView(true);
            dealCancel();
            return;
        }
        dealCancelDialog();
    }


    private void dealCancelDialog() {
        if (mContext == null || ((AppCompatActivity) mContext).isFinishing()) {
            isShowPage = false;
            removeCaptionString();
            setShowSelectedView(true);
            dealCancel();
        }
        String hintStr = mContext.getResources().getString(R.string.paiya_clean_add_caption_hint);
        String cancelStr = mContext.getResources().getString(R.string.cancel);
        String confirmStr = mContext.getResources().getString(R.string.confirm);
        DialogUtils.showDialog((AppCompatActivity) mContext, hintStr, cancelStr, confirmStr, new OnDialogListener() {
            @Override
            public void onRightClick() {
                isShowPage = false;
                removeCaptionString();
                setShowSelectedView(true);
                dealCancel();
            }
        });
    }


    /**
     * 取消处理
     */
    private void dealCancel() {
        if (mOnEffectActionLister != null) {
            mAlivcEditView.isShowEditCaptions = false;
            hideCaptionHint();
            mOnEffectActionLister.onCancel();
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

    public BaseThumbFragment(@NonNull @NotNull Context context) {
        super(context);
        initView(context);
    }

    private boolean isShowSelectedView;

    public void setShowSelectedView(boolean isShow) {
        this.isShowSelectedView = isShow;
    }

    @Override
    public boolean isShowSelectedView() {
        return isShowSelectedView;
    }

    @Override
    protected void init() {
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }

    private StaticHandler mHandler;

    private final static int CODE_WHAT_UPDATE_SCROLL = 0x1;
    private final static int CODE_WHAT_UPDATE_PLAY_SEEK = 0x2;
    private final static int CODE_WHAT_TOUCH_PLAY_SEEK = 0x3;
    private final static int CODE_WHAT_UPDATE_SCROLL_TO = 0x4;

    private static class StaticHandler extends Handler {
        private WeakReference<BaseThumbFragment> mContext;

        StaticHandler(BaseThumbFragment context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            final BaseThumbFragment context = mContext.get();
            if (context == null) {
                return;
            }
            if (msg.what == CODE_WHAT_UPDATE_SCROLL) {
                float position = msg.getData().getInt("position", -1);
                int offsetX = msg.getData().getInt("offsetX");
                if (position == 0) {
                    context.mTimelineRecyclerView.scrollToPosition(0);
                    context.mThumbnailRecyclerView.scrollToPosition(0);
                    context.mOverlayRecyclerView.scrollToPosition(0);
                } else {
                    if (context.isAddCaptionView) {
                        context.mThumbnailRecyclerView.smoothScrollBy(offsetX, 0);
                        context.isAddCaptionView = false;
                    } else {
                        context.mThumbnailRecyclerView.scrollBy(offsetX, 0);
                    }
                }
            } else if (msg.what == CODE_WHAT_UPDATE_PLAY_SEEK) {
                long currentPlayTime = (long) msg.obj;
                if (context.mPlayerListener != null) {
                    context.mPlayerListener.onPlayPause();
                    context.mPlayerListener.updateDuration(currentPlayTime);
                }
                if (context.mAliyunIEditor != null) {
                    context.mAliyunIEditor.seek(currentPlayTime);
                }
            } else if (msg.what == CODE_WHAT_TOUCH_PLAY_SEEK) {
                long currentPlayTime = (long) msg.obj;
                if (context.mAliyunIEditor != null) {
                    context.mAliyunIEditor.draw(currentPlayTime);
                }
                if (context.mPlayerListener != null) {
                    context.mPlayerListener.updateDuration(currentPlayTime);
                }
            } else if (msg.what == CODE_WHAT_UPDATE_SCROLL_TO) {
                int offsetX = (int) msg.obj;
                context.mThumbnailRecyclerView.scrollBy(offsetX, 0);
            }
        }
    }

    protected boolean isAddCaptionView;

    protected final List<String> mBlessingList = new ArrayList<>();


    public void saveCaptionString() {
        SPUtils.getInstance().put("mBlessingList", GsonManage.toJson(mBlessingList));
    }

    public static void removeCaptionString() {
        SPUtils.getInstance().remove("mBlessingList");
    }


}
