package com.aliyun.svideo.editor.effects.captions;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.manwei.libs.utils.GsonManage;

import java.util.ArrayList;
import java.util.List;

/**
 * 字幕选择器
 */
public class CaptionChooserFragment extends BaseThumbFragment {


    public boolean isOverlayViewTouch;

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void initView(Context context) {
        super.initView(context);
        mOverlayRecyclerView.setOnTouchClickListener(new TouchRecyclerView.OnTouchClickListener() {
            @Override
            public void onSingleClick(RecyclerView view) {
                if (!isOverlayViewTouch) {
                    cleanOverlaySelected();
                }
                isOverlayViewTouch = false;
            }
        });
        mToolsLayout.setOnTouchClickListener(new TouchRecyclerView.OnTouchClickListener() {
            @Override
            public void onSingleClick(RecyclerView view) {
                cleanOverlaySelected();
            }
        });
    }

    public CaptionChooserFragment(@NonNull Context context) {
        super(context);
    }

    private int mInitPosition;

    public void initData(Uri uri, long startTime, long endTime, List<String> blessingList, int initPostion, boolean isShowSelectedView) {
        super.initData(uri, startTime, endTime, isShowSelectedView);
        mAlivcEditView.isShowEditCaptions = true;
        mInitPosition = initPostion;
        if (initPostion < blessingList.size()) {
            hideCaptionHint();
        } else {
            showCaptionHint();
        }
        mBlessingList.clear();
        mBlessingList.addAll(blessingList);
        initToolsLayout();
        mCaptionLineOverlayList.clear();
        if (!isShowSelectedView) {
            ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER, 0, 0).show(mContext.getResources().getString(R.string.paiya_caption_hint));
            ToastUtils.getDefaultMaker().setGravity(-1, -1, -1);
        }
        if (getOverlayFrameLayout() != null) {
            getOverlayFrameLayout().removeAllViews();
        }
        fillOverlayAdapter();
    }

    private void initToolsLayout() {
        if (mInitPosition > 0 && mInitPosition == mBlessingList.size() - 1) {
            return;
        }
        mToolsLayout.setCaptionChooserFragment(this);
        //刷新分词器
        mToolsLayout.fillAdapter(mBlessingList, mInitPosition);
        mToolsLayout.setOnAddCaptionTextCallback(new CaptionView.OnAddCaptionTextCallback() {
            @Override
            public void onAddText(int position, String blessing) {
                addOverlayView(position, blessing);
                if (position == mBlessingList.size() - 1) {
                    showCaptionHint();
                } else {
                    hideCaptionHint();
                }
            }
        });
    }

    public boolean isAddCaptionText() {
        //当最后一条字幕距离末尾小于最小距离时，禁止再添加
        if (mCaptionLineOverlayList != null && !mCaptionLineOverlayList.isEmpty()) {
            CaptionLineOverlay captionLineOverlay = mCaptionLineOverlayList.get(mCaptionLineOverlayList.size() - 1);
            return captionLineOverlay.getMarginEnd() > captionLineOverlay.mMinDistance + captionLineOverlay.minIntervalDistance + captionLineOverlay.mThumbWidth * 2;
        }
        return true;
    }


    private final List<CaptionLineOverlay> mCaptionLineOverlayList = new ArrayList<>();
    private int mSelectedOverlayPosition = -1;


    /**
     * 取消覆盖层的选中
     */
    public void cleanOverlaySelected() {
        if (mSelectedOverlayPosition >= 0 && mCaptionLineOverlayList.size() > mSelectedOverlayPosition) {
            mCaptionLineOverlayList.get(mSelectedOverlayPosition).showThumb(false);
        }
    }

    public void addInitCaptionOverlayView(int position, String text, long startTime, long endTime) {
        int width = (int) (getTimelineBarViewWidth() + mOverlayThumbWidth * 2);
        int distance = duration2Distance(endTime - startTime) + mOverlayThumbWidth * 2;
        int startWidth = duration2Distance(startTime);
        CaptionLineOverlay overlay = new CaptionLineOverlay(mContext);
        overlay.initData(position, mDuration, mCoverItemWidth, mCaptionLineOverlayList);

        overlay.getOverlayViewByDuration(startWidth, distance, text, width);

        overlay.setOnCaptionLineOverlayClickListener(new CaptionLineOverlay.OnCaptionLineOverlayClickListener() {
            @Override
            public void onSingleClick(int position, CaptionLineOverlay overlay) {
                isOverlayViewTouch = true;
                if (mSelectedOverlayPosition >= 0 && mCaptionLineOverlayList.size() > mSelectedOverlayPosition) {
                    mCaptionLineOverlayList.get(mSelectedOverlayPosition).showThumb(false);
                }
                overlay.showThumb(true);
                mSelectedOverlayPosition = position;
                if (getOverlayFrameLayout() != null) {
                    getOverlayFrameLayout().bringChildToFront(overlay);
                    getOverlayFrameLayout().updateViewLayout(overlay, overlay.getLayoutParams());
                }
            }

            @Override
            public void onDoubleClick(int position, CaptionLineOverlay overlay, float offsetX) {
                int addX = 0;
                long positionTime = mAliyunIEditor.getCurrentPlayPosition();
                if (mAliyunIEditor.getCurrentStreamPosition() > overlay.getEndTime()) {
                    positionTime = overlay.getEndTime() - distance2Duration(addX);
                    setRvScrollTo((int) offsetX);
                } else if (mAliyunIEditor.getCurrentStreamPosition() < overlay.getStartTime()) {
                    positionTime = overlay.getEndTime() + distance2Duration(addX);
                    setRvScrollTo((int) offsetX);
                }
                mAlivcEditView.onCaptionDoubleClick(position, positionTime);
            }

            @Override
            public void onUpdateTime(long playTime) {
                setPlaySeekDraw(playTime);
            }

            @Override
            public void onChangeTime(int position, long startTime, long endTime, int startType) {
                if (mOnEffectChangeListener != null) {
                    mOnEffectChangeListener.onChangeTime(position, startTime, endTime);
                }
                if (startType == 0) {
                    mAliyunIEditor.seek(startTime);
                } else {
                    mAliyunIEditor.seek(endTime);
                }
            }

            @Override
            public void onMoveOffsetX(int offsetX) {
                mThumbnailRecyclerView.scrollBy((int) offsetX, 0);
                mCurrScroll += offsetX;
            }
        });
        if (mSelectedOverlayPosition >= 0 && mCaptionLineOverlayList.size() > mSelectedOverlayPosition) {
            mCaptionLineOverlayList.get(mSelectedOverlayPosition).showThumb(false);
        }

        if (mAliyunIEditor.getCurrentPlayPosition() >= startTime && mAliyunIEditor.getCurrentPlayPosition() <= endTime) {
            mSelectedOverlayPosition = position;
            overlay.showThumb(true);
        } else {
            overlay.showThumb(false);
        }

        if (getOverlayFrameLayout() != null) {
            getOverlayFrameLayout().addView(overlay);
        }
        mCaptionLineOverlayList.add(overlay);
    }

    /**
     * 添加覆盖层
     */
    private void addOverlayView(int position, String blessing) {
        int baseWidth = (int) (getTimelineBarViewWidth() + mOverlayThumbWidth * 2);
        int distance = duration2Distance(mCacheIntervalTime * 3) + mOverlayThumbWidth * 2 - mMinIntervalDistance;

        int startWidth = 0;
        CaptionLineOverlay overlay = new CaptionLineOverlay(mContext);
        overlay.initData(position, mDuration, mCoverItemWidth, mCaptionLineOverlayList);
        if (!mCaptionLineOverlayList.isEmpty()) {
            CaptionLineOverlay endOverlay = mCaptionLineOverlayList.get(mCaptionLineOverlayList.size() - 1);
            //上一个的尾部位置 - 右边滑块的宽度 + mCoverItemWidth * 0.1f 的间距
            startWidth = (int) (endOverlay.getOverlayRight() - mOverlayThumbWidth * 2 + mMinIntervalDistance);
            endOverlay.showThumb(false);
        }

        overlay.getOverlayViewByDuration(startWidth, distance, blessing, baseWidth);

        overlay.setOnCaptionLineOverlayClickListener(new CaptionLineOverlay.OnCaptionLineOverlayClickListener() {
            @Override
            public void onSingleClick(int position, CaptionLineOverlay overlay) {
                isOverlayViewTouch = true;
                if (mSelectedOverlayPosition >= 0 && mCaptionLineOverlayList.size() > mSelectedOverlayPosition) {
                    mCaptionLineOverlayList.get(mSelectedOverlayPosition).showThumb(false);
                }
                overlay.showThumb(true);
                mSelectedOverlayPosition = position;
                if (getOverlayFrameLayout() != null) {
                    getOverlayFrameLayout().bringChildToFront(overlay);
                    getOverlayFrameLayout().updateViewLayout(overlay, overlay.getLayoutParams());
                }
            }

            @Override
            public void onDoubleClick(int position, CaptionLineOverlay overlay, float offsetX) {
                int addX = 0;
                long positionTime = mAliyunIEditor.getCurrentPlayPosition();
                if (mAliyunIEditor.getCurrentStreamPosition() > overlay.getEndTime()) {
                    positionTime = overlay.getEndTime() - distance2Duration(addX);
                    setRvScrollTo((int) offsetX);
                } else if (mAliyunIEditor.getCurrentStreamPosition() < overlay.getStartTime()) {
                    positionTime = overlay.getEndTime() + distance2Duration(addX);
                    setRvScrollTo((int) offsetX);
                }
                mAlivcEditView.onCaptionDoubleClick(position, positionTime);
            }

            @Override
            public void onUpdateTime(long playTime) {
                setPlaySeekDraw(playTime);
            }

            @Override
            public void onChangeTime(int position, long startTime, long endTime, int startType) {
                if (mOnEffectChangeListener != null) {
                    mOnEffectChangeListener.onChangeTime(position, startTime, endTime);
                }
                isAddCaptionView = true;
//                Log.e("AAA", "startTime:" + startTime + ";endTime:" + endTime);
                if (startType == 0) {
                    mAliyunIEditor.seek(startTime);
                } else {
                    mAliyunIEditor.seek(endTime);
                }
            }

            @Override
            public void onMoveOffsetX(int offsetX) {
                mThumbnailRecyclerView.scrollBy((int) offsetX, 0);
                mCurrScroll += offsetX;
            }
        });
        if (mSelectedOverlayPosition >= 0 && mCaptionLineOverlayList.size() > mSelectedOverlayPosition) {
            mCaptionLineOverlayList.get(mSelectedOverlayPosition).showThumb(false);
        }
        mSelectedOverlayPosition = position;
        if (getOverlayFrameLayout() != null) {
            getOverlayFrameLayout().addView(overlay);
        }
        mCaptionLineOverlayList.add(overlay);

        overlay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                overlay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                long startTime = overlay.getStartTime();
                long endTime = overlay.getEndTime();
                addFontEffect(position, blessing, startTime, endTime);
            }
        });
    }

    /**
     * 添加字体
     */
    private void addFontEffect(int position, String blessing, long startTime, long endTime) {
        isAddCaptionView = true;
        mAliyunIEditor.seek(startTime);
        EffectInfo effectInfo = new EffectInfo();
        effectInfo.type = UIEditorPage.CAPTION;
        effectInfo.setPath(null);
        effectInfo.effectTextPosition = position;
        effectInfo.effectText = blessing;
        effectInfo.fontPath = "system_font";
        effectInfo.startTime = startTime;
        effectInfo.endTime = endTime;
        effectInfo.streamStartTime = startTime;
        effectInfo.streamEndTime = endTime;
        if (mOnEffectChangeListener != null) {
            mOnEffectChangeListener.onEffectChange(effectInfo);
        }
    }


    /**
     * 时间转为尺寸
     */
    private int duration2Distance(long duration) {
        float distance = duration * mCoverItemWidth / mCacheIntervalTime;
        return Math.round(distance);
    }

    /**
     * 尺寸转为时间
     */
    private long distance2Duration(float distance) {
        float duration = distance * mCacheIntervalTime / mCoverItemWidth;
        return Math.round(duration);
    }


    public static List<String> getCaptionStringToList() throws Exception {
        String text = SPUtils.getInstance().getString("mBlessingList");
        return GsonManage.fromJsonList(text, String.class);
    }


    public void setBlessingChanger(int position, String text) {
        if (mBlessingList != null && mBlessingList.size() > position) {
            mBlessingList.set(position, text);
        }
        if (mCaptionLineOverlayList != null && mCaptionLineOverlayList.size() > position) {
            mCaptionLineOverlayList.get(position).setBlessingChanger(text);
        }
    }


}
