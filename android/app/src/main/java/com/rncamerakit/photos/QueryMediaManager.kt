package com.rncamerakit.photos

import android.content.Context
import com.aliyun.svideo.media.JsonExtend.JSONSupportImpl
import com.aliyun.svideo.media.MediaInfo
import com.aliyun.svideo.media.MediaStorage
import com.facebook.react.uimanager.events.RCTEventEmitter


class QueryMediaManager {

    private var mListener: OnQueryMediaListener? = null

    fun setOnQueryMediaListener(listener: OnQueryMediaListener) {
        mListener = listener
    }

    abstract class OnQueryMediaListener {
        open fun onMediaDirChanged(photoList: MutableList<MediaInfo>, baseCount: Int) {}
        open fun onDataUpdate(photoList: MutableList<MediaInfo>, baseCount: Int) {}
        open fun onCompletion() {}
    }


    private var mMediaStorage: MediaStorage? = null

    fun initLoad(context: Context, sortMode: String?) {
        mMediaStorage = MediaStorage(context, JSONSupportImpl())

        if (sortMode == "video") {
            mMediaStorage?.setSortMode(MediaStorage.SORT_MODE_VIDEO)
        } else if (sortMode == "photo") {
            mMediaStorage?.setSortMode(MediaStorage.SORT_MODE_PHOTO)
        } else {
            //显示视频和图片
            mMediaStorage?.setSortMode(MediaStorage.SORT_MODE_MERGE)
        }

        //设置显示的最小时长和最大时长
        mMediaStorage?.setVideoDurationRange(2000, 100*60*1000)

        mMediaStorage?.setOnMediaDirChangeListener {
            //相册切换
            var mediaInfoList: MutableList<MediaInfo>? = null
            val dir = mMediaStorage?.currentDir
            if (dir != null && dir.id != -1) {
                mediaInfoList = if (dir.id == -1) {
                    //相机胶卷
                    mMediaStorage?.medias
                } else {
                    mMediaStorage?.findMediaByDir(dir)
                }
            }
            if (mediaInfoList != null) {
                mListener?.onMediaDirChanged(mediaInfoList, mediaInfoList.size)
            }
        }

        mMediaStorage?.setOnMediaDataUpdateListener { list ->
            //更新
            mMediaStorage?.medias?.size?.let { mListener?.onDataUpdate(list, it) }
        }
        mMediaStorage?.setOnCompletionListener {
            //加载完成
            mListener?.onCompletion()
        }


        try {
            //开始加载
            mMediaStorage?.startFetchMedias()
        } catch (e: SecurityException) {
        }

    }

    fun onDestroy() {
        mMediaStorage?.saveCurrentDirToCache()
        mMediaStorage?.cancelTask()
    }

}