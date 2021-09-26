package com.aliyun.svideo.editor.util;

import android.content.Context;

import com.aliyun.svideo.common.utils.FileUtils;

import java.io.File;

public class AliCacheUtils {


    /**
     * 清空阿里云缓存视频
     */
    public static void cleanVideoCaches(Context context){
        String path = context.getExternalFilesDir("") + File.separator + "Media" + File.separator;
        FileUtils.deleteFileDirectory(new File(path));

        String logPath = context.getExternalFilesDir("") + File.separator + "Log" + File.separator;
        FileUtils.deleteFileDirectory(new File(logPath));

        String jsonPath = context.getExternalFilesDir("") + File.separator + "project_json" + File.separator;
        FileUtils.deleteFileDirectory(new File(jsonPath));

    }

}
