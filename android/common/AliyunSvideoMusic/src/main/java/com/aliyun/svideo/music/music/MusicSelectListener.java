package com.aliyun.svideo.music.music;

import com.aliyun.svideo.base.http.MusicFileBean;

public interface MusicSelectListener {
    default void onShowMusicVolume(){
    }
    void onMusicSelect(MusicFileBean musicFileBean, long startTime);
    void onCancel();
}
