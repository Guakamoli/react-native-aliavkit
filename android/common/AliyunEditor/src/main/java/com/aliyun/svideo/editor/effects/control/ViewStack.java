/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.control;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcRollCaptionSubtitleBean;
import com.aliyun.svideo.editor.bean.PasterRestoreBean;
import com.aliyun.svideo.editor.editor.AbstractPasterUISimpleImpl;
import com.aliyun.svideo.editor.effectmanager.MoreAnimationEffectActivity;
import com.aliyun.svideo.editor.effectmanager.MoreCaptionActivity;
import com.aliyun.svideo.editor.effectmanager.MoreMVActivity;
import com.aliyun.svideo.editor.effectmanager.RollCaptionSubtitleActivity;
import com.aliyun.svideo.editor.effects.audiomix.MusicChooser;
import com.aliyun.svideo.editor.effects.caption.CaptionChooserView;
import com.aliyun.svideo.editor.effects.captions.CaptionChooserFragment;
import com.aliyun.svideo.editor.effects.captions.ImportCaptionDialog;
import com.aliyun.svideo.editor.effects.captions.PasterUICaptionsImpl;
import com.aliyun.svideo.editor.effects.trim.TrimVideoFragment;
import com.aliyun.svideo.editor.effects.trim.TrimVideoView;
import com.aliyun.svideo.editor.effects.cover.CoverChooserView;
import com.aliyun.svideo.editor.effects.filter.AnimationFilterChooserView;
import com.aliyun.svideo.editor.effects.filter.ColorFilterChooserView;
import com.aliyun.svideo.editor.effects.imv.ImvChooserMediator;
import com.aliyun.svideo.editor.effects.overlay.OverlayChooserView;
import com.aliyun.svideo.editor.effects.paint.PaintChooserView;
import com.aliyun.svideo.editor.effects.rollcaption.RollCaptionEffectChooseView;
import com.aliyun.svideo.editor.effects.sound.SoundEffectChooseView;
import com.aliyun.svideo.editor.effects.time.TimeChooserView;
import com.aliyun.svideo.editor.effects.transition.TransitionChooserView;
import com.aliyun.svideo.editor.effects.videoeq.VideoEqChooserView;
import com.aliyun.svideo.editor.util.FixedToastUtils;
import com.aliyun.svideo.editor.util.SharedPreferenceUtils;
import com.aliyun.svideo.editor.view.AlivcEditView;
import com.aliyun.svideo.editor.view.EditorVideHelper;
import com.aliyun.svideo.editor.viewoperate.ViewOperator;
import com.aliyun.svideosdk.common.AliyunIClipConstructor;
import com.aliyun.svideosdk.common.struct.common.AliyunClip;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.effect.EffectCaption;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.effect.EffectText;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.AliyunRollCaptionComposer;
import com.aliyun.svideosdk.editor.AudioEffectType;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.manwei.libs.dialog.DialogUtils;
import com.manwei.libs.dialog.OnDialogListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 底部导航栏的 view stack
 */
public class ViewStack {

    private final static String TAG = ViewStack.class.getName();
    private AlivcEditView rootView;
    private ViewOperator mViewOperator;
    private final Context mContext;
    private EditorService mEditorService;
    private AliyunRollCaptionComposer mAliyunRollCaptionComposer;
    private OnEffectChangeListener mOnEffectChangeListener;
    private TransitionChooserView.OnPreviewListener mOnPreviewListener;

    private ColorFilterChooserView mColorFilterCHoosrView;
    private VideoEqChooserView mVideoEqChooserView;
    private MusicChooser mAudioMixChooserMediator;
    private ImvChooserMediator mImvChooserMediator;
    private TimeChooserView mTimeChooserView;
    private PaintChooserView mPaintChooserView;

    private TransitionChooserView mTransitionChooserView;

    private TrimVideoView mTrimVideoView;//裁剪
    private TrimVideoFragment mTrimVideoFragment;//裁剪

    private OverlayChooserView mOverlayChooserView;//动图
    private CaptionChooserView mCaptionChooserView;//字幕
    private AnimationFilterChooserView mAnimationChooserView;//特效
    private OnEffectActionLister mOnEffectActionLister;
    private AlivcEditView.PlayerListener mPlayerListener;
    /**
     * 封面选择view
     */
    private CoverChooserView mCoverChooserView;
    private SoundEffectChooseView mSoundEffectChooseView;
    private RollCaptionEffectChooseView mRollCaptionEffectChooseView;

    /**
     * 编辑核心接口类
     */
    private AliyunIEditor mAliyunIEditor;

    public ViewStack(Context context, AlivcEditView editView, ViewOperator viewOperator) {
        mContext = context;
        rootView = editView;
        mViewOperator = viewOperator;
    }

    private long mStartTime;
    private long mEndTime;

    public long getVideoStartTime() {
        return mStartTime;
    }

    public long getVideoEndTime() {
        return mEndTime;
    }

    public void setAliyunIEditor(AliyunIEditor aliyunIEditor) {
        mAliyunIEditor = aliyunIEditor;

    }

    /**
     * 点击空白出弹窗消失
     */
    public void hideBottomEditorView(UIEditorPage page) {
        switch (page) {
//            case TRIM:
//                if (mTrimVideoView != null) {
//                    mTrimVideoView.hideBottomView();
//                }
//                break;
            default:
                break;
        }
    }


    private ImportCaptionDialog mCaptionDialog;
    public CaptionChooserFragment mCaptionLayout;


    /**
     * 展示字幕编辑View
     */
    private void showCaptionLayout(List<String> blessingList, boolean isShowSelectedView) {
        if (mCaptionLayout == null) {
            mCaptionLayout = new CaptionChooserFragment(mContext);
            mCaptionLayout.setActivity(mActivity);
            mCaptionLayout.setBaseData(rootView, mAliyunIEditor);
            mCaptionLayout.setEditorService(mEditorService);
            mCaptionLayout.setOnEffectChangeListener(mOnEffectChangeListener);
            mCaptionLayout.setOnEffectActionLister(mOnEffectActionLister);
            mCaptionLayout.setPlayerListener(mPlayerListener);
        }
        rootView.setCaptionEdit();
        mStartTime = 0;
        mEndTime = mAliyunIEditor.getDuration();
        List<PasterUICaptionsImpl> captionList = rootView.getCaptionsUiList();
        mCaptionLayout.initData(rootView.getVideoUri(), mStartTime, mEndTime, blessingList, captionList.size(), isShowSelectedView);
        for (int i = 0; i < captionList.size(); i++) {
            PasterUICaptionsImpl caption = captionList.get(i);
            mCaptionLayout.addInitCaptionOverlayView(i, caption.getTextString(), caption.getStartTime(), caption.getEndTime());
        }
        mViewOperator.showBottomView(mCaptionLayout);

        BaseChooser bottomView = mViewOperator.getBottomView();
        if (bottomView != null && bottomView.isPlayerNeedZoom()) {
            //缩放
            rootView.setPasterDisplayScale(ViewOperator.SCALE_SIZE);
        }
    }


    public void setActiveIndex(int value) {
        UIEditorPage index = UIEditorPage.get(value);
        switch (index) {
            case CAPTION://字幕
                rootView.playingPause();
                List<String> stringList = null;
                try {
                    stringList = CaptionChooserFragment.getCaptionStringToList();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (stringList != null && !stringList.isEmpty()) {
                    showCaptionLayout(stringList, true);
                    break;
                }
                if (mCaptionDialog == null) {
                    mCaptionDialog = new ImportCaptionDialog((AppCompatActivity) mContext);
                    mCaptionDialog.setOnDialogDismissCallback(new ImportCaptionDialog.OnDialogDismissCallback() {
                        @Override
                        public void onClean() {
                            rootView.playingResume();
                            mViewOperator.showTopLayout();
                        }

                        @Override
                        public void onConfirm(List<String> blessingList) {
//                            mViewOperator.showTopLayout();
                            showCaptionLayout(blessingList, false);
                            mCaptionDialog = null;
                        }
                    });
                }
                mCaptionDialog.setBlessingContent(rootView.getBlessingContent());
                mViewOperator.hideTopLayout();
                mCaptionDialog.setStateExpanded();
                mCaptionDialog.show();
                return;
            case TRIM://剪裁
//                rootView.playingPause();
                if (mTrimVideoFragment == null) {
                    mTrimVideoFragment = new TrimVideoFragment(mContext);
                    mTrimVideoFragment.setEditorService(mEditorService);
                    mTrimVideoFragment.setOnEffectChangeListener(mOnEffectChangeListener);
                    mTrimVideoFragment.setOnEffectActionLister(mOnEffectActionLister);
                    mTrimVideoFragment.setPlayerListener(mPlayerListener);
                    mTrimVideoFragment.setBaseData(rootView, mAliyunIEditor);
                    mTrimVideoFragment.initData(rootView.getVideoUri());
                }
                if (rootView.isChangeEffect()) {
                    String hintStr = mContext.getResources().getString(R.string.paiya_clean_add_trim_hint);
                    String cancelStr = mContext.getResources().getString(R.string.cancel);
                    String confirmStr = mContext.getResources().getString(R.string.confirm);
                    DialogUtils.showDialog((AppCompatActivity) mContext, hintStr, cancelStr, confirmStr, new OnDialogListener() {
                        @Override
                        public void onRightClick() {
                            rootView.cleanAllEffect();
                            mTrimVideoFragment.showPage();
                            mViewOperator.showBottomView(mTrimVideoFragment);
                            BaseChooser bottomView = mViewOperator.getBottomView();
                            if (bottomView != null && bottomView.isPlayerNeedZoom()) {
                                //缩放
                                rootView.setPasterDisplayScale(ViewOperator.SCALE_SIZE);
                            }
                        }
                    });
                    return;
                } else {
                    mTrimVideoFragment.showPage();
                    mViewOperator.showBottomView(mTrimVideoFragment);
                }
                break;
            case FILTER:
                // 颜色滤镜
                if (mColorFilterCHoosrView == null) {
                    mColorFilterCHoosrView = new ColorFilterChooserView(mContext);
                }
                //没滤镜，把选中重置
                if (rootView.getLastColorFilterEffect() != null && TextUtils.isEmpty(rootView.getLastColorFilterEffect().getPath())) {
                    mColorFilterCHoosrView.reSetSelectedPosition();
                }
                mViewOperator.showBottomView(mColorFilterCHoosrView);
                break;
            case FILTER_EFFECT:
                rootView.playingPause();
                // 特效滤镜
                if (mAnimationChooserView == null) {
                    mAnimationChooserView = new AnimationFilterChooserView(mContext);
                    mAnimationChooserView.setEditorService(mEditorService);
                    mAnimationChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                    mAnimationChooserView.setOnEffectActionLister(mOnEffectActionLister);
                    mAnimationChooserView.setPlayerListener(mPlayerListener);
                }

                mAnimationChooserView.setFirstShow(SharedPreferenceUtils.isAnimationEffectFirstShow(mContext));
                mAnimationChooserView.addThumbView(rootView.getThumbLineBar());
                mViewOperator.showBottomView(mAnimationChooserView);
                SharedPreferenceUtils.setAnimationEffectFirstShow(mContext, false);
                break;
            case SOUND:
                // 音效
                if (rootView.isHasRecordMusic()) {
                    FixedToastUtils.show(mContext,
                            mContext.getResources().getString(R.string.alivc_editor_dialog_sound_not_support));
                    break;
                }
                if (mSoundEffectChooseView == null) {
                    mSoundEffectChooseView = new SoundEffectChooseView(mContext);
                    mSoundEffectChooseView.setOnEffectChangeListener(mOnEffectChangeListener);
                }

                //没音效了，把选中重置成原声
                if (rootView.getLastSoundEffect() != null && rootView.getLastSoundEffect().audioEffectType == AudioEffectType.EFFECT_TYPE_DEFAULT) {
                    mSoundEffectChooseView.reSetSelectedPosition();
                }

                mViewOperator.showBottomView(mSoundEffectChooseView);
                break;
            case MV:
                // MV
                if (mImvChooserMediator == null) {
                    mImvChooserMediator = new ImvChooserMediator(mContext);
                    mImvChooserMediator.setEditorService(mEditorService);
                    mImvChooserMediator.setOnEffectChangeListener(mOnEffectChangeListener);
                }
                if (rootView.getLastMVEffect() != null && rootView.getLastMVEffect().list == null) {
                    mImvChooserMediator.reSetSelectedPosition();
                }
                mViewOperator.showBottomView(mImvChooserMediator);
                break;
            case OVERLAY:
                rootView.playingPause();
                // 动图
                if (mOverlayChooserView == null) {
                    mOverlayChooserView = new OverlayChooserView(mContext);
                    mOverlayChooserView.setEditorService(mEditorService);
                    mOverlayChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                    mOverlayChooserView.setOnEffectActionLister(mOnEffectActionLister);
                }
                setLayoutParams(mOverlayChooserView);
                mOverlayChooserView.addThumbView(rootView.getThumbLineBar());
                mViewOperator.showBottomView(mOverlayChooserView);
                break;
            case TEXT:
                rootView.playingPause();
                // 字幕
                if (mCaptionChooserView == null) {
                    mCaptionChooserView = new CaptionChooserView(mContext);
                    mCaptionChooserView.setEditorService(mEditorService);
                    mCaptionChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                    mCaptionChooserView.setOnEffectActionLister(mOnEffectActionLister);
                }
                setLayoutParams(mCaptionChooserView);
                mCaptionChooserView.addThumbView(rootView.getThumbLineBar());
                mViewOperator.showBottomView(mCaptionChooserView);
                break;
            case AUDIO_MIX:
                // 音乐
                //合拍不允许添加音乐
                if (rootView.isMaxRecord()) {
                    FixedToastUtils.show(mContext, mContext.getResources().getString(R.string.alivc_mix_record_waring_content));
                    break;
                }
                if (mAudioMixChooserMediator == null) {
                    mAudioMixChooserMediator = new MusicChooser(mContext);
                    mAudioMixChooserMediator.setOnEffectChangeListener(mOnEffectChangeListener);
                    mAudioMixChooserMediator.setOnEffectActionLister(mOnEffectActionLister);
                }
                long duration = rootView.getEditor().getStreamDuration() / 1000;
                mAudioMixChooserMediator.setStreamDuration(duration);
                mViewOperator.showBottomView(mAudioMixChooserMediator);
                if (rootView.getLastMusicBean() == null || rootView.getLastMusicBean().getSource() == null || (TextUtils.isEmpty(rootView.getLastMusicBean().getSource().getPath()) && TextUtils.isEmpty(rootView.getLastMusicBean().getSource().getURL()))) {
                    mAudioMixChooserMediator.reSetSelectedPosition();
                }
                break;
            case PAINT:
                // 涂鸦
                if (mPaintChooserView == null) {
                    mPaintChooserView = new PaintChooserView(mContext);
                    mPaintChooserView.setEditorService(mEditorService);
                    mPaintChooserView.setOnEffectActionLister(mOnEffectActionLister);
                    mPaintChooserView.setPaintSelectListener(new PaintChooserView.PaintSelect() {
                        @Override
                        public void onColorSelect(int color) {
                            if (rootView.mCanvasController != null) {
                                rootView.mCanvasController.setCurrentColor(color);
                            }
                        }

                        @Override
                        public void onSizeSelect(float size) {
                            if (rootView.mCanvasController != null) {
                                rootView.mCanvasController.setCurrentSize(size);
                            }

                        }

                        @Override
                        public void onUndo() {
                            rootView.mCanvasController.undo();
                        }
                    });
                }
                mViewOperator.showBottomView(mPaintChooserView);
                break;
            case TIME:
                rootView.playingPause();
                // 时间
                if (rootView.getEditor().getSourcePartManager().getAllClips().size() > 1) {
                    FixedToastUtils.show(mContext,
                            mContext.getResources().getString(R.string.alivc_editor_dialog_time_tip_not_support));
                    break;
                }
                if (mTimeChooserView == null) {
                    mTimeChooserView = new TimeChooserView(mContext);
                    mTimeChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                    mTimeChooserView.setEditorService(mEditorService);
                    mTimeChooserView.setOnEffectActionLister(mOnEffectActionLister);
                }
                mTimeChooserView.setFirstShow(SharedPreferenceUtils.isTimeEffectFirstShow(mContext));
                mTimeChooserView.addThumbView(rootView.getThumbLineBar());
                mViewOperator.showBottomView(mTimeChooserView);
                SharedPreferenceUtils.setTimeEffectFirstShow(mContext, false);
                break;
            case TRANSITION:
                // 转场
                AliyunIClipConstructor clipConstructor = TransitionChooserView.isClipLimit(rootView.getEditor());
                if (clipConstructor == null) {
                    Toast.makeText(mContext, mContext.getString(R.string.alivc_editor_dialog_transition_tip_limit),
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                if (mTransitionChooserView == null) {
                    mTransitionChooserView = new TransitionChooserView(mContext);
                    mTransitionChooserView.setEditorService(mEditorService);
                    mTransitionChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                    mTransitionChooserView.setOnEffectActionLister(mOnEffectActionLister);
                    mTransitionChooserView.setOnPreviewListener(mOnPreviewListener);
                }
                mTransitionChooserView.initTransitionAdapter(clipConstructor);
                setLayoutParams(mTransitionChooserView);
                mViewOperator.showBottomView(mTransitionChooserView);
                break;
            case COVER:
                // show cover select view
                if (mCoverChooserView == null) {
                    mCoverChooserView = new CoverChooserView(mContext);
                    mCoverChooserView.setOnEffectActionLister(mOnEffectActionLister);
                }
                mCoverChooserView.addThumbView(rootView.getThumbLineBar());
                boolean isFirstShow = SharedPreferenceUtils.isCoverViewFirstShow(mContext);
                if (isFirstShow) {
                    SharedPreferenceUtils.setCoverViewFirstShow(mContext, false);
                }
                mCoverChooserView.setFirstShow(isFirstShow);
                mViewOperator.showBottomView(mCoverChooserView);

                break;
            case ROLL_CAPTION:
                //翻转字幕
                if (mRollCaptionEffectChooseView == null) {
                    mRollCaptionEffectChooseView = new RollCaptionEffectChooseView(mContext);
                    mRollCaptionEffectChooseView.setEditorService(mEditorService);
                    mRollCaptionEffectChooseView.setOnEffectActionLister(mOnEffectActionLister);
                    mRollCaptionEffectChooseView.setOnEffectChangeListener(mOnEffectChangeListener);
                }
                mRollCaptionEffectChooseView.setAliyunRollCaptionComposer(mAliyunRollCaptionComposer);
                mViewOperator.showBottomView(mRollCaptionEffectChooseView);
                break;
            case VIDEOEQ:
                if (mVideoEqChooserView == null) {
                    mVideoEqChooserView = new VideoEqChooserView(mContext);
                }
                mViewOperator.showBottomView(mVideoEqChooserView);
                break;
            default:
                Log.d(TAG, "点击编辑效果，方法setActiveIndex未匹配");
                return;
        }
        BaseChooser bottomView = mViewOperator.getBottomView();
        if (bottomView != null && bottomView.isPlayerNeedZoom()) {
            //缩放
            rootView.setPasterDisplayScale(ViewOperator.SCALE_SIZE);
        }

    }

    /**
     * 设置LayoutParams
     *
     * @param baseChooser view
     */
    private void setLayoutParams(BaseChooser baseChooser) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        baseChooser.setLayoutParams(layoutParams);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case BaseChooser.TRANSITION_EFFECT_REQUEST_CODE:
                if (mTransitionChooserView == null) {
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreAnimationEffectActivity.SELECTD_ID, 0);
                    mTransitionChooserView.setCurrResourceID(id);
                } else {
                    mTransitionChooserView.setCurrResourceID(-1);
                }
                break;
            case BaseChooser.ANIMATION_FILTER_REQUEST_CODE:
                if (mAnimationChooserView == null) {
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreAnimationEffectActivity.SELECTD_ID, 0);
                    mAnimationChooserView.setCurrResourceID(id);
                } else {
                    mAnimationChooserView.setCurrResourceID(-1);
                }
                break;
            case BaseChooser.CAPTION_REQUEST_CODE:
                if (mCaptionChooserView == null) {
                    break;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreCaptionActivity.SELECTED_ID, 0);
                    mCaptionChooserView.setCurrResourceID(id);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    mCaptionChooserView.setCurrResourceID(-1);
                }
                break;
            case BaseChooser.IMV_REQUEST_CODE:
                if (mImvChooserMediator == null) {
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreMVActivity.SELECTD_ID, 0);
                    mImvChooserMediator.setCurrResourceID(id);
                } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
                    int selectedId = data.getIntExtra(MoreMVActivity.SELECTD_ID, 0);
                    mImvChooserMediator.setCurrResourceID(selectedId);
                }
                break;
            case BaseChooser.PASTER_REQUEST_CODE:
                if (mOverlayChooserView == null) {
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreCaptionActivity.SELECTED_ID, 0);
                    mOverlayChooserView.setCurrResourceID(id);
                } else {
                    mOverlayChooserView.setCurrResourceID(-1);
                }
                break;
            case BaseChooser.ROLL_CAPTION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (mOnEffectChangeListener != null) {
                        EffectInfo effectInfo = new EffectInfo();
                        if (data != null) {
                            ArrayList<AlivcRollCaptionSubtitleBean> subtitleBeans = (ArrayList<AlivcRollCaptionSubtitleBean>) data.getSerializableExtra(RollCaptionSubtitleActivity.INTENT_ROLL_CAPTION_SUBTITLE_LIST);
                            if (mRollCaptionEffectChooseView != null) {
                                mRollCaptionEffectChooseView.setUseFamilyColor(false);
                                mRollCaptionEffectChooseView.setSubtitleList(subtitleBeans);
                            }
                        }
                        effectInfo.type = UIEditorPage.ROLL_CAPTION;
                        mOnEffectChangeListener.onEffectChange(effectInfo);
                    }
                }
                break;
            default:
                break;
        }
    }

    private AppCompatActivity mActivity;


    public void setActivity(AppCompatActivity activity) {
        this.mActivity = activity;
    }

    public void setEditorService(EditorService editorService) {
        mEditorService = editorService;
    }

    public void setAliyunRollCaptionComposer(AliyunRollCaptionComposer aliyunRollCaptionComposer) {
        this.mAliyunRollCaptionComposer = aliyunRollCaptionComposer;
    }

    public void setEffectChange(OnEffectChangeListener onEffectChangeListener) {
        mOnEffectChangeListener = onEffectChangeListener;
    }

    public void setOnEffectActionLister(OnEffectActionLister effectActionLister) {
        mOnEffectActionLister = effectActionLister;
    }

    /**
     * 设置view的可见状态, 会在activity的onStart和onStop中调用
     *
     * @param isVisible true: 可见, false: 不可见
     */
    public void setVisibleStatus(boolean isVisible) {
        if (mAudioMixChooserMediator != null) {
            mAudioMixChooserMediator.setVisibleStatus(isVisible);
        }
    }

    //转场预览监听
    public void setOnTransitionPreviewListener(TransitionChooserView.OnPreviewListener onPreviewListener) {
        mOnPreviewListener = onPreviewListener;
    }

    /**
     * 播放时间回调器
     *
     * @param playerListener PlayerListener
     */
    public void setPlayerListener(AlivcEditView.PlayerListener playerListener) {
        mPlayerListener = playerListener;
    }


    public void onDestroy() {
        if (mTrimVideoView != null) {
            mTrimVideoView.onDestroy();
        }
        if (mTrimVideoFragment != null) {
            mTrimVideoFragment.onDestroy();
        }
        if (mCaptionLayout != null) {
            mCaptionLayout.onDestroy();
        }
    }
}
