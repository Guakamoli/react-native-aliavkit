package com.rncamerakit.recorder.manager

import android.media.MediaPlayer
import android.text.TextUtils
import com.facebook.react.bridge.Promise

class MediaPlayerManage private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = MediaPlayerManage()
    }

    private var mMediaPlayer: MediaPlayer? = null
    private var duration = 0
    private var isPlaying = false

    init {
        mMediaPlayer = MediaPlayer()
    }

    fun start(path: String,promise: Promise) {
        release()
        if (TextUtils.isEmpty(path)) {
            promise.reject("playMusic","error: musicPath is empty")
            return
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
        }

        if (mMediaPlayer!!.isPlaying) {
            stop()
        }
        mMediaPlayer?.setDataSource(path)
        mMediaPlayer?.prepare()
        mMediaPlayer?.setOnPreparedListener {
            mMediaPlayer?.start()
            promise.resolve(true)
        }

        //循环播放
        mMediaPlayer?.isLooping = true

//        //播放结束回调
//        mMediaPlayer?.setOnCompletionListener {
//            it.reset()
//        }

        duration = mMediaPlayer!!.duration
        mMediaPlayer?.start()
    }


    private fun stop() {
        this.isPlaying = false
        this.duration = 0
        if(mMediaPlayer?.isPlaying == true){
            mMediaPlayer?.stop()
            mMediaPlayer?.reset()
        }
    }


    fun resume() {
        isPlaying = true
        mMediaPlayer?.start()
    }

    fun pause() {
        isPlaying = false
        mMediaPlayer?.pause()
    }

    fun release() {
        stop()
        mMediaPlayer?.setOnPreparedListener(null)
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

}