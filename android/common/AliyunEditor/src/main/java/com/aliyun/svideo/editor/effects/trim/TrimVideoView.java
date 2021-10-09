package com.aliyun.svideo.editor.effects.trim;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.base.widget.VideoSliceSeekBar;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.publish.paiya.CoverInfo;
import com.aliyun.svideo.editor.publish.paiya.CoverThumbnailAdapter;
import com.aliyun.svideo.editor.publish.paiya.ThumbnailFetcherManage;
import com.aliyun.svideo.editor.view.AlivcEditView;
import com.aliyun.svideosdk.editor.AliyunIEditor;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class TrimVideoView extends BaseChooser {
    private static final int PLAY_VIDEO = 1000;
    private static final int PAUSE_VIDEO = 1001;
    private static final int END_VIDEO = 1003;

    private int playState = END_VIDEO;
    /**
     * 编辑核心接口类
     */
    private AliyunIEditor mAliyunIEditor;


    private StaticHandler mHandler;


    public TrimVideoView(@NonNull @NotNull Context context, AliyunIEditor aliyunIEditor) {
        this(context, null, aliyunIEditor);
    }

    public TrimVideoView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, AliyunIEditor aliyunIEditor) {
        this(context, attrs, 0, aliyunIEditor);
    }

    public TrimVideoView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, AliyunIEditor aliyunIEditor) {
        super(context, attrs, defStyleAttr);
        mAliyunIEditor = aliyunIEditor;
        if (mHandler == null) {
            mHandler = new StaticHandler(this);
        }
    }

    private ThumbnailFetcherManage mThumbnailFetcherManage;


//    /**
//     * 视频地址
//     */
//    private String mVideoPath = "";

    /**
     * 截取视频单个封面的宽度
     */
    private int mCoverItemWidth, mCoverItemHeight;
    private final int mThumbnailCount = 10;
    private List<CoverInfo> mBitmapList = Arrays.asList(new CoverInfo[mThumbnailCount]);

    private TextView dirationTxt;
    private VideoSliceSeekBar mSeekBar;
    private RecyclerView mRecyclerView;

    private int cropDuration = 2000;
    private long duration;
    private int screenWidth;

    /**
     * 每次修改裁剪结束位置时需要重新播放视频
     */
    private boolean needPlayStart = false;

    private void initData() {
        screenWidth = ScreenUtils.getWidth(getContext());
        duration = mAliyunIEditor.getDuration();
        Log.e("AAA", "duration：" + duration);
        mCoverItemHeight = DensityUtils.dip2px(getContext(), 64f);
        mCoverItemWidth = (ScreenUtils.getWidth(getContext()) - DensityUtils.dip2px(getContext(), 40f)) / mThumbnailCount;

        Uri uri = mAlivcEditView.getVideoUri();
        mThumbnailFetcherManage = new ThumbnailFetcherManage(uri, mCoverItemWidth, mCoverItemHeight, mThumbnailCount);
        mThumbnailFetcherManage.getCoverThumbnailList(new ThumbnailFetcherManage.OnCoverThumbnailCallback() {
            @Override
            public void onNext(CoverInfo coverInfo) {
                if (coverInfo != null) {
                    mBitmapList.set(coverInfo.getPosition(), coverInfo);
                    fillAdapter(coverInfo.getPosition());
                }
            }
        });
    }


    private void setDirationTxt(double diration) {
        DecimalFormat fnum = new DecimalFormat("##0.0");
        dirationTxt.setText(fnum.format(diration));
    }

    private AlivcEditView mAlivcEditView;

    public void initView(AlivcEditView rootView) {
        mAlivcEditView = rootView;

        initData();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_trim_overlay, this);
        initTitleView(view);

        mRecyclerView = view.findViewById(R.id.aliyun_video_tailor_image_list);
        mSeekBar = view.findViewById(R.id.aliyun_seek_bar);
        dirationTxt = view.findViewById(R.id.aliyun_duration_txt);
        setDirationTxt(duration / 1000000.00);
        //设置最小范围
        int minDiff = (int) (cropDuration / (float) (duration / 1000F) * 100) + 1;
        mSeekBar.setProgressMinDiff(minDiff > 100 ? 100 : minDiff);
        mSeekBar.setSeekBarChangeListener(mSeekBarListener);

//        mSeekBar.setSliceBlocked(true);
        mSeekBar.showFrameProgress(true);

        setListViewHeight();
    }

    /**
     * 非首次加载才会调用
     */
    public void refreshView(long startTime, long endTime) {
        mCountStartTime = mStartTime = startTime;
        mCountEndTime = mEndTime = endTime;
        if (mSeekBar != null) {
            double startProgress = startTime * 100.00 / duration;
            double endProgress = endTime * 100.00 / duration;
            mSeekBar.setProgress((int) startProgress, (int) endProgress);
        }

        setDirationTxt((mEndTime - mStartTime) / 1000000.00);

        mAlivcEditView.setPlayCallback(new AlivcEditView.OnPlayingCallback() {
            @Override
            public void onPlaying(long currentPlayTime, long currentStreamPlayTime) {
                Log.e("bbb", ";mStartTime:" + mStartTime+ ";mEndTime:" + mEndTime + ";currentPlayTime:" + currentPlayTime);
                if ((mEndTime > 0 && currentPlayTime >= mEndTime) || (mStartTime > 0 && currentPlayTime < mStartTime)) {
                    //currentPlayTime 13360000
                    //mEndTime 13375000
                    mHandler.sendEmptyMessage(1);
                }
                android.os.Message handlerMsg = new android.os.Message();
                handlerMsg.what = 0;
                handlerMsg.obj = currentPlayTime;
                mHandler.sendMessage(handlerMsg);
            }
        });
    }

    private void setListViewHeight() {
        LayoutParams layoutParams = (LayoutParams) mRecyclerView.getLayoutParams();
        layoutParams.height = (screenWidth - DensityUtils.dip2px(getContext(), 50)) / 10;
        mRecyclerView.setLayoutParams(layoutParams);
        mSeekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, layoutParams.height));
        mSeekBar.initView();
    }

    private long mCountStartTime, mCountEndTime;
    private long mStartTime, mEndTime;

    private VideoSliceSeekBar.SeekBarChangeListener mSeekBarListener = new VideoSliceSeekBar.SeekBarChangeListener() {
        @Override
        public void seekBarValueChanged(float leftThumb, float rightThumb, int whitchSide) {
            long seekPos = 0;
            if (whitchSide == 0) {
                seekPos = (long) (duration * leftThumb / 100);
                mStartTime = seekPos;
                if (mEndTime == 0) {
                    mEndTime = (long) (duration * rightThumb / 100);
                }
                Log.e("AAA", "rightThumb:" + rightThumb + ";mEndTime:" + mEndTime);
            } else if (whitchSide == 1) {
                seekPos = (long) (duration * rightThumb / 100);
                mEndTime = seekPos;
            }
            setDirationTxt((mEndTime - mStartTime) / 1000000.00);

            if (mAliyunIEditor != null) {
                mAliyunIEditor.seek((int) seekPos);
                mAliyunIEditor.play();
            }
        }

        @Override
        public void onSeekStart() {
            pauseVideo();
        }

        @Override
        public void onSeekEnd() {
            needPlayStart = true;
            if (playState == PAUSE_VIDEO) {
                playVideo();
            }
        }
    };

    private void playVideo() {
        if (mAliyunIEditor == null) {
            return;
        }
        mAliyunIEditor.seek((int) mStartTime);
        mAliyunIEditor.resume();
        playState = PLAY_VIDEO;
        //重新播放之后修改为false，防止暂停、播放的时候重新开始播放
        needPlayStart = false;
    }

    private void pauseVideo() {
        if (mAliyunIEditor == null) {
            return;
        }
        mAliyunIEditor.pause();
        playState = PAUSE_VIDEO;
        mSeekBar.showFrameProgress(false);
        mSeekBar.invalidate();
    }

//    private void setIndicatorView() {
//        View indicator = new View(getContext());
//        LayoutParams params = new LayoutParams(DensityUtils.dip2px(getContext(), 3), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
//        indicator.setLayoutParams(params);
//        indicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.alivc_common_font_cyan_light));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            indicator.setOutlineProvider(new ViewOutlineProvider() {
//                @Override
//                public void getOutline(View view, Outline outline) {
//                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), DensityUtils.dip2px(getContext(), 2));
//                }
//            });
//            indicator.setClipToOutline(true);
//        }
//        addView(indicator);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dealCancel();
    }

    private void initTitleView(View view) {
        ImageView ivEffect = view.findViewById(R.id.iv_effect_icon);
        TextView tvTitle = view.findViewById(R.id.tv_effect_title);
        ivEffect.setImageResource(R.mipmap.ic_video_editor_trim);
        tvTitle.setText(R.string.alivc_editor_effect_trim);
        view.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消裁剪
                dealCancel();
            }
        });
        view.findViewById(R.id.iv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlivcEditView != null) {
                    mAlivcEditView.setPlayCallback(null);
                }
                //确定裁剪
                if (mOnEffectActionLister != null) {
                    mOnEffectActionLister.onComplete();
                }
                if (mTrimVideoCallback != null) {
                    mTrimVideoCallback.onTrimVideo(mStartTime, mEndTime);
                }
                if (mOnEffectChangeListener != null) {
                    EffectInfo trimEffectInfo = new EffectInfo();
                    trimEffectInfo.type = UIEditorPage.TRIM;
//                    trimEffectInfo.startTime = mStartTime;
//                    trimEffectInfo.endTime = mEndTime;
//                    trimEffectInfo.streamEndTime = mStartTime;
//                    trimEffectInfo.streamStartTime = mEndTime;
                    mOnEffectChangeListener.onEffectChange(trimEffectInfo);
                }
            }
        });
    }

    /**
     * 点击空白出弹窗消失
     */
    public void hideBottomView() {
        dealCancel();
    }

    /**
     * 取消处理
     */
    private void dealCancel() {
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
        if (mTrimVideoCallback != null) {
            mTrimVideoCallback.onCancel(mCountStartTime, mCountEndTime);
        }
        mStartTime = mCountStartTime;
        mEndTime = mCountEndTime;
        if (mAliyunIEditor != null) {
            mAliyunIEditor.seek(mStartTime);
            mAliyunIEditor.play();
        }
        if (mAlivcEditView != null) {
            mAlivcEditView.setPlayCallback(null);
        }
    }

    @Override
    protected void init() {
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }


    private CoverThumbnailAdapter mAdapter;

    private void fillAdapter(Integer position) {
        if (mAdapter == null) {
            mAdapter = new CoverThumbnailAdapter(getContext(), mBitmapList, mCoverItemWidth, mCoverItemHeight);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
                @Override
                public boolean canScrollHorizontally() {
                    return false;
                }
            };
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);
        } else {
            if (position != null) {
                int index = position;
                if (index >= 0 && index < mBitmapList.size()) {
                    mAdapter.notifyItemChanged(index);
                    return;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }


    private static class StaticHandler extends Handler {
        private WeakReference<TrimVideoView> mContext;

        StaticHandler(TrimVideoView context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            final TrimVideoView context = mContext.get();
            if (context == null) {
                return;
            }
            if (msg.what == 0) {
                long currentPlayTime = (long) msg.obj;
                context.mSeekBar.showFrameProgress(true);
                context.mSeekBar.setFrameProgress((currentPlayTime / 1000f) / (context.duration / 1000f));
            } else if (msg.what == 1) {
                if (context.mAliyunIEditor != null) {
                    context.mAliyunIEditor.seek((int) context.mStartTime);
                    context.mAliyunIEditor.play();
                }
            }
        }
    }


    private OnTrimVideoCallback mTrimVideoCallback;

    public void setTrimVideoCallback(OnTrimVideoCallback callback) {
        mTrimVideoCallback = callback;
    }

    public interface OnTrimVideoCallback {
        void onCancel(long startTime, long endTime);

        void onTrimVideo(long startTime, long endTime);
    }


    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (mThumbnailFetcherManage != null) {
            mThumbnailFetcherManage.cleans();
        }

    }
}
