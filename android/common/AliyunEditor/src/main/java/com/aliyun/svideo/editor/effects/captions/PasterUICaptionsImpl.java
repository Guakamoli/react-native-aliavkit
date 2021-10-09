package com.aliyun.svideo.editor.effects.captions;

import android.view.View;

import com.aliyun.svideo.editor.editor.PasterUITextImpl;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.widget.BaseAliyunPasterView;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.AliyunPasterController;

/**
 * 字幕导入
 */
public class PasterUICaptionsImpl extends PasterUITextImpl {

    public PasterUICaptionsImpl(BaseAliyunPasterView pasterView, AliyunPasterController controller, OverlayThumbLineBar thumbLineBar, AliyunIEditor editor, boolean completed) {
        super(pasterView, controller, thumbLineBar, editor, completed);
        mEditorPage = UIEditorPage.CAPTION;
    }

    public long getStartTime() {
        if (mController != null) {
            return mController.getPasterStartTime();
        }
        return 0;
    }

    public long getEndTime() {
        if (mController != null) {
            return mController.getPasterStartTime() + mController.getPasterDuration();
        }
        return 0;
    }


    public String getTextString() {
        if (mText != null && mText.getText() != null) {
            return mText.getText().toString();
        }
        return "";
    }


    public void setStartEndTime(long startTime, long endTime) {
        if (mController != null) {
            mController.setPasterStartTime(startTime);
            mController.setPasterDuration(endTime - startTime);
        }
    }

    /**
     * 移除动图（将动图从UI框架中移除掉）
     */
    public void removePaster() {
        super.removePaster();
    }

    /**
     * 显示动图（将动图渲染到视频上，并且从UI层面消失）
     */
    public void editCompleted() {
        super.editTimeCompleted();
    }

    /**
     * 隐藏动图（将动图从视频渲染上移除掉，并且显示在UI层面）
     */
    public void editStart() {
        super.editTimeStart();
    }

    public void showCaptionView() {
        mPasterView.setVisibility(View.VISIBLE);
        editStart();
    }

    public void hideCaptionView() {
        mPasterView.setVisibility(View.GONE);
        editCompleted();
    }

    private int mCurrentPosition;

    public void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }


    private long mCurrentPlayTime;

    public void setCurrentPlayTime(long currentPlayTime) {
        mCurrentPlayTime = currentPlayTime;
    }


    /**
     * 进入字幕编辑，初始化当前显示的View.
     */
    public PasterUICaptionsImpl initShow(long currentPlayTime) {
        if (currentPlayTime >= getStartTime() && currentPlayTime <= getEndTime()) {
            showCaptionView();
            mCurrentPlayTime = currentPlayTime;
            isEditShow = true;
            return this;
        } else {
            hideCaptionView();
            mCurrentPlayTime = currentPlayTime;
            isEditShow = false;
            return null;
        }
    }

    /**
     * 根据播放进度设置当前 View 是否显示
     */
    public boolean setShow(long currentPlayTime) {
        if (mCurrentPlayTime == currentPlayTime) {
            isEditShow = true;
            return true;
        }
        if (currentPlayTime >= getStartTime() && currentPlayTime <= getEndTime()) {
            showCaptionView();
            mCurrentPlayTime = currentPlayTime;
            isEditShow = true;
            return true;
        } else {
            hideCaptionView();
            mCurrentPlayTime = currentPlayTime;
            isEditShow = false;
            return false;
        }
    }


    public boolean resetShow(long currentPlayTime) {
        if (currentPlayTime >= getStartTime() && currentPlayTime <= getEndTime()) {
            showCaptionView();
            mCurrentPlayTime = currentPlayTime;
            isEditShow = true;
            return true;
        } else {
            hideCaptionView();
            mCurrentPlayTime = currentPlayTime;
            isEditShow = false;
            return false;
        }
    }


    /**
     * 是否正在展示UI编辑
     */
    private boolean isEditShow;


    public boolean isEditShow() {
        return isEditShow;
    }


//    @Override
//    public UIEditorPage getEditorPage() {
//        return UIEditorPage.CAPTION;
//    }

}
