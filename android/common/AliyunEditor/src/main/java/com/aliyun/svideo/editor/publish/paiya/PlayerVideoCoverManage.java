package com.aliyun.svideo.editor.publish.paiya;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.aliyun.svideosdk.player.AliyunISVideoPlayer;
import com.aliyun.svideosdk.player.PlayerCallback;
import com.aliyun.svideosdk.player.impl.AliyunSVideoPlayerCreator;

/**
 * 播放封面
 */
public class PlayerVideoCoverManage implements TextureView.SurfaceTextureListener {

    private Context mContext;
    private String mVideoPath;
    /**
     * sdk提供的播放器，支持非关键帧的实时预览
     */
    private AliyunISVideoPlayer mPlayer;
    private Surface mSurface;

    public PlayerVideoCoverManage(Context context, String videoPath) {
        this.mContext = context;
        this.mVideoPath = videoPath;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mPlayer == null) {
            mSurface = new Surface(surface);
            mPlayer = AliyunSVideoPlayerCreator.createPlayer();
            mPlayer.init(mContext);
            mPlayer.setDisplay(mSurface);
            mPlayer.setSource(mVideoPath);
            mPlayer.setPlayerCallback(new PlayerCallback() {
                @Override
                public void onPlayComplete() {

                }

                @Override
                public void onDataSize(int dataWidth, int dataHeight) {

                }

                @Override
                public void onError(int errorCode) {

                }
            });
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void seekTo(long time) {
        if (mPlayer != null) {
            mPlayer.seek(time);
        }
    }

}
