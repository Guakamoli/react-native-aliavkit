package com.aliyun.svideo.common.widget;

import android.content.Context;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import com.aliyun.svideo.common.R;
import com.bumptech.glide.load.engine.Resource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Objects;

/**
 * 音量调节进度条
 */
public class MusicVolumeChangeDialog extends BaseBottomFragment {

    private FragmentActivity mActivity;

    private Context mContext;

    private AudioManager mAudioManager;

    private int mMaxMusicVolume;//最大音量


    private int mStartCurrentMusicVolume;//初始音量

    private int mCurrentMusicVolume;//当前音量


    private Bitmap mBlurBitmap;

    private ConstraintLayout bastLayout;
    private SeekBar musicSeekBar;

    private ImageView imgClose;


    public MusicVolumeChangeDialog(FragmentActivity activity) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        setState(BottomSheetBehavior.STATE_EXPANDED);
        setSkipCollapsed(true);
    }

    @Override
    protected int getStyle() {
        return R.style.BaseBottomFragmentTransparentStyle;
    }


    @Override
    protected int getContentView() {
        return R.layout.dialog_bottom_music_volume_change;
    }

    private void initData() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        getMaxMusicVolume();
        getCurrentMusicVolume();
        mStartCurrentMusicVolume = mCurrentMusicVolume;
        registerVolumeChangeReceiver();
    }

    private void initBackground(View view) {
//        Bitmap bmp=Bitmap.createBitmap(720,1080, Bitmap.Config.ARGB_8888);
//        Canvas canvas=new Canvas(bmp);
//        canvas.drawColor(Color.parseColor("#80FFFFFF"));
//        mBlurBitmap = BlurBitmap.blur(mContext, bmp);
//        setWindowBackground(new BitmapDrawable(mContext.getResources(),bmp));


        setWindowBackground(Color.parseColor("#70FFFFFF"));

//        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.paiya_music_volume_dialog_bg);
//        setWindowBackground(new BitmapDrawable(mContext.getResources(),bmp));
    }



    @Override
    public void onDestroyView() {
//        setMusicVolume(mStartCurrentMusicVolume);
        if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
            mBlurBitmap.recycle();
        }
        unregisterVolumeChangeReceiver();
        super.onDestroyView();
    }

    @Override
    protected void initView(View view) {
        initBackground(view);
        initData();

        musicSeekBar = view.findViewById(R.id.musicSeekBar);
        musicSeekBar.setMax(mMaxMusicVolume);

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentMusicVolume = seekBar.getProgress();
                setMusicVolume(mCurrentMusicVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMusicVolumeCallback != null) {
                    mMusicVolumeCallback.getMusicWeight(getMusicVolumeWeight(), mStartCurrentMusicVolume);
                }
            }
        });

        view.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });
        setSeekBarProgress(false);
    }

    /**
     * 设置音量进度
     */
    private void setSeekBarProgress(boolean animate) {
        if (mCurrentMusicVolume >= 0 && musicSeekBar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                musicSeekBar.setProgress(mCurrentMusicVolume, animate);
            } else {
                musicSeekBar.setProgress(mCurrentMusicVolume);
            }
        }
    }

    /**
     * 获取音量。按最大值100获取
     */
    private int getMusicVolumeWeight() {
        int progress;
        if (musicSeekBar != null) {
            progress = musicSeekBar.getProgress();
        } else {
            progress = mCurrentMusicVolume;
        }
        float progressF = 50f;
        if (mMaxMusicVolume > 0) {
            progressF = progress * 100f / mMaxMusicVolume;
        }
        return (int) progressF;
    }

    /**
     * 获取最大音乐音量
     */
    private int getMaxMusicVolume() {
        if (mAudioManager != null) {
            mMaxMusicVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        return mMaxMusicVolume;
    }

    /**
     * 获取当前音乐音量
     */
    private int getCurrentMusicVolume() {
        if (mAudioManager != null) {
            mCurrentMusicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        return mCurrentMusicVolume;
    }

    /**
     * 设置音乐音量
     */
    private void setMusicVolume(int currentMusicVolume) {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, 0);
        }
    }


    private SettingsContentObserver mSettingsContentObserver;

    private void registerVolumeChangeReceiver() {
        if (mContext == null) {
            return;
        }
        mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
        mContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
    }

    private void unregisterVolumeChangeReceiver() {
        if (mContext != null && mSettingsContentObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        }
    }

    private class SettingsContentObserver extends ContentObserver {
        Context context;

        public SettingsContentObserver(Context c, Handler handler) {
            super(handler);
            context = c;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getCurrentMusicVolume();
            setSeekBarProgress(true);
            if (mMusicVolumeCallback != null) {
                mMusicVolumeCallback.getMusicWeight(getMusicVolumeWeight(), mStartCurrentMusicVolume);
            }
        }
    }


    public static MusicVolumeChangeDialog show(FragmentActivity activity) {
        MusicVolumeChangeDialog inputFragment = new MusicVolumeChangeDialog(activity);
        inputFragment.show(activity.getSupportFragmentManager(), "dialog");
        return inputFragment;
    }


    private OnMusicVolumeCallback mMusicVolumeCallback;

    public void setOnMusicVolumeCallback(OnMusicVolumeCallback callback) {
        this.mMusicVolumeCallback = callback;
    }

    public interface OnMusicVolumeCallback {
        void getMusicWeight(int musicWeight, int startMusicWeight);
    }
}
