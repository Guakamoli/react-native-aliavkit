package com.aliyun.svideo.editor.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.UIConfigManager;
import com.aliyun.svideo.base.http.MusicFileBean;
import com.aliyun.svideo.common.ScreenOrientationListener;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.common.widget.AutocueLayoutView;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcEditOutputParam;
import com.aliyun.svideo.editor.bean.AlivcTransBean;
import com.aliyun.svideo.editor.bean.PasterRestoreBean;
import com.aliyun.svideo.editor.editor.AbstractPasterUISimpleImpl;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.editor.editor.PasterUICaptionImpl;
import com.aliyun.svideo.editor.editor.PasterUIGifImpl;
import com.aliyun.svideo.editor.editor.PasterUITextImpl;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineConfig;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineOverlay;
import com.aliyun.svideo.editor.effects.captions.BaseThumbFragment;
import com.aliyun.svideo.editor.effects.captions.PasterUICaptionsImpl;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EditorService;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnEffectActionLister;
import com.aliyun.svideo.editor.effects.control.OnEffectChangeListener;
import com.aliyun.svideo.editor.effects.control.OnTabChangeListener;
import com.aliyun.svideo.editor.effects.control.TabGroup;
import com.aliyun.svideo.editor.effects.control.TabViewStackBinding;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.effects.control.ViewStack;
import com.aliyun.svideo.editor.effects.filter.AnimationFilterController;
import com.aliyun.svideo.editor.effects.sound.MockEffectSoundData;
import com.aliyun.svideo.editor.effects.transition.TransitionChooserView;
import com.aliyun.svideo.editor.effects.transition.TransitionEffectCache;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.msg.body.BrightnessProgressMsg;
import com.aliyun.svideo.editor.msg.body.CheckDeleteFilter;
import com.aliyun.svideo.editor.msg.body.ContrastProgressMsg;
import com.aliyun.svideo.editor.msg.body.FilterTabClick;
import com.aliyun.svideo.editor.msg.body.LongClickAnimationFilter;
import com.aliyun.svideo.editor.msg.body.LongClickUpAnimationFilter;
import com.aliyun.svideo.editor.msg.body.SaturationProgressMsg;
import com.aliyun.svideo.editor.msg.body.SelectColorFilter;
import com.aliyun.svideo.editor.msg.body.SharpProgressMsg;
import com.aliyun.svideo.editor.msg.body.VideoEqResetAllMsg;
import com.aliyun.svideo.editor.msg.body.VideoEqResetMsg;
import com.aliyun.svideo.editor.msg.body.VignetteMsg;
import com.aliyun.svideo.editor.util.AlivcSnapshot;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideo.editor.util.FixedToastUtils;
import com.aliyun.svideo.editor.util.ThreadUtil;
import com.aliyun.svideo.editor.viewoperate.ViewOperator;
import com.aliyun.svideo.editor.widget.AliyunPasterWithImageView;
import com.aliyun.svideo.editor.widget.AliyunPasterWithTextView;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.AliyunIClipConstructor;
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory;
import com.aliyun.svideosdk.common.internal.videoaugment.VideoAugmentationType;
import com.aliyun.svideosdk.common.struct.CanvasInfo;
import com.aliyun.svideosdk.common.struct.common.AliyunClip;
import com.aliyun.svideosdk.common.struct.common.AliyunImageClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.ActionTranslate;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.effect.EffectBean;
import com.aliyun.svideosdk.common.struct.effect.EffectCaption;
import com.aliyun.svideosdk.common.struct.effect.EffectConfig;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.effect.EffectText;
import com.aliyun.svideosdk.common.struct.effect.TransitionBase;
import com.aliyun.svideosdk.common.struct.effect.TransitionCircle;
import com.aliyun.svideosdk.common.struct.effect.TransitionFade;
import com.aliyun.svideosdk.common.struct.effect.TransitionFiveStar;
import com.aliyun.svideosdk.common.struct.effect.TransitionShutter;
import com.aliyun.svideosdk.common.struct.effect.TransitionTranslate;
import com.aliyun.svideosdk.common.struct.effect.ValueTypeEnum;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.crop.AliyunICrop;
import com.aliyun.svideosdk.crop.CropCallback;
import com.aliyun.svideosdk.crop.CropParam;
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator;
import com.aliyun.svideosdk.editor.AliyunICanvasController;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.AliyunPasterController;
import com.aliyun.svideosdk.editor.AliyunPasterManager;
import com.aliyun.svideosdk.editor.AliyunRollCaptionComposer;
import com.aliyun.svideosdk.editor.AudioEffectType;
import com.aliyun.svideosdk.editor.EditorCallBack;
import com.aliyun.svideosdk.editor.EffectType;
import com.aliyun.svideosdk.editor.OnAnimationFilterRestored;
import com.aliyun.svideosdk.editor.OnPasterRestored;
import com.aliyun.svideosdk.editor.TimeEffectType;
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory;
import com.aliyun.svideosdk.editor.impl.AliyunPasterAbstractController;
import com.aliyun.svideosdk.editor.impl.d;
import com.duanqu.transcode.NativeParser;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

/**
 * @author zsy_18 data:2018/8/24
 */
public class AlivcEditView extends RelativeLayout
        implements View.OnClickListener, OnEffectChangeListener, OnTabChangeListener,
        OnAnimationFilterRestored {
    private static final String TAG = AlivcEditView.class.getName();


    private Context mContext;
    private AppCompatActivity mActivity;


    public void setActivity(AppCompatActivity activity) {
        this.mActivity = activity;
    }

    public AppCompatActivity getAppCompatActivity() {
        return mActivity;
    }

    /**
     * 选择音乐或者mv时所占音量的权重比
     */
    private final int bgmWeight = 20;

    /**
     * 编辑核心接口类
     */
    private AliyunIEditor mAliyunIEditor;
    /**
     * 动图管理接口类
     */
    private AliyunPasterManager mPasterManager;
    /**
     * 涂鸦使用的Controller接口，可以对涂鸦做一系列的操作
     */
    public AliyunICanvasController mCanvasController;
    /**
     * 获取缩略图片接口
     */
    private AliyunIThumbnailFetcher mThumbnailFetcher;

    /**
     * 裁剪接口核心类，对于Gop比较大的视频做时间特效时需要先检查是否满足实时，如果不满足实时，需要提前做转码，逻辑如下
     */
    private AliyunICrop mTranscoder;

    /**
     * 动图使用的Controller接口,可以获取动图的系列属性并且可以操作动图
     */
    private AliyunPasterController mAliyunPasterController;

    private OverlayThumbLineBar mThumbLineBar;

    /**
     * 底部滑动item的横向ScrollView
     */
    private HorizontalScrollView mBottomLinear;
    /**
     * 编辑需要渲染显示的SurfaceView
     */
    private SurfaceView mSurfaceView;
    /**
     * 底部菜单点击事件管理类
     */
    private TabGroup mTabGroup;
    /**
     * 处理底部菜单点击事件
     */
    private ViewStack mViewStack;
    /**
     * 主要用于记录各个功能view上次的状态，用于下次进入的时候进行恢复
     */
    private EditorService mEditorService;
    /**
     * 控件
     */
    private RelativeLayout mActionBar;
    private FrameLayout resCopy;
    private FrameLayout mTransCodeTip;
    private ProgressBar mTransCodeProgress;
    public FrameLayout mPasterContainer;
    private FrameLayout mGlSurfaceContainer;
    private ImageView mIvLeft;
    private TextView mTvRight;
    private LinearLayout mBarLinear;
    private TextView mPlayImage;
    private TextView mTvCurrTime;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth, mScreenHeight;
    /**
     * 水印图片
     */
    private Bitmap mWatermarkBitmap;
    /**
     * 特效使用的控制类
     */
    private AnimationFilterController mAnimationFilterController;
    /**
     * 时间特效在缩略图上的浮层 用于删除时间浮层
     */
    private ThumbLineOverlay mTimeEffectOverlay;
    private ThumbLineOverlay.ThumbLineOverlayView mThumbLineOverlayView;
    /**
     * 状态，使用倒放时间特效
     */
    private boolean mUseInvert = false;
    /**
     * 状态，正在添加滤镜特效那个中
     */
    private boolean mUseAnimationFilter = false;
    /**
     * 状态，判断是否可以继续添加时间特效，true不可以继续添加特效
     */
    private boolean mCanAddAnimation = true;
    /**
     * 状态，是否正在转码中
     */
    private boolean mIsTranscoding = false;
    /**
     * 状态，界面是否被销毁
     */
    private boolean mIsDestroyed = false;
    /**
     * 状态，与生命周期onStop有关
     */
    private boolean mIsStop = false;
    private boolean mWaitForReady = false;

    private AbstractPasterUISimpleImpl mCurrentEditEffect;
    /**
     * 音量
     */
    private int mVolume = 50;
    /**
     * 控制UI变动
     */
    private ViewOperator mViewOperate;
    private Point mPasterContainerPoint;
    private EffectBean lastMusicBean;

    public EffectBean getLastMusicBean() {
        return lastMusicBean;
    }

    //用户滑动thumbLineBar时的监听器
    private ThumbLineBar.OnBarSeekListener mBarSeekListener;
    //播放时间、显示时间、缩略图位置同步接口
    private PlayerListener mPlayerListener;

    private EffectInfo mLastColorFilterEffect;

    public EffectInfo getLastColorFilterEffect() {
        return mLastColorFilterEffect;
    }

    private EffectInfo mLastMVEffect;

    public EffectInfo getLastMVEffect() {
        return mLastMVEffect;
    }

    private EffectInfo mLastSoundEffect;

    public EffectInfo getLastSoundEffect() {
        return mLastSoundEffect;
    }

    private ObjectAnimator animatorX;
    private Toast showToast;

    private AutocueLayoutView llVerticalTitleLayout;
    private FrameLayout tvContentLayout;
    private TextView tvTitle, tvContent;

    public String getBlessingContent() {
        return mBlessingContent;
    }

    private String mBlessingContent;

    public void setBlessingData(String mStrTitle, String mStrContent) {
        BaseThumbFragment.removeCaptionString();
//        mBlessingContent = mStrTitle + "\n" + mStrContent;
        mBlessingContent = mStrContent;
        if (tvTitle != null) {
            tvTitle.setText(mStrTitle);
        }
        if (tvContent != null) {
            tvContent.setText(mStrContent);
        }
    }


    /**
     * 编辑模块Handler处理类
     */
    private AlivcEditHandler alivcEditHandler;
    /**
     * 线程池
     */
    private ExecutorService executorService;
    /**
     * 封面保存路径
     */
    private final String PATH_THUMBNAIL = Constants.SDCardConstants.getDir(getContext()) + File.separator + "thumbnail.jpg";
    /**
     * 是否可以截图
     */
    private boolean isTakeFrame = false;
    /**
     * 是否确认选择截图
     */
    private boolean isTakeFrameSelected = false;
    /**
     * 是否已经截取封面
     */
    private boolean hasCaptureCover = false;
    /**
     * 截图工具，用于获取surface的画面
     */
    private AlivcSnapshot mSnapshop;
    /**
     * 是否使用默认水印
     */
    private boolean hasWaterMark;
    /**
     * 判断是否有音乐
     */
    private boolean mHasRecordMusic;
    /**
     * 是否替换原视频中音乐
     */
    private boolean isReplaceMusic;
    /**
     * 是否合拍，合拍无法使用音乐
     */
    private boolean isMixRecord;

    private AlivcCircleLoadingDialog mLoadingDialog;

    /**
     * 记录可调节的转场特效
     */
    private LinkedHashMap<Integer, EffectInfo> mTransitionCache;
    /**
     * 记录可调节的转场特效的初始值
     */
    private LinkedHashMap<Integer, List<AlivcTransBean>> mTransitionParamsCache;

    /**
     * 是否开启降噪，默认无
     */
    private boolean mHasDeNoise = false;
    /**
     * 翻转字幕
     */
    private AliyunRollCaptionComposer mAliyunRollCaptionComposer;

    public AlivcEditView(Context context) {
        this(context, null);
    }

    public AlivcEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlivcEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        Dispatcher.getInstance().register(this);

        Point point = new Point();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        mScreenHeight = point.y;
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_edit, this, true);
        initView();
        initListView();
        add2Control();
        initThreadHandler();
        if (PermissionUtils.checkPermissionsGroup(getContext(), PermissionUtils.PERMISSION_STORAGE)) {
            copyAssets();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        llVerticalTitleLayout = findViewById(R.id.llVerticalTitleLayout);
        tvContentLayout = findViewById(R.id.tvContentLayout);
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        resCopy = (FrameLayout) findViewById(R.id.copy_res_tip);
        mTransCodeTip = (FrameLayout) findViewById(R.id.transcode_tip);
        mTransCodeProgress = (ProgressBar) findViewById(R.id.transcode_progress);
        mBarLinear = (LinearLayout) findViewById(R.id.bar_linear);
        mBarLinear.bringToFront();
        mActionBar = (RelativeLayout) findViewById(R.id.action_bar);
        mActionBar.setBackgroundDrawable(null);
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mTvRight = findViewById(R.id.tv_right);
        mIvLeft.setImageResource(R.mipmap.aliyun_svideo_icon_back);
        //uiConfig中的属性
        //UIConfigManager.setImageResourceConfig(mTvRight, R.attr.finishImage, R.mipmap.aliyun_svideo_complete_red);
        mIvLeft.setVisibility(View.VISIBLE);

        mIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) getContext()).finish();
            }
        });
        mTvCurrTime = (TextView) findViewById(R.id.tv_curr_duration);

        mGlSurfaceContainer = (FrameLayout) findViewById(R.id.glsurface_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.play_view);
        mBottomLinear = findViewById(R.id.edit_bottom_tab);
        setBottomTabResource();
        mPasterContainer = (FrameLayout) findViewById(R.id.pasterView);

        mPlayImage = findViewById(R.id.play_button);
        mPlayImage.setOnClickListener(this);
        switchPlayStateUI(false);

        final GestureDetector mGesture = new GestureDetector(getContext(), new MyOnGestureListener());
        View.OnTouchListener pasterTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGesture.onTouchEvent(event);
            }
        };

        mPasterContainer.setOnTouchListener(pasterTouchListener);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBottomEditorView();
            }
        });

    }

    /**
     * 设置底部效果按钮图标资源
     */
    private void setBottomTabResource() {
        TextView[] textViews = {
                findViewById(R.id.tab_trim),
                findViewById(R.id.tab_effect_caption),
                findViewById(R.id.tab_effect_audio_mix),
                findViewById(R.id.tab_effect_filter),
                findViewById(R.id.tab_filter),
                findViewById(R.id.tab_effect_overlay),
                findViewById(R.id.tab_effect_text),
                findViewById(R.id.tab_effect_mv),
                findViewById(R.id.tab_effect_sound),
                findViewById(R.id.tab_effect_time),
                findViewById(R.id.tab_effect_transition),
                findViewById(R.id.tab_paint),
                findViewById(R.id.tab_cover),
                findViewById(R.id.tab_videoeq),
                findViewById(R.id.tab_roll_caption)
        };
        int length = textViews.length;
        int[] index = new int[length];
        for (int i = 0; i < length; i++) {
            //所有的图片方向都是top
            index[i] = 1;
        }
        int[] attrs = {
                R.attr.clipImage,
                R.attr.captionImage,
                R.attr.musicImage,
                R.attr.effectImage,
                R.attr.filterImage,
                R.attr.pasterImage,
                R.attr.textImage,
                R.attr.mvImage,
                R.attr.sound,//音效
                R.attr.timeImage,
                R.attr.translationImage,
                R.attr.paintImage,
                R.attr.coverImage,
                R.attr.videoEqImage,
                R.attr.rollCoverImage
        };
        int[] defaultResourceIds = {
                R.mipmap.ic_video_editor_trim,
                R.mipmap.aliyun_svideo_caption,
                R.mipmap.aliyun_svideo_music,
                R.mipmap.alivc_svideo_effect,
                R.mipmap.aliyun_svideo_filter,
                R.mipmap.aliyun_svideo_overlay,
                R.mipmap.aliyun_svideo_caption,
                R.mipmap.aliyun_svideo_mv,
                R.mipmap.aliyun_svideo_sound,//音效, 暂用mv icon
                R.mipmap.aliyun_svideo_time,
                R.mipmap.aliyun_svideo_transition,
                R.mipmap.aliyun_svideo_paint,
                R.mipmap.aliyun_svideo_cover,
                R.mipmap.aliyun_svideo_augmentation,
                R.mipmap.aliyun_svideo_caption

        };
        UIConfigManager.setImageResourceConfig(textViews, index, attrs, defaultResourceIds);

        trimLayout = findViewById(R.id.trimLayout);
        FrameLayout captionLayout = findViewById(R.id.captionLayout);

        //控制底部菜单显示内容
        int[] bottomItemMenuVisibleTags = getContext().getResources().getIntArray(R.array.bottomItemMenuVisibleTags);
        for (int i = 0; i < textViews.length; i++) {
            if (bottomItemMenuVisibleTags[i] == 0) {
                textViews[i].setVisibility(GONE);
                if (i == 0) {
                    trimLayout.setVisibility(GONE);
                }
                if (i == 1) {
                    captionLayout.setVisibility(GONE);
                }
            } else {
                textViews[i].setVisibility(VISIBLE);
                if (i == 0) {
                    trimLayout.setVisibility(VISIBLE);
                }
                if (i == 1) {
                    captionLayout.setVisibility(VISIBLE);
                }
            }
        }
        trimLayout.setVisibility(isShowTrim ? VISIBLE : GONE);
    }

    FrameLayout trimLayout;
    private boolean isShowTrim = true;

    public void setShowTrim(boolean isShow) {
        isShowTrim = isShow;
        if (trimLayout != null) {
            trimLayout.setVisibility(isShowTrim ? VISIBLE : GONE);
        }
    }

    public OverlayThumbLineBar getThumbLineBar() {
        return mThumbLineBar;
    }

    private void initGlSurfaceView() {
        if (mVideoParam == null) {
            return;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mGlSurfaceContainer.getLayoutParams();
        FrameLayout.LayoutParams surfaceLayout = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        int outputWidth = mVideoParam.getOutputWidth();
        int outputHeight = mVideoParam.getOutputHeight();

        /*
          指定surfaceView的宽高比是有必要的，这样可以避免某些非标分辨率下造成显示比例不对的问题
         */
        surfaceLayout.width = mScreenWidth;
        surfaceLayout.height = Math.round((float) outputHeight * mScreenWidth / outputWidth);
        mPasterContainerPoint = new Point(surfaceLayout.width, surfaceLayout.height);

        if (mVideoRotation == 90 || mVideoRotation == 270) {
            surfaceLayout.topMargin = mScreenHeight / 2 - surfaceLayout.height / 2;
//            //横屏
//            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//            surfaceLayout.bottomMargin = (mScreenHeight - surfaceLayout.height) / 4;
        } else {
//            surfaceLayout.topMargin = (mScreenHeight - surfaceLayout.height)/4;
        }
        mGlSurfaceContainer.setLayoutParams(layoutParams);
        mPasterContainer.setLayoutParams(surfaceLayout);
        mSurfaceView.setLayoutParams(surfaceLayout);
    }

    public float dip2px(Context paramContext, float paramFloat) {
        return 0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density;
    }

    private void initListView() {
        mViewOperate = new ViewOperator(this, mActionBar, llVerticalTitleLayout, mSurfaceView, mBottomLinear, mPasterContainer, mPlayImage);
        mViewOperate.setVideoRotation(mVideoRotation);
        mViewOperate.setAnimatorListener(new ViewOperator.AnimatorListener() {
            @Override
            public void onShowAnimationEnd() {
                UIEditorPage index = UIEditorPage.get(mTabGroup.getCheckedIndex());
                switch (index) {
                    case PAINT:
                        //2018/8/30 添加涂鸦画布
                        if (mCanvasController == null) {
                            int width = mPasterContainer.getLayoutParams().width;
                            int height = mPasterContainer.getLayoutParams().height;
                            mCanvasController = mAliyunIEditor.obtainCanvasController(getContext(),
                                    width, height);
                            mCanvasController.setCurrentSize(dip2px(getContext(), 5));
                        }

                        mCanvasController.removeCanvas();
                        View canvasView = mCanvasController.getCanvas();
                        mPasterContainer.removeView(canvasView);
                        mPasterContainer.addView(canvasView, mPasterContainer.getWidth(), mPasterContainer.getHeight());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onHideAnimationEnd() {
                if (isTakeFrameSelected) {
                    isTakeFrame = true;
                    //继续播放保证截图
                    playingResume();
                    //播放按钮变为可见状态
                    mPlayImage.setVisibility(VISIBLE);
                    isTakeFrameSelected = false;
                }
            }
        });
        mTvRight.setVisibility(View.VISIBLE);
        mEditorService = new EditorService();
        mTabGroup = new TabGroup();
        mViewStack = new ViewStack(getContext(), this, mViewOperate);
        mViewStack.setActivity(mActivity);
        mViewStack.setEditorService(mEditorService);
        mViewStack.setEffectChange(this);
        mViewStack.setOnEffectActionLister(mOnEffectActionLister);
        mViewStack.setOnTransitionPreviewListener(mOnTransitionPreviewListener);
        mTabGroup.addView(findViewById(R.id.tab_trim));
        mTabGroup.addView(findViewById(R.id.tab_effect_caption));
        mTabGroup.addView(findViewById(R.id.tab_effect_audio_mix));
        mTabGroup.addView(findViewById(R.id.tab_effect_filter));
        mTabGroup.addView(findViewById(R.id.tab_filter));
        mTabGroup.addView(findViewById(R.id.tab_effect_overlay));
        mTabGroup.addView(findViewById(R.id.tab_effect_text));
        mTabGroup.addView(findViewById(R.id.tab_effect_mv));
        mTabGroup.addView(findViewById(R.id.tab_effect_sound));
        mTabGroup.addView(findViewById(R.id.tab_effect_time));
        mTabGroup.addView(findViewById(R.id.tab_effect_transition));
        mTabGroup.addView(findViewById(R.id.tab_paint));
        mTabGroup.addView(findViewById(R.id.tab_cover));
        mTabGroup.addView(findViewById(R.id.tab_videoeq));
        mTabGroup.addView(findViewById(R.id.tab_roll_caption));

    }

    private void add2Control() {
        TabViewStackBinding tabViewStackBinding = new TabViewStackBinding();
        tabViewStackBinding.setViewStack(mViewStack);
        mTabGroup.setOnCheckedChangeListener(tabViewStackBinding);
        mTabGroup.setOnTabChangeListener(this);
    }

    private void initEditor() {
        //设置onTextureRender能够回调
        mEditorCallback.mNeedRenderCallback = EditorCallBack.RENDER_CALLBACK_TEXTURE;
        mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(mUri, mEditorCallback);

        mViewStack.setAliyunIEditor(mAliyunIEditor);

        mAliyunRollCaptionComposer = mAliyunIEditor.createRollCaptionComposer();
        if (mViewStack != null) {
            mViewStack.setAliyunRollCaptionComposer(mAliyunRollCaptionComposer);
        }
        initGlSurfaceView();
        {
            //该代码块中的操作必须在AliyunIEditor.init之前调用，否则会出现动图、动效滤镜的UI恢复回调不执行，开发者将无法恢复动图、动效滤镜UI
            mPasterManager = mAliyunIEditor.createPasterManager();
            FrameLayout.LayoutParams surfaceLayout = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
            /*
              指定显示区域大小后必须调用mPasterManager.setDisplaySize，否则将无法添加和恢复一些需要提前获知区域大小的资源，如字幕，动图等
              如果开发者的布局使用了wrapContent或者matchParent之类的布局，务必获取到view的真实宽高之后在调用
             */
            try {
                mPasterManager.setDisplaySize(surfaceLayout.width, surfaceLayout.height);
            } catch (Exception e) {
                showToast = FixedToastUtils.show(getContext(), e.getMessage());
                ((Activity) getContext()).finish();
                return;
            }
            mPasterManager.setOnPasterRestoreListener(mOnPasterRestoreListener);
            mAnimationFilterController = new AnimationFilterController(getContext().getApplicationContext(),
                    mAliyunIEditor);
            mAliyunIEditor.setAnimationRestoredListener(AlivcEditView.this);
        }

        mTranscoder = AliyunCropCreator.createCropInstance(getContext());
        VideoDisplayMode mode = mVideoParam.getScaleMode();
        int ret = mAliyunIEditor.init(mSurfaceView, getContext().getApplicationContext());
        mAliyunIEditor.setDisplayMode(mode);
        mAliyunIEditor.setVolume(mVolume);
        mAliyunIEditor.setFillBackgroundColor(Color.BLACK);
        List<AliyunClip> clips = mAliyunIEditor.getSourcePartManager().getAllClips();
        mAliyunIEditor.denoise(clips.get(0).getId(), mHasDeNoise);
        if (ret != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            showToast = FixedToastUtils.show(getContext(),
                    getResources().getString(R.string.alivc_editor_edit_tip_init_failed));
            ((Activity) getContext()).finish();
            return;
        }
        mEditorService.addTabEffect(UIEditorPage.MV, mAliyunIEditor.getMVLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.FILTER_EFFECT, mAliyunIEditor.getFilterLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX, mAliyunIEditor.getMusicLastApplyId());
        mEditorService.setPaint(mAliyunIEditor.getPaintLastApply());

        mTvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
//                for (PasterUICaptionsImpl uiCaptions : mCaptionsUiList) {
//                    if (uiCaptions != null) {
//                        uiCaptions.mController.editStart();
//                        uiCaptions.mController.editCompleted();
//                    }
//                }
//                mAliYunClipList = EditorVideHelper.setVideoTimes(mAliyunIEditor, mViewStack.getVideoStartTime(), mViewStack.getVideoEndTime());
//                //重新设置转场动画。
//                if (mTransitionBaseMap != null) {
//                    mAliyunIEditor.setTransition(mTransitionBaseMap);
//                }

                if (FastClickUtil.isFastClickActivity(EditorActivity.class.getSimpleName())) {
                    return;
                }
                mTvRight.setEnabled(false);
                //合成方式分为两种，当前页面合成（前台页面）和其他页面合成（后台合成，这里后台并不是真正的app退到后台）
                //前台合成如下：如果要直接合成（当前页面合成），请打开注释，参考注释代码这种方式
                //                int ret = mAliyunIEditor.compose(mVideoParam, "/sdcard/output_compose.mp4", new
                // AliyunIComposeCallBack() {
                //                    @Override
                //                    public void onComposeError(int errorCode) {
                //                        runOnUiThread(new Runnable() {
                //                            @Override
                //                            public void run() {
                //                                v.setEnabled(true);
                //                            }
                //                        });
                //
                //                        Log.d(AliyunTag.TAG, "Compose error, error code "+errorCode);
                //                    }
                //
                //                    @Override
                //                    public void onComposeProgress(int progress) {
                //                        Log.d(AliyunTag.TAG, "Compose progress "+progress+"%");
                //                    }
                //
                //                    @Override
                //                    public void onComposeCompleted() {
                //                        runOnUiThread(new Runnable() {
                //                            @Override
                //                            public void run() {
                //                                v.setEnabled(true);
                //                            }
                //                        });
                //                        Log.d(AliyunTag.TAG, "Compose complete");
                //                    }
                //                });
                //                if(ret != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
                //                    Log.e(AliyunTag.TAG, "Compose error, error code "+ret);
                //                    v.setEnabled(true);//compose error
                //                }

                //后台合成如下：如果要像Demo默认的这样，在其他页面合成，请参考下面这种方式
                mAliyunIEditor.saveEffectToLocal();
                //已经选择封面，并且封面尚未生成的过程不允许跳转
                if (hasCaptureCover && mSnapshop.isSnapshotting()) {
                    alivcEditHandler.sendEmptyMessageDelayed(SAVE_COVER, 500);
                    if (mTransitionAnimation == null) {
                        //转场animation
                        mTransitionAnimation = new AlivcCircleLoadingDialog(getContext(), mPasterContainer.getHeight());
                    }
                    mTransitionAnimation.show();
                    return;
                }

                //封面选择，如果剪裁了视频。必须重新获取封面
                long thumbatilTime = 0;
                if (mViewStack != null) {
                    thumbatilTime = mViewStack.getVideoStartTime() / 1000;
                }

                jumpToNextActivity();

            }
        });

        mPlayerListener = new PlayerListener() {

            @Override
            public long getCurrDuration() {
                return mAliyunIEditor.getCurrentPlayPosition();
            }

            @Override
            public long getDuration() {
                long streamDuration = mAliyunIEditor.getStreamDuration();
                Log.d(TAG, "getDuration: " + streamDuration);
                return streamDuration;
            }

            @Override
            public void updateDuration(long duration) {
                mTvCurrTime.setText(convertDuration2Text(duration));
            }

            @Override
            public void onPlayPause() {
                playingPause();
            }
        };

        mViewStack.setPlayerListener(mPlayerListener);
        //配置缩略图滑动条
        initThumbLineBar();
        //非编辑态隐藏
        mThumbLineBar.hide();
        File mWatermarkFile = new File(
                getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png");
        if (mWatermarkFile.exists()) {
            if (mWatermarkBitmap == null || mWatermarkBitmap.isRecycled()) {
                mWatermarkBitmap = BitmapFactory.decodeFile(
                        getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png");
            }
            mSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    int outputWidth = mVideoParam.getOutputWidth();
                    int outputHeight = mVideoParam.getOutputHeight();
                    int mWatermarkBitmapWidth = DensityUtils.dip2px(getContext(), 30);
                    int mWatermarkBitmapHeight = DensityUtils.dip2px(getContext(), 30);
                    if (mWatermarkBitmap != null && !mWatermarkBitmap.isRecycled()) {
                        mWatermarkBitmapWidth = mWatermarkBitmap.getWidth();
                        mWatermarkBitmapHeight = mWatermarkBitmap.getHeight();
                    }
                    float posY = 0;
                    float percent = (float) outputHeight / outputWidth;
                    if (percent > 1.5) {
                        posY = 0f
                                + (float) (mWatermarkBitmapHeight / 2 + getContext().getResources().getDimensionPixelSize(
                                R.dimen.alivc_svideo_title_height)) / 1.5f / mSurfaceView.getHeight();
                    } else {
                        posY = 0f + (float) mWatermarkBitmapHeight / 1.5f / mSurfaceView.getHeight() / 2;
                    }
                    /**
                     * 水印例子 水印的大小为 ：水印图片的宽高和显示区域的宽高比，注意保持图片的比例，不然显示不完全
                     * 水印的位置为 ：以水印图片中心点为基准，显示区域宽高的比例为偏移量，0,0为左上角，1,1为右下角
                     *
                     */
                    if (hasWaterMark) {
                        mAliyunIEditor.applyWaterMark(
                                getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png",
                                (float) mWatermarkBitmapWidth * 0.5f * 0.8f / mSurfaceView.getWidth(),
                                (float) mWatermarkBitmapHeight * 0.5f * 0.8f / mSurfaceView.getHeight(),
                                (float) mWatermarkBitmapWidth / 1.5f / mSurfaceView.getWidth() / 2,
                                posY);
                    }
                    //旋转水印
                    //ActionRotate actionRotate = new ActionRotate();
                    //actionRotate.setStartTime(0);
                    //actionRotate.setTargetId(id);
                    //actionRotate.setDuration(10 * 1000 * 1000);
                    //actionRotate.setRepeat(true);
                    //actionRotate.setDurationPerCircle(3 * 1000 * 1000);
                    //mAliyunIEditor.addFrameAnimation(actionRotate);


//                    if (hasWaterMark) {
//                        //图片水印
//                        EffectPicture effectPicture = new EffectPicture(getContext().getExternalFilesDir("")+ File.separator + "/AliyunEditorDemo/tail/logo.png");
//                        effectPicture.x = 0.12f;
//                        effectPicture.y = 0.1f;
//                        effectPicture.width = (float) mWatermarkBitmapWidth * 0.5f * 0.8f / mSurfaceView.getWidth();
//                        effectPicture.height = (float) mWatermarkBitmapHeight * 0.5f * 0.8f / mSurfaceView.getHeight();
//                        effectPicture.start = 0;
//                        effectPicture.end = mAliyunIEditor.getDuration() + 1000;
//                        mAliyunIEditor.addImage(effectPicture);
//
//                        ActionBase mActionBase = new ActionTranslate();
//                        ((ActionTranslate) mActionBase).setToPointX(1f);
//                        mActionBase.setStartTime(0);
//                        mActionBase.setDuration(1000 * 1000);
//                        mActionBase.setTargetId(effectPicture.getViewId());
//                        setTranslateParams(mWatermarkBitmapWidth,mWatermarkBitmapHeight,mActionBase);
//                        mAliyunIEditor.addFrameAnimation(mActionBase);
//                    }


//                    ActionRotate actionRotateImg = new ActionRotate();
//                    actionRotateImg.setStartTime(0);
//                    actionRotateImg.setTargetId(effectPicture.getViewId());
//                    actionRotateImg.setDuration(2 * 1000 * 1000);
//                    actionRotateImg.setRepeat(true);
//                    actionRotateImg.setDurationPerCircle(3 * 1000 * 1000);

                    if (hasTailAnimation) {
                        //片尾水印
                        mAliyunIEditor.addTailWaterMark(
                                getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png",
                                (float) mWatermarkBitmapWidth / mSurfaceView.getWidth(),
                                (float) mWatermarkBitmapHeight / mSurfaceView.getHeight(), 0.5f, 0.5f, 2000 * 1000);
                    }

                }
            });
        }

        mAliyunIEditor.play();


//        List<AliyunClip> clips = mAliyunIEditor.getSourcePartManager().getAllClips();
//
////0-3s模糊
//        mAliyunIEditor.applyBlurBackground(clips.get(0).getId(), 0, 3000, 10f);
//
////0-3s进行 0.5x -> 1.5x  的放大
//        ActionScale scaleAction = new ActionScale();
//        scaleAction.setTargetId(clips.get(0).getId());
//        scaleAction.setStartTime(0);
//        scaleAction.setDuration(3000 * 1000);
//        scaleAction.setFromScale(0.5f);
//        scaleAction.setToScale(1f);
//        scaleAction.setIsStream(true);
//        mAliyunIEditor.addFrameAnimation(scaleAction);
    }


    /**
     * @param actionBase ActionBase
     */
    private void setTranslateParams(int width, int height, ActionBase actionBase) {
        ActionTranslate actionTranslate = (ActionTranslate) actionBase;
        float x = -0.1f;
        float y = 1 - 0.12f;
        //入场1s结束
        actionTranslate.setToPointX(x);
        actionTranslate.setToPointY(y);
        //向右平移
        actionTranslate.setFromPointY(y);
        actionTranslate.setFromPointX(-1);
    }

    /**
     * 配置新的缩略条
     */
    private void initThumbLineBar() {
        //获取每张缩略图的尺寸
        int thumbnailSize = getResources().getDimensionPixelOffset(R.dimen.aliyun_editor_size_square_thumbnail);
        Point thumbnailPoint = new Point(thumbnailSize, thumbnailSize);

        //缩略图获取
//        if (mThumbnailFetcher == null) {
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.fromConfigJson(mUri.getPath());
//        } else if (mThumbnailFetcher.getTotalDuration() != mAliyunIEditor.getStreamDuration() / 1000) {
//            //时长改变的时候才去修改缩略图
//            Log.i(TAG, "initThumbLineBar: reset thumbLine");
//            mAliyunIEditor.saveEffectToLocal();
//            mThumbnailFetcher.release();
//            mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
//            mThumbnailFetcher.fromConfigJson(mUri.getPath());
//        }

        //设置缩略条配置文件
        ThumbLineConfig thumbLineConfig = new ThumbLineConfig.Builder()
                .thumbnailFetcher(mThumbnailFetcher)
                .uri(mUri)
                .startTime(mStartTime)
                .endTime(mEndTime)
                .screenWidth(mScreenWidth)
                .thumbPoint(thumbnailPoint)
                .thumbnailCount(10).build();

        if (mThumbLineBar == null) {
            mThumbLineBar = findViewById(R.id.simplethumblinebar);

            mBarSeekListener = new ThumbLineBar.OnBarSeekListener() {

                @Override
                public void onThumbLineBarSeek(long duration) {
                    mAliyunIEditor.seek(duration);
                    if (mThumbLineBar != null) {
                        mThumbLineBar.pause();
                    }
                    switchPlayStateUI(true);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        if (!mCurrentEditEffect.isVisibleInTime(duration)) {
                            //隐藏
                            mCurrentEditEffect.mPasterView.setVisibility(View.GONE);
                        } else {
                            //显示
                            mCurrentEditEffect.mPasterView.setVisibility(View.VISIBLE);
                        }
                    }
                    if (mUseInvert) {
                        //当seek到最后时，不允许添加特效
                        if (duration <= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        } else {
                            mCanAddAnimation = true;
                        }
                    } else {
                        //当seek到最后时，不允许添加特效
                        if (mAliyunIEditor.getDuration() - duration <= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        } else {
                            mCanAddAnimation = true;
                        }
                    }

                }

                @Override
                public void onThumbLineBarSeekFinish(long duration) {
                    mAliyunIEditor.seek(duration);
                    if (mThumbLineBar != null) {
                        mThumbLineBar.pause();
                    }
                    switchPlayStateUI(true);
                    if (mUseInvert) {
                        //当seek到最后时，不允许添加特效
                        if (duration <= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        } else {
                            mCanAddAnimation = true;
                        }
                    } else {
                        //当seek到最后时，不允许添加特效
                        if (mAliyunIEditor.getDuration() - duration >= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = true;
                        } else {
                            mCanAddAnimation = false;
                        }
                    }
                }
            };

            //Overlay相关View
            mThumbLineOverlayView = new ThumbLineOverlay.ThumbLineOverlayView() {
                View rootView = LayoutInflater.from(getContext()).inflate(
                        R.layout.alivc_editor_view_timeline_overlay, null);
                View headView = rootView.findViewById(R.id.head_view);
                View tailView = rootView.findViewById(R.id.tail_view);
                View middleView = rootView.findViewById(R.id.middle_view);

                @Override
                public ViewGroup getContainer() {
                    return (ViewGroup) rootView;
                }

                @Override
                public View getHeadView() {
                    return headView;
                }

                @Override
                public View getTailView() {
                    return tailView;
                }

                @Override
                public View getMiddleView() {
                    return middleView;
                }
            };

        }

        mThumbLineBar.setup(thumbLineConfig, mBarSeekListener, mPlayerListener, mStartTime, mEndTime);

    }

    /**
     * 更改播放状态的图标和文字 播放时,文字内容显示为: 暂停播放, 图标使暂停图标, mipmap/aliyun_svideo_pause 暂停时,文字内容显示为: 播放全篇, 图标使用播放图标,
     * mipmap/aliyun_svideo_play
     *
     * @param changeState, 需要显示的状态,  true: 播放全篇, false: 暂停播放
     */
    public void switchPlayStateUI(boolean changeState) {
        if (changeState) {
            mPlayImage.setText(getResources().getString(R.string.alivc_editor_edit_play_start));
            UIConfigManager.setImageResourceConfig(mPlayImage, 0, R.attr.playImage, R.mipmap.aliyun_svideo_play);
        } else {
            mPlayImage.setText(getResources().getString(R.string.alivc_editor_edit_play_pause));
            UIConfigManager.setImageResourceConfig(mPlayImage, 0, R.attr.pauseImage, R.mipmap.aliyun_svideo_pause);
        }
    }


    //3.22.0
    private final OnPasterRestored mOnPasterRestoreListener = new OnPasterRestored() {

        @Override
        public void onPasterRestored(final List<AliyunPasterAbstractController> controllers) {

            Log.d(TAG, "onPasterRestored: " + controllers.size());


            mPasterContainer.post(new Runnable() {//之所以要放在这里面，是因为下面的操作中有UI相关的，需要保证布局完成后执行，才能保证UI更新的正确性
                @Override
                public void run() {

                    if (mThumbLineBar != null && mThumbLineBar.getChildCount() != 0) {
                        //这里做合成（时间和转场特效会清空paster特效）恢复 针对缩略图的覆盖效果
                        mThumbLineBar.removeOverlayByPages(
                                UIEditorPage.CAPTION,
                                UIEditorPage.TEXT,
                                UIEditorPage.FONT,
                                UIEditorPage.OVERLAY
                        );
                    }

                    if (mPasterContainer != null) {
                        mPasterContainer.removeAllViews();
                    }
                    final List<AbstractPasterUISimpleImpl> aps = new ArrayList<>();
//                    for (AliyunPasterController c : controllers) {
                    for (AliyunPasterAbstractController controller : controllers) {
                        if (controller instanceof AliyunPasterController) {
                            AliyunPasterController c = (AliyunPasterController) controller;
                            if (!c.isPasterExists()) {
                                continue;
                            }
                            if (c.getPasterStartTime() >= mAliyunIEditor.getStreamDuration()) {
                                //恢复时覆盖超出缩略图,丢弃
                                continue;
                            }
                            c.setOnlyApplyUI(true);
                            if (c.getPasterType() == EffectPaster.PASTER_TYPE_GIF) {
                                mCurrentEditEffect = addPaster(c);
                            } else if (c.getPasterType() == EffectPaster.PASTER_TYPE_TEXT) {
                                mCurrentEditEffect = addSubtitle(c, true);
                            } else if (c.getPasterType() == EffectPaster.PASTER_TYPE_CAPTION) {
                                mCurrentEditEffect = addCaption(c);
                            }

                            mCurrentEditEffect.showTimeEdit();
                            mCurrentEditEffect.getPasterView().setVisibility(View.INVISIBLE);
                            aps.add(mCurrentEditEffect);
                            mCurrentEditEffect.moveToCenter();
                            mCurrentEditEffect.hideOverlayView();

                        }

                        for (AbstractPasterUISimpleImpl pui : aps) {
                            pui.editTimeCompleted();
                            pui.getController().setOnlyApplyUI(false);
                        }
                    }
                }
            });
        }

    };


    private List<PasterUICaptionsImpl> mCaptionsUiList = new ArrayList<>();

    //动态管理类，只用于清空动图
    private List<PasterUIGifImpl> mGifUiList = new ArrayList<>();


    public boolean isShowEditCaptions;

    public List<PasterUICaptionsImpl> getCaptionsUiList() {
        return mCaptionsUiList;
    }


    /**
     * 字幕双击处理
     */
    public void onCaptionDoubleClick(int position, long positionTime) {
//        if (mCurrentEditEffect instanceof PasterUICaptionsImpl) {
//            PasterUICaptionsImpl captions = (PasterUICaptionsImpl) mCurrentEditEffect;
//            if (captions.isEditShow() && captions.getCurrentPosition() == position) {
//                //双击的是当前字幕，
//                mCurrentEditEffect.showTextEdit(mUseInvert);
//                return;
//            }
//        }
        if (!mCaptionsUiList.isEmpty() && position >= 0 && position < mCaptionsUiList.size()) {
            PasterUICaptionsImpl captions = mCaptionsUiList.get(position);
            captions.showTextEdit(mUseInvert);
            mCurrentEditEffect = captions;
        }

    }

    /**
     * 进入字幕编辑时，将之前保存的字幕显示到编辑页面
     */
    public void setCaptionEdit() {
        for (PasterUICaptionsImpl uiCaptions : mCaptionsUiList) {
            PasterUICaptionsImpl uiCaptionView = uiCaptions.initShow(mAliyunIEditor.getCurrentPlayPosition());
            if (uiCaptionView != null) {
                mCurrentEditEffect = uiCaptionView;
            }
        }
    }

    @Override
    public void onChangeTime(int position, long startTime, long endTime) {
        if (mCaptionsUiList.size() > position) {
            mCaptionsUiList.get(position).setStartEndTime(startTime, endTime);
            long currentPlayTime = mAliyunIEditor.getCurrentPlayPosition();
            for (PasterUICaptionsImpl uiCaptions : mCaptionsUiList) {
                boolean isShow = uiCaptions.resetShow(currentPlayTime);
                if (isShow) {
                    mCurrentEditEffect = uiCaptions;
                }
            }
        }
    }

    @Override
    public void onEffectChange(final EffectInfo effectInfo) {
        //返回素材属性
        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        effect.setPath(effectInfo.getPath());
        UIEditorPage type = effectInfo.type;
        if (type == UIEditorPage.AUDIO_MIX || type == UIEditorPage.FILTER || type == UIEditorPage.MV || type == UIEditorPage.SOUND) {
            isChangeEffect = true;
        }
        switch (type) {
            case CAPTION:
                int effectTextPosition = effectInfo.effectTextPosition;
                mAliyunPasterController = mPasterManager.addSubtitleWithStartTime(effectInfo.effectText, effectInfo.fontPath + "/font.ttf", effectInfo.startTime, effectInfo.endTime - effectInfo.startTime);
                if (mAliyunPasterController != null) {
                    AliyunPasterWithTextView captionView = (AliyunPasterWithTextView) View.inflate(getContext(), R.layout.alivc_editor_view_paster_text, null);
                    mPasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    PasterUICaptionsImpl uiCaptions = new PasterUICaptionsImpl(captionView, mAliyunPasterController, mThumbLineBar, mAliyunIEditor, false);
                    uiCaptions.setCurrentPlayTime(mAliyunIEditor.getCurrentPlayPosition());
                    uiCaptions.setCurrentPosition(effectTextPosition);
                    uiCaptions.showTextEdit2(mContext, mUseInvert, effectInfo.effectText, mPasterContainer.getMeasuredWidth());
                    uiCaptions.setOnEditChangerListener(new AbstractPasterUISimpleImpl.OnEditChangerListener() {
                        @Override
                        public void onChanger(String result) {
                            int position = effectInfo.effectTextPosition;
                            if (mViewStack != null && mViewStack.mCaptionLayout != null) {
                                mViewStack.mCaptionLayout.setBlessingChanger(position, result);
                            }
                        }
                    });
                    mCaptionsUiList.add(uiCaptions);
                    playingPause();
                } else {
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_word_fail));
                }
                break;
            case TRIM:
                break;
            case AUDIO_MIX://
                if (!effectInfo.isAudioMixBar) {
                    //重制mv和混音的音效
                    mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MIX);
                    mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MV_AUDIO);
                    if (lastMusicBean != null) {
                        mAliyunIEditor.removeMusic(lastMusicBean);
                    }
                    lastMusicBean = new EffectBean();
                    lastMusicBean.setId(effectInfo.id);
                    lastMusicBean.setSource(effectInfo.getSource());

                    if (lastMusicBean.getSource() != null) {
                        //切换音乐seek到0清音乐缓存，避免响一声
                        lastMusicBean.setStartTime(effectInfo.startTime * 1000);//单位是us所以要x1000
                        //lastMusicBean.setDuration(effectInfo.endTime == 0 ? Integer.MAX_VALUE
                        //                          : (effectInfo.endTime - effectInfo.startTime) * 1000);//单位是us所以要x1000
                        lastMusicBean.setDuration(Integer.MAX_VALUE);//设置为最大时长
                        lastMusicBean.setStreamStartTime(effectInfo.streamStartTime * 1000);
                        lastMusicBean.setWeight(effectInfo.musicWeight);
                        lastMusicBean.setStreamDuration(
                                (effectInfo.streamEndTime - effectInfo.streamStartTime) * 1000);//单位是us所以要x1000
                        effectInfo.mixId = mAliyunIEditor.applyMusic(lastMusicBean);
//                        lastMusicBean.setWeight(bgmWeight);
                    } else {
                        //恢复mv声音
                        if (mLastMVEffect != null && !TextUtils.isEmpty(mLastMVEffect.getPath())) {
                            applyMVEffect(mLastMVEffect);
                        }
                    }
                } else {
                    effectInfo.mixId = mAliyunIEditor.getMusicLastApplyId();
                }
                if (isReplaceMusic) {
                    mAliyunIEditor.applyMusicMixWeight(effectInfo.mixId, 100);
                } else {
                    mAliyunIEditor.applyMusicMixWeight(effectInfo.mixId, effectInfo.musicWeight);
                }
                mAliyunIEditor.setVolume(mVolume);
                mAliyunIEditor.seek(0);
                // 重新播放
                playingResume();
                break;
            case FILTER_EFFECT:
                if (effect.getSource() != null && effect.getSource().getPath().contains("Vertigo")) {
                    EffectFilter filter = new EffectFilter(effect.getSource());
                    mAliyunIEditor.addAnimationFilter(filter);
                } else {
                    mAliyunIEditor.applyFilter(effect);
                }
                break;

            case SOUND:
                // 音效

                List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
                int size = allClips.size();
                for (int i = 0; i < size; i++) {
                    if (mLastSoundEffect != null) {
                        mAliyunIEditor.removeAudioEffect(allClips.get(i).getId(), mLastSoundEffect.audioEffectType);
                    }
                    mAliyunIEditor.audioEffect(allClips.get(i).getId(), effectInfo.audioEffectType, effectInfo.soundWeight);
                }
                mLastSoundEffect = effectInfo;
                Log.i("log_editor_sound_type", String.valueOf(effectInfo.audioEffectType));
                mAliyunIEditor.seek(0);
//                if(mViewStack!=null){
//                    mAliyunIEditor.seek(mViewStack.getVideoStartTime());
//                }
                mAliyunIEditor.play();
                switchPlayStateUI(false);

                break;
            case MV:
                //保存最后一次应用的MV，用于音乐选择无的时候恢复MV的声音
                mLastMVEffect = effectInfo;
                applyMVEffect(effectInfo);

                break;
            case TEXT:
                mAliyunPasterController = mPasterManager.addPaster(effectInfo.getPath());

                if (mAliyunPasterController != null) {
                    //获取字幕中的字体
                    EffectBase effectBase = mAliyunPasterController.getEffect();
                    if (effectBase instanceof EffectCaption) {
                        ((EffectCaption) effectBase).font = effectInfo.fontPath + "/font.ttf";
                        mAliyunPasterController.setEffect(effectBase);
                    }

                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getCurrentStreamPosition());
                    PasterUICaptionImpl cui = addCaption(mAliyunPasterController);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        //如果有正在编辑的paster，之前的remove
                        mCurrentEditEffect.removePaster();
                    }
                    playingPause();
                    mCurrentEditEffect = cui;
                    mCurrentEditEffect.showTimeEdit();
//              气泡字幕默认不弹出输入法
//                cui.showTextEdit(mUseInvert);
                } else {
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_captions_fail));
                }
                break;
            case OVERLAY:
                mAliyunPasterController = mPasterManager.addPaster(effectInfo.getPath());
                if (mAliyunPasterController != null) {
                    //add success
                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getCurrentStreamPosition());
                    PasterUIGifImpl gifui = addPaster(mAliyunPasterController);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        //如果有正在编辑的paster，之前的remove
                        mCurrentEditEffect.removePaster();
                    }
                    playingPause();
                    mCurrentEditEffect = gifui;
                    mGifUiList.add(gifui);
                    mCurrentEditEffect.showTimeEdit();
                } else {
                    //add failed
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_gif_fail));
                }

                break;
            case FONT:
                mAliyunPasterController = mPasterManager.addSubtitle(null, effectInfo.fontPath + "/font.ttf");
                if (mAliyunPasterController != null) {
                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getCurrentStreamPosition());
                    PasterUITextImpl textui = addSubtitle(mAliyunPasterController, false);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        //如果有正在编辑的paster，之前的remove
                        mCurrentEditEffect.removePaster();
                    }
                    playingPause();
                    mCurrentEditEffect = textui;
                    mCurrentEditEffect.showTimeEdit();
                    textui.showTextEdit(mUseInvert);
                } else {
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_word_fail));
                }
                //                mCurrentEditEffect.setImageView((ImageView) findViewById(R.id.test_image));

                break;
            case TIME:
                if (effectInfo.startTime < 0) {
                    effectInfo.startTime = mAliyunIEditor.getCurrentStreamPosition();
                }
                if (mIsTranscoding) {
                    showToast = FixedToastUtils.show(getContext(),
                            getResources().getString(R.string.alivc_editor_edit_tip_transcode_no_operate));
                    return;
                }
//            当前有动画效果时，执行倒播，增加提示信息
                if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_INVERT) && mCurrentEditEffect != null && mCurrentEditEffect.isAddedAnimation()) {
                    FixedToastUtils.show(getContext(), getContext().getString(R.string.alivc_editor_dialog_caption_tip_not_support));
                }
                applyTimeEffect(effectInfo);
                break;
            case TRANSITION:
                if (effectInfo.isUpdateTransition) {
//                保存转场效果，只保存第一次获取到的值
                    if (mTransitionCache == null) {
                        mTransitionCache = new LinkedHashMap<>();
                    }
                    if (mTransitionParamsCache == null) {
                        mTransitionParamsCache = new LinkedHashMap<>();
                    }
                    if (mTransitionCache != null && mTransitionCache.get(effectInfo.clipIndex) == null) {
                        mTransitionCache.put(effectInfo.clipIndex, effectInfo);
                        List<EffectConfig.NodeBean> nodeTree = effectInfo.transitionBase.getNodeTree();
                        List<AlivcTransBean> paramsList = new ArrayList<>();
                        if (nodeTree == null || nodeTree.size() == 0) {
                            return;
                        }
                        for (EffectConfig.NodeBean nodeBean : nodeTree) {
                            List<EffectConfig.NodeBean.Params> params = nodeBean.getParams();
                            if (params == null || params.size() == 0) {
                                continue;
                            }
                            for (EffectConfig.NodeBean.Params param : params) {
                                ValueTypeEnum valueTypeEnum = param.getType();
                                if (valueTypeEnum == ValueTypeEnum.INT) {
                                    AlivcTransBean alivcTransBean = new AlivcTransBean();
                                    alivcTransBean.setmType(valueTypeEnum);
                                    if (param.getValue().getValue() != null && param.getValue().getValue().length > 0) {
                                        alivcTransBean.setmIntergerValue((int) param.getValue().getValue()[0]);
                                    }
                                    paramsList.add(alivcTransBean);
                                } else if (valueTypeEnum == ValueTypeEnum.FLOAT) {
                                    AlivcTransBean alivcTransBean = new AlivcTransBean();
                                    alivcTransBean.setmType(valueTypeEnum);
                                    if (param.getValue().getValue() != null && param.getValue().getValue().length > 0) {
                                        alivcTransBean.setmFloatValue((float) param.getValue().getValue()[0]);
                                    }
                                    paramsList.add(alivcTransBean);
                                } else {
                                    AlivcTransBean alivcTransBean = new AlivcTransBean();
                                    alivcTransBean.setmType(valueTypeEnum);
                                    paramsList.add(alivcTransBean);
                                }
                            }
                        }
                        mTransitionParamsCache.put(effectInfo.clipIndex, paramsList);
                    }
//                更新转场
                    effectInfo.isUpdateTransition = false;
                    mAliyunIEditor.updateTransition(effectInfo.clipIndex, effectInfo.transitionBase);
                    mTransitionBaseMap.put(effectInfo.clipIndex, effectInfo.transitionBase);
                } else {
                    setTransition(effectInfo);
                }

                break;
            case ROLL_CAPTION:
                mAliyunIEditor.seek(0);
//                if(mViewStack!=null){
//                    mAliyunIEditor.seek(mViewStack.getVideoStartTime());
//                }
                mAliyunIEditor.play();
                switchPlayStateUI(false);
                break;
            default:
                break;
        }
    }

    /**
     * 应用MV特效
     *
     * @param effectInfo
     */
    private void applyMVEffect(EffectInfo effectInfo) {
        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
            mCurrentEditEffect.editTimeCompleted();
        }

        String path = null;
        if (effectInfo.list != null) {
            path = EditorCommon.getMVPath(effectInfo.list, mVideoParam.getOutputWidth(),
                    mVideoParam.getOutputHeight());
        }
        effect.setPath(path);
        effectInfo.setPath(path);
        int id;
        if (path != null && new File(path).exists()) {
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MIX);
            //先执行applyMV之后才能拿到对应的getMvAudioId
            mAliyunIEditor.applyMV(effect);
            id = effect.getMvAudioId();
            Log.d(TAG, "editor resetEffect end:" + id);
            if (isReplaceMusic) {
                mAliyunIEditor.applyMusicMixWeight(id, 100);

            } else if (isMixRecord) {
                //如果是合拍，不需要mv音乐
                mAliyunIEditor.applyMusicWeight(id, 0);
            } else {
//                mAliyunIEditor.applyMusicMixWeight(id, effect.getWeight());
                mAliyunIEditor.applyMusicMixWeight(id, bgmWeight);

            }
        } else {
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MV);
            if (lastMusicBean != null && lastMusicBean.getPath() != null) {
                mAliyunIEditor.applyMusic(lastMusicBean);
                id = lastMusicBean.getId();
                if (isReplaceMusic) {
                    mAliyunIEditor.applyMusicMixWeight(id, 100);
                } else if (isMixRecord) {
                    mAliyunIEditor.applyMusicWeight(id, 0);
                } else {
//                    mAliyunIEditor.applyMusicMixWeight(id, lastMusicBean.getWeight());
                    mAliyunIEditor.applyMusicMixWeight(id, bgmWeight);
                }
            } else {
                if (isReplaceMusic) {
                    //恢复原音
                    mAliyunIEditor.applyMusicMixWeight(0, 0);
                }
            }
        }
        //重新播放，倒播重播流时间轴需要设置到最后
        if (mUseInvert) {
            mAliyunIEditor.seek(mAliyunIEditor.getStreamDuration());
//            if(mViewStack!=null){
//                mAliyunIEditor.seek(mViewStack.getVideoEndTime());
//            }
        } else {
            mAliyunIEditor.seek(0);
//            if(mViewStack!=null){
//                mAliyunIEditor.seek(mViewStack.getVideoStartTime());
//            }
        }
        mAliyunIEditor.resume();
        if (mThumbLineBar != null) {
            mThumbLineBar.resume();
        }
        switchPlayStateUI(false);
    }

    private void applyTimeEffect(EffectInfo effectInfo) {

        mUseInvert = false;
        if (mTimeEffectOverlay != null) {
            mThumbLineBar.removeOverlay(mTimeEffectOverlay);
        }
        mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_TIME);
        if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_NONE)) {
            playingResume();
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_RATE)) {
            if (effectInfo.isMoment) {

                mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime, 1000 * 1000, mThumbLineOverlayView,
                        0, false, UIEditorPage.TIME);
                //mAliyunIEditor.stop();
                //playingPause();
                mAliyunIEditor.stop();
                mAliyunIEditor.rate(effectInfo.timeParam, effectInfo.startTime / 1000, 1000, false);
                playingResume();
            } else {
                mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000000L, mThumbLineOverlayView, 0, false,
                        UIEditorPage.TIME);
                //playingPause();
                mAliyunIEditor.stop();
                mAliyunIEditor.rate(effectInfo.timeParam, 0, 1000000000L, false);
                playingResume();

            }
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_INVERT)) {

            mUseInvert = true;
            mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000000L, mThumbLineOverlayView, 0, false,
                    UIEditorPage.TIME);
            //mAliyunIEditor.stop();
            //playingPause();
            checkAndTranscode(TimeEffectType.TIME_EFFECT_TYPE_INVERT, 0, 0, 0, false);
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_REPEAT)) {
            mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime, 1000 * 1000, mThumbLineOverlayView, 0,
                    false, UIEditorPage.TIME);
            //mAliyunIEditor.stop();
            //playingPause();
            checkAndTranscode(TimeEffectType.TIME_EFFECT_TYPE_REPEAT, 3, effectInfo.startTime / 1000, 1000, false);
        }
        if (mTimeEffectOverlay != null) {
            mTimeEffectOverlay.switchState(ThumbLineOverlay.STATE_FIX);
        }
    }

    private boolean mIsTransitioning = false;
    private AlivcCircleLoadingDialog mTransitionAnimation;
    private Map<Integer, TransitionBase> mTransitionBaseMap = new HashMap<>();

    public Map<Integer, TransitionBase> getTransitionBaseMap() {
        return mTransitionBaseMap;
    }

    private void startTransitionAnimation() {
        mTransitionAnimation.show();
        mIsTransitioning = true;
    }

    private void stopTransitionAnimation() {
        mTransitionAnimation.dismiss();
        mIsTransitioning = false;
    }

    private void setTransition(final EffectInfo effectInfo) {

        if (mTransitionAnimation == null) {
            //转场animation
            mTransitionAnimation = new AlivcCircleLoadingDialog(getContext(), mPasterContainer.getHeight());
        }
        if (mIsTransitioning) {
            return;
        }
        startTransitionAnimation();

        if (effectInfo.mutiEffect == null) {
            //添加转场特效
            final TransitionBase transition = getTransitionBase(effectInfo);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    mAliyunIEditor.saveEffectToLocal();
                    mAliyunIEditor.setTransition(effectInfo.clipIndex, transition);
                    mTransitionBaseMap.put(effectInfo.clipIndex, transition);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("effectInfo", effectInfo);
                    Message message = new Message();
                    message.what = ADD_TRANSITION;
                    message.setData(bundle);
                    alivcEditHandler.sendMessage(message);
                    resetTimeLineLayout();
                }
            });
        } else if (effectInfo.mutiEffect.size() != 0) {
            //撤销转场特效
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    mAliyunIEditor.saveEffectToLocal();
                    Map<Integer, TransitionBase> hashMap = new HashMap<>();
                    for (EffectInfo info : effectInfo.mutiEffect) {
                        TransitionBase transitionBase = getTransitionBase(info);
                        hashMap.put(info.clipIndex, transitionBase);
                        mTransitionBaseMap.put(info.clipIndex, transitionBase);
                    }
                    mAliyunIEditor.setTransition(hashMap);

                    alivcEditHandler.sendEmptyMessage(REVERT_TRANSITION);
                    resetTimeLineLayout();

                }
            });

        } else {
            stopTransitionAnimation();
        }

    }

    /**
     * 初始化线程池和Handler
     */
    private void initThreadHandler() {
        executorService = ThreadUtil.newDynamicSingleThreadedExecutor(new AlivcEditThread());
        alivcEditHandler = new AlivcEditHandler(this);
    }

    /**
     * 是否存在录制有音乐
     *
     * @param isHashRecordMusic boolean
     */
    public void setHasRecordMusic(boolean isHashRecordMusic) {
        this.mHasRecordMusic = isHashRecordMusic;
    }

    /**
     * 获取存在录制有音乐
     *
     * @return boolean
     */
    public boolean isHasRecordMusic() {
        return mHasRecordMusic;
    }

    /**
     * 是否是合拍过来的视频
     */
    public void setIsMixRecord(boolean isMixRecord) {
        this.isMixRecord = isMixRecord;
    }

    /**
     * 获取是否是合拍
     */
    public boolean isMaxRecord() {
        return isMixRecord;
    }

    public void setHasDeNoise(boolean deNoise) {
        this.mHasDeNoise = deNoise;
    }

    private int mVideoRotation;

    public void setVideoRotation(int rotation) {
        this.mVideoRotation = rotation;
        if (mViewOperate != null) {
            mViewOperate.setVideoRotation(mVideoRotation);
        }
    }

    public static class AlivcEditThread implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("AlivcEdit Thread");
            return thread;
        }
    }

    private static final int ADD_TRANSITION = 1;
    private static final int REVERT_TRANSITION = 2;
    private static final int SAVE_COVER = 3;

    private static class AlivcEditHandler extends Handler {

        private WeakReference<AlivcEditView> reference;

        public AlivcEditHandler(AlivcEditView editView) {
            reference = new WeakReference<>(editView);
        }

        @Override
        public void handleMessage(Message msg) {
            AlivcEditView alivcEditView = reference.get();
            if (alivcEditView == null) {
                return;
            }
            switch (msg.what) {
                case REVERT_TRANSITION:
                    alivcEditView.playingResume();
                    alivcEditView.stopTransitionAnimation();
                    if (sIsDeleteTransitionSource) {
                        sIsDeleteTransitionSource = false;
                        Log.i(TAG, "delete transition source");
                    } else {
                        alivcEditView.clickCancel();
                    }
                    break;
                case ADD_TRANSITION:
                    EffectInfo effectInfo = (EffectInfo) msg.getData().getSerializable("effectInfo");
                    alivcEditView.addTransitionSuccess(effectInfo);
                    break;

                case SAVE_COVER:
                    //循环查询截取封面工作是否结束，结束跳转到下个页面
                    if (alivcEditView.mSnapshop.isSnapshotting()) {
                        sendEmptyMessageDelayed(SAVE_COVER, 500);
                    } else {
                        removeMessages(SAVE_COVER);
                        alivcEditView.mTransitionAnimation.dismiss();
                        alivcEditView.jumpToNextActivity();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 添加转场成功
     *
     * @param effectInfo
     */
    private void addTransitionSuccess(EffectInfo effectInfo) {

        //提前一秒
        long advanceTime = 1000 * 1000;
        long clipStartTime = mAliyunIEditor.getClipStartTime(effectInfo.clipIndex + 1);

        advanceTime = clipStartTime - advanceTime >= 0 ? clipStartTime - advanceTime : 0;
        mAliyunIEditor.seek(advanceTime);
        playingResume();
        mWaitForReady = true;
        stopTransitionAnimation();
        Log.d(TAG, "onTransitionPreview: index = " + effectInfo.clipIndex
                + " ,clipStartTime = " + clipStartTime
                + " ,duration = " + mAliyunIEditor.getDuration()
                + " ,advanceTime = " + advanceTime
        );
    }

    @Nullable
    private TransitionBase getTransitionBase(EffectInfo effectInfo) {
        TransitionBase transition = null;
        long overlapDuration = 1000 * 1000;//转场时长
        switch (effectInfo.transitionType) {
            case TransitionChooserView.EFFECT_NONE:
                break;
            case TransitionChooserView.EFFECT_RIGHT:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_RIGHT);
                break;
            case TransitionChooserView.EFFECT_CIRCLE:
                transition = new TransitionCircle();
                transition.setOverlapDuration(overlapDuration);
                break;
            case TransitionChooserView.EFFECT_FADE:
                transition = new TransitionFade();
                transition.setOverlapDuration(overlapDuration);
                break;
            case TransitionChooserView.EFFECT_FIVE_STAR:
                transition = new TransitionFiveStar();
                transition.setOverlapDuration(overlapDuration);
                break;
            case TransitionChooserView.EFFECT_SHUTTER:
                transition = new TransitionShutter();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionShutter) transition).setLineWidth(0.1f);
                ((TransitionShutter) transition).setOrientation(TransitionBase.ORIENTATION_HORIZONTAL);
                break;
            case TransitionChooserView.EFFECT_UP:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_UP);
                break;
            case TransitionChooserView.EFFECT_DOWN:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_DOWN);
                break;
            case TransitionChooserView.EFFECT_LEFT:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_LEFT);
                break;
            case TransitionChooserView.EFFECT_CUSTOM:
                transition = effectInfo.transitionBase;
                transition.setOverlapDuration(overlapDuration);
                break;
            default:
                break;
        }
        return transition;
    }

    /**
     * 对于Gop比较大的视频做时间特效时需要先检查是否满足实时，如果不满足实时，需要提前做转码，逻辑如下
     * 转码倒播参数要求：小于1080, 1920，gop小于5，fps小于30
     * 转码生成临时文件的默认参数为：分辨率：小于1080, 1920，gop：5，fps：30，type： ffmpeg。
     * <p>
     * 高清分辨率处理在导入时处理
     *
     * @param type         操作的类型（倒放，反复等）
     * @param times        这里指的是反复的次数
     * @param startTime    反复开始的时间
     * @param duration     反复的时长
     * @param needDuration 是否需要保持原视频长度
     */
    private void checkAndTranscode(final TimeEffectType type, final int times, final long startTime,
                                   final long duration, final boolean needDuration) {

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                AliyunClip clip = mAliyunIEditor.getSourcePartManager().getAllClips().get(0);
                final AtomicInteger flag = new AtomicInteger(0);
                if (clip == null) {
                    return null;
                }
                boolean ret = checkInvert(clip.getSource());
                if (!ret) {
                    mAliyunIEditor.saveEffectToLocal();
                    final CountDownLatch countDownLatch = new CountDownLatch(1);

                    CropParam param = new CropParam();
                    param.setGop(5);
                    param.setFrameRate(30);
                    param.setQuality(VideoQuality.SSD);
                    param.setInputPath(clip.getSource());
                    param.setVideoCodec(VideoCodecs.H264_SOFT_OPENH264);
                    param.setCrf(19);
                    param.setOutputPath(clip.getSource() + "_invert_transcode");
                    Log.i(TAG, "log_editor_edit_transcode : " + param.getOutputPath());
                    int width = 0;
                    int height = 0;
                    int rotate = 0;
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(clip.getSource());
                        rotate = Integer.parseInt(
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                        if (rotate == 90 || rotate == 270) {
                            height = Integer.parseInt(
                                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            width = Integer.parseInt(
                                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                        } else {
                            width = Integer.parseInt(
                                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            height = Integer.parseInt(
                                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                        }
                    } catch (Exception e) {
                        width = mVideoParam.getOutputWidth();
                        height = mVideoParam.getOutputHeight();
                    } finally {
                        mmr.release();
                    }
                    param.setOutputWidth(width);
                    param.setOutputHeight(height);
                    mTranscoder.setCropParam(param);
                    mTranscoder.setCropCallback(new CropCallback() {
                        @Override
                        public void onProgress(final int percent) {
                            Log.d(TAG, "percent" + percent);
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    mTransCodeProgress.setProgress(percent);
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            Log.d(TAG, "onError" + code);
                            flag.set(1);
                            countDownLatch.countDown();
                            mIsTranscoding = false;
                        }

                        @Override
                        public void onComplete(long duration) {
                            AliyunIClipConstructor clipConstructor = mAliyunIEditor.getSourcePartManager();
                            AliyunClip clip = clipConstructor.getMediaPart(0);
                            clip.setSource(clip.getSource() + "_invert_transcode");
                            clipConstructor.updateMediaClip(0, clip);
                            mAliyunIEditor.applySourceChange();
                            flag.set(2);
                            countDownLatch.countDown();
                            mIsTranscoding = false;
                        }

                        @Override
                        public void onCancelComplete() {
                            flag.set(3);
                            if (mIsDestroyed) {
                                mTranscoder.dispose();
                            }
                            countDownLatch.countDown();
                            mIsTranscoding = false;
                        }
                    });
                    mIsTranscoding = true;
                    int r = mTranscoder.startCrop();
                    if (r != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
                        return null;
                    }
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mTransCodeTip.setVisibility(View.VISIBLE);
                            BaseChooser bottomView = mViewOperate.getBottomView();
                            if (bottomView != null) {
                                bottomView.setClickable(false);
                            }
                        }
                    });
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                return flag;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (mIsDestroyed) {
                    return;
                }
                mTransCodeTip.setVisibility(View.GONE);
                mAliyunIEditor.stop();
                if (o instanceof AtomicInteger) {
                    if (((AtomicInteger) o).get() == 0 || ((AtomicInteger) o).get() == 2) {
                        if (type == TimeEffectType.TIME_EFFECT_TYPE_INVERT) {
                            mAliyunIEditor.invert();
                        } else if (type == TimeEffectType.TIME_EFFECT_TYPE_REPEAT) {
                            mAliyunIEditor.repeat(times, startTime, duration, needDuration);
                        }

                    }
                }
                //如果转码完成时，本页面被stop，则不进行恢复播放
                //只是把isNeedResume改为true
                if (!mIsStop) {
                    playingResume();
                } else {
                    isNeedResume = true;
                }

                //mAliyunIEditor.play();
                BaseChooser bottomView = mViewOperate.getBottomView();
                if (bottomView != null) {
                    bottomView.setClickable(true);
                }
            }
        }.execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onTabChange() {
        Log.d(TAG, "onTabChange: ");
        UIEditorPage page = UIEditorPage.get(mTabGroup.getCheckedIndex());
        switch (page) {
            case AUDIO_MIX:
                playingPause();
                break;
            case SOUND:
                //音效。
                break;
            case FONT:
            case TEXT:
            case OVERLAY:
                //case穿透统一处理paster的保存，用于撤销
//                mPasterEffectCachetList.clear();
//                for (int i = 0; i < mPasterContainer.getChildCount(); i++) {
//                    View childAt = mPasterContainer.getChildAt(i);
//                    Object tag = childAt.getTag();
//                    if (tag == null || !(tag instanceof AbstractPasterUISimpleImpl)) {
//                        //如果子pasterView的tag异常
//                        continue;
//                    }
//                    AbstractPasterUISimpleImpl uiSimple = (AbstractPasterUISimpleImpl) tag;
//                    if (!isPasterTypeHold(page, uiSimple.getEditorPage())) {
//                        //如果paster类型与所打开的编辑页面不一致
//                        continue;
//                    }
//                    PasterRestoreBean restoreBean = new PasterRestoreBean();
//                    restoreBean.setFrameAction(uiSimple.getFrameAction());
//                    restoreBean.setTempFrameAction(uiSimple.getTempFrameAction());
//                    restoreBean.setFrameSelectedPosition(uiSimple.getFrameSelectPosition());
//                    EffectBase effect = uiSimple.getController().getEffect();
//                    if (effect instanceof EffectCaption) {
//                        EffectCaption src = (EffectCaption) effect;
//                        EffectCaption copy = new EffectCaption("");
//                        src.copy(copy);
//                        restoreBean.setEffectBase(copy);
//                    } else if (effect instanceof EffectText) {
//                        EffectText src = (EffectText) effect;
//                        EffectText copy = new EffectText("");
//                        src.copy(copy);
//                        restoreBean.setEffectBase(copy);
//                    } else if (effect instanceof EffectPaster) {
//                        EffectPaster src = (EffectPaster) effect;
//                        EffectPaster copy = new EffectPaster("");
//                        src.copy(copy);
//                        restoreBean.setEffectBase(copy);
//                    }
//                    mPasterEffectCachetList.add(restoreBean);
//                }
                break;
            case COVER:
                //暂停播放并隐藏播放按钮。
                playingPause();
                mPlayImage.setVisibility(GONE);
                break;

            default:
                break;
        }
    }

    /**
     * 贴纸是否相同的超强力判断
     *
     * @param pageOne {@link UIEditorPage}
     * @param page2   {@link UIEditorPage}
     * @return boolean
     */
    private boolean isPasterTypeHold(UIEditorPage pageOne, UIEditorPage page2) {
        //当pageOne为动图时，page2也是动图返回true
        //当pageOne是字幕或者字体，page2也是字幕或者字体时返回true
        return pageOne == UIEditorPage.OVERLAY && page2 == UIEditorPage.OVERLAY
                || pageOne != UIEditorPage.OVERLAY && page2 != UIEditorPage.OVERLAY;
    }

//    private List<PasterRestoreBean> mPasterEffectCachetList = new ArrayList<>();

    private void checkAndRemoveEffects() {
        //删除资源时，如果有使用对应的特效也删除
        checkAndRemovePaster();
        checkAndRemoveTransition();
        checkAndRemoveAnimationFilter();
    }

    private void checkAndRemoveAnimationFilter() {
        if (mAliyunIEditor != null) {
            Dispatcher.getInstance().postMsg(new CheckDeleteFilter());
        }
    }

    /**
     * 转场资源被删除，应用的动画结束后不关闭窗口 {@link AlivcEditHandler#handleMessage(Message)}
     */
    private static boolean sIsDeleteTransitionSource = false;

    private void checkAndRemoveTransition() {
        if (mAliyunIEditor != null) {
            TransitionEffectCache transitionEffectCache = mEditorService.getTransitionEffectCache(mAliyunIEditor.getSourcePartManager());

            List<EffectInfo> deleteList = transitionEffectCache.checkTransitionCacheIsDelete();
            if (deleteList.size() == 0) {
                return;
            }
            sIsDeleteTransitionSource = true;
            EffectInfo effectInfo = new EffectInfo();
            effectInfo.type = UIEditorPage.TRANSITION;
            effectInfo.transitionType = TransitionChooserView.EFFECT_CUSTOM;
            effectInfo.mutiEffect = deleteList;
            onEffectChange(effectInfo);
        }
    }

    private void checkAndRemovePaster() {
        int count = mPasterContainer.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View pv = mPasterContainer.getChildAt(i);
            AbstractPasterUISimpleImpl uic = (AbstractPasterUISimpleImpl) pv.getTag();
            if (uic != null && !uic.isPasterExists()) {
                Log.e(TAG, "removePaster");
                uic.removePaster();
            }
        }
    }

    /**
     * TODO
     * TODO 调用resetThumbLine的时机
     * TODO
     * TODO
     */
    private void resetTimeLineLayout() {
        Log.i(TAG, "resetTimeLineLayout");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                initThumbLineBar();
            }
        });
    }

    public void playingPause() {
        if (mAliyunIEditor.isPlaying()) {
            mAliyunIEditor.pause();
            if (mThumbLineBar != null) {
                mThumbLineBar.pause();
            }
            switchPlayStateUI(true);
        }
    }

    public void playingResume() {
        if (!mAliyunIEditor.isPlaying()) {
            if (mAliyunIEditor.isPaused()) {
                mAliyunIEditor.resume();
            } else {
                mAliyunIEditor.play();
            }
            if (mThumbLineBar != null) {
                mThumbLineBar.resume();
            }
            switchPlayStateUI(false);
        }
    }

    private PasterUIGifImpl addPaster(AliyunPasterController controller) {
        Log.d(TAG, "add GIF");
        AliyunPasterWithImageView pasterView = (AliyunPasterWithImageView) View.inflate(getContext(),
                R.layout.alivc_editor_view_paster_gif, null);
        final PasterUIGifImpl pasterUIGif = new PasterUIGifImpl(pasterView, controller, mThumbLineBar, mAliyunIEditor);

        ImageView imageView = pasterView.findViewById(R.id.qupai_btn_edit_overlay_animation);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pasterUIGif.showAnimationDialog(mUseInvert);
            }
        });
        mPasterContainer.addView(pasterView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return pasterUIGif;
    }


    private PasterUITextImpl addCaptions(AliyunPasterController controller, boolean restore) {
        Log.d(TAG, "add 文字");
        AliyunPasterWithTextView captionView = (AliyunPasterWithTextView) View.inflate(getContext(),
                R.layout.alivc_editor_view_paster_text, null);
        mPasterContainer.addView(captionView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) captionView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        captionView.setLayoutParams(layoutParams);


//        AliyunPasterWithTextView captionView = (AliyunPasterWithTextView) View.inflate(getContext(),
//                R.layout.alivc_editor_view_paster_text, null);
//        mPasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return new PasterUITextImpl(captionView, controller, mThumbLineBar, mAliyunIEditor, restore);

    }

    /**
     * 添加字幕
     *
     * @param controller
     * @return
     */
    private PasterUICaptionImpl addCaption(AliyunPasterController controller) {
        AliyunPasterWithImageView captionView = (AliyunPasterWithImageView) View.inflate(getContext(),
                R.layout.alivc_editor_view_paster_caption, null);
        mPasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Log.d(TAG, "add 字幕");
        return new PasterUICaptionImpl(captionView, controller, mThumbLineBar, mAliyunIEditor);
    }

    /**
     * 添加文字
     *
     * @param controller
     * @param restore
     * @return
     */
    private PasterUITextImpl addSubtitle(AliyunPasterController controller, boolean restore) {
        Log.d(TAG, "add 文字");
        AliyunPasterWithTextView captionView = (AliyunPasterWithTextView) View.inflate(getContext(),
                R.layout.alivc_editor_view_paster_text, null);
        mPasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return new PasterUITextImpl(captionView, controller, mThumbLineBar, mAliyunIEditor, restore);
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayImage && mAliyunIEditor != null) {
            //当在添加特效的时候，关闭该按钮
            if (mUseAnimationFilter) {
                return;
            }
            if (mAliyunIEditor.isPlaying()) {
                playingPause();
            } else {
                playingResume();
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                }
            }
        }
    }

    /**
     * 是否有特效
     */
    private boolean isChangeEffect;

    //是否添加过特效 or 动效
    public boolean isChangeEffect() {
        if (mAliyunIEditor != null) {
            mAliyunIEditor.setVolume(mVolume);
        }
        if (isChangeEffect) {
            return isEffect();
        }
        return false;
    }

    public boolean isEffect() {
        //有字幕
        if (mCaptionsUiList != null && !mCaptionsUiList.isEmpty()) {
            return true;
        }
        //有背景音乐
        if (lastMusicBean != null && lastMusicBean.getSource() != null && (!TextUtils.isEmpty(lastMusicBean.getSource().getURL()) || !TextUtils.isEmpty(lastMusicBean.getSource().getPath()))) {
            return true;
        }
        //有特效滤镜
        if (mAnimationFilterController != null && mAnimationFilterController.getFilterSize() > 0) {
            return true;
        }
        //有颜色滤镜
        if (mLastColorFilterEffect != null && mLastColorFilterEffect.getSource() != null && (!TextUtils.isEmpty(mLastColorFilterEffect.getSource().getURL()) || !TextUtils.isEmpty(mLastColorFilterEffect.getSource().getPath()))) {
            return true;
        }
        //有动图
        if (mGifUiList != null && !mGifUiList.isEmpty()) {
            return true;
        }
        //有MV效果
        if (mLastMVEffect != null && mLastMVEffect.list != null) {
            return true;
        }
        //有音效
        if (mLastSoundEffect != null && mLastSoundEffect.audioEffectType != AudioEffectType.EFFECT_TYPE_DEFAULT) {
            return true;
        }
        //有涂鸦
        if (mCanvasController != null && mCanvasController.hasCanvasPath()) {
            return true;
        }
        return isChangeEffect = false;
    }


    public void cleanAllEffect() {
        //清空字幕
        for (PasterUICaptionsImpl uiCaptions : mCaptionsUiList) {
            mPasterContainer.removeView(uiCaptions.getPasterView());
            uiCaptions.removePaster();
        }
        mCaptionsUiList.clear();

        if (lastMusicBean != null && lastMusicBean.getSource() != null) {
            //清空背景音乐
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MIX);
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MV_AUDIO);
            if (lastMusicBean != null) {
                mAliyunIEditor.removeMusic(lastMusicBean);
                mAliyunIEditor.setVolume(mVolume);
            }
            lastMusicBean = null;
        }

        //清除所有的特效滤镜
        if (mAnimationFilterController != null && mAnimationFilterController.getFilterSize()>0) {
            if (mThumbLineBar != null) {
                mThumbLineBar.clearOverlay();
            }
            mAnimationFilterController.onEventClearAllAnimationFilter();
        }

        if (mLastColorFilterEffect != null) {
            //清除滤镜
            EffectInfo colorEffect = new EffectInfo();
            colorEffect.type = UIEditorPage.FILTER;
            EffectBean effect = new EffectBean();
            effect.setPath(null);
            effect.setSource(new Source());
            mAliyunIEditor.applyFilter(effect);
            mLastColorFilterEffect = colorEffect;
        }

        //清空动图
        for (PasterUIGifImpl pasterUIGif : mGifUiList) {
            mPasterContainer.removeView(pasterUIGif.getPasterView());
            pasterUIGif.removePaster();
            mPasterManager.remove(pasterUIGif.mController);
        }
        mGifUiList.clear();

        if (mLastMVEffect != null) {
            //清空MV
            EffectInfo mvEffectInfo = new EffectInfo();
            mvEffectInfo.type = UIEditorPage.MV;
            mvEffectInfo.list = null;
            applyMVEffect(mvEffectInfo);
            mLastMVEffect = mvEffectInfo;
        }

        //清空音效
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (int i = 0; i < allClips.size(); i++) {
            if (mLastSoundEffect != null) {
                mAliyunIEditor.removeAudioEffect(allClips.get(i).getId(), mLastSoundEffect.audioEffectType);
            }
        }
        mLastSoundEffect = MockEffectSoundData.getEffectSound().get(0);

        //清空涂鸦
        if (mCanvasController != null && mCanvasController.hasCanvasPath()) {
            mCanvasController.clear();
            mCanvasController.confirm();
            mCanvasController.applyPaintCanvas();
            mPasterContainer.removeView(mCanvasController.getCanvas());
            mCanvasController.removeCanvas();
        }
        isChangeEffect = false;
    }


    private long mStartTime, mEndTime;

    public void setTrimVideoTimes(long startTime, long endTime) {
        mStartTime = startTime;
        mEndTime = endTime;
    }

    private void clickConfirm() {
        // 确认后变化，各个模块自行实现
        int checkIndex = mTabGroup.getCheckedIndex();
        UIEditorPage page = UIEditorPage.get(checkIndex);
        if (page != UIEditorPage.CAPTION) {
            if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                mCurrentEditEffect.editTimeCompleted();
            }
        }
        if (page != UIEditorPage.TRIM) {
            isChangeEffect = true;
        }
        playingResume();
        switch (page) {
            case CAPTION:
                //确认添加字幕，将字幕从UI层移除，添加到视频中，并且清空临时字幕
                for (PasterUICaptionsImpl uiCaptions : mCaptionsUiList) {
                    if (uiCaptions != null) {
                        uiCaptions.editCompleted();
                    }
                }
                break;
            case TRIM:
                //确定裁剪了，清空其他效果
                resetTimeLineLayout();
                break;
            case FILTER_EFFECT:
                break;
            case COVER:
                //改变标示，具体截图代码在mEditorCallback.onTextureRender中实现
                isTakeFrameSelected = true;
                hasCaptureCover = true;
                break;
            case OVERLAY://动图
                if (mGifUiList != null && !mGifUiList.isEmpty()) {
                    Iterator<PasterUIGifImpl> iterator = mGifUiList.iterator();
                    while (iterator.hasNext()) {
                        PasterUIGifImpl pasterUIGif = iterator.next();
                        if (pasterUIGif.isPasterRemoved()) {
                            iterator.remove();
                        }
                    }
                }
                break;
            case PAINT:
                if (mCanvasController != null) {
                    mCanvasController.confirm();
                    mCanvasController.applyPaintCanvas();
                    mPasterContainer.removeView(mCanvasController.getCanvas());

//                    View viewCanvas = mCanvasController.getCanvas();
//                    if (viewCanvas instanceof com.aliyun.svideosdk.editor.impl.d) {
//                        CanvasInfo canvasInfo = ((d) viewCanvas).getCanvasInfo();
//                        if (canvasInfo != null && canvasInfo.transfer() != null) {
//                            int size = canvasInfo.transfer().size();
//                            if (size == 0) {
//                                mCanvasController.removeCanvas();
//                            }
//                        }
//                    }
                }
                break;
            default:
                break;
        }
//        将转场效果的缓存清空
        if (mTransitionCache != null) {
            mTransitionCache.clear();
        }
        if (mTransitionParamsCache != null) {
            mTransitionParamsCache.clear();
        }
        mViewOperate.hideBottomView();
    }

    /**
     * 编辑态视图点击返回
     */
    private void clickCancel() {
        // 取消后变化，各个模块自行实现
        int checkIndex = mTabGroup.getCheckedIndex();
        UIEditorPage page = UIEditorPage.get(checkIndex);
        if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
            mCurrentEditEffect.removePaster();
        }
        playingResume();
        switch (page) {
            case CAPTION:
                //取消添加字幕
                playingResume();
                for (PasterUICaptionsImpl uiCaptions : mCaptionsUiList) {
                    mPasterContainer.removeView(uiCaptions.getPasterView());
                    uiCaptions.removePaster();
                }
//                ViewStack.cleanCaptions(mPasterContainer);
                mCaptionsUiList.clear();
                break;
            case FILTER_EFFECT:
                break;
            case COVER:
                isTakeFrameSelected = false;
                mPlayImage.setVisibility(VISIBLE);
                break;
            case AUDIO_MIX://
                playingResume();
                break;
            case PAINT:
                if (mCanvasController == null) {
                    break;
                }
                //清除当前操作
                mCanvasController.cancel();
                mCanvasController.applyPaintCanvas();
                mPasterContainer.removeView(mCanvasController.getCanvas());
                break;
            case SOUND:

                break;
            case TRANSITION:
//            点击取消时，重新调用updateTransition，使用设置参数之前的值
                if (mTransitionCache != null && mTransitionParamsCache != null && !mTransitionCache.isEmpty() && !mTransitionParamsCache.isEmpty() && mTransitionCache.size() == mTransitionParamsCache.size()) {
                    for (Integer i : mTransitionCache.keySet()) {
                        for (Integer key : mTransitionCache.keySet()) {
                            EffectInfo effectInfo = mTransitionCache.get(key);
                            List<AlivcTransBean> paramList = mTransitionParamsCache.get(key);
                            List<EffectConfig.NodeBean> nodeTree = effectInfo.transitionBase.getNodeTree();
                            if (nodeTree == null || nodeTree.size() == 0) {
                                break;
                            }
                            for (EffectConfig.NodeBean nodeBean : nodeTree) {
                                List<EffectConfig.NodeBean.Params> params = nodeBean.getParams();
                                if (params == null || params.size() == 0) {
                                    continue;
                                }
                                if (params.size() != paramList.size()) {
                                    break;
                                }
                                for (int j = 0; j < params.size(); j++) {
                                    EffectConfig.NodeBean.Params param = params.get(j);
                                    AlivcTransBean alivcTransBean = paramList.get(j);
                                    ValueTypeEnum type = param.getType();
                                    if (type == ValueTypeEnum.INT) {
//                                    重设之前的int值
                                        param.getValue().updateINT(alivcTransBean.getmIntergerValue());
                                    } else if (type == ValueTypeEnum.FLOAT) {
//                                    重设之前的float值
                                        param.getValue().updateFLOAT(alivcTransBean.getmFloatValue());
                                    }
                                }
                            }
                        }
//                    调用updateTransition方法，重新设置动画效果
                        mAliyunIEditor.updateTransition(mTransitionCache.get(i).clipIndex, mTransitionCache.get(i).transitionBase);
                        mTransitionBaseMap.put(mTransitionCache.get(i).clipIndex, mTransitionCache.get(i).transitionBase);
                    }
//                设置完成后清空
                    mTransitionCache.clear();
                    mTransitionParamsCache.clear();
                }
                break;
            case FONT:
            case OVERLAY:
            case TEXT:
//                //这里做paster的撤销恢复处理
//                if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
//                    mCurrentEditEffect.removePaster();
//                }
//
//                //先remove所有指定类型的paster
//                for (int i = 0; i < mPasterContainer.getChildCount(); i++) {
//                    View childAt = mPasterContainer.getChildAt(i);
//                    Object tag = childAt.getTag();
//                    if (tag == null || !(tag instanceof AbstractPasterUISimpleImpl)) {
//                        continue;
//                    }
//                    AbstractPasterUISimpleImpl uiSimple = (AbstractPasterUISimpleImpl) tag;
//
//                    if (isPasterTypeHold(uiSimple.getEditorPage(), page)) {
//                        // 1.Controller remove
//                        // 2.pasterContainer remove
//                        // 3.ThumbLBar remove
//                        uiSimple.removePaster();
//                        //涉及到集合遍历删除元素的问题（角标前移）
//                        i--;
//                    }
//                }
//
//                //恢复缓存的指定类型paster
//                for (PasterRestoreBean restoreBean : mPasterEffectCachetList) {
//                    final AliyunPasterController pasterController;
//                    EffectBase effectBase = restoreBean.getEffectBase();
//                    //获取对应的controller、（判断文件存在，避免用户删除了对应的资源后恢复时crash）
//                    if (effectBase instanceof EffectCaption && new File(effectBase.getPath()).exists()) {
//                        EffectCaption effect = (EffectCaption) effectBase;
//                        pasterController = mPasterManager.addPasterWithStartTime(effect.getPath(), effect.start,
//                                effect.end - effect.start);
//                    } else if (effectBase instanceof EffectText) {
//                        EffectText effect = (EffectText) effectBase;
//                        pasterController = mPasterManager.addSubtitleWithStartTime(effect.text, effect.font,
//                                effect.start, effect.end - effect.start);
//                    } else if (effectBase instanceof EffectPaster && new File(effectBase.getPath()).exists()) {
//                        EffectPaster effect = (EffectPaster) effectBase;
//                        pasterController = mPasterManager.addPasterWithStartTime(effect.getPath(), effect.start,
//                                effect.end - effect.start);
//                    } else {
//                        continue;
//                    }
//                    pasterController.setEffect(effectBase);
//                    //锁定参数（避免被设置effectBase参数被冲掉）
//                    pasterController.setRevert(true);
//                    if (pasterController.getPasterType() == EffectPaster.PASTER_TYPE_GIF) {
//                        mCurrentEditEffect = addPaster(pasterController);
//                    } else if (pasterController.getPasterType() == EffectPaster.PASTER_TYPE_TEXT) {
//                        mCurrentEditEffect = addSubtitle(pasterController, true);
//                    } else if (pasterController.getPasterType() == EffectPaster.PASTER_TYPE_CAPTION) {
//                        mCurrentEditEffect = addCaption(pasterController);
//                    }
//                    mCurrentEditEffect.showTimeEdit();
//                    mCurrentEditEffect.editTimeStart();
//                    mCurrentEditEffect.moveToCenter();
//                    mCurrentEditEffect.editTimeCompleted();
//
//                    ActionBase frameAction = restoreBean.getFrameAction();
//                    ActionBase tempFrameAction = restoreBean.getTempFrameAction();
//                    if (frameAction != null) {
//                        //恢复贴纸的帧动画
//                        frameAction.setTargetId(pasterController.getEffect().getViewId());
//                        mCurrentEditEffect.setFrameAction(frameAction);
//                        mCurrentEditEffect.setTempFrameAction(tempFrameAction);
//                        mCurrentEditEffect.setFrameSelectedPosition(restoreBean.getFrameSelectedPosition());
//                        mAliyunIEditor.addFrameAnimation(frameAction);
//                    }
//
//                    pasterController.setRevert(false);
//                }
                break;
            default:
                break;
        }

        mViewOperate.hideBottomView();
    }

    /**
     * 点击空白出弹窗消失
     */
    private void hideBottomEditorView() {
        int checkIndex = mTabGroup.getCheckedIndex();
        if (checkIndex == -1) {
            return;
        }
        UIEditorPage page = UIEditorPage.get(checkIndex);

        if (mViewOperate != null) {
            mViewOperate.hideBottomEditorView(page);
        }

        if (mViewStack != null) {
            mViewStack.hideBottomEditorView(page);
        }

    }

    /**
     * 恢复动效滤镜UI（这里主要是编辑页面顶部时间轴的覆盖
     *
     * @param animationFilters
     */
    @Override
    public void animationFilterRestored(final List<EffectFilter> animationFilters) {
        mPasterContainer.post(new Runnable() {
            @Override
            public void run() {
                mAnimationFilterController.setThumbLineBar(mThumbLineBar);
                if (mAnimationFilterController != null) {
                    mAnimationFilterController.restoreAnimationFilters(animationFilters);
                }
            }
        });
    }

    /**
     * 页面缩小时 对应的paster也要缩小
     *
     * @param scaleSize 缩小比率
     */
    public void setPasterDisplayScale(float scaleSize) {
        mPasterManager.setDisplaySize((int) (mPasterContainerPoint.x * scaleSize),
                (int) (mPasterContainerPoint.y * scaleSize));
    }

    //todo
    private class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        float mPosX;
        float mPosY;
        boolean shouldDrag = true;

        boolean shouldDrag() {
            return shouldDrag;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d(TAG, "onDoubleTapEvent");
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed");

            if (!shouldDrag) {
                boolean outside = true;
                BaseChooser bottomView = null;
                if (mViewOperate != null) {
                    bottomView = mViewOperate.getBottomView();
                }
                if (bottomView != null) {

                    int count = mPasterContainer.getChildCount();
                    for (int i = count - 1; i >= 0; i--) {
                        View pv = mPasterContainer.getChildAt(i);
                        AbstractPasterUISimpleImpl uic = (AbstractPasterUISimpleImpl) pv.getTag();

                        if (uic != null && bottomView.isHostPaster(uic)) {
                            if (uic.isVisibleInTime(mAliyunIEditor.getCurrentStreamPosition()) && uic.contentContains(e.getX(), e.getY())) {
                                outside = false;
                                if (mCurrentEditEffect != null && mCurrentEditEffect != uic && !mCurrentEditEffect
                                        .isEditCompleted()) {
                                    mCurrentEditEffect.editTimeCompleted();
                                }
                                mCurrentEditEffect = uic;
                                if (uic.isEditCompleted()) {
                                    playingPause();
                                    uic.editTimeStart();
                                }
                                break;
                            } else {
                                if (mCurrentEditEffect != uic && uic.isVisibleInTime(
                                        mAliyunIEditor.getCurrentStreamPosition())) {
                                    uic.editTimeCompleted();
                                }
                            }
                        }
                    }
                }

                if (outside) {
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        mCurrentEditEffect.editTimeCompleted();
                    }
                    hideBottomEditorView();
                }
            } else {
                playingPause();
                mCurrentEditEffect.showTextEdit(mUseInvert);
            }
            //            if (mAliyunPasterController != null) {
            //                //旋转动图，文字，字幕
            //                ActionRotate actionRotate = new ActionRotate();
            //                actionRotate.setStartTime(0);
            //                actionRotate.setTargetId(mAliyunPasterController.getEffect().getViewId());
            //                actionRotate.setDuration(10 * 1000 * 1000);
            //                actionRotate.setRepeat(true);
            //                actionRotate.setDurationPerCircle(3 * 1000 * 1000);
            //                mAliyunIEditor.addFrameAnimation(actionRotate);
            //                if(mAliyunPasterController.getEffect() instanceof EffectCaption){
            //                    actionRotate = new ActionRotate();
            //                    actionRotate.setStartTime(0);
            //                    actionRotate.setDuration(10 * 1000 * 1000);
            //                    actionRotate.setRepeat(true);
            //                    actionRotate.setDurationPerCircle(3 * 1000 * 1000);
            //                    actionRotate.setTargetId(((EffectCaption) mAliyunPasterController.getEffect())
            // .gifViewId);
            //                    mAliyunIEditor.addFrameAnimation(actionRotate);
            //                }
            //            }
            return shouldDrag;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG, "onShowPress");
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (shouldDrag()) {
                if (mPosX == 0 || mPosY == 0) {
                    mPosX = e1.getX();
                    mPosY = e1.getY();
                }
                float x = e2.getX();
                float y = e2.getY();

                mCurrentEditEffect.moveContent(x - mPosX, y - mPosY);
                mPosX = x;
                mPosY = y;

            } else {

            }

            return shouldDrag;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return shouldDrag;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            if (mCurrentEditEffect != null && mCurrentEditEffect.isPasterRemoved()) {
                mCurrentEditEffect = null;
            }

            if (mCurrentEditEffect != null) {
                Log.d(TAG, "mCurrentEditEffect != null");
                shouldDrag = !mCurrentEditEffect.isEditCompleted()
                        && mCurrentEditEffect.contentContains(e.getX(), e.getY())
                        && mCurrentEditEffect.isVisibleInTime(mAliyunIEditor.getCurrentStreamPosition()

                );
            } else {
                shouldDrag = false;

            }

            mPosX = 0;
            mPosY = 0;
            return true;

        }
    }

    StringBuilder mDurationText = new StringBuilder(5);

    private String convertDuration2Text(long duration) {
        mDurationText.delete(0, mDurationText.length());
        float relSec = (float) duration / (1000 * 1000);// us -> s
        int min = (int) ((relSec % 3600) / 60);
        int sec = 0;
        sec = (int) (relSec % 60);
        if (min >= 10) {
            mDurationText.append(min);
        } else {
            mDurationText.append("0").append(min);
        }
        mDurationText.append(":");
        if (sec >= 10) {
            mDurationText.append(sec);
        } else {
            mDurationText.append("0").append(sec);
        }
        return mDurationText.toString();
    }

    private void copyAssets() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                EditorCommon.copyAll(getContext(), resCopy);
            }
        });
    }

    public AliyunIEditor getEditor() {
        return this.mAliyunIEditor;
    }

    public void showMessage(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(id);
        builder.setNegativeButton(R.string.alivc_common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 转码倒播参数要求：gop小于5，fps小于35 （1080P分辨率要求在进入编辑前处理）
     *
     * @param filePath 视频地址
     * @return true 视频满足倒播要求，不需要转码 otherwise 需要转码
     */
    private boolean checkInvert(String filePath) {
        NativeParser parser = new NativeParser();
        if (parser.checkIfSupportedImage(filePath)) {
            parser.release();
            parser.dispose();
            return true;
        }
        parser.init(filePath);
        boolean gop = parser.getMaxGopSize() <= 5;
        boolean fps = false;
        try {
            fps = Float.parseFloat(parser.getValue(NativeParser.VIDEO_FPS)) <= 35;
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        parser.release();
        parser.dispose();
        return gop && fps;
    }

    /**
     * 设置转场的预览监听
     */
    private TransitionChooserView.OnPreviewListener mOnTransitionPreviewListener = new TransitionChooserView
            .OnPreviewListener() {
        @Override
        public void onPreview(int clipIndex, long leadTime, boolean isStop) {
            //提前一秒
            long advanceTime = 1000 * 1000;
            long clipStartTime = mAliyunIEditor.getClipStartTime(clipIndex + 1);
            advanceTime = clipStartTime - advanceTime >= 0 ? clipStartTime - advanceTime : 0;
            mAliyunIEditor.seek(advanceTime);
            playingResume();
            Log.d(TAG, "onTransitionPreview: index = " + clipIndex
                    + " ,clipStartTime = " + clipStartTime
                    + " ,duration = " + mAliyunIEditor.getDuration()
                    + " ,advanceTime = " + advanceTime
            );
        }
    };

    private OnEffectActionLister mOnEffectActionLister = new OnEffectActionLister() {
        @Override
        public void onCancel() {
            clickCancel();
        }

        @Override
        public void onComplete() {
            clickConfirm();
        }
    };

    //添加颜色滤镜回调
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventColorFilterSelected(SelectColorFilter selectColorFilter) {
        EffectInfo effectInfo = selectColorFilter.getEffectInfo();
        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        effect.setPath(effectInfo.getPath());
        mAliyunIEditor.applyFilter(effect);
        //颜色滤镜通过 EventBus 回调到这
        isChangeEffect = true;
        mLastColorFilterEffect = effectInfo;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onBrightnessProgressChange(BrightnessProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.BRIGHTNESS, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onContrastProgressChange(ContrastProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.CONTRAST, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSaturationProgressChange(SaturationProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.SATURATION, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSharpProgressChange(SharpProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.SHARPNESS, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onVignetteCornerChange(VignetteMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.VIGNETTE, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onVideoEqResetMsg(VideoEqResetMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), msg.getType());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onVideoEqResetAllMsg(VideoEqResetAllMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.BRIGHTNESS);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.CONTRAST);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.SATURATION);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.SHARPNESS);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.VIGNETTE);
        }
    }

    /**
     * 长按时需要恢复播放
     *
     * @param filter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterLongClick(LongClickAnimationFilter filter) {
        if (!mUseAnimationFilter) {
            mUseAnimationFilter = true;
        }
        if (mCanAddAnimation) {
            playingResume();
        } else {
            playingPause();
        }

    }

    /**
     * 长按抬起手指需要暂停播放
     *
     * @param filter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterClickUp(LongClickUpAnimationFilter filter) {
        if (mUseAnimationFilter) {
            mUseAnimationFilter = false;
        }
        if (mAliyunIEditor.isPlaying()) {
            playingPause();

        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventFilterTabClick(FilterTabClick ft) {
        //切换到特效的tab需要暂停播放，切换到滤镜的tab需要恢复播放
        if (mAliyunIEditor != null) {
            switch (ft.getPosition()) {
                case FilterTabClick.POSITION_ANIMATION_FILTER:
                    if (mAliyunIEditor.isPlaying()) {
                        playingPause();
                    }
                    break;
                case FilterTabClick.POSITION_COLOR_FILTER:
                    if (!mAliyunIEditor.isPlaying()) {
                        playingResume();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private OnPlayingCallback mPlayingCallback;

    private ArrayList<OnPlayingCallback> mPlayingCallbacks;

    public interface OnPlayingCallback {
        void onPlaying(final long currentPlayTime, final long currentStreamPlayTime);
    }

    public void addPlayCallback(OnPlayingCallback callback) {
        if (mPlayingCallbacks == null) {
            mPlayingCallbacks = new ArrayList<>();
        }
        if (!mPlayingCallbacks.contains(callback)) {
            mPlayingCallbacks.add(callback);
        }
    }

    public void removePlayCallback(final OnPlayingCallback listener) {
        if (mPlayingCallbacks != null && listener != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mPlayingCallbacks.removeIf(new Predicate<OnPlayingCallback>() {
                    @Override
                    public boolean test(OnPlayingCallback onPlayingCallback) {
                        return onPlayingCallback.equals(listener);
                    }
                });
            } else {
                Iterator<OnPlayingCallback> it = mPlayingCallbacks.iterator();
                while (it.hasNext()) {
                    try {
                        OnPlayingCallback backgroundListener = it.next();
                        if (backgroundListener.equals(listener)) {
                            it.remove();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void removeAllPlayCallback() {
        if (mPlayingCallbacks != null) {
            mPlayingCallbacks.clear();
        }
    }


    public void setPlayCallback(OnPlayingCallback playCallback) {
        mPlayingCallback = playCallback;
    }

    private EditorCallBack mEditorCallback = new EditorCallBack() {
        @Override
        public void onEnd(int state) {

            post(new Runnable() {
                @Override
                public void run() {

                    if (!mUseAnimationFilter) {
                        //当正在添加滤镜的时候，不允许重新播放
                        mAliyunIEditor.replay();
                        mThumbLineBar.restart();
                    } else {
                        mCanAddAnimation = false;
                        switchPlayStateUI(true);

                    }

                }
            });
        }

        @Override
        public void onError(final int errorCode) {
            Log.e(TAG, "play error " + errorCode);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (errorCode) {
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_WRONG_STATE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_PROCESS_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_NO_FREE_DISK_SPACE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CREATE_DECODE_GOP_TASK_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_AUDIO_STREAM_DECODER_INIT_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_VIDEO_STREAM_DECODER_INIT_FAILED:

                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_SPS_PPS_NULL:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_H264_PARAM_SET_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_HEVC_PARAM_SET_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_DECODER_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_STATE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INPUT:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_NO_BUFFER_AVAILABLE:

                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_DECODE_SPS:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_CREATE_DECODER_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_STATE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_INPUT:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_NO_BUFFER_AVAILABLE:
                            showToast = FixedToastUtils.show(getContext(), errorCode + "");
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CACHE_DATA_SIZE_OVERFLOW:
                            showToast = FixedToastUtils.show(getContext(), errorCode + "");
                            mThumbLineBar.restart();
                            mAliyunIEditor.play();
                            break;
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                            showToast = FixedToastUtils.show(getContext(),
                                    getResources().getString(R.string.alivc_editor_error_tip_not_supported_audio));
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                            showToast = FixedToastUtils.show(getContext(),
                                    getResources().getString(R.string.alivc_editor_error_tip_not_supported_video));
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_STREAM_NOT_EXISTS:
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_PIXEL_FORMAT:
                            showToast = FixedToastUtils.show(getContext(),
                                    getResources().getString(R.string.alivc_editor_error_tip_not_supported_pixel_format));
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INTERRUPT:
                            showToast = FixedToastUtils.show(getContext(),
                                    getResources().getString(R.string.alivc_editor_edit_tip_decoder_error_interrupt));
                            ((Activity) getContext()).finish();
                            break;
                        default:
                            showToast = FixedToastUtils.show(getContext(),
                                    getResources().getString(R.string.alivc_editor_error_tip_play_video_error));
                            ((Activity) getContext()).finish();
                            break;
                    }
                }
            });

        }

        @Override
        public int onCustomRender(int srcTextureID, int width, int height) {
            return srcTextureID;
        }

        @Override
        public int onTextureRender(int srcTextureID, int width, int height) {
            if (isTakeFrame) {
                if (mSnapshop == null) {
                    mSnapshop = new AlivcSnapshot();
                }
                mSnapshop.useTextureIDGetFrame(srcTextureID, mSurfaceView, width, height, new File(PATH_THUMBNAIL));
                isTakeFrame = false;
            }
            return 0;
        }

        //TODO
        //TODO
        //TODO
        @Override
        public void onPlayProgress(final long currentPlayTime, final long currentStreamPlayTime) {
            if (mPlayingCallback != null) {
                mPlayingCallback.onPlaying(currentPlayTime, currentStreamPlayTime);
            }
            if (mPlayingCallbacks != null && mPlayingCallbacks.size() > 0) {
                for (OnPlayingCallback listener : mPlayingCallbacks) {
                    listener.onPlaying(currentPlayTime, currentStreamPlayTime);
                }
            }
            post(new Runnable() {
                @Override
                public void run() {

                    long currentPlayTime = mAliyunIEditor.getCurrentPlayPosition();

                    //监听滑动，显示隐藏编辑的字幕
                    if (isShowEditCaptions && mCaptionsUiList != null) {
                        for (PasterUICaptionsImpl uiCaptions : mCaptionsUiList) {
                            boolean isShow = uiCaptions.setShow(currentPlayTime);
                            if (isShow) {
                                mCurrentEditEffect = uiCaptions;
                            }
                        }
                    }

                    if (mUseAnimationFilter
                            && mAliyunIEditor.getDuration() - currentPlayTime < USE_ANIMATION_REMAIN_TIME) {
                        mCanAddAnimation = false;
                    } else {
                        mCanAddAnimation = true;
                    }
                }
            });

        }

        private int c = 0;

        @Override
        public void onDataReady() {
            post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDataReady received");
                    if (mWaitForReady && c > 0) {
                        Log.d(TAG, "onDataReady resume");
                        mWaitForReady = false;
                        mAliyunIEditor.resume();
                    }
                    c++;
                }
            });

        }
    };
    public static final int USE_ANIMATION_REMAIN_TIME = 300 * 1000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEYCODE_VOLUME_DOWN:
                mVolume -= 5;
                if (mVolume < 0) {
                    mVolume = 0;
                }
                mAliyunIEditor.setVolume(mVolume);
                return true;
            case KEYCODE_VOLUME_UP:
                mVolume += 5;
                if (mVolume > 100) {
                    mVolume = 100;
                }
                mAliyunIEditor.setVolume(mVolume);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private boolean isNeedResume = true;

    public void onStart() {
        mIsStop = false;
        if (mViewStack != null) {
            mViewStack.setVisibleStatus(true);
        }
    }

    public void onResume() {
        mTvRight.setEnabled(true);
        if (isNeedResume) {
            playingResume();
        }
        //当删除使用的MV的时候，会发生崩溃，所以在次判断一下mv是否被删除
        if (mLastMVEffect != null) {
            String path = EditorCommon.getMVPath(mLastMVEffect.list, mVideoParam.getOutputWidth(),
                    mVideoParam.getOutputHeight());

            if (!TextUtils.isEmpty(path) && !new File(path).exists()) {
                applyMVEffect(new EffectInfo());
            }
        }
        checkAndRemoveEffects();

    }

    public void onPause() {
        isNeedResume = mAliyunIEditor.isPlaying();
        playingPause();
        mAliyunIEditor.saveEffectToLocal();
    }

    public void onStop() {

        if (mTvRight != null) {
            mTvRight.setEnabled(true);
        }
        mIsStop = true;
        if (mViewStack != null) {
            mViewStack.setVisibleStatus(false);
        }
        if (showToast != null) {
            showToast.cancel();
            showToast = null;
        }
    }

    public void onDestroy() {
        mIsDestroyed = true;
        Dispatcher.getInstance().unRegister(this);
        if (mAliyunIEditor != null) {
            mAliyunIEditor.onDestroy();
        }

        if (mViewStack != null) {
            mViewStack.onDestroy();
        }

        if (mAnimationFilterController != null) {
            mAnimationFilterController.destroyController();
        }

        if (mThumbLineBar != null) {
            mThumbLineBar.stop();
        }

        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }

        if (mCanvasController != null) {
            mCanvasController.release();
        }

        if (mTranscoder != null) {
            if (mIsTranscoding) {
                mTranscoder.cancel();
            } else {
                mTranscoder.dispose();
            }
        }

        if (mViewOperate != null) {
            mViewOperate.setAnimatorListener(null);
            mViewOperate = null;
        }

        if (animatorX != null) {
            animatorX.cancel();
            animatorX.addUpdateListener(null);
            animatorX.addListener(null);
            animatorX = null;
        }

        if (mWatermarkBitmap != null && !mWatermarkBitmap.isRecycled()) {
            mWatermarkBitmap.recycle();
            mWatermarkBitmap = null;
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }

    }

    private List<AliyunClip> mAliYunClipList;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewStack.onActivityResult(requestCode, resultCode, data);
//        if (EditorActivity.request_Code == requestCode && resultCode == Activity.RESULT_OK) {
//            EditorVideHelper.resetVideoTimes(mAliYunClipList, mAliyunIEditor);
//            //重新设置转场动画。
//            if (mTransitionBaseMap != null) {
//                mAliyunIEditor.setTransition(mTransitionBaseMap);
//            }
//            mAliyunIEditor.seek(mViewStack.getVideoStartTime());
//            onClick(mPlayImage);
////            mAliyunIEditor.seek(mViewStack.getVideoStartTime());
//            mAliyunIEditor.play();
//        }
    }

    public boolean onBackPressed() {
        if (mIsTranscoding) {
            //转码过程中无法操作
            showToast = FixedToastUtils.show(getContext(),
                    getResources().getString(R.string.alivc_editor_edit_tip_transcode_no_operate));
            return true;
        }
        if (mViewOperate != null) {
            boolean isShow = mViewOperate.isBottomViewShow();
            // 直接隐藏
            if (isShow) {
                if (mViewOperate != null) {
                    mViewOperate.getBottomView().onBackPressed();
                    hideBottomEditorView();
                }
            }
            return isShow;
        } else {
            return false;
        }
    }


    public Uri getVideoUri() {
        return mUri;
    }

    private Uri mUri;
    private Uri mOutputUri;
    private boolean hasTailAnimation = false;
    private List<MediaInfo> mMediaInfoList;
    private Uri mBaseUri;
    private List<MediaInfo> mTrimMediaInfoList = new ArrayList<>();

    public void setParam(AliyunVideoParam mVideoParam, Uri mUri, boolean hasTailAnimation, boolean hasWaterMark, List<MediaInfo> mediaInfoList) {
        this.hasTailAnimation = hasTailAnimation;
        mBaseUri = this.mUri = mUri;
        this.mVideoParam = mVideoParam;
        this.hasWaterMark = hasWaterMark;
        this.mMediaInfoList = mediaInfoList;
//        mTrimMediaInfoList.clear();
        mStartTime = 0L;
        for (MediaInfo mediaInfo : mMediaInfoList) {
//            mTrimMediaInfoList.add(mediaInfo.clone());
            mEndTime = mEndTime + mediaInfo.duration;
        }
        mEndTime = mEndTime * 1000;
        initEditor();
    }

    public void setReplaceMusic(boolean replaceMusic) {
        isReplaceMusic = replaceMusic;
    }

    private AliyunVideoParam mVideoParam;

    /**
     * 播放时间、显示时间同步接口
     */
    public interface PlayerListener {

        default void onPlayPause() {

        }

        /**
         * 获取当前的播放时间（-->缩略图条位置同步）
         *
         * @return 前的播放时间
         */
        long getCurrDuration();

        /**
         * 获取视频总时间
         *
         * @return 视频总时间
         */
        long getDuration();

        /**
         * 更新时间（-->显示时间同步）
         *
         * @param duration 更新时间
         */
        void updateDuration(long duration);
    }

    /**
     * 根据配置跳转到下一个activity
     */
    private void jumpToNextActivity() {
        if (mOnFinishListener != null) {
//            if (mViewStack != null) {
//                long startTime = mViewStack.getVideoStartTime();
//                long entTime = mViewStack.getVideoEndTime();
//                if (startTime == 0 && (entTime == 0 || entTime / 1000 == mAliyunIEditor.getDuration())) {
//                    //没有裁剪过
//                } else {
////                    uri = EditorVideHelper.upDataJson(mContext,mVideoParam,mAliyunIEditor, startTime, entTime);
////                    uri = EditorVideHelper.copyJson(mContext,mUri, startTime, entTime);
//                }
//            }
            AlivcEditOutputParam outputParam = new AlivcEditOutputParam();
            if (mViewStack != null) {
                outputParam.setVideoStartTime(mViewStack.getVideoStartTime());
                outputParam.setVideoEndTime(mViewStack.getVideoEndTime());
            }

            if (mOutputUri != null) {
                outputParam.setConfigPath(mOutputUri.getPath());
            } else {
                outputParam.setConfigPath(mUri.getPath());
            }

            outputParam.setOutputVideoHeight(mAliyunIEditor.getVideoHeight());
            outputParam.setOutputVideoWidth(mAliyunIEditor.getVideoWidth());
            outputParam.setVideoRatio(((float) mPasterContainerPoint.x) / mPasterContainerPoint.y);
            outputParam.setVideoParam(mVideoParam);
            outputParam.setThumbnailPath(PATH_THUMBNAIL);
            mOnFinishListener.onComplete(outputParam, mAliyunIEditor);
        }
    }


    /**
     * 编辑完成事件监听
     */
    public interface OnFinishListener {
        void onComplete(AlivcEditOutputParam outputParam, AliyunIEditor mAliyunIEditor);

    }

    private OnFinishListener mOnFinishListener;

    public OnFinishListener getOnFinishListener() {
        return mOnFinishListener;
    }

    public void setmOnFinishListener(OnFinishListener finishListener) {
        this.mOnFinishListener = finishListener;
    }


}

